#!/bin/bash
rm config.properties
touch config.properties
echo "vmmPort=9000" >> config.properties
echo "schedulerPort=9001" >> config.properties
echo "maxConnections=${MAX_CONNECTIONS}" >> config.properties
echo "schedulerIp=${SCHEDULER_IP}" >> config.properties

jar -uf ds.jar config.properties
java -jar ./ds.jar
