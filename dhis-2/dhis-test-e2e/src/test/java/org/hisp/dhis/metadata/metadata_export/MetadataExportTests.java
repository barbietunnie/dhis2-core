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
package org.hisp.dhis.metadata.metadata_export;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;

import org.hisp.dhis.ApiTest;
import org.hisp.dhis.test.e2e.Constants;
import org.hisp.dhis.test.e2e.actions.LoginActions;
import org.hisp.dhis.test.e2e.actions.UserActions;
import org.hisp.dhis.test.e2e.actions.metadata.MetadataActions;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;
import org.hisp.dhis.test.e2e.utils.DataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportTests extends ApiTest {
  private String userWithoutAccessUsername =
      ("MetadataExportTestsUser" + DataGenerator.randomString()).toLowerCase();

  private String userWithoutAccessPassword = Constants.USER_PASSWORD;

  private MetadataActions metadataActions;

  private LoginActions loginActions;

  private UserActions userActions;

  @BeforeAll
  public void beforeAll() {
    metadataActions = new MetadataActions();
    loginActions = new LoginActions();
    userActions = new UserActions();

    userActions.addUser(userWithoutAccessUsername, userWithoutAccessPassword);
  }

  @Test
  public void shouldNotExportAllMetadataWithoutAuthority() {
    loginActions.loginAsUser(userWithoutAccessUsername, userWithoutAccessPassword);

    metadataActions
        .get()
        .validate()
        .statusCode(409)
        .body(
            "message",
            equalTo(
                "Unfiltered access to metadata export requires super user or 'F_METADATA_EXPORT' authority."));
  }

  @Test
  public void shouldNotExportUserMetadataWithoutAuthority() {

    loginActions.loginAsUser(userWithoutAccessUsername, userWithoutAccessPassword);

    metadataActions
        .get("", new QueryParamsBuilder().add("users=true"))
        .validate()
        .statusCode(409)
        .body("message", equalTo("Exporting user metadata requires the 'F_USER_VIEW' authority."));
  }

  @Test
  public void shouldExportFilteredMetadataWithoutAuthority() {
    loginActions.loginAsUser(userWithoutAccessUsername, userWithoutAccessPassword);

    metadataActions
        .get("", new QueryParamsBuilder().add("dataElements=true&users=true"))
        .validate()
        .statusCode(200)
        .body("dataElements", not(emptyArray()))
        .body("users", not(emptyArray()));
  }

  @Test
  public void shouldExportAllMetadataAsSuperuser() {
    loginActions.loginAsSuperUser();

    metadataActions
        .get()
        .validate()
        .statusCode(200)
        .body("relationshipTypes", not(emptyArray()))
        .body("userRoles", not(emptyArray()));
  }
}
