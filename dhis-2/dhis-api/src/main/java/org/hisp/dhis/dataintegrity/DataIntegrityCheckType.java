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
package org.hisp.dhis.dataintegrity;

/**
 * The different types of data integrity checks one can run
 *
 * @author Jan Bernitt
 */
public enum DataIntegrityCheckType {
  /*
   * Please note that the integrity checks will be performed in the order
   * given by the types.
   */

  // DataElements
  DATA_ELEMENTS_IN_DATA_SET_NOT_IN_FORM,

  // Indicators
  INDICATORS_WITH_IDENTICAL_FORMULAS,
  INDICATORS_WITH_INVALID_NUMERATOR,
  INDICATORS_WITH_INVALID_DENOMINATOR,

  // OrganisationUnits
  ORG_UNITS_WITH_CYCLIC_REFERENCES,

  // ValidationRules
  VALIDATION_RULES_WITH_INVALID_LEFT_SIDE_EXPRESSION,
  VALIDATION_RULES_WITH_INVALID_RIGHT_SIDE_EXPRESSION,

  // ProgramIndicators
  PROGRAM_INDICATORS_WITH_INVALID_EXPRESSIONS,
  PROGRAM_INDICATORS_WITH_INVALID_FILTERS,

  // ProgramRuleVariables
  PROGRAM_RULE_VARIABLES_WITHOUT_DATA_ELEMENT,
  PROGRAM_RULE_VARIABLES_WITHOUT_ATTRIBUTE,

  // ProgramRuleActions
  PROGRAM_RULE_ACTIONS_WITHOUT_DATA_OBJECT,
  PROGRAM_RULE_ACTIONS_WITHOUT_NOTIFICATION,
  PROGRAM_RULE_ACTIONS_WITHOUT_SECTION,
  PROGRAM_RULE_ACTIONS_WITHOUT_STAGE_ID;

  public String getName() {
    return name().toLowerCase();
  }
}
