@echo off
sleep 3
start java tracker.PeerTracker 1
sleep 2
start java peer.FileSharing 2 127.0.0.1:1 rmi1
sleep 2
start java peer.FileSharing 3 127.0.0.1:1 rmi2
sleep 2
start java peer.FileSharing 4 127.0.0.1:1 rmi3
sleep 2
start java peer.FileSharing 5 127.0.0.1:1 rmi4
sleep 
start java peer.FileSharing 6 127.0.0.1:1 rmi5
sleep 2
start java peer.FileSharing 7 127.0.0.1:1 rmi6
sleep 2
start java peer.FileSharing 8 127.0.0.1:1 rmi7
sleep 2
start java client.Main rmi7 STATE
exit