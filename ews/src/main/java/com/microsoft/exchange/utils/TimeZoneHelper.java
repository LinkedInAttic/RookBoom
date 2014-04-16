/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.exchange.utils;

import com.microsoft.exchange.types.DayOfWeekType;
import com.microsoft.exchange.types.SerializableTimeZone;
import com.microsoft.exchange.types.SerializableTimeZoneTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * This class contains helper methods to deal with time zones.
 *
 * @author Dmitriy Yefremov
 */
public class TimeZoneHelper {

    private static final SerializableTimeZoneTime FIXED_STANDARD_TIME = new SerializableTimeZoneTime(0, "00:00:00", (short) 1, (short) 1, DayOfWeekType.SUNDAY, null);
    private static final SerializableTimeZoneTime FIXED_DAYLIGHT_TIME = new SerializableTimeZoneTime(0, "00:00:00", (short) 1, (short) 12, DayOfWeekType.SUNDAY, null);

    /**
     * Return a EWS representation of the default time zone.
     *
     * @return the resulting SerializableTimeZone object
     */
    public static SerializableTimeZone defaultTimeZone() {
        return toSerializableTimeZone(TimeZone.getDefault());
    }

    /**
     * Converts the given Java time zone into into the corresponding EWS representation.
     *
     * @param tz time zone to convert
     * @return the resulting SerializableTimeZone object
     */
    public static SerializableTimeZone toSerializableTimeZone(TimeZone tz) {
        long now = DateTimeUtils.currentTimeMillis();
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        int standardOffset = zone.getStandardOffset(now);
        SerializableTimeZone result = new SerializableTimeZone();
        result.setBias(toBias(standardOffset));
        // check if the zone has no transitions
        if (zone.isFixed()) {
            // fake transitions for fixed zones
            result.setStandardTime(FIXED_STANDARD_TIME);
            result.setDaylightTime(FIXED_DAYLIGHT_TIME);
        } else {
            // it is assumed that 2 subsequent transition will have both STD and DST
            long transition = setTransitionTime(zone, now, result);
            setTransitionTime(zone, transition, result);
        }
        return result;
    }

    private static SerializableTimeZoneTime toSerializableTimeZoneTime(DateTimeZone zone, long transition) {
        int standardOffset = zone.getStandardOffset(transition);
        long offset = zone.getOffset(transition);
        int bias = toBias(offset - standardOffset);
        DateTime time = new DateTime(transition, zone);
        short month = (short) time.getMonthOfYear();
        short day = (short) time.getDayOfMonth();
        DayOfWeekType dayOfWeek = toDayOfWeek(time.getDayOfWeek());
        return new SerializableTimeZoneTime(bias, time.toString("HH:mm:ss"), day, month, dayOfWeek, time.toString("yyyy"));
    }

    private static long setTransitionTime(DateTimeZone zone, long now, SerializableTimeZone target) {
        long transition = zone.nextTransition(now);
        boolean isStandard = zone.isStandardOffset(transition);
        SerializableTimeZoneTime time = toSerializableTimeZoneTime(zone, transition);
        if (isStandard) {
            target.setStandardTime(time);
        } else {
            target.setDaylightTime(time);
        }
        return transition;
    }

    private static int toBias(long offset) {
        return (int) TimeUnit.MILLISECONDS.toMinutes(-offset);
    }

    private static DayOfWeekType toDayOfWeek(int day) {
        if (day == 7) {
            return DayOfWeekType.SUNDAY;
        }
        return DayOfWeekType.values()[day];
    }

    /**
     * Returns the Windows time zone id corresponding to the given time zone.
     *
     * @param timeZone time zone
     * @return windows time zone id
     * @throws IllegalArgumentException if there is no corresponding windows time zone id found
     */
    public static String getWindowsId(TimeZone timeZone) {
        String standardId = timeZone.getID();
        return WindowsZonesMapping.getWindowsId(standardId);
    }

}
