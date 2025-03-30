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
package org.hisp.dhis.common;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Lars Helge Overland
 */
public class RegexUtils {
  /**
   * Return the matches in the given input based on the given pattern and group number.
   *
   * @param pattern the pattern.
   * @param input the input. If the input is null, an empty set is returned.
   * @param group the group, can be null.
   * @return a set of matches.
   */
  public static Set<String> getMatches(Pattern pattern, String input, Integer group) {
    Objects.requireNonNull(pattern);

    int gr = ObjectUtils.firstNonNull(group, 0);

    Set<String> set = new HashSet<>();

    if (input != null) {
      Matcher matcher = pattern.matcher(input);

      while (matcher.find()) {
        set.add(matcher.group(gr));
      }
    }

    return set;
  }

  /**
   * Null safe pattern matcher test.
   *
   * @param pattern the {@link Pattern}.
   * @param input the string input.
   * @return true if matches, false if not.
   */
  public static boolean matches(Pattern pattern, String input) {
    return input != null && pattern.matcher(input).matches();
  }
}
