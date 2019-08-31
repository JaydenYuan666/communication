package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ReceiveMessage implements Runnable {

	private JTextArea showMessage;
	private boolean flag;
	private Client client;
	private Socket socket;

	public ReceiveMessage(Client client, Socket socket, JTextArea showMessage) {
		this.showMessage = showMessage;
		this.socket = socket;
		flag = true;
		this.client = client;
	}

	/**
	 * 先决条件是服务器端先开启
	 * 
	 */
	public void run() {
		while (flag) {
			try {
				byte[] buffer = new byte[1];
				int amount = -1;
				String message;
				InputStream socketIn = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(socketIn));
				amount = socketIn.read(buffer);
				if (amount != -1) {
//					System.out.println(buffer[0]);
					switch (buffer[0]) {
					case Constant.RE_HEART:
						client.lastDate = new Date().getTime();
						break;
					case Constant.ACK:
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							responseMesg(message);
						}
						break;
					case Constant.EnMESSAGE:
						message = reader.readLine();

						if (message == null) {
							break;
						} else {
							String sender = "客户端 ";
							String[] temps = message.split(Constant.DIVIDE);
							if (temps.length < 2) {
								continue;
							}
							int id = Integer.parseInt(temps[0]);
							if (id == 0) {
								sender = "服务器 ";
							}
							sender += id + ":      (" + Constant.format.format(new Date()) + ")";

							message = temps[1];

							synchronized (showMessage) {
								showMessage.setText(showMessage.getText() + "\n" + sender + ":\n    解密前：" + message);
							}
							message = new String(EncryptUtil.decrypt(message.getBytes()));
							synchronized (showMessage) {
								showMessage.setText(showMessage.getText() + "\n" + sender + ":\n    解密后：" + message);
							}
						}
						break;
					case Constant.REMESSAGE:// 传输消息
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							String sender = "客户端 ";
							String[] temps = message.split(Constant.DIVIDE);
							if (temps.length < 2) {
								continue;
							}

							int id = Integer.parseInt(temps[0]);
							if (id == 0) {
								sender = "服务器 ";
							}
							sender += id + ":      (" + Constant.format.format(new Date()) + ")";

							message = temps[1];
							synchronized (showMessage) {
								showMessage.setText(showMessage.getText() + "\n" + sender + "\n    " + message);
							}
						}
						break;
					case Constant.ID:
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							client.addItem(Integer.parseInt(message));
						}
						break;
					case Constant.RID:
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							client.removeItem(Integer.parseInt(message));
						}
						break;
					case Constant.IDS:
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							client.initItem(message);
						}
						break;
					case Constant.DISUSE1:
					case Constant.DISUSE2:
						break;
					default:
						System.out.println("没有与:" + buffer[0] + "匹配的");

						break;
					}
				}

			} catch (SocketTimeoutException e) {
				// System.out.println("读取等待超时...");
			} catch (SocketException e) {
				flag = false;
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	// 应答
	private void responseMesg(String colMessage) {
		String[] subs = colMessage.split(Constant.DIVIDE);
		String fileID;

		String req = subs[1];
		switch (req) {
		case Constant.SENDFILE:

			fileID = subs[2];
			String filename = subs[3];
			String fileType = subs[4];
			long fileSize = Long.parseLong(subs[5]);
			int n = JOptionPane.showConfirmDialog(client.getClientFream(),
					"是否接收客户端 " + subs[0] + ",向您发送的文件" + filename + "\n大小为：" + fileSize + "KB" + "?", "确认对话框",
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				client.showMassage("你同意了来自客户端 " + subs[0] + " 向您发送文件" + filename + " 的请求");
				initReceiveFile(Byte.parseByte(fileID), filename, fileType, Integer.parseInt(subs[0]));
				// 发送确认 两个连续的Control代表确认
				String respon = Constant.CONTROL + subs[0] + Constant.DIVIDE + Constant.CONTROL + Constant.CONTROL
						+ Constant.DIVIDE + fileID;
				System.out.println(respon);
				client.sendMassage(respon);
				System.out.println(subs[0]);
			} else if (n == JOptionPane.NO_OPTION) {
				client.sendMassage(
						Constant.CONTROL + subs[0] + Constant.DIVIDE + Constant.REFUSE + Constant.DIVIDE + fileID);
				client.showMassage("你拒绝了来自客户端 " + subs[0] + " 向您发送文件" + filename + " 的请求");
			}
			break;
		case Constant.CONTROL + Constant.CONTROL:
			int sourceID = Integer.parseInt(subs[0]);
			fileID = subs[2];
			System.out.println("fileiD" + fileID);
			// 发送fileID对应的文件
			byte fid = Byte.parseByte(fileID);
			String path = client.getPath(fid);
			client.showMassage("对方同意了接受文件" + path + "！！！");
			// JOptionPane.showMessageDialog(new JFrame(), );
			client.sendFile(new File(client.getPath(fid)), fid, sourceID);
			break;
		case Constant.REFUSE:
			fileID = subs[2];
			byte fileid = Byte.parseByte(fileID);
			// 发送fileID对应的文件
			client.showMassage("对方拒绝了接受文件" + client.getPath(fileid) + "！！！");
			// JOptionPane.showMessageDialog(new JFrame(), );
			client.removeIdToFile(fileid);
			break;
		}
	}

	public void initReceiveFile(byte fid, String fileName, String fileType, int sourceID) {
		// 弹出文件选择框
		JFileChooser chooser = new JFileChooser();

		// 后缀名过滤器
		FileNameExtensionFilter filter = new FileNameExtensionFilter("文件(*" + fileType + ")", fileType);
		chooser.setFileFilter(filter);
		chooser.setSelectedFile(new File(fileName));

		// 下面的方法将阻塞，直到【用户按下保存按钮且“文件名”文本框不为空】或【用户按下取消按钮】
		int option = chooser.showSaveDialog(new javax.swing.JFrame("选择保存位置"));
		if (option == JFileChooser.APPROVE_OPTION) { // 假如用户选择了保存
			File savedFile = chooser.getSelectedFile();

			String fname = chooser.getName(savedFile); // 从文件名输入框中获取文件名

			// 假如用户填写的文件名不带我们制定的后缀名，那么我们给它添上后缀
			int index = fname.lastIndexOf(".");
			String inputType = null;
			if (index > 0 && index < fname.length()) {
				inputType = fname.substring(index);
			}

			if (!fileType.equals(inputType)) {
				if (fileType.equals(".gz")) {
					String trueType = fileName.substring(fileName.lastIndexOf("."));
					if (!trueType.equals(inputType)) {
						fileType = trueType + fileType;
					}
					savedFile = new File(chooser.getCurrentDirectory(), fname + fileType);
				} else {
					savedFile = new File(chooser.getCurrentDirectory(), fname + fileType);
				}
			}

			client.pushReceiveFileToPathMap(sourceID << 8 ^ fid, savedFile.getPath());
			// 创建Socket 准备连接
			client.receiveFile(savedFile, sourceID << 8 ^ fid);
		}
	}
}
