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
import org.joda.time.DateTimeUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.TimeZone;

@Test
public class TimeZoneHelperTest {

    private static final SerializableTimeZone UTC_TIME_ZONE = new SerializableTimeZone()
            .withBias(0)
            .withStandardTime(new SerializableTimeZoneTime(0, "00:00:00", (short) 1, (short) 1, DayOfWeekType.SUNDAY, null))
            .withDaylightTime(new SerializableTimeZoneTime(0, "00:00:00", (short) 1, (short) 12, DayOfWeekType.SUNDAY, null));

    private static SerializableTimeZone LA_TIME_ZONE = new SerializableTimeZone()
            .withBias(480)
            .withStandardTime(new SerializableTimeZoneTime(0, "01:00:00", (short) 3, (short) 11, DayOfWeekType.SUNDAY, "2013"))
            .withDaylightTime(new SerializableTimeZoneTime(-60, "03:00:00", (short) 9, (short) 3, DayOfWeekType.SUNDAY, "2014"));

    @BeforeMethod
    public void setup() {
        DateTimeUtils.setCurrentMillisFixed(1377022378000L);
    }

    @AfterMethod
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    public void testToSerializableTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        SerializableTimeZone actual = TimeZoneHelper.toSerializableTimeZone(timeZone);
        Assert.assertEquals(actual, LA_TIME_ZONE);
    }

    public void testToSerializableTimeZoneFixed() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        SerializableTimeZone actual = TimeZoneHelper.toSerializableTimeZone(timeZone);
        Assert.assertEquals(actual, UTC_TIME_ZONE);
    }

    public void testGetWindowsIdd() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        String windowsId = TimeZoneHelper.getWindowsId(timeZone);
        Assert.assertEquals(windowsId, "Pacific Standard Time");
    }

}
