#!/usr/bin/env sh
# Minimal Gradle wrapper bootstrap script.
# If gradle-wrapper.jar is missing, install Gradle locally and run: gradle wrapper

DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
PROPS="$DIR/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "gradle-wrapper.jar is missing."
  echo "Install Gradle locally and run: gradle wrapper"
  exit 1
fi

exec java -jar "$WRAPPER_JAR" "$@"
