#!/bin/sh

set -e

TYPE="$1"
if [ -z "$TYPE" ]; then
  echo MUST specify type
  exit 1
fi
echo Building "$TYPE" package
echo System is $(uname -a)
unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=linux;;
    Darwin*)    machine=mac;;
    CYGWIN*)    machine=win;;
    MINGW*)     machine=win;;
    *)          machine="UNKNOWN:${unameOut}"
esac
echo Machine type detected ${machine}


# Make sure /usr/bin/ is first in the path, bcause of stupid cicrle CI setup
export PATH=/usr/bin:$PATH

# Workaround stupid circle ci java docker setup
JAVA_LINK=$(readlink /usr/bin/java || true)
if [ "$JAVA_LINK" ]; then
  JDK_BIN=`dirname $JAVA_LINK`
  echo Prepending $JDK_BIN to PATH
  export PATH=$JDK_BIN:$PATH
fi

export MSYS_NO_PATHCONV=1

WINDOWS_UUID="${windows.uuid}"
STAGING_DIR="${staging.dir}"
APP_NAME="${project.name}"
DESCRIPTION="${project.description}"
MAIN_JAR="${main.jar.name}"
MAIN_CLASS="pmedit.Main"
APP_VERSION="${project.version}"
ICON_FORMAT="${icon.format}"
DEST_DIR="${STAGING_DIR}/packages"
DEST_IMAGE_DIR="${STAGING_DIR}/packages-image"
APP_IMAGE_DIR="${DEST_IMAGE_DIR}/${APP_NAME}/"

if [ "$TYPE" = "app-image" ]; then
  rm -rf "${DEST_IMAGE_DIR}"  "${DEST_DIR}/$APP_NAME.app"
fi

mkdir -p ${DEST_DIR} ${DEST_IMAGE_DIR}

JP_OPTS="--verbose"
JP_OPTS="$JP_OPTS --type $TYPE"
JP_OPTS="$JP_OPTS --name '$APP_NAME'"
JP_OPTS="$JP_OPTS --vendor 'broken-by.me'"
JP_OPTS="$JP_OPTS --icon '${STAGING_DIR}/jpackage-scripts/pdf-metadata-edit.${ICON_FORMAT}'"
JP_OPTS="$JP_OPTS --app-version '$APP_VERSION'"
JP_OPTS="$JP_OPTS --description '$DESCRIPTION'"

if [ "$TYPE" = "app-image" -o "$machine" = "mac" ]; then
  JP_OPTS="$JP_OPTS --input '${STAGING_DIR}/jpackage'"
  JP_OPTS="$JP_OPTS --main-class '$MAIN_CLASS'"
  JP_OPTS="$JP_OPTS --java-options '--add-opens java.base/java.nio=ALL-UNNAMED --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED'"
  JP_OPTS="$JP_OPTS --main-jar '$MAIN_JAR'"
  JP_OPTS="$JP_OPTS --add-launcher 'Batch ${APP_NAME}=${STAGING_DIR}/jpackage-scripts/batch-launcher.properties'"
  JP_OPTS="$JP_OPTS --add-launcher 'pmedit-cli=${STAGING_DIR}/jpackage-scripts/cli.properties'"
  JP_OPTS="$JP_OPTS --runtime-image '${STAGING_DIR}/preparedJDK'"
  JP_OPTS="$JP_OPTS --dest '${DEST_IMAGE_DIR}'"
fi

if [ "$TYPE" != "app-image" -a "$machine" != "mac" ]; then
  JP_OPTS="$JP_OPTS --file-associations 'jpackage/file-associations.properties'"
  JP_OPTS="$JP_OPTS --dest '${DEST_DIR}'"
  JP_OPTS="$JP_OPTS --app-image '$APP_IMAGE_DIR' "
fi

if [ "$TYPE" != "app-image" -a "$machine" = "mac" ]; then
  JP_OPTS="$JP_OPTS --file-associations 'jpackage/file-associations.properties'"
  JP_OPTS="$JP_OPTS --dest '${DEST_DIR}'"
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

ls -la ${STAGING_DIR}/packages/
ls -la ${DEST_IMAGE_DIR}


#if [ "$machine" = "mac" -a "$TYPE" = "app-image" ]; then
#  (cd ${DEST_IMAGE_DIR}; zip -r "../packages/PdfMetadataEditor.app.zip" Pdf\ Metadata\ Editor.app/)
#  zip -r "${STAGING_DIR}/packages/$APP_NAME.app.zip" target/packages/
#fi

set +x
### Handle linux deliveries
if [ "${machine}" = "linux" ]; then
  if [ "$TYPE" = "app-image" ]; then
    # Copy the uberjar as release package
    cp -v "${STAGING_DIR}/jpackage/${MAIN_JAR}" target/packages/
  fi
fi

### Sign Windows deliveries
if [ "${machine}" = "win" ]; then

  # try to detech signtool installation and add it to path
  SIGNTOOL_PFX=jpackage/cert/win-cert.pfx

  if [ -d '/c/WinKit/bin/' ]; then
    SIGNTOOL=$(ls -d1 '/c/Program Files (x86)/Windows Kits/10/bin/'*/x64/signtool.exe | sort | tail -n 1)
  elif [ -d '/c/Program Files (x86)/Windows Kits/' ]; then
    SIGNTOOL=$(ls -d1 '/c/Program Files (x86)/Windows Kits/10/bin/'*/x64/signtool.exe | sort | tail -n 1)
  else
    SIGNTOOL=$(which signtool)
  fi

  if [ -z "$CERTUM_SHA" ]; then
    if [ ! -f "$SIGNTOOL_PFX" ]; then
      echo "$SIGNTOOL_PFX" not found, trying to create it from SIGNTOOL_CERT env
      if [ "$SIGNTOOL_CERT" ]; then
        echo "$SIGNTOOL_CERT" | base64 -d > "$SIGNTOOL_PFX"
      else
        echo "SIGNTOOL_CERT not set"
      fi
    fi
  fi

  ls -lah jpackage/cert

  signtool_file() {
    # Based on https://simplefury.com/posts/java/windows/jpackage-win-codesign/
    DESC=$1
    FILE=$2
    echo ">>> Signing '$FILE' with signtool"
    if [ -z "${SIGNTOOL}" ];  then
       echo "!!!!!!!!! SKIP: no SIGNTOOL found"
    fi
    chmod a+w "$FILE"
    file "$FILE"
    if [ -z "$CERTUM_SHA" ]; then
      set -x
      "${SIGNTOOL}"  sign /f jpackage/cert/win-cert.pfx /p 123456 /d "$DESC" /v /fd SHA256 /tr "http://timestamp.sectigo.com" /td SHA256 "$FILE"
      set +x
    else
      set -x
      "${SIGNTOOL}"  sign /sha1 "$CERTUM_SHA" /tr http://time.certum.pl /td sha256 /fd sha256 /v "$FILE"
      set +x
    fi
  }

  if [ "$TYPE" = "app-image" -a "${SIGNTOOL}" ]; then
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

fi

### Sign macOS deliveries
# if [ "$TYPE" = "dmg" ]; then
#   codesign -s - -f "${STAGING_DIR}/packages/${APP_NAME}-${APP_VERSION}.dmg"
# fi
