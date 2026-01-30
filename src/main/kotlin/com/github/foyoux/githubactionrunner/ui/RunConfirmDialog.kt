package com.github.foyoux.githubactionrunner.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.Align
import javax.swing.JComponent
import javax.swing.JTextArea

import com.intellij.ui.components.JBScrollPane
import java.awt.Dimension

import javax.swing.Icon
import com.intellij.ui.dsl.builder.RightGap

class RunConfirmDialog(
    project: Project,
    private val scriptContent: String,
    private val titleText: String = "Confirm Run on GitHub Actions",
    private val icon: Icon? = null
) : DialogWrapper(project) {

    init {
        title = titleText
        init()
    }

    override fun createCenterPanel(): JComponent {
        val previewText = if (scriptContent.length > 1000) {
            scriptContent.take(1000) + "\n... (truncated)"
        } else {
            scriptContent
        }
        
        val previewArea = JTextArea(previewText).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            // Remove explicit rows here, control via ScrollPane size
        }
        
        val scrollPane = JBScrollPane(previewArea).apply {
            preferredSize = Dimension(600, 300)
            minimumSize = Dimension(400, 200)
        }

        return panel {
            row {
                if (icon != null) {
                    icon(icon).gap(RightGap.SMALL)
                }
                label("Script Preview:")
            }
            row {
                cell(scrollPane)
                    .align(Align.FILL)
            }.resizableRow()
        }
    }
}
