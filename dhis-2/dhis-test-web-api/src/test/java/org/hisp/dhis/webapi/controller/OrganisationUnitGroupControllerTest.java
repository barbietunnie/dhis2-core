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
package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.http.HttpAssertions.assertStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hisp.dhis.http.HttpStatus;
import org.hisp.dhis.test.webapi.H2ControllerIntegrationTestBase;
import org.hisp.dhis.test.webapi.json.domain.JsonIdentifiableObject;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests the {@link org.hisp.dhis.organisationunit.OrganisationUnitGroup} using (mocked) REST
 * requests.
 *
 * @author Jan Bernitt
 */
@Transactional
class OrganisationUnitGroupControllerTest extends H2ControllerIntegrationTestBase {
  @Test
  void testCreateWithDescription() {
    String id =
        assertStatus(
            HttpStatus.CREATED,
            POST(
                "/organisationUnitGroups/",
                "{'name':'test', 'shortName':'test', 'description': 'desc' }"));

    assertEquals(
        "desc",
        GET("/organisationUnitGroups/{id}", id)
            .content()
            .as(JsonIdentifiableObject.class)
            .getDescription());
  }

  @Test
  void testUpdateWithDescription() {
    String id =
        assertStatus(
            HttpStatus.CREATED,
            POST("/organisationUnitGroups/", "{'name':'test', 'shortName':'test'}"));

    assertStatus(
        HttpStatus.OK,
        PATCH(
            "/organisationUnitGroups/" + id + "?importReportMode=ERRORS",
            "[" + "{'op': 'add', 'path': '/description', 'value': 'desc' }" + "]"));

    assertEquals(
        "desc",
        GET("/organisationUnitGroups/{id}", id)
            .content()
            .as(JsonIdentifiableObject.class)
            .getDescription());
  }
}
