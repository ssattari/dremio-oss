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
package com.dremio.exec.store.sys;

import com.dremio.exec.calcite.logical.ScanCrel;
import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.planner.logical.Rel;
import com.dremio.exec.store.common.SourceLogicalConverter;
import org.apache.calcite.plan.RelOptRuleCall;

public class SystemScanDrule extends SourceLogicalConverter {

  public SystemScanDrule(SourceType type) {
    super(type);
  }

  @Override
  public Rel convertScan(ScanCrel scan) {
    return new SystemScanDrel(
        scan.getCluster(),
        scan.getTraitSet().plus(Rel.LOGICAL),
        scan.getTable(),
        scan.getPluginId(),
        scan.getTableMetadata(),
        scan.getProjectedColumns(),
        scan.getObservedRowcountAdjustment(),
        scan.getHints());
  }

  @Override
  public boolean matches(RelOptRuleCall call) {
    return super.matches(call);
  }
}
