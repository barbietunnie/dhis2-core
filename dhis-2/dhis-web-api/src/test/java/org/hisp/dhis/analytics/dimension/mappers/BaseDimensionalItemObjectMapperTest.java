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
package org.hisp.dhis.analytics.dimension.mappers;

import static org.hisp.dhis.analytics.dimension.DimensionMapperTestSupport.asserter;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.hisp.dhis.common.BaseDimensionalItemObject;
import org.hisp.dhis.common.DimensionItemType;
import org.hisp.dhis.webapi.dimension.DimensionResponse;
import org.hisp.dhis.webapi.dimension.mappers.BaseDimensionalItemObjectMapper;
import org.junit.jupiter.api.Test;

class BaseDimensionalItemObjectMapperTest {
  private static final DimensionItemType DIMENSION_ITEM_TYPE = DimensionItemType.INDICATOR;

  @Test
  void testDimensionalItemObjectMapper() {
    asserter(
        new BaseDimensionalItemObjectMapper(),
        BaseDimensionalItemObject::new,
        List.of(b -> b.setDimensionItemType(DIMENSION_ITEM_TYPE)),
        List.of(Pair.of(DimensionResponse::getDimensionType, DIMENSION_ITEM_TYPE)));
  }
}
