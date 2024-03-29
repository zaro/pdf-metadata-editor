#!/bin/sh

if [ "$TRAVIS_OS_NAME" = "windows" ]; then
  export TOOLS_PATH=/c/Users/travis
else
  export TOOLS_PATH=~/travis
fi
mkdir -p TOOLS_PATH

if [ "$TRAVIS_OS_NAME" = "linux" ]; then
  sudo apt-get update
  sudo apt -y install rpm fakeroot p7zip-full
fi

if [ "$TRAVIS_OS_NAME" = "osx" ]; then
  export HOMEBREW_NO_INSTALL_CLEANUP=1
  export HOMEBREW_NO_AUTO_UPDATE=1
  brew install p7zip
fi

if [ "$TRAVIS_OS_NAME" = "windows" ]; then
  pushd ${TOOLS_PATH}

  echo ==============================================
  echo Install WiX
  echo ==============================================
  WIX_UTL="https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip"
  export WIX_HOME=$TOOLS_PATH/wix

  echo "Downloading $WIX_UTL..."
  curl -fsS -Lo wix.zip "$WIX_UTL"
  rm -rf ${WIX_HOME}

  echo "Extracting wix.zip..."
  7z x wix.zip -y -o${WIX_HOME}/

  export PATH=${WIX_HOME}:$PATH

  popd
fi

# Install JDK 14
if [ "$TRAVIS_OS_NAME" = "windows" ]; then

  pushd ${TOOLS_PATH}
  
  export JAVA_VERSION=17
  echo ==============================================
  echo Install JDK ${JAVA_VERSION}
  echo ==============================================
  JAVA_URL="https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip"
  export JAVA_HOME=${JAVA_HOME:-$TOOLS_PATH/jdk}

  echo "Downloading $JAVA_URL..."
  curl -fsS -o openjdk.zip "$JAVA_URL"
  rm -rf ${JAVA_HOME}

  echo "Extracting openjdk.zip..."
  7z x openjdk.zip -y -o${TOOLS_PATH}/
  mv ${TOOLS_PATH}/jdk-${JAVA_VERSION} ${JAVA_HOME}

  export PATH=${JAVA_HOME}/bin:$PATH
  java -version

  popd
else
  curl --create-dirs -Lo ~/bin/install-jdk.sh https://github.com/sormuras/bach/raw/master/install-jdk.sh
  export JAVA_HOME=~/openjdk14
  export PATH="$JAVA_HOME/bin:$PATH"
  source ~/bin/install-jdk.sh --feature "17"  --cacerts

  # On macos  the feature release has symlink instead of cacrts file, replace with the current one
  if [ "$TRAVIS_OS_NAME" = "osx" ]; then
    CA_LINK=`readlink "$JAVA_HOME/lib/security/cacerts"`
    if [ -n "$CA_LINK" ]; then
      OLD_JAVA_HOME=$(/usr/libexec/java_home)
      echo Preinstalled Java Home: $OLD_JAVA_HOME

      if [ -n "$OLD_JAVA_HOME" ]; then
        rm -v $JAVA_HOME/lib/security/cacerts
        cp -v "$OLD_JAVA_HOME/lib/security/cacerts" "$JAVA_HOME/lib/security/"
      fi
    fi
    ls -lah $JAVA_HOME/lib/security/
  fi

fi

echo ==============================================
echo Install maven
echo ==============================================
MVN_VERSION=3.8.1
MVN_URL="https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/apache-maven-${MVN_VERSION}-bin.zip"
export MVN_HOME=$TOOLS_PATH/mvn
echo "Downloading $MVN_URL..."
curl -fsS -o maven.zip "$MVN_URL"
rm -rf ${MVN_HOME}

echo "Extracting maven.zip..."
7z x maven.zip -y -o${TOOLS_PATH}/
mv ${TOOLS_PATH}/apache-maven-${MVN_VERSION} ${MVN_HOME}

export PATH=${MVN_HOME}/bin:$PATH
mvn -version
