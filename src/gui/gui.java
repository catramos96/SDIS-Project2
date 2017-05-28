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
    public static void main(String[] args) {
    	
    	 try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	 System.out.println(System.getProperty("os.name"));
    	gui test = new gui();
    	
    }

    public gui () {
        initUI();
    }

    private void initUI() {
        
    	// gui  graphical blocks
        setTitle("Distributed Storage System");
        setSize(350, 473);
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
        btnNewButton.setBounds(72, 172, 189, 25);
        
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
        rdbtnBackup.setBounds(26, 228, 149, 23);
        panel.add(rdbtnBackup);
        
        JRadioButton rdbtnDelete = new JRadioButton("Delete");
        rdbtnDelete.setBounds(26, 267, 149, 23);
        panel.add(rdbtnDelete);
        
        JRadioButton rdbtnRestore = new JRadioButton("Restore");
        rdbtnRestore.setBounds(191, 228, 149, 23);
        panel.add(rdbtnRestore);
        
        JRadioButton rdbtnReclaim = new JRadioButton("Reclaim");
        rdbtnReclaim.setBounds(191, 267, 149, 23);
        panel.add(rdbtnReclaim);
        
        ButtonGroup group = new ButtonGroup();
        
        group.add(rdbtnBackup);
        group.add(rdbtnDelete);
        group.add(rdbtnRestore);
        group.add(rdbtnReclaim);
        
        path = new JTextField();
        path.setBounds(72, 339, 239, 26);
        path.setText("/home/syram/Documents/oi.pdf");
        panel.add(path);
        path.setColumns(10);
        
        JLabel lblNewLabel = new JLabel("Path");
        lblNewLabel.setBounds(26, 341, 70, 15);
        panel.add(lblNewLabel);
        
        JButton btnStartProtocol = new JButton("Start Protocol");
        btnStartProtocol.setBounds(72, 370, 189, 25);
        panel.add(btnStartProtocol);
        
        JLabel lblNewLabel_1 = new JLabel("Peer RMI");
        lblNewLabel_1.setBounds(72, 141, 70, 15);
        panel.add(lblNewLabel_1);
        
        rmiID = new JTextField();
        rmiID.setBounds(147, 141, 114, 25);
        rmiID.setText("rmi1");
        panel.add(rmiID);
        rmiID.setColumns(10);
        
        //listeners
        btnStartTracker.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
            	String command[] = {"/bin/sh", "-c", 
                "gnome-terminal --execute java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker " + trackerPort.getText()};  
           
            	String teste = executeCommand(command);
           		System.out.println(teste);
            	}
        });
        
        btnNewButton.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent arg0) {
        		String command[] = {"/bin/sh", "-c", 
        		"gnome-terminal --execute java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing "+peerID.getText()+" "+rmiID.getText()+" " + trackerIP.getText() + ":" + trackerPort.getText()}; 
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
        			command = new String [] {"/bin/sh", "-c", "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText() + " " + defaulRep};
        		} else if(rdbtnDelete.isSelected()) {
        			operation = "DELETE";
        			command = new String [] {"/bin/sh", "-c", "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()};
        		} else if(rdbtnRestore.isSelected()) {
        			operation = "RESTORE";
        			command = new String [] {"/bin/sh", "-c", "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()};
        		} else if(rdbtnReclaim.isSelected()) {
        			operation = "RECLAIM";
        			command = new String [] {"/bin/sh", "-c", "java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()};
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
