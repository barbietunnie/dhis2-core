/*
 * Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.analytics.generator.impl;

import org.hisp.dhis.analytics.generator.Generator;

/**
 * Set of behaviour and settings required by the test generation of
 * "/analytics/events/aggregate/{program}?" endpoint.
 */
public class EventAggregatedGenerator implements Generator {
  private String[] scenarios = new String[] {};

  public EventAggregatedGenerator() {}

  public EventAggregatedGenerator(String... scenarios) {
    this.scenarios = scenarios;
  }

  @Override
  public String[] getScenarios() {
    return scenarios;
  }

  @Override
  public int getMaxTestsPerClass() {
    return 4;
  }

  @Override
  public String getAction() {
    return "aggregate";
  }

  @Override
  public String getClassNamePrefix() {
    return "EventsAggregate";
  }

  @Override
  public String getScenarioFile() {
    return "event-aggregated.json";
  }

  @Override
  public String getActionDeclaration() {
    return "private final AnalyticsEventActions actions = new AnalyticsEventActions();";
  }

  @Override
  public String getPackage() {
    return "org.hisp.dhis.analytics.event.aggregate";
  }

  @Override
  public String getTopClassComment() {
    return "Groups e2e tests for \"/events/aggregate\" endpoint.";
  }

  @Override
  public boolean assertMetaData() {
    return true;
  }

  @Override
  public boolean assertRowIndex() {
    return false;
  }
}
