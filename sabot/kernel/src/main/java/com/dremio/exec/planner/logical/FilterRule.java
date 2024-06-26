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
package com.dremio.exec.planner.logical;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalFilter;

/**
 * Rule that converts a {@link org.apache.calcite.rel.logical.LogicalFilter} to a Dremio "filter"
 * operation.
 */
public class FilterRule extends RelOptRule {
  public static final RelOptRule INSTANCE = new FilterRule();

  private FilterRule() {
    super(RelOptHelper.any(LogicalFilter.class, Convention.NONE), "FilterRule");
  }

  @Override
  public void onMatch(RelOptRuleCall call) {
    final LogicalFilter filter = (LogicalFilter) call.rel(0);
    final RelNode input = filter.getInput();
    // final RelTraitSet traits = filter.getTraitSet().plus(Rel.LOGICAL);
    final RelNode convertedInput = convert(input, input.getTraitSet().plus(Rel.LOGICAL).simplify());
    call.transformTo(
        new FilterRel(
            filter.getCluster(),
            convertedInput.getTraitSet(),
            convertedInput,
            filter.getCondition()));
  }
}
