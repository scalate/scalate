#!/bin/sh
mkdir ~/.ivy2 2> /dev/null
java -Xmx512M -XX:MaxPermSize=350m -jar `dirname $0`/sbt-launch.jar "$@"
