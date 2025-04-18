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
package org.hisp.dhis.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Immutable class representing parameters for query planning. Should be instantiated using the
 * Builder class. Example usage:
 *
 * <pre>
 * {
 *     &#64;code
 *     QueryPlannerParams params = QueryPlannerParams.newBuilder()
 *         .withOptimalQueries( 4 )
 *         .withTableType( AnalyticsTableType.DATA_VALUE )
 *         .withTableSuffix( "table_suffix" )
 *         .build();
 * }
 * </pre>
 *
 * @author Lars Helge Overland
 */
public class QueryPlannerParams {
  private int optimalQueries;

  private AnalyticsTableType tableType;

  private List<Function<DataQueryParams, List<DataQueryParams>>> queryGroupers = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  private QueryPlannerParams() {}

  public static Builder newBuilder() {
    return new Builder();
  }

  // -------------------------------------------------------------------------
  // Get and set methods
  // -------------------------------------------------------------------------

  /**
   * Returns the number of optimal queries for the planner to return for each query group.
   *
   * @return number of optimal queries.
   */
  public int getOptimalQueries() {
    return optimalQueries;
  }

  /**
   * Returns the {@link AnalyticsTableType}.
   *
   * @return the {@link AnalyticsTableType}.
   */
  public AnalyticsTableType getTableType() {
    return tableType;
  }

  /**
   * Returns the base name of the analytics table.
   *
   * @return the base name of the analytics table.
   */
  public String getTableName() {
    return tableType.getTableName();
  }

  /**
   * Returns additional query groupers to apply in planning.
   *
   * @return additional query groupers to apply in planning.
   */
  public List<Function<DataQueryParams, List<DataQueryParams>>> getQueryGroupers() {
    return queryGroupers;
  }

  // -------------------------------------------------------------------------
  // Builder of immutable instances
  // -------------------------------------------------------------------------

  /** Builder of {@link QueryPlannerParams} instances. */
  public static class Builder {
    private QueryPlannerParams params;

    private Builder() {
      params = new QueryPlannerParams();
    }

    public Builder withOptimalQueries(int optimalQueries) {
      this.params.optimalQueries = optimalQueries;
      return this;
    }

    public Builder withTableType(AnalyticsTableType tableType) {
      this.params.tableType = tableType;
      return this;
    }

    public Builder withQueryGroupers(
        List<Function<DataQueryParams, List<DataQueryParams>>> queryGroupers) {
      this.params.queryGroupers = queryGroupers;
      return this;
    }

    public QueryPlannerParams build() {
      return params;
    }
  }
}
