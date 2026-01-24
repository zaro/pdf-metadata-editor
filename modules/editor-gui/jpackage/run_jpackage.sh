#!/bin/sh

set -e

TYPE="$1"
if [ -z "$TYPE" ]; then
  echo MUST specify type
  exit 1
fi

echo Building "$TYPE" package
echo System is $(uname -a)
echo Current PWD="${PWD}"

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=linux;;
    Darwin*)    machine=mac;;
    CYGWIN*)    machine=win;;
    MINGW*)     machine=win;;
    MSYS_NT*)   machine=win;;
    *)          machine="UNKNOWN:${unameOut}"
esac
echo Machine type detected ${machine}


# Make sure /usr/bin/ is first in the path, bcause of stupid cicrle CI setup
export PATH=/usr/bin:$PATH

## Workaround stupid circle ci java docker setup
#JAVA_LINK=$(readlink /usr/bin/java || true)
#if [ "$JAVA_LINK" ]; then
#  JDK_BIN=`dirname $JAVA_LINK`
#  echo Prepending $JDK_BIN to PATH
#  export PATH=$JDK_BIN:$PATH
#fi

export MSYS_NO_PATHCONV=1

FULL_APP_VERSION="${project.version}"

WINDOWS_UUID="${windows.uuid}"
STAGING_DIR="${staging.dir}"
APP_NAME="${project.name}"
DESCRIPTION="${project.description}"
MAIN_JAR="${main.jar.name}"
MAIN_CLASS="pmedit.Main"
ICON_FORMAT="${icon.format}"
DEST_DIR="${STAGING_DIR}/packages"
DEST_IMAGE_DIR="${STAGING_DIR}/packages-image"
APP_IMAGE_DIR="${DEST_IMAGE_DIR}/${APP_NAME}/"
BUILD_ARCH="${cpu.arch}"
EXTENSIONS_DIR="${extensionsDir}"

APP_VERSION="${FULL_APP_VERSION}"

# Remove beta/rc qualifiers as they cannot be used on windows/macos
if [ "${machine}" = "win" -o "$machine" = "mac" ]; then
  PRE_TAG="${APP_VERSION##[0-9].[0-9].[0-9]}"
  PRE_TYPE=${PRE_TAG%[0-9]*}
  PRE_VERSION=${PRE_TAG##*[!0-9]}

  APP_VERSION="${APP_VERSION%rc[0-9]*}"
  APP_VERSION="${APP_VERSION%beta[0-9]*}"

  if [ "${machine}" = "win" ]; then
    if [ "${PRE_TAG}" ]; then
      echo '>>> Adjusting version for prerelease for Windows'
      echo ">>> PRE_TAG=${PRE_TAG} PRE_TYPE=${PRE_TYPE} PRE_VERSION=${PRE_VERSION}"

      if [ -z "${PRE_VERSION}" ]; then
        PRE_VERSION=1
      fi
      if [ "${PRE_TYPE}" = "beta" ]; then
        PRE_VERSION=$(expr $PRE_VERSION + 100)
      fi
      if [ "${PRE_TYPE}" = "rc" ]; then
        PRE_VERSION=$(expr $PRE_VERSION + 200)
      fi
      APP_VERSION="${APP_VERSION}.${PRE_VERSION}"
    else
        APP_VERSION="${APP_VERSION}.1000"
    fi
  fi

fi

if [ "$TYPE" = "app-image" ]; then
  rm -rf "${DEST_IMAGE_DIR}"  "${DEST_DIR}/$APP_NAME.app"
fi

mkdir -p ${DEST_DIR} ${DEST_IMAGE_DIR}

JP_OPTS="--verbose"
JP_OPTS="$JP_OPTS --type $TYPE"
JP_OPTS="$JP_OPTS --name '$APP_NAME'"
JP_OPTS="$JP_OPTS --vendor 'pdf.metadata.care'"
JP_OPTS="$JP_OPTS --icon '${STAGING_DIR}/jpackage-scripts/pdf-metadata-edit.${ICON_FORMAT}'"
JP_OPTS="$JP_OPTS --app-version '$APP_VERSION'"
JP_OPTS="$JP_OPTS --description '$DESCRIPTION'"

if [ "$TYPE" = "app-image" -o "$machine" = "mac" ]; then
  JP_OPTS="$JP_OPTS --input '${STAGING_DIR}/jpackage'"
  JP_OPTS="$JP_OPTS --main-class '$MAIN_CLASS'"
  JP_OPTS="$JP_OPTS --java-options '--add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED --enable-native-access=ALL-UNNAMED'"
  JP_OPTS="$JP_OPTS --main-jar '$MAIN_JAR'"
  JP_OPTS="$JP_OPTS --add-launcher 'Batch ${APP_NAME}=${STAGING_DIR}/jpackage-scripts/batch-launcher.properties'"
  JP_OPTS="$JP_OPTS --add-launcher 'pmedit-cli=${STAGING_DIR}/jpackage-scripts/cli.properties'"
  JP_OPTS="$JP_OPTS --runtime-image '${STAGING_DIR}/preparedJDK'"
  JP_OPTS="$JP_OPTS --dest '${DEST_IMAGE_DIR}'"

  # Add extensions to input dir if needed
  if [ "${EXTENSIONS_DIR}" ]; then
    find "${EXTENSIONS_DIR}" -name 'pme-*-plugin*' -exec cp -v {} "${STAGING_DIR}/jpackage" \;
  fi
fi

if [ "$TYPE" != "app-image" -a "$machine" != "mac" ]; then
  JP_OPTS="$JP_OPTS --file-associations '${STAGING_DIR}/jpackage-scripts/file-associations.properties'"
  JP_OPTS="$JP_OPTS --dest '${DEST_DIR}'"
  JP_OPTS="$JP_OPTS --app-image '$APP_IMAGE_DIR' "
fi

if [ "$TYPE" != "app-image" -a "$machine" = "mac" ]; then
  JP_OPTS="$JP_OPTS --file-associations '${STAGING_DIR}/jpackage-scripts/file-associations.properties'"
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

  echo ">>> Checking wix toolset"
  if type wix; then
    echo ">>> Detected wix toolset 4+"
    wix --version
    export WIX_EXTENSIONS=$(cygpath -w "$PWD")
    if ! wix extension list --global; then
        echo "!!! failed to list wix global extensions"
    fi
    if ! wix extension list ; then
        echo "!!! failed to list wixS extensions"
    fi
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


### Handle linux deliveries
if [ "${machine}" = "linux" ]; then
  if [ "$TYPE" = "app-image" ]; then
    echo Copy the uberjar as release package
    cp -v "${STAGING_DIR}/jpackage/${MAIN_JAR}" target/packages/
  fi
fi

### Sign Windows deliveries
echo "EXTERNAL_SIGNING=${EXTERNAL_SIGNING}"
set +x
if [ "${machine}" = "win" -a "${EXTERNAL_SIGNING}" != "yes" ]; then

  # try to detect signtool installation and add it to path
  SIGNTOOL_PFX=jpackage/cert/win-cert.pfx

  if [ -d '/c/WinKit/bin/' ]; then
    SIGNTOOL=$(ls -d1 '/c/Program Files (x86)/Windows Kits/10/bin/'*/x64/signtool.exe | sort | tail -n 1)
  elif [ -d '/c/Program Files (x86)/Windows Kits/' ]; then
    SIGNTOOL=$(ls -d1 '/c/Program Files (x86)/Windows Kits/10/bin/'*/x64/signtool.exe | sort | tail -n 1)
  else
    SIGNTOOL=$(which signtool)
  fi
  echo Using SIGNTOOL=$SIGNTOOL

  if [ -z "$CERTUM_SHA" ]; then
    if [ ! -f "$SIGNTOOL_PFX" ]; then
      echo "$SIGNTOOL_PFX" not found, trying to create it from SIGNTOOL_CERT env
      if [ "$SIGNTOOL_CERT" ]; then
        echo "$SIGNTOOL_CERT" | base64 -d > "$SIGNTOOL_PFX"
      else
        echo "SIGNTOOL_CERT not set"
      fi
    fi
  else
    echo "Using CERTUM_SHA=$CERTUM_SHA"
  fi

  ls -la jpackage/cert

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

# Rename files in case of pre-relase
if [ "${machine}" = "win" -o "$machine" = "mac" ]; then

  if [ "${machine}" = "win" ]; then
    FULL_VERSION_WITH_ARCH="${FULL_APP_VERSION}-${BUILD_ARCH}"
  else
    FULL_VERSION_WITH_ARCH="${FULL_APP_VERSION}"
  fi

  if [ "${APP_VERSION}" != "${FULL_VERSION_WITH_ARCH}" ]; then
    D="${STAGING_DIR}/packages/"

    OIFS="$IFS"
    IFS=$'\n'
    for file in  $(find "${D}" -type f); do
      ofile="${file//$APP_VERSION/$FULL_VERSION_WITH_ARCH}"
      mv -v "$file" "$ofile"
    done
    IFS="$OIFS"
  fi

fi

### Sign macOS deliveries
# if [ "$TYPE" = "dmg" ]; then
#   codesign -s - -f "${STAGING_DIR}/packages/${APP_NAME}-${APP_VERSION}.dmg"
# fi
