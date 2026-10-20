#!/usr/bin/env bash
# Waits until the Selenium container at SELENIUM_REMOTE_URL reports ready (up to 2 minutes).
set -euo pipefail

url="${SELENIUM_REMOTE_URL:-http://localhost:4444}/status"
for _ in $(seq 1 60); do
  if curl -sf "$url" | grep -Eq '"ready" *: *true'; then
    echo "Selenium is ready at $url"
    exit 0
  fi
  sleep 2
done
echo "Selenium did not become ready at $url" >&2
exit 1
