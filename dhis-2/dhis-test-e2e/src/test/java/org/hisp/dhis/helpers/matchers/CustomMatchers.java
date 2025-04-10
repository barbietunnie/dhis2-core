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
package org.hisp.dhis.helpers.matchers;

import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class CustomMatchers {

  public static TypeSafeDiagnosingMatcher<String> startsWithOneOf(List<String> strings) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      protected boolean matchesSafely(String item, Description mismatchDescription) {
        if (strings != null) {
          return strings.stream().anyMatch(item::startsWith);
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a string that starts with one of " + strings.toString());
      }
    };
  }

  public static TypeSafeDiagnosingMatcher<String> containsOneOf(List<String> strings) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      protected boolean matchesSafely(String item, Description mismatchDescription) {
        if (strings != null) {
          return strings.stream().anyMatch(item::contains);
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a string that contains one of " + strings.toString());
      }
    };
  }

  public static TypeSafeDiagnosingMatcher<Object> hasToStringContaining(List<String> substrings) {
    return new TypeSafeDiagnosingMatcher<Object>() {
      @Override
      protected boolean matchesSafely(Object item, Description mismatchDescription) {
        String toString = item.toString();
        return substrings.stream().allMatch(toString::contains);
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue("a toString() that contains substrings in any order " + substrings);
      }
    };
  }
}
