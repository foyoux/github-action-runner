<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Runner for GitHub Actions Changelog

## [0.1.1] - 2026-01-30

### Added
- **UX**: Added "View Documentation" link in Settings and error notifications.
- **UX**: Added a smart "Generate Token" link to the README for easier setup.

### Fixed
- **Error Handling**: Improved feedback for 401 (Unauthorized) and 404 (Not Found) API errors with direct "Open Settings" actions.
- **Workflow**: Enforced Base64 encoding for the `script` input in `jetbrains-runner.yml` to ensure formatting (newlines, spaces) is preserved during manual execution.

### Internal
- **Cleanup**: Optimized code imports and performed general project cleanup.

## [0.1.0] - 2026-01-30

### Added
- **UI/UX**: Brand new "Cloud Runner" plugin logo and action icons, supporting both Light and Dark themes.
- **Workflow**: Enhanced `jetbrains-runner.yml` to support manual script execution directly from GitHub Actions UI (plaintext input).

### Changed
- **Experience**: Confirmation dialog now displays the specific action icon (Run vs. Cleanup) for better visual feedback.
- **Optimization**: Simplified workflow inputs by merging `gzip` and `base64` logic. Plugin transport remains optimized (Gzip+Base64).

## [0.0.2] - 2026-01-30

### Added
- **Feature**: Added "Runs On" configuration to specify runner type (e.g., `ubuntu-22.04`, `windows-latest`).
- **Documentation**: Added "Script Support" section clarifying Python/Node.js usage with Shebangs.

### Changed
- **UI**: Updated confirmation dialog title to clearly indicate when "Free Disk Space" mode is active.

### Fixed
- **Stability**: Resolved `SlowOperations` exception in Settings UI by optimizing secure token retrieval.

## [0.0.1] - 2026-01-29

### Added
- **Core Feature**: Run selected text or files on GitHub Actions directly from the IDE.
- **Smart Execution**: Supports Shell scripts, Python, Node.js (via Shebang auto-detection).
- **Transport**: Uses Gzip + Base64 encoding to safely transfer script content.
- **Disk Optimization**: Optional "Free Disk Space" mode for large tasks (e.g., Docker builds).
- **Feedback**: Real-time notifications with direct links to GitHub Job logs.
- **Configuration**: Secure management of GitHub PAT and Repository settings.