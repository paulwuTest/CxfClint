package com.aisino.procotocl;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aisino.procotocl.outbean.Data;
import com.aisino.procotocl.outbean.GlobalInfo;
import com.aisino.procotocl.outbean.ReturnStateInfo;
import com.aisino.procotocl.util.ProXml;
import com.aisino.procotocl.util.XmlPar;

public class FirstLayerXml {
	private Logger log = LoggerFactory.getLogger(FirstLayerXml.class);
	public static FirstLayerXml firstLayerXml;
	
	public static FirstLayerXml instantiation(){
		if(firstLayerXml == null){
			return new FirstLayerXml();
		}
		return firstLayerXml;
	}
	
	public String makeFirstLayerXml(GlobalInfo glo,Object reqObj){
		ReturnStateInfo rs = ProXml.getReturnStateInfo(XmlPar.BUSI_SSUCCESS, "");
		ByteArrayOutputStream out = SecondLayerXml.instantiation().makeSecondLayerXml(reqObj);
		Data data = ProXml.getData(out);
		String reqXml =	ProXml.getXml(glo, rs, data);
		return reqXml;
	}
	
	public Map<String,Object> praseFirstLayerXml(String resXml){
		Map<String,Object> map = new HashMap<String, Object>();
		if(resXml != null){
			Map<String,Object> interFaceMap = ProXml.getInterface(resXml,true);
			ReturnStateInfo rs = (ReturnStateInfo)interFaceMap.get(XmlPar.RETURNSTATEINFO);
			boolean b = checkIsNull(rs);
			if(!b){
				if(rs.getReturnCode().equals(XmlPar.BUSI_SSUCCESS)){
					Data data = (Data)interFaceMap.get(XmlPar.DATA);
					b = checkIsNull(data);
					if(!b){
						try {
							Object resObj =  SecondLayerXml.instantiation().praseSecondLayerXml(data.getContent());
							log.info("解析报文成功");
							map.put(XmlPar.RETURNCODE, XmlPar.BUSI_SSUCCESS);
							map.put(XmlPar.RETURNMESSAGE, "外层协议返回成功，但data对象为空");
							map.put(XmlPar.DATA, resObj);
						} catch (Exception e) {
							log.error("外层返回成功，但内层报文解析失败");
							map.put(XmlPar.RETURNCODE, XmlPar.BUSI_SSUCCESS);
							map.put(XmlPar.RETURNMESSAGE, "外层返回成功，但内层报文解析失败");
						}
					}else{
						log.warn("外层协议返回成功，但data对象为空");
						map.put(XmlPar.RETURNCODE, XmlPar.BUSI_SSUCCESS);
						map.put(XmlPar.RETURNMESSAGE, "外层协议返回成功，但data对象为空");
					}
				}else{
					map.put(XmlPar.RETURNCODE, rs.getReturnCode());
					map.put(XmlPar.RETURNMESSAGE, rs.getReturnMessage());
				}
			}else{
				log.error("解析应答报文 returnstateinfo 为空");
				map.put(XmlPar.RETURNCODE, XmlPar.BUSI_FAIL);
				map.put(XmlPar.RETURNMESSAGE, "应答报文为空");
			}
		}else{
			map.put(XmlPar.RETURNCODE, XmlPar.BUSI_FAIL);
			map.put(XmlPar.RETURNMESSAGE, "应答报文为空");
		}
		return map;
	}
	
	
	private boolean checkIsNull(Object obj){
		if(obj == null){
			return true;
		}
		return false;
	}
}
