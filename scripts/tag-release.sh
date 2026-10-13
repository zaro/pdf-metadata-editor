#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
POM_FILE="pom.xml"

PUSH=true
if [ "${1:-}" = "--no-push" ]; then
  PUSH=false
fi

# 1) Validate the version, propagate it to child modules and update the
#    flatpak metainfo (rejects invalid versions before anything else).
"$SCRIPT_DIR/update-version.sh"

VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" "$ROOT_DIR/$POM_FILE")
TAG="v${VERSION}"

cd "$ROOT_DIR"

# 2) Commit version-related changes (poms + metainfo) so the tagged commit
#    carries the bumped version. Everything else in the tree stays untouched.
CHANGED=$(git status --porcelain | sed 's/^...//' \
  | grep -E '(^|/)pom\.xml$|metainfo\.xml' || true)

if [ -n "$CHANGED" ]; then
  echo "Committing version changes:"
  echo "$CHANGED" | sed 's/^/  /'
  echo "$CHANGED" | xargs git add --
  git commit -m "Version ${VERSION}"
else
  echo "No version-related changes to commit"
fi

# 3) The tag must not exist yet
if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null 2>&1; then
  echo "ERROR: tag $TAG already exists locally" >&2
  exit 1
fi
if git ls-remote --tags origin "refs/tags/$TAG" 2>/dev/null | grep -q .; then
  echo "ERROR: tag $TAG already exists on origin" >&2
  exit 1
fi

# 4) Warn (not abort) when not tagging from master
BRANCH=$(git branch --show-current)
if [ "$BRANCH" != "master" ]; then
  echo "WARNING: tagging from branch '$BRANCH' (expected master)"
fi

git tag "$TAG"
echo "Created tag $TAG at $(git rev-parse --short HEAD)"

if [ "$PUSH" = true ]; then
  git push origin "$TAG"
  echo "Pushed $TAG to origin"
else
  echo "Tag created locally (not pushed: --no-push)"
fi