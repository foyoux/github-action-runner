package com.github.foyoux.githubactionrunner.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {

    private var settingsPanel: DialogPanel? = null

    override fun getDisplayName(): String = "Runner for GitHub Actions"

    override fun createComponent(): JComponent? {
        settingsPanel = panel {
            val settings = AppSettingsState.instance

            group("GitHub Configuration") {
                row("GitHub Token:") {
                    passwordField()
                        .bindText(
                            { settings.getGhToken() ?: "" },
                            { settings.setGhToken(it) }
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
            }
        }
        return settingsPanel
    }

    override fun isModified(): Boolean {
        return settingsPanel?.isModified() ?: false
    }

    override fun apply() {
        settingsPanel?.apply()
    }

    override fun reset() {
        settingsPanel?.reset()
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}