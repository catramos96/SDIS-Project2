package gui;

import javax.swing.*;

import peer.Peer;
import tracker.Tracker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.event.ActionEvent;

/**
 * Created by syram on 5/24/17.
 */
public class gui extends JFrame {
	private JTextField trackerIP;
	private JTextField trackerPort;
	private JTextField peerIP;
	private JTextField peerPort;
	private JTextField peerID;
	private JTextField path;
	private JTextField rmiID;

    public static void main(String[] args) {
    	gui test = new gui();
    }

    public gui () {
        initUI();
    }

    private void initUI() {
        
    	// gui  graphical blocks
        setTitle("Distributed Storage System");
        setSize(350, 600);
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
        trackerIP.setBounds(149, 12, 114, 19);
        trackerIP.setText("127.0.0.1");
        panel.add(trackerIP);
        trackerIP.setColumns(10);
        
        JLabel lblPorto = new JLabel("Port");
        lblPorto.setBounds(74, 41, 70, 15);
        panel.add(lblPorto);
        
        trackerPort = new JTextField();
        trackerPort.setBounds(149, 39, 114, 19);
        trackerPort.setText("8080");
        trackerPort.setColumns(10);
        panel.add(trackerPort);
        
        JButton btnStartTracker = new JButton("Start Tracker");
        btnStartTracker.setBounds(74, 77, 189, 25);
        panel.add(btnStartTracker);
               
        JLabel lblPeerIp = new JLabel("Peer IP");
        lblPeerIp.setBounds(74, 154, 70, 15);
        panel.add(lblPeerIp);
        
        peerIP = new JTextField();
        peerIP.setBounds(149, 152, 114, 19);
        peerIP.setText("127.0.0.1");
        panel.add(peerIP);
        peerIP.setColumns(10);
        
        JLabel label = new JLabel("Port");
        label.setBounds(74, 185, 70, 15);
        panel.add(label);
        
        peerPort = new JTextField();
        peerPort.setBounds(149, 183, 114, 19);
        peerPort.setText("3333");
        peerPort.setColumns(10);
        panel.add(peerPort);
        
        JButton btnNewButton = new JButton("Start Peer");
        btnNewButton.setBounds(74, 270, 189, 25);
        
        panel.add(btnNewButton);
        
        peerID = new JTextField();
        peerID.setBounds(149, 214, 114, 19);
        peerID.setText("123");
        panel.add(peerID);
        peerID.setColumns(10);
        
        JLabel lblPeerId = new JLabel("Peer ID");
        lblPeerId.setBounds(74, 212, 70, 15);
        panel.add(lblPeerId);
        
        JRadioButton rdbtnBackup = new JRadioButton("Backup");
        rdbtnBackup.setBounds(28, 326, 149, 23);
        panel.add(rdbtnBackup);
        
        JRadioButton rdbtnDelete = new JRadioButton("Delete");
        rdbtnDelete.setBounds(28, 365, 149, 23);
        panel.add(rdbtnDelete);
        
        JRadioButton rdbtnRestore = new JRadioButton("Restore");
        rdbtnRestore.setBounds(193, 326, 149, 23);
        panel.add(rdbtnRestore);
        
        JRadioButton rdbtnReclaim = new JRadioButton("Reclaim");
        rdbtnReclaim.setBounds(193, 365, 149, 23);
        panel.add(rdbtnReclaim);
        
        ButtonGroup group = new ButtonGroup();
        
        group.add(rdbtnBackup);
        group.add(rdbtnDelete);
        group.add(rdbtnRestore);
        group.add(rdbtnReclaim);
        
        path = new JTextField();
        path.setText("/home/syram/Documents/oi.pdf");
        path.setBounds(74, 437, 239, 19);
        panel.add(path);
        path.setColumns(10);
        
        JLabel lblNewLabel = new JLabel("Path");
        lblNewLabel.setBounds(28, 439, 70, 15);
        panel.add(lblNewLabel);
        
        JButton btnStartProtocol = new JButton("Start Protocol");
        btnStartProtocol.setBounds(74, 468, 189, 25);
        panel.add(btnStartProtocol);
        
        JLabel lblNewLabel_1 = new JLabel("Peer RMI");
        lblNewLabel_1.setBounds(74, 239, 70, 15);
        panel.add(lblNewLabel_1);
        
        rmiID = new JTextField();
        rmiID.setText("rmi1");
        rmiID.setBounds(149, 239, 114, 19);
        panel.add(rmiID);
        rmiID.setColumns(10);
        
        //listeners
        btnStartTracker.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
            	String command[] = {"/bin/sh", "-c", 
                "gnome-terminal --execute java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 tracker.PeerTracker " + trackerPort.getText()}; //TODO
           
            	String teste = executeCommand(command);
           		System.out.println(teste);
            	}
        });
        
        btnNewButton.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent arg0) {
        		String command[] = {"/bin/sh", "-c", 
        		"gnome-terminal --execute java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=client.keys -Djavax.net.ssl.keyStorePassword=123456 peer.FileSharing "+peerID.getText()+" "+rmiID.getText()+" " + trackerIP.getText() + ":" + trackerPort.getText()}; //TODO
        	 	System.out.println(command[2]);
        		String teste = executeCommand(command);
        		System.out.println(teste);
        	}
        });
        
        btnStartProtocol.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		String operation = null;
        		
        		if(rdbtnBackup.isSelected()) {
        			operation = "BACKUP";
        		} else if(rdbtnDelete.isSelected()) {
        			operation = "DELETE";
        		} else if(rdbtnRestore.isSelected()) {
        			operation = "RESTORE";
        		} else if(rdbtnReclaim.isSelected()) {
        			operation = "RECLAIM";
        		}
        		else {
        			return;
        		}
        		String command[] = {"/bin/sh", "-c", 
        				"java client.Main " + rmiID.getText() + " " + operation +" " + path.getText()}; //TODO
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
