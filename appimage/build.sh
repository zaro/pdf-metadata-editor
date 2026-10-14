#!/bin/bash
# Build the release AppImage (and .zsync update metadata) from the linux
# portable tarball produced by the jpackage installer build.
#
# Used by the GitHub Actions build (.github/workflows/maven.yml) and runnable
# locally from the repository root after building the installers:
#
#     appimage/build.sh
#
# The AppDir is assembled so that appimagetool's AppStream validation passes
# (metainfo named after the app id, desktop in both the AppDir root and
# usr/share/applications):
#   AppRun
#   me.broken_by.PdfMetadataEditor.desktop            <- root desktop
#   Pdf Metadata Editor.png                           <- icon referenced by the desktop
#   Pdf Metadata Editor/                              <- the app, extracted from the tarball
#   usr/share/applications/me.broken_by.PdfMetadataEditor.desktop
#   usr/share/metainfo/me.broken_by.PdfMetadataEditor.appdata.xml
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
PACKAGES_DIR="$ROOT_DIR/modules/editor-gui/target/packages"
APP_ID="me.broken_by.PdfMetadataEditor"
APP_NAME="Pdf Metadata Editor"
APPDIR="$PACKAGES_DIR/$APP_NAME.AppDir"
ARCH="$(uname -m)"

if [ ! -d "$PACKAGES_DIR" ]; then
  echo "ERROR: $PACKAGES_DIR does not exist - build the installers first." >&2
  exit 1
fi

cd "$PACKAGES_DIR"

TAR=$(ls pdf-metadata-editor-*-linux-portable.tar.gz 2>/dev/null | head -1 || true)
if [ -z "$TAR" ]; then
  echo "ERROR: no pdf-metadata-editor-*-linux-portable.tar.gz in $PACKAGES_DIR - build the installers first." >&2
  exit 1
fi

# Note: the qualifier may be glued to the version (4.4.1rc1), so match
# everything between the prefix and the architecture marker.
VERSION=$(echo "$TAR" | sed -E 's/^pdf-metadata-editor-(.+)-(amd64|arm64)-linux-portable\.tar\.gz$/\1/')
if [ "$VERSION" = "$TAR" ]; then
  echo "ERROR: cannot parse the version from '$TAR'" >&2
  exit 1
fi

# Assemble the AppDir
rm -rf "$APPDIR"
mkdir -p "$APPDIR/usr/share/applications" "$APPDIR/usr/share/metainfo"
tar -xzf "$TAR" -C "$APPDIR"
cp "$SCRIPT_DIR/AppRun" "$APPDIR/AppRun"
chmod +x "$APPDIR/AppRun"
cp "$SCRIPT_DIR/$APP_ID.desktop" "$APPDIR/$APP_ID.desktop"
cp "$SCRIPT_DIR/$APP_ID.desktop" "$APPDIR/usr/share/applications/$APP_ID.desktop"
cp "$ROOT_DIR/modules/editor-gui/jpackage/pdf-metadata-edit.png" "$APPDIR/$APP_NAME.png"
cp "$ROOT_DIR/flatpak/$APP_ID.metainfo.xml" "$APPDIR/usr/share/metainfo/$APP_ID.appdata.xml"

# appimagetool (continuous, the same binary portable2appimage would fetch)
curl -fsSL -o appimagetool \
  "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-$ARCH.AppImage"
chmod +x appimagetool

export APPIMAGE_EXTRACT_AND_RUN=1
ARCH="$ARCH" VERSION="$VERSION" ./appimagetool \
  -u "gh-releases-zsync|zaro|pdf-metadata-editor|latest|*$ARCH.AppImage.zsync" \
  "$APPDIR"

# Keep only the AppImage and its .zsync in the packages directory
rm -rf "$APPDIR" appimagetool

echo
echo "Built:"
ls -la ./*.AppImage ./*.AppImage.zsync 2>/dev/null
