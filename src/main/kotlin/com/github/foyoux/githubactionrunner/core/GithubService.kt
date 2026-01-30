package com.github.foyoux.githubactionrunner.core

import com.github.foyoux.githubactionrunner.settings.AppSettingsState
import com.intellij.util.io.HttpRequests
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.HttpURLConnection

class GithubService {

    private val gson = Gson()

    data class DispatchPayload(
        val ref: String,
        val inputs: Map<String, String>
    )

    fun triggerWorkflow(content: String, freeSpace: Boolean) {
        val settings = AppSettingsState.instance
        val token = settings.retrieveGhToken()
        val repository = settings.ghRepository
        val branch = settings.ghBranch
        val workflowFile = settings.workflowFile
        val runsOn = settings.runsOn

        validateConfig(token, repository, branch, workflowFile)

        val encodedContent = GzipUtil.compressAndEncode(content)
        
        val inputs = mapOf(
            "free-space" to freeSpace.toString(),
            "script" to encodedContent,
            "gzip" to "true",
            "runs-on" to runsOn
        )

        val payload = DispatchPayload(ref = branch, inputs = inputs)
        val jsonPayload = gson.toJson(payload)
        
        val url = "https://api.github.com/repos/$repository/actions/workflows/$workflowFile/dispatches"

        try {
            HttpRequests.post(url, "application/json")
                .tuner { connection ->
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                }
                .connect { request ->
                    request.write(jsonPayload)
                    val connection = request.connection as HttpURLConnection
                    if (connection.responseCode !in 200..299) {
                         val errorResponse = request.readString()
                         throw IllegalStateException("GitHub API Error (${connection.responseCode}): $errorResponse")
                    }
                }
        } catch (e: Exception) {
            if (e is com.intellij.util.io.HttpRequests.HttpStatusException) {
                if (e.statusCode == 404) {
                    throw RuntimeException("404 Not Found: Workflow '$workflowFile' not found in '$repository'.")
                }
                if (e.statusCode == 401) {
                    throw RuntimeException("401 Unauthorized: Invalid GitHub Token.")
                }
            }
            throw e
        }
    }
    
    fun getLatestRunId(afterTimestamp: Long): Long? {
        val settings = AppSettingsState.instance
        val token = settings.retrieveGhToken()
        val repository = settings.ghRepository
        val branch = settings.ghBranch
        val workflowFile = settings.workflowFile
        
        // Remove .yml or .yaml extension to get workflow ID or just filter by filename in list
        // Actually, listing runs for a workflow file is supported
        
        val url = "https://api.github.com/repos/$repository/actions/workflows/$workflowFile/runs?branch=$branch&event=workflow_dispatch&per_page=1"
        
        return try {
             HttpRequests.request(url)
                .tuner { connection ->
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                }
                .connect { request ->
                    val response = gson.fromJson(request.readString(), JsonObject::class.java)
                    val runs = response.getAsJsonArray("workflow_runs")
                    if (runs.size() > 0) {
                        val latestRun = runs[0].asJsonObject
                        // "created_at": "2023-01-01T00:00:00Z"
                        val createdAtStr = latestRun.get("created_at").asString
                        val createdAt = java.time.Instant.parse(createdAtStr).toEpochMilli()
                        
                        // Check if this run was created after we triggered it
                        // Giving a small buffer (e.g. -5 seconds) to account for clock skew, 
                        // though API usually is consistent. 
                        // But strictly, we should look for runs created AFTER our trigger time.
                        // Let's rely on the fact that we are polling.
                        
                        if (createdAt >= afterTimestamp) {
                            latestRun.get("id").asLong
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
        } catch (e: Exception) {
            // Ignore errors during polling
            null
        }
    }
    
    fun getJobUrl(runId: Long): String? {
        val settings = AppSettingsState.instance
        val token = settings.retrieveGhToken()
        val repository = settings.ghRepository
        
        val url = "https://api.github.com/repos/$repository/actions/runs/$runId/jobs"
        
        return try {
             HttpRequests.request(url)
                .tuner { connection ->
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                }
                .connect { request ->
                    val response = gson.fromJson(request.readString(), JsonObject::class.java)
                    val jobs = response.getAsJsonArray("jobs")
                    if (jobs.size() > 0) {
                        // Return the HTML URL of the first job
                        // Ideally we should find the one matching our job name, but usually there's only one or the first one is main
                        jobs[0].asJsonObject.get("html_url").asString
                    } else {
                        null
                    }
                }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun validateConfig(token: String?, repository: String, branch: String, workflowFile: String) {
        if (token.isNullOrBlank()) {
             throw IllegalStateException("GitHub Token is missing.")
        }
        if (repository.isBlank() || !repository.contains("/")) {
            throw IllegalStateException("Repository is invalid (expected 'owner/repo').")
        }
        if (branch.isBlank()) {
            throw IllegalStateException("Branch is not configured.")
        }
    }
}