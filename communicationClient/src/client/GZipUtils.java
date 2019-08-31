package client;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;


public class GZipUtils {
	private static final String UTF_8 = "UTF-8";

	/** * @param data * @return */
	public static final byte[] compress(String data) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutputtStream = new GZIPOutputStream(out);
		try {
			gzipOutputtStream.write(data.getBytes(UTF_8));
		} finally {
			closeQuietly(gzipOutputtStream);
		}
		return out.toByteArray();
	}

	/** * @param data * @return */
	public static final byte[] compress(byte[] data) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutputtStream = new GZIPOutputStream(out);
		try {
			gzipOutputtStream.write(data);
		} finally {
			closeQuietly(gzipOutputtStream);
		}
		return out.toByteArray();
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}
}
