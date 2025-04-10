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

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hisp.dhis.calendar.impl.Iso8601Calendar;
import org.hisp.dhis.period.BiWeeklyPeriodType;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.WeeklyAbstractPeriodType;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DateUnitPeriodTypeParser implements PeriodTypeParser, Serializable {
  private static CalendarService calendarService;

  public static void setCalendarService(CalendarService calendarService) {
    DateUnitPeriodTypeParser.calendarService = calendarService;
  }

  public static CalendarService getCalendarService() {
    return calendarService;
  }

  public static org.hisp.dhis.calendar.Calendar getCalendar() {
    if (calendarService != null) {
      return calendarService.getSystemCalendar();
    }

    return Iso8601Calendar.getInstance();
  }

  @Override
  public DateInterval parse(String period) {
    return parse(getCalendar(), period);
  }

  @Override
  public DateInterval parse(Calendar calendar, String period) {
    return DateUnitType.find(period)
        .map(dateUnitTypeWithPattern -> parseInternal(calendar, period, dateUnitTypeWithPattern))
        .orElse(null);
  }

  private DateInterval parseInternal(
      Calendar calendar,
      String period,
      DateUnitType.DateUnitTypeWithPattern dateUnitTypeWithPattern) {
    Pattern pattern = dateUnitTypeWithPattern.getPattern();
    DateUnitType dateUnitType = dateUnitTypeWithPattern.getDateUnitType();
    Matcher matcher = pattern.matcher(period);
    boolean match = matcher.find();

    if (!match) {
      return null;
    }

    if (DateUnitType.DAILY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int month = Integer.parseInt(matcher.group(2));
      int day = Integer.parseInt(matcher.group(3));

      DateTimeUnit dateTimeUnit = new DateTimeUnit(year, month, day, calendar.isIso8601());
      dateTimeUnit.setDayOfWeek(calendar.weekday(dateTimeUnit));

      return new DateInterval(dateTimeUnit, dateTimeUnit);
    } else if (DateUnitType.WEEKLY == dateUnitType
        || DateUnitType.WEEKLY_WEDNESDAY == dateUnitType
        || DateUnitType.WEEKLY_THURSDAY == dateUnitType
        || DateUnitType.WEEKLY_SATURDAY == dateUnitType
        || DateUnitType.WEEKLY_SUNDAY == dateUnitType) {
      DateTimeUnit start;
      DateTimeUnit end;
      int year = Integer.parseInt(matcher.group(1));
      int week = Integer.parseInt(matcher.group(2));

      WeeklyAbstractPeriodType periodType =
          (WeeklyAbstractPeriodType) PeriodType.getByNameIgnoreCase(dateUnitType.getName());

      if (periodType == null || week < 1 || week > calendar.weeksInYear(year)) {
        return null;
      }

      start =
          getDateTimeFromWeek(
              year,
              week,
              calendar,
              PeriodType.MAP_WEEK_TYPE.get(periodType.getName()),
              new DateTimeUnit(year, 1, 1));

      end = calendar.plusWeeks(start, 1);
      end = calendar.minusDays(end, 1);

      return new DateInterval(start, end);
    } else if (DateUnitType.BI_WEEKLY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int week = Integer.parseInt(matcher.group(2)) * 2 - 1;

      BiWeeklyPeriodType periodType =
          (BiWeeklyPeriodType) PeriodType.getByNameIgnoreCase(dateUnitType.getName());

      if (periodType == null || week < 1 || week > calendar.weeksInYear(year)) {
        return null;
      }

      DateTimeUnit start =
          getDateTimeFromWeek(year, week, calendar, DayOfWeek.MONDAY, new DateTimeUnit(year, 1, 1));
      DateTimeUnit end = calendar.plusWeeks(start, 2);
      end = calendar.minusDays(end, 1);

      return new DateInterval(start, end);
    } else if (DateUnitType.MONTHLY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int month = Integer.parseInt(matcher.group(2));

      DateTimeUnit start = new DateTimeUnit(year, month, 1, calendar.isIso8601());
      DateTimeUnit end =
          new DateTimeUnit(
              year,
              month,
              calendar.daysInMonth(start.getYear(), start.getMonth()),
              calendar.isIso8601());

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.BI_MONTHLY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int month = Integer.parseInt(matcher.group(2));

      if (month < 1 || month > 6) {
        return null;
      }

      DateTimeUnit start = new DateTimeUnit(year, (month * 2) - 1, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusMonths(end, 2);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.QUARTERLY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int quarter = Integer.parseInt(matcher.group(2));

      // valid quarters are from 1 - 4
      if (quarter < 1 || quarter > 4) {
        return null;
      }

      DateTimeUnit start = new DateTimeUnit(year, ((quarter - 1) * 3) + 1, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusMonths(end, 3);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.QUARTERLY_NOV == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int quarter = Integer.parseInt(matcher.group(2));

      // valid quarters are from 1 - 4
      if (quarter < 1 || quarter > 4) {
        return null;
      }

      int monthOffset =
          -2; // this is because first quarter (of financial November) starts 2 months back from the
      // start of the year

      /**
       * Q1 => month = ((1-1)*3) + 1 = 1; month = 1 + (-2) = -1; month = -1 + 12; month = 11; year =
       * year - 1 => first quarter starts on Month 11 of previous year
       *
       * <p>Q2 => month = ((2-1)*3) + 1 = 4; month = 4 + (-2) = 2; month = 2; year = year => second
       * quarter starts on Month 2 of same year
       */
      int month = ((quarter - 1) * 3) + 1;
      month = month + monthOffset;

      if (month < 0) {
        month += 12;
        year -= 1;
      }

      DateTimeUnit start = new DateTimeUnit(year, month, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusMonths(end, 3);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.SIX_MONTHLY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int semester = Integer.parseInt(matcher.group(2));

      // valid six-monthly are from 1 - 2
      if (semester < 1 || semester > 2) {
        return null;
      }

      DateTimeUnit start = new DateTimeUnit(year, semester == 1 ? 1 : 7, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusMonths(end, 6);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.SIX_MONTHLY_APRIL == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int semester = Integer.parseInt(matcher.group(2));

      // valid six-monthly are from 1 - 2
      if (semester < 1 || semester > 2) {
        return null;
      }

      DateTimeUnit start = new DateTimeUnit(year, semester == 1 ? 4 : 10, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusMonths(end, 6);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.SIX_MONTHLY_NOVEMBER == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));
      int semester = Integer.parseInt(matcher.group(2));

      // valid six-monthly are from 1 - 2
      if (semester < 1 || semester > 2) {
        return null;
      }

      DateTimeUnit start =
          new DateTimeUnit(
              semester == 1 ? year - 1 : year, semester == 1 ? 11 : 5, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusMonths(end, 6);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);

    } else if (DateUnitType.YEARLY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));

      DateTimeUnit start = new DateTimeUnit(year, 1, 1, calendar.isIso8601());
      DateTimeUnit end =
          new DateTimeUnit(
              year,
              calendar.monthsInYear(),
              calendar.daysInMonth(start.getYear(), calendar.monthsInYear()),
              calendar.isIso8601());

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.FINANCIAL_APRIL == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));

      DateTimeUnit start = new DateTimeUnit(year, 4, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusYears(end, 1);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.FINANCIAL_JULY == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));

      DateTimeUnit start = new DateTimeUnit(year, 7, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusYears(end, 1);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.FINANCIAL_SEPTEMBER == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));

      DateTimeUnit start = new DateTimeUnit(year, 9, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusYears(end, 1);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.FINANCIAL_OCTOBER == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));

      DateTimeUnit start = new DateTimeUnit(year, 10, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusYears(end, 1);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    } else if (DateUnitType.FINANCIAL_NOVEMBER == dateUnitType) {
      int year = Integer.parseInt(matcher.group(1));

      DateTimeUnit start = new DateTimeUnit(year - 1, 11, 1, calendar.isIso8601());
      DateTimeUnit end = new DateTimeUnit(start);
      end = calendar.plusYears(end, 1);
      end = calendar.minusDays(end, 1);

      start.setDayOfWeek(calendar.weekday(start));
      end.setDayOfWeek(calendar.weekday(end));

      return new DateInterval(start, end);
    }

    return null;
  }

  /**
   * returns a date based on a week number
   *
   * @param year The year of the date
   * @param week The week of the date
   * @param calendar The calendar used to calculate the daate
   * @param firstDayOfWeek The first day of the week
   * @param adjustedDate The first day of the year adjusted to the first day of the week it belongs
   *     to
   * @return The Date of the week
   */
  private DateTimeUnit getDateTimeFromWeek(
      int year, int week, Calendar calendar, DayOfWeek firstDayOfWeek, DateTimeUnit adjustedDate) {
    if (calendar.isIso8601()) {
      WeekFields weekFields = WeekFields.of(firstDayOfWeek, 4);

      LocalDate date =
          LocalDate.now()
              .with(weekFields.weekBasedYear(), year)
              .with(weekFields.weekOfWeekBasedYear(), week)
              .with(weekFields.dayOfWeek(), 1);

      return new DateTimeUnit(
          date.getYear(), date.getMonthValue(), date.getDayOfMonth(), calendar.isIso8601());
    } else {
      DateTimeUnit date =
          new DateTimeUnit(
              year, adjustedDate.getMonth(), adjustedDate.getDay(), calendar.isIso8601());

      // since we rewind to start of week, we might end up in the previous
      // years weeks, so we check and forward if needed
      if (calendar.isoWeek(date) == calendar.weeksInYear(year)) {
        date = calendar.plusWeeks(date, 1);
      }

      date = calendar.plusWeeks(date, week - 1);
      return date;
    }
  }
}
