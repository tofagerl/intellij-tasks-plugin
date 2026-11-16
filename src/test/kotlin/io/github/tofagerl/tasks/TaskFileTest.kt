package io.github.tofagerl.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TaskFileTest {
    @Test
    fun `parse reads task lines and skips others`() {
        assertEquals(
            listOf(TaskItem("tom", "Merge PR", false), TaskItem("klaus", "Review", true)),
            TaskFile.parse("- [ ] (tom) Merge PR\n- [x] (klaus) Review\nnotes\n"),
        )
    }

    @Test
    fun `parse treats uppercase X as done`() {
        assertEquals(listOf(TaskItem("tom", "A", true)), TaskFile.parse("- [X] (tom) A"))
    }

    @Test
    fun `parse ignores lines without owner or with other bullet`() {
        assertEquals(emptyList<TaskItem>(), TaskFile.parse("- [ ] Merge\n* [ ] (tom) A\n"))
    }

    @Test
    fun `toggle changes only the matching line`() {
        val content = "- [ ] (tom) Merge PR\n- [x] (klaus) Review\nnotes\n"
        assertEquals(
            "- [x] (tom) Merge PR\n- [x] (klaus) Review\nnotes\n",
            TaskFile.toggle(content, TaskItem("tom", "Merge PR", false)),
        )
    }

    @Test
    fun `toggle keeps CRLF and missing final newline`() {
        val content = "- [ ] (tom) A\r\nnotes\r\n- [ ] (tom) B"
        assertEquals(
            "- [x] (tom) A\r\nnotes\r\n- [ ] (tom) B",
            TaskFile.toggle(content, TaskItem("tom", "A", false)),
        )
        assertEquals(
            "- [ ] (tom) A\r\nnotes\r\n- [x] (tom) B",
            TaskFile.toggle(content, TaskItem("tom", "B", false)),
        )
    }

    @Test
    fun `toggle flips only the first of identical lines`() {
        assertEquals(
            "- [x] (tom) A\n- [ ] (tom) A\n",
            TaskFile.toggle("- [ ] (tom) A\n- [ ] (tom) A\n", TaskItem("tom", "A", false)),
        )
    }

    @Test
    fun `toggle unticks uppercase X as lowercase space`() {
        assertEquals("- [ ] (tom) A", TaskFile.toggle("- [X] (tom) A", TaskItem("tom", "A", true)))
    }

    @Test
    fun `toggle returns null when no line matches`() {
        assertNull(TaskFile.toggle("- [x] (tom) A\n", TaskItem("tom", "A", false)))
        assertNull(TaskFile.toggle("- [ ] (tom) B\n", TaskItem("tom", "A", false)))
    }

    @Test
    fun `title is the first level-one heading`() {
        assertEquals("quest-823 review", TaskFile.title("- [ ] (tom) A\n# quest-823 review \n# later\n"))
        assertNull(TaskFile.title("- [ ] (tom) A\n## not level one\n#nospace\n"))
    }
}
