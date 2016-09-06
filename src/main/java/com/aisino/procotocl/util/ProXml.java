/**
 * 文件名：ProXml.java
 *
 * 创建人：张士锋
 *
 * 创建时间：Jul 6, 2012 10:16:03 AM
 *
 * 版权所有：航天信息股份有限公司
 */
package com.aisino.procotocl.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;

import com.aisino.PKCS7;
import com.aisino.common.util.XMLShellFactory;
import com.aisino.procotocl.outbean.Data;
import com.aisino.procotocl.outbean.GlobalInfo;
import com.aisino.procotocl.outbean.ReturnStateInfo;

/**
 * <p>
 * 处理xml数据，转码，打zip包,生成xml
 * </p>
 * @author 张士锋
 * @version 1.0 Created on Jul 6, 2012 10:16:03 AM
 */
public class ProXml {
	private static Logger log = LoggerFactory.getLogger(ProXml.class);
	/**
	 * 
	 * <p>
	 * 转码
	 * </p>
	 * @param string
	 * @return String
	 * @author: 张士锋 
	 * @date: Created on Jul 6, 2012 12:52:12 PM
	 */
	public static String encode(String res){
		try{
		Base64 base = new Base64();
		return new String(base.encode(res.getBytes("UTF-8")));
		}catch (Exception e) {
			log.error("未知：" + e);
			return "";
		}
	}
	
	public static byte[] encode(byte[] res){
		try{
		Base64 base = new Base64();
		return base.encode(res);
		}catch (Exception e) {
			log.error("未知：" + e);
			return null;
		}
	}

	/**
	 * 
	 * <p>
	 * 解码
	 * </p>
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 *             byte[]
	 * @author: 张士锋
	 * @date: Created on Jul 6, 2012 2:25:06 PM
	 */
	public static String decode(String str) throws UnsupportedEncodingException {
		return new String(new Base64().decode(str.getBytes()),"UTF-8");
	}
	
	/**
	 * 
	 * <p>
	 * 解码
	 * </p>
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 *             byte[]
	 * @author: 张士锋
	 * @date: Created on Jul 6, 2012 2:25:06 PM
	 */
	public static byte[] decode(byte[] str) throws UnsupportedEncodingException {
		return new Base64().decode(str);
	}
	
	/**
	 * 
	 * <p>
	 * 解码
	 * </p>
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 *             byte[]
	 * @author: 张士锋
	 * @throws IOException 
	 * @date: Created on Jul 6, 2012 2:25:06 PM
	 */
	public static byte[] decodeBuffer(String str) throws IOException {
		return new BASE64Decoder().decodeBuffer(str);
	}
	
	/**
	 * 
	 * <p>
	 * 传输数据与固定值比较大小
	 * </p>
	 * 
	 * @param byteRsp
	 * @param size
	 * @return boolean
	 * @author: 张士锋
	 * @date: Created on Jul 6, 2012 2:29:15 PM
	 */
	public static String isZip(String xml, int size) {
		if(xml == null){
			xml = "";
		}
		String isz = "0";
		if (xml.getBytes().length > 1024 * size){
			isz = "1";
		}
		return isz;
	}

	/**
	 * 
	 * <p>
	 * 根据原始生成传输数据 
	 * </p>
	 * 
	 * @param data
	 * @param globalInfo
	 * @param responseStateInfo
	 * @param content
	 * @return String
	 * @author: 张士锋
	 * @throws UnsupportedEncodingException 
	 * @date: Created on Jul 6, 2012 2:25:57 PM
	 */

	public static String getXml(GlobalInfo globalInfo,ReturnStateInfo returnStateInfo, Data data){
		return getXml(globalInfo, returnStateInfo, data, true);
	}
	/**
	 * 业务数据已加密，只封装外层xml
	 * @param globalInfo
	 * @param returnStateInfo
	 * @param data
	 * @return
	 */
	public static String getXmlOuter(GlobalInfo globalInfo,ReturnStateInfo returnStateInfo, Data data){
		return getXml(globalInfo, returnStateInfo, data, false);
	}
	
	
	public static String getTransXml(GlobalInfo globalInfo,ReturnStateInfo returnStateInfo, Data data){
		String resData = "";
		ByteArrayOutputStream byteRsp = null;
		try {
		Element root = new Element(XmlPar.ROOT_BASE);
		Namespace ns = Namespace.getNamespace(XmlPar.NS_ONE);
		root.addNamespaceDeclaration(ns);
		Namespace ns1 = Namespace.getNamespace("xsi", XmlPar.NS_TWO);
		root.addNamespaceDeclaration(ns1);
		Namespace ns2 = Namespace.getNamespace("schemaLocation",XmlPar.NS_THREE);
		root.addNamespaceDeclaration(ns2);
		Document doc = new Document(root);
		Element eRoot = doc.getRootElement();
		eRoot.setAttribute("version", XmlPar.VERSION_NO);
	
		// 全局信息
		Element eGlobalInfo = new Element(XmlPar.GLOBALINFO);
		Element terminalCode = new Element(XmlPar.TERMINALCODE);
		terminalCode.setText("0");
		eGlobalInfo.addContent(terminalCode);
		Element eAppID = new Element(XmlPar.APPID);
		eAppID.setText(globalInfo.getAppId());
		eGlobalInfo.addContent(eAppID);
		Element version = new Element(XmlPar.VERSION);
		version.setText("2.0");
		eGlobalInfo.addContent(version);
		Element eInterfaceCode;
		eInterfaceCode = new Element(XmlPar.INTERFACECODE);
		eInterfaceCode.setText(globalInfo.getInterfaceCode());
		eGlobalInfo.addContent(eInterfaceCode);
		Element ERequestCode = new Element(XmlPar.REQUESTCODE);
		ERequestCode.setText(globalInfo.getRequestCode());
		eGlobalInfo.addContent(ERequestCode);
		Element eRequestTime = new Element(XmlPar.REQUESTTIME);
		eRequestTime.setText(globalInfo.getRequestTime());
		eGlobalInfo.addContent(eRequestTime);
		Element eResponseCode;
		eResponseCode = new Element(XmlPar.RESPONSECODE);
		eResponseCode.setText(globalInfo.getResponseCode());
		eGlobalInfo.addContent(eResponseCode);
		Element eDataExchangeId = new Element(XmlPar.DATAEXCHANGEID);
		eDataExchangeId.setText(globalInfo.getDataExchangeId());
		eGlobalInfo.addContent(eDataExchangeId);
		Element eUserName = new Element(XmlPar.USERNAME);
		eUserName.setText(globalInfo.getUserName());
		eGlobalInfo.addContent(eUserName);
		Element ePassWord = new Element(XmlPar.PASSWORD);
		ePassWord.setText(globalInfo.getPassWord());
		eGlobalInfo.addContent(ePassWord);
		Element authorizationCode = new Element(XmlPar.AUTHORIZATIONCODE);
		authorizationCode.setText("");
		eGlobalInfo.addContent(authorizationCode);
		 Element efjh = new Element(XmlPar.FJH);
         efjh.setText(globalInfo.getFjh());
         eGlobalInfo.addContent(efjh);
         
         final Element jqbhElement = new Element(XmlPar.JQBH);
         jqbhElement.setText("0000000");
         eGlobalInfo.addContent(jqbhElement);
		// 返回信息
		Element eReturnStateInfo = new Element(XmlPar.RETURNSTATEINFO);
		Element eReturnCode = new Element(XmlPar.RETURNCODE);
		eReturnCode.setText(returnStateInfo.getReturnCode());
		eReturnStateInfo.addContent(eReturnCode);
		Element eReturnMessage = new Element(XmlPar.RETURNMESSAGE);
		//修改String串为空的判断
		if ( StringUtils.isNotEmpty(returnStateInfo.getReturnMessage()) ) {
			eReturnMessage.setText(encode(returnStateInfo.getReturnMessage()));
		} else {
			eReturnMessage.setText("");
		}
		eReturnStateInfo.addContent(eReturnMessage);
		// 交换数据
		Element eData = new Element(XmlPar.ZZSDATA);
		Element eDataDescription;
		eDataDescription = new Element(XmlPar.DATADESCRIPTION);
		Element eZipCode = new Element(XmlPar.ZIPCODE);
		String iszip="";
		if(data!=null&&data.getZipCode()!=null){
			iszip=data.getZipCode();
		}else{
			iszip=isZip(data.getContent(), 10);
		}
		eZipCode.setText(iszip);
		eDataDescription.addContent(eZipCode);
		Element eEncryptCode = new Element(XmlPar.ENCRYPTCODE);
		eEncryptCode.setText(data.getEncryptCode());
		eDataDescription.addContent(eEncryptCode);
		Element eCodeType = new Element(XmlPar.CODETYPE);
		eCodeType.setText(data.getCodeType());
		eDataDescription.addContent(eCodeType);
		eData.addContent(eDataDescription);
		Element eContent = new Element(XmlPar.CONTENT);
		String content=encodeData(data.getContent(), iszip, data.getEncryptCode(), globalInfo.getPassWord(),globalInfo.getNSRSBH());
		eContent.setText(content);
		eData.addContent(eContent);
		root.addContent(eGlobalInfo);
		root.addContent(eReturnStateInfo);
		root.addContent(eData);
		Format format = Format.getCompactFormat();
		format.setEncoding("UTF-8");
		format.setIndent(" ");
		XMLOutputter xmlout = new XMLOutputter(format);
		byteRsp = new ByteArrayOutputStream();
		xmlout.output(doc, byteRsp);
		resData = byteRsp.toString("UTF-8");
	} catch (IOException e) {
		log.error("未知：" + e);
	}finally{
		if(byteRsp != null){
			try {
				byteRsp.close();
			} catch (IOException e) {
				log.error("关闭流出错",e);
			}
		}
	}
	return resData;
}
	
	
	public static String getXml(GlobalInfo globalInfo,ReturnStateInfo returnStateInfo, Data data,boolean flag){
		ByteArrayOutputStream byteRsp = null;
		String resData = "";
		try {
		Element root = new Element(XmlPar.ROOT_BASE);
		Namespace ns = Namespace.getNamespace(XmlPar.NS_ONE);
		root.addNamespaceDeclaration(ns);
		Namespace ns1 = Namespace.getNamespace("xsi", XmlPar.NS_TWO);
		root.addNamespaceDeclaration(ns1);
		Namespace ns2 = Namespace.getNamespace("schemaLocation",XmlPar.NS_THREE);
		root.addNamespaceDeclaration(ns2);
		Document doc = new Document(root);
		Element eRoot = doc.getRootElement();
		eRoot.setAttribute("version", XmlPar.VERSION_NO);
	
		// 全局信息
        Element eGlobalInfo = new Element(XmlPar.GLOBALINFO);
        
        Element eTerminalCode = new Element(XmlPar.TERMINALCODE);
        eTerminalCode.setText(globalInfo.getTerminalCode());
        eGlobalInfo.addContent(eTerminalCode);

        Element eAppID = new Element(XmlPar.APPID);
        eAppID.setText(globalInfo.getAppId());
        eGlobalInfo.addContent(eAppID);

        Element eVersion = new Element(XmlPar.VERSION);
        eVersion.setText(globalInfo.getVersion());
        eGlobalInfo.addContent(eVersion);

        Element eInterfaceCode;
        eInterfaceCode = new Element(XmlPar.INTERFACECODE);
        eInterfaceCode.setText(globalInfo.getInterfaceCode());
        eGlobalInfo.addContent(eInterfaceCode);

        Element eUserName = new Element(XmlPar.USERNAME);
        eUserName.setText(globalInfo.getUserName());
        eGlobalInfo.addContent(eUserName);

        Element ePassWord = new Element(XmlPar.PASSWORD);
        ePassWord.setText(globalInfo.getPassWord());
        eGlobalInfo.addContent(ePassWord);

        Element eDataExchangeId = new Element(XmlPar.DATAEXCHANGEID);
        eDataExchangeId.setText(globalInfo.getDataExchangeId());
        eGlobalInfo.addContent(eDataExchangeId);

        Element eSQM = new Element(XmlPar.AUTHORIZATIONCODE);
        eSQM.setText(globalInfo.getAuthorizationCode());
        eGlobalInfo.addContent(eSQM);

        Element ERequestCode = new Element(XmlPar.REQUESTCODE);
        ERequestCode.setText(globalInfo.getRequestCode());
        eGlobalInfo.addContent(ERequestCode);

        Element eRequestTime = new Element(XmlPar.REQUESTTIME);
        eRequestTime.setText(globalInfo.getRequestTime());
        eGlobalInfo.addContent(eRequestTime);

        Element eResponseCode;
        eResponseCode = new Element(XmlPar.RESPONSECODE);
        eResponseCode.setText(globalInfo.getResponseCode());
        eGlobalInfo.addContent(eResponseCode);

        
        Element efjh = new Element(XmlPar.FJH);
        efjh.setText(globalInfo.getFjh());
        eGlobalInfo.addContent(efjh);
        
        final Element jqbhElement = new Element(XmlPar.JQBH);
        jqbhElement.setText(globalInfo.getJqbh());
        eGlobalInfo.addContent(jqbhElement);
        
        
		// 返回信息
		Element eReturnStateInfo = new Element(XmlPar.RETURNSTATEINFO);
		Element eReturnCode = new Element(XmlPar.RETURNCODE);
		eReturnCode.setText(returnStateInfo.getReturnCode());
		eReturnStateInfo.addContent(eReturnCode);
		Element eReturnMessage = new Element(XmlPar.RETURNMESSAGE);
		//修改String串为空的判断
		if ( StringUtils.isNotEmpty(returnStateInfo.getReturnMessage()) ) {
			eReturnMessage.setText(encode(returnStateInfo.getReturnMessage()));
		} else {
			eReturnMessage.setText("");
		}
		eReturnStateInfo.addContent(eReturnMessage);
		// 交换数据
		Element eData = new Element(XmlPar.DATA);
		Element eDataDescription;
		eDataDescription = new Element(XmlPar.DATADESCRIPTION);
		Element eZipCode = new Element(XmlPar.ZIPCODE);
		String iszip="";
		if(data!=null&&data.getZipCode()!=null){
			iszip=data.getZipCode();
		}else{
			iszip=isZip(data.getContent(), 10);
		}
		eZipCode.setText(iszip);
		eDataDescription.addContent(eZipCode);
		Element eEncryptCode = new Element(XmlPar.ENCRYPTCODE);
		eEncryptCode.setText(data.getEncryptCode());
		eDataDescription.addContent(eEncryptCode);
		Element eCodeType = new Element(XmlPar.CODETYPE);
		eCodeType.setText(data.getCodeType());
		eDataDescription.addContent(eCodeType);
		eData.addContent(eDataDescription);
		Element eContent = new Element(XmlPar.CONTENT);
		if(flag){
			String content=encodeData(data.getContent(), iszip, data.getEncryptCode(), globalInfo.getPassWord(),globalInfo.getNSRSBH());
			eContent.setText(content);
		}else{
			eContent.setText(data.getData());
		}
		eData.addContent(eContent);
		root.addContent(eGlobalInfo);
		root.addContent(eReturnStateInfo);
		root.addContent(eData);
		Format format = Format.getCompactFormat();
		format.setEncoding("UTF-8");
		format.setIndent(" ");
		XMLOutputter xmlout = new XMLOutputter(format);
		byteRsp = new ByteArrayOutputStream();
		xmlout.output(doc, byteRsp);
		resData = byteRsp.toString("UTF-8");
	} catch (IOException e) {
		log.error("未知：" + e);
	}finally{
		if(byteRsp!=null){
			try {
				byteRsp.close();
			} catch (IOException e) {
				log.error("关闭流出错",e);
			}
		}
	}
	return resData;
}
	
	
	public static String getPtdzfpXml(GlobalInfo globalInfo,ReturnStateInfo returnStateInfo, Data data,boolean flag){
		ByteArrayOutputStream byteRsp = null;
		String resData = "";
		try {
		Element root = new Element(XmlPar.ROOT_BASE);
		Namespace ns = Namespace.getNamespace(XmlPar.NS_ONE);
		root.addNamespaceDeclaration(ns);
		Namespace ns1 = Namespace.getNamespace("xsi", XmlPar.NS_TWO);
		root.addNamespaceDeclaration(ns1);
		Namespace ns2 = Namespace.getNamespace("schemaLocation",XmlPar.NS_THREE);
		root.addNamespaceDeclaration(ns2);
		Document doc = new Document(root);
		Element eRoot = doc.getRootElement();
		eRoot.setAttribute("version", XmlPar.VERSION_NO);
	
		// 全局信息
		Element eGlobalInfo = new Element(XmlPar.GLOBALINFO);
		Element terminalCode = new Element(XmlPar.TERMINALCODE);
		terminalCode.setText("0");
		eGlobalInfo.addContent(terminalCode);
		Element eAppID = new Element(XmlPar.APPID);
		eAppID.setText(globalInfo.getAppId());
		eGlobalInfo.addContent(eAppID);
		Element version = new Element(XmlPar.VERSION);
		version.setText("2.0");
		eGlobalInfo.addContent(version);
		Element eInterfaceCode;
		eInterfaceCode = new Element(XmlPar.INTERFACECODE);
		eInterfaceCode.setText(globalInfo.getInterfaceCode());
		eGlobalInfo.addContent(eInterfaceCode);
		Element ERequestCode = new Element(XmlPar.REQUESTCODE);
		ERequestCode.setText(globalInfo.getRequestCode());
		eGlobalInfo.addContent(ERequestCode);
		Element eRequestTime = new Element(XmlPar.REQUESTTIME);
		eRequestTime.setText(globalInfo.getRequestTime());
		eGlobalInfo.addContent(eRequestTime);
		Element eResponseCode;
		eResponseCode = new Element(XmlPar.RESPONSECODE);
		eResponseCode.setText(globalInfo.getResponseCode());
		eGlobalInfo.addContent(eResponseCode);
		Element eDataExchangeId = new Element(XmlPar.DATAEXCHANGEID);
		eDataExchangeId.setText(globalInfo.getDataExchangeId());
		eGlobalInfo.addContent(eDataExchangeId);
		Element eUserName = new Element(XmlPar.USERNAME);
		eUserName.setText(globalInfo.getUserName());
		eGlobalInfo.addContent(eUserName);
		Element ePassWord = new Element(XmlPar.PASSWORD);
		ePassWord.setText(globalInfo.getPassWord());
		eGlobalInfo.addContent(ePassWord);
		Element taxpayerId = new Element(XmlPar.TAXPAYERID);
		taxpayerId.setText(globalInfo.getNSRSBH());
		eGlobalInfo.addContent(taxpayerId);
		Element authorizationCode = new Element(XmlPar.AUTHORIZATIONCODE);
		authorizationCode.setText("");
		eGlobalInfo.addContent(authorizationCode);
		// 返回信息
		Element eReturnStateInfo = new Element(XmlPar.RETURNSTATEINFO);
		Element eReturnCode = new Element(XmlPar.RETURNCODE);
		eReturnCode.setText(returnStateInfo.getReturnCode());
		eReturnStateInfo.addContent(eReturnCode);
		Element eReturnMessage = new Element(XmlPar.RETURNMESSAGE);
		//修改String串为空的判断
		if ( StringUtils.isNotEmpty(returnStateInfo.getReturnMessage()) ) {
			eReturnMessage.setText(encode(returnStateInfo.getReturnMessage()));
		} else {
			eReturnMessage.setText("");
		}
		eReturnStateInfo.addContent(eReturnMessage);
		// 交换数据
		Element eData = new Element(XmlPar.DATA);
		Element eDataDescription;
		eDataDescription = new Element(XmlPar.DATADESCRIPTION);
		Element eZipCode = new Element(XmlPar.ZIPCODE);
		String iszip="";
		if(data!=null&&data.getZipCode()!=null){
			iszip=data.getZipCode();
		}else{
			iszip=isZip(data.getContent(), 10);
		}
		eZipCode.setText(iszip);
		eDataDescription.addContent(eZipCode);
		Element eEncryptCode = new Element(XmlPar.ENCRYPTCODE);
		eEncryptCode.setText(data.getEncryptCode());
		eDataDescription.addContent(eEncryptCode);
		Element eCodeType = new Element(XmlPar.CODETYPE);
		eCodeType.setText(data.getCodeType());
		eDataDescription.addContent(eCodeType);
		eData.addContent(eDataDescription);
		Element eContent = new Element(XmlPar.CONTENT);
		if(flag){
			String content=encodeData(data.getContent(), iszip, data.getEncryptCode(), globalInfo.getPassWord(),globalInfo.getNSRSBH());
			eContent.setText(content);
		}else{
			eContent.setText(data.getData());
		}
		eData.addContent(eContent);
		root.addContent(eGlobalInfo);
		root.addContent(eReturnStateInfo);
		root.addContent(eData);
		Format format = Format.getCompactFormat();
		format.setEncoding("UTF-8");
		format.setIndent(" ");
		XMLOutputter xmlout = new XMLOutputter(format);
		byteRsp = new ByteArrayOutputStream();
		xmlout.output(doc, byteRsp);
		resData = byteRsp.toString("UTF-8");
	} catch (IOException e) {
		log.error("未知：" + e);
		e.printStackTrace();
	}finally{
		if(byteRsp!=null){
			try {
				byteRsp.close();
			} catch (IOException e) {
				log.error("关闭流出错",e);
			}
		}
	}
	return resData;
}

	/**
	 * 取得全部明文信息
	 * @deprecated  将传入的xml参数字符串转换成为map  by 苏海林
	 * @return Map
	 * @author: 张士锋 
	 * @date: Created on Jul 7, 2012 3:20:54 PM
	 */
	
	@SuppressWarnings("rawtypes")
	public static Map getInterface(String requestMessage){
		return getInterface(requestMessage, true);
	}
	/**
	 * 只解密第一层协议
	 */
	@SuppressWarnings("rawtypes")
	public static Map getInterfaceOuter(String requestMessage){
		return getInterface(requestMessage, true);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getInterface(String requestMessage,boolean flag) {
		Map map = new HashMap();
		GlobalInfo globalInfo = new GlobalInfo();
		StringReader read = null;
		try {
			read = new StringReader(requestMessage);
			InputSource source = new InputSource(read);
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(source);
			Element root = doc.getRootElement();
			List node = root.getChildren();
			if (node != null && node.size() > 0) {
				for (int i = 0; i < node.size(); i++) {
					Element e1 = (Element) node.get(i);
					if (e1.getName().equals(XmlPar.GLOBALINFO)) {
//						globalInfo.setTerminalCode(e1.getChild(XmlPar.TERMINALCODE)==null||e1.getChild(XmlPar.TERMINALCODE).getText()==null?"":e1.getChild(XmlPar.TERMINALCODE).getText());
						globalInfo.setAppId(e1.getChild(XmlPar.APPID)==null||e1.getChild(XmlPar.APPID).getText()==null?"":e1.getChild(XmlPar.APPID).getText());
//						globalInfo.setVersion(e1.getChild(XmlPar.VERSION)==null||e1.getChild(XmlPar.VERSION).getText()==null?"":e1.getChild(XmlPar.VERSION).getText());
						globalInfo.setInterfaceCode(e1.getChild(XmlPar.INTERFACECODE)==null||e1.getChild(XmlPar.INTERFACECODE).getText()==null?"":e1.getChild(XmlPar.INTERFACECODE).getText());
						globalInfo.setRequestCode(e1.getChild(XmlPar.REQUESTCODE)==null||e1.getChild(XmlPar.REQUESTCODE).getText()==null?"":e1.getChild(XmlPar.REQUESTCODE).getText());
						globalInfo.setRequestTime(e1.getChild(XmlPar.REQUESTTIME)==null||e1.getChild(XmlPar.REQUESTTIME).getText()==null?"":e1.getChild(XmlPar.REQUESTTIME).getText());
						globalInfo.setResponseCode(e1.getChild(XmlPar.RESPONSECODE)==null||e1.getChild(XmlPar.RESPONSECODE).getText()==null?"":e1.getChild(XmlPar.RESPONSECODE).getText());
						globalInfo.setDataExchangeId(e1.getChild(XmlPar.DATAEXCHANGEID)==null||e1.getChild(XmlPar.DATAEXCHANGEID).getText()==null?"":e1.getChild(XmlPar.DATAEXCHANGEID).getText());
						globalInfo.setUserName(e1.getChild(XmlPar.USERNAME)==null||e1.getChild(XmlPar.USERNAME).getText()==null?"":e1.getChild(XmlPar.USERNAME).getText());
						globalInfo.setPassWord(e1.getChild(XmlPar.PASSWORD)==null||e1.getChild(XmlPar.PASSWORD).getText()==null?"":e1.getChild(XmlPar.PASSWORD).getText());
						globalInfo.setNSRSBH(e1.getChild(XmlPar.TAXPAYERID)==null||e1.getChild(XmlPar.TAXPAYERID).getText()==null?"":e1.getChild(XmlPar.TAXPAYERID).getText());
//						globalInfo.setAuthorizationCode(e1.getChild(XmlPar.AUTHORIZATIONCODE)==null||e1.getChild(XmlPar.AUTHORIZATIONCODE).getText()==null?"":e1.getChild(XmlPar.AUTHORIZATIONCODE).getText());
						globalInfo.setJqbh(e1.getChild(XmlPar.JQBH)==null||e1.getChild(XmlPar.JQBH).getText()==null?"":e1.getChild(XmlPar.JQBH).getText());
						map.put(e1.getName(), globalInfo);
					}
					if (e1.getName().equals(XmlPar.RETURNSTATEINFO)) {
						ReturnStateInfo returneStateInfo = new ReturnStateInfo();
						returneStateInfo.setReturnCode(e1.getChild(XmlPar.RETURNCODE).getText()==null?"":e1.getChild(XmlPar.RETURNCODE).getText());
						//修改String串为空的判断
						if ( StringUtils.isNotEmpty(e1.getChild(XmlPar.RETURNMESSAGE).getText())) {
							returneStateInfo.setReturnMessage(decode(e1.getChild(XmlPar.RETURNMESSAGE).getText()==null?"":e1.getChild(XmlPar.RETURNMESSAGE).getText()));
						} else {
							returneStateInfo.setReturnMessage("");
						}
						map.put(e1.getName(), returneStateInfo);
					}
					if (e1.getName().equals(XmlPar.ZZSDATA)) {
						Data data = new Data();
						data.setDataDescription(e1.getChild(XmlPar.DATADESCRIPTION).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getText());
						data.setZipCode(e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ZIPCODE).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ZIPCODE).getText());
						data.setEncryptCode(e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ENCRYPTCODE).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ENCRYPTCODE).getText());
						data.setCodeType(e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.CODETYPE).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.CODETYPE).getText());
						log.debug(e1.getChild(XmlPar.CONTENT).getText());
						if(flag){
							String content=decodeData(e1.getChild(XmlPar.CONTENT).getText(), data.getZipCode(), data.getEncryptCode(), globalInfo.getPassWord());
							data.setContent(content);
						}else{
							data.setData(e1.getChild(XmlPar.CONTENT).getText());
						}
						
						if(log.isDebugEnabled())
						log.debug(data.getContent()+"+=============请求数据");
						map.put(e1.getName(), data);
					}
				}
			}
		} catch (JDOMException e) {
			log.error("未知：" + e);
		} catch (IOException e) {
			log.error("未知：" + e);
		}finally{
			if(read != null){
				read.close();
			}
		}
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getPtdzfpInterface(String requestMessage,boolean flag) {
		Map map = new HashMap();
		GlobalInfo globalInfo = new GlobalInfo();
		StringReader read = null;
		try {
			read = new StringReader(requestMessage);
			InputSource source = new InputSource(read);
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(source);
			Element root = doc.getRootElement();
			List node = root.getChildren();
			if (node != null && node.size() > 0) {
				for (int i = 0; i < node.size(); i++) {
					Element e1 = (Element) node.get(i);
					if (e1.getName().equals(XmlPar.GLOBALINFO)) {
//						globalInfo.setTerminalCode(e1.getChild(XmlPar.TERMINALCODE)==null||e1.getChild(XmlPar.TERMINALCODE).getText()==null?"":e1.getChild(XmlPar.TERMINALCODE).getText());
						globalInfo.setAppId(e1.getChild(XmlPar.APPID)==null||e1.getChild(XmlPar.APPID).getText()==null?"":e1.getChild(XmlPar.APPID).getText());
//						globalInfo.setVersion(e1.getChild(XmlPar.VERSION)==null||e1.getChild(XmlPar.VERSION).getText()==null?"":e1.getChild(XmlPar.VERSION).getText());
						globalInfo.setInterfaceCode(e1.getChild(XmlPar.INTERFACECODE)==null||e1.getChild(XmlPar.INTERFACECODE).getText()==null?"":e1.getChild(XmlPar.INTERFACECODE).getText());
						globalInfo.setRequestCode(e1.getChild(XmlPar.REQUESTCODE)==null||e1.getChild(XmlPar.REQUESTCODE).getText()==null?"":e1.getChild(XmlPar.REQUESTCODE).getText());
						globalInfo.setRequestTime(e1.getChild(XmlPar.REQUESTTIME)==null||e1.getChild(XmlPar.REQUESTTIME).getText()==null?"":e1.getChild(XmlPar.REQUESTTIME).getText());
						globalInfo.setResponseCode(e1.getChild(XmlPar.RESPONSECODE)==null||e1.getChild(XmlPar.RESPONSECODE).getText()==null?"":e1.getChild(XmlPar.RESPONSECODE).getText());
						globalInfo.setDataExchangeId(e1.getChild(XmlPar.DATAEXCHANGEID)==null||e1.getChild(XmlPar.DATAEXCHANGEID).getText()==null?"":e1.getChild(XmlPar.DATAEXCHANGEID).getText());
						globalInfo.setUserName(e1.getChild(XmlPar.USERNAME)==null||e1.getChild(XmlPar.USERNAME).getText()==null?"":e1.getChild(XmlPar.USERNAME).getText());
						globalInfo.setPassWord(e1.getChild(XmlPar.PASSWORD)==null||e1.getChild(XmlPar.PASSWORD).getText()==null?"":e1.getChild(XmlPar.PASSWORD).getText());
//						globalInfo.setAuthorizationCode(e1.getChild(XmlPar.AUTHORIZATIONCODE)==null||e1.getChild(XmlPar.AUTHORIZATIONCODE).getText()==null?"":e1.getChild(XmlPar.AUTHORIZATIONCODE).getText());
						map.put(e1.getName(), globalInfo);
					}
					if (e1.getName().equals(XmlPar.RETURNSTATEINFO)) {
						ReturnStateInfo returneStateInfo = new ReturnStateInfo();
						returneStateInfo.setReturnCode(e1.getChild(XmlPar.RETURNCODE).getText()==null?"":e1.getChild(XmlPar.RETURNCODE).getText());
						//修改String串为空的判断
						if ( StringUtils.isNotEmpty(e1.getChild(XmlPar.RETURNMESSAGE).getText())) {
							returneStateInfo.setReturnMessage(decode(e1.getChild(XmlPar.RETURNMESSAGE).getText())==null?"":decode(e1.getChild(XmlPar.RETURNMESSAGE).getText()));
						} else {
							returneStateInfo.setReturnMessage("");
						}
						map.put(e1.getName(), returneStateInfo);
					}
					if (e1.getName().equals(XmlPar.DATA)) {
						Data data = new Data();
						data.setDataDescription(e1.getChild(XmlPar.DATADESCRIPTION).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getText());
						data.setZipCode(e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ZIPCODE).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ZIPCODE).getText());
						data.setEncryptCode(e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ENCRYPTCODE).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.ENCRYPTCODE).getText());
						data.setCodeType(e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.CODETYPE).getText()==null?"":e1.getChild(XmlPar.DATADESCRIPTION).getChild(XmlPar.CODETYPE).getText());
						log.debug(e1.getChild(XmlPar.CONTENT).getText());
						if(flag){
							String content=decodeData(e1.getChild(XmlPar.CONTENT).getText(), data.getZipCode(), data.getEncryptCode(), globalInfo.getPassWord());
							data.setContent(content);
						}else{
							data.setData(e1.getChild(XmlPar.CONTENT).getText());
						}
						
						if(log.isDebugEnabled())
						log.debug(data.getContent()+"+=============请求数据");
						map.put(e1.getName(), data);
					}
				}
			}
		} catch (JDOMException e) {
			log.error("未知：" + e);
		} catch (IOException e) {
			log.error("未知：" + e);
		}finally{
			if(read!=null){
				read.close();
			}
		}
		return map;
	}

	/**
	 * 
	 * <p>
	 * 取得系统当前格式化时间
	 * </p>
	 * 
	 * @param args
	 *            void
	 * @author: 张士锋
	 * @date: Created on Jul 9, 2012 10:04:24 AM
	 */
	public static String getCurDate(String formatStyle) {
		DateFormat format1 = new SimpleDateFormat(formatStyle);
		return format1.format(new Date());
	}
	
	public static String getFormatDate(Date date,String formatStyle) {
		DateFormat format1 = new SimpleDateFormat(formatStyle);
		return format1.format(date);
	}
	
	public static String decodeData(String org,String zipCode,String encryptCode,String password){
		byte[] temp_coutnent;
		try {
			//修改String串为空的判断
			if ( StringUtils.isNotEmpty(org)) {
				temp_coutnent = decode(org.getBytes());
				if ("1".equals(zipCode)) {
					temp_coutnent = GZipUtils.decompress(temp_coutnent);
				}
				if("1".equals(encryptCode)){
					temp_coutnent = TripleDESUtil.decryptMode(password.substring(CaCfg.PROTOCOLPASSWORDSJSLENGTH, password.length()), temp_coutnent);
				}else if("2".equals(encryptCode)){
					//CA解密
					 temp_coutnent = PKCS7.pkcs7Decrypt(temp_coutnent);
				}
				return new String(temp_coutnent,"UTF-8");
				
			}
		}  catch (Exception e) {
				log.error("请求数据",e);
		}
			return "";
	}
	
    public static String encodeData(String org, String zipCode, String encryptCode, String password,String nsrsbh) throws IOException {
        byte[] temp_content = org == null ? "".getBytes() : org.getBytes("UTF-8");
//        try {
            if (StringUtils.isNotEmpty(org)) {
                if ("1".equals(encryptCode)) {
                    temp_content = TripleDESUtil.encryptMode(password.substring(CaCfg.PROTOCOLPASSWORDSJSLENGTH, password.length()), temp_content);
                } else if ("2".equals(encryptCode)) {
                	byte[] publicPFXBytes = FileUtils.readFileToByteArray(new File(CaCfg.CA_SERVER_PATH));
                	temp_content = PKCS7.pkcs7Encrypt(org, publicPFXBytes);
                }
                if ("1".equals(zipCode)) {
                    temp_content = GZipUtils.compress(temp_content);
                }
//                return new String(encode(temp_content), "UTF-8");
                return new String(encode(temp_content));
            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return "";
    }

	/**
	 * 
	 * <p>
	 * 取得返回信息对像
	 * </p>
	 * 
	 * @param args
	 *            void
	 * @author: 张士锋
	 * @date: Created on Jul 10, 2012 8:56:35 AM
	 */
	public static ReturnStateInfo getReturnStateInfo(String returnStateCode,
			String returnMessage) {
		ReturnStateInfo returnStateInfo = new ReturnStateInfo();
		returnStateInfo.setReturnCode(returnStateCode);
		returnStateInfo.setReturnMessage(returnMessage);
		return returnStateInfo;
	}

	

	public static String replaceStr(String s) {
		return s.replaceAll(" ", "");
	}

	@SuppressWarnings("rawtypes")
	public static List getDataRoot(String xml) throws Exception {
		String xmlRootStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <ROOT>";
		String xmlRootEnd = "</ROOT>";
	    if(xml.startsWith("<?xml")){
	        XMLShellFactory factory = XMLShellFactory.newInstance();
	        return (List) factory.generateDomainObject(
	                xml ).get(0);
	    }else{
	        XMLShellFactory factory = XMLShellFactory.newInstance();
            return (List) factory.generateDomainObject(xmlRootStart+ xml +xmlRootEnd).get(0);
	    }
	    
	}
	
	public static Data getData(ByteArrayOutputStream out) {
		Data data = new Data();
		try {
			String temp_str = new String(out.toByteArray(),"UTF-8");
			data.setZipCode(ProXml.isZip(temp_str, 10));
			data.setEncryptCode("0");
			//修改String串为空的判断
			if ( StringUtils.isNotEmpty(temp_str)) {
				temp_str = temp_str.substring(temp_str.indexOf("<ROOT>") + 6,temp_str.lastIndexOf("</ROOT>"));
			} else {
				temp_str = "";
			}
			data.setContent(temp_str);
		} catch (Exception e) {
			log.error("未知：" + e);
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * 获取随机数
	 * @param ws
	 * @return
	 * @author peterli
	 * @date Sep 23, 2013
	 */
    public static String getRdom(int ws) {
        Random r = new Random();
        String nums = Integer.toString((Math.abs(r.nextInt(Integer.MAX_VALUE))));
        if (nums.length() >= ws)
            return nums.substring(nums.length() - ws);
        else
            return StringUtils.leftPad(nums, ws, "0");
    }
}
