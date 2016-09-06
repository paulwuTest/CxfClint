package com.aisino.procotocl;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aisino.common.util.XMLShellFactory;
import com.aisino.procotocl.util.ProXml;

public class SecondLayerXml {
	private Logger log = LoggerFactory.getLogger(SecondLayerXml.class);
	public static SecondLayerXml secondLayerXml;
	public static SecondLayerXml instantiation(){
		if(secondLayerXml == null){
			return new SecondLayerXml();
		}
		return secondLayerXml;
	}
	
	public ByteArrayOutputStream makeSecondLayerXml(Object reqObj){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLShellFactory.newInstance().saveXml(out, reqObj);
		return out;
	}
	
	
	public Object praseSecondLayerXml(String secondXml) throws Exception{
		Object resObj = null;
		if(secondXml != null){
			try{
				resObj = ProXml.getDataRoot(secondXml).get(0);
			}catch (Exception e) {
				log.error("解析报文失败",e);
			}
		}else{
			log.error("第二层协议为空，解析失败");
			throw new Exception("第二层协议为空，解析失败");
		}
		return resObj;
	}
}
