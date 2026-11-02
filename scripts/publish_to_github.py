#!/usr/bin/env python3
"""
Publishes the PesoFlow Android repository directly to your GitHub account as a Public repository
using the GitHub REST API (no git.exe or gh CLI required, only Python 3!).

Usage:
    set GITHUB_TOKEN=ghp_yourPersonalAccessToken
    python scripts/publish_to_github.py --name pesoflow
"""

import argparse
import base64
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

API_BASE = "https://api.github.com"


def github_request(method: str, path: str, token: str, payload: dict | None = None) -> dict:
    url = f"{API_BASE}{path}"
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    req = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "PesoFlow-Repo-Publisher",
        },
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main() -> None:
    parser = argparse.ArgumentParser(description="Create and push PesoFlow to a Public GitHub repo.")
    parser.add_argument("--name", default="pesoflow", help="GitHub repository name (default: pesoflow)")
    parser.add_argument("--private", action="store_true", help="Create as private instead of public")
    args = parser.parse_args()

    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    if not token:
        print("Error: Please set GITHUB_TOKEN environment variable with a GitHub Personal Access Token.")
        print("Example (PowerShell):")
        print('  $env:GITHUB_TOKEN="ghp_xxx"; python scripts/publish_to_github.py')
        sys.exit(1)

    root = Path(__file__).resolve().parent.parent
    user = github_request("GET", "/user", token)
    owner = user["login"]
    print(f"Authenticated as @{owner}. Creating repository '{args.name}'...")

    try:
        repo = github_request(
            "POST",
            "/user/repos",
            token,
            {
                "name": args.name,
                "description": "PesoFlow — Hybrid Zero-Based & Cashflow Budgeting Android App with GCash Auto-Sync (Kotlin + Jetpack Compose + Room)",
                "private": args.private,
                "auto_init": True,
            },
        )
        print(f"Created repository: {repo['html_url']}")
    except urllib.error.HTTPError as e:
        if e.code == 422:
            print(f"Repository @{owner}/{args.name} already exists, uploading files...")
        else:
            raise

    ignore_dirs = {".git", ".gradle", "build", ".idea"}
    files_uploaded = 0

    for file_path in sorted(root.rglob("*")):
        if not file_path.is_file():
            continue
        rel = file_path.relative_to(root)
        if any(part in ignore_dirs for part in rel.parts):
            continue

        rel_posix = rel.as_posix()
        content_b64 = base64.b64encode(file_path.read_bytes()).decode("ascii")

        # Check if file exists to get sha
        sha = None
        try:
            existing = github_request("GET", f"/repos/{owner}/{args.name}/contents/{rel_posix}", token)
            sha = existing.get("sha")
        except urllib.error.HTTPError:
            pass

        body = {
            "message": f"Add {rel_posix}",
            "content": content_b64,
        }
        if sha:
            body["sha"] = sha

        github_request("PUT", f"/repos/{owner}/{args.name}/contents/{rel_posix}", token, body)
        files_uploaded += 1
        print(f"  ✓ Uploaded {rel_posix}")

    print(f"\nSuccess! Uploaded {files_uploaded} files to https://github.com/{owner}/{args.name}")


if __name__ == "__main__":
    main()
