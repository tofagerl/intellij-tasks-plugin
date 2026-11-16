package io.github.tofagerl.tasks

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import java.nio.file.Path

class TasksState : BaseState() {
    var tasksDir by string(TasksSettings.DEFAULT_DIR)
}

@Service(Service.Level.APP)
@State(name = "TasksSettings", storages = [Storage("tasks.xml")])
class TasksSettings : SimplePersistentStateComponent<TasksState>(TasksState()) {
    /** The tasks directory for [basePath]: relative settings resolve against the project root. */
    fun resolve(basePath: String?): String =
        Path.of(basePath.orEmpty()).resolve(state.tasksDir ?: DEFAULT_DIR).normalize().toString()

    companion object {
        const val DEFAULT_DIR = "tmp/tasks"
        fun getInstance(): TasksSettings = service()
    }
}

class TasksConfigurable : BoundConfigurable("Tasks") {
    private val state get() = TasksSettings.getInstance().state

    override fun createPanel() = panel {
        row("Tasks directory:") {
            textField()
                .columns(30)
                .bindText({ state.tasksDir ?: TasksSettings.DEFAULT_DIR }, { state.tasksDir = it.trim().ifEmpty { null } })
                .comment("Relative to the project root, or absolute. Must match the Claude Code plugin's tasks_dir.")
        }
    }
}
