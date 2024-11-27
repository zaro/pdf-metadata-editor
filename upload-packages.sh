#!/bin/bash

if [ -z "$PUBLISH_VERSION" ]; then
  echo env PUBLISH_VERSION must be set when using this script
  exit 1
fi

UPLOAD_FILES=("target/packages/Pdf Metadata Editor-${PUBLISH_VERSION}.msi")

for f in "${UPLOAD_FILES[@]}"; do
  extension="${f##*.}"
  target="pmc/pdf-metadata-editor/release-files/$PUBLISH_VERSION/release.${extension}"
  if mc stat -q $target > /dev/null ; then
    echo $target already exists, skipping
  else
    echo Uploading $f as $target
    mc cp "$f" "$target"
  fi
done
