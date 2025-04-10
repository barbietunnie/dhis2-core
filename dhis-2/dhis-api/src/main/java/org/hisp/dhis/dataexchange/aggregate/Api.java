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
package org.hisp.dhis.dataexchange.aggregate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * See {@link ApiSerializer} for JSON serialization.
 *
 * @author Lars Helge Overland
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Api implements Serializable {
  @JsonProperty private String url;

  /**
   * Access token. For Personal Access Token (PAT) authentication. The access token is encrypted and
   * must be decrypted before used to authenticate with external systems. Sensitive, do not expose
   * in API output.
   */
  @JsonProperty private String accessToken;

  /** Username. For basic authentication. */
  @JsonProperty private String username;

  /**
   * Password. For basic authentication. The password is encrypted and must be decrypted before used
   * to authenticate with external systems. Sensitive, do not expose in API output.
   */
  @JsonProperty private String password;

  /**
   * Indicates if API is configured for access token based authentication.
   *
   * @return true if API is configured for access token based authentication.
   */
  @JsonIgnore
  public boolean isAccessTokenAuth() {
    return StringUtils.isNotBlank(accessToken);
  }

  /**
   * Indicates if API is configured for basic authentication.
   *
   * @return true if API is configured for basic authentication.
   */
  @JsonIgnore
  public boolean isBasicAuth() {
    return StringUtils.isNoneBlank(username, password);
  }
}
