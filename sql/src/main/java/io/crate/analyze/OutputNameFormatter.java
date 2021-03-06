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

import io.crate.sql.ExpressionFormatter;
import io.crate.sql.tree.ArrayComparisonExpression;
import io.crate.sql.tree.Expression;
import io.crate.sql.tree.QualifiedNameReference;
import io.crate.sql.tree.SubqueryExpression;
import io.crate.sql.tree.SubscriptExpression;

import java.util.List;
import java.util.NoSuchElementException;

public class OutputNameFormatter {

    private static final InnerOutputNameFormatter INSTANCE = new InnerOutputNameFormatter();

    public static String format(Expression expression) {
        return INSTANCE.process(expression, null);
    }

    private static class InnerOutputNameFormatter extends ExpressionFormatter.Formatter {
        @Override
        protected String visitQualifiedNameReference(QualifiedNameReference node, Void context) {
            List<String> parts = node.getName().getParts();
            if (parts.isEmpty()) {
                throw new NoSuchElementException("Parts of QualifiedNameReference are empty: " + node.getName());
            }
            return parts.get(parts.size() - 1);
        }

        @Override
        protected String visitSubscriptExpression(SubscriptExpression node, Void context) {
            return process(node.name(), null) + '[' + process(node.index(), null) + ']';
        }

        @Override
        public String visitArrayComparisonExpression(ArrayComparisonExpression node, Void context) {
            return process(node.getLeft(), null) + ' ' +
                   node.getType().getValue() + ' ' +
                   node.quantifier().name() + '(' +
                   process(node.getRight(), null) + ')';
        }

        @Override
        protected String visitSubqueryExpression(SubqueryExpression node, Void context) {
            return super.visitSubqueryExpression(node, context).replace("\n", "");
        }
    }
}
