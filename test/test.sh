#!/bin/bash

echo -e "\nbuilding & uploading to local maven repo"
echo -e "==============================================="
gradle clean build uploadArchives
echo -e "==============================================="

TEST_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="/tmp/sample_poject_`date +%s%3N`"

echo -e "\ncopying sample_project to ${PROJECT_DIR}"
echo -e "==============================================="
cp -r "${TEST_DIR}/sample_project" "${PROJECT_DIR}"
echo -e "==============================================="

echo -e "\nrunning gradle build in ${PROJECT_DIR}"
echo -e "==============================================="
gradle -b "${PROJECT_DIR}/build.gradle" build $*
echo -e "==============================================="
