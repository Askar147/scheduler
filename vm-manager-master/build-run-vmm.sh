#!/bin/bash

if [ -f .env ]
then
  export $(cat .env | sed 's/#.*//g' | xargs)
fi

#build as
rm config.properties
touch config.properties
echo "rapidFolder=rapid-server" >> config.properties
echo "asPort=${AS_PORT}" >> config.properties
echo "asPortSSL=${AS_PORT_SSL}" >> config.properties
echo "dsIp=${DS_IP}" >> config.properties
echo "dsPort=${DS_PORT}" >> config.properties
echo "slamIp=${DS_IP}" >> config.properties
echo "slamPort=${DS_PORT}" >> config.properties
echo "vmmIp=${VMM_IP}" >> config.properties
echo "vmmPort=${VMM_PORT}" >> config.properties
echo "taskDeadline=10" >> config.properties
echo "taskCycles=10" >> config.properties

jar -uf rapid-as/rapid-linux-as.jar config.properties
docker build -t rapid/as ./rapid-as/.

rm config.properties

#build vmm
touch config.properties

echo "dsAddress=${DS_IP}" >> config.properties
echo "dsPort=${DS_PORT}" >> config.properties
echo "vmmPort=${VMM_PORT}" >> config.properties
echo "vmmAddress=${VMM_IP}" >> config.properties
echo "maxConnections=${MAX_CONNECTIONS}" >> config.properties
echo "gpuCores=${GPU_CORES}" >> config.properties
echo "nvidiaSmiPath=${NVIDIA_SMI_PATH}" >> config.properties
echo "gpuDeviceNum=${GPU_DEVICE_NUM}" >> config.properties
echo "availableType=${AVAILABLE_TYPES}" >> config.properties
echo "dataPath=${HOME}/${DATA_PATH_FROM_HOME}/" >> config.properties
echo "deviceType=${DEVICE_TYPE}" >> config.properties

jar -uf target/rapid-vmmanager-3.0.2-SNAPSHOT-jar-with-dependencies.jar config.properties
mkdir ${HOME}/${DATA_PATH_FROM_HOME}  #if no success it will just continue
#rm ${HOME}/${DATA_PATH_FROM_HOME}/vmList.out

#run vmm
java -jar target/rapid-vmmanager-3.0.2-SNAPSHOT-jar-with-dependencies.jar


