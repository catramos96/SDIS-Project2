export CLASSPATH="src"

javac src/*/*.java
nohup rmiregistry &
sleep 2
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker 8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8001 8001:8002:8003:8004 rmi1 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8002 8005:8006:8007:8008 rmi2 127.0.0.1:8000"
sleep 1
gnome-terminal --working-directory=${PWD} -e "java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8003 8009:8010:8011:8012 rmi3 127.0.0.1:8000"
sleep 2
java client.Main rmi1 BACKUP horario.PNG 2


