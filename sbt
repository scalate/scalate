#!/bin/sh
java -Xmx256M -XX:MaxPermSize=250m -jar `dirname $0`/sbt-launch.jar "$@"
