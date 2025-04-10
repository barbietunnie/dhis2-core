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
package org.hisp.dhis.tracker.imports.config;

import java.util.List;
import java.util.Map;
import org.hisp.dhis.tracker.imports.preheat.supplier.ClassBasedSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.CurrentUserSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.DefaultsSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.DuplicateRelationshipSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.EnrollmentsWithAtLeastOneEventSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.EventCategoryOptionComboSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.EventProgramEnrollmentSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.EventProgramStageMapSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.FileResourceSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.OrgUnitValueTypeSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.PreheatStrategyScanner;
import org.hisp.dhis.tracker.imports.preheat.supplier.PreheatSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.ProgramOrgUnitsSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.ProgramOwnerSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.TrackedEntityEnrollmentSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.UniqueAttributesSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.UserSupplier;
import org.hisp.dhis.tracker.imports.preheat.supplier.UsernameValueTypeSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("trackerPreheatConfig")
public class TrackerPreheatConfig {
  private final List<Class<? extends PreheatSupplier>> preheatOrder =
      List.of(
          ClassBasedSupplier.class,
          DefaultsSupplier.class,
          TrackedEntityEnrollmentSupplier.class,
          EventProgramEnrollmentSupplier.class,
          EnrollmentsWithAtLeastOneEventSupplier.class,
          EventProgramStageMapSupplier.class,
          ProgramOrgUnitsSupplier.class,
          ProgramOwnerSupplier.class,
          UniqueAttributesSupplier.class,
          CurrentUserSupplier.class,
          UserSupplier.class,
          UsernameValueTypeSupplier.class,
          FileResourceSupplier.class,
          EventCategoryOptionComboSupplier.class,
          DuplicateRelationshipSupplier.class,
          OrgUnitValueTypeSupplier.class);

  @Bean("preheatOrder")
  public List<String> getPreheatOrder() {
    return preheatOrder.stream().map(Class::getSimpleName).toList();
  }

  @Bean("preheatStrategies")
  public Map<String, String> getPreheatStrategies() {
    return new PreheatStrategyScanner().scanSupplierStrategies();
  }
}
