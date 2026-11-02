#!/usr/bin/env python3
"""
Publishes the PesoFlow Android repository directly to your GitHub account as a Public repository
using the GitHub REST API (no git.exe or gh CLI required, only Python 3!).

Usage:
    python scripts/publish_to_github.py --name pesoflow --token ghp_yourPersonalAccessToken
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
    with urllib.request.urlopen(req, timeout=30) as resp:
        raw = resp.read().decode("utf-8")
        return json.loads(raw) if raw else {}


def main() -> None:
    parser = argparse.ArgumentParser(description="Create and push PesoFlow to a Public GitHub repo.")
    parser.add_argument("--name", default="pesoflow", help="GitHub repository name (default: pesoflow)")
    parser.add_argument("--token", default=None, help="GitHub Personal Access Token (or set GITHUB_TOKEN)")
    parser.add_argument("--private", action="store_true", help="Create as private instead of public")
    args = parser.parse_args()

    token = args.token or os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    if not token:
        print("Error: Please pass --token or set GITHUB_TOKEN environment variable.")
        sys.exit(1)

    root = Path(__file__).resolve().parent.parent
    user = github_request("GET", "/user", token)
    owner = user["login"]
    print(f"Authenticated as @{owner}. Ensuring repository '{args.name}' exists...")

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
            print(f"Repository https://github.com/{owner}/{args.name} already exists, updating files...")
        else:
            err_body = e.read().decode("utf-8", errors="ignore")
            print(f"Failed to create repository (HTTP {e.code}): {err_body}")
            sys.exit(1)

    # Get latest commit on default branch (main)
    ref_data = github_request("GET", f"/repos/{owner}/{args.name}/git/ref/heads/main", token)
    base_commit_sha = ref_data["object"]["sha"]

    ignore_dirs = {".git", ".gradle", "build", ".idea", "__pycache__"}
    tree_items = []

    for file_path in sorted(root.rglob("*")):
        if not file_path.is_file():
            continue
        rel = file_path.relative_to(root)
        if any(part in ignore_dirs for part in rel.parts):
            continue

        rel_posix = rel.as_posix()
        content_b64 = base64.b64encode(file_path.read_bytes()).decode("ascii")
        blob = github_request(
            "POST",
            f"/repos/{owner}/{args.name}/git/blobs",
            token,
            {"content": content_b64, "encoding": "base64"},
        )
        mode = "100755" if rel_posix == "gradlew" else "100644"
        tree_items.append(
            {
                "path": rel_posix,
                "mode": mode,
                "type": "blob",
                "sha": blob["sha"],
            }
        )
        print(f"  [OK] Staged {rel_posix}")

    new_tree = github_request(
        "POST",
        f"/repos/{owner}/{args.name}/git/trees",
        token,
        {"tree": tree_items},
    )

    new_commit = github_request(
        "POST",
        f"/repos/{owner}/{args.name}/git/commits",
        token,
        {
            "message": "Release v1.0.1: Minimalist Obsidian & Sage UI, custom logo, and initial GCash balance onboarding",
            "tree": new_tree["sha"],
            "parents": [base_commit_sha],
        },
    )

    github_request(
        "PATCH",
        f"/repos/{owner}/{args.name}/git/refs/heads/main",
        token,
        {"sha": new_commit["sha"], "force": True},
    )

    print(f"\nSuccess! Published {len(tree_items)} files to https://github.com/{owner}/{args.name}")


if __name__ == "__main__":
    main()
