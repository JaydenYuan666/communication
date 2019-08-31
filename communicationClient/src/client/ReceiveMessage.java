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
	 * �Ⱦ������Ƿ��������ȿ���
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
							String sender = "�ͻ��� ";
							String[] temps = message.split(Constant.DIVIDE);
							if (temps.length < 2) {
								continue;
							}
							int id = Integer.parseInt(temps[0]);
							if (id == 0) {
								sender = "������ ";
							}
							sender += id + ":      (" + Constant.format.format(new Date()) + ")";

							message = temps[1];

							synchronized (showMessage) {
								showMessage.setText(showMessage.getText() + "\n" + sender + ":\n    ����ǰ��" + message);
							}
							message = new String(EncryptUtil.decrypt(message.getBytes()));
							synchronized (showMessage) {
								showMessage.setText(showMessage.getText() + "\n" + sender + ":\n    ���ܺ�" + message);
							}
						}
						break;
					case Constant.REMESSAGE:// ������Ϣ
						message = reader.readLine();
						if (message == null) {
							break;
						} else {
							String sender = "�ͻ��� ";
							String[] temps = message.split(Constant.DIVIDE);
							if (temps.length < 2) {
								continue;
							}

							int id = Integer.parseInt(temps[0]);
							if (id == 0) {
								sender = "������ ";
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
						System.out.println("û����:" + buffer[0] + "ƥ���");

						break;
					}
				}

			} catch (SocketTimeoutException e) {
				// System.out.println("��ȡ�ȴ���ʱ...");
			} catch (SocketException e) {
				flag = false;
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	// Ӧ��
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
					"�Ƿ���տͻ��� " + subs[0] + ",�������͵��ļ�" + filename + "\n��СΪ��" + fileSize + "KB" + "?", "ȷ�϶Ի���",
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				client.showMassage("��ͬ�������Կͻ��� " + subs[0] + " ���������ļ�" + filename + " ������");
				initReceiveFile(Byte.parseByte(fileID), filename, fileType, Integer.parseInt(subs[0]));
				// ����ȷ�� ����������Control����ȷ��
				String respon = Constant.CONTROL + subs[0] + Constant.DIVIDE + Constant.CONTROL + Constant.CONTROL
						+ Constant.DIVIDE + fileID;
				System.out.println(respon);
				client.sendMassage(respon);
				System.out.println(subs[0]);
			} else if (n == JOptionPane.NO_OPTION) {
				client.sendMassage(
						Constant.CONTROL + subs[0] + Constant.DIVIDE + Constant.REFUSE + Constant.DIVIDE + fileID);
				client.showMassage("��ܾ������Կͻ��� " + subs[0] + " ���������ļ�" + filename + " ������");
			}
			break;
		case Constant.CONTROL + Constant.CONTROL:
			int sourceID = Integer.parseInt(subs[0]);
			fileID = subs[2];
			System.out.println("fileiD" + fileID);
			// ����fileID��Ӧ���ļ�
			byte fid = Byte.parseByte(fileID);
			String path = client.getPath(fid);
			client.showMassage("�Է�ͬ���˽����ļ�" + path + "������");
			// JOptionPane.showMessageDialog(new JFrame(), );
			client.sendFile(new File(client.getPath(fid)), fid, sourceID);
			break;
		case Constant.REFUSE:
			fileID = subs[2];
			byte fileid = Byte.parseByte(fileID);
			// ����fileID��Ӧ���ļ�
			client.showMassage("�Է��ܾ��˽����ļ�" + client.getPath(fileid) + "������");
			// JOptionPane.showMessageDialog(new JFrame(), );
			client.removeIdToFile(fileid);
			break;
		}
	}

	public void initReceiveFile(byte fid, String fileName, String fileType, int sourceID) {
		// �����ļ�ѡ���
		JFileChooser chooser = new JFileChooser();

		// ��׺��������
		FileNameExtensionFilter filter = new FileNameExtensionFilter("�ļ�(*" + fileType + ")", fileType);
		chooser.setFileFilter(filter);
		chooser.setSelectedFile(new File(fileName));

		// ����ķ�����������ֱ�����û����±��水ť�ҡ��ļ������ı���Ϊ�ա����û�����ȡ����ť��
		int option = chooser.showSaveDialog(new javax.swing.JFrame("ѡ�񱣴�λ��"));
		if (option == JFileChooser.APPROVE_OPTION) { // �����û�ѡ���˱���
			File savedFile = chooser.getSelectedFile();

			String fname = chooser.getName(savedFile); // ���ļ���������л�ȡ�ļ���

			// �����û���д���ļ������������ƶ��ĺ�׺������ô���Ǹ������Ϻ�׺
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
			// ����Socket ׼������
			client.receiveFile(savedFile, sourceID << 8 ^ fid);
		}
	}
}
