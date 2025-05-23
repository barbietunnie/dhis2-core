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
package org.hisp.dhis.common;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * UID represents an alphanumeric string of 11 characters starting with a letter.
 *
 * <p>A "virtual" UID type that is "context-sensitive" and points to a UID of the current {@code
 * Api.Endpoint}'s {@link org.hisp.dhis.common.OpenApi.EntityType}.
 *
 * <p>In other words by using this type in {@link OpenApi.Param#value()} the annotated parameter
 * becomes a UID string of the controllers' entity type.
 *
 * @author Jan Bernitt
 */
@Getter
@EqualsAndHashCode
public final class UID implements Serializable {

  private final String value;

  private UID(String value) {
    if (!CodeGenerator.isValidUid(value)) {
      throw new IllegalArgumentException(
          "UID must be an alphanumeric string of 11 characters starting with a letter, but was: "
              + value);
    }
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return value;
  }

  public static UID generate() {
    return new UID(CodeGenerator.generateUid());
  }

  @JsonCreator
  public static UID of(@Nonnull String value) {
    return new UID(value);
  }

  public static UID of(@CheckForNull UidObject object) {
    return object == null ? null : new UID(object.getUid());
  }

  public static Set<UID> of(@Nonnull String... values) {
    return Stream.of(values).map(UID::of).collect(toUnmodifiableSet());
  }

  public static Set<UID> of(@Nonnull Collection<String> values) {
    return values.stream().map(UID::of).collect(toUnmodifiableSet());
  }

  public static Set<UID> of(@Nonnull UidObject... objects) {
    return Stream.of(objects).map(obj -> UID.of(obj.getUid())).collect(toUnmodifiableSet());
  }

  public static Set<UID> of(@Nonnull Stream<? extends UidObject> s) {
    return s.map(el -> UID.of(el.getUid())).collect(Collectors.toSet());
  }

  public static Set<String> toValueSet(Collection<UID> uids) {
    return uids.stream().map(UID::getValue).collect(toUnmodifiableSet());
  }

  public static List<String> toValueList(Collection<UID> uids) {
    return uids.stream().map(UID::getValue).toList();
  }

  public static <T extends BaseIdentifiableObject> Set<String> toUidValueSet(
      @Nonnull Collection<T> elements) {
    return elements.stream().map(BaseIdentifiableObject::getUid).collect(toUnmodifiableSet());
  }
}
