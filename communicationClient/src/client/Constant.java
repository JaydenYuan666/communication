package client;

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
	public final static int WIDTH = 515;
	// ���ڸ߶�
	public final static int HEIGHT = 680;
//	// ����λ�á���x����
//	public final static int LOCATION_X = 400;
//	// ����λ�á���y����
//	public final static int LOCATION_Y = 0;

	public final static String CONTROL = (char) 0b00000101 + "";

	public final static String MESSAGE = " ";
	public final static String EnciphermentMESSAGE="+";
	// �ָ�
	public final static String DIVIDE = (char) 0b00000110 + "";
	// �����ļ��ַ�
	public final static byte SENDING = 0b00000110;
	// ���������ļ��ַ�
	public final static String ENDSENDING = (char) 0b00000111 + "";
	// �����ļ�����
	public final static String SENDFILE = "0";

	public final static String SERVERID = "0";

	public final static String REFUSE = (char) 0b00000111 + "";
	public final static int BUFFER = 1024;
	
	public final static byte ACK = 5;
	public final static byte REMESSAGE = 32;
	public final static byte EnMESSAGE = 43;
	public final static byte ID = 0b00001110;
	public static final byte RID = 0b00011110;
	public static final byte IDS = 0b00011111;
	public static final String RECEIVEFILE = "1";
	public static final String CONNECT = (char)0b00001111+"";
	public static final String RECONNECT = (char)0b00100001+"";//����
	public static final String HEART = "-";//����
	
	public static final byte RE_HEART = 45;//�յ�������
	public static final byte  DISUSE1 = 10;//����
	public static final byte  DISUSE2 = 13;//����
}
