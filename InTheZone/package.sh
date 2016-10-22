#!/bin/bash
git pull --tags
./gradlew clean packageNative
