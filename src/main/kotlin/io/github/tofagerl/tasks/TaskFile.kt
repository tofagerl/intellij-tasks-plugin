package io.github.tofagerl.tasks

data class TaskItem(val owner: String, val text: String, val done: Boolean)

object TaskFile {
    private val LINE = Regex("""^- \[( |x|X)\] \(([a-z][a-z0-9-]*)\) (.+)$""")

    fun parse(content: String): List<TaskItem> =
        content.lines().mapNotNull { line -> match(line)?.toItem() }

    fun toggle(content: String, item: TaskItem): String? {
        // Split keeping each line's terminator so CRLF and a missing final newline survive.
        val parts = Regex("[^\n]*\n|[^\n]+").findAll(content).map { it.value }.toMutableList()
        for (i in parts.indices) {
            val body = parts[i].trimEnd('\n').trimEnd('\r')
            val tail = parts[i].substring(body.length)
            val m = match(body) ?: continue
            if (m.toItem() == item) {
                parts[i] = "- [${if (item.done) ' ' else 'x'}] (${item.owner}) ${item.text}$tail"
                return parts.joinToString("")
            }
        }
        return null
    }

    private fun match(line: String): MatchResult? = LINE.matchEntire(line.trimEnd('\r'))

    private fun MatchResult.toItem() = TaskItem(groupValues[2], groupValues[3], groupValues[1] != " ")
}
