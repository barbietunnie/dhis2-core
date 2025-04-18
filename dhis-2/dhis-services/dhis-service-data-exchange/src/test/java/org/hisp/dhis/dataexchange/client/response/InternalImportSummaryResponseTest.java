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
package org.hisp.dhis.dataexchange.client.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hisp.dhis.dxf2.importsummary.ImportCount;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.junit.jupiter.api.Test;

class InternalImportSummaryResponseTest {
  @Test
  void testGetImportSummary238() {
    InternalImportSummaryResponse response = new InternalImportSummaryResponse();
    response.setResponse(
        new ImportSummary(
            ImportStatus.WARNING, "One more conflicts encountered", new ImportCount(8, 4, 2, 0)));

    assertEquals(ImportStatus.WARNING, response.getImportSummary().getStatus());
    assertEquals(4, response.getImportSummary().getImportCount().getUpdated());
  }

  @Test
  void testGetImportSummary237() {
    InternalImportSummaryResponse response = new InternalImportSummaryResponse();
    response.setStatus(Status.WARNING);
    response.setDescription("One more conflicts encountered");
    response.setImportCount(new ImportCount(4, 2, 6, 0));

    assertEquals(ImportStatus.WARNING, response.getImportSummary().getStatus());
    assertEquals(2, response.getImportSummary().getImportCount().getUpdated());
  }

  @Test
  void testToImportStatus() {
    InternalImportSummaryResponse response = new InternalImportSummaryResponse();

    assertEquals(ImportStatus.SUCCESS, response.toImportStatus(Status.SUCCESS));
    assertEquals(ImportStatus.WARNING, response.toImportStatus(Status.WARNING));
    assertEquals(ImportStatus.ERROR, response.toImportStatus(Status.ERROR));
  }
}
