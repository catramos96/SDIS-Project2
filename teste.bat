@echo off
sleep 3
start java tracker.PeerTracker 1
sleep 2
start java peer.FileSharing 2 127.0.0.1:1
sleep 2
start java peer.FileSharing 3 127.0.0.1:1
sleep 2
start java peer.FileSharing 4 127.0.0.1:1
sleep 2
start java peer.FileSharing 5 127.0.0.1:1
sleep 
start java peer.FileSharing 6 127.0.0.1:1
sleep 2
start java peer.FileSharing 7 127.0.0.1:1
sleep 2
start java peer.FileSharing 8 127.0.0.1:1
sleep 2
exit