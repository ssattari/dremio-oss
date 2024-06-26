/*
 * Copyright (C) 2017-2019 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.exec.physical.impl.limit;

import static org.junit.Assert.assertEquals;

import com.dremio.BaseTestQuery;
import com.dremio.PlanTestBase;
import com.dremio.common.util.TestTools;
import com.dremio.exec.ExecConstants;
import org.junit.Ignore;
import org.junit.Test;

public class TestLimitWithExchanges extends BaseTestQuery {
  final String WORKING_PATH = TestTools.getWorkingPath();
  final String TEST_RES_PATH = WORKING_PATH + "/src/test/resources";

  @Ignore("DX-3872")
  @Test
  public void testLimitWithExchanges() throws Exception {
    testPhysicalFromFile("limit/limit_exchanges.json");
  }

  @Test
  public void testPushLimitPastUnionExchange() throws Exception {
    // Push limit past through UnionExchange.
    try {
      test("alter session set \"planner.slice_target\" = 1");
      test("alter session set \"planner.enable_trivial_singular\" = false");
      final String[] excludedPlan = {};

      // case 1. single table query.
      final String sql =
          String.format(
              "select * from dfs_root.\"%s/multilevel/json\" limit 1 offset 2", TEST_RES_PATH);
      final String[] expectedPlan = {
        "(?s)Limit\\(offset=\\[2\\], fetch=\\[1\\].*UnionExchange.*Limit\\(fetch=\\[3\\]\\).*Scan"
      };
      testLimitHelper(sql, expectedPlan, excludedPlan, 1);

      final String sql2 =
          String.format(
              "select * from dfs_root.\"%s/multilevel/json\" limit 1 offset 0", TEST_RES_PATH);
      final String[] expectedPlan2 = {
        "(?s)Limit\\(offset=\\[0\\], fetch=\\[1\\].*UnionExchange.*Limit\\(fetch=\\[1\\]\\).*Scan"
      };
      testLimitHelper(sql2, expectedPlan2, excludedPlan, 1);

      final String sql3 =
          String.format("select * from dfs_root.\"%s/multilevel/json\" limit 1", TEST_RES_PATH);
      final String[] expectedPlan3 = {
        "(?s)Limit\\(fetch=\\[1\\].*UnionExchange.*Limit\\(fetch=\\[1\\]\\).*Scan"
      };
      testLimitHelper(sql3, expectedPlan3, excludedPlan, 1);

      // case 2: join query.
      final String sql4 =
          String.format(
              "select * from dfs_root.\"%s/tpchmulti/region\" r,  dfs_root.\"%s/tpchmulti/nation\" n "
                  + "where r.r_regionkey = n.n_regionkey limit 1 offset 2",
              TEST_RES_PATH, TEST_RES_PATH);

      final String[] expectedPlan4 = {
        "(?s)Limit\\(offset=\\[2\\], fetch=\\[1\\].*UnionExchange.*Limit\\(fetch=\\[3\\]\\).*Join"
      };

      testLimitHelper(sql4, expectedPlan4, excludedPlan, 1);

      final String sql5 =
          String.format(
              "select * from dfs_root.\"%s/tpchmulti/region\" r,  dfs_root.\"%s/tpchmulti/nation\" n "
                  + "where r.r_regionkey = n.n_regionkey limit 1",
              TEST_RES_PATH, TEST_RES_PATH);

      final String[] expectedPlan5 = {
        "(?s)Limit\\(fetch=\\[1\\].*UnionExchange.*Limit\\(fetch=\\[1\\]\\).*Join"
      };
      testLimitHelper(sql5, expectedPlan5, excludedPlan, 1);
    } finally {
      test("alter session set \"planner.enable_trivial_singular\" = true");
      test(
          "alter session set \"planner.slice_target\" = "
              + ExecConstants.SLICE_TARGET_OPTION.getDefault().getValue());
    }
  }

  @Test
  public void testNegPushLimitPastUnionExchange() throws Exception {
    // Negative case: should not push limit past through UnionExchange.
    try {
      test("alter session set \"planner.slice_target\" = 1");
      final String[] expectedPlan = {};

      // case 1. Only "offset", but no "limit" : should not push "limit" down.
      final String sql =
          String.format("select * from dfs_root.\"%s/tpchmulti/region\" offset 2", TEST_RES_PATH);
      final String[] excludedPlan = {"(?s)Limit\\(offset=\\[2\\].*UnionExchange.*Limit.*Scan"};

      // case 2. "limit" is higher than # of rowcount in table : should not push "limit" down.
      final String sql2 =
          String.format("select * from dfs_root.\"%s/tpchmulti/region\" limit 100", TEST_RES_PATH);
      final String[] excludedPlan2 = {"(?s)Limit\\(fetch=\\[100\\].*UnionExchange.*Limit.*Scan"};

      testLimitHelper(sql2, expectedPlan, excludedPlan2, 5);
    } finally {
      test(
          "alter session set \"planner.slice_target\" = "
              + ExecConstants.SLICE_TARGET_OPTION.getDefault().getValue());
    }
  }

  private void testLimitHelper(
      final String sql,
      final String[] expectedPlan,
      final String[] excludedPattern,
      int expectedRecordCount)
      throws Exception {
    // Validate the plan
    PlanTestBase.testPlanMatchingPatterns(sql, expectedPlan, excludedPattern);

    // Validate row count
    final int actualRecordCount = testSql(sql);
    assertEquals(
        String.format(
            "Received unexpected number of rows in output: expected=%d, received=%s",
            expectedRecordCount, actualRecordCount),
        expectedRecordCount,
        actualRecordCount);
  }
}
