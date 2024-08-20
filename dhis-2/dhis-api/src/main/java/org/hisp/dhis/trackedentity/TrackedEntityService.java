/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
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
package org.hisp.dhis.trackedentity;

import java.util.Date;
import java.util.Set;

/**
 * This interface is responsible for retrieving tracked entities (TE). The query methods accepts a
 * TrackedEntityQueryParams object which encapsulates all arguments.
 *
 * <p>
 *
 * <p>The TEs are returned as a Grid object, which is a two-dimensional list with headers. The TE
 * attribute values are returned in the same order as specified in the arguments. The grid has a set
 * of columns which are always present starting at index 0, followed by attributes specified for the
 * query. All values in the grid are of type String. The order is:
 *
 * <p>
 *
 * <ul>
 *   <li>0: Tracked entity UID
 *   <li>1: Created time stamp
 *   <li>2: Last updated time stamp
 *   <li>3: Organisation unit UID
 *   <li>4: Tracked entity UID
 *       <ul>
 *         <p>
 *         <p>Attributes specified in the query follows on the next column indexes. Example usage
 *         for retrieving TEs with two attributes using one attribute as filter:
 *         <pre>
 * <code>
 * TrackedEntityQueryParams params = new TrackedEntityQueryParams();
 *
 * params.addAttribute( new QueryItem( gender, QueryOperator.EQ, "Male", false ) );
 * params.addAttribute( new QueryItem( age, QueryOperator.LT, "5", true ) );
 * params.addFilter( new QueryItem( weight, QueryOperator.GT, "2500", true ) );
 * params.addOrganisationUnit( unit );
 *
 * Grid instances = teService.getTrackedEntityGrid( params );
 *
 * for ( List&lt;Object&gt; row : instances.getRows() )
 * {
 *     String te = row.get( 0 );
 *     String ou = row.get( 3 );
 *     String gender = row.get( 5 );
 *     String age = row.get( 6 );
 * }
 * </code>
 * </pre>
 *
 * @author Abyot Asalefew Gizaw
 * @author Lars Helge Overland
 */
public interface TrackedEntityService {
  String ID = TrackedEntityService.class.getName();

  String SEPARATOR = "_";

  /** */
  void updateTrackedEntityLastUpdated(
      Set<String> trackedEntityUIDs, Date lastUpdated, String userInfoSnapshot);
}
