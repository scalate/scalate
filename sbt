#!/bin/sh
mkdir ~/.ivy2 2> /dev/null
java -Xmx712M -XX:MaxPermSize=350m -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar 'project scalate-core' shell "$@"
