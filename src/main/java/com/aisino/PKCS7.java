package com.aisino;

import static com.google.common.base.Strings.isNullOrEmpty;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aisino.procotocl.util.CaCfg;
import com.google.common.io.Closer;

public final class PKCS7 {
	private final static Logger	LOGGER	= LoggerFactory.getLogger(PKCS7.class);

	// 设置信任链
	public static native boolean setTrusts(String trusts);

	// 设置解密证书
	public static native boolean setDecryptPfx(byte[] decPfx, String passwd);

	// 设置签名证书
	public static native boolean setSignedPfx(byte[] sigPfx, String passwd);

	// 验证证书，成功返回1
	public static native int validateCert(String base64Cert);

	// 打包数字信封，传递加密证书(即接收者的证书)
	public synchronized static native byte[] signedAndEnveloped(String encBase64Cert, byte[] inData);

	// 解包数字信封
	public synchronized static native PKCS7 unpack(byte[] inData);

	// 获取错误码
	public static native int getLastError();

	/**
	 * sigCert、serial、subject、data以下参数 请不要有任何幻想修改，包括你看不惯的命名！！！！！！！！！！！！！！
	 */
	private String	sigCert;	// 签名证书
	private String	serial;	// 证书序列号
	private String	subject;	// 证书主题
	private byte[]	data;		// 原文

	static {
		System.load(CaCfg.CA_DLLPATH);
	}

	public PKCS7() {
	}

	/**
	 * @param trustsBytes
	 *            证书信任链
	 * @param privatePFXBytes
	 *            加密/签名私钥
	 * @param privatePFXKey
	 *            私钥密码
	 * @throws Exception
	 */
	public PKCS7(byte[] trustsBytes, byte[] privatePFXBytes, String privatePFXKey) throws Exception {
		if (!setTrusts(new String(trustsBytes))) {
			throw new Exception("" + getLastError());
		}

		if (!setDecryptPfx(privatePFXBytes, privatePFXKey)) {
			throw new Exception("" + getLastError());
		}

		if (!setSignedPfx(privatePFXBytes, privatePFXKey)) {
			throw new Exception("" + getLastError());
		}
	}

	/**
	 * 依据文件绝对路径, 读取文件
	 * 
	 * @param fileUri
	 *            文件绝对路径
	 * @return byte[] 读取成功的文件字节流
	 */
	private byte[] readFile(String fileUri) {
		final Closer closer = Closer.create();
		try {
			final BufferedInputStream bufferedInputStream = closer.register(new BufferedInputStream(
					new FileInputStream(fileUri)));
			final byte[] bufferedBytes = new byte[bufferedInputStream.available()];

			bufferedInputStream.read(bufferedBytes, 0, bufferedBytes.length);

			return bufferedBytes;
		} catch (IOException e) {
			LOGGER.error("read file ioException:", e.fillInStackTrace());
		} finally {
			try {
				closer.close();
			} catch (IOException e) {
				LOGGER.error("close file ioException:", e.fillInStackTrace());
			}
		}

		return new byte[0];
	}

	/**
	 * 签名加密
	 * 
	 * @param plainContent
	 *            预加密的原文
	 * @param publicPFXBytes
	 *            公钥加/解密证书的绝对路径
	 * @return 加密后的密文数据
	 */
	public static byte[] pkcs7Encrypt(String plainContent, byte[] publicPFXBytes) {
		try {
			final byte[] certBytes = publicPFXBytes;

			if (certBytes == null) {
				throw new Exception("传入参数公钥为NULL,不可用");
			}

			final String encCert = new String(certBytes);
			if (1 != validateCert(encCert)) {// 证书无效
				throw new Exception("" + getLastError());
			}

			if (isNullOrEmpty(plainContent)) {
				throw new Exception("传入参数原文为NULL,不可用");
			}

			return signedAndEnveloped(encCert, plainContent.getBytes(CaCfg.CHARTSET));
		} catch (Exception e) {
			LOGGER.error("pkcs7Encrypt Exception:", e.fillInStackTrace());
		}

		return new byte[0];
	}

	/**
	 * 解密验签
	 * 
	 * @param decodeBase64EncryptTxtBytes
	 *            经过Base64解压后的密文字节流
	 * @return byte[] 经过解密的明文字节流
	 */
	public static byte[] pkcs7Decrypt(byte[] decodeBase64EncryptTxtBytes) {
		byte[] decryptBytes = new byte[0];
		try {
			// 解密
			if (decodeBase64EncryptTxtBytes == null) {
				throw new Exception("传入参数密文为NULL,不可用");
			}

			final PKCS7 pkcs7 = unpack(decodeBase64EncryptTxtBytes);

			if (pkcs7 == null) {
				throw new Exception("" + getLastError());
			}

			decryptBytes = pkcs7.data;
		} catch (Exception e) {
			LOGGER.error("pkcs7Decrypt Exception:", e.fillInStackTrace());
		}

		return decryptBytes;
	}

	/**
	 * 测试入口方法2 2014.12.02 总结： 客户端事件需要 客户端私钥(pfx)、pwd + 平台公钥(cer) 平台端事件需要
	 * 平台端私钥(pfx)、pwd + 客户端公钥(cer)
	 */
//	public static void main(String[] args) throws Exception {
//		// 加密使用的对方加密公钥（客户端调用传入服务端公钥，服务端调用传入客户端公钥）
//		// final String source = "姆神要吃巧克力";
//
//		final String source = "<REQUEST_COMMON_DOWNLOAD_CA class=\"REQUEST_COMMON_DOWNLOAD_CA\">\n"
//				+ "  <DSPTBM>12110302</DSPTBM>\n" + "  <DSPTZCM>51807674</DSPTZCM>\n"
//				+ "  <NSRSBH>510107698868804</NSRSBH>\n" + "  <NSRSQM>1234567890Ko1ztjNt4xGg76Tqxl4rvg==</NSRSQM>\n"
//				+ "</REQUEST_COMMON_DOWNLOAD_CA>";
//
//		LOGGER.info("原文:{}", source);
//
//		final String trustsBytes = CaCfg.CA_CLIENT_TRUSTPATH;
//		String decryptPFXBytes = CaCfg.CA_CLIENT_FPXPATH;
//		String decryptPFXKey = CaCfg.CA_CLIENT_PASSWORD;
//
//		/* 公共信任链OMIT */
//		/* 客户端加密过程 :客户端私钥(pfx)、pwd + 平台公钥(cer) */
//		final PKCS7 pkcs7Client = new PKCS7(FileUtils.readFileToByteArray(new File(trustsBytes)), FileUtils
//				.readFileToByteArray(new File(decryptPFXBytes)), decryptPFXKey);
//		final byte[] encodeData = pkcs7Client.pkcs7Encrypt(source, FileUtils.readFileToByteArray(new File(CaConstant
//				.getProperty("PLATFORM_DECRYPTCER"))));
//
//		String encodeText = new String(Base64.encodeBase64(encodeData));
//		LOGGER.info("客户端加密:{}", encodeText);
//		final String base64EncryptTxt = encodeText;
//
//		/* 平台端解密过程 :平台端私钥(pfx)、pwd */
//		decryptPFXBytes = CaCfg.CA_CLIENT_FPXPATH;
//		decryptPFXKey = CaCfg.CA_CLIENT_PASSWORD;
//		final PKCS7 pkcs7Platform = new PKCS7(FileUtils.readFileToByteArray(new File(trustsBytes)), FileUtils
//				.readFileToByteArray(new File(decryptPFXBytes)), decryptPFXKey);
//
//		final byte[] decodeData = pkcs7Platform.pkcs7Decrypt(Base64.decodeBase64(base64EncryptTxt));
//		LOGGER.info("平台端解密:{}", new String(decodeData));
//
//		/* 平台端加密过程 :平台端私钥(pfx)、pwd + 客户端公钥(cer) */
//		final String source2 = "姆神要吃巧克力";
//		LOGGER.info("平台端加密原文:{}", source2);
//		final byte[] encodeData2 = pkcs7Client.pkcs7Encrypt(source2, FileUtils.readFileToByteArray(new File(CaConstant
//				.getProperty("CLIENT_DECRYPTCER"))));
//		String encodeText2 = new String(Base64.encodeBase64(encodeData2));
//		LOGGER.info("平台端加密:{}", encodeText2);
//
//		/* 客户端解密过程 :*客户端私钥(pfx)、pwd */
//		decryptPFXBytes = CaConstant.getProperty("CLIENT_DECRYPTPFX");
//		decryptPFXKey = CaConstant.getProperty("CLIENT_DECRYPTPFX_KEY");
//		final PKCS7 pkcs7Client2 = new PKCS7(FileUtils.readFileToByteArray(new File(trustsBytes)), FileUtils
//				.readFileToByteArray(new File(decryptPFXBytes)), decryptPFXKey);
//		final String base64EncryptTxt2 = encodeText2;
//		final byte[] decodeData2 = pkcs7Client2.pkcs7Decrypt(Base64.decodeBase64(base64EncryptTxt2));
//		LOGGER.info("客户端解密:{}", new String(decodeData2));
//
//	}

	/* 测试入口方法 @throws Exception 2014-9-4 */
	/*
	 * public static void main(String[] args) throws Exception {
	 * //加密使用的对方加密公钥（客户端调用传入服务端公钥，服务端调用传入客户端公钥） //final String source =
	 * "姆神要吃巧克力";
	 * 
	 * final String
	 * source="<REQUEST_COMMON_DOWNLOAD_CA class=\"REQUEST_COMMON_DOWNLOAD_CA\">\n"
	 * + "  <DSPTBM>12110302</DSPTBM>\n" + "  <DSPTZCM>51807674</DSPTZCM>\n" +
	 * "  <NSRSBH>510107698868804</NSRSBH>\n" +
	 * "  <NSRSQM>1234567890Ko1ztjNt4xGg76Tqxl4rvg==</NSRSQM>\n" +
	 * "</REQUEST_COMMON_DOWNLOAD_CA>";
	 * 
	 * LOGGER.info("原文:{}",source);
	 * 
	 * 
	 * final String trustsBytes = CaConstant.getProperty("TRUSTS"); final String
	 * decryptPFXBytes = CaConstant.getProperty("DECRYPTPFX"); final String
	 * decryptPFXKey = CaConstant.getProperty("DECRYPTPFX_KEY"); final PKCS7
	 * pkcs7 = new PKCS7(FileUtils.readFileToByteArray(new File(trustsBytes)),
	 * FileUtils.readFileToByteArray(new File(decryptPFXBytes)), decryptPFXKey);
	 * 
	 * final byte[] encodeData = pkcs7.pkcs7Encrypt(source,
	 * FileUtils.readFileToByteArray(new
	 * File(CaConstant.getProperty("DECRYPTCER")))); //final byte[] encodeData =
	 * signedAndEnveloped(new
	 * String(read(CaConstant.getProperty("DECRYPTCER"))),
	 * source.getBytes("UTF-8"));
	 * 
	 * String encodeText= new String(Base64.encodeBase64(encodeData));
	 * LOGGER.info("加密:{}",encodeText);
	 * 
	 * //final String base64EncryptTxt =
	 * "MIIDsQYJKoZIhvcNAQcEoIIDojCCA54CAQExgeUwgeICAQAwWzBOMQswCQYDVQQGEwJDTjELMAkGA1UECAwCY2ExCzAJBgNVBAcMAmNhMQswCQYDVQQKDAJjYTELMAkGA1UECwwCY2ExCzAJBgNVBAMMAmNhAgkAtARsgPhh+JwwDQYJKoEcz1UBgi0DBQAEcQQRyIrgTOwbpVTQPVtZcDM6g1hYJsKphd5VINnpNDie+4S1LTRPshqo6jiklAyDMmkrjU2iOTVJIS6v3A8Rylycls/tOUS8D7P1irM5dnH103J/WNUycbkym062FIJ0WRd1IBVPHDcWwMpbXkUdM4v1MQ4wDAYIKoEcz1UBgxEFADA6BgkqhkiG9w0BBwEwGwYHKoEcz1UBaAQQNiW7UMb4rFcCx+3nv9eam4AQEwRmdQRxxCU9QW9/GKJb4aCCAZswggGXMIIBPAIJALQEbID4YficMAoGCCqBHM9VAYN1ME4xCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJjYTELMAkGA1UEBwwCY2ExCzAJBgNVBAoMAmNhMQswCQYDVQQLDAJjYTELMAkGA1UEAwwCY2EwHhcNMTQwMjEyMDIzNzUyWhcNMTUwMjEyMDIzNzUyWjBYMQswCQYDVQQGEwJDTjENMAsGA1UECAwEdGVzdDENMAsGA1UEBwwEdGVzdDENMAsGA1UECgwEdGVzdDENMAsGA1UECwwEdGVzdDENMAsGA1UEAwwEdGVzdDBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABMquDf1MxKMDyY+W+hlkytS9Pj/cScQTaOq5WV4zbxFIwSWvFzPHIavtCrsCr2iqGeNcQ6iKiILF7x9P4nqA9u8wCgYIKoEcz1UBg3UDSQAwRgIhAL/D5yLhlvLe+M6MWN5HcWSKp49uHoqeMnBfc9CLPE1RAiEA2M/wcgBjNd5pTwCLmx5qWOxemYBt/SrcpYrjaLNwDS4xgcUwgcICAQEwWzBOMQswCQYDVQQGEwJDTjELMAkGA1UECAwCY2ExCzAJBgNVBAcMAmNhMQswCQYDVQQKDAJjYTELMAkGA1UECwwCY2ExCzAJBgNVBAMMAmNhAgkAtARsgPhh+JwwDAYIKoEcz1UBgxEFADAKBggqgRzPVQGDdQRGMEQCIDazti/K7lOJnUZuT7cfs8WQe4VepH4Vw6+tG4XjWOlDAiALwSlOJdycktThwOPYC2zSQPStPz2U0sqTrWgHYMjUzg=="
	 * ; final String base64EncryptTxt = encodeText; //final String
	 * base64EncryptTxt=
	 * "MIIEpwYJKoZIhvcNAQcEoIIEmDCCBJQCAQExgeUwgeICAQAwWzBOMQswCQYDVQQGEwJDTjELMAkGA1UECAwCY2ExCzAJBgNVBAcMAmNhMQswCQYDVQQKDAJjYTELMAkGA1UECwwCY2ExCzAJBgNVBAMMAmNhAgkAtARsgPhh+JwwDQYJKoEcz1UBgi0DBQAEcQQRyIrgTOwbpVTQPVtZcDM6g1hYJsKphd5VINnpNDie+4S1LTRPshqo6jiklAyDMmkrjU2iOTVJIS6v3A8RylycroF+96tnsipphRpVjbjILAUlZ0P+nbjMvbnqxzmhtInO5iY2545zGnfAp5gmMrLtMQ4wDAYIKoEcz1UBgxEFADCCASwGCSqGSIb3DQEHATAbBgcqgRzPVQFoBBA0v3XLbanPgDGjX9vcPVFagIIBACs9vgt2O39xmOOsFDQzSoICASgXPTMzyo+DGftrFjakgJSHRW9qDY38XWlnDmJiyfL5WqysQkkKVQFnYhOU6/OGiVRGWDWvXjguKSKl4qTbjMu0iMNiwBMR04gy5Ub8QrIhwOXfsGUSu5WQaXtyNecnxKgYiCbtMTAvtiRqPLNWLaf5ptxB62J6PdppJPb6vmSqsfNm4bXZfH6V2sAC3XwV7yJen1i4QIO1j6dhT+yiesD8BVJM7yBp8UwLuVTYKKqFuYmf3e0Qu7PX7b2c0HKetYRqDr5B6YJxrkrkvMrYyfsrZFNapuQjJGu4LaDXGWeGXBHJ4jSh1Jk2WCcuhy2gggGbMIIBlzCCATwCCQC0BGyA+GH4nDAKBggqgRzPVQGDdTBOMQswCQYDVQQGEwJDTjELMAkGA1UECAwCY2ExCzAJBgNVBAcMAmNhMQswCQYDVQQKDAJjYTELMAkGA1UECwwCY2ExCzAJBgNVBAMMAmNhMB4XDTE0MDIxMjAyMzc1MloXDTE1MDIxMjAyMzc1MlowWDELMAkGA1UEBhMCQ04xDTALBgNVBAgMBHRlc3QxDTALBgNVBAcMBHRlc3QxDTALBgNVBAoMBHRlc3QxDTALBgNVBAsMBHRlc3QxDTALBgNVBAMMBHRlc3QwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAATKrg39TMSjA8mPlvoZZMrUvT4/3EnEE2jquVleM28RSMElrxczxyGr7Qq7Aq9oqhnjXEOoioiCxe8fT+J6gPbvMAoGCCqBHM9VAYN1A0kAMEYCIQC/w+ci4Zby3vjOjFjeR3FkiqePbh6KnjJwX3PQizxNUQIhANjP8HIAYzXeaU8Ai5sealjsXpmAbf0q3KWK42izcA0uMYHHMIHEAgEBMFswTjELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAmNhMQswCQYDVQQHDAJjYTELMAkGA1UECgwCY2ExCzAJBgNVBAsMAmNhMQswCQYDVQQDDAJjYQIJALQEbID4YficMAwGCCqBHM9VAYMRBQAwCgYIKoEcz1UBg3UESDBGAiEAqb2Up51nMek8V8J88M7z3aoo6VdgdcIDxSpHzvu8bA8CIQCdeq1PKfe7OkGcz0bpJQ9prxGxRociME53aEqYsaexEg=="
	 * ; final byte[] decodeData =
	 * pkcs7.pkcs7Decrypt(Base64.decodeBase64(base64EncryptTxt));
	 * LOGGER.info("解密:{}",new String(decodeData)); }
	 */
}
