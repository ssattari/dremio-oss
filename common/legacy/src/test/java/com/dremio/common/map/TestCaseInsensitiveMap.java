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
package com.dremio.common.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class TestCaseInsensitiveMap {

  @SuppressWarnings("CollectionIncompatibleType")
  @Test
  public void putAndGet() {
    final CaseInsensitiveMap<Integer> map = CaseInsensitiveMap.newConcurrentMap();
    assertTrue(map.isEmpty());
    map.put("DREMIO", 1);
    map.put("bABy", 2);
    map.put("dremio!", 3);

    assertTrue(!map.isEmpty());
    assertEquals(3, map.size());
    assertTrue(map.containsKey("dremio"));
    assertEquals(2, (long) map.get("BabY"));
    // invalid type
    assertNull(map.get(2));

    assertEquals(3, (long) map.remove("DremiO!"));
    assertEquals(2, map.size());

    assertTrue(map.containsValue(1));
  }

  @Test
  public void addAllFromAnother() {
    final Map<String, Integer> another = new HashMap<>();
    another.put("JuSt", 1);
    another.put("RUN", 2);
    another.put("it", 3);

    final Map<String, Integer> map = CaseInsensitiveMap.newConcurrentMap();
    map.putAll(another);
    assertEquals(3, map.size());
    assertTrue(map.containsKey("just"));
    assertEquals(2, (long) map.get("run"));
    assertEquals(3, (long) map.remove("IT"));

    final Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    assertEquals(2, entrySet.size());
  }
}
