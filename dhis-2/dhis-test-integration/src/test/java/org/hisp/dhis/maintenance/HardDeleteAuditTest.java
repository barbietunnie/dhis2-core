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
package org.hisp.dhis.maintenance;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hisp.dhis.audit.Audit;
import org.hisp.dhis.audit.AuditQuery;
import org.hisp.dhis.audit.AuditService;
import org.hisp.dhis.audit.AuditType;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dbms.DbmsManager;
import org.hisp.dhis.maintenance.jdbc.JdbcMaintenanceStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@ActiveProfiles(profiles = {"test-audit"})
@Disabled("until we can inject dhis.conf property overrides")
class HardDeleteAuditTest extends PostgresIntegrationTestBase {

  private static final int TIMEOUT = 5;

  @Autowired private AuditService auditService;

  @Autowired private IdentifiableObjectManager manager;

  @Autowired private JdbcMaintenanceStore jdbcMaintenanceStore;

  @Autowired private TransactionTemplate transactionTemplate;

  @Autowired private DbmsManager dbmsManager;

  @Test
  void testHardDeleteTrackedEntity() {
    OrganisationUnit ou = createOrganisationUnit('A');
    TrackedEntityAttribute attribute = createTrackedEntityAttribute('A');
    TrackedEntityType trackedEntityType = createTrackedEntityType('O');
    manager.save(trackedEntityType);
    TrackedEntity trackedEntity = createTrackedEntity('A', ou, attribute, trackedEntityType);
    transactionTemplate.execute(
        status -> {
          manager.save(ou);
          manager.save(attribute);
          manager.save(trackedEntity);
          manager.delete(trackedEntity);
          dbmsManager.clearSession();
          return null;
        });
    final AuditQuery query =
        AuditQuery.builder().uid(Sets.newHashSet(trackedEntity.getUid())).build();
    await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> auditService.countAudits(query) > 0);
    List<Audit> audits = auditService.getAudits(query);
    assertEquals(2, audits.size());
    transactionTemplate.execute(
        status -> {
          jdbcMaintenanceStore.deleteSoftDeletedTrackedEntities();
          dbmsManager.clearSession();
          return null;
        });
    final AuditQuery deleteQuery =
        AuditQuery.builder()
            .uid(Sets.newHashSet(trackedEntity.getUid()))
            .auditType(Sets.newHashSet(AuditType.DELETE))
            .build();
    audits = auditService.getAudits(deleteQuery);
    await()
        .atMost(TIMEOUT, TimeUnit.SECONDS)
        .until(() -> auditService.countAudits(deleteQuery) > 0);
    assertEquals(1, audits.size());
    Audit audit = audits.get(0);
    assertEquals(AuditType.DELETE, audit.getAuditType());
    assertEquals(TrackedEntity.class.getName(), audit.getKlass());
    assertEquals(trackedEntity.getUid(), audit.getUid());
  }
}
