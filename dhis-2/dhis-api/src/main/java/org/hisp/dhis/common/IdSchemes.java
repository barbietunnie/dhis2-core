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
package org.hisp.dhis.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hisp.dhis.util.ObjectUtils;

/**
 * Identifier schemes used to map meta data. The general identifier scheme can be overridden by id
 * schemes specific to individual object types. The default id scheme is UID.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@ToString
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(setterVisibility = Visibility.NONE, getterVisibility = Visibility.NONE)
public class IdSchemes implements Serializable {
  public static final IdScheme DEFAULT_ID_SCHEME = IdScheme.UID;

  @OpenApi.Property(value = IdentifiableProperty.class)
  @JsonProperty
  private IdScheme idScheme;

  @JsonProperty private IdScheme dataElementIdScheme;

  @JsonProperty private IdScheme dataElementGroupIdScheme;

  @JsonProperty private IdScheme categoryOptionComboIdScheme;

  @JsonProperty private IdScheme categoryOptionIdScheme;

  @JsonProperty private IdScheme categoryIdScheme;

  @JsonProperty private IdScheme orgUnitIdScheme;

  @JsonProperty private IdScheme orgUnitGroupIdScheme;

  @JsonProperty private IdScheme programIdScheme;

  @JsonProperty private IdScheme programStageIdScheme;

  @JsonProperty private IdScheme trackedEntityAttributeIdScheme;

  @JsonProperty private IdScheme dataSetIdScheme;

  @JsonProperty private IdScheme attributeOptionComboIdScheme;

  public IdSchemes() {}

  public IdScheme getScheme(IdScheme idScheme) {
    return IdScheme.from(ObjectUtils.firstNonNull(idScheme, getIdScheme()));
  }

  public IdScheme getIdScheme() {
    return IdScheme.from(ObjectUtils.firstNonNull(idScheme, DEFAULT_ID_SCHEME));
  }

  public IdSchemes setIdScheme(String idScheme) {
    this.idScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdSchemes setDefaultIdScheme(IdScheme idScheme) {
    if (this.idScheme == null) {
      this.idScheme = idScheme;
    }
    return this;
  }

  // --------------------------------------------------------------------------
  // Object type id schemes
  // --------------------------------------------------------------------------

  public IdScheme getDataElementIdScheme() {
    return getScheme(dataElementIdScheme);
  }

  public IdSchemes setDataElementIdScheme(String idScheme) {
    this.dataElementIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getDataElementGroupIdScheme() {
    return getScheme(dataElementGroupIdScheme);
  }

  public IdSchemes setDataElementGroupIdScheme(String idScheme) {
    this.dataElementGroupIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getCategoryOptionComboIdScheme() {
    return getScheme(categoryOptionComboIdScheme);
  }

  public IdSchemes setCategoryOptionComboIdScheme(String idScheme) {
    this.categoryOptionComboIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getCategoryOptionIdScheme() {
    return getScheme(categoryOptionIdScheme);
  }

  public IdSchemes setCategoryOptionIdScheme(String idScheme) {
    this.categoryOptionIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getCategoryIdScheme() {
    return getScheme(categoryIdScheme);
  }

  public IdSchemes setCategoryIdScheme(String idScheme) {
    this.categoryIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getAttributeOptionComboIdScheme() {
    return getScheme(attributeOptionComboIdScheme);
  }

  public IdSchemes setAttributeOptionComboIdScheme(String idScheme) {
    this.attributeOptionComboIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getDataSetIdScheme() {
    return getScheme(dataSetIdScheme);
  }

  public IdSchemes setDataSetIdScheme(String idScheme) {
    this.dataSetIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getOrgUnitIdScheme() {
    return getScheme(orgUnitIdScheme);
  }

  public IdSchemes setOrgUnitIdScheme(String idScheme) {
    this.orgUnitIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getOrgUnitGroupIdScheme() {
    return getScheme(orgUnitGroupIdScheme);
  }

  public IdSchemes setOrgUnitGroupIdScheme(String idScheme) {
    this.orgUnitGroupIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getProgramIdScheme() {
    return getScheme(programIdScheme);
  }

  public IdSchemes setProgramIdScheme(String idScheme) {
    this.programIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getProgramStageIdScheme() {
    return getScheme(programStageIdScheme);
  }

  public IdSchemes setProgramStageIdScheme(String idScheme) {
    this.programStageIdScheme = IdScheme.from(idScheme);
    return this;
  }

  public IdScheme getTrackedEntityAttributeIdScheme() {
    return getScheme(trackedEntityAttributeIdScheme);
  }

  public IdSchemes setTrackedEntityAttributeIdScheme(String idScheme) {
    this.trackedEntityAttributeIdScheme = IdScheme.from(idScheme);
    return this;
  }

  // --------------------------------------------------------------------------
  // Get value methods
  // --------------------------------------------------------------------------

  public static String getValue(
      String uid, String code, IdentifiableProperty identifiableProperty) {
    return getValue(uid, code, IdScheme.from(identifiableProperty));
  }

  public static String getValue(String uid, String code, IdScheme idScheme) {
    boolean isId = idScheme.is(IdentifiableProperty.ID) || idScheme.is(IdentifiableProperty.UID);

    return isId ? uid : code;
  }

  public static String getValue(
      IdentifiableObject identifiableObject, IdentifiableProperty identifiableProperty) {
    return getValue(identifiableObject, IdScheme.from(identifiableProperty));
  }

  public static String getValue(IdentifiableObject identifiableObject, IdScheme idScheme) {
    boolean isId = idScheme.is(IdentifiableProperty.ID) || idScheme.is(IdentifiableProperty.UID);

    if (isId) {
      return identifiableObject.getUid();
    } else if (idScheme.is(IdentifiableProperty.CODE)) {
      return identifiableObject.getCode();
    } else if (idScheme.is(IdentifiableProperty.NAME)) {
      return identifiableObject.getName();
    }

    return null;
  }
}
