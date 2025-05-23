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
package org.hisp.dhis.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.calendar.impl.Iso8601Calendar;
import org.hisp.dhis.common.IndirectTransactional;
import org.hisp.dhis.common.NonTransactional;
import org.hisp.dhis.period.Cal;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.setting.SystemSettings;
import org.hisp.dhis.setting.SystemSettingsProvider;
import org.springframework.stereotype.Service;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@RequiredArgsConstructor
@Service("org.hisp.dhis.calendar.CalendarService")
public class DefaultCalendarService implements CalendarService {
  private final SystemSettingsProvider settingsProvider;

  private final Set<Calendar> calendars;

  private final Map<String, Calendar> calendarMap = Maps.newHashMap();

  private static final List<DateFormat> DATE_FORMATS =
      Lists.newArrayList(
          new DateFormat("yyyy-MM-dd", "yyyy-MM-dd", "yyyy-MM-dd", "yyyy-mm-dd"),
          new DateFormat("dd-MM-yyyy", "dd-MM-yyyy", "dd-MM-yyyy", "dd-mm-yyyy"));

  // -------------------------------------------------------------------------
  // CalendarService implementation
  // -------------------------------------------------------------------------

  @PostConstruct
  public void init() {
    for (Calendar calendar : calendars) {
      calendarMap.put(calendar.name(), calendar);
    }

    PeriodType.setCalendarService(this);
    Cal.setCalendarService(this);
    DateUnitPeriodTypeParser.setCalendarService(this);
  }

  @Override
  @NonTransactional
  public List<Calendar> getAllCalendars() {
    List<Calendar> sortedCalendars = Lists.newArrayList(calendarMap.values());
    Collections.sort(sortedCalendars, CalendarComparator.INSTANCE);
    return sortedCalendars;
  }

  @Override
  public List<DateFormat> getAllDateFormats() {
    return DATE_FORMATS;
  }

  @Override
  @IndirectTransactional
  public Calendar getSystemCalendar() {
    SystemSettings settings = settingsProvider.getCurrentSettings();
    String calendarKey = settings.getCalendar();
    String dateFormat = settings.getDateFormat();

    Calendar calendar = null;

    if (calendarMap.containsKey(calendarKey)) {
      calendar = calendarMap.get(calendarKey);
    } else {
      calendar = Iso8601Calendar.getInstance();
    }

    calendar.setDateFormat(dateFormat);

    return calendar;
  }

  @Override
  @IndirectTransactional
  public DateFormat getSystemDateFormat() {
    String dateFormatKey = settingsProvider.getCurrentSettings().getDateFormat();

    for (DateFormat dateFormat : DATE_FORMATS) {
      if (dateFormat.name().equals(dateFormatKey)) {
        return dateFormat;
      }
    }

    return DATE_FORMATS.get(0);
  }
}
