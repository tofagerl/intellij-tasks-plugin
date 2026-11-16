package io.github.tofagerl.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DoneTrackerTest {
    private val open = TaskItem("tom", "A", false)
    private val done = TaskItem("tom", "A", true)
    private val other = TaskItem("klaus", "B", false)

    @Test
    fun `done task stays visible until hideAfter has passed since it was first seen done`() {
        val tracker = DoneTracker(100)
        assertEquals(listOf(open, other), tracker.visible("f", 0, listOf(open, other), 1000))
        assertEquals(listOf(done, other), tracker.visible("f", 1050, listOf(done, other), 1050))
        assertEquals(1150, tracker.nextExpiry)
        assertEquals(listOf(done, other), tracker.visible("f", 1050, listOf(done, other), 1149))
        assertEquals(listOf(other), tracker.visible("f", 1050, listOf(done, other), 1150))
    }

    @Test
    fun `done task in an old file is hidden at once`() {
        assertEquals(listOf(other), DoneTracker(100).visible("f", 0, listOf(done, other), 1000))
    }

    @Test
    fun `unticking and reticking restarts the clock`() {
        val tracker = DoneTracker(100)
        tracker.visible("f", 1000, listOf(done), 1000)
        tracker.visible("f", 1080, listOf(open), 1080)
        assertEquals(listOf(done), tracker.visible("f", 1090, listOf(done), 1150))
    }
}
