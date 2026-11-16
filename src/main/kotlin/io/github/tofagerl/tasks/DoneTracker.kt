package io.github.tofagerl.tasks

/** Hides done tasks [hideAfterMs] after they were first seen done. Display only: the file is never changed. */
class DoneTracker(private val hideAfterMs: Long) {
    private val doneSince = mutableMapOf<String, Map<TaskItem, Long>>()

    /** Earliest time a currently shown done task will expire; the caller re-renders then. */
    @Volatile
    var nextExpiry = Long.MAX_VALUE
        private set

    fun startRender() {
        nextExpiry = Long.MAX_VALUE
    }

    /** Items of [path] to show at [now]. A task already done when first seen counts as done since [fileTime]. */
    fun visible(path: String, fileTime: Long, items: List<TaskItem>, now: Long): List<TaskItem> {
        val previous = doneSince[path].orEmpty()
        val since = items.filter { it.done }.associateWith { previous[it] ?: minOf(fileTime, now) }
        doneSince[path] = since
        return items.filter { item ->
            val expiry = since[item]?.plus(hideAfterMs) ?: return@filter true
            (expiry > now).also { if (it) nextExpiry = minOf(nextExpiry, expiry) }
        }
    }
}
