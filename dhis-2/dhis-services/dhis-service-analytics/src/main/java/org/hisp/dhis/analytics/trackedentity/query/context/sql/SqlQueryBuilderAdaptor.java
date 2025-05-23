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
package org.hisp.dhis.analytics.trackedentity.query.context.sql;

import java.util.List;
import java.util.stream.Stream;
import org.hisp.dhis.analytics.common.params.AnalyticsSortingParams;
import org.hisp.dhis.analytics.common.params.dimension.DimensionIdentifier;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParam;
import org.hisp.dhis.analytics.common.query.Field;
import org.hisp.dhis.analytics.common.query.GroupableCondition;
import org.hisp.dhis.analytics.common.query.IndexedOrder;
import org.hisp.dhis.analytics.common.query.LeftJoin;

/**
 * Convenience class to build a {@link RenderableSqlQuery} from a {@link QueryContext}, allowing to
 * override only the parts of the query that are needed.
 */
public abstract class SqlQueryBuilderAdaptor implements SqlQueryBuilder {
  @Override
  public RenderableSqlQuery buildSqlQuery(
      QueryContext queryContext,
      List<DimensionIdentifier<DimensionParam>> acceptedHeaders,
      List<DimensionIdentifier<DimensionParam>> acceptedDimensions,
      List<AnalyticsSortingParams> acceptedSortingParams) {
    RenderableSqlQuery.RenderableSqlQueryBuilder builder = RenderableSqlQuery.builder();

    getSelect(queryContext).forEach(builder::selectField);
    getLeftJoin(queryContext).forEach(builder::leftJoin);
    getWhereClauses(queryContext, acceptedDimensions).forEach(builder::groupableCondition);
    getOrderClauses(queryContext, acceptedSortingParams).forEach(builder::orderClause);

    return builder.build();
  }

  protected Stream<Field> getSelect(QueryContext unused) {
    return Stream.empty();
  }

  protected Stream<LeftJoin> getLeftJoin(QueryContext unused) {
    return Stream.empty();
  }

  protected Stream<GroupableCondition> getWhereClauses(
      QueryContext unusedOne, List<DimensionIdentifier<DimensionParam>> unusedTwo) {
    return Stream.empty();
  }

  protected Stream<IndexedOrder> getOrderClauses(
      QueryContext unused, List<AnalyticsSortingParams> unusedTwo) {
    return Stream.empty();
  }
}
