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
package org.hisp.dhis.tracker.imports.events;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hisp.dhis.helpers.matchers.MatchesJson.matchesJSON;

import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.hisp.dhis.helpers.TestCleanUp;
import org.hisp.dhis.helpers.file.FileReaderUtils;
import org.hisp.dhis.test.e2e.Constants;
import org.hisp.dhis.test.e2e.actions.metadata.ProgramStageActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.dto.TrackerApiResponse;
import org.hisp.dhis.test.e2e.helpers.JsonObjectBuilder;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;
import org.hisp.dhis.tracker.TrackerApiTest;
import org.hisp.dhis.tracker.imports.databuilder.EventDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
class EventsTests extends TrackerApiTest {
  private static final String OU_ID_0 = Constants.ORG_UNIT_IDS[0];

  private static final String OU_ID = Constants.ORG_UNIT_IDS[1];

  private static final String OU_ID_2 = Constants.ORG_UNIT_IDS[2];

  private static Stream<Arguments> provideEventFilesTestArguments() {
    return Stream.of(
        Arguments.arguments("event.json", ContentType.JSON.toString()),
        Arguments.arguments("event.csv", "text/csv"));
  }

  @BeforeAll
  public void beforeAll() {
    loginActions.loginAsSuperUser();
  }

  @Test
  void shouldImportEvents() throws Exception {
    JsonObject eventBody =
        new FileReaderUtils()
            .readJsonAndGenerateData(
                new File("src/test/resources/tracker/importer/events/events.json"));

    TrackerApiResponse importResponse = trackerImportExportActions.postAndGetJobReport(eventBody);

    importResponse
        .validateSuccessfulImport()
        .validateEvents()
        .body("stats.created", Matchers.equalTo(4))
        .body("objectReports", notNullValue())
        .body("objectReports[0].errorReports", empty());

    eventBody
        .getAsJsonArray("events")
        .forEach(
            event ->
                trackerImportExportActions
                    .getEvent(event.getAsJsonObject().get("event").getAsString())
                    .validate()
                    .statusCode(200)
                    .body("", matchesJSON(event)));
  }

  @ParameterizedTest
  @MethodSource("provideEventFilesTestArguments")
  void eventsImportNewEventsFromFile(String fileName, String contentType) throws Exception {
    Object obj =
        new FileReaderUtils()
            .read(new File("src/test/resources/tracker/importer/events/" + fileName))
            .replacePropertyValuesWithIds("event")
            .get();

    ApiResponse response =
        trackerImportExportActions.post(
            "",
            contentType,
            obj,
            new QueryParamsBuilder()
                .addAll("dryRun=false", "eventIdScheme=UID", "orgUnitIdScheme=UID"));
    response.validate().statusCode(200);

    String jobId = response.extractString("response.id");

    trackerImportExportActions.waitUntilJobIsCompleted(jobId);

    response = trackerImportExportActions.getJobReport(jobId, "FULL");

    response.validate().statusCode(200).body("status", equalTo("OK"));
  }

  @Test
  void eventsImportNewEventsWithInvalidUidForCSV() throws Exception {
    Object obj =
        new FileReaderUtils()
            .read(new File("src/test/resources/tracker/importer/events/event.csv"))
            .replacePropertyValuesWith("event", "invalid_uid")
            .get();

    ApiResponse response =
        trackerImportExportActions.post("", "text/csv", obj, new QueryParamsBuilder());
    response
        .validate()
        .statusCode(400)
        .body("status", equalTo("ERROR"))
        .body(
            "message",
            startsWith(
                "UID must be an alphanumeric string of 11 characters starting with a letter"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  void shouldImportToRepeatableStage(Boolean repeatableStage) throws Exception {
    // arrange
    String program = Constants.TRACKER_PROGRAM_ID;
    String programStage =
        new ProgramStageActions()
            .get(
                "",
                new QueryParamsBuilder()
                    .addAll(
                        "filter=program.id:eq:" + program,
                        "filter=repeatable:eq:" + repeatableStage))
            .extractString("programStages.id[0]");

    TrackerApiResponse response = importTrackedEntityWithEnrollment(program);
    String teId = response.extractImportedTrackedEntities().get(0);
    String enrollmentId = response.extractImportedEnrollments().get(0);

    JsonObject event =
        new EventDataBuilder()
            .setEnrollment(enrollmentId)
            .setTrackedEntity(teId)
            .array(OU_ID, program, programStage)
            .getAsJsonArray("events")
            .get(0)
            .getAsJsonObject();

    JsonObject payload = new JsonObjectBuilder().addArray("events", event, event).build();

    // act
    response = trackerImportExportActions.postAndGetJobReport(payload);

    // assert
    if (repeatableStage) {
      response.validateSuccessfulImport().validate().body("stats.created", equalTo(2));
    } else {
      response.validateErrorReport().body("errorCode", hasItem("E1039"));
    }
  }

  @Test
  void shouldImportAndGetEventWithOrgUnitDifferentFromEnrollmentOrgUnit() throws Exception {
    String programId = Constants.TRACKER_PROGRAM_ID;
    String programStageId = "nlXNK4b7LVr";

    TrackerApiResponse response = importTrackedEntityWithEnrollment(programId);

    String enrollmentId = response.extractImportedEnrollments().get(0);

    JsonObject event =
        new EventDataBuilder()
            .setEnrollment(enrollmentId)
            .array(OU_ID_0, programId, programStageId);

    response = trackerImportExportActions.postAndGetJobReport(event).validateSuccessfulImport();

    String eventId = response.extractImportedEvents().get(0);

    trackerImportExportActions
        .get("/enrollments/" + enrollmentId)
        .validate()
        .statusCode(200)
        .body("orgUnit", equalTo(OU_ID));

    trackerImportExportActions
        .get("/events/" + eventId + "?fields=*")
        .validate()
        .statusCode(200)
        .body("orgUnit", equalTo(OU_ID_0));

    QueryParamsBuilder builder =
        new QueryParamsBuilder()
            .add("orgUnitMode", "DESCENDANTS")
            .add("orgUnit", OU_ID_2)
            .add("program", programId);
    trackerImportExportActions
        .getEvents(builder)
        .validate()
        .statusCode(200)
        .body("events", hasSize(greaterThanOrEqualTo(1)))
        .body("events[0].orgUnit", equalTo(OU_ID_0));
  }

  @Test
  void shouldAddEventsToExistingTrackedEntity() throws Exception {
    String programId = Constants.TRACKER_PROGRAM_ID;
    String programStageId = "nlXNK4b7LVr";

    TrackerApiResponse response = importTrackedEntityWithEnrollment(programId);

    String enrollmentId = response.extractImportedEnrollments().get(0);

    JsonObject event =
        new EventDataBuilder().setEnrollment(enrollmentId).array(OU_ID, programId, programStageId);

    response = trackerImportExportActions.postAndGetJobReport(event).validateSuccessfulImport();

    String eventId = response.extractImportedEvents().get(0);

    trackerImportExportActions
        .get("/events/" + eventId + "?fields=*")
        .validate()
        .statusCode(200)
        .body("enrollment", equalTo(enrollmentId));
  }

  @Test
  void shouldImportWithCategoryCombo() {
    ApiResponse program =
        programActions.get(
            "",
            new QueryParamsBuilder()
                .add("programType=WITHOUT_REGISTRATION")
                .add("filter=categoryCombo.code:!eq:default")
                .add("filter=name:like:TA")
                .add("fields=id,categoryCombo[categories[categoryOptions]]"));

    String programId = program.extractString("programs.id[0]");
    List<String> category =
        program.extractList("programs[0].categoryCombo.categories.categoryOptions.id.flatten()");

    Assumptions.assumeFalse(StringUtils.isEmpty(programId));

    JsonObject object =
        new EventDataBuilder()
            .setProgram(programId)
            .setAttributeCategoryOptions(category)
            .setOrgUnit(OU_ID)
            .array();

    trackerImportExportActions.postAndGetJobReport(object).validateSuccessfulImport();
  }

  @AfterEach
  public void afterEach() {
    new TestCleanUp().deleteCreatedEntities("/events");
  }
}
