#!/bin/sh

APP="spider-55haitao-controller.jar"

ps aux | grep "$APP" | awk -F' ' '{print $2}' | xargs -I {} kill -9 {}

echo "shutdown OK!"
