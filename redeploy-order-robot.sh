#!/bin/sh

## Add by Arthur.Liu on 2016-10-10

here=`pwd`
source_war_path="${here}/spider-55haitao-order-robot/target"
source_war_name="55haitao-order-robot.war"
tomcat_home="/home/arthur/installedprogrames/tomcat9/apache-tomcat-9.0.0.M6"
tomcat_process_name="tomcat9"

## 1.kill old tomcat process
tomcat_process_id=`ps aux | grep "${tomcat_process_name}" | grep -v "grep" | awk -F' ' '{print $2}'`
echo "Will kill old-tomcat-id:${tomcat_process_id}"
kill -9 ${tomcat_process_id}
sleep 1s

## 2.remove old-war-package/logs/cache
rm ${tomcat_home}/webapps/${source_war_name}
rm -rf ${tomcat_home}/webapps/${source_war_name%.*}
rm ${tomcat_home}/logs/*
rm -rf ${tomcat_home}/work/Catalina/localhost/${source_war_name%.*}
sleep 1s

## 3.Copy new-war-package to tomcat-webapps-path
cp ${source_war_path}"/"${source_war_name} ${tomcat_home}"/webapps/"

## 4.Startup new-tomcat-process
cd ${tomcat_home}"/bin"
sh ./startup.sh
cd ${here}

new_tomcat_process_id=`ps aux | grep "${tomcat_process_name}" | grep -v "grep" | awk -F' ' '{print $2}'`
echo "Redeploy successfully! new-tomcat-process-id is:${new_tomcat_process_id}"
