#!/bin/sh

#set -x

echo '--------------------------------'
java -version
echo '--------------------------------'
mvn -version
echo '--------------------------------'

cd $HOME
echo "Clonning jdde"
git clone --depth 1 --branch jdk14 https://github.com/zaro/jdde-bundle.git


echo "cd jdde"

cd jdde-bundle

echo "build and install"

mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

echo "Done"
