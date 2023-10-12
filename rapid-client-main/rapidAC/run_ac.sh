#!/bin/bash
export $(grep -v '^#' .env | xargs -d '\n')
rm config.properties
touch config.properties
echo "rapidFolder=rapid-client" >> config.properties
echo "asPort=${AS_PORT}" >> config.properties
echo "asPortSsl=${AS_PORT_SSL}" >> config.properties
echo "dsIp=${DS_IP}" >> config.properties
echo "dsPort=${DS_PORT}" >> config.properties
echo "acRmPort=${AC_RM_PORT}" >> config.properties
echo "slamIp=${DS_IP}" >> config.properties
echo "slamPort=${DS_PORT}" >> config.properties
echo "connectToPrevVm=${CONNECT_TO_PREV_VM}" >> config.properties
echo "connectSSL=${CONNECT_SSL}" >> config.properties
echo "taskDeadline=${TASK_DEADLINE}" >> config.properties
echo "taskCycles=${TASK_CYCLES}" >> config.properties

echo "gvirtusIp=storm.uniparthenope.it
gvirutsPort=9991
# Cryptographic parameters
# sslKeyStore=keystore.bks
sslKeyStore=client-keystore.jks
sslKeyStorePassword=passkeystore
caTrustStore=truststore.jks
caTrustStorePassword=passkeystore
certAlias=client-cert
certPassword=passclient
assymmetricAlg=RSA;
symmetricAlg=AES;
symmtricKeySize=256;"  >> config.properties

jar -uf ac.jar config.properties
java -Djava.library.path=./Resources/libs -jar ./ac.jar -rapid -conn clear
