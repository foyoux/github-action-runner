# GitHub Action Runner

<!-- Plugin description -->
Run selected scripts (or files) directly on GitHub Actions from your JetBrains IDE.

Ideal for tasks requiring high bandwidth (e.g., Docker image mirroring) or specific Linux environments.

## Features

- **Execute Selection**: Select any text (Shell script, Docker commands, etc.) in the editor and run it on GitHub Actions.
- **Execute File**: Right-click any file to run its content.
- **Free Disk Space**: Optional mode to free up disk space on the GitHub Runner before execution.
- **Secure Configuration**: GitHub Token is stored securely using the IDE's Credential Store.
- **Smart Feedback**: Automatically retrieves and links to the triggered GitHub Action Run.
<!-- Plugin description end -->

## Installation

1. Open **Settings/Preferences** > **Plugins** > **Marketplace**.
2. Search for "**Runner for GitHub Actions**" and install.

## Configuration

Before use, you must configure your GitHub credentials and target repository.

1. Go to **Settings/Preferences** > **Tools** > **Runner for GitHub Actions**.
2. **GitHub Token**: A Personal Access Token (PAT) with `repo` scope.
3. **Repository**: The target GitHub repository in `owner/repo` format (e.g., `foyoux/github-action-runner`).
4. **Branch**: The default branch to trigger the workflow on (e.g., `main`).
5. **Workflow Filename**: The name of the workflow file (default: `jetbrains-runner.yml`).
6. **Runs On**: The system type for the runner (default: `ubuntu-22.04`).

## Workflow Setup

Ensure your target repository has the following workflow file at `.github/workflows/jetbrains-runner.yml`:

```yaml
name: JetBrains Runner

on:
  workflow_dispatch:
    inputs:
      runs-on:
        description: 'Runner System Type'
        type: string
        required: false
        default: 'ubuntu-22.04'
      free-space:
        description: 'Free Disk Space'
        type: boolean
        required: false
        default: false
      base64_content:
        description: 'Gzipped & Base64 encoded script'
        type: string
        required: true

jobs:
  remote-execution:
    runs-on: ${{ inputs.runs-on }}
    steps:
      - name: Free Disk Space
        if: ${{ inputs.free-space }}
        uses: jlumbroso/free-disk-space@main
        with:
          tool-cache: false
          android: true
          dotnet: true
          haskell: true
          large-packages: true
          docker-images: true
          swap-storage: true

      - name: Execute Script
        run: |
          # 🚀 Runner for GitHub Actions
          printf '%s' "${{ inputs.base64_content }}" | base64 -d | gunzip > script.sh
          chmod +x script.sh
          ./script.sh
```

## Usage

1. Select text in the editor OR right-click a file in the project view.
2. Select **Run on GitHub Actions** (or with Free Disk Space) from the context menu.
3. Confirm the dialog.
4. Click the notification link to view the live execution logs on GitHub.
