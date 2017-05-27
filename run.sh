export CLASSPATH="src"

javac src/*/*.java
nohup rmiregistry &
sleep 2
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker 8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8001 rmi1 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8002 rmi2 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8003 rmi3 127.0.0.1:8000"
sleep 2
java client.Main rmi1 BACKUP horario.PNG 2
sleep 10
java client.Main rmi1 DELETE horario.PNG

