@echo off
start rmiregistry
timeout /t 3
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=server.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker 8000
timeout /t  2
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8001 rmi1 127.0.0.1:8000
timeout /t  2
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8002 rmi2 127.0.0.1:8000
timeout /t  2
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8003 rmi3 127.0.0.1:8000
timeout /t  2
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8004 rmi4 127.0.0.1:8000
timeout /t  2
start java client.Main rmi1 BACKUP ../../../resources/idea.jpg 3
exit