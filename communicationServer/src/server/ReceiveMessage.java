package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ReceiveMessage implements Runnable {
	private Socket clientSocket;
	private JTextArea showMessage;
	private boolean flag;
	private int clientId;
	Server server;
	// 用于读取
	InputStream inStream;
	// 随机访问文件
	RandomAccessFile inFile = null;
	// 临时缓寸区
	byte byteBuffer[] = new byte[1024];

	private Integer sotimeout = 1 * 1 * 10000;// 超时时间，以毫秒为单位 10秒

	String[] temps;
	int id;
	String head = "\n客户端";

	public ReceiveMessage(Server server, Socket socket, JTextArea showMessage, int clientId) {
		this.server = server;
		this.showMessage = showMessage;
		flag = true;
		this.clientSocket = socket;
		this.clientId = clientId;
		try {
			socket.setKeepAlive(true);// 开启保持活动状态的套接字
			socket.setSoTimeout(sotimeout);// 设置超时时间
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// 头部协议[发送者id,目标id,发送者昵称,消息类型(响应[r],文字,图片,文件,二进制文件),/*中间用0b11111111分隔*/
	// （如果是文件）文件id,文件类型,文件大小]/*中间用0b11111111分隔*/
	// （如果是消息）消息内容
	// （如果是响应消息）（连接[c]，确认和传输,结束)
	// 如果是连接 后跟id
	// 回馈一个确认帧，[发送者id,响应消息,(消息类型)是否接受,确认文件id]
	// 结束协议,发送者id,文件id,消息类型(结束),

	@Override
	public void run() {

		while (flag) {
			try {

				String message;
				InputStream socketIn = clientSocket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(socketIn));

				byte control = (byte) socketIn.read();
				if (control != -1) {

					switch (control) {
					case Constant.HEART:
						// 心跳包
//						System.out.println("接收到一个心跳包");
						sendMessage('-' + "");
						break;
					case Constant.ACK:
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							responseMesg(message);
						}
						break;
					case Constant.EnciphermentMESSAGE:
						System.out.println("加密消息");
						message = reader.readLine();
						temps = message.split(Constant.DIVIDE);
						if (temps.length < 2) {
							continue;
						}

						id = Integer.parseInt(temps[0]);// 目标id

						message = temps[1];

						if (message == null) {
							break;
						} else {
							String temp = head + clientId + ":      (" + Constant.format.format(new Date()) + ")\n    ";
							if (id == 0) {
								synchronized (showMessage) {
									showMessage.setText(showMessage.getText() + temp + "解密前：" + message);
								}
								message = new String(EncryptUtil.decrypt(message.getBytes()));
								synchronized (showMessage) {
									showMessage.setText(showMessage.getText() + temp + "解密后：" + message);
								}
							} else {
								sendENMessage(message, id);
								System.out.println("转发消息");
							}
						}
						break;
					case Constant.MESSAGE:// 传输消息
						message = reader.readLine();

						temps = message.split(Constant.DIVIDE);
						if (temps.length < 2) {
							continue;
						}

						id = Integer.parseInt(temps[0]);// 目标id
						message = temps[1];

						if (id == 0) {

							if (message == null) {
								break;
							} else {
								String temp = head + clientId + ":      (" + Constant.format.format(new Date())
										+ ")\n    ";
								synchronized (showMessage) {
									showMessage.setText(showMessage.getText() + temp + message);
								}
							}
						} else {
							// 转发
							sendMessage(message, id, true);
						}
						break;
					case Constant.DISUSE1:
					case Constant.DISUSE2:
						break;
					default:
						System.out.println("" + control);
						break;
					}
				} else {
					flag = false;
					clientSocket.close();
				}

				// 头部协议[主机地址,发送者昵称,消息类型(响应（确认和传输）,文字,图片,文件,二进制文件),/*中间用0b11111111分隔*/
				// （如果是文件）文件id,文件类型,文件大小]/*中间用0b11111111分隔*/
				// （如果是消息）消息内容
				// 回馈一个确认帧，[发送者id,响应消息,(消息类型)是否接受,确认文件id]
				// 结束协议,发送者id,文件id,消息类型(结束),

			} catch (SocketTimeoutException e) {
				flag = false;
			} catch (SocketException e) {
				flag = false;
			} catch (IOException e) {
				flag = false;
			}
		}
		server.deleteItem(clientId);
		server.removeIdToSocketItem(clientId);
	}

	public void sendMessage(String message, int target, boolean isMessage) {
		if (isMessage) {
			server.sendMassage(clientId, message, target);
		} else {
			server.sendMassage(message, target);
		}
	}

	public void sendENMessage(String message, int target) {
		server.sendENMassage(clientId, message, target);
	}

	public void sendMessage(String message) {

		server.sendMassage(message, clientSocket);
	}

	// 应答
	private void responseMesg(String colMessage) {
		System.out.println("ser顶部：" + colMessage);
		String[] subs = colMessage.split(Constant.DIVIDE);
		int targetID = Integer.parseInt(subs[0]);

		String req = subs[1];

		switch (req) {
		case Constant.SENDFILE:
			if (targetID != server.getId()) {
				sendMessage(Constant.CONTROL + colMessage.replaceFirst(subs[0], clientId + ""), targetID, false);

				return;
			}

			String fileID = subs[2];
			String filename = subs[3];
			String fileType = subs[4];
			long fileSize = Long.parseLong(subs[5]);
			int n = JOptionPane.showConfirmDialog(server.getServerFream(),
					"是否接收客户端 " + clientId + ",向您发送的文件" + filename + "\n大小为：" + fileSize + "KB" + "?", "确认对话框",
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				if(initReceiveFile(Byte.parseByte(fileID), filename, fileType)) {
					// 发送确认 两个连续的Control代表确认
					sendMessage(Constant.CONTROL + server.getId() + Constant.DIVIDE + Constant.CONTROL + ""
							+ Constant.CONTROL + Constant.DIVIDE + fileID);
					server.showMassage("你同意了来自客户端 " + clientId + " 向您发送文件" + filename + " 的请求" + "      ("
							+ Constant.format.format(new Date()) + ")");
				}else {
					sendMessage(Constant.CONTROL + server.getId() + Constant.DIVIDE + Constant.REFUSE + Constant.DIVIDE
							+ fileID);
					server.showMassage("你取消了来自客户端 " + clientId + " 向您发送文件" + filename + " 的请求" + "      ("
							+ Constant.format.format(new Date()) + ")");
				}
				
			} else if (n == JOptionPane.NO_OPTION) {
				sendMessage(Constant.CONTROL + server.getId() + Constant.DIVIDE + Constant.REFUSE + Constant.DIVIDE
						+ fileID);
				server.showMassage("你拒绝了来自客户端 " + clientId + " 向您发送文件" + filename + " 的请求" + "      ("
						+ Constant.format.format(new Date()) + ")");
			}
			break;
		case Constant.CONTROL + Constant.CONTROL:
		case Constant.REFUSE:
			// 转发
			sendMessage(Constant.CONTROL + colMessage.replaceFirst(subs[0], clientId + ""), targetID, false);
			break;
		default:
			System.out.println(req);
			System.out.println(colMessage);
			break;
		}
	}

	public boolean initReceiveFile(byte fid, String fileName, String fileType) {
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

			server.addItemToidFileMap(((clientId << 8) ^ fid), savedFile);

		}else {
			//选择了取消
			return false;
		}
		return true;
	}

}
