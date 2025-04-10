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
package org.hisp.dhis.aggregate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import org.hisp.dhis.ApiTest;
import org.hisp.dhis.helpers.file.JsonFileReader;
import org.hisp.dhis.test.e2e.actions.LoginActions;
import org.hisp.dhis.test.e2e.actions.SystemActions;
import org.hisp.dhis.test.e2e.actions.aggregate.DataValueActions;
import org.hisp.dhis.test.e2e.actions.aggregate.DataValueSetActions;
import org.hisp.dhis.test.e2e.actions.metadata.MetadataActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.dto.ImportSummary;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Disabled
@Tag("category:aggregate")
class DataImportTest extends ApiTest {
  private DataValueSetActions dataValueSetActions;

  private DataValueActions dataValueActions;

  private SystemActions systemActions;

  @BeforeAll
  public void before() {
    dataValueSetActions = new DataValueSetActions();
    MetadataActions metadataActions = new MetadataActions();
    dataValueActions = new DataValueActions();
    systemActions = new SystemActions();

    new LoginActions().loginAsSuperUser();
    metadataActions
        .importMetadata(new File("src/test/resources/aggregate/metadata.json"), "async=false")
        .validate()
        .statusCode(200);
  }

  @Test
  void dataValuesCanBeImportedInBulk() {
    ApiResponse response =
        dataValueSetActions.postFile(
            new File("src/test/resources/aggregate/dataValues_bulk.json"),
            new QueryParamsBuilder().add("importReportMode=FULL"));

    response
        .validate()
        .statusCode(200)
        .rootPath("response")
        .body("status", equalTo("SUCCESS"))
        .body("conflicts", empty())
        .body("importCount", notNullValue())
        .rootPath("importCount")
        .body("ignored", not(greaterThan(0)))
        .body("deleted", not(greaterThan(0)));

    ImportSummary importSummary = response.getImportSummaries().get(0);
    assertThat(
        response.getAsString(),
        importSummary.getImportCount().getImported() + importSummary.getImportCount().getUpdated(),
        greaterThan(0));
  }

  @Test
  void dataValuesCanBeImportedAsync() {
    ApiResponse response =
        dataValueSetActions
            .postFile(
                new File("src/test/resources/aggregate/dataValues_bulk.json"),
                new QueryParamsBuilder().addAll("reportMode=DEBUG", "async=true"))
            .validateStatus(200);

    String taskId = response.extractString("response.id");

    // Validate that job was successful
    systemActions
        .waitUntilTaskCompleted("DATAVALUE_IMPORT", taskId)
        .validate()
        .body("message", hasItem(containsString("Process started")))
        .body("message", hasItem(containsString("Importing data values")))
        .body("message", hasItem(containsString("Import done")));

    // validate task summaries were created
    ApiResponse taskSummariesResponse =
        systemActions.waitForTaskSummaries("DATAVALUE_IMPORT", taskId);

    taskSummariesResponse
        .validate()
        .statusCode(200)
        .body("status", equalTo("SUCCESS"))
        .rootPath("importCount")
        .body("deleted", equalTo(0))
        .body("ignored", equalTo(0));

    ImportSummary importSummary = taskSummariesResponse.getImportSummaries().get(0);
    assertThat(
        taskSummariesResponse.getAsString(),
        importSummary.getImportCount().getImported() + importSummary.getImportCount().getUpdated(),
        greaterThan(0));
  }

  @Test
  @DisplayName(
      "When an empty json file is async-imported then a task summary should be available with an error message")
  void asyncImportEmptyJsonFileSummaryAvailable() {
    // given an empty json file is async imported
    ApiResponse response =
        dataValueSetActions
            .postFile(
                new File("src/test/resources/aggregate/empty_file.json"),
                new QueryParamsBuilder().addAll("reportMode=DEBUG", "async=true", "format=json"))
            .validateStatus(200);

    String taskId = response.extractString("response.id");

    // when the task is completed
    systemActions
        .waitUntilTaskCompleted("DATAVALUE_IMPORT", taskId)
        .validate()
        .body(
            "message",
            hasItem(
                containsString(
                    "Import complete with status ERROR, 0 created, 0 updated, 0 deleted, 0 ignored")))
        .body("message", hasItem(containsString("No content to map due to end-of-input")));

    // then a task summary should be available with an error message
    ApiResponse taskSummariesResponse =
        systemActions.waitForTaskSummaries("DATAVALUE_IMPORT", taskId);

    taskSummariesResponse
        .validate()
        .statusCode(200)
        .body("status", equalTo("ERROR"))
        .body("description", containsString("No content to map due to end-of-input"))
        .rootPath("importCount")
        .body("imported", equalTo(0))
        .body("updated", equalTo(0))
        .body("deleted", equalTo(0))
        .body("ignored", equalTo(0));
  }

  @Test
  @DisplayName(
      "When an empty xml file is async-imported for adx then a task summary should be available with an error message")
  void asyncImportEmptyXmlFileSummaryAvailable() {
    // given an empty xml file is async imported for adx
    ApiResponse response =
        dataValueSetActions
            .postFile(
                new File("src/test/resources/aggregate/empty_file.xml"),
                new QueryParamsBuilder().addAll("reportMode=DEBUG", "async=true", "format=adx"),
                "application/adx+xml")
            .validateStatus(200);

    String taskId = response.extractString("response.id");

    // when the task is completed
    systemActions
        .waitUntilTaskCompleted("DATAVALUE_IMPORT", taskId)
        .validate()
        .body(
            "message",
            hasItem(
                containsString(
                    "Import complete with status ERROR, 0 created, 0 updated, 0 deleted, 0 ignored")));

    // then a task summary should be available with an error message
    ApiResponse taskSummariesResponse =
        systemActions.waitForTaskSummaries("DATAVALUE_IMPORT", taskId);

    taskSummariesResponse
        .validate()
        .statusCode(200)
        .body("status", equalTo("ERROR"))
        .body("description", containsString("Data set import failed"))
        .rootPath("importCount")
        .body("imported", equalTo(0))
        .body("updated", equalTo(0))
        .body("deleted", equalTo(0))
        .body("ignored", equalTo(0));
  }

  @Test
  void dataValuesCanBeImportedForSingleDataSet() throws IOException {
    String orgUnit = "O6uvpzGd5pu";
    String period = "201911";
    String dataSet = "VEM58nY22sO";

    JsonObject importedPayload =
        new JsonFileReader(new File("src/test/resources/aggregate/dataValues_single_dataset.json"))
            .get();
    ApiResponse response = dataValueSetActions.post(importedPayload);

    response
        .validate()
        .statusCode(200)
        .rootPath("response")
        .body("status", equalTo("SUCCESS"))
        .body("conflicts", empty())
        .body("importCount", notNullValue())
        .rootPath("response.importCount")
        .body("ignored", not(greaterThan(0)))
        .body("deleted", not(greaterThan(0)));

    ImportSummary importSummary = response.getImportSummaries().get(0);

    assertThat(importSummary, notNullValue());
    assertThat(
        response.getAsString(),
        importSummary.getImportCount().getImported() + importSummary.getImportCount().getUpdated(),
        greaterThanOrEqualTo(2));

    response =
        dataValueSetActions.get(
            String.format("?orgUnit=%s&period=%s&dataSet=%s", orgUnit, period, dataSet));

    response
        .validate()
        .body("dataSet", equalTo(dataSet))
        .body("period", equalTo(period))
        .body("orgUnit", equalTo(orgUnit))
        .body("dataValues", hasSize(greaterThanOrEqualTo(2)));

    JsonArray dataValues = response.getBody().get("dataValues").getAsJsonArray();

    for (JsonElement j : dataValues) {
      JsonObject object = j.getAsJsonObject();

      response =
          dataValueActions.get(
              String.format(
                  "?ou=%s&pe=%s&de=%s&co=%s",
                  orgUnit,
                  period,
                  object.get("dataElement").getAsString(),
                  object.get("categoryOptionCombo").getAsString()));

      response.validate().statusCode(200).body(containsString(object.get("value").getAsString()));
    }
  }

  @AfterEach
  public void cleanUp() {
    QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder();
    queryParamsBuilder.addAll("importReportMode=FULL", "importStrategy=DELETE");

    ApiResponse response =
        dataValueSetActions.postFile(
            new File("src/test/resources/aggregate/dataValues_bulk.json"), queryParamsBuilder);
    response.validate().statusCode(200);

    response =
        dataValueSetActions.postFile(
            new File("src/test/resources/aggregate/dataValues_single_dataset.json"),
            queryParamsBuilder);
    response.validate().statusCode(200);
  }
}
