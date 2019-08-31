package client;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class IpInFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private JButton quit;
	private JTextField ipAdress;
	private JButton connect;
	private JTextField port;
	private Client client;
	private CilentFrame clientFream;

	public IpInFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new EncryptUtil();// 预加载

		clientFream = new CilentFrame();
		client = new Client(clientFream);
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				IpInFrame.class.getResource("/photo/ico.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(400, 300);
		this.setTitle("连接服务器");
		this.setResizable(false);
		getContentPane().setLayout(null);

		ipAdress = new JTextField();
		ipAdress.setBounds(81, 105, 287, 21);
		getContentPane().add(ipAdress);

		JLabel lblIp = new JLabel("IP\u5730\u5740");
		lblIp.setBounds(22, 105, 56, 15);
		getContentPane().add(lblIp);

		JLabel label = new JLabel("\u7AEF\u53E3\u53F7");
		label.setBounds(22, 168, 56, 15);
		getContentPane().add(label);

		port = new JTextField();
		port.setBounds(81, 168, 287, 21);
		getContentPane().add(port);

		connect = new JButton("\u8FDE\u63A5");
		connect.setForeground(SystemColor.desktop);
		connect.setBackground(SystemColor.menu);
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		connect.setBounds(104, 224, 93, 23);
		getContentPane().add(connect);

		quit = new JButton("\u9000\u51FA");
		quit.setForeground(SystemColor.desktop);
		quit.setBackground(SystemColor.menu);
		quit.setBounds(251, 224, 93, 23);
		getContentPane().add(quit);

		JLabel label_1 = new JLabel(
				"\u8F93\u5165\u670D\u52A1\u5668\u4FE1\u606F");
		label_1.setFont(new Font("\u6977\u4F53", label_1.getFont().getStyle(),
				22));
		label_1.setBounds(123, 25, 169, 33);
		getContentPane().add(label_1);
		this.setLocationRelativeTo(null);// 设置窗口到屏幕中央
		initListenner();
		this.setVisible(true);
	}

	private void initListenner() {

		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				judgeConnect();
			}
		});

		quit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);

			}
		});

		ipAdress.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					port.setFocusable(true);
				}else if(e.getKeyCode() == KeyEvent.VK_ENTER){
					judgeConnect();
				}else{
					
				}
			}
		});
		
		port.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					ipAdress.setFocusable(true);
				}else if(e.getKeyCode() == KeyEvent.VK_ENTER){
					judgeConnect();
				}else{
					
				}
			}
		});
	}

	public void judgeConnect() {
		String IP = ipAdress.getText();
		String serverPort = port.getText();
		String[] tempIP = IP.split("[.]");
		int serPort = 8080;
		if ("".equals(IP)) {
			hintMessage("请输入IP地址！！！", "缺少输入");
			return;
		}
		if ("".equals(serverPort)) {
			hintMessage("请输入端口号！！！", "缺少输入");
			return;
		}

		if (tempIP == null || tempIP.length != 4) {
			hintMessage("IP地址输入有误，应为四个点分十进制！！！", "IP出错");
			return;
		}

		try {
			for (int i = 0; i < 4; i++) {
				int temp = Integer.parseInt(tempIP[i]);
				if (temp >= 0 && temp <= 255) {
					continue;
				}
				hintMessage("IP输入有误，每个点分十进制应在0-255之间，请重新输入！！！", "IP出错");
				return;
			}

		} catch (NumberFormatException e1) {
			hintMessage("IP输入有误，每个点分十进制是0-255之间的整数！！！", "IP出错");
			return;
		}

		try {
			serPort = Integer.parseInt(serverPort);
		} catch (NumberFormatException e1) {
			hintMessage("端口号输入有误，请重新输入！！！", "端口出错");

			return;
		}
		try {
			if (!InetAddress.getByName(IP).isReachable(2000)) {
				hintMessage("IP输入有误，不能连接到此IP，请重新输入！！！", "IP出错");
				return;
			}
		} catch (UnknownHostException e1) {
			hintMessage("IP输入有误，不可识别的IP地址，请重新输入！！！", "IP出错");
			return;
		} catch (IOException e1) {
			hintMessage("IP输入有误，请重新输入！！！", "IP出错");
			return;
		}

		if (client.conectServer(IP, serPort)) {
			clientFream.initShow(client);
			// clientFream.setVisible(true);

			this.dispose();
		} else {
			hintMessage("连接失败，请检查端口号是否可用！！！！！！", "error");
			return;
		}
	}

	public void hintMessage(String hint, String hintTitle) {
		JOptionPane.showMessageDialog(this, hint, hintTitle,
				JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		new IpInFrame();

	}
}
