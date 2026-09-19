package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SystemStatsMathTest {

    @Test
    fun `usedPercent is 0 when nothing is used`() {
        assertEquals(0, usedPercent(totalBytes = 1000L, freeBytes = 1000L))
    }

    @Test
    fun `usedPercent is 100 when nothing is free`() {
        assertEquals(100, usedPercent(totalBytes = 1000L, freeBytes = 0L))
    }

    @Test
    fun `usedPercent rounds to the nearest whole percent`() {
        // used=2 of total=3 is 66.66...%, which rounds up to 67
        assertEquals(67, usedPercent(totalBytes = 3L, freeBytes = 1L))
    }

    @Test
    fun `usedPercent is 0 when total is zero`() {
        assertEquals(0, usedPercent(totalBytes = 0L, freeBytes = 0L))
    }

    @Test
    fun `gaugeFilledSegments is 0 at 0 percent`() {
        assertEquals(0, gaugeFilledSegments(percent = 0, segmentCount = 5))
    }

    @Test
    fun `gaugeFilledSegments is all segments at 100 percent`() {
        assertEquals(5, gaugeFilledSegments(percent = 100, segmentCount = 5))
    }

    @Test
    fun `gaugeFilledSegments rounds to the nearest segment`() {
        // 61% of 5 segments = 3.05, rounds down to 3
        assertEquals(3, gaugeFilledSegments(percent = 61, segmentCount = 5))
        // 70% of 5 segments = 3.5, rounds up to 4
        assertEquals(4, gaugeFilledSegments(percent = 70, segmentCount = 5))
    }

    @Test
    fun `isCriticalUsage is false below 90 percent`() {
        assertEquals(false, isCriticalUsage(percent = 89))
    }

    @Test
    fun `isCriticalUsage is true at and above 90 percent`() {
        assertEquals(true, isCriticalUsage(percent = 90))
        assertEquals(true, isCriticalUsage(percent = 100))
    }

    @Test
    fun `bytesPerSecond scales a delta up to a full second`() {
        assertEquals(2000L, bytesPerSecond(deltaBytes = 1000L, deltaMillis = 500L))
    }

    @Test
    fun `bytesPerSecond is 0 when the elapsed time is not positive`() {
        assertEquals(0L, bytesPerSecond(deltaBytes = 1000L, deltaMillis = 0L))
    }

    @Test
    fun `formatThroughput shows KB per second under 1MB per second`() {
        assertEquals("12KB/s", formatThroughput(bytesPerSecond = 12 * 1024L))
    }

    @Test
    fun `formatThroughput shows MB per second at and above 1MB per second`() {
        assertEquals("1.4MB/s", formatThroughput(bytesPerSecond = 1_468_006L))
    }

    @Test
    fun `parseCpuTimes reads the aggregate cpu line`() {
        val result = parseCpuTimes("cpu  100 200 300 1000 50 0 0 0 0 0")
        assertEquals(CpuTimes(idle = 1050L, total = 1650L), result)
    }

    @Test
    fun `parseCpuTimes returns null for a per-core line`() {
        assertEquals(null, parseCpuTimes("cpu0 100 200 300 1000 50 0 0 0 0 0"))
    }

    @Test
    fun `parseCpuTimes returns null for an unrecognized line`() {
        assertEquals(null, parseCpuTimes("garbage input"))
    }

    @Test
    fun `cpuUsagePercent computes busy fraction between two samples`() {
        val previous = CpuTimes(idle = 1000L, total = 2000L)
        val current = CpuTimes(idle = 1300L, total = 3000L)
        assertEquals(70, cpuUsagePercent(previous, current))
    }

    @Test
    fun `cpuUsagePercent is 0 when total does not advance`() {
        val sample = CpuTimes(idle = 1000L, total = 2000L)
        assertEquals(0, cpuUsagePercent(sample, sample))
    }
}
