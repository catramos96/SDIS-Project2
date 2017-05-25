package gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;

public class desktopGUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				
				try {
					desktopGUI frame = new desktopGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public desktopGUI() {
		
		setDefaultTheme();
		
		// Frame Specs
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 286, 620);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTrackerIp = new JLabel("Tracker IP");
		lblTrackerIp.setBounds(10, 11, 74, 14);
		contentPane.add(lblTrackerIp);
		
		textField = new JTextField();
		textField.setText("127.0.0.1");
		textField.setBounds(70, 8, 179, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(10, 36, 46, 14);
		contentPane.add(lblPort);
		
		textField_1 = new JTextField();
		textField_1.setText("8080");
		textField_1.setBounds(70, 36, 179, 20);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnNewButton = new JButton("Launch Tracker");
		btnNewButton.setBounds(20, 67, 232, 23);
		contentPane.add(btnNewButton);
		
		textField_2 = new JTextField();
		textField_2.setText("127.0.0.1");
		textField_2.setBounds(70, 112, 179, 20);
		contentPane.add(textField_2);
		textField_2.setColumns(10);
		
		JLabel lblPeerIp = new JLabel("Peer IP");
		lblPeerIp.setBounds(10, 115, 46, 14);
		contentPane.add(lblPeerIp);
		
		textField_3 = new JTextField();
		textField_3.setText("8081");
		textField_3.setBounds(70, 142, 179, 23);
		contentPane.add(textField_3);
		textField_3.setColumns(10);
		
		JLabel lblPort_1 = new JLabel("Port");
		lblPort_1.setBounds(10, 146, 46, 14);
		contentPane.add(lblPort_1);
		
		JButton btnLaunchPeer = new JButton("Launch Peer");
		btnLaunchPeer.setBounds(20, 176, 232, 20);
		contentPane.add(btnLaunchPeer);
		
		textField_4 = new JTextField();
		textField_4.setBounds(70, 264, 179, 20);
		contentPane.add(textField_4);
		textField_4.setColumns(10);
		
		JLabel lblPath = new JLabel("Path");
		lblPath.setBounds(10, 267, 46, 14);
		contentPane.add(lblPath);
		
		ButtonGroup group = new ButtonGroup();
		
		JRadioButton rdbtnSend = new JRadioButton("Backup");
		rdbtnSend.setBounds(70, 309, 109, 23);
		group.add(rdbtnSend);
		
		JRadioButton rdbtnDelete = new JRadioButton("Delete");
		rdbtnDelete.setBounds(70, 335, 109, 23);
		group.add(rdbtnDelete);
		
		JRadioButton rdbtnRestore = new JRadioButton("Restore");
		rdbtnRestore.setBounds(70, 361, 109, 23);
		group.add(rdbtnRestore);
		
		contentPane.add(rdbtnSend);
		contentPane.add(rdbtnDelete);
		contentPane.add(rdbtnRestore);
		
		
		
		JButton btnNewButton_1 = new JButton("Start");
		btnNewButton_1.setBounds(10, 396, 239, 23);
		contentPane.add(btnNewButton_1);
		
		JTextPane textPane = new JTextPane();
		textPane.setBounds(255, 430, -244, 140);
		contentPane.add(textPane);
	}
	/**
	 * Set default theme
	 */
	
	public void setDefaultTheme() {
		// Defines the theme
		 try {
	            // Set System L&F
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }
	}
}
