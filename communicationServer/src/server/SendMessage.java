package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SendMessage  implements Runnable {

	// 发送的消息
	private String massage;
	// 临时套接字
	private Socket tempSocket;

	public SendMessage(String massage, Socket tempSocket) {
		this.massage = massage;
		this.tempSocket = tempSocket;
	}

	/**
	 * 先决条件是服务器端先开启
	 * 
	 */
	public void run() {
		// 头部协议[发送者id,发送者昵称,消息类型(响应（确认和传输）,文字,图片,文件,二进制文件),/*中间用0b11111111分隔*/
		// （如果是文件）文件id,文件类型,文件大小]/*中间用0b11111111分隔*/
		// （如果是消息）消息内容
		//
		// 回馈一个确认帧，[发送者id,响应消息,(消息类型)是否接受,确认文件id]
		// 结束协议,发送者id,文件id,消息类型(结束),
		OutputStream socketOut;
		try {
			socketOut = tempSocket.getOutputStream();
			PrintWriter writer = new PrintWriter(socketOut);
			writer.println(massage);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
