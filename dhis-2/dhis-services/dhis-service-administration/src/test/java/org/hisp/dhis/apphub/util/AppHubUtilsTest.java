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
package org.hisp.dhis.apphub.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hisp.dhis.apphub.AppHubUtils;
import org.hisp.dhis.feedback.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

/**
 * @author Lars Helge Overland
 */
class AppHubUtilsTest {

  @Test
  void testValidateQuery() throws ConflictException {
    AppHubUtils.validateQuery("apps");
  }

  @Test
  void testValidateInvalidQueryA() {
    assertThrows(
        ConflictException.class, () -> AppHubUtils.validateQuery("apps/../../evil/endpoint"));
  }

  @Test
  void testValidateInvalidQueryB() {
    assertThrows(ConflictException.class, () -> AppHubUtils.validateQuery("http://evildomain"));
  }

  @Test
  void testValidateInvalidQueryC() {
    assertThrows(ConflictException.class, () -> AppHubUtils.validateQuery(""));
  }

  @Test
  void testValidateInvalidQueryD() {
    assertThrows(ConflictException.class, () -> AppHubUtils.validateQuery(null));
  }

  @Test
  void testValidateApiVersionA() throws ConflictException {
    AppHubUtils.validateApiVersion("v2");
  }

  @Test
  void testValidateApiVersionB() throws ConflictException {
    AppHubUtils.validateApiVersion("v146");
  }

  @Test
  void testValidateInvalidApiVersionA() {
    assertThrows(ConflictException.class, () -> AppHubUtils.validateApiVersion("25"));
  }

  @Test
  void testValidateInvalidApiVersionB() {
    assertThrows(
        ConflictException.class, () -> AppHubUtils.validateApiVersion("malicious_script.js"));
  }

  @Test
  void testSanitizeQuery() {
    assertEquals("apps", AppHubUtils.sanitizeQuery("apps"));
    assertEquals("apps", AppHubUtils.sanitizeQuery("/apps"));
    assertEquals("apps", AppHubUtils.sanitizeQuery("//apps"));
  }

  @Test
  void testGetJsonRequestEntity() {
    HttpEntity<String> entity = AppHubUtils.getJsonRequestEntity();
    assertTrue(entity.getHeaders().getAccept().contains(MediaType.APPLICATION_JSON));
  }
}
