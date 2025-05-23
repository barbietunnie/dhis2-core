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
package org.hisp.dhis.commons.util;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.hisp.dhis.common.RegexUtils;
import org.slf4j.helpers.MessageFormatter;

/**
 * Utility class with methods for managing strings.
 *
 * @author Lars Helge Overland
 */
public class TextUtils {
  public static final TextUtils INSTANCE = new TextUtils();

  public static final String EMPTY = "";

  public static final String SPACE = " ";

  public static final String SEP = "-";

  public static final String LN = System.getProperty("line.separator");

  public static final String SEMICOLON = ";";

  public static final String COMMA = ",";

  private static final String DELIMITER = ", ";

  private static final String OPTION_SEP = ";";

  private static final Character DOUBLE_QUOTE = '\"';

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

  /**
   * Remove all non-alphanumeric characters within string
   *
   * @param str input string
   * @return string with only alphanumeric characters and spaces, dash and underscore
   */
  public static String removeNonEssentialChars(String str) {
    return str.replaceAll("[^a-zA-Z0-9 ;:,'=._@-]", "");
  }

  /**
   * Returns a list of tokens based on the given string.
   *
   * @param string the string.
   * @return the list of tokens.
   */
  public static List<String> getTokens(String string) {
    if (string == null) {
      return null;
    }

    return new ArrayList<>(Arrays.asList(string.split("\\s")));
  }

  /**
   * Gets the sub string of the given string. If the beginIndex is larger than the length of the
   * string, the empty string is returned. If the beginIndex + the length is larger than the length
   * of the string, the part of the string following the beginIndex is returned. Method is
   * out-of-range safe.
   *
   * @param string the string.
   * @param beginIndex the zero-based begin index.
   * @param length the length of the sub string starting at the begin index.
   * @return the sub string of the given string.
   */
  public static String subString(String string, int beginIndex, int length) {
    if (string == null) {
      return null;
    }

    final int endIndex = beginIndex + length;

    if (beginIndex >= string.length()) {
      return EMPTY;
    }

    if (endIndex > string.length()) {
      return string.substring(beginIndex, string.length());
    }

    return string.substring(beginIndex, endIndex);
  }

  /**
   * Removes the last occurrence of the word "or" from the given string, including potential
   * trailing spaces, case-insensitive.
   *
   * @param string the string.
   * @return the chopped string.
   */
  public static String removeLastOr(String string) {
    string = StringUtils.stripEnd(string, " ");

    return StringUtils.removeEndIgnoreCase(string, "or");
  }

  /**
   * Removes the last occurrence of the word "and" from the given string, including potential
   * trailing spaces, case-insensitive.
   *
   * @param string the string.
   * @return the chopped string.
   */
  public static String removeLastAnd(String string) {
    string = StringUtils.stripEnd(string, " ");

    return StringUtils.removeEndIgnoreCase(string, "and");
  }

  /**
   * Removes the last occurrence of comma (",") from the given string, including potential trailing
   * spaces.
   *
   * @param string the string.
   * @return the chopped string.
   */
  public static String removeLastComma(String string) {
    string = StringUtils.stripEnd(string, " ");

    return StringUtils.removeEndIgnoreCase(string, ",");
  }

  /**
   * Removes the last occurrence of comma (",") from the given StringBuilder, including any
   * character after the comma. It changes the object by reference, and returns the same object for
   * convenience.
   *
   * @param builder the StringBuilder.
   * @return the chopped StringBuilder.
   */
  public static StringBuilder removeLastComma(StringBuilder builder) {
    int index = -1;

    if (builder != null && ((index = builder.lastIndexOf(",")) != -1)) {
      builder.delete(index, builder.length());
    }

    return builder;
  }

  /**
   * Removes the last occurrence of the the given string, including potential trailing spaces.
   *
   * @param string the string, without potential trailing spaces.
   * @param remove the text to remove.
   * @return the chopped string.
   */
  public static String removeLast(String string, String remove) {
    string = StringUtils.stripEnd(string, " ");

    return StringUtils.removeEndIgnoreCase(string, remove);
  }

  /**
   * Removes line breaks form the given string.
   *
   * @param string the string.
   * @return the chopped string.
   */
  public static String removeNewlines(String string) {
    return string.replaceAll("\r", EMPTY).replaceAll("\n", EMPTY);
  }

  /**
   * Returns an empty string if the given argument is true, the string otherwise. This is a
   * convenience method.
   *
   * @param string the string.
   * @param emptyString whether to return an empty string.
   * @return a string.
   */
  public static String getString(String string, boolean emptyString) {
    return emptyString ? EMPTY : string;
  }

  /**
   * Joins the elements of the provided array into a single String containing the provided list of
   * elements.
   *
   * @param <T> type.
   * @param list the list of objects to join.
   * @param separator the separator string.
   * @param nullReplacement the value to replace nulls in list with.
   * @return the joined string.
   */
  public static <T> String join(List<T> list, String separator, T nullReplacement) {
    if (list == null) {
      return null;
    }

    List<T> objects = new ArrayList<>(list);

    if (nullReplacement != null) {
      Collections.replaceAll(objects, null, nullReplacement);
    }

    return StringUtils.join(objects, separator);
  }

  /**
   * Joins the given elements with a {@code -} character as separator.
   *
   * @param elements the elements to join.
   * @return the joined string.
   */
  @SafeVarargs
  public static <T> String joinHyphen(T... elements) {
    return StringUtils.join(elements, "-");
  }

  /**
   * Transforms a collection of Integers into a comma delimited String. If the given collection of
   * elements are null or is empty, an empty String is returned.
   *
   * @param elements the collection of Integers
   * @return a comma delimited String.
   */
  public static String getCommaDelimitedString(Collection<?> elements) {
    if (elements != null) {
      return elements.stream().map(Object::toString).collect(Collectors.joining(DELIMITER));
    }

    return "";
  }

  /**
   * Transforms a collection of strings into a comma delimited string, where each component is
   * single quoted.
   *
   * @param elements the collection of Integers
   * @return a comma delimited String.
   */
  public static String getQuotedCommaDelimitedString(Collection<String> elements) {
    if (elements != null && !elements.isEmpty()) {
      final StringBuilder builder = new StringBuilder();

      for (Object element : elements) {
        builder.append("'").append(element).append("', ");
      }

      return builder.substring(0, builder.length() - ", ".length());
    }

    return null;
  }

  /**
   * This method will double quote the given value, if this is not double-quoted nor blank. If the
   * value is already double-quoted, it returns the same value.
   *
   * @param value the string to be double-quoted.
   * @return a double quoted value, or empty.
   */
  public static String doubleQuote(String value) {
    if (StringUtils.isBlank(value)) {
      return EMPTY;
    }

    value = value.trim();

    if (value.startsWith("\"") && value.endsWith("\"")) {
      return value;
    } else {
      return DOUBLE_QUOTE + value + DOUBLE_QUOTE;
    }
  }

  /**
   * Checks the two strings for equality.
   *
   * @param s1 string 1.
   * @param s2 string 2.
   * @return true if strings are equal, false otherwise.
   */
  public static boolean equalsNullSafe(String s1, String s2) {
    return s1 == null ? s2 == null : s1.equals(s2);
  }

  /**
   * Returns the string value of the given boolean. Returns null if argument is null.
   *
   * @param value the boolean.
   * @return the string value.
   */
  public static String valueOf(Boolean value) {
    return value != null ? String.valueOf(value) : null;
  }

  /**
   * Returns the boolean value of the given string. Returns null if argument is null.
   *
   * @param value the string value.
   * @return the boolean.
   */
  public static Boolean valueOf(String value) {
    return value != null ? Boolean.valueOf(value) : null;
  }

  /**
   * Null-safe method for converting the given string to lower-case.
   *
   * @param string the string.
   * @return the string in lower-case.
   */
  public static String lower(String string) {
    return string != null ? string.toLowerCase() : null;
  }

  /**
   * Null-safe method for writing the items of a string array out as a string separated by the given
   * char separator.
   *
   * @param array the array.
   * @param separator the separator of the array items.
   * @return a string.
   */
  public static String toString(String[] array, String separator) {
    StringBuilder builder = new StringBuilder();

    if (array != null && array.length > 0) {
      for (String string : array) {
        builder.append(string).append(separator);
      }

      builder.deleteCharAt(builder.length() - 1);
    }

    return builder.toString();
  }

  /**
   * Returns the string representation of the object, or null if the object is null.
   *
   * @param object the object.
   * @return the string representation.
   */
  public static String toString(Object object) {
    return object != null ? object.toString() : null;
  }

  /**
   * Returns the empty string if the given string is equal to the given test, the string if not.
   *
   * @param string the string.
   * @param test the test to check the string for equality.
   * @return a string.
   */
  public static String emptyIfEqual(String string, String test) {
    return test != null && test.equals(string) ? EMPTY : string;
  }

  /**
   * Returns the empty string if the given test is false, the string if not.
   *
   * @param string the string.
   * @param test the test to check.
   * @return a string.
   */
  public static String emptyIfFalse(String string, boolean test) {
    return test ? string : EMPTY;
  }

  /**
   * Returns the empty string if the given test is true, the string if not.
   *
   * @param string the string.
   * @param test the test to check.
   * @return a string.
   */
  public static String emptyIfTrue(String string, boolean test) {
    return test ? EMPTY : string;
  }

  /**
   * Invokes append tail on matcher with the given string buffer, and returns the string buffer as a
   * string.
   *
   * @param matcher the matcher.
   * @param sb the string buffer.
   * @return a string.
   */
  public static String appendTail(Matcher matcher, StringBuffer sb) {
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * Returns a pretty name variant of the given class.
   *
   * @param clazz the class.
   * @return a pretty class name.
   */
  public static String getPrettyClassName(Class<?> clazz) {
    StringBuilder name = new StringBuilder();

    String className = clazz.getSimpleName();

    for (int i = 0; i < className.length(); i++) {
      char c = className.charAt(i);

      if (i > 0 && Character.isUpperCase(c)) {
        name.append(StringUtils.SPACE);
      }

      name.append(c);
    }

    return name.toString();
  }

  /**
   * Returns a human friendly name of the given enum.
   *
   * @param enumeration the enum.
   * @return a human friendly name.
   */
  public static String getPrettyEnumName(Enum<?> enumeration) {
    return StringUtils.capitalize(enumeration.name().replaceAll("_", " ").toLowerCase());
  }

  /**
   * Returns a human friendly name of the given property value.
   *
   * @param property the property value.
   * @return a human friendly name.
   */
  public static String getPrettyPropertyName(String property) {
    List<String> fieldStrings =
        Arrays.stream(property.split("(?=[A-Z])"))
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    fieldStrings.set(0, StringUtils.capitalize(fieldStrings.get(0)));

    return String.join(" ", fieldStrings);
  }

  /**
   * Gets the string at the given index of the array produced by splitting the given string on the
   * given separator. Returns null if the given string is null or if the given index is out of
   * bounds of the array.
   *
   * @param string the string to split.
   * @param separator the character to split on.
   * @param index the index of the string in the resulting array to return.
   * @return a string.
   */
  public static String splitSafe(String string, String separator, int index) {
    if (string == null) {
      return null;
    }

    String[] split = string.split(separator);

    if (index >= 0 && split.length > index && split[index] != null) {
      return String.valueOf(split[index]);
    }

    return null;
  }

  /**
   * Indicates whether the given string contains any of the given search strings. The operation
   * ignores case and leading and trailing blanks.
   *
   * @param string the string to check, can be null.
   * @param searchStrings the strings to check against.
   * @return true or false.
   */
  public static boolean containsAnyIgnoreCase(String string, Collection<String> searchStrings) {
    if (string == null || searchStrings == null) {
      return false;
    }

    for (String searchString : searchStrings) {
      if (string.trim().toLowerCase().contains(searchString.trim().toLowerCase())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Splits the given string value into independent values using a given separator.
   *
   * @param value the string to be splitted.
   * @param separator for splitting value
   * @return the set of values.
   */
  public static Set<String> splitToSet(String value, String separator) {
    if (value == null || value.isEmpty()) {
      return null;
    }

    String[] values = value.split(separator);

    return new HashSet<>(Arrays.asList(values));
  }

  /**
   * Replaces variables in the given template string with the given variable values.
   *
   * @param template the template string.
   * @param variables the map of variables and values.
   * @return a resolved string.
   */
  public static String replace(String template, Map<String, String> variables) {
    return new StringSubstitutor(variables)
        .setEnableUndefinedVariableException(true)
        .replace(template);
  }

  /**
   * Replaces variables in the given template string with the given variable key and value.
   *
   * @param template the template string.
   * @param k1 the variable key.
   * @param v1 the variable value.
   * @return a resolved string.
   */
  public static String replace(String template, String k1, String v1) {
    return replace(template, Map.of(k1, v1));
  }

  /**
   * Replaces variables in the given template string with the given variable key and value.
   *
   * @param template the template string.
   * @param k1 the variable key.
   * @param v1 the variable value.
   * @param k2 the variable key.
   * @param v2 the variable value.
   * @return a resolved string.
   */
  public static String replace(String template, String k1, String v1, String k2, String v2) {
    return replace(template, Map.of(k1, v1, k2, v2));
  }

  /**
   * Splits the parameter into options based on {@code ;} (semicolon) as separator.
   *
   * @param param the parameter string.
   * @return the list of options.
   */
  public static List<String> getOptions(String param) {
    if (StringUtils.isEmpty(param)) {
      return Lists.newArrayList();
    }

    return Lists.newArrayList(param.split(OPTION_SEP));
  }

  /**
   * Creates a regular expression from the given glob string.
   *
   * @param glob the glob string.
   * @return a regular expression.
   */
  public static String createRegexFromGlob(String glob) {
    StringBuilder out = new StringBuilder("^");
    for (int i = 0; i < glob.length(); ++i) {
      final char c = glob.charAt(i);
      switch (c) {
        case '*':
          out.append(".*");
          break;
        case '?':
          out.append('.');
          break;
        case '.':
          out.append("\\.");
          break;
        case '\\':
          out.append("\\\\");
          break;
        default:
          out.append(c);
      }
    }

    out.append('$');
    return out.toString();
  }

  /**
   * Returns the given string as a list of lines. Splits the string on newline characters (UNIX and
   * Windows).
   *
   * @param string the string.
   * @return a list of lines.
   */
  public static List<String> toLines(String string) {
    return Lists.newArrayList(string.split("\\r?\\n"));
  }

  /**
   * Method to remove a trailing '/' if it's the last char.
   *
   * @param string string to update if condition met
   * @return string with no trailing '/' or the string unchanged
   */
  public static String removeAnyTrailingSlash(@Nonnull String string) {
    return string.endsWith("/") ? StringUtils.chop(string) : string;
  }

  /**
   * Returns a formatted message string, a pair of curly braces represents a variable.
   *
   * @param pattern the pattern string.
   * @param arguments the pattern arguments.
   * @return a formatted message string.
   */
  public static String format(String pattern, Object... arguments) {
    return MessageFormatter.arrayFormat(pattern, arguments).getMessage();
  }

  /**
   * Returns the names of the variables in the given input. The definition of a variable is <code>
   * ${variableName}</code>.
   *
   * @param input the input potentially containing variables.
   * @return a set of variable names.
   */
  public static Set<String> getVariableNames(String input) {
    return RegexUtils.getMatches(VARIABLE_PATTERN, input, 1);
  }

  /**
   * Provides the ability to form a valid URL, by providing a 'baseUrl' and a 'path'. The 'baseUrl'
   * has any trailing slash '/' removed, keeping it's scheme (e.g. 'http://') with 2 '/'s . The
   * remaining string concatenation is put together with a '/' and the remaining 'path', which has
   * any extra '/'s replaced with a single '/'. <br>
   * This method is useful when you are stitching together URL parts and are not sure if any of the
   * params start or end with '/'.
   *
   * @param baseUrl base URL e.g. 'http://localhost'
   * @param path the remaining path to concatenate with the base URL
   * @return the fully-cleaned URl, meaning the URL will have a valid URL scheme included (if
   *     provided) and the remaining path will only have single '/'s.
   */
  public static String cleanUrlPathOnly(@Nonnull String baseUrl, @Nonnull String path) {
    String base = removeAnyTrailingSlash(baseUrl);
    return base + ("/" + path).replaceAll("/+", "/");
  }
}
