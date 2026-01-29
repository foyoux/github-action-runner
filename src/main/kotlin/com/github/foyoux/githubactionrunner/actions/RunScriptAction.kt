package com.github.foyoux.githubactionrunner.actions

import com.github.foyoux.githubactionrunner.core.GithubService
import com.github.foyoux.githubactionrunner.settings.AppSettingsConfigurable
import com.github.foyoux.githubactionrunner.settings.AppSettingsState
import com.github.foyoux.githubactionrunner.ui.RunConfirmDialog
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import java.time.Instant

import com.intellij.openapi.actionSystem.ActionUpdateThread

abstract class RunScriptAction : AnAction() {

    abstract val freeSpace: Boolean
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // 1. Validate Configuration
        val settings = AppSettingsState.instance
        if (settings.retrieveGhToken().isNullOrBlank() || settings.ghRepository.isBlank()) {
            notify("GitHub Action Runner", "Please configure GitHub Token and Repository.", NotificationType.ERROR) {
                 ShowSettingsUtil.getInstance().showSettingsDialog(project, AppSettingsConfigurable::class.java)
            }
            return
        }

        // 2. Get Content (Strict Context Logic)
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val place = e.place
        
        // Determine if we should look for selection
        val isProjectView = place == ActionPlaces.PROJECT_VIEW_POPUP || place == ActionPlaces.PROJECT_VIEW_TOOLBAR
        val isEditorPopup = place == ActionPlaces.EDITOR_POPUP
        
        val contentToRun = when {
            // Case A: Project View -> Always use File (Ignore any background editor selection)
            isProjectView -> {
                file?.let { readFileContent(it) }
            }
            // Case B: Editor Popup -> Use Selection if present, else File
            isEditorPopup -> {
                if (editor != null && editor.selectionModel.hasSelection()) {
                    editor.selectionModel.selectedText
                } else {
                    file?.let { readFileContent(it) }
                }
            }
            // Case C: Shortcut / Main Menu -> Prefer Selection, fallback to File
            else -> {
                if (editor != null && editor.selectionModel.hasSelection()) {
                    editor.selectionModel.selectedText
                } else {
                    file?.let { readFileContent(it) }
                }
            }
        }

        if (contentToRun.isNullOrBlank()) {
             notify("GitHub Action Runner", "No content selected or file is empty.", NotificationType.WARNING)
             return
        }
        
        // 3. Confirm Dialog (Custom UI)
        val dialog = RunConfirmDialog(project, contentToRun)
        if (!dialog.showAndGet()) return

        // 4. Execute
        val timestampBeforeTrigger = Instant.now().minusSeconds(5).toEpochMilli() // Buffer

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Triggering GitHub Action", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val service = GithubService()
                    service.triggerWorkflow(contentToRun, freeSpace)
                    
                    ApplicationManager.getApplication().invokeLater {
                         notify("GitHub Action Runner", "Request sent. Waiting for Run ID...", NotificationType.INFORMATION)
                    }
                    
                    // Poll for Run ID
                    var runId: Long? = null
                    for (i in 1..5) { // Try 5 times
                        if (indicator.isCanceled) return
                        indicator.text = "Waiting for Run ID (Attempt $i/5)..."
                        
                        Thread.sleep(2000) // Wait 2s
                        
                        runId = service.getLatestRunId(timestampBeforeTrigger)
                        if (runId != null) break
                    }
                    
                    if (runId != null) {
                        // Fetch Job URL for direct console access
                        val jobUrl = service.getJobUrl(runId)
                        val mainUrl = jobUrl ?: "https://github.com/${settings.ghRepository}/actions/runs/$runId"
                        
                        ApplicationManager.getApplication().invokeLater {
                             notify("GitHub Action Runner", "Workflow started (Run ID: $runId)", NotificationType.INFORMATION) { notification ->
                                 notification.addAction(com.intellij.notification.NotificationAction.createSimple("View Job Log") {
                                     BrowserUtil.browse(mainUrl)
                                 })
                                 notification.addAction(com.intellij.notification.NotificationAction.createSimple("View All Runs") {
                                     BrowserUtil.browse("https://github.com/${settings.ghRepository}/actions/workflows/${settings.workflowFile}")
                                 })
                             }
                        }
                    } else {
                        val runsUrl = "https://github.com/${settings.ghRepository}/actions/workflows/${settings.workflowFile}"
                         ApplicationManager.getApplication().invokeLater {
                             notify("GitHub Action Runner", "Workflow triggered but Run ID not found. Please check manually.", NotificationType.WARNING) { notification ->
                                 notification.addAction(com.intellij.notification.NotificationAction.createSimple("Open Runs List") {
                                     BrowserUtil.browse(runsUrl)
                                 })
                             }
                        }
                    }
                    
                } catch (ex: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        notify("GitHub Action Runner", "Failed to trigger workflow: ${ex.message}", NotificationType.ERROR)
                    }
                }
            }
        })
    }
    
    private fun readFileContent(file: com.intellij.openapi.vfs.VirtualFile): String? {
        return try {
            // Try to read from Document (memory) first to get unsaved changes
            val document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file)
            if (document != null) {
                document.text
            } else {
                // Fallback to disk content
                String(file.contentsToByteArray(), file.charset)
            }
        } catch (ex: Exception) {
            null
        }
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        
        e.presentation.isEnabledAndVisible = project != null && (
            (editor != null && editor.selectionModel.hasSelection()) || file != null
        )
    }

    private fun notify(title: String, content: String, type: NotificationType, customizer: ((com.intellij.notification.Notification) -> Unit)? = null) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup("GitHub Action Runner Notification")
        val notification = group.createNotification(title, content, type)
        
        if (customizer != null) {
            customizer(notification)
        }
        
        notification.notify(null)
    }
}
