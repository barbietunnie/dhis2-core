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
package org.hisp.dhis.dxf2.monitoring;

import java.util.Date;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.external.conf.ConfigurationKey;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.setting.SystemSettingsService;
import org.hisp.dhis.system.SystemInfo;
import org.hisp.dhis.system.SystemService;
import org.hisp.dhis.system.util.HttpHeadersBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Lars Helge Overland
 */
@Slf4j
@RequiredArgsConstructor
@Service("org.hisp.dhis.dxf2.monitoring.MonitoringService")
public class DefaultMonitoringService implements MonitoringService {
  private static final int PUSH_INTERVAL = DateTimeConstants.MILLIS_PER_MINUTE * 5;

  private static final int PUSH_INITIAL_DELAY = DateTimeConstants.MILLIS_PER_SECOND * 30;

  private final SystemService systemService;

  private final DhisConfigurationProvider config;

  private final SystemSettingsService settingsService;

  private final RestTemplate restTemplate;

  private final TaskScheduler scheduler;

  @PostConstruct
  public void init() {
    Date date = new DateTime().plus(PUSH_INITIAL_DELAY).toDate();

    String url = config.getProperty(ConfigurationKey.SYSTEM_MONITORING_URL);

    if (StringUtils.isNotBlank(url)) {
      log.info(String.format("Monitoring service configured, URL: %s", url));
    }

    scheduler.scheduleWithFixedDelay(this::pushMonitoringInfo, date, PUSH_INTERVAL);

    log.info("Scheduled monitoring service");
  }

  @Override
  public void pushMonitoringInfo() {
    MonitoringTarget target = getMonitoringTarget();
    if (StringUtils.isBlank(target.getUrl())) {
      log.debug("Monitoring service URL not configured, aborting monitoring request");
      return;
    }

    SystemInfo systemInfo = systemService.getSystemInfo().withoutSensitiveInfo();

    if (StringUtils.isBlank(systemInfo.getSystemId())) {
      log.warn("System ID not available, aborting monitoring request");
      return;
    }

    pushSystemInfo(systemInfo, target);
  }

  /**
   * Returns the monitoring target instance URL and credentials.
   *
   * @return the {@link MonitoringTarget}.
   */
  private MonitoringTarget getMonitoringTarget() {
    return new MonitoringTarget(
        config.getProperty(ConfigurationKey.SYSTEM_MONITORING_URL),
        config.getProperty(ConfigurationKey.SYSTEM_MONITORING_USERNAME),
        config.getProperty(ConfigurationKey.SYSTEM_MONITORING_PASSWORD));
  }

  /**
   * Pushes system info to the monitoring target.
   *
   * @param systemInfo the {@link SystemInfo}.
   * @param target the {@link MonitoringTarget}.
   */
  private void pushSystemInfo(SystemInfo systemInfo, MonitoringTarget target) {

    HttpHeadersBuilder headersBuilder = new HttpHeadersBuilder().withContentTypeJson();

    if (StringUtils.isNotBlank(target.getUsername())
        && StringUtils.isNotBlank(target.getPassword())) {
      headersBuilder.withBasicAuth(target.getUsername(), target.getPassword());
    }

    Date startTime = new Date();

    HttpEntity<SystemInfo> requestEntity = new HttpEntity<>(systemInfo, headersBuilder.build());

    ResponseEntity<String> response = null;
    HttpStatusCode sc = null;

    try {
      response = restTemplate.postForEntity(target.getUrl(), requestEntity, String.class);
      sc = response.getStatusCode();
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.warn(String.format("Monitoring request failed, status code: %s", sc), ex);
      return;
    } catch (ResourceAccessException ex) {
      log.info("Monitoring request failed, network is unreachable");
      return;
    }

    if (sc.is2xxSuccessful()) {
      settingsService.put("keyLastSuccessfulSystemMonitoringPush", startTime);

      log.debug(String.format("Monitoring request successfully sent, URL: %s", target.getUrl()));
    } else {
      log.warn(String.format("Monitoring request failed with status code: %s", sc));
    }
  }

  @Getter
  @RequiredArgsConstructor
  private static class MonitoringTarget {
    private final String url;
    private final String username;
    private final String password;
  }
}
