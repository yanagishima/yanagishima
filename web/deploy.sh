#!/bin/bash -eu

npm install
npm run build
mkdir -p dist/share
mkdir -p dist/error
mkdir -p dist/diff
cp dist/index.html dist/share
cp dist/index.html dist/error
cp dist/index.html dist/diff
