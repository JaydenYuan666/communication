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
	// ���ڶ�ȡ
	InputStream inStream;
	// ��������ļ�
	RandomAccessFile inFile = null;
	// ��ʱ������
	byte byteBuffer[] = new byte[1024];

	private Integer sotimeout = 1 * 1 * 10000;// ��ʱʱ�䣬�Ժ���Ϊ��λ 10��

	String[] temps;
	int id;
	String head = "\n�ͻ���";

	public ReceiveMessage(Server server, Socket socket, JTextArea showMessage, int clientId) {
		this.server = server;
		this.showMessage = showMessage;
		flag = true;
		this.clientSocket = socket;
		this.clientId = clientId;
		try {
			socket.setKeepAlive(true);// �������ֻ״̬���׽���
			socket.setSoTimeout(sotimeout);// ���ó�ʱʱ��
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// ͷ��Э��[������id,Ŀ��id,�������ǳ�,��Ϣ����(��Ӧ[r],����,ͼƬ,�ļ�,�������ļ�),/*�м���0b11111111�ָ�*/
	// ��������ļ����ļ�id,�ļ�����,�ļ���С]/*�м���0b11111111�ָ�*/
	// ���������Ϣ����Ϣ����
	// ���������Ӧ��Ϣ��������[c]��ȷ�Ϻʹ���,����)
	// ��������� ���id
	// ����һ��ȷ��֡��[������id,��Ӧ��Ϣ,(��Ϣ����)�Ƿ����,ȷ���ļ�id]
	// ����Э��,������id,�ļ�id,��Ϣ����(����),

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
						// ������
//						System.out.println("���յ�һ��������");
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
						System.out.println("������Ϣ");
						message = reader.readLine();
						temps = message.split(Constant.DIVIDE);
						if (temps.length < 2) {
							continue;
						}

						id = Integer.parseInt(temps[0]);// Ŀ��id

						message = temps[1];

						if (message == null) {
							break;
						} else {
							String temp = head + clientId + ":      (" + Constant.format.format(new Date()) + ")\n    ";
							if (id == 0) {
								synchronized (showMessage) {
									showMessage.setText(showMessage.getText() + temp + "����ǰ��" + message);
								}
								message = new String(EncryptUtil.decrypt(message.getBytes()));
								synchronized (showMessage) {
									showMessage.setText(showMessage.getText() + temp + "���ܺ�" + message);
								}
							} else {
								sendENMessage(message, id);
								System.out.println("ת����Ϣ");
							}
						}
						break;
					case Constant.MESSAGE:// ������Ϣ
						message = reader.readLine();

						temps = message.split(Constant.DIVIDE);
						if (temps.length < 2) {
							continue;
						}

						id = Integer.parseInt(temps[0]);// Ŀ��id
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
							// ת��
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

				// ͷ��Э��[������ַ,�������ǳ�,��Ϣ����(��Ӧ��ȷ�Ϻʹ��䣩,����,ͼƬ,�ļ�,�������ļ�),/*�м���0b11111111�ָ�*/
				// ��������ļ����ļ�id,�ļ�����,�ļ���С]/*�м���0b11111111�ָ�*/
				// ���������Ϣ����Ϣ����
				// ����һ��ȷ��֡��[������id,��Ӧ��Ϣ,(��Ϣ����)�Ƿ����,ȷ���ļ�id]
				// ����Э��,������id,�ļ�id,��Ϣ����(����),

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

	// Ӧ��
	private void responseMesg(String colMessage) {
		System.out.println("ser������" + colMessage);
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
					"�Ƿ���տͻ��� " + clientId + ",�������͵��ļ�" + filename + "\n��СΪ��" + fileSize + "KB" + "?", "ȷ�϶Ի���",
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				if(initReceiveFile(Byte.parseByte(fileID), filename, fileType)) {
					// ����ȷ�� ����������Control����ȷ��
					sendMessage(Constant.CONTROL + server.getId() + Constant.DIVIDE + Constant.CONTROL + ""
							+ Constant.CONTROL + Constant.DIVIDE + fileID);
					server.showMassage("��ͬ�������Կͻ��� " + clientId + " ���������ļ�" + filename + " ������" + "      ("
							+ Constant.format.format(new Date()) + ")");
				}else {
					sendMessage(Constant.CONTROL + server.getId() + Constant.DIVIDE + Constant.REFUSE + Constant.DIVIDE
							+ fileID);
					server.showMassage("��ȡ�������Կͻ��� " + clientId + " ���������ļ�" + filename + " ������" + "      ("
							+ Constant.format.format(new Date()) + ")");
				}
				
			} else if (n == JOptionPane.NO_OPTION) {
				sendMessage(Constant.CONTROL + server.getId() + Constant.DIVIDE + Constant.REFUSE + Constant.DIVIDE
						+ fileID);
				server.showMassage("��ܾ������Կͻ��� " + clientId + " ���������ļ�" + filename + " ������" + "      ("
						+ Constant.format.format(new Date()) + ")");
			}
			break;
		case Constant.CONTROL + Constant.CONTROL:
		case Constant.REFUSE:
			// ת��
			sendMessage(Constant.CONTROL + colMessage.replaceFirst(subs[0], clientId + ""), targetID, false);
			break;
		default:
			System.out.println(req);
			System.out.println(colMessage);
			break;
		}
	}

	public boolean initReceiveFile(byte fid, String fileName, String fileType) {
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

			server.addItemToidFileMap(((clientId << 8) ^ fid), savedFile);

		}else {
			//ѡ����ȡ��
			return false;
		}
		return true;
	}

}
