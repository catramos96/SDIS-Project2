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
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8004 rmi4 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8005 rmi5 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8006 rmi6 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8007 rmi7 127.0.0.1:8000"
sleep 1
java client.Main rmi7 BACKUP horario.PNG 3
sleep 5
java client.Main rmi7 DELETE horario.PNG
sleep 5
java client.Main rmi7 BACKUP horario.PNG 3