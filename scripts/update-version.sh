#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
POM_FILE="pom.xml"

if ! command -v xmllint >/dev/null 2>&1; then
  echo "ERROR: xmllint not found (install libxml2-utils)" >&2
  exit 1
fi

VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" "$ROOT_DIR/$POM_FILE")

# Validation FIRST: reject anything the packaging pipeline cannot handle.
# Valid: X.Y.Z with an optional GLUED prerelease suffix (no hyphen!):
#   4.4.1, 4.4.1rc1, 4.5.0beta1
if ! echo "$VERSION" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+(beta[0-9]+|rc[0-9]+)?$'; then
  echo "ERROR: version '$VERSION' in $POM_FILE is not a valid release version." >&2
  echo "  Expected: X.Y.Z with optional glued suffix betaN/rcN (no hyphen)." >&2
  echo "  Hyphenated versions (e.g. 4.4.1-rc1) and SNAPSHOT break the packaging pipeline." >&2
  echo "  Examples: 4.4.1, 4.4.1rc1, 4.5.0beta1" >&2
  exit 1
fi

echo "Updating version: $VERSION"

# Propagate the parent version to all child modules
( cd "$ROOT_DIR" && mvn -N versions:update-child-modules -DgenerateBackupPoms=false )

# Add a release entry to the flatpak metainfo (stable versions only)
( cd "$ROOT_DIR" && "$SCRIPT_DIR/update-metainfo-release.sh" )