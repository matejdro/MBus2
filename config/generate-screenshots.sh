#!/bin/bash

set -e
# Copy current snapshots to a temp folder (ignore if there is no existing folder)
cp -R app-screenshot-tests/src/test/snapshots/ tmpSnapshots || mkdir tmpSnapshots

# Regenerate snapshots
./gradlew recordPaparazziDebug

# Copy previous snapshots back to ensure we only create new snapshot, not delete old ones
cp -R tmpSnapshots/. app-screenshot-tests/src/test/snapshots/
rm -rf ./tmpSnapshots

git add app-screenshot-tests/src/test/snapshots

if [[ -n $(git status --porcelain=v1 | grep "^[A|M|D|R]") ]]
then
  git config --global user.email "ci@github.com"
  git config --global user.name "Build Bot"
  git commit -m "chore: add new screenshot tests"
  git push origin HEAD:$1
fi
