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

for pkg in ${PKG_LIST//,/ }; do
    echo "*** Building $pkg ***"
    $THIS_DIR/run_jpackage.sh $pkg
done