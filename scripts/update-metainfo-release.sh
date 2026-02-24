#!/bin/bash
set -e

METAINFO="flatpak/me.broken_by.PdfMetadataEditor.metainfo.xml"
POM_FILE="pom.xml"

VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" "$POM_FILE")

if echo "$VERSION" | grep -qi 'SNAPSHOT'; then
    echo "Version $VERSION is not stable (contains SNAPSHOT), skipping metainfo update"
    exit 0
fi

if echo "$VERSION" | grep -qiE '.*-(alpha|beta|rc|cr|a|b)[0-9]*'; then
    echo "Version $VERSION is not stable (has qualifier), skipping metainfo update"
    exit 0
fi

if grep -q "version=\"$VERSION\"" "$METAINFO"; then
    echo "Version $VERSION already exists in $METAINFO, skipping"
    exit 0
fi

DATE=$(date +%Y-%m-%d)

sed -i "s|<releases>|<releases>\n    <release version=\"$VERSION\" date=\"$DATE\" type=\"stable\" />|" "$METAINFO"

echo "Added release $VERSION ($DATE) to $METAINFO"
