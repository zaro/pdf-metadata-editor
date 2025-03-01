#!/bin/bash

if [ -z "$PUBLISH_VERSION" ]; then
  echo env PUBLISH_VERSION must be set when using this script
  exit 1
fi
declare -i length current

UPLOAD_FILES=(
  "target/packages/Pdf Metadata Editor-${PUBLISH_VERSION}.msi"
  "target/packages/Pdf Metadata Editor-${PUBLISH_VERSION}.exe"
  "target/packages/Pdf Metadata Editor-${PUBLISH_VERSION}.zip"
)
REL_JSON=target/release.json
length=${#UPLOAD_FILES[@]}
current=0

echo "{" > ${REL_JSON}
echo "  \"files\": [" >> ${REL_JSON}
for f in "${UPLOAD_FILES[@]}"; do
  file=$(basename "$f")
  target="pmc/pdf-metadata-editor/release-files/$PUBLISH_VERSION/${file}"
  if mc stat -q "$target" > /dev/null ; then
    echo "$target" already exists, skipping
  else
    echo Uploading "$f" as "$target"
    mc cp "$f" "$target"
  fi

  current=$((current + 1))
  if [[ "$current" -eq "$length" ]]; then
     echo "    \"${file}\"" >> ${REL_JSON}
     echo "  ]" >> ${REL_JSON}
  else
     echo "    \"${file}\"," >> ${REL_JSON}
  fi
done
echo "}" >> ${REL_JSON}


