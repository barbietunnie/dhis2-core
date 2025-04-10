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
package org.hisp.dhis.tracker.deduplication;

import com.google.gson.JsonObject;
import org.hisp.dhis.test.e2e.Constants;
import org.hisp.dhis.test.e2e.TestRunStorage;
import org.hisp.dhis.test.e2e.actions.UserActions;
import org.hisp.dhis.test.e2e.actions.tracker.PotentialDuplicatesActions;
import org.hisp.dhis.test.e2e.actions.tracker.TrackerImportExportActions;
import org.hisp.dhis.test.e2e.dto.TrackerApiResponse;
import org.hisp.dhis.test.e2e.utils.DataGenerator;
import org.hisp.dhis.tracker.TrackerApiTest;
import org.hisp.dhis.tracker.imports.databuilder.TrackedEntityDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class PotentialDuplicatesApiTest extends TrackerApiTest {
  protected static final String TRACKER_PROGRAM_ID = Constants.TRACKER_PROGRAM_ID;

  protected static final String TRACKER_PROGRAM_STAGE_ID = "nlXNK4b7LVr";

  protected static final String MERGE_AUTHORITY = "F_TRACKED_ENTITY_MERGE";

  protected static final String USER_PASSWORD = Constants.USER_PASSWORD;

  protected UserActions userActions;

  protected TrackerImportExportActions trackerImportExportActions;

  protected PotentialDuplicatesActions potentialDuplicatesActions;

  @BeforeEach
  public void beforeEachPotentialDuplicateTest() {
    trackerImportExportActions = new TrackerImportExportActions();
    userActions = new UserActions();
    potentialDuplicatesActions = new PotentialDuplicatesActions();
  }

  protected String createUserWithAccessToMerge() {
    String username = (DataGenerator.randomString()).toLowerCase();
    String userid =
        userActions.addUserFull(
            "firstNameA", "lastNameB", username, USER_PASSWORD, MERGE_AUTHORITY);

    userActions.grantUserAccessToTAOrgUnits(userid);
    userActions.addUserToUserGroup(userid, Constants.USER_GROUP_ID);

    return username;
  }

  protected String createTrackedEntity() {
    JsonObject object =
        new TrackedEntityDataBuilder()
            .array(Constants.TRACKED_ENTITY_TYPE, Constants.ORG_UNIT_IDS[0]);

    return trackerImportExportActions
        .postAndGetJobReport(object)
        .extractImportedTrackedEntities()
        .get(0);
  }

  protected String createTrackedEntity(String teType) {
    JsonObject object = new TrackedEntityDataBuilder().array(teType, Constants.ORG_UNIT_IDS[0]);

    return trackerImportExportActions
        .postAndGetJobReport(object)
        .extractImportedTrackedEntities()
        .get(0);
  }

  protected TrackerApiResponse createTrackedEntityWithEnrollmentsAndEvents() {
    return createTrackedEntityWithEnrollmentsAndEvents(
        TRACKER_PROGRAM_ID, TRACKER_PROGRAM_STAGE_ID);
  }

  protected TrackerApiResponse createTrackedEntityWithEnrollmentsAndEvents(
      String program, String programStage) {
    return trackerImportExportActions
        .postAndGetJobReport(
            new TrackedEntityDataBuilder()
                .buildWithEnrollmentAndEvent(
                    Constants.TRACKED_ENTITY_TYPE,
                    Constants.ORG_UNIT_IDS[0],
                    program,
                    programStage))
        .validateSuccessfulImport();
  }

  @AfterEach
  public void afterEachPotentialDuplicateTest() {
    // DELETE is not implemented on PotentialDuplicatesController, so
    // there's no point to clean up
    TestRunStorage.removeEntities("/potentialDuplicates");
  }
}
