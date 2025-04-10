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
package org.hisp.dhis.dataapproval;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Sets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.hisp.dhis.cache.CacheProvider;
import org.hisp.dhis.category.CategoryCombo;
import org.hisp.dhis.category.CategoryOption;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataapproval.hibernate.HibernateDataApprovalStore;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dbms.DbmsManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodStore;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.setting.SystemSettingsService;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.hisp.dhis.user.sharing.UserAccess;
import org.hisp.dhis.user.sharing.UserGroupAccess;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DataApprovalStore tests that no longer work in the H2 database but must be done in the PostgreSQL
 * database.
 *
 * @author Jim Grace
 */
@Transactional
class DataApprovalStoreIntegrationTest extends PostgresIntegrationTestBase {

  private HibernateDataApprovalStore dataApprovalStore;

  @Autowired private DataApprovalLevelService dataApprovalLevelService;

  @Autowired private DataApprovalService dataApprovalService;

  @Autowired private PeriodService periodService;

  @Autowired private PeriodStore periodStore;

  @Autowired private CategoryService categoryService;

  @Autowired private UserGroupService userGroupService;

  @Autowired private OrganisationUnitService organisationUnitService;

  @Autowired private DataSetService dataSetService;

  @PersistenceContext private EntityManager entityManager;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private ApplicationEventPublisher publisher;

  @Autowired private CacheProvider cacheProvider;

  @Autowired private SystemSettingsService settingsService;

  @Autowired private DbmsManager dbmsManager;

  @Autowired private TransactionTemplate transactionTemplate;

  private DataApprovalLevel level1;

  private DataApprovalWorkflow workflowA;

  private OrganisationUnit sourceA;

  private DataSet dataSetA;

  private Period periodJan;

  private Period periodFeb;

  private Period periodMay;

  private Period periodJun;

  private User userA;

  private User userB;

  private UserGroup userGroupA;

  private UserGroup userGroupB;

  private CategoryOption categoryOptionA;

  private CategoryOption categoryOptionB;

  private org.hisp.dhis.category.Category categoryA;

  private CategoryCombo categoryComboA;

  private CategoryOptionCombo categoryOptionCombo;

  private List<DataApprovalLevel> userApprovalLevels;

  @BeforeEach
  void setUp() {
    dataApprovalStore =
        new HibernateDataApprovalStore(
            entityManager,
            jdbcTemplate,
            publisher,
            cacheProvider,
            periodService,
            periodStore,
            categoryService,
            settingsService,
            userService);

    // ---------------------------------------------------------------------
    // Add supporting data
    // ---------------------------------------------------------------------

    level1 = new DataApprovalLevel("01", 1, null);

    dataApprovalLevelService.addDataApprovalLevel(level1);

    userApprovalLevels = List.of(level1);

    sourceA = createOrganisationUnit('A');

    sourceA.setHierarchyLevel(1);

    organisationUnitService.addOrganisationUnit(sourceA);

    periodJan = createPeriod("202001");
    periodFeb = createPeriod("202002");
    periodMay = createPeriod("202005");
    periodJun = createPeriod("202006");

    periodService.addPeriod(periodJan);
    periodService.addPeriod(periodFeb);
    periodService.addPeriod(periodMay);
    periodService.addPeriod(periodJun);

    userA = makeUser("A");
    userB = makeUser("B");

    userA.addOrganisationUnit(sourceA);
    userB.addOrganisationUnit(sourceA);

    userService.addUser(userA);
    userService.addUser(userB);

    userGroupA = createUserGroup('A', Sets.newHashSet(userA));
    userGroupB = createUserGroup('B', Sets.newHashSet(userB));

    userGroupService.addUserGroup(userGroupA);
    userGroupService.addUserGroup(userGroupB);

    userA.getGroups().add(userGroupA);
    userB.getGroups().add(userGroupB);

    userService.updateUser(userA);
    userService.updateUser(userB);

    categoryOptionA = createCategoryOption('A');
    categoryOptionB = createCategoryOption('B');

    categoryOptionA.setPublicAccess("--------");
    categoryOptionB.setPublicAccess("--------");

    categoryService.addCategoryOption(categoryOptionA);
    categoryService.addCategoryOption(categoryOptionB);

    categoryOptionA.setPublicAccess("--------");
    categoryOptionB.setPublicAccess("--------");
    categoryService.updateCategoryOption(categoryOptionA);
    categoryService.updateCategoryOption(categoryOptionB);

    categoryA = createCategory('A', categoryOptionA, categoryOptionB);

    categoryService.addCategory(categoryA);

    categoryComboA = createCategoryCombo('A', categoryA);

    categoryService.addCategoryCombo(categoryComboA);

    categoryOptionCombo =
        createCategoryOptionCombo(categoryComboA, categoryOptionA, categoryOptionB);
    categoryOptionCombo.setName("categoryOptionCombo");
    categoryOptionCombo.setUid("optComboUid");

    categoryService.addCategoryOptionCombo(categoryOptionCombo);

    categoryComboA.getOptionCombos().add(categoryOptionCombo);

    categoryService.updateCategoryCombo(categoryComboA);

    PeriodType periodType = PeriodType.getPeriodTypeByName("Monthly");

    workflowA =
        new DataApprovalWorkflow("workflowA1", periodType, categoryComboA, newHashSet(level1));

    dataApprovalService.addWorkflow(workflowA);

    dataSetA = createDataSet('A', periodType, categoryComboA);
    dataSetA.assignWorkflow(workflowA);
    dataSetA.addOrganisationUnit(sourceA);

    dataSetService.addDataSet(dataSetA);

    dbmsManager.flushSession();
    dbmsManager.clearSession();
  }

  @Test
  void testGetDataApprovalStatusesWithOpenPeriodsAfterCoEndDate() {

    transactionTemplate.execute(
        status -> {
          categoryOptionA.setPublicAccess("r-r-----");
          categoryOptionB.setPublicAccess("r-r-----");

          categoryService.updateCategoryOption(categoryOptionA);
          categoryService.updateCategoryOption(categoryOptionB);

          dataApprovalLevelService.addDataApprovalLevel(level1);

          hibernateService.flushSession();
          injectSecurityContextUser(userA);

          assertEquals(
              1,
              dataApprovalStore
                  .getDataApprovalStatuses(
                      workflowA,
                      periodJan,
                      null,
                      1,
                      null,
                      categoryComboA,
                      null,
                      userApprovalLevels,
                      null)
                  .size());
          assertEquals(
              1,
              dataApprovalStore
                  .getDataApprovalStatuses(
                      workflowA,
                      periodFeb,
                      null,
                      1,
                      null,
                      categoryComboA,
                      null,
                      userApprovalLevels,
                      null)
                  .size());
          assertEquals(
              1,
              dataApprovalStore
                  .getDataApprovalStatuses(
                      workflowA,
                      periodMay,
                      null,
                      1,
                      null,
                      categoryComboA,
                      null,
                      userApprovalLevels,
                      null)
                  .size());
          assertEquals(
              1,
              dataApprovalStore
                  .getDataApprovalStatuses(
                      workflowA,
                      periodJun,
                      null,
                      1,
                      null,
                      categoryComboA,
                      null,
                      userApprovalLevels,
                      null)
                  .size());

          categoryOptionA.setStartDate(new DateTime(2020, 1, 1, 0, 0).toDate());
          categoryOptionA.setEndDate(new DateTime(2020, 5, 30, 0, 0).toDate());

          categoryOptionB.setStartDate(new DateTime(2020, 2, 1, 0, 0).toDate());
          categoryOptionB.setEndDate(new DateTime(2020, 6, 30, 0, 0).toDate());

          clearSecurityContext();

          hibernateService.flushSession();
          injectAdminIntoSecurityContext();

          categoryService.updateCategoryOption(categoryOptionA);
          categoryService.updateCategoryOption(categoryOptionB);

          dbmsManager.clearSession();
          return null;
        });

    injectSecurityContextUser(userA);

    assertEquals(
        0,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodJan, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
    assertEquals(
        1,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodFeb, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
    assertEquals(
        1,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodMay, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
    assertEquals(
        0,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodJun, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());

    dataSetA.setOpenPeriodsAfterCoEndDate(1);

    injectAdminIntoSecurityContext();
    dataSetService.updateDataSet(dataSetA);

    injectSecurityContextUser(userA);

    assertEquals(
        0,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodJan, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
    assertEquals(
        1,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodFeb, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
    assertEquals(
        1,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodMay, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
    assertEquals(
        1,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodJun, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());
  }

  @Test
  void testApprovalStatusWithNoAccess() {
    transactionTemplate.execute(
        status -> {
          sharingTest(0);

          return null;
        });
  }

  @Test
  void testApprovalStatusWithOtherUserAccess() {
    transactionTemplate.execute(
        status -> {
          categoryOptionA.getSharing().setOwner(userB);
          categoryOptionB.getSharing().setOwner(userB);

          categoryOptionA.getSharing().addUserAccess(new UserAccess(userB, "r-r-----"));
          categoryOptionB.getSharing().addUserAccess(new UserAccess(userB, "r-r-----"));

          categoryOptionA
              .getSharing()
              .addUserGroupAccess(new UserGroupAccess(userGroupB, "r-r-----"));
          categoryOptionB
              .getSharing()
              .addUserGroupAccess(new UserGroupAccess(userGroupB, "r-r-----"));

          sharingTest(0);

          return null;
        });
  }

  @Test
  void testApprovalStatusWithPublic() {
    transactionTemplate.execute(
        status -> {
          categoryOptionA.getSharing().setPublicAccess("r-r-----");
          categoryOptionB.getSharing().setPublicAccess("r-r-----");

          sharingTest(1);

          return null;
        });
  }

  @Test
  void testApprovalStatusWithOwner() {
    transactionTemplate.execute(
        status -> {
          categoryOptionA.getSharing().setOwner(userA);
          categoryOptionB.getSharing().setOwner(userA);

          sharingTest(1);

          return null;
        });
  }

  @Test
  void testApprovalStatusWithUserSharing() {
    transactionTemplate.execute(
        status -> {
          categoryOptionA.getSharing().addUserAccess(new UserAccess(userA, "r-r-----"));
          categoryOptionB.getSharing().addUserAccess(new UserAccess(userA, "r-r-----"));

          sharingTest(1);

          return null;
        });
  }

  @Test
  void testApprovalStatusWithUserGroupSharing() {
    transactionTemplate.execute(
        status -> {
          categoryOptionA
              .getSharing()
              .addUserGroupAccess(new UserGroupAccess(userGroupA, "r-r-----"));
          categoryOptionB
              .getSharing()
              .addUserGroupAccess(new UserGroupAccess(userGroupA, "r-r-----"));

          sharingTest(1);

          return null;
        });
  }

  private void sharingTest(int expectedApprovalCount) {
    categoryService.updateCategoryOption(categoryOptionA);
    categoryService.updateCategoryOption(categoryOptionB);

    dataApprovalLevelService.addDataApprovalLevel(level1);

    hibernateService.flushSession();
    injectSecurityContextUser(userA);

    assertEquals(
        expectedApprovalCount,
        dataApprovalStore
            .getDataApprovalStatuses(
                workflowA, periodFeb, null, 1, null, categoryComboA, null, userApprovalLevels, null)
            .size());

    dbmsManager.clearSession();
  }
}
