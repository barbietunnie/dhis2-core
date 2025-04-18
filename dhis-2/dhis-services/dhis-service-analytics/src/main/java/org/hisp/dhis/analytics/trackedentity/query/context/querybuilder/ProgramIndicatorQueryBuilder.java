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
package org.hisp.dhis.analytics.trackedentity.query.context.querybuilder;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;
import static org.hisp.dhis.analytics.common.ValueTypeMapping.NUMERIC;
import static org.hisp.dhis.analytics.common.params.dimension.DimensionParamObjectType.PROGRAM_INDICATOR;
import static org.hisp.dhis.analytics.common.query.BinaryConditionRenderer.fieldsEqual;
import static org.hisp.dhis.analytics.trackedentity.query.context.QueryContextConstants.ENROLLMENT_UID;
import static org.hisp.dhis.analytics.trackedentity.query.context.QueryContextConstants.TRACKED_ENTITY_ALIAS;
import static org.hisp.dhis.analytics.trackedentity.query.context.QueryContextConstants.TRACKED_ENTITY_UID;
import static org.hisp.dhis.analytics.trackedentity.query.context.sql.SqlQueryBuilders.isOfType;
import static org.hisp.dhis.commons.util.TextUtils.EMPTY;
import static org.hisp.dhis.commons.util.TextUtils.doubleQuote;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.analytics.DataType;
import org.hisp.dhis.analytics.common.params.AnalyticsSortingParams;
import org.hisp.dhis.analytics.common.params.dimension.DimensionIdentifier;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParam;
import org.hisp.dhis.analytics.common.params.dimension.ElementWithOffset;
import org.hisp.dhis.analytics.common.query.AndCondition;
import org.hisp.dhis.analytics.common.query.BinaryConditionRenderer;
import org.hisp.dhis.analytics.common.query.Field;
import org.hisp.dhis.analytics.common.query.GroupableCondition;
import org.hisp.dhis.analytics.common.query.IndexedOrder;
import org.hisp.dhis.analytics.common.query.LeftJoin;
import org.hisp.dhis.analytics.common.query.Order;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.QueryContext;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.RenderableSqlQuery;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.SqlQueryBuilder;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.SqlQueryBuilders;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.program.AnalyticsType;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramIndicator;
import org.hisp.dhis.program.ProgramIndicatorService;
import org.springframework.stereotype.Service;

/**
 * A {@link SqlQueryBuilder} for {@link ProgramIndicator} dimensions. It will generate a subquery
 * for each program indicator dimension.
 */
@Service
@RequiredArgsConstructor
@org.springframework.core.annotation.Order(998)
public class ProgramIndicatorQueryBuilder implements SqlQueryBuilder {
  public static final String SUBQUERY_TABLE_ALIAS = "subax";

  private final ProgramIndicatorService programIndicatorService;

  @Getter
  private final List<Predicate<DimensionIdentifier<DimensionParam>>> dimensionFilters =
      List.of(d -> isOfType(d, PROGRAM_INDICATOR));

  @Getter
  private final List<Predicate<AnalyticsSortingParams>> sortingFilters =
      List.of(d -> isOfType(d.getOrderBy(), PROGRAM_INDICATOR));

  @Override
  public RenderableSqlQuery buildSqlQuery(
      QueryContext queryContext,
      List<DimensionIdentifier<DimensionParam>> acceptedHeaders,
      List<DimensionIdentifier<DimensionParam>> acceptedDimensions,
      List<AnalyticsSortingParams> acceptedSortingParams) {

    List<ProgramIndicatorDimensionIdentifier> allDimensionIdentifiers =
        streamDimensions(acceptedHeaders, acceptedDimensions, acceptedSortingParams)
            .map(ProgramIndicatorDimensionIdentifier::of)
            .distinct()
            .collect(toList());

    RenderableSqlQuery.RenderableSqlQueryBuilder builder = RenderableSqlQuery.builder();

    buildLeftJoins(allDimensionIdentifiers, builder, queryContext.getSequence());

    buildConditions(queryContext, acceptedDimensions, builder);

    buildOrder(acceptedSortingParams, builder);

    return builder.build();
  }

  private static void buildOrder(
      List<AnalyticsSortingParams> acceptedSortingParams,
      RenderableSqlQuery.RenderableSqlQueryBuilder builder) {
    for (AnalyticsSortingParams param : acceptedSortingParams) {
      String assignedAlias = doubleQuote(param.getOrderBy().toString());
      Field sortField =
          Field.ofUnquoted(
              () -> "coalesce(" + assignedAlias + ".value, double precision 'NaN')", EMPTY);
      builder.orderClause(
          IndexedOrder.of(param.getIndex(), Order.of(sortField, param.getSortDirection())));
    }
  }

  private static void buildConditions(
      QueryContext queryContext,
      List<DimensionIdentifier<DimensionParam>> acceptedDimensions,
      RenderableSqlQuery.RenderableSqlQueryBuilder builder) {
    for (DimensionIdentifier<DimensionParam> dimensionIdentifier : acceptedDimensions) {
      String assignedAlias = doubleQuote(dimensionIdentifier.toString());
      if (SqlQueryBuilders.hasRestrictions(dimensionIdentifier)) {
        builder.groupableCondition(
            GroupableCondition.of(
                dimensionIdentifier.getGroupId(),
                AndCondition.of(
                    dimensionIdentifier.getDimension().getItems().stream()
                        .map(
                            item ->
                                BinaryConditionRenderer.of(
                                    () -> assignedAlias + ".value",
                                    item.getOperator(),
                                    item.getValues(),
                                    NUMERIC,
                                    queryContext))
                        .collect(toList()))));
      }
    }
  }

  private void buildLeftJoins(
      List<ProgramIndicatorDimensionIdentifier> allDimensionIdentifiers,
      RenderableSqlQuery.RenderableSqlQueryBuilder builder,
      AtomicInteger counter) {
    for (ProgramIndicatorDimensionIdentifier param : allDimensionIdentifiers) {
      String assignedAlias = doubleQuote(param.getDimensionIdentifier().toString());

      ProgramIndicator programIndicator =
          (ProgramIndicator) param.getDimensionIdentifier().getDimension().getQueryItem().getItem();

      ProgramIndicatorQueryParts programIndicatorQueryParts =
          getProgramIndicatorQueryParts(programIndicator);

      builder.selectField(
          Field.ofUnquoted(
              StringUtils.EMPTY,
              () -> "coalesce(" + assignedAlias + ".value::text, '')",
              param.getDimensionIdentifier()));

      if (programIndicator.getAnalyticsType() == AnalyticsType.ENROLLMENT) {
        builder.leftJoin(
            LeftJoin.of(
                () ->
                    "("
                        + enrollmentProgramIndicatorSelect(
                            param.getDimensionIdentifier().getProgram(),
                            programIndicatorQueryParts,
                            true)
                        + ") as "
                        + assignedAlias,
                fieldsEqual(
                    TRACKED_ENTITY_ALIAS, TRACKED_ENTITY_UID, assignedAlias, TRACKED_ENTITY_UID)));
      } else {
        String enrollmentAlias = "ENR_" + counter.getAndIncrement();
        builder
            .leftJoin(
                LeftJoin.of(
                    () ->
                        "("
                            + enrollmentProgramIndicatorSelect(
                                param.getDimensionIdentifier().getProgram(),
                                programIndicatorQueryParts,
                                false)
                            + ") as "
                            + enrollmentAlias,
                    fieldsEqual(
                        TRACKED_ENTITY_ALIAS,
                        TRACKED_ENTITY_UID,
                        enrollmentAlias,
                        TRACKED_ENTITY_UID)))
            .leftJoin(
                LeftJoin.of(
                    () ->
                        "("
                            + eventProgramIndicatorSelect(
                                param.getDimensionIdentifier().getProgram(),
                                programIndicatorQueryParts)
                            + ") as "
                            + assignedAlias,
                    fieldsEqual(enrollmentAlias, ENROLLMENT_UID, assignedAlias, ENROLLMENT_UID)));
      }
    }
  }

  private record ProgramIndicatorQueryParts(String function, String expression, String filter) {}

  private ProgramIndicatorQueryParts getProgramIndicatorQueryParts(
      ProgramIndicator programIndicator) {

    return new ProgramIndicatorQueryParts(
        // function
        TextUtils.emptyIfEqual(
            programIndicator.getAggregationTypeFallback().getValue(),
            AggregationType.CUSTOM.getValue()),
        // expression
        programIndicatorService.getAnalyticsSql(
            programIndicator.getExpression(),
            DataType.NUMERIC,
            programIndicator,
            null,
            null,
            SUBQUERY_TABLE_ALIAS),
        // filter
        programIndicatorService.getAnalyticsSql(
            programIndicator.getFilter(),
            DataType.BOOLEAN,
            programIndicator,
            null,
            null,
            SUBQUERY_TABLE_ALIAS));
  }

  private static String enrollmentProgramIndicatorSelect(
      ElementWithOffset<Program> program,
      ProgramIndicatorQueryParts programIndicatorQueryParts,
      boolean needsExpressions) {
    String expression = programIndicatorQueryParts.expression();
    String filter = programIndicatorQueryParts.filter();
    int offset = program.getOffsetWithDefault();

    return "select innermost_enr.*"
        + " from (select trackedentity as "
        + TRACKED_ENTITY_UID
        + ", enrollment as "
        + ENROLLMENT_UID
        + ", "
        + (needsExpressions ? expression + " as value, " : "")
        + " row_number() over (partition by trackedentity order by enrollmentdate desc) as rn "
        + " from analytics_enrollment_"
        + program.getElement().getUid()
        + " as "
        + SUBQUERY_TABLE_ALIAS
        + (needsExpressions ? " where " + filter : "")
        + ") innermost_enr"
        + " where innermost_enr.rn = "
        // This logic is needed because of the row_number(), which starts in 1.
        + (offset >= 0 ? ++offset : abs(offset));
  }

  static String eventProgramIndicatorSelect(
      ElementWithOffset<Program> program, ProgramIndicatorQueryParts programIndicatorQueryParts) {

    String filter = programIndicatorQueryParts.filter();
    String function = programIndicatorQueryParts.function();
    String expression = programIndicatorQueryParts.expression();

    String whereCondition = "";

    if (StringUtils.isNotBlank(filter)) {
      whereCondition = " where " + filter;
    }
    return "select innermost_evt.enrollment, "
        + function
        + "(innermost_evt.value) as value"
        + " from (select enrollment as "
        + ENROLLMENT_UID
        + ", "
        + expression
        + " as value "
        + " from analytics_event_"
        + program.getElement().getUid()
        + " as "
        + SUBQUERY_TABLE_ALIAS
        + whereCondition
        + ") innermost_evt group by innermost_evt.enrollment";
  }

  @Getter
  @RequiredArgsConstructor(staticName = "of")
  static class ProgramIndicatorDimensionIdentifier {
    private final DimensionIdentifier<DimensionParam> dimensionIdentifier;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ProgramIndicatorDimensionIdentifier that = (ProgramIndicatorDimensionIdentifier) o;
      return Objects.equals(
              dimensionIdentifier.getProgram(), that.getDimensionIdentifier().getProgram())
          && Objects.equals(
              dimensionIdentifier.getProgramStage(),
              that.getDimensionIdentifier().getProgramStage())
          && Objects.equals(
              dimensionIdentifier.getDimension().getQueryItem(),
              that.getDimensionIdentifier().getDimension().getQueryItem());
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          dimensionIdentifier.getProgram(),
          dimensionIdentifier.getProgramStage(),
          dimensionIdentifier.getDimension().getQueryItem());
    }
  }
}
