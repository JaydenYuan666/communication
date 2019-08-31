package client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Constant 定义一些常量,用时直接改变即可
 * 
 * @author 袁飞
 */
public class Constant {

	
	private Constant() {
	}
	
	public final static DateFormat format = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	// 窗口宽度
	public final static int WIDTH = 515;
	// 窗口高度
	public final static int HEIGHT = 680;
//	// 窗口位置——x坐标
//	public final static int LOCATION_X = 400;
//	// 窗口位置——y坐标
//	public final static int LOCATION_Y = 0;

	public final static String CONTROL = (char) 0b00000101 + "";

	public final static String MESSAGE = " ";
	public final static String EnciphermentMESSAGE="+";
	// 分隔
	public final static String DIVIDE = (char) 0b00000110 + "";
	// 传输文件字符
	public final static byte SENDING = 0b00000110;
	// 结束传输文件字符
	public final static String ENDSENDING = (char) 0b00000111 + "";
	// 发送文件请求
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
	public static final String RECONNECT = (char)0b00100001+"";//重连
	public static final String HEART = "-";//心跳
	
	public static final byte RE_HEART = 45;//收到的心跳
	public static final byte  DISUSE1 = 10;//弃用
	public static final byte  DISUSE2 = 13;//弃用
}
