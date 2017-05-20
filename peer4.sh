export CLASSPATH="src"

javac src/*/*.java
nohup rmiregistry &
sleep 2
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8004 rmi4 127.0.0.1:8000"