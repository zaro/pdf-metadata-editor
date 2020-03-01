#!/bin/sh

#set -x

cd $HOME
echo "Clonning jdde"
git clone https://github.com/zaro/jdde-bundle.git


echo "cd jdde"
cd jdde-bundle

echo "build and install"
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
