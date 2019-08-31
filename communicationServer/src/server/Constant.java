package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Constant ����һЩ����,��ʱֱ�Ӹı伴��
 * 
 * @author Ԭ��
 */
public class Constant {

	private Constant() {
	}
	public final static DateFormat format = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	// ���ڿ��
	public final static int WIDTH = 600;
	// ���ڸ߶�
	public final static int HEIGHT = 700;
	// ����λ�á���x����
	public final static int LOCATION_X = 400;
	// ����λ�á���y����
	public final static int LOCATION_Y = 0;
	public final static byte ACK = 5;
	public final static String CONTROL = (char) 0b00000101 + "";

	public final static String REFUSE = (char) 0b00000111 + "";
	// �ָ��ַ�
	public final static String DIVIDE = (char) 0b00000110 + "";
	public final static String EnMESSAGE ="+";
	public final static byte EnciphermentMESSAGE = 43;
	// �����ļ�����
	public final static String SENDFILE = "0";
	public final static String SENDMESSAGE = " ";
	public final static byte MESSAGE = 32;
	public final static String ID = (char) 0b00001110+"";
	public static final byte RECEIVEFILE = 49;
	public static final byte CONNECT = 0b0001111;
	public static final byte RECONNECT = 0b00100001;//����
	public static final byte HEART = 45;//����
	public final static byte SENDF = 48;
	public static final String IDS = (char) 0b00011111+"";
	public static final String RID = (char) 0b00011110+"";
	public static final byte  DISUSE1 = 10;//����
	public static final byte  DISUSE2 = 13;//����
}
