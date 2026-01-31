# 📚 Examples & Use Cases

Collection of practical scripts and use cases for the **Runner for GitHub Actions** plugin.

## 💡 The "Private Repo" Strategy

The most efficient way to use this plugin is with a **Private Repository**.

1.  Create a private GitHub repository (free for individuals).
2.  Set up the `jetbrains-runner.yml` workflow there.
3.  **Benefit:** You can hardcode API keys, Tokens, and Passwords directly in your scripts (Plaintext).
    *   No need to configure GitHub Secrets.
    *   No need to worry about leaks (since only you can see the repo).
    *   Modify and run scripts instantly.

## 📂 Catalog

*   **[High-Speed Docker Image Mirroring](docker-mirror.md)**  
    Utilize GitHub's 10Gbps+ bandwidth to sync Docker images between registries (e.g., Docker Hub -> Private Registry) in seconds.
