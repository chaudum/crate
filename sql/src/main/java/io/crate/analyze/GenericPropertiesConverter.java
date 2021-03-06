/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.analyze;

import io.crate.analyze.expressions.ExpressionToStringVisitor;
import io.crate.data.Row;
import io.crate.sql.tree.ArrayLiteral;
import io.crate.sql.tree.Expression;
import io.crate.sql.tree.GenericProperties;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class GenericPropertiesConverter {

    private static final String INVALID_SETTING_MESSAGE = "setting '%s' not supported";

    /**
     * Put a genericProperty into a settings-structure
     */
    static void genericPropertyToSetting(Settings.Builder builder,
                                         String name,
                                         Expression value,
                                         Row parameters) {
        if (value instanceof ArrayLiteral) {
            ArrayLiteral array = (ArrayLiteral) value;
            List<String> values = new ArrayList<>(array.values().size());
            for (Expression expression : array.values()) {
                values.add(ExpressionToStringVisitor.convert(expression, parameters));
            }
            builder.putList(name, values.toArray(new String[0]));
        } else {
            builder.put(name, ExpressionToStringVisitor.convert(value, parameters));
        }
    }

    private static void genericPropertiesToSettings(Settings.Builder builder,
                                                    GenericProperties genericProperties,
                                                    Row parameters) {
        for (Map.Entry<String, Expression> entry : genericProperties.properties().entrySet()) {
            genericPropertyToSetting(builder, entry.getKey(), entry.getValue(), parameters);
        }
    }

    static Settings genericPropertiesToSettings(GenericProperties genericProperties, Row parameters) {
        Settings.Builder builder = Settings.builder();
        genericPropertiesToSettings(builder, genericProperties, parameters);
        return builder.build();
    }


    static Settings.Builder settingsFromProperties(GenericProperties properties,
                                                   Row parameters,
                                                   Map<String, Setting> supportedSettings) {

        return settingsFromProperties(properties, parameters, supportedSettings, true);
    }

    public static Settings.Builder settingsFromProperties(GenericProperties properties,
                                                          Row parameters,
                                                          Map<String, Setting> supportedSettings,
                                                          boolean setDefaults) {

        Settings.Builder builder = Settings.builder();
        settingsFromProperties(builder, properties, parameters, supportedSettings, setDefaults, (s) -> false, INVALID_SETTING_MESSAGE);
        return builder;
    }

    static void settingsFromProperties(Settings.Builder builder,
                                       GenericProperties properties,
                                       Row parameters,
                                       Map<String, Setting> supportedSettings,
                                       boolean setDefaults,
                                       Predicate<String> ignoreProperty,
                                       String invalidMessage) {
        if (setDefaults) {
            setDefaults(builder, supportedSettings);
        }
        if (!properties.isEmpty()) {
            for (Map.Entry<String, Expression> entry : properties.properties().entrySet()) {
                String settingName = entry.getKey();
                if (ignoreProperty.test(settingName)) {
                    continue;
                }
                Setting<?> setting = supportedSettings.get(settingName);
                if (setting == null) {
                    throw new IllegalArgumentException(String.format(Locale.ENGLISH, invalidMessage, entry.getKey()));
                }
                Settings.Builder singleSettingBuilder = Settings.builder();
                genericPropertyToSetting(singleSettingBuilder, setting.getKey(), entry.getValue(), parameters);
                Object value = setting.get(singleSettingBuilder.build());
                if (value instanceof Settings) {
                    builder.put((Settings) value);
                } else {
                    builder.put(setting.getKey(), value.toString());
                }
            }
        }
    }

    private static void setDefaults(Settings.Builder builder, Map<String, Setting> supportedSettings) {
        for (Map.Entry<String, Setting> entry : supportedSettings.entrySet()) {
            Setting setting = entry.getValue();
            Object value = setting.getDefault(Settings.EMPTY);
            if (value instanceof Settings) {
                builder.put((Settings) value);
            } else {
                builder.put(setting.getKey(), value.toString());
            }
        }
    }
}
