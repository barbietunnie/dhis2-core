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
package org.hisp.dhis.tracker.imports.programrule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.programrule.ProgramRuleVariable;
import org.hisp.dhis.programrule.ProgramRuleVariableService;
import org.hisp.dhis.programrule.ProgramRuleVariableSourceType;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.tracker.TestSetup;
import org.hisp.dhis.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zubair Asghar
 */
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProgramRuleVariableIntegrationTest extends PostgresIntegrationTestBase {
  @Autowired private TestSetup testSetup;

  @Autowired private ProgramRuleVariableService programRuleVariableService;

  @Autowired private ProgramService programService;

  @Autowired private TrackedEntityAttributeService trackedEntityAttributeService;

  @BeforeAll
  void setUp() throws IOException {
    testSetup.importMetadata("tracker/tracker_metadata_with_program_rules_variables.json");

    User importUser = userService.getUser("tTgjgobT1oS");
    injectSecurityContextUser(importUser);
  }

  @Test
  public void shouldAssignValueTypeFromTrackedEntityAttributeToProgramRuleVariable() {
    Program program = programService.getProgram("BFcipDERJne");
    TrackedEntityAttribute trackedEntityAttribute =
        trackedEntityAttributeService.getTrackedEntityAttribute("sYn3tkL3XKa");
    List<ProgramRuleVariable> ruleVariables =
        programRuleVariableService.getProgramRuleVariable(program);

    ProgramRuleVariable prv =
        ruleVariables.stream()
            .filter(r -> ProgramRuleVariableSourceType.TEI_ATTRIBUTE == r.getSourceType())
            .findFirst()
            .get();
    assertEquals(trackedEntityAttribute.getValueType(), prv.getValueType());
  }

  @Test
  public void shouldAssignDefaultValueTypeToProgramRuleVariable() {
    Program program = programService.getProgram("BFcipDERJne");
    List<ProgramRuleVariable> ruleVariables =
        programRuleVariableService.getProgramRuleVariable(program);

    ProgramRuleVariable prv =
        ruleVariables.stream()
            .filter(r -> ProgramRuleVariableSourceType.CALCULATED_VALUE == r.getSourceType())
            .findFirst()
            .get();
    assertEquals(ValueType.TEXT, prv.getValueType());
  }
}
