package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class CilentFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField field;
	private JTextArea showMessage;
	private JButton browse;
	private JTextField sendMsg;
	private Client clinet;
	private JButton send;

	private JMenuBar showjMenubar;
	private JScrollPane showMsgPanel;
	private JComboBox<String> clientList;
	private JCheckBox compress;
	private JCheckBox encipherment;
	public void initScrollBar() {
		showMessage = new JTextArea();
		showMessage.setFont(new Font("楷体", Font.PLAIN, 14));
		showMessage.setColumns(10);
		showMessage.setEditable(false);
		showMessage.setBackground(new Color(245, 245, 245));
		showjMenubar = new JMenuBar();
		this.setJMenuBar(showjMenubar);
		showMsgPanel = new JScrollPane(showMessage);
		getContentPane().add(showMsgPanel);
		showMsgPanel.setBounds(10, 10, 489, 368);
		getContentPane().add(showMsgPanel);
	}

	public CilentFrame() {
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
	}

	public void initShow(Client myclinet) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				IpInFrame.class.getResource("/photo/ico.png")));
		getContentPane().setBackground(SystemColor.controlHighlight);
		this.setSize(Constant.WIDTH, Constant.HEIGHT);
		// this.setLocation(Constant.LOCATION_X, Constant.LOCATION_Y);

		this.setResizable(false);
		getContentPane().setLayout(null);
		this.setLocationRelativeTo(null);// 设置窗口到屏幕中央
		send = new JButton("发送");
		send.setForeground(SystemColor.desktop);
		send.setBackground(SystemColor.menu);
		send.setBounds(404, 607, 95, 23);

		getContentPane().add(send);

		initScrollBar();

		field = new JTextField();
		field.setForeground(SystemColor.desktop);
		field.setBackground(SystemColor.controlLtHighlight);
		field.setEditable(false);
		field.setBounds(10, 419, 423, 34);
		getContentPane().add(field);
		field.setColumns(10);

		browse = new JButton("浏览");
		browse.setForeground(SystemColor.desktop);
		browse.setBackground(SystemColor.control);

		browse.setBounds(435, 418, 64, 34);
		getContentPane().add(browse);

		sendMsg = new JTextField();
		sendMsg.setFont(new Font("楷体", Font.PLAIN, 14));
		sendMsg.setBackground(SystemColor.controlLtHighlight);
		sendMsg.setBounds(10, 463, 489, 134);
		getContentPane().add(sendMsg);
		sendMsg.setColumns(10);

		JLabel nmae = new JLabel("");
		nmae.setBounds(404, 388, 105, 21);
		getContentPane().add(nmae);

		compress = new JCheckBox("\u6587\u4EF6\u538B\u7F29");
		compress.setForeground(SystemColor.desktop);
		compress.setBackground(SystemColor.controlHighlight);
		compress.setBounds(20, 390, 95, 23);
		getContentPane().add(compress);

		encipherment = new JCheckBox("\u6D88\u606F\u52A0\u5BC6");
		encipherment.setForeground(SystemColor.desktop);
		encipherment.setBackground(SystemColor.controlHighlight);
		encipherment.setBounds(142, 390, 95, 23);
		getContentPane().add(encipherment);

		clientList = new JComboBox<String>();
		clientList.setBackground(SystemColor.menu);
		clientList.setBounds(243, 388, 157, 21);
		clientList.addItem("服务器  " + 0);

		getContentPane().add(clientList);

		this.clinet = myclinet;
		initListener();
		nmae.setText("我是：客户端 " + clinet.getClientId());
	
		
		this.setTitle("客户端 " + clinet.getClientId());
		clinet.startReceiveMessage();
		this.setVisible(true);
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

		browse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				JFileChooser jfc = new JFileChooser(".");
				int returnVal = jfc.showOpenDialog(new javax.swing.JFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					field.setText(jfc.getSelectedFile().getPath());
				}
			}
		});

		field.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean importData(JComponent comp, Transferable t) {
				try {
					Object o = t.getTransferData(DataFlavor.javaFileListFlavor);

					String filepath = o.toString();
					if (filepath.startsWith("[")) {
						filepath = filepath.substring(1);
					}
					if (filepath.endsWith("]")) {
						filepath = filepath.substring(0, filepath.length() - 1);
					}
					System.out.println(filepath);
					field.setText(filepath);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] flavors) {
				for (int i = 0; i < flavors.length; i++) {
					if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
						return true;
					}
				}
				return false;
			}
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				clinet.close();// 关闭连接
				dispose();
				System.exit(0);
			}
		});
	}

	public JTextArea getShowMessage() {
		return showMessage;
	}

	private void sendM() {
		String msg = sendMsg.getText();
		String path = field.getText();
		String targetName = (String) clientList.getSelectedItem();

		String[] temp = targetName.split(" ");
		if (temp == null || temp.length <= 1) {
			hintMessage("出现未知错误，不能发送消息", "提示");
			return;
		}
		String targetId = temp[1];

		if (msg.equals("") && path.equals("")) {
			hintMessage("不能发送空消息", "提示!");
			return;
		}
		if (clinet.isServerClose()) {
			showMassage("服务器已断开连接...", "提示");
			clinet.reconectServer();
			return;
		}

		// field不为空
		if (!path.equals("")) {
			// 发送传输文件请求

			clinet.sendFileHeadMsg(path, targetId);

			field.setText("");
		}
		if (!msg.equals("")) {
			showMassage(msg, "me", targetName);
			sendMsg.setText("");// 清空消息框
			clinet.sendMassage(msg, targetId);
		}
	}

	// 发送消息
	public void showMassage(String massage, String who, String target) {
		

		if (who.length() > 0) {
			who = who + " 对  " + target + "说:" + "      ("
					+ Constant.format.format(new Date()) + ")\n";
		}

		synchronized (showMessage) {
			showMessage.setText(showMessage.getText() + "\n" + who + "   "
					+ massage);
		}
	}

	public void showMassage(String massage, String who) {

		if (who.length() > 0) {
			who = who + ":" + "      (" + Constant.format.format(new Date()) + ")\n";
		}

		synchronized (showMessage) {
			showMessage.setText(showMessage.getText() + "\n" + who + "   "
					+ massage);
		}
	}

	public void addComItem(int id) {
		clientList.addItem("客户端 " + id);
	}

	public boolean isCompress() {
		return compress.isSelected();
	}

	public boolean isEncipherment() {
		return encipherment.isSelected();
	}

	public void hintMessage(String hint, String hintTitle) {
		JOptionPane.showMessageDialog(this, hint, hintTitle,
				JOptionPane.WARNING_MESSAGE);
	}

	public void hintMessage(JFrame parent, String hint, String hintTitle,
			int TYPE) {
		JOptionPane.showMessageDialog(parent, hint, hintTitle, TYPE);
	}

	public void initItem(String allid) {
		clientList.removeAllItems();
		clientList.addItem("服务器 " + 0);
		String[] allID = allid.split(Constant.DIVIDE);

		for (int i = 0; i < allID.length; i++) {
			int id = Integer.parseInt(allID[i]);
			if (id == clinet.getClientId()) {
				continue;
			}
			clientList.addItem("客户端 " + id);
		}
	}

	public void addItem(int id) {

		clientList.addItem("客户端 " + id);
	}

	public void removeItem(int parseInt) {
		clientList.removeItem("客户端 " + parseInt);
	}
}