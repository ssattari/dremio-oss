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
package com.dremio.service.coordinator.zk;

import com.dremio.exec.proto.CoordinationProtos.NodeEndpoint;
import com.dremio.service.coordinator.RegistrationHandle;
import org.apache.curator.x.discovery.ServiceInstance;

class ZKRegistrationHandle implements RegistrationHandle {
  static final org.slf4j.Logger logger =
      org.slf4j.LoggerFactory.getLogger(ZKRegistrationHandle.class);

  private final ZKServiceSet zkServiceSet;
  private final ServiceInstance<NodeEndpoint> serviceInstance;

  public ZKRegistrationHandle(
      ZKServiceSet zkServiceSet, ServiceInstance<NodeEndpoint> serviceInstance) {
    super();
    this.zkServiceSet = zkServiceSet;
    this.serviceInstance = serviceInstance;
  }

  public ServiceInstance<NodeEndpoint> getServiceInstance() {
    return serviceInstance;
  }

  @Override
  public void close() {
    this.zkServiceSet.unregister(this);
  }
}
