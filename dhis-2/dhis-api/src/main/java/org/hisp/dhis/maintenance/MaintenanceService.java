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

import org.hisp.dhis.common.event.ApplicationCacheClearedEvent;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.organisationunit.OrganisationUnit;

/**
 * @author Lars Helge Overland
 */
public interface MaintenanceService {
  String ID = MaintenanceService.class.getName();

  /**
   * Deletes data values registered with 0 as value and associated with data elements with sum as
   * aggregation operator.
   *
   * @return the number of deleted data values.
   */
  int deleteZeroDataValues();

  /**
   * Permanently deletes data values which have been soft deleted, i.e. data values where the
   * deleted property is true.
   *
   * @return the number of deleted data values.
   */
  int deleteSoftDeletedDataValues();

  /**
   * Permanently deletes events which have been soft-deleted, i.e. events where the deleted property
   * is true.
   *
   * @return the number of deleted events.
   */
  int deleteSoftDeletedEvents();

  /**
   * Permanently deletes relationships which have been soft deleted, i.e. relationships where the
   * deleted property is true.
   *
   * @return the number of deleted relationships.
   */
  int deleteSoftDeletedRelationships();

  /**
   * Permanently deletes Enrollments which have been soft deleted, i.e. Enrollments where the
   * deleted property is true.
   *
   * @return the number of deleted Enrollments.
   */
  int deleteSoftDeletedEnrollments();

  /**
   * Permanently deletes tracked entities which have been soft deleted, i.e. tracked entities where
   * the deleted property is true.
   *
   * @return the number of deleted tracked entities
   */
  int deleteSoftDeletedTrackedEntities();

  /** Deletes periods which are not associated with any other table. */
  void prunePeriods();

  /**
   * Prunes complete data set registrations, data approvals, data value audits and data values for
   * the given organisation unit.
   *
   * @param organisationUnit the organisation unit.
   * @return true if the data pruning took place, false if not permitted.
   */
  boolean pruneData(OrganisationUnit organisationUnit);

  /**
   * Prunes data and audit records related to the given data element
   *
   * @param dataElement the data element.
   * @return true if the data pruning took place, false if not permitted.
   */
  boolean pruneData(DataElement dataElement);

  /**
   * Deletes user accounts representing expired account invitations.
   *
   * @return the number of removed user invitations as a result of this operation.
   */
  int removeExpiredInvitations();

  /**
   * Emits an {@link ApplicationCacheClearedEvent} which relevant caches can listen and react to.
   */
  void clearApplicationCaches();
}
