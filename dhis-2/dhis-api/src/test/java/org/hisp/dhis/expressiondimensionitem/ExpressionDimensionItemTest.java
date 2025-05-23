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
package org.hisp.dhis.expressiondimensionitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hisp.dhis.indicator.Indicator;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExpressionDimensionItem}. */
class ExpressionDimensionItemTest {
  @Test
  void testToIndicator() {
    // Given
    ExpressionDimensionItem expressionDimensionItem = new ExpressionDimensionItem();
    expressionDimensionItem.setExpression(
        "#{R4KStuS8qt7.LbkJRbDblhe} / #{o0fOD1HLuv8.LbkJRbDblhe}");
    expressionDimensionItem.setUid("anyUid");
    expressionDimensionItem.setCode("anyCode");
    expressionDimensionItem.setName("anyName");
    expressionDimensionItem.setDescription("anyDescription");

    // When
    Indicator indicator = expressionDimensionItem.toIndicator();

    // Then
    assertEquals("anyUid", indicator.getUid());
    assertEquals("anyCode", indicator.getCode());
    assertEquals("anyName", indicator.getName());
    assertEquals(
        "#{R4KStuS8qt7.LbkJRbDblhe} / #{o0fOD1HLuv8.LbkJRbDblhe}", indicator.getNumerator());
    assertEquals("1", indicator.getDenominator());
    assertEquals(1, indicator.getIndicatorType().getFactor());
    assertNull(indicator.getDescription());
    assertNull(indicator.getDecimals());
    assertFalse(indicator.isAnnualized());
    assertTrue(indicator.getIndicatorType().isNumber());
  }
}
