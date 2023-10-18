#!/bin/sh

set -e

echo '--------------------------------'
java -version
echo '--------------------------------'
mvn -version
echo '--------------------------------'

echo "build and install"

mvn test

echo "Done"
