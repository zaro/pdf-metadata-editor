#!/bin/sh

set -x

cd $HOME
git clone https://github.com/zaro/jdde-bundle.git
cd jdde-bundle
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
