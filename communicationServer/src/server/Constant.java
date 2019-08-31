package server;

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
	public final static int WIDTH = 600;
	// 窗口高度
	public final static int HEIGHT = 700;
	// 窗口位置——x坐标
	public final static int LOCATION_X = 400;
	// 窗口位置——y坐标
	public final static int LOCATION_Y = 0;
	public final static byte ACK = 5;
	public final static String CONTROL = (char) 0b00000101 + "";

	public final static String REFUSE = (char) 0b00000111 + "";
	// 分隔字符
	public final static String DIVIDE = (char) 0b00000110 + "";
	public final static String EnMESSAGE ="+";
	public final static byte EnciphermentMESSAGE = 43;
	// 发送文件请求
	public final static String SENDFILE = "0";
	public final static String SENDMESSAGE = " ";
	public final static byte MESSAGE = 32;
	public final static String ID = (char) 0b00001110+"";
	public static final byte RECEIVEFILE = 49;
	public static final byte CONNECT = 0b0001111;
	public static final byte RECONNECT = 0b00100001;//重连
	public static final byte HEART = 45;//心跳
	public final static byte SENDF = 48;
	public static final String IDS = (char) 0b00011111+"";
	public static final String RID = (char) 0b00011110+"";
	public static final byte  DISUSE1 = 10;//弃用
	public static final byte  DISUSE2 = 13;//弃用
}
