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
package com.dremio.common.graph;

import com.dremio.common.logical.UnexpectedOperatorType;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Graph<G extends GraphValue<G>, R extends G, T extends G> {

  static final Logger logger = LoggerFactory.getLogger(Graph.class);

  private AdjacencyList<G> adjList;
  private final List<R> roots;
  private final List<T> leaves;

  public Graph(List<G> operators, Class<R> root, Class<T> leaf) {
    adjList = AdjacencyList.newInstance(operators);
    roots =
        checkOperatorType(
            adjList.getRootNodes(),
            root,
            String.format("Root nodes must be a subclass of %s.", root.getSimpleName()));
    leaves =
        checkOperatorType(
            adjList.getLeafNodes(),
            leaf,
            String.format("Leaf nodes must be a subclass of %s.", leaf.getSimpleName()));
  }

  public AdjacencyList<G> getAdjList() {
    return adjList;
  }

  public Collection<R> getRoots() {
    return roots;
  }

  public Collection<T> getLeaves() {
    return leaves;
  }

  public static <G extends GraphValue<G>, R extends G, T extends G> Graph<G, R, T> newGraph(
      List<G> operators, Class<R> root, Class<T> leaf) {
    return new Graph<>(operators, root, leaf);
  }

  @SuppressWarnings("unchecked")
  private static <G, O extends G> List<O> checkOperatorType(
      Collection<G> ops, Class<O> classIdentifier, String error) {
    for (G o : ops) {
      if (!classIdentifier.isAssignableFrom(o.getClass())) {
        throw new UnexpectedOperatorType(o, error);
      }
    }
    return (List<O>) ops;
  }
}
