#!/bin/sh
#
# Gradle start up script for UN*X
#
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
APP_HOME=`pwd -P`
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Fix (quality-p0): the old script ran `exec java $DEFAULT_JVM_OPTS ...`, which
# passes the literal string "-Xmx64m" (quotes included) as the main class, so
# `./gradlew --version` fails with: Could not find or load main class "-Xmx64m".
# Expand the quoted JVM opts in a command substitution so the user's "$@" stays
# intact, then word-split them (they contain no spaces) for the java command.
JVM_OPTS=$(eval "echo $DEFAULT_JVM_OPTS")
# shellcheck disable=SC2086
exec java $JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
