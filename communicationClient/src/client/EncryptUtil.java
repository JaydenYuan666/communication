package client;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
	// ��Կ�㷨
	private static final String KEY_ALGORITHM = "DES";
	// �����㷨��algorithm/mode/padding �㷨/����ģʽ/���ģʽ
	private static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";
	// ��Կ
	private static final String KEY = "yuanfei0";// DES��Կ���ȱ�����8λ

//	public static void main(String args[]) {
//		String data = "���ܽ���";
//		System.out.println("�������ݣ�" + data);
//		byte[] encryptData = encrypt(data.getBytes());
//		System.out.println("���ܺ�����ݣ�" + new String(encryptData));
//		byte[] decryptData = decrypt(encryptData);
//		System.out.println("���ܺ�����ݣ�" + new String(decryptData));
//	}

	public static byte[] encrypt(byte[] data) {
		// ��ʼ����Կ
		SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] result = cipher.doFinal(data);
			return Base64.getEncoder().encode(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decrypt(byte[] data) {
		byte[] resultBase64 = Base64.getDecoder().decode(data);
		SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);

		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] result = cipher.doFinal(resultBase64);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}