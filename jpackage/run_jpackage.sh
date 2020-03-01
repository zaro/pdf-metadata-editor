#!/bin/sh

TYPE="$1"
if [ -z "$TYPE" ]; then
  echo MUST specify type
  exit 1
fi

WINDOWS_UUID="c71564cd-5068-4d6d-874b-6a189abd40d3"
STAGING_DIR="${staging.dir}"
APP_NAME="${project.name}"
APP_NAME="${project.name}"
DESCRIPTION="${project.description}"
MAIN_JAR="${main.jar.name}"
MAIN_CLASS="pmedit.Main"
APP_VERSION="${project.version}"
ICON_FORMAT="${icon.format}"
DEST_DIR=target/packages

rm -vr "${DEST_DIR}/$APP_NAME"  "${DEST_DIR}/$APP_NAME.app"

JP_OPTS=""
JP_OPTS="$JP_OPTS --type $TYPE"
JP_OPTS="$JP_OPTS --input '${STAGING_DIR}/jpackage'"
JP_OPTS="$JP_OPTS --name '$APP_NAME'"
JP_OPTS="$JP_OPTS --main-class '$MAIN_CLASS'"
JP_OPTS="$JP_OPTS --main-jar '$MAIN_JAR'"
JP_OPTS="$JP_OPTS --icon '${STAGING_DIR}/jpackage-scripts/pdf-metadata-edit.${ICON_FORMAT}'"
JP_OPTS="$JP_OPTS --app-version '$APP_VERSION'"
JP_OPTS="$JP_OPTS --description '$DESCRIPTION'"
JP_OPTS="$JP_OPTS --add-launcher 'Batch ${APP_NAME}=${STAGING_DIR}/jpackage-scripts/batch-launcher.properties'"
JP_OPTS="$JP_OPTS --add-launcher 'pmedit-cli=${STAGING_DIR}/jpackage-scripts/cli.properties'"
JP_OPTS="$JP_OPTS --dest '${STAGING_DIR}/packages'"

if [ "$TYPE" = "app-image" ]; then
  true
else
  JP_OPTS="$JP_OPTS --file-associations 'jpackage/file-associations.properties'"
fi

# Remove spaces in app name, because desktop registration fails
if [ "$TYPE" = "deb" -o  "$TYPE" = "rpm" ]; then
  APP_NAME=$(echo $APP_NAME | tr -d " ")
  JP_OPTS="$JP_OPTS --linux-package-name pdf-metadata-editor"
fi

if [ "$TYPE" = "msi" -o  "$TYPE" = "exe" ]; then
  JP_OPTS="$JP_OPTS --win-dir-chooser --win-menu  --win-upgrade-uuid '$WINDOWS_UUID' --win-per-user-install --win-menu-group '$APP_NAME'"
fi

set -x
eval jpackage $JP_OPTS