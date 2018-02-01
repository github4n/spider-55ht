#!/bin/sh

APP="spider-55haitao-crawler.jar"

nohup java -XX:+DisableExplicitGC -Xms512m -Xmx512m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:./gc.log -Xdebug -Xrunjdwp:transport=dt_socket,address=11111,server=y,suspend=n -jar $APP 1>run.log 2>&1 &
