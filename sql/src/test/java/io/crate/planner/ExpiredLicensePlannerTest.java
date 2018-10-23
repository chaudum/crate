/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.planner;

import io.crate.metadata.RelationName;
import io.crate.planner.statement.SetLicensePlan;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;

public class ExpiredLicensePlannerTest extends CrateDummyClusterServiceUnitTest {

    private static String EXPIRED_LICENSE_ERROR_MESSAGE = "Statement is not allowed. License is now expired";

    private SQLExecutor e;

    @Before
    public void prepare() throws IOException {
        e = SQLExecutor.builder(clusterService)
            .enableDefaultTables()
            .addView(new RelationName("doc", "v1"), "select * from users")
            .setHasValidLicense(false)
            .build();
    }

    @Test
    public void testSelectPlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("select id from users where id = 1");
    }

    @Test
    public void testInsertPlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("insert into users (id, name) values (42, 'Deep Thought')");
    }

    @Test
    public void testUpdatePlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("update users set name='Vogon lyric fan' where id = 1");
    }

    @Test
    public void testDeletePlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("delete from users where id = 1");
    }

    @Test
    public void testExplainPlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("explain analyze select id from users where id = 1");
    }

    @Test
    public void testCreateTablePlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("create table users2(name string)");
    }

    @Test
    public void testDropTablePlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("drop table users");
    }

    @Test
    public void testCopyPlanThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("copy users (name) to directory '/tmp'");
    }

    @Test
    public void testCreateViewThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("create view v2 as select * from users");
    }

    @Test
    public void testDropViewThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("drop view v1");
    }

    @Test
    public void testSetGlobalThrowsException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(EXPIRED_LICENSE_ERROR_MESSAGE);
        e.plan("set global transient stats.enabled=false,stats.jobs_log_size=0");
    }

    @Test
    public void testSetLicenseIsAllowed() {
        Plan setLicensePlan = e.plan("set license 'XXX'");
        assertThat(setLicensePlan, instanceOf(SetLicensePlan.class));
    }
}
