@echo off
start rmiregistry
timeout /t 3
start java tracker.PeerTracker 1
timeout /t  2
start java peer.FileSharing 2 rmi1 127.0.0.1:1
timeout /t  2
start java peer.FileSharing 3 rmi2 127.0.0.1:1
timeout /t  2
start java peer.FileSharing 4 rmi3 127.0.0.1:1
timeout /t  2
start java peer.FileSharing 5 rmi4 127.0.0.1:1
timeout /t  
start java peer.FileSharing 6 rmi5 127.0.0.1:1
timeout /t  2
start java peer.FileSharing 7 rmi6 127.0.0.1:1
timeout /t  2
start java peer.FileSharing 8 rmi7 127.0.0.1:1
timeout /t  2
start java client.Main rmi7 BACKUP ../horario.PNG 2
exit