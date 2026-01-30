package com.github.foyoux.githubactionrunner.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

import com.intellij.openapi.application.ApplicationManager

class AppSettingsConfigurable : Configurable {

    private var settingsPanel: DialogPanel? = null
    private var myToken: String = ""

    override fun getDisplayName(): String = "Runner for GitHub Actions"

    override fun createComponent(): JComponent? {
        settingsPanel = panel {
            val settings = AppSettingsState.instance

            group("GitHub Configuration") {
                row("GitHub Token:") {
                    passwordField()
                        .bindText(
                            { myToken },
                            { myToken = it }
                        )
                        .comment("Personal Access Token with 'repo' scope.")
                }
                row("Repository:") {
                    textField()
                        .bindText(settings::ghRepository)
                        .comment("Format: owner/repo (e.g., foyoux/github-action-runner)")
                }
                row("Branch:") {
                    textField()
                        .bindText(settings::ghBranch)
                        .comment("Default branch to run the workflow (e.g., main, master).")
                }
                row("Workflow Filename:") {
                    textField()
                        .bindText(settings::workflowFile)
                        .comment("The filename of the workflow in .github/workflows/ (e.g., jetbrains-runner.yml).")
                }
                row("Runs On:") {
                    comboBox(listOf(
                        "ubuntu-latest", "ubuntu-24.04", "ubuntu-22.04", "ubuntu-20.04",
                        "windows-latest", "windows-2022", "windows-2019",
                        "macos-latest", "macos-14", "macos-13", "macos-12"
                    ))
                        .bindItem(
                            { settings.runsOn },
                            { settings.runsOn = it ?: "ubuntu-22.04" }
                        )
                        .applyToComponent { isEditable = true }
                        .comment("System type for the runner (e.g., ubuntu-22.04). Can be customized.")
                }
                
                separator()
                
                row {
                    browserLink("View Documentation & Source Code", "https://github.com/foyoux/github-action-runner")
                }
            }
        }
        return settingsPanel
    }

    override fun isModified(): Boolean {
        return settingsPanel?.isModified() ?: false
    }

    override fun apply() {
        settingsPanel?.apply()
        AppSettingsState.instance.saveGhToken(myToken)
    }

    override fun reset() {
        val settings = AppSettingsState.instance
        // Reset non-token fields immediately
        settingsPanel?.reset()
        
        // Load token asynchronously
        ApplicationManager.getApplication().executeOnPooledThread {
            val token = settings.retrieveGhToken() ?: ""
            ApplicationManager.getApplication().invokeLater {
                if (settingsPanel != null) {
                    myToken = token
                    settingsPanel?.reset()
                }
            }
        }
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}