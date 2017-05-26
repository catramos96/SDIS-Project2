@echo off
start rmiregistry
timeout /t 3
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=server.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker 8000
timeout /t  2
start java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing 8001 rmi1 127.0.0.1:8000
timeout /t  2
exit