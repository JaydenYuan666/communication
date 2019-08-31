package server;

import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Font;

public class ServerFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextArea showMessage;
	private JTextField sendMsg;
	private Server server;
	private JComboBox<String> clientList;
	JButton send;
	private JTextField ip_prot;
	private JMenuBar showjMenubar;
	private JScrollPane showMsgPanel;

	public ServerFrame() {
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

		setIconImage(Toolkit.getDefaultToolkit().getImage(
				ServerFrame.class.getResource("/photo/ico.png")));
		server = new Server(this);

		this.setSize(Constant.WIDTH, Constant.HEIGHT);
		this.setLocation(Constant.LOCATION_X, Constant.LOCATION_Y);

		this.setResizable(false);
		getContentPane().setLayout(null);
		this.setLocationRelativeTo(null);// 设置窗口到屏幕中央
		send = new JButton("发送");
		send.setForeground(SystemColor.desktop);
		send.setBackground(SystemColor.menu);
		send.setBounds(489, 618, 95, 23);

		getContentPane().add(send);

		initScrollBar();

		// jfc = new JFileChooser(".");

		sendMsg = new JTextField();
		sendMsg.setBounds(10, 442, 574, 164);
		getContentPane().add(sendMsg);
		sendMsg.setColumns(10);

		clientList = new JComboBox<String>();
		clientList.setForeground(SystemColor.desktop);
		clientList.setBackground(SystemColor.menu);
		clientList.setBounds(92, 388, 139, 21);
		getContentPane().add(clientList);
		JLabel lblNewLabel = new JLabel("选择客户端：");
		lblNewLabel.setBounds(10, 388, 83, 21);
		getContentPane().add(lblNewLabel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initListener();

		ip_prot = new JTextField("");
		ip_prot.setFont(new Font("楷体", Font.PLAIN, 14));
		ip_prot.setBounds(257, 388, 323, 23);
		getContentPane().add(ip_prot);
		ip_prot.setText("服务器IP和端口号：" + server.getIPAndProt());
		ip_prot.setEditable(false);
		this.setTitle("服务器");
		this.setVisible(true);
		server.start(showMessage);
	}

	public void initScrollBar() {
		showMessage = new JTextArea();
		showMessage.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
		showMessage.setColumns(10);
		showMessage.setEditable(false);
		showMessage.setBackground(SystemColor.controlHighlight);
		showjMenubar = new JMenuBar();
		this.setJMenuBar(showjMenubar);
		showMsgPanel = new JScrollPane(showMessage);
		getContentPane().add(showMsgPanel);
		showMsgPanel.setBounds(10, 10, 570, 368);
		getContentPane().add(showMsgPanel);
	}

	public void addComItem(int id) {
		clientList.addItem("客户端 " + id);
	}

	private void initListener() {

		sendMsg.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendM();
				}

			}
		});

		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendM();
			}
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				server.close();
				dispose();
				System.exit(0);
			}
		});
	}

	private void sendM() {
		String msg = sendMsg.getText();
		if (msg.equals("")) {
			hintMessage("不能发送空消息", "提示!");
			return;
		}
		String s = (String) clientList.getSelectedItem();
		System.out.println(s);
		if (s == null) {
			hintMessage("当前没有客户端连接，不能发送消息", "提示");
			return;
		}
		String[] temp = s.split(" ");
		if (temp == null || temp.length <= 1) {
			hintMessage("当前没有客户端连接，不能发送消息", "提示");
			return;
		}
		String id = temp[1];

		showMassageToTextArea(msg, "me", s);
		// 清空消息框
		sendMsg.setText("");
		server.sendMassage(server.getId(), msg, Integer.parseInt(id));
	}

	public void showMassageToTextArea(String massage, String who, String target) {
		
		if (who.length() > 0) {
			who = who + " 对  "+target+"说:"+ "      (" + Constant.format.format(new Date()) + ")\n";
		}
		
		synchronized (showMessage) {
			showMessage.setText(showMessage.getText() + "\n" + who + "   "
					+ massage );
		}
	}

	public void showMassageToTextArea(String massage, String who) {

		if (who.length() > 0) {
			who = who + ":"+"      (" + Constant.format.format(new Date()) + ")\n";
		}
		synchronized (showMessage) {
			showMessage.setText(showMessage.getText() + "\n" + who + "   " + massage);
		}
	}

	public void deleteItem(int clientId) {
		clientList.removeItem("客户端 " + clientId);

	}

	public void hintMessage(String hint, String hintTitle) {
		JOptionPane.showMessageDialog(this, hint, hintTitle,
				JOptionPane.WARNING_MESSAGE);
	}

	public void hintMessage(JFrame parent, String hint, String hintTitle,
			int TYPE) {
		JOptionPane.showMessageDialog(parent, hint, hintTitle, TYPE);
	}

	public static void main(String[] args) {
		new ServerFrame();
	}
}
