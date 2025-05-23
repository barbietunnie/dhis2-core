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
package org.hisp.dhis.predictor;

import lombok.AllArgsConstructor;
import org.hisp.dhis.scheduling.Job;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.scheduling.parameters.PredictorJobParameters;
import org.hisp.dhis.system.notification.Notifier;
import org.springframework.stereotype.Component;

/**
 * @author Henning Håkonsen
 */
@Component
@AllArgsConstructor
public class PredictorJob implements Job {
  private final PredictionService predictionService;

  private final Notifier notifier;

  @Override
  public JobType getJobType() {
    return JobType.PREDICTOR;
  }

  @Override
  public void execute(JobConfiguration config, JobProgress progress) {
    PredictorJobParameters params = (PredictorJobParameters) config.getJobParameters();

    if (params == null) {
      throw new IllegalStateException("No job parameters present in predictor job");
    }

    progress.startingProcess("Making predictions");
    PredictionSummary summary = predictionService.predictJob(params, progress);
    notifier.addJobSummary(config, summary, PredictionSummary.class);
    if (summary.getStatus() == PredictionStatus.SUCCESS) {
      progress.completedProcess(summary.getDescription());
    } else {
      progress.failedProcess(summary.getDescription());
    }
  }
}
