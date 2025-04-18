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
package org.hisp.dhis.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.hisp.dhis.common.BaseDimensionalItemObject;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DataDimensionType;
import org.hisp.dhis.common.DimensionItemType;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.MetadataObject;

/**
 * @author Lars Helge Overland
 */
@JacksonXmlRootElement(localName = "categoryOptionGroup", namespace = DxfNamespaces.DXF_2_0)
public class CategoryOptionGroup extends BaseDimensionalItemObject implements MetadataObject {
  private Set<CategoryOption> members = new HashSet<>();

  private Set<CategoryOptionGroupSet> groupSets = new HashSet<>();

  private DataDimensionType dataDimensionType;

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  public CategoryOptionGroup() {}

  public CategoryOptionGroup(String name) {
    this();
    this.name = name;
  }

  public CategoryOptionGroup(String name, DataDimensionType dataDimensionType) {
    this(name);
    this.dataDimensionType = dataDimensionType;
  }

  // -------------------------------------------------------------------------
  // DimensionalItemObject
  // -------------------------------------------------------------------------

  @Override
  public DimensionItemType getDimensionItemType() {
    return DimensionItemType.CATEGORY_OPTION_GROUP;
  }

  // -------------------------------------------------------------------------
  // Getters and setters
  // -------------------------------------------------------------------------

  @JsonProperty("categoryOptions")
  @JsonSerialize(contentAs = BaseIdentifiableObject.class)
  @JacksonXmlElementWrapper(localName = "categoryOptions", namespace = DxfNamespaces.DXF_2_0)
  @JacksonXmlProperty(localName = "categoryOption", namespace = DxfNamespaces.DXF_2_0)
  public Set<CategoryOption> getMembers() {
    return members;
  }

  public void setMembers(Set<CategoryOption> members) {
    this.members = members;
  }

  @JsonProperty
  @JsonSerialize(contentAs = BaseIdentifiableObject.class)
  @JacksonXmlElementWrapper(localName = "groupSets", namespace = DxfNamespaces.DXF_2_0)
  @JacksonXmlProperty(localName = "groupSet", namespace = DxfNamespaces.DXF_2_0)
  public Set<CategoryOptionGroupSet> getGroupSets() {
    return groupSets;
  }

  public void setGroupSets(Set<CategoryOptionGroupSet> groupSets) {
    this.groupSets = groupSets;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public DataDimensionType getDataDimensionType() {
    return dataDimensionType;
  }

  public void setDataDimensionType(DataDimensionType dataDimensionType) {
    this.dataDimensionType = dataDimensionType;
  }

  // -------------------------------------------------------------------------
  // Logic
  // -------------------------------------------------------------------------

  public void addCategoryOption(CategoryOption categoryOption) {
    members.add(categoryOption);
    categoryOption.getGroups().add(this);
  }

  public void removeCategoryOption(CategoryOption categoryOption) {
    members.remove(categoryOption);
    categoryOption.getGroups().remove(this);
  }

  public void removeCategoryOptions(Collection<CategoryOption> categoryOptions) {
    categoryOptions.forEach(this::removeCategoryOption);
  }
}
