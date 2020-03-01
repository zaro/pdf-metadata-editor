#!/bin/sh

TYPE="$1"
if [ -z "$TYPE" ]; then
  echo MUST specify type
fi

NAME="Pdf Metadata Editor"
DESCRIPTION="Simple Editor for PDF metadata"
MAIN_JAR="pdf-metadata-editor-3.0.0.jar"
MAIN_CLASS="pmedit.Main"
APP_VERSION="3.0.0"
DEST_DIR=target/packages


if [ "$TYPE" = "app-image" ]; then
  FILE_ASSOCIATION=""
  rm -r "${DEST_DIR}/$NAME"
else
  FILE_ASSOCIATION="--file-associations jpackage/file-associations.properties"
fi

set -x
exec jpackage \
  --type \
  $TYPE \
  --input target/jpackage \
  --name "$NAME" \
  --main-class "$MAIN_CLASS" \
  --main-jar "$MAIN_JAR" \
  --icon jpackage/pdf-metadata-edit.png \
  --app-version "$APP_VERSION" \
  --description "$DESCRIPTION" \
  --add-launcher "Batch ${NAME}=jpackage/batch-launcher.properties" \
  --add-launcher "pmedit-cli=jpackage/cli.properties" \
  ${FILE_ASSOCIATION} \
  --dest target/packages
