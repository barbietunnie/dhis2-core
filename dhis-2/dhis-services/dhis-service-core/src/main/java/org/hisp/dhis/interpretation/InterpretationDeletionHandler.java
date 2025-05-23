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
package org.hisp.dhis.interpretation;

import java.util.List;
import lombok.AllArgsConstructor;
import org.hisp.dhis.eventvisualization.EventVisualization;
import org.hisp.dhis.mapping.Map;
import org.hisp.dhis.system.deletion.DeletionHandler;
import org.hisp.dhis.user.User;
import org.hisp.dhis.visualization.Visualization;
import org.springframework.stereotype.Component;

/**
 * @author Lars Helge Overland
 */
@Component
@AllArgsConstructor
public class InterpretationDeletionHandler extends DeletionHandler {
  private final InterpretationService interpretationService;

  @Override
  protected void register() {
    whenDeleting(User.class, this::deleteUser);
    whenDeleting(Visualization.class, this::deleteVisualizationInterpretations);
    whenDeleting(EventVisualization.class, this::deleteEventVisualizationInterpretations);
    whenDeleting(Map.class, this::deleteMapInterpretations);
  }

  private void deleteUser(User user) {
    List<Interpretation> interpretations = interpretationService.getInterpretations();

    for (Interpretation interpretation : interpretations) {
      if (interpretation.getCreatedBy() != null && interpretation.getCreatedBy().equals(user)) {
        interpretation.setCreatedBy(null);
        interpretationService.updateInterpretation(interpretation);
      }
    }
  }

  private void deleteVisualizationInterpretations(Visualization visualization) {
    List<Interpretation> interpretations = interpretationService.getInterpretations(visualization);

    if (interpretations != null) {
      interpretations.forEach(interpretationService::deleteInterpretation);
    }
  }

  private void deleteEventVisualizationInterpretations(EventVisualization eventVisualization) {
    List<Interpretation> interpretations =
        interpretationService.getInterpretations(eventVisualization);

    if (interpretations != null) {
      interpretations.forEach(interpretationService::deleteInterpretation);
    }
  }

  private void deleteMapInterpretations(Map map) {
    List<Interpretation> interpretations = interpretationService.getInterpretations(map);

    if (interpretations != null) {
      interpretations.forEach(interpretationService::deleteInterpretation);
    }
  }
}
