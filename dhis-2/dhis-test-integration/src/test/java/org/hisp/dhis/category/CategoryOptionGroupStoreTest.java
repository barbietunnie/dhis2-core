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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.hibernate.PropertyValueException;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test needs to extend DhisTest in order to test the bidirectional group set to group association
 * from both sides as save transactions must commit.
 *
 * @author Lars Helge Overland
 */
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
class CategoryOptionGroupStoreTest extends PostgresIntegrationTestBase {

  @Autowired private CategoryService categoryService;

  @Autowired private CategoryOptionGroupStore categoryOptionGroupStore;

  private CategoryOption coA;

  private CategoryOption coB;

  private CategoryOption coC;

  private CategoryOption coD;

  private CategoryOption coE;

  private CategoryOption coF;

  private CategoryOption coG;

  private CategoryOption coH;

  @BeforeAll
  void setUp() {
    coA = createCategoryOption('A');
    coB = createCategoryOption('B');
    coC = createCategoryOption('C');
    coD = createCategoryOption('D');
    coE = createCategoryOption('E');
    coF = createCategoryOption('F');
    coG = createCategoryOption('G');
    coH = createCategoryOption('H');
    categoryService.addCategoryOption(coA);
    categoryService.addCategoryOption(coB);
    categoryService.addCategoryOption(coC);
    categoryService.addCategoryOption(coD);
    categoryService.addCategoryOption(coE);
    categoryService.addCategoryOption(coF);
    categoryService.addCategoryOption(coG);
    categoryService.addCategoryOption(coH);
  }

  // -------------------------------------------------------------------------
  // Tests
  // -------------------------------------------------------------------------
  @Test
  void testAddGet() {
    CategoryOptionGroup cogA = createCategoryOptionGroup('A', coA, coB);
    CategoryOptionGroup cogB = createCategoryOptionGroup('B', coC, coD);
    CategoryOptionGroup cogC = createCategoryOptionGroup('C', coE, coF);
    CategoryOptionGroup cogD = createCategoryOptionGroup('D', coG, coH);
    categoryOptionGroupStore.save(cogA);
    categoryOptionGroupStore.save(cogB);
    categoryOptionGroupStore.save(cogC);
    categoryOptionGroupStore.save(cogD);
    assertEquals(cogA, categoryOptionGroupStore.get(cogA.getId()));
    assertTrue(cogA.getMembers().contains(coA));
    assertTrue(cogA.getMembers().contains(coB));
    assertEquals(cogB, categoryOptionGroupStore.get(cogB.getId()));
    assertTrue(cogB.getMembers().contains(coC));
    assertTrue(cogB.getMembers().contains(coD));
    assertEquals(cogC, categoryOptionGroupStore.get(cogC.getId()));
    assertTrue(cogC.getMembers().contains(coE));
    assertTrue(cogC.getMembers().contains(coF));
    assertEquals(cogD, categoryOptionGroupStore.get(cogD.getId()));
    assertTrue(cogD.getMembers().contains(coG));
    assertTrue(cogD.getMembers().contains(coH));
  }

  @Test
  void testGetByGroupSet() {
    CategoryOptionGroup cogA = createCategoryOptionGroup('A', coA, coB);
    CategoryOptionGroup cogB = createCategoryOptionGroup('B', coC, coD);
    CategoryOptionGroup cogC = createCategoryOptionGroup('C', coE, coF);
    CategoryOptionGroup cogD = createCategoryOptionGroup('D', coG, coH);
    categoryOptionGroupStore.save(cogA);
    categoryOptionGroupStore.save(cogB);
    categoryOptionGroupStore.save(cogC);
    categoryOptionGroupStore.save(cogD);
    CategoryOptionGroupSet cogsA = createCategoryOptionGroupSet('A', cogA, cogB);
    CategoryOptionGroupSet cogsB = createCategoryOptionGroupSet('B', cogC, cogD);
    categoryService.saveCategoryOptionGroupSet(cogsA);
    categoryService.saveCategoryOptionGroupSet(cogsB);
    assertEquals(1, cogA.getGroupSets().size());
    assertEquals(cogsA, cogA.getGroupSets().iterator().next());
    assertEquals(1, cogB.getGroupSets().size());
    assertEquals(cogsA, cogB.getGroupSets().iterator().next());
    List<CategoryOptionGroup> groupsA = categoryOptionGroupStore.getCategoryOptionGroups(cogsA);
    assertEquals(2, groupsA.size());
    assertTrue(groupsA.contains(cogA));
    assertTrue(groupsA.contains(cogB));
    List<CategoryOptionGroup> groupsB = categoryOptionGroupStore.getCategoryOptionGroups(cogsB);
    assertEquals(2, groupsB.size());
    assertTrue(groupsB.contains(cogC));
    assertTrue(groupsB.contains(cogD));
  }

  @Test
  @DisplayName("Should throw error if adding category option group without data dimension type")
  void testAddCogWithoutDataDimensionType() {
    CategoryOptionGroup cogA = createCategoryOptionGroup('A', coA, coB);
    cogA.setDataDimensionType(null);
    assertThrows(PropertyValueException.class, () -> categoryOptionGroupStore.save(cogA));
  }

  @Test
  @DisplayName("Should throw error if adding category option group set without data dimension type")
  void testAddCogsWithoutDataDimensionType() {
    CategoryOptionGroup cogA = createCategoryOptionGroup('A', coA, coB);
    CategoryOptionGroup cogB = createCategoryOptionGroup('B', coC, coD);
    categoryOptionGroupStore.save(cogA);
    categoryOptionGroupStore.save(cogB);
    CategoryOptionGroupSet cogsA = createCategoryOptionGroupSet('A', cogA, cogB);
    cogsA.setDataDimensionType(null);
    assertThrows(
        PropertyValueException.class, () -> categoryService.saveCategoryOptionGroupSet(cogsA));
  }

  @Test
  @DisplayName(
      "Should return the expected category option groups when searching by category option")
  void getByCategoryOptionTest() {
    CategoryOptionGroup cogA = createCategoryOptionGroup('W', coA);
    CategoryOptionGroup cogB = createCategoryOptionGroup('X', coB);
    CategoryOptionGroup cogC = createCategoryOptionGroup('Y', coB, coC);
    CategoryOptionGroup cogD = createCategoryOptionGroup('Z', coD);
    categoryOptionGroupStore.save(cogA);
    categoryOptionGroupStore.save(cogB);
    categoryOptionGroupStore.save(cogC);
    categoryOptionGroupStore.save(cogD);

    List<CategoryOptionGroup> cogs =
        categoryOptionGroupStore.getByCategoryOption(
            List.of(coA.getUid(), coB.getUid(), coC.getUid()));

    assertEquals(3, cogs.size());
    assertTrue(
        cogs.stream()
            .flatMap(cog -> cog.getMembers().stream())
            .toList()
            .containsAll(List.of(coA, coB, coC)));
  }
}
