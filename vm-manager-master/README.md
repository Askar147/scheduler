1) Get IP address of the physical machine\
Set VMM ip in .env\
Set DS IP in .env

2) Make sure it has the proper version of AS in rapid-as/rapid-linux-as.jar 

2) Build and package VMM jar:
mvn clean package

4) Run VMM as\
sh build-run-vmm.sh
