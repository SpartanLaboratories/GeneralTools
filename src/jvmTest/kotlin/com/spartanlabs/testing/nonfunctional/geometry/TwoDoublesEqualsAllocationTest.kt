package com.spartanlabs.testing.nonfunctional.geometry

import com.spartanlabs.geometry.Point
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Level 4c (Non-Functional Validation) check of assumption A5: the `this::class` reads in
 * `TwoDoubles.equals` and `hashCode` allocate nothing measurable once the JIT has compiled them, so
 * comparing Points every frame stays allocation-free. Asserts allocated bytes only, never timing. Skipped
 * when the JVM cannot measure per-thread allocation or runs interpreted (`-Xint`).
 */
class TwoDoublesEqualsAllocationTest {
    private val threadBean = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean

    // Two arrays of distinct but equal Points, so every comparison runs the full equals path.
    private val left = Array(1024) { Point(it.toDouble(), it * 0.5) }
    private val right = Array(1024) { Point(it.toDouble(), it * 0.5) }

    // 8 MiB across 5,000,000 calls is under 1.7 bytes per call; a surviving ClassReference costs 16 bytes per read.
    private val allocationBudget = 8L * 1024 * 1024
    private val warmUpCalls = 2_000_000
    private val measuredCalls = 5_000_000

    private fun assumeMeasurable() {
        assumeTrue(threadBean.isThreadAllocatedMemorySupported && threadBean.isThreadAllocatedMemoryEnabled)
        assumeTrue("-Xint" !in ManagementFactory.getRuntimeMXBean().inputArguments)
    }

    private fun countEqual(calls: Int): Int {
        var sink = 0
        for (i in 0 until calls) {
            val k = i and 1023
            if (left[k] == right[k]) sink++
        }
        return sink
    }

    private fun sumHashes(calls: Int): Int {
        var sink = 0
        for (i in 0 until calls) sink += left[i and 1023].hashCode()
        return sink
    }

    private fun allocatedBytes(): Long = threadBean.getThreadAllocatedBytes(Thread.currentThread().id)

    @Test
    fun `Point equals allocates nothing once compiled`() {
        assumeMeasurable()
        assertEquals(warmUpCalls, countEqual(warmUpCalls))
        val before = allocatedBytes()
        val sink = countEqual(measuredCalls)
        val allocated = allocatedBytes() - before
        assertEquals(measuredCalls, sink)
        assertTrue(allocated < allocationBudget, "equals allocated $allocated bytes over $measuredCalls calls")
    }

    @Test
    fun `Point hashCode allocates nothing once compiled`() {
        assumeMeasurable()
        val warmUpSink = sumHashes(warmUpCalls)
        val before = allocatedBytes()
        val sink = sumHashes(measuredCalls)
        val allocated = allocatedBytes() - before
        // The sum is deterministic, so a second run outside the measurement must reproduce it.
        assertEquals(sumHashes(measuredCalls), sink)
        assertTrue(warmUpSink == sumHashes(warmUpCalls))
        assertTrue(allocated < allocationBudget, "hashCode allocated $allocated bytes over $measuredCalls calls")
    }
}
