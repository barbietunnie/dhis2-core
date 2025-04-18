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
package org.hisp.dhis.dxf2.metadata.objectbundle.hooks;

import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserRole;
import org.hisp.dhis.user.UserService;
import org.springframework.stereotype.Component;

/**
 * @author Morten Svanæs <msvanaes@dhi2.org>
 */
@Component
@AllArgsConstructor
@Slf4j
public class UserRoleBundleHook extends AbstractObjectBundleHook<UserRole> {

  public static final String INVALIDATE_SESSION_KEY = "shouldInvalidateUserSessions";

  private final UserService userService;

  @Override
  public void preUpdate(UserRole update, UserRole existing, ObjectBundle bundle) {
    if (update == null) return;
    bundle.putExtras(update, INVALIDATE_SESSION_KEY, userRolesUpdated(update, existing));
  }

  private Boolean userRolesUpdated(UserRole update, UserRole existing) {
    Set<String> newAuthorities = update.getAuthorities();
    Set<String> existingAuthorities = existing.getAuthorities();
    return !Objects.equals(newAuthorities, existingAuthorities);
  }

  @Override
  public void postUpdate(UserRole updatedUserRole, ObjectBundle bundle) {
    final Boolean invalidateSessions =
        (Boolean) bundle.getExtras(updatedUserRole, INVALIDATE_SESSION_KEY);

    if (Boolean.TRUE.equals(invalidateSessions)) {
      for (User user : updatedUserRole.getUsers()) {
        userService.invalidateUserSessions(user.getUsername());
      }
    }

    bundle.removeExtras(updatedUserRole, INVALIDATE_SESSION_KEY);
  }
}
