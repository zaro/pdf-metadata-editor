#!/bin/sh

set -e

echo '--------------------------------'
java -version
echo '--------------------------------'
mvn -version
echo '--------------------------------'

echo "build and install"

mvn -DskipTests=true clean package

echo "Done"
