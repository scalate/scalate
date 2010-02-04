#!/bin/sh
mkdir ~/.ivy2 2> /dev/null
java -Xmx256M -jar `dirname $0`/sbt-launch.jar "$@"
