#!/bin/sh
# Gradle start-up script for POSIX systems (Linux/macOS).
# Eksik olan Unix wrapper'ı geri yükler; gradlew.bat ile aynı jar'ı kullanır.
set -e

APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=$(command -v java || true)
fi

if [ -z "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME ayarlanmadi veya 'java' bulunamadi." >&2
    exit 1
fi

exec "$JAVACMD" $JAVA_OPTS -classpath "$CLASSPATH" \
    -Dorg.gradle.appname=gradlew \
    org.gradle.wrapper.GradleWrapperMain "$@"
