package gui;

import javax.swing.*;


import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.event.ActionEvent;

/**
 * Created by syram on 5/24/17.
 */
public class gui extends JFrame {
	private JTextField trackerIP;
	private JTextField trackerPort;
	private JTextField peerID;
	private JTextField path;
	private JTextField rmiID;
	private int defaulRep = 2;
	private String commandHead;
	private String shell;
	private String shellFlags;
	private JTextField backupChannel;
	private JTextField deletechannel;
	private JTextField trackerChannel;
	private JTextField controlChannel;
    public static void main(String[] args) {
    	
    	 try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	 
    	gui test = new gui();
    	
    }

    public gui () {
   	 	System.out.println(System.getProperty("os.name"));
   	 	String OS = System.getProperty("os.name");
   	 	if(OS.startsWith("Linux")){
   	 		commandHead = "gnome-terminal --execute";
   	 		shell = "/bin/sh";
   	 		shellFlags = "-c";
   	 	} else if(OS.startsWith("Windows")){
   	 		commandHead = "start";
   	 		shell = "cmd";
	 		shellFlags = "/c";
   	 	}else{
   	 		System.out.println("Unsupported OS");
   	 	}
        initUI();
    }

    private void initUI() {
        
    	// gui  graphical blocks
        setTitle("Distributed Storage System");
        setSize(350, 576);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);
        
        JLabel lblTrackerIp = new JLabel("Tracker IP");
        lblTrackerIp.setBounds(74, 14, 70, 15);
        panel.add(lblTrackerIp);
        
        trackerIP = new JTextField();
        trackerIP.setBounds(149, 12, 114, 25);
        trackerIP.setText("127.0.0.1");
        panel.add(trackerIP);
        trackerIP.setColumns(10);
        
        JLabel lblPorto = new JLabel("Port");
        lblPorto.setBounds(74, 41, 70, 15);
        panel.add(lblPorto);
        
        trackerPort = new JTextField();
        trackerPort.setBounds(149, 39, 114, 26);
        trackerPort.setText("8080");
        trackerPort.setColumns(10);
        panel.add(trackerPort);
        
        JButton btnStartTracker = new JButton("Start Tracker");
        btnStartTracker.setBounds(74, 77, 189, 25);
        panel.add(btnStartTracker);
        
     
        
        JButton btnNewButton = new JButton("Start Peer");
        btnNewButton.setBounds(74, 328, 189, 25);
        
        panel.add(btnNewButton);
        
        peerID = new JTextField();
        peerID.setBounds(147, 114, 114, 25);
        peerID.setText("123");
        panel.add(peerID);
        peerID.setColumns(10);
        
        JLabel lblPeerId = new JLabel("Peer ID");
        lblPeerId.setBounds(72, 114, 70, 15);
        panel.add(lblPeerId);
        
        JRadioButton rdbtnBackup = new JRadioButton("Backup");
        rdbtnBackup.setBounds(28, 361, 149, 23);
        panel.add(rdbtnBackup);
        
        JRadioButton rdbtnDelete = new JRadioButton("Delete");
        rdbtnDelete.setBounds(28, 400, 149, 23);
        panel.add(rdbtnDelete);
        
        JRadioButton rdbtnRestore = new JRadioButton("Restore");
        rdbtnRestore.setBounds(193, 361, 149, 23);
        panel.add(rdbtnRestore);
        
        JRadioButton rdbtnReclaim = new JRadioButton("Reclaim");
        rdbtnReclaim.setBounds(193, 400, 149, 23);
        panel.add(rdbtnReclaim);
        
        ButtonGroup group = new ButtonGroup();
        
        group.add(rdbtnBackup);
        group.add(rdbtnDelete);
        group.add(rdbtnRestore);
        group.add(rdbtnReclaim);
        
        path = new JTextField();
        path.setBounds(74, 472, 239, 26);
        path.setText("/home/syram/Documents/oi.pdf");
        panel.add(path);
        path.setColumns(10);
        
        JLabel lblNewLabel = new JLabel("Path");
        lblNewLabel.setBounds(28, 474, 70, 15);
        panel.add(lblNewLabel);
        
        JButton btnStartProtocol = new JButton("Start Protocol");
        btnStartProtocol.setBounds(74, 503, 189, 25);
        panel.add(btnStartProtocol);
        
        JLabel lblNewLabel_1 = new JLabel("Peer RMI");
        lblNewLabel_1.setBounds(72, 141, 70, 15);
        panel.add(lblNewLabel_1);
        
        rmiID = new JTextField();
        rmiID.setBounds(147, 141, 114, 25);
        rmiID.setText("rmi1");
        panel.add(rmiID);
        rmiID.setColumns(10);
        
        backupChannel = new JTextField();
        backupChannel.setText("8000");
        backupChannel.setColumns(10);
        backupChannel.setBounds(147, 172, 114, 25);
        panel.add(backupChannel);
        
        JLabel lblPeerMcb = new JLabel("BackupChannel");
        lblPeerMcb.setBounds(28, 172, 114, 15);
        panel.add(lblPeerMcb);
        
        deletechannel = new JTextField();
        deletechannel.setText("8001");
        deletechannel.setColumns(10);
        deletechannel.setBounds(147, 199, 114, 25);
        panel.add(deletechannel);
        
        JLabel lblPeerMcd = new JLabel("DeleteChannel");
        lblPeerMcd.setBounds(28, 199, 114, 15);
        panel.add(lblPeerMcd);
        
        trackerChannel = new JTextField();
        trackerChannel.setText("8002");
        trackerChannel.setColumns(10);
        trackerChannel.setBounds(147, 226, 114, 25);
        panel.add(trackerChannel);
        
        JLabel lblRestoreChannel = new JLabel("TrackerChannel");
        lblRestoreChannel.setBounds(28, 231, 116, 15);
        panel.add(lblRestoreChannel);
        
        controlChannel = new JTextField();
        controlChannel.setText("8003");
        controlChannel.setColumns(10);
        controlChannel.setBounds(146, 253, 116, 25);
        panel.add(controlChannel);
        
        JLabel lblControlchannel = new JLabel("ControlChannel");
        lblControlchannel.setBounds(28, 258, 116, 15);
        panel.add(lblControlchannel);
        
        //listeners
        //defPort:mcPort:mdrPort:mdbPort
        btnStartTracker.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
            	String command[] = {shell, shellFlags, 
                "gnome-terminal --execute java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker " + trackerPort.getText()};  
           
            	String teste = executeCommand(command);
           		System.out.println(teste);
            	}
        });
        
        btnNewButton.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent arg0) {
        		
        		String ports = trackerChannel.getText()+":"+controlChannel.getText()+":"+deletechannel.getText()+":"+backupChannel.getText();
        		String command[] = {shell, shellFlags, 
        		commandHead + " java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing "+peerID.getText()+" "+ ports +" "+rmiID.getText()+" " + trackerIP.getText() + ":" + trackerPort.getText()}; 
        	 	System.out.println(command[2]);
        		String teste = executeCommand(command);
        		System.out.println(teste);
        	}
        });
        
        btnStartProtocol.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		String operation = null;
        		String command[];
        		if(rdbtnBackup.isSelected()) {
        			operation = "BACKUP";
        			command = new String [] {shell, shellFlags, "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText() + " " + defaulRep};
        		} else if(rdbtnDelete.isSelected()) {
        			operation = "DELETE";
        			command = new String [] {shell, shellFlags, "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()};
        		} else if(rdbtnRestore.isSelected()) {
        			operation = "RESTORE";
        			command = new String [] {shell, shellFlags, "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()};
        		} else if(rdbtnReclaim.isSelected()) {
        			operation = "RECLAIM";
        			command = new String [] {shell, shellFlags, "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()};
        		}
        		else {
        			return;
        		}
        		String teste = executeCommand(command);
        		System.out.println(teste);
        	}
        });
        
     
        setVisible(true);
    }
    
    
    private String executeCommand(String[] command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
}
