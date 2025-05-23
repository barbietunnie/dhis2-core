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
package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.test.TestBase.injectSecurityContextNoSettings;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.feedback.ForbiddenException;
import org.hisp.dhis.fileresource.FileResource;
import org.hisp.dhis.fileresource.FileResourceDomain;
import org.hisp.dhis.fileresource.FileResourceService;
import org.hisp.dhis.user.CurrentUserUtil;
import org.hisp.dhis.user.SystemUser;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.webapi.utils.FileResourceUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class FileResourceControllerMockTest {

  private FileResourceController controller;

  @Mock private FileResourceService fileResourceService;

  @Mock private UserService userService;

  @Mock private FileResourceUtils fileResourceUtils;

  @Mock private DhisConfigurationProvider dhisConfig;

  @BeforeAll
  static void setup() {
    injectSecurityContextNoSettings(new SystemUser());
  }

  @Test
  void testGetOrgUnitImage() throws Exception {
    controller = new FileResourceController(fileResourceService, fileResourceUtils, dhisConfig);
    FileResource fileResource = new FileResource();
    fileResource.setContentType("image/png");
    fileResource.setDomain(FileResourceDomain.ORG_UNIT);
    fileResource.setUid("id");

    when(fileResourceService.getFileResource("id")).thenReturn(fileResource);

    User currentUser = userService.getUserByUsername(CurrentUserUtil.getCurrentUsername());
    controller.getFileResourceData("id", new MockHttpServletResponse(), null, currentUser);

    verify(fileResourceService).copyFileResourceContent(any(), any());
  }

  @Test
  void testGetDataValue() {
    controller = new FileResourceController(fileResourceService, fileResourceUtils, dhisConfig);
    FileResource fileResource = new FileResource();
    fileResource.setContentType("image/png");
    fileResource.setDomain(FileResourceDomain.DATA_VALUE);
    fileResource.setUid("id");

    when(fileResourceService.getFileResource("id")).thenReturn(fileResource);

    User currentUser = userService.getUserByUsername(CurrentUserUtil.getCurrentUsername());
    assertThrows(
        ForbiddenException.class,
        () ->
            controller.getFileResourceData("id", new MockHttpServletResponse(), null, currentUser));
  }
}
