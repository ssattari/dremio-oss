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
package com.dremio.exec.planner.fragment;

import com.dremio.exec.physical.EndpointAffinity;
import com.dremio.exec.physical.PhysicalOperatorSetupException;
import com.dremio.exec.proto.CoordinationProtos.NodeEndpoint;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;

/**
 * Implementation of {@link FragmentParallelizer} where fragment requires running on a given set of
 * endpoints. Width per node is depended on the affinity to the endpoint and total width (calculated
 * using costs)
 */
public class HardAffinityFragmentParallelizer implements FragmentParallelizer {
  private static final Logger logger =
      org.slf4j.LoggerFactory.getLogger(HardAffinityFragmentParallelizer.class);

  public static final HardAffinityFragmentParallelizer INSTANCE =
      new HardAffinityFragmentParallelizer();

  private static String EOL = System.getProperty("line.separator");

  private HardAffinityFragmentParallelizer() {
    /* singleton */
  }

  @Override
  public void parallelizeFragment(
      final Wrapper fragmentWrapper,
      final ParallelizationParameters parameters,
      final Collection<NodeEndpoint> activeEndpoints)
      throws PhysicalOperatorSetupException {

    final Stats stats = fragmentWrapper.getStats();
    final ParallelizationInfo pInfo = stats.getParallelizationInfo();

    int totalMaxWidth = 0;

    // Go through the affinity map and extract the endpoints that have mandatory assignment
    // requirement
    final Map<NodeEndpoint, EndpointAffinity> endpointPool = Maps.newHashMap();
    for (Entry<NodeEndpoint, EndpointAffinity> entry : pInfo.getEndpointAffinityMap().entrySet()) {
      if (entry.getValue().isAssignmentRequired()) {
        endpointPool.put(entry.getKey(), entry.getValue());

        // Limit the max width of the endpoint to allowed max width.
        totalMaxWidth += Math.min(parameters.getMaxWidthPerNode(), entry.getValue().getMaxWidth());
        if (totalMaxWidth < 0) {
          // If the totalWidth overflows, just keep it at the max value.
          totalMaxWidth = Integer.MAX_VALUE;
        }
      }
    }

    // Step 1: Find the width taking into various parameters
    // 1.1. Find the parallelization based on cost. Use max cost of all operators in this fragment;
    // this is consistent
    //      with the calculation that ExcessiveExchangeRemover uses.
    int width = (int) Math.ceil(stats.getMaxCost() / parameters.getSliceTarget());

    // 1.2. Make sure the width is at least the number of endpoints that require an assignment
    width = Math.max(endpointPool.size(), width);

    // 1.3. Cap the parallelization width by fragment level width limit and system level per query
    // width limit
    width = Math.max(1, Math.min(width, pInfo.getMaxWidth()));
    checkOrThrow(
        endpointPool.size() <= width,
        logger,
        "Number of mandatory endpoints ({}) that require an assignment is more than the allowed fragment max "
            + "width ({}).",
        endpointPool.size(),
        pInfo.getMaxWidth());

    // 1.5 Cap the parallelization width by max allowed parallelization per node
    width = Math.max(1, Math.min(width, endpointPool.size() * parameters.getMaxWidthPerNode()));

    // 1.6 Cap the parallelization width by total of max allowed width per node. The reason is if we
    // the width is more,
    // we end up allocating more work units to one or more endpoints that don't have those many work
    // units.
    width = Math.min(totalMaxWidth, width);

    // Step 2: Select the endpoints
    final Map<NodeEndpoint, Integer> endpoints = Maps.newHashMap();

    // 2.1 First add each endpoint from the pool once so that the mandatory assignment requirement
    // is fulfilled.
    for (Entry<NodeEndpoint, EndpointAffinity> entry : endpointPool.entrySet()) {
      endpoints.put(entry.getKey(), 1);
    }
    int totalAssigned = endpoints.size();

    // Normalize the affinities
    double totalAffinity = 1.0;
    for (EndpointAffinity epAff : endpointPool.values()) {
      totalAffinity += epAff.getAffinity();
    }

    // 2.2 Assign the remaining slots to endpoints proportional to the affinity of each endpoint
    int remainingSlots = width - endpoints.size();
    while (remainingSlots > 0) {
      for (EndpointAffinity epAf : endpointPool.values()) {
        final int moreAllocation =
            (int) Math.ceil((epAf.getAffinity() / totalAffinity) * remainingSlots);
        int currentAssignments = endpoints.get(epAf.getEndpoint());
        for (int i = 0;
            i < moreAllocation
                && totalAssigned < width
                && currentAssignments < parameters.getMaxWidthPerNode()
                && currentAssignments < epAf.getMaxWidth();
            i++) {
          totalAssigned++;
          currentAssignments++;
        }
        endpoints.put(epAf.getEndpoint(), currentAssignments);
      }
      final int previousRemainingSlots = remainingSlots;
      remainingSlots = width - totalAssigned;
      if (previousRemainingSlots == remainingSlots) {
        logger.error(
            "Can't parallelize fragment: "
                + "Every mandatory node has exhausted the maximum width per node limit."
                + EOL
                + "Endpoint pool: {}"
                + EOL
                + "Assignment so far: {}"
                + EOL
                + "Width: {}",
            endpointPool,
            endpoints,
            width);
        throw new PhysicalOperatorSetupException("Can not parallelize fragment.");
      }
    }

    final List<NodeEndpoint> assignedEndpoints = Lists.newArrayList();
    for (Entry<NodeEndpoint, Integer> entry : endpoints.entrySet()) {
      for (int i = 0; i < entry.getValue(); i++) {
        assignedEndpoints.add(entry.getKey());
      }
    }

    fragmentWrapper.setWidth(width);
    fragmentWrapper.assignEndpoints(parameters, assignedEndpoints);
  }

  @Override
  public int getIdealFragmentWidth(
      final Wrapper fragment, final ParallelizationParameters parameters) {
    // Ideal fragment width doesn't matter for hard affinity. The node assignment will be done based
    // on the
    // full set of endpoints available, not based on a best fit subset.
    return 1;
  }

  private static void checkOrThrow(
      final boolean expr, final Logger logger, final String errMsg, Object... args)
      throws PhysicalOperatorSetupException {
    if (!expr) {
      logger.error(errMsg, args);
      throw new PhysicalOperatorSetupException("Can not parallelize fragment.");
    }
  }
}
