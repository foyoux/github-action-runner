<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Runner for GitHub Actions Changelog

## [0.0.1] - 2026-01-29

### Added
- **Core Feature**: Run selected text or files on GitHub Actions directly from the IDE.
- **Smart Execution**: Supports Shell scripts, Python, Node.js (via Shebang auto-detection).
- **Transport**: Uses Gzip + Base64 encoding to safely transfer script content.
- **Disk Optimization**: Optional "Free Disk Space" mode for large tasks (e.g., Docker builds).
- **Feedback**: Real-time notifications with direct links to GitHub Job logs.
- **Configuration**: Secure management of GitHub PAT and Repository settings.