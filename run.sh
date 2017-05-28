export CLASSPATH="src"

javac src/*/*.java
nohup rmiregistry &
sleep 2
gnome-terminal --working-directory=${PWD} -e "java gui.gui"



