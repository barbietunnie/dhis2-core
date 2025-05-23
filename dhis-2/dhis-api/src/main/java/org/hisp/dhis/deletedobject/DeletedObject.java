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
package org.hisp.dhis.deletedobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Entity
@Table(
    name = "deletedobject",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "key_deleted_object_klass_uid",
          columnNames = {"klass", "uid"})
    })
@JacksonXmlRootElement(localName = "deletedObject", namespace = DxfNamespaces.DXF_2_0)
public class DeletedObject implements Serializable {
  /** The database internal identifier for this Object. */
  @Id
  @Column(name = "deletedobjectid")
  @GeneratedValue(generator = "deletedobject_sequence")
  @SequenceGenerator(
      name = "deletedobject_sequence",
      sequenceName = "deletedobject_sequence",
      allocationSize = 1)
  private long id;

  /** Class of object that was deleted. */
  @Column(nullable = false)
  private String klass;

  /** The Unique Identifier for this Object. */
  @Column(nullable = false)
  private String uid;

  /** The unique code for this Object. */
  @Column private String code;

  /** Date this object was deleted. */
  @Column(nullable = false, name = "deleted_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date deletedAt = new Date();

  /** User who deleted this object (if available) */
  @Column(name = "deleted_by")
  private String deletedBy;

  protected DeletedObject() {}

  public DeletedObject(IdentifiableObject identifiableObject) {
    Assert.notNull(identifiableObject, "IdentifiableObject is required and can not be null.");
    Assert.notNull(
        identifiableObject.getUid(), "IdentifiableObject.uid is required and can not be null.");

    this.klass = ClassUtils.getShortName(identifiableObject.getClass());
    this.uid = identifiableObject.getUid();
    this.code =
        !StringUtils.isEmpty(identifiableObject.getCode()) ? identifiableObject.getCode() : null;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getKlass() {
    return klass;
  }

  public void setKlass(String klass) {
    this.klass = klass;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public Date getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Date deletedAt) {
    this.deletedAt = deletedAt;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DeletedObject that = (DeletedObject) o;

    return Objects.equal(klass, that.klass)
        && Objects.equal(uid, that.uid)
        && Objects.equal(code, that.code)
        && Objects.equal(deletedAt, that.deletedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(klass, uid, code, deletedAt);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("klass", klass)
        .add("uid", uid)
        .add("code", code)
        .add("deletedAt", deletedAt)
        .toString();
  }
}
