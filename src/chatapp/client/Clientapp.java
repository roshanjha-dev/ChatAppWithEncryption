package chatapp.client;

import java.net.*;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import chatapp.KleinCipher;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class Clientapp extends JFrame {
	
	private int key[] = {1, 15, 12, 5, 7, 3, 4, 10, 11, 8, 9, 13, 6, 14, 0, 2};
	private int Sbox[] = {7, 4, 10, 9, 1, 15, 11, 0, 12, 3, 2, 6, 8, 14, 13, 5};
	private KleinCipher kleinCipher;
	
	private Socket socket;
	private BufferedReader br;
	private PrintWriter out;
	
	private JLabel heading = new JLabel("Alice");
	private JTextArea messageArea = new JTextArea();
	private JTextField messageInput = new JTextField();
	private Font font = new Font("Roboto", Font.PLAIN, 20);
	
	public Clientapp() {
		
		try {
			System.out.println("Sending request to server...");
			socket = new Socket("127.0.0.1", 7777);
			System.out.println("Connection done!");
			
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			
			createGUI();
			handleEvents();
			
			startReading();
			//startWriting();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createGUI() {
		
		this.setTitle("MySimpleChatApp");
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		heading.setFont(font);
		messageArea.setFont(font);
		messageInput.setFont(font);
		
		heading.setIcon(new ImageIcon(""));
		heading.setHorizontalTextPosition(SwingConstants.CENTER);
		heading.setVerticalTextPosition(SwingConstants.BOTTOM);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		messageArea.setEditable(false);
		messageInput.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.setLayout(new BorderLayout());
		
		
		this.add(heading, BorderLayout.NORTH);
		
		JScrollPane jScrollPane = new JScrollPane(messageArea);
		this.add(jScrollPane, BorderLayout.CENTER);
		this.add(messageInput, BorderLayout.SOUTH);
		
		this.setVisible(true);
	}
	
	private void handleEvents() {
		
		messageInput.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
				if(e.getKeyCode() == 10) {
					String contentToSend = messageInput.getText();
					messageArea.append("Me: " + contentToSend + "\n");
					String input[] = contentToSend.split("");
					int in[] = new int[16];
					
					for(int i=0, j=0; i<input.length && j < in.length; i++){
						
						int bits[] = kleinCipher.decimalToBinary(i, 8);
						int nibble[] = Arrays.copyOfRange(bits, 0, 4);
						in[j++] = Integer.parseInt(kleinCipher.concat(nibble), 2);
						nibble = Arrays.copyOfRange(bits, 4, 8);
						in[j++] = Integer.parseInt(kleinCipher.concat(nibble), 2);
						
					}
					int cipher[] = kleinCipher.encryption(in, key, Sbox);
					String output = "";
					for(int i=0; i<16; i++) {
						output += Integer.toHexString(cipher[i]);
					}
					out.println(contentToSend);
					out.flush();
					messageInput.setText("");
					messageInput.requestFocus();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
	}
	
	public void startReading() {
		
		Runnable r1 = ()->{
			
			System.out.println("Reader started...");
			
			while(true) {
				String msg;
				try {
					msg = br.readLine();
					
					if(msg.equals("exit")) {
						System.out.println("Server terminated the chat!");
						JOptionPane.showMessageDialog(this, "Alice terminated the chat!");
						messageInput.setEnabled(false);
						socket.close();
						break;
					}
					
					//System.out.println("Server: " + msg);
					messageArea.append("Alice: " + msg + "\n");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(r1).start();
	}
	
	public void startWriting() {
		
		Runnable r2 = ()->{
			
			System.out.println("Writer started...");
			while(true) {
				
				try {
					
					BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
					String content = br1.readLine();
					out.println(content);
					out.flush();
					
				}catch(Exception e) {
					
				}
			}
		};
		new Thread(r2).start(); 
	}
	
	public static void main(String[] args) {

		System.out.println("This is client!!!");
		new Clientapp();
	}

}
