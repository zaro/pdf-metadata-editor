#!/bin/bash

set -e

# Make sure /usr/bin/ is first in the path, bcause of stupid cicrle CI setup
export PATH=/usr/bin:$PATH

THIS_DIR=$(cd `dirname $0`; pwd)

PKG_LIST="$1"
if [ -z "$PKG_LIST" ]; then
  echo MUST specify package list to build
  exit 1
fi

if [ "$2" = "prebuilt-app-image" ]; then
  PREBUILD_APP_IMAGE=1
fi

for pkg in ${PKG_LIST//,/ }; do
    if [ "$pkg" = "app-image" -a "$PREBUILD_APP_IMAGE" ]; then
      echo "****** Not building $pkg ******"
      continue
    fi
    echo "****** Building $pkg ******"
    bash $THIS_DIR/run_jpackage.sh $pkg
    echo "****** Done building $pkg ******"
done