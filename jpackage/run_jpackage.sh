#!/bin/sh

THIS_DIR=$(cd `dirname $0`;pwd)

TYPE="$1"
if [ -z "$TYPE" ]; then
  echo MUST specify type
  exit 1
fi

STAGING_DIR="${staging.dir}"
NAME="${project.name}"
DESCRIPTION="${project.description}"
MAIN_JAR="${main.jar.name}"
MAIN_CLASS="pmedit.Main"
APP_VERSION="${project.version}"
ICON_FORMAT="${icon.format}"
DEST_DIR=target/packages

rm -vr "${DEST_DIR}/$NAME"  "${DEST_DIR}/$NAME.app"


if [ "$TYPE" = "app-image" ]; then
  FILE_ASSOCIATION=""
else
  FILE_ASSOCIATION="--file-associations jpackage/file-associations.properties"
fi

set -x
exec jpackage \
  --type \
  $TYPE \
  --input ${STAGING_DIR}/jpackage \
  --name "$NAME" \
  --main-class "$MAIN_CLASS" \
  --main-jar "$MAIN_JAR" \
  --icon "$THIS_DIR/pdf-metadata-edit.${ICON_FORMAT}" \
  --app-version "$APP_VERSION" \
  --description "$DESCRIPTION" \
  --add-launcher "Batch ${NAME}=$THIS_DIR/batch-launcher.properties" \
  --add-launcher "pmedit-cli=$THIS_DIR/cli.properties" \
  ${FILE_ASSOCIATION} \
  --dest ${STAGING_DIR}/packages
