#!/bin/sh
mkdir ~/.ivy2 2> /dev/null
java -Xmx256M -XX:MaxPermSize=250m -jar `dirname $0`/sbt-launch.jar "$@"
