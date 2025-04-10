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
package org.hisp.dhis.icon;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * This class is the API response representation of the Icon class. It is used to serialize and
 * deserialize Icon objects. Ideally it should live in the dhis-web-api module, but it is not
 * possible to do so because we need to use it in IconSchemaDescriptor, which is in the
 * dhis-service-schema module.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IconResponse {

  @JsonProperty private String uid;

  @JsonProperty private String code;

  @JsonProperty private String key;

  @JsonProperty private String description;

  @JsonProperty private Set<String> keywords = new HashSet<>();

  @JsonProperty private String fileResourceUid;

  @JsonProperty private String userUid;

  @JsonProperty("href")
  private String reference;

  @JsonProperty private Date created;

  @JsonProperty private Date lastUpdated;

  @JsonProperty private Boolean custom;

  public IconResponse(
      String key,
      String description,
      Set<String> keywords,
      String reference,
      Date created,
      Date lastUpdated) {
    this.key = key;
    this.description = description;
    this.keywords = keywords;
    this.reference = reference;
    this.created = created;
    this.lastUpdated = lastUpdated;
  }
}
