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
package org.hisp.dhis.trackedentity;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.fileresource.FileResource;
import org.hisp.dhis.fileresource.FileResourceService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramTrackedEntityAttributeStore;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.user.CurrentUserUtil;
import org.hisp.dhis.user.UserDetails;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.util.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author Abyot Asalefew
 */
@RequiredArgsConstructor
@Service("org.hisp.dhis.trackedentity.TrackedEntityAttributeService")
public class DefaultTrackedEntityAttributeService implements TrackedEntityAttributeService {
  private static final int VALUE_MAX_LENGTH = 50000;

  private static final Set<String> VALID_IMAGE_FORMATS =
      ImmutableSet.<String>builder().add(ImageIO.getReaderFormatNames()).build();

  // -------------------------------------------------------------------------
  // Dependencies
  // -------------------------------------------------------------------------

  private final TrackedEntityAttributeStore attributeStore;

  private final ProgramService programService;

  private final TrackedEntityTypeService trackedEntityTypeService;

  private final FileResourceService fileResourceService;

  private final UserService userService;

  private final AclService aclService;

  private final TrackedEntityAttributeStore trackedEntityAttributeStore;

  private final TrackedEntityTypeAttributeStore entityTypeAttributeStore;

  private final ProgramTrackedEntityAttributeStore programAttributeStore;

  private final OrganisationUnitService organisationUnitService;

  // -------------------------------------------------------------------------
  // Implementation methods
  // -------------------------------------------------------------------------

  @Override
  @Transactional
  public void deleteTrackedEntityAttribute(TrackedEntityAttribute attribute) {
    attributeStore.delete(attribute);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getAllTrackedEntityAttributes() {
    return attributeStore.getAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getProgramTrackedEntityAttributes(List<Program> programs) {
    return programAttributeStore.getAttributes(programs);
  }

  @Override
  @Transactional(readOnly = true)
  public TrackedEntityAttribute getTrackedEntityAttribute(long id) {
    return attributeStore.get(id);
  }

  @Override
  @Transactional
  public long addTrackedEntityAttribute(TrackedEntityAttribute attribute) {
    attributeStore.save(attribute);
    return attribute.getId();
  }

  @Override
  @Transactional
  public void updateTrackedEntityAttribute(TrackedEntityAttribute attribute) {
    attributeStore.update(attribute);
  }

  @Override
  @Transactional(readOnly = true)
  public TrackedEntityAttribute getTrackedEntityAttributeByName(String name) {
    return attributeStore.getByName(name);
  }

  @Override
  @Transactional(readOnly = true)
  public TrackedEntityAttribute getTrackedEntityAttribute(String uid) {
    return attributeStore.getByUid(uid);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getTrackedEntityAttributes(@Nonnull List<String> uids) {
    return attributeStore.getByUid(uids);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getTrackedEntityAttributesById(List<Long> ids) {
    return attributeStore.getById(ids);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getTrackedEntityAttributesByDisplayOnVisitSchedule(
      boolean displayOnVisitSchedule) {
    return attributeStore.getByDisplayOnVisitSchedule(displayOnVisitSchedule);
  }

  @Override
  @Transactional(readOnly = true)
  public String validateValueType(TrackedEntityAttribute trackedEntityAttribute, String value) {
    Assert.notNull(trackedEntityAttribute, "tracked entity attribute is required");
    ValueType valueType = trackedEntityAttribute.getValueType();

    String errorValue = StringUtils.substring(value, 0, 30);

    if (value.length() > VALUE_MAX_LENGTH) {
      return "Value length is greater than 50000 chars for attribute "
          + trackedEntityAttribute.getUid();
    }

    if (ValueType.NUMBER == valueType && !MathUtils.isNumeric(value)) {
      return "Value '"
          + errorValue
          + "' is not a valid numeric type for attribute "
          + trackedEntityAttribute.getUid();
    } else if (ValueType.BOOLEAN == valueType && !MathUtils.isBool(value)) {
      return "Value '"
          + errorValue
          + "' is not a valid boolean type for attribute "
          + trackedEntityAttribute.getUid();
    } else if (ValueType.DATE == valueType && DateUtils.parseDate(value) == null) {
      return "Value '"
          + errorValue
          + "' is not a valid date type for attribute "
          + trackedEntityAttribute.getUid();
    } else if (ValueType.TRUE_ONLY == valueType && !"true".equals(value)) {
      return "Value '"
          + errorValue
          + "' is not true (true-only type) for attribute "
          + trackedEntityAttribute.getUid();
    } else if (ValueType.USERNAME == valueType) {
      if (userService.getUserByUsername(value) == null) {
        return "Value '"
            + errorValue
            + "' is not a valid username for attribute "
            + trackedEntityAttribute.getUid();
      }
    } else if (ValueType.DATE == valueType && !DateUtils.dateIsValid(value)) {
      return "Value '"
          + errorValue
          + "' is not a valid date for attribute "
          + trackedEntityAttribute.getUid();
    } else if (ValueType.DATETIME == valueType && !DateUtils.dateTimeIsValid(value)) {
      return "Value '"
          + errorValue
          + "' is not a valid datetime for attribute "
          + trackedEntityAttribute.getUid();
    } else if (ValueType.IMAGE == valueType) {
      return validateImage(value);
    } else if (null != trackedEntityAttribute.getOptionSet()
        && trackedEntityAttribute.getOptionSet().getOptions().stream()
            .filter(Objects::nonNull)
            .noneMatch(o -> o.getCode().equalsIgnoreCase(value))) {
      return "Value '"
          + errorValue
          + "' is not a valid option for attribute "
          + trackedEntityAttribute.getUid()
          + " and option set "
          + trackedEntityAttribute.getOptionSet().getUid();
    } else if (ValueType.FILE_RESOURCE == valueType
        && fileResourceService.getFileResource(value) == null) {
      return "Value '" + value + "' is not a valid file resource.";
    } else if (ValueType.ORGANISATION_UNIT == valueType
        && organisationUnitService.getOrganisationUnit(value) == null) {
      return "Value '" + value + "' is not a valid organisation unit.";
    }

    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<TrackedEntityAttribute> getProgramAttributes(Program program) {
    return getAllUserReadableTrackedEntityAttributes(List.of(program), List.of());
  }

  @Override
  @Transactional(readOnly = true)
  public Set<TrackedEntityAttribute> getTrackedEntityTypeAttributes(
      TrackedEntityType trackedEntityType) {
    return getAllUserReadableTrackedEntityAttributes(List.of(), List.of(trackedEntityType));
  }

  @Override
  @Transactional(readOnly = true)
  public Set<TrackedEntityAttribute> getAllUserReadableTrackedEntityAttributes(
      List<Program> programs, List<TrackedEntityType> trackedEntityTypes) {
    UserDetails userDetails = CurrentUserUtil.getCurrentUserDetails();
    Set<TrackedEntityAttribute> attributes = new HashSet<>();

    if (programs != null && !programs.isEmpty()) {
      attributes.addAll(
          programAttributeStore.getAttributes(
              programs.stream()
                  .filter(program -> aclService.canDataRead(userDetails, program))
                  .collect(toList())));
    }

    if (trackedEntityTypes != null && !trackedEntityTypes.isEmpty()) {
      attributes.addAll(
          entityTypeAttributeStore.getAttributes(
              trackedEntityTypes.stream()
                  .filter(
                      trackedEntityType -> aclService.canDataRead(userDetails, trackedEntityType))
                  .collect(toList())));
    }

    return attributes;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<TrackedEntityAttribute> getAllTrigramIndexableTrackedEntityAttributes() {
    return attributeStore.getAllSearchableAndUniqueTrackedEntityAttributes();
  }

  // -------------------------------------------------------------------------
  // ProgramTrackedEntityAttribute
  // -------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getAllSystemWideUniqueTrackedEntityAttributes() {
    return getAllTrackedEntityAttributes().stream()
        .filter(TrackedEntityAttribute::isSystemWideUnique)
        .collect(toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackedEntityAttribute> getAllUniqueTrackedEntityAttributes() {
    return getAllTrackedEntityAttributes().stream()
        .filter(TrackedEntityAttribute::isUnique)
        .collect(toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Set<TrackedEntityAttribute> getTrackedEntityAttributesByTrackedEntityTypes() {
    return this.trackedEntityAttributeStore.getTrackedEntityAttributesByTrackedEntityTypes();
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Program, Set<TrackedEntityAttribute>> getTrackedEntityAttributesByProgram() {
    return this.trackedEntityAttributeStore.getTrackedEntityAttributesByProgram();
  }

  private String validateImage(String uid) {
    FileResource fileResource = fileResourceService.getFileResource(uid);

    if (fileResource == null) {
      return "Value '" + uid + "' is not the uid of a file";
    } else if (!VALID_IMAGE_FORMATS.contains(fileResource.getFormat())) {
      return "File resource with uid '" + uid + "' is not a valid image";
    }

    return null;
  }
}
