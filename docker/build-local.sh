#!/usr/bin/env bash

set -euxo pipefail

SOURCE_DIR=".."

# Retrieve the script directory.
SCRIPT_DIR="${BASH_SOURCE%/*}"
cd ${SCRIPT_DIR}

# Move to the root directory to run gradle for current version.
pushd ${SOURCE_DIR}
YANAGISHIMA_VERSION=$(./gradlew properties -q | grep "version:" | awk '{print $2}')
popd

WORK_DIR="$(mktemp -d)"
cp ${SOURCE_DIR}/build/distributions/yanagishima-${YANAGISHIMA_VERSION}.zip ${WORK_DIR}
unzip -q ${WORK_DIR}/yanagishima-${YANAGISHIMA_VERSION}.zip -d ${WORK_DIR}
rm ${WORK_DIR}/yanagishima-${YANAGISHIMA_VERSION}.zip

CONTAINER="yanagishima:${YANAGISHIMA_VERSION}"

docker build ${WORK_DIR} --pull -f Dockerfile -t ${CONTAINER} --build-arg "YANAGISHIMA_VERSION=${YANAGISHIMA_VERSION}"

rm -r ${WORK_DIR}
