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
package org.hisp.dhis.dxf2.datavalueset;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import org.hisp.dhis.common.IdSchemes;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataExportParams;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.node.types.RootNode;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.scheduling.JobProgress;

/**
 * @author Lars Helge Overland
 */
public interface DataValueSetService {
  /**
   * Returns a {@link DataExportParams} based on the given {@link DataValueSetQueryParams}.
   *
   * @param params the {@link DataValueSetQueryParams}.
   * @return a {@link DataExportParams}.
   */
  DataExportParams getFromUrl(DataValueSetQueryParams params);

  void validate(DataExportParams params);

  void decideAccess(DataExportParams params);

  void exportDataValueSetXml(DataExportParams params, OutputStream out);

  void exportDataValueSetJson(DataExportParams params, OutputStream out);

  /**
   * Query for {@link DataValueSet DataValueSets} and write result as JSON.
   *
   * @param lastUpdated specifies the date to filter complete data sets last updated after
   * @param outputStream the stream to write to
   * @param idSchemes idSchemes
   */
  void exportDataValueSetJson(Date lastUpdated, OutputStream outputStream, IdSchemes idSchemes);

  /**
   * Query for {@link DataValueSet DataValueSets} and write result as JSON.
   *
   * @param lastUpdated specifies the date to filter complete data sets last updated after
   * @param outputStream the stream to write to
   * @param idSchemes idSchemes
   * @param pageSize pageSize
   * @param page page
   */
  void exportDataValueSetJson(
      Date lastUpdated, OutputStream outputStream, IdSchemes idSchemes, int pageSize, int page);

  void exportDataValueSetCsv(DataExportParams params, Writer writer);

  RootNode getDataValueSetTemplate(
      DataSet dataSet,
      Period period,
      List<String> orgUnits,
      boolean writeComments,
      String ouScheme,
      String deScheme);

  ImportSummary importDataValueSetXml(InputStream in);

  ImportSummary importDataValueSetJson(InputStream in);

  ImportSummary importDataValueSetXml(InputStream in, ImportOptions importOptions);

  ImportSummary importDataValueSetJson(InputStream in, ImportOptions importOptions);

  ImportSummary importDataValueSetCsv(InputStream in, ImportOptions importOptions);

  ImportSummary importDataValueSetPdf(InputStream in, ImportOptions importOptions);

  ImportSummary importDataValueSet(DataValueSet dataValueSet, ImportOptions importOptions);

  ImportSummary importDataValueSetXml(
      InputStream in, ImportOptions importOptions, @Nonnull JobProgress progress);

  ImportSummary importDataValueSetJson(
      InputStream in, ImportOptions importOptions, @Nonnull JobProgress progress);

  ImportSummary importDataValueSetCsv(
      InputStream in, ImportOptions importOptions, @Nonnull JobProgress progress);

  ImportSummary importDataValueSetPdf(
      InputStream in, ImportOptions importOptions, @Nonnull JobProgress progress);

  ImportSummary importDataValueSetAdx(
      DataValueSetReader reader, ImportOptions importOptions, @Nonnull JobProgress progress);
}
