package io.github.tofagerl.tasks

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import java.util.concurrent.TimeUnit
import javax.swing.BoxLayout
import javax.swing.JPanel

class TasksPanel(private val project: Project, parent: Disposable) : JPanel(BorderLayout()) {
    private val content = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
    }
    private val dirPath get() = TasksSettings.getInstance().resolve(project.basePath)
    @Volatile
    private var renderedDir = ""
    private val doneTracker = DoneTracker(HIDE_DONE_AFTER_MS)

    init {
        add(JBScrollPane(content), BorderLayout.CENTER)

        project.messageBus.connect(parent).subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    if (events.any { isTaskPath(it.path) }) scheduleRefresh()
                }
            },
        )
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(
            object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    val path = FileDocumentManager.getInstance().getFile(event.document)?.path
                    if (path != null && isTaskPath(path)) scheduleRefresh()
                }
            },
            parent,
        )
        // shortcut: polls for external writes every 2s, replace with a native file watcher root if it shows up in profiling
        val poller = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
            {
                LocalFileSystem.getInstance().refreshAndFindFileByPath(dirPath)?.refresh(false, true)
                if (System.currentTimeMillis() >= doneTracker.nextExpiry || dirPath != renderedDir) scheduleRefresh()
            },
            POLL_MS, POLL_MS, TimeUnit.MILLISECONDS,
        )
        Disposer.register(parent) { poller.cancel(false) }
        refresh()
    }

    fun refresh() {
        content.removeAll()
        renderedDir = dirPath
        doneTracker.startRender()
        val now = System.currentTimeMillis()
        val sessions = ReadAction.compute<List<Session>, Throwable> {
            taskFiles().mapNotNull { vf ->
                FileDocumentManager.getInstance().getDocument(vf)?.text?.let {
                    val items = doneTracker.visible(vf.path, vf.timeStamp, TaskFile.parse(it), now)
                    Session(vf, TaskFile.title(it) ?: vf.parent.name.take(8), items)
                }
            }
        }
            .filter { it.items.isNotEmpty() }
            // Sessions with open tasks first, newest first; fully done sessions sink to the bottom.
            .sortedWith(compareBy<Session> { s -> s.items.all { it.done } }.thenByDescending { it.file.timeStamp })
        if (sessions.isEmpty()) content.add(JBLabel("No open tasks in $renderedDir"))
        sessions.forEach { addSession(it) }
        content.revalidate()
        content.repaint()
    }

    private fun addSession(session: Session) {
        val (vf, title, items) = session
        content.add(JBLabel(title).apply {
            font = font.deriveFont(Font.BOLD, font.size2D + 1)
            border = JBUI.Borders.empty(12, 0, 2, 0)
        })
        items.groupBy { it.owner }
            .toSortedMap(compareBy<String> { it != OWNER_TOM }.thenBy { it })
            .forEach { (owner, ownerItems) ->
                content.add(JBLabel(owner).apply {
                    font = font.deriveFont(Font.BOLD)
                    border = JBUI.Borders.empty(4, 8, 2, 0)
                })
                ownerItems.forEach { item ->
                    content.add(JBCheckBox(item.text, item.done).apply {
                        border = JBUI.Borders.emptyLeft(8)
                        isEnabled = owner == OWNER_TOM
                        addActionListener { toggle(vf, item) }
                    })
                }
            }
    }

    private fun toggle(vf: VirtualFile, item: TaskItem) {
        WriteCommandAction.runWriteCommandAction(project) {
            val document = FileDocumentManager.getInstance().getDocument(vf) ?: return@runWriteCommandAction
            TaskFile.toggle(document.text, item)?.let {
                document.setText(it)
                FileDocumentManager.getInstance().saveDocument(document)
            }
        }
        refresh()
    }

    // One folder per session: tmp/tasks/<session-id>/tasks.md. Caller must hold a read action.
    private fun taskFiles(): List<VirtualFile> =
        LocalFileSystem.getInstance().findFileByPath(renderedDir)?.children
            ?.filter { it.isDirectory }
            ?.mapNotNull { it.findChild(TASKS_FILE) }
            .orEmpty()

    private data class Session(val file: VirtualFile, val title: String, val items: List<TaskItem>)

    private fun isTaskPath(path: String) = path == dirPath || path.startsWith("$dirPath/")

    private fun scheduleRefresh() =
        ApplicationManager.getApplication().invokeLater({ refresh() }, project.disposed)

    companion object {
        private const val OWNER_TOM = "tom"
        private const val TASKS_FILE = "tasks.md"
        private const val POLL_MS = 2000L
        private const val HIDE_DONE_AFTER_MS = 5 * 60 * 1000L
    }
}
