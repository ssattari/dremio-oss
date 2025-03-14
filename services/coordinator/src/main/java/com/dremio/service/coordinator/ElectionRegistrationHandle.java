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
package com.dremio.service.coordinator;

/** Provides number of participants for elections */
public interface ElectionRegistrationHandle extends AutoCloseable {
  /**
   * Get the number of election participants in this Election.
   *
   * @return
   */
  int instanceCount();

  /**
   * Unregister the handle
   *
   * <p>Close the handle, causing elections to close
   */
  @Override
  void close();

  /**
   * Expose a object that clients can use to synchronize access to the election
   *
   * @return
   */
  Object synchronizer();
}
