@echo off
start rmiregistry
timeout /t 3
start java tracker.PeerTracker 1
timeout /t 2
start java peer.FileSharing 2 127.0.0.1:1 rmi1
timeout /t 2
start java peer.FileSharing 3 127.0.0.1:1 rmi2
timeout /t 2
start java peer.FileSharing 4 127.0.0.1:1 rmi3
timeout /t 2
start java peer.FileSharing 5 127.0.0.1:1 rmi4
timeout /t 
start java peer.FileSharing 6 127.0.0.1:1 rmi5
timeout /t 2
start java peer.FileSharing 7 127.0.0.1:1 rmi6
timeout /t 2
start java peer.FileSharing 8 127.0.0.1:1 rmi7
timeout /t 2
exit