/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.bootstrap.CommonConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.session.remoting.DataNodeNotifyExchanger;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.AbstractMetaServerService;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 20:05 yuzhi.lyz Exp $
 */
public class MetaServerServiceImpl extends AbstractMetaServerService<BaseHeartBeatResponse> {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private SlotTableCache slotTableCache;

  @Autowired private DataNodeExchanger dataNodeExchanger;

  @Autowired private DataNodeNotifyExchanger dataNodeNotifyExchanger;

  @Autowired private CommonConfig commonConfig;

  @Override
  protected long getCurrentSlotTableEpoch() {
    return slotTableCache.getEpoch();
  }

  @Override
  public int getRenewIntervalSecs() {
    return sessionServerConfig.getSchedulerHeartbeatIntervalSecs();
  }

  @Override
  protected void handleRenewResult(BaseHeartBeatResponse result) {
    Set<String> dataServerList = getDataServerList();
    if (dataServerList != null && !dataServerList.isEmpty()) {
      dataNodeNotifyExchanger.setServerIps(dataServerList);
      dataNodeNotifyExchanger.notifyConnectServerAsync();
      dataNodeExchanger.setServerIps(dataServerList);
      dataNodeExchanger.notifyConnectServerAsync();
    }
    if (result.getSlotTable() != null && result.getSlotTable() != SlotTable.INIT) {
      slotTableCache.updateSlotTable(result.getSlotTable());
    } else {
      RENEWER_LOGGER.warn("[handleRenewResult] no slot table result");
    }
  }

  @Override
  protected HeartbeatRequest createRequest() {
    return new HeartbeatRequest(
            createNode(),
            slotTableCache.getEpoch(),
            sessionServerConfig.getSessionServerDataCenter(),
            System.currentTimeMillis(),
            SlotConfig.slotBasicInfo())
        .setSlotTable(slotTableCache.getCurrentSlotTable());
  }

  @Override
  protected NodeType nodeType() {
    return NodeType.SESSION;
  }

  @Override
  protected String cell() {
    return commonConfig.getLocalRegion();
  }

  private Node createNode() {
    return new SessionNode(
        new URL(ServerEnv.IP), sessionServerConfig.getSessionServerRegion(), ServerEnv.PROCESS_ID);
  }

  @VisibleForTesting
  void setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
  }

  @VisibleForTesting
  void setSlotTableCache(SlotTableCache slotTableCache) {
    this.slotTableCache = slotTableCache;
  }

  @VisibleForTesting
  void setDataNodeExchanger(DataNodeExchanger dataNodeExchanger) {
    this.dataNodeExchanger = dataNodeExchanger;
  }

  @VisibleForTesting
  void setDataNodeNotifyExchanger(DataNodeNotifyExchanger dataNodeNotifyExchanger) {
    this.dataNodeNotifyExchanger = dataNodeNotifyExchanger;
  }
}
