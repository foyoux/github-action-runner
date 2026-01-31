# 🐳 High-Speed Docker Image Mirroring

GitHub Actions runners possess incredibly high network bandwidth (often exceeding 10Gbps). This makes them perfect for moving large Docker images between registries (e.g., from Docker Hub to a private registry or a different region) without consuming your local bandwidth.

## The Script

This script uses `skopeo` to copy images directly between registries without pulling them to the disk first.

> **💡 Best Practice:** Run this in a **Private Repository**. This allows you to hardcode your registry passwords directly in the script for convenience, bypassing the need for GitHub Secrets.

### Copy & Paste Template

Select the code below, modify the credentials, and run it via the plugin!

```bash
#!/bin/bash

sudo apt-get update -qq && sudo apt-get install -y -qq skopeo

skopeo login -u "YOUR_USERNAME" -p "YOUR_PASSWORD" "swr.cn-south-1.myhuaweicloud.com"

skopeo copy --all --retry-times 10 \
    docker://vllm/vllm-openai:v0.15.0 \
    docker://swr.cn-south-1.myhuaweicloud.com/YOUR_NAMESPACE/vllm-openai:v0.15.0
```
