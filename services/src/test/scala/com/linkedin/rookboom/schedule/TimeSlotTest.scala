package com.linkedin.rookboom.schedule

import org.testng.Assert._
import org.testng.annotations.Test

class TimeSlotTest {

  @Test
  def testOverlapsDisjoint() {
    val ts1 = TimeSlot(1, 2)
    val ts2 = TimeSlot(4, 5)
    assertFalse(ts1.overlaps(ts2))
  }

  @Test
  def testOverlapsIntersecting() {
    val ts1 = TimeSlot(1, 3)
    val ts2 = TimeSlot(2, 4)
    assertTrue(ts1.overlaps(ts2))
  }

  @Test
  def testOverlapsTouching() {
    val ts1 = TimeSlot(1, 2)
    val ts2 = TimeSlot(2, 3)
    assertTrue(ts1.overlaps(ts2))
  }

}
