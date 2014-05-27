package com.linkedin.rookboom.schedule

import org.testng.annotations.Test
import org.testng.Assert._

class TimeMaskManagerImplTest {

  @Test
  def testGetMaskMorning() {
    val manager = new TimeMaskManagerImpl
    val mask = manager.getMask(0, Timeframe.Morning)
    assertEquals(mask.interval, 1800000)
    assertEquals(mask.frames, Seq(1800000, 3600000, 5400000, 7200000, 9000000, 10800000, 12600000, 14400000, 16200000, 18000000, 19800000, 21600000, 23400000, 25200000, 27000000, 28800000, 30600000, 32400000, 34200000, 36000000, 37800000, 39600000))
  }

}
