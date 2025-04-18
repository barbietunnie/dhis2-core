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
package org.hisp.dhis.program;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hisp.dhis.analytics.DataType.NUMERIC;
import static org.hisp.dhis.antlr.AntlrParserUtils.castString;
import static org.hisp.dhis.parser.expression.ExpressionItem.ITEM_GET_DESCRIPTIONS;
import static org.hisp.dhis.parser.expression.ExpressionItem.ITEM_GET_SQL;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.hisp.dhis.antlr.AntlrExprLiteral;
import org.hisp.dhis.antlr.Parser;
import org.hisp.dhis.antlr.literal.DefaultLiteral;
import org.hisp.dhis.common.DimensionService;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementDomain;
import org.hisp.dhis.db.sql.PostgreSqlBuilder;
import org.hisp.dhis.expression.ExpressionParams;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.parser.expression.CommonExpressionVisitor;
import org.hisp.dhis.parser.expression.ExpressionItemMethod;
import org.hisp.dhis.parser.expression.ProgramExpressionParams;
import org.hisp.dhis.parser.expression.literal.SqlLiteral;
import org.hisp.dhis.test.TestBase;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * @author Jim Grace
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ProgramSqlGeneratorItemsTest extends TestBase {
  private ProgramIndicator programIndicator;

  private ProgramStage programStageA;

  private Program programA;

  private DataElement dataElementA;

  private TrackedEntityAttribute attributeA;

  private Constant constantA;

  private Map<String, Constant> constantMap;

  private final Date startDate = getDate(2020, 1, 1);

  private final Date endDate = getDate(2020, 12, 31);

  @Mock private ProgramIndicatorService programIndicatorService;

  @Mock private ProgramStageService programStageService;

  @Mock private IdentifiableObjectManager idObjectManager;

  @Mock private DimensionService dimensionService;

  @Spy private PostgreSqlBuilder sqlBuilder;

  @BeforeEach
  public void setUp() {
    dataElementA = createDataElement('A');
    dataElementA.setDomainType(DataElementDomain.TRACKER);
    dataElementA.setUid("DataElmentA");

    attributeA = createTrackedEntityAttribute('A', ValueType.NUMBER);
    attributeA.setUid("Attribute0A");

    constantA = new Constant("Constant A", 123.456);
    constantA.setUid("constant00A");

    constantMap = Map.of("constant00A", new Constant("constant", 123.456));

    OrganisationUnit organisationUnit = createOrganisationUnit('A');

    programStageA = new ProgramStage("StageA", programA);
    programStageA.setSortOrder(1);
    programStageA.setUid("ProgrmStagA");

    programA = createProgram('A', new HashSet<>(), organisationUnit);
    programA.setUid("Program000A");

    programIndicator = new ProgramIndicator();
    programIndicator.setProgram(programA);
    programIndicator.setAnalyticsType(AnalyticsType.EVENT);
  }

  @Test
  void testDataElement() {
    when(idObjectManager.get(DataElement.class, dataElementA.getUid())).thenReturn(dataElementA);
    when(programStageService.getProgramStage(programStageA.getUid())).thenReturn(programStageA);

    String sql = test("#{ProgrmStagA.DataElmentA}");
    assertThat(
        sql,
        is(
            "coalesce(case when ax.\"ps\" = 'ProgrmStagA' then \"DataElmentA\" else null end::numeric,0)"));
  }

  @Test
  void testDataElementAllowingNulls() {
    when(idObjectManager.get(DataElement.class, dataElementA.getUid())).thenReturn(dataElementA);
    when(programStageService.getProgramStage(programStageA.getUid())).thenReturn(programStageA);

    String sql = test("d2:oizp(#{ProgrmStagA.DataElmentA})");
    assertThat(
        sql,
        is(
            "coalesce(case when case when ax.\"ps\" = 'ProgrmStagA' then \"DataElmentA\" else null end >= 0 then 1 else 0 end, 0)"));
  }

  @Test
  void testDataElementNotFound() {
    when(programStageService.getProgramStage(programStageA.getUid())).thenReturn(programStageA);

    assertThrows(
        org.hisp.dhis.antlr.ParserException.class, () -> test("#{ProgrmStagA.NotElementA}"));
  }

  @Test
  void testAttribute() {
    when(idObjectManager.get(TrackedEntityAttribute.class, attributeA.getUid()))
        .thenReturn(attributeA);

    String sql = test("A{Attribute0A}");
    assertThat(sql, is("coalesce(\"Attribute0A\"::numeric,0)"));
  }

  @Test
  void testAttributeAllowingNulls() {
    when(idObjectManager.get(TrackedEntityAttribute.class, attributeA.getUid()))
        .thenReturn(attributeA);

    String sql = test("d2:oizp(A{Attribute0A})");
    assertThat(sql, is("coalesce(case when \"Attribute0A\" >= 0 then 1 else 0 end, 0)"));
  }

  @Test
  void testAttributeNotFound() {
    assertThrows(org.hisp.dhis.antlr.ParserException.class, () -> test("A{NoAttribute}"));
  }

  @Test
  void testConstant() {
    String sql = test("C{constant00A}");
    assertThat(sql, is("123.456"));
  }

  @Test
  void testConstantNotFound() {
    assertThrows(org.hisp.dhis.antlr.ParserException.class, () -> test("C{notConstant}"));
  }

  @Test
  void testInvalidItemType() {
    assertThrows(org.hisp.dhis.antlr.ParserException.class, () -> test("I{notValidItm}"));
  }

  @Test
  void testNumericExpression() {
    String sql = testNumeric("1/2");
    assertThat(sql, is("1::numeric / 2::numeric"));
  }

  // -------------------------------------------------------------------------
  // Supportive methods
  // -------------------------------------------------------------------------

  private String testNumeric(String expression) {
    return castString(test(expression, new SqlLiteral(new PostgreSqlBuilder()), ITEM_GET_SQL));
  }

  private String test(String expression) {
    test(expression, new DefaultLiteral(), ITEM_GET_DESCRIPTIONS);

    return castString(test(expression, new SqlLiteral(new PostgreSqlBuilder()), ITEM_GET_SQL));
  }

  private Object test(
      String expression, AntlrExprLiteral exprLiteral, ExpressionItemMethod itemMethod) {
    Set<String> dataElementsAndAttributesIdentifiers = new LinkedHashSet<>();
    dataElementsAndAttributesIdentifiers.add(BASE_UID + "a");
    dataElementsAndAttributesIdentifiers.add(BASE_UID + "b");
    dataElementsAndAttributesIdentifiers.add(BASE_UID + "c");

    ExpressionParams params = ExpressionParams.builder().dataType(NUMERIC).build();

    ProgramExpressionParams progParams =
        ProgramExpressionParams.builder()
            .programIndicator(programIndicator)
            .reportingStartDate(startDate)
            .reportingEndDate(endDate)
            .dataElementAndAttributeIdentifiers(dataElementsAndAttributesIdentifiers)
            .build();

    CommonExpressionVisitor visitor =
        CommonExpressionVisitor.builder()
            .idObjectManager(idObjectManager)
            .dimensionService(dimensionService)
            .programIndicatorService(programIndicatorService)
            .programStageService(programStageService)
            .i18nSupplier(() -> new I18n(null, null))
            .constantMap(constantMap)
            .itemMap(new ExpressionMapBuilder().getExpressionItemMap())
            .itemMethod(itemMethod)
            .params(params)
            .progParams(progParams)
            .sqlBuilder(sqlBuilder)
            .build();

    visitor.setExpressionLiteral(exprLiteral);

    return Parser.visit(expression, visitor);
  }
}
