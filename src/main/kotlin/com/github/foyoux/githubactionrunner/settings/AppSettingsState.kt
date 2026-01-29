package com.github.foyoux.githubactionrunner.settings

import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.foyoux.githubactionrunner.settings.AppSettingsState",
    storages = [Storage("GithubActionRunnerPlugin.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {

    var ghRepository: String = "" // merged owner/repo
    var ghBranch: String = "main"
    var workflowFile: String = "jetbrains-runner.yml"
    var runsOn: String = "ubuntu-22.04"

    // Helper to access token securely
    fun setGhToken(token: String?) {
        val attributes = createCredentialAttributes()
        val credentials = if (token != null) Credentials("GitHubToken", token) else null
        PasswordSafe.instance.set(attributes, credentials)
    }

    fun getGhToken(): String? {
        val attributes = createCredentialAttributes()
        val credentials = PasswordSafe.instance.get(attributes)
        return credentials?.getPasswordAsString()
    }

    private fun createCredentialAttributes(): CredentialAttributes {
        return CredentialAttributes(
            generateServiceName("GithubActionRunner", "GitHubToken")
        )
    }

    companion object {
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }

    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}