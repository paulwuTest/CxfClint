package com.aisino.procotocl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaCfg {
	private static Logger log = LoggerFactory.getLogger(CaCfg.class);
	public static String CHARTSET="";
	public static String CA_DLLPATH="";
	public static String CA_SERVER_PATH="";
	public static String CA_CLIENT_FPXPATH="";
	public static String CA_CLIENT_PUBILCKEYPATH="";
	public static String CA_CLIENT_TRUSTPATH="";
	public static String CA_CLIENT_PASSWORD="";
	public static int PROTOCOLPASSWORDSJSLENGTH=10;
	public static String PROTOCOL_ENCRYPTCODE="";
	public static final String PROTOCOL_CA="2";
	
	
	static{
		Properties pro = new Properties();
		InputStream ins = null;
		try{
			ins = CaCfg.class.getResourceAsStream("/caCfg.properties");
			pro.load(ins);
			CHARTSET = "UTF-8";
			CA_DLLPATH = pro.getProperty("APPLICATION.CA.DLL.PATH");
			CA_SERVER_PATH = pro.getProperty("APPLICATION.SERVER.CA.PUBLICKEYPATH");
			CA_CLIENT_FPXPATH = pro.getProperty("APPLICATION.CLIENT.CA.PFXKEYPATH");
			CA_CLIENT_PUBILCKEYPATH = pro.getProperty("APPLICATION.CLIENT.CA.PUBLICKEYPATH");
			CA_CLIENT_TRUSTPATH = pro.getProperty("APPLICATION.CLIENT.CA.TRUSTPATH");
			CA_CLIENT_PASSWORD = pro.getProperty("APPLICATION.CLIENT.CA.PASSWORD");
			PROTOCOL_ENCRYPTCODE =pro.getProperty("APPLICATION.DEFAULT.ENCRYPTCODE");
		}catch (Exception e) {
			log.error("初始化协议处理配置失败",e);
		}finally{
			if(ins!=null){
				try {
					ins.close();
				} catch (IOException e) {
					log.error("关闭初始化协议处理配置失败流失败",e);
				}
			}
		}
	}
}
