/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.leader.election;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hisp.dhis.external.conf.ConfigurationKey;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;

/**
 * No operation leader election implementation which will be used when redis is not configured.
 *
 * @author Ameen Mohamed
 */
@Slf4j
public class NoOpLeaderManager implements LeaderManager {

  private final String nodeUuid;
  private final String nodeId;

  public NoOpLeaderManager(DhisConfigurationProvider dhisConfigurationProvider) {
    this.nodeId = dhisConfigurationProvider.getProperty(ConfigurationKey.NODE_ID);
    this.nodeUuid = UUID.randomUUID().toString();
    log.info(
        "Initialized NoOp leader manager with node UUID: '{}' and node ID: '{}'", nodeUuid, nodeId);
  }

  @Override
  public void renewLeader(int ttlSeconds) {
    // No operation
  }

  @Override
  public void electLeader(int ttlSeconds) {
    // No operation
  }

  @Override
  public boolean isLeader() {
    return true;
  }

  @Override
  public String getCurrentNodeUuid() {
    return this.nodeUuid;
  }

  @Override
  public String getLeaderNodeUuid() {
    return this.nodeUuid;
  }

  @Override
  public String getLeaderNodeId() {
    return this.nodeId;
  }
}
