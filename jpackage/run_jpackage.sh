#!/bin/sh

set -e

TYPE="$1"
if [ -z "$TYPE" ]; then
  echo MUST specify type
  exit 1
fi
echo Building "$TYPE" package
export MSYS_NO_PATHCONV=1 

WINDOWS_UUID="c71564cd-5068-4d6d-874b-6a189abd40d3"
STAGING_DIR="${staging.dir}"
APP_NAME="${project.name}"
DESCRIPTION="${project.description}"
MAIN_JAR="${main.jar.name}"
MAIN_CLASS="pmedit.Main"
APP_VERSION="${project.version}"
ICON_FORMAT="${icon.format}"
DEST_DIR=target/packages
APP_IMAGE_DIR="${DEST_DIR}/${APP_NAME}/"

if [ "$TYPE" = "app-image" ]; then
  rm -rf "${DEST_DIR}/$APP_NAME"  "${DEST_DIR}/$APP_NAME.app"
fi

mkdir -p ${DEST_DIR}

JP_OPTS=""
JP_OPTS="$JP_OPTS --type $TYPE"
JP_OPTS="$JP_OPTS --name '$APP_NAME'"
JP_OPTS="$JP_OPTS --vendor 'CN=Broken By Design, O=Broken By Design OU, C=EE'"
JP_OPTS="$JP_OPTS --icon '${STAGING_DIR}/jpackage-scripts/pdf-metadata-edit.${ICON_FORMAT}'"
JP_OPTS="$JP_OPTS --app-version '$APP_VERSION'"
JP_OPTS="$JP_OPTS --description '$DESCRIPTION'"
JP_OPTS="$JP_OPTS --dest '${STAGING_DIR}/packages'"

if [ "$TYPE" = "app-image" ]; then
  JP_OPTS="$JP_OPTS --input '${STAGING_DIR}/jpackage'"
  JP_OPTS="$JP_OPTS --main-class '$MAIN_CLASS'"
  JP_OPTS="$JP_OPTS --main-jar '$MAIN_JAR'"
  JP_OPTS="$JP_OPTS --add-launcher 'Batch ${APP_NAME}=${STAGING_DIR}/jpackage-scripts/batch-launcher.properties'"
  JP_OPTS="$JP_OPTS --add-launcher 'pmedit-cli=${STAGING_DIR}/jpackage-scripts/cli.properties'"
  JP_OPTS="$JP_OPTS --runtime-image '${STAGING_DIR}/preparedJDK'"
else
  JP_OPTS="$JP_OPTS --app-image '$APP_IMAGE_DIR' --file-associations 'jpackage/file-associations.properties'"
fi

# Remove spaces in app name, because desktop registration fails
if [ "$TYPE" = "deb" -o  "$TYPE" = "rpm" ]; then
  APP_NAME=$(echo $APP_NAME | tr -d " ")
  JP_OPTS="$JP_OPTS --linux-package-name pdf-metadata-editor"
fi

if [ "$TYPE" = "msi" -o  "$TYPE" = "exe" ]; then
  JP_OPTS="$JP_OPTS --win-menu  --win-upgrade-uuid '$WINDOWS_UUID' --win-menu-group '$APP_NAME'"

  # Allow some customization options for the exe
  if [ "$TYPE" = "exe" ]; then
    JP_OPTS="$JP_OPTS --win-dir-chooser --win-per-user-install"
  fi
fi


set -x
eval jpackage $JP_OPTS
set +x

ls -lah ${STAGING_DIR}/packages/

### Sign Windows deliveries
signtool_file() {
  # Based on https://simplefury.com/posts/java/windows/jpackage-win-codesign/
  DESC=$1
  FILE=$2
  echo ">>> Signing '$FILE' with signtool"
  chmod a+w "$FILE"
  signtool.exe  sign /f jpackage/cert/win-cert.pfx /d "$DESC" /p 123456 /v /fd SHA256 /sha1 7A5F1BDE4221B11C8EB94EDAD77B9217A5F74C59 /tr "http://timestamp.sectigo.com" /td SHA256 "$FILE"

}

if [ "$TYPE" = "app-image" -a "$(which signtool)" ]; then
  OIFS="$IFS"
  IFS=$'\n'
  for file in  $(find "${APP_IMAGE_DIR}" -type f -name "*.exe"); do 
    signtool_file "$APP_NAME" "$file"
  done
  IFS="$OIFS"
fi

if [ "$TYPE" = "msi" ]; then
  file=$(ls -1 "${STAGING_DIR}/packages/"*.msi)
  signtool_file "$APP_NAME MSI installer" "$file"
fi

if [ "$TYPE" = "exe" ]; then
  file=$(ls -1 "${STAGING_DIR}/packages/"*.exe)
  signtool_file "$APP_NAME EXE installer" "$file"
fi

### Sign macOS deliveries
if [ "$TYPE" = "dmg" ]; then
  codesign -s - -f "${STAGING_DIR}/packages/${APP_NAME}-${APP_VERSION}.dmg"
fi
