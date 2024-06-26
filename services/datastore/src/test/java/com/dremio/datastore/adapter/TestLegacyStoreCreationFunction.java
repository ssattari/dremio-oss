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
package com.dremio.datastore.adapter;

import com.dremio.datastore.api.LegacyKVStoreCreationFunction;
import com.dremio.datastore.format.Format;
import java.util.List;

/** Exposes KeyFormat for test harness logic. */
public interface TestLegacyStoreCreationFunction<K, V> extends LegacyKVStoreCreationFunction<K, V> {

  Format<K> getKeyFormat();

  List<Class<?>> getKeyClasses();
}
