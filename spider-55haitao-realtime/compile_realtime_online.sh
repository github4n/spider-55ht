#!/bin/sh

mvn clean
sleep 1s

mvn eclipse:clean
sleep 1s

mvn -Pproduct install -Dmaven.test.skip=true
sleep 1s

mvn eclipse:eclipse
sleep 1s
