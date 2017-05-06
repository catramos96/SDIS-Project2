@echo off
start java tracker.PeerTracker 1
timeout 2
start java peer.FileSharing 2 127.0.0.1:1
start java peer.FileSharing 3 127.0.0.1:1
exit