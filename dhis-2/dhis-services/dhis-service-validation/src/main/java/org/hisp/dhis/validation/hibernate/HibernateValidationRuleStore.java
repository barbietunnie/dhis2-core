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
package org.hisp.dhis.validation.hibernate;

import static com.google.common.base.Preconditions.checkNotNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.List;
import javax.annotation.Nonnull;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.validation.ValidationRule;
import org.hisp.dhis.validation.ValidationRuleStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author Chau Thu Tran
 */
@Repository("org.hisp.dhis.validation.ValidationRuleStore")
public class HibernateValidationRuleStore extends HibernateIdentifiableObjectStore<ValidationRule>
    implements ValidationRuleStore {
  // -------------------------------------------------------------------------
  // Dependency
  // -------------------------------------------------------------------------

  private final PeriodService periodService;

  public HibernateValidationRuleStore(
      EntityManager entityManager,
      JdbcTemplate jdbcTemplate,
      ApplicationEventPublisher publisher,
      AclService aclService,
      PeriodService periodService) {
    super(entityManager, jdbcTemplate, publisher, ValidationRule.class, aclService, true);

    checkNotNull(periodService);

    this.periodService = periodService;
  }

  // -------------------------------------------------------------------------
  // Implementation
  // -------------------------------------------------------------------------

  @Override
  public void save(@Nonnull ValidationRule validationRule) {
    PeriodType periodType = periodService.reloadPeriodType(validationRule.getPeriodType());

    validationRule.setPeriodType(periodType);

    super.save(validationRule);
  }

  @Override
  public void update(@Nonnull ValidationRule validationRule) {
    PeriodType periodType = periodService.reloadPeriodType(validationRule.getPeriodType());

    validationRule.setPeriodType(periodType);

    super.update(validationRule);
  }

  @Override
  public List<ValidationRule> getAllFormValidationRules() {
    CriteriaBuilder builder = getCriteriaBuilder();

    return getList(
        builder,
        newJpaParameters()
            .addPredicates(getSharingPredicates(builder))
            .addPredicate(root -> builder.equal(root.get("skipFormValidation"), false)));
  }

  @Override
  public List<ValidationRule> getValidationRulesWithNotificationTemplates() {
    CriteriaBuilder builder = getCriteriaBuilder();

    return getList(
        builder,
        newJpaParameters()
            .addPredicate(root -> builder.isNotEmpty(root.get("notificationTemplates")))
            .setUseDistinct(true));
  }

  @Override
  public List<ValidationRule> getValidationRulesWithoutGroups() {
    return getQuery("from ValidationRule vr where size(vr.groups) = 0").list();
  }
}
