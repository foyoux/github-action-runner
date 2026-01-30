# Runner for GitHub Actions

<!-- Plugin description -->
Run selected scripts (or files) directly on GitHub Actions from your JetBrains IDE.

Ideal for tasks requiring high bandwidth (e.g., Docker image mirroring), specific Linux environments, or when you just need a clean cloud environment to run a snippet of code.

## ✨ Features

- **Execute Selection**: Select any text (Shell, Python, Node.js, etc.) in the editor and run it instantly.
- **Execute File**: Right-click any file to run its content.
- **Cloud Runner UI**: Beautiful "Cloud Runner" icons and confirmation dialogs.
- **Free Disk Space**: One-click option to free up disk space on the GitHub Runner before execution.
- **Secure Configuration**: GitHub Token is stored safely using the IDE's Credential Store.
- **Smart Feedback**: Real-time notifications with direct links to live GitHub Job logs.

<!-- Plugin description end -->

## 📸 Screenshots

| Context Menu | Confirm Dialog |
| :---: | :---: |
| ![Context Menu](docs/images/context-menu.png) | ![Confirm Dialog](docs/images/confirm-dialog.png) |
| *Right-click to run selection or file* | *Preview content before triggering* |

## 🚀 Getting Started

### 1. Installation

1. Open **Settings/Preferences** > **Plugins** > **Marketplace**.
2. Search for "**Runner for GitHub Actions**" and install.

### 2. Workflow Setup

Create a file named `.github/workflows/jetbrains-runner.yml` in your target repository:

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
      script:
        description: 'Script Content'
        type: string
        required: true
      gzip:
        description: 'Is Base64 encoded Gzip content'
        type: boolean
        required: false
        default: false

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
          
          # 1. Save input to file
          printf '%s' "${{ inputs.script }}" > raw_script
          
          # 2. Process Script
          # If gzip is enabled, it MUST be Base64 encoded to survive transport.
          if [ "${{ inputs.gzip }}" == "true" ]; then
            base64 -d raw_script | gunzip > script.sh
          else
            mv raw_script script.sh
          fi

          # 3. Execute
          chmod +x script.sh
          ./script.sh
```

### 3. Configuration

Go to **Settings/Preferences** > **Tools** > **Runner for GitHub Actions**.

![Settings Panel](docs/images/settings.png)

1. **GitHub Token**: A Personal Access Token (PAT) with `repo` scope.
2. **Repository**: Target repository (e.g., `foyoux/github-action-runner`).
3. **Branch**: Branch to trigger (e.g., `main`).
4. **Workflow Filename**: `jetbrains-runner.yml` (default).
5. **Runs On**: Runner type (e.g., `ubuntu-22.04`, `windows-latest`).

---

## 📖 Usage Guide

### Running Scripts

1. **Select Code**: Highlight code in your editor (or right-click a file).
2. **Trigger**: Right-click and choose **Run on GitHub Actions**.
3. **Confirm**: Review the script in the dialog and click OK.
4. **Monitor**: Click the notification link to watch the execution live on GitHub.

![Notification](docs/images/notification.png)

### Script Support & Languages

The plugin supports any language available on the GitHub Runner. Ensure you include a **Shebang** for non-shell scripts:

*   **Python**: `#!/usr/bin/env python3`
*   **Node.js**: `#!/usr/bin/env node`
*   **Bash**: `#!/bin/bash` (Optional, default)

### Free Disk Space Mode

Select **Run on GitHub Actions (Free Disk Space)** to perform a cleanup step before your script runs. Useful for Docker builds or large artifacts.

### Manual Execution

You can also manually run scripts from the GitHub Actions UI!
1. Go to your repository's **Actions** tab.
2. Select **JetBrains Runner**.
3. Click **Run workflow**.
4. Paste your script directly into the **Script Content** box (no encoding needed).