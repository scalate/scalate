#!/bin/sh
java -Xmx256M -jar `dirname $0`/sbt-launch.jar "$@"
