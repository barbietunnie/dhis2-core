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
package org.hisp.dhis.fileresource;

import java.util.Set;

/**
 * @author Halvdan Hoem Grelland
 */
public enum FileResourceDomain {
  DATA_VALUE("dataValue"),
  PUSH_ANALYSIS("pushAnalysis"),
  DOCUMENT("document"),
  MESSAGE_ATTACHMENT("messageAttachment"),
  USER_AVATAR("userAvatar"),
  ORG_UNIT("organisationUnit"),
  ICON("icon"),
  JOB_DATA("jobData");

  /** Container name to use when storing blobs of this FileResourceDomain */
  private final String containerName;

  public static final Set<FileResourceDomain> DOMAIN_FOR_MULTIPLE_IMAGES =
      Set.of(DATA_VALUE, USER_AVATAR, ORG_UNIT);

  FileResourceDomain(String containerName) {
    this.containerName = containerName;
  }

  public String getContainerName() {
    return containerName;
  }

  public static boolean isDomainForMultipleImages(FileResourceDomain domain) {
    return domain != null && DOMAIN_FOR_MULTIPLE_IMAGES.contains(domain);
  }
}
