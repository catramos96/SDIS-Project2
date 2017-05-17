package gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTree;

public class desktopGUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 475, 620);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(223, 77, 114, 19);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(223, 108, 114, 19);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnRegister.setBounds(98, 139, 104, 25);
		contentPane.add(btnRegister);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(113, 79, 89, 15);
		contentPane.add(lblUsername);
		
		JLabel lblPass = new JLabel("Pass:");
		lblPass.setBounds(113, 110, 70, 15);
		contentPane.add(lblPass);
		
		JLabel lblLoginregister = new JLabel("Login/Register");
		lblLoginregister.setBounds(162, 50, 110, 15);
		contentPane.add(lblLoginregister);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(223, 139, 94, 25);
		contentPane.add(btnLogin);
		
		JTree tree = new JTree();
		tree.setBounds(34, 249, 155, 270);
		contentPane.add(tree);
		
		JLabel lblFileManager = new JLabel("File Manager");
		lblFileManager.setBounds(34, 223, 104, 15);
		contentPane.add(lblFileManager);
		
		JButton btnNewButton_1 = new JButton("Download");
		btnNewButton_1.setBounds(262, 531, 117, 25);
		contentPane.add(btnNewButton_1);
		
		JButton btnLoadFile = new JButton("Upload");
		btnLoadFile.setBounds(55, 531, 117, 25);
		contentPane.add(btnLoadFile);
		
		JTree tree_1 = new JTree();
		tree_1.setBounds(223, 249, 181, 270);
		contentPane.add(tree_1);
		
		JLabel lblStoredFiles = new JLabel("Stored Files");
		lblStoredFiles.setBounds(223, 223, 114, 15);
		contentPane.add(lblStoredFiles);
	}
}
