package com.aisino.cxf;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import com.ctc.wstx.sw.SimpleNsStreamWriter;
import org.codehaus.stax2.ri.Stax2WriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aisino.procotocl.util.XmlPar;
import com.aisino.ws.IDataChangeService;

public class CxfClient {
	private static Logger log = LoggerFactory.getLogger(CxfClient.class);
	public static String doCxfClent(String url,String reqXml,String busi_code) throws Exception{
		String resXml = "";
		JaxWsProxyFactoryBean factory=null;
		try {
			factory = new JaxWsProxyFactoryBean();
		} catch (Exception e) {
			log.warn("初始化工厂失败");
			
		}    
		factory.setAddress(url);    
		factory.setServiceClass(IDataChangeService.class);    
		IDataChangeService service = (IDataChangeService) factory.create(); 
		Client proxy = ClientProxy.getClient(service);  
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();  
        HTTPClientPolicy policy = new HTTPClientPolicy();  
        policy.setConnectionTimeout(1200000);  
        policy.setReceiveTimeout(1000000);  
        conduit.setClient(policy);
        if(busi_code.equals(XmlPar.INTERFACE_CODE_M2)){
        	resXml = service.makeCa(reqXml);
        }else if(busi_code.equals(XmlPar.INTERFACE_CODE_RSA)){
        	resXml = service.makeSing(reqXml);
        }else if(busi_code.equals(XmlPar.INTERFACE_CODE_TAXREGPAYERREG)){
        	resXml = service.regTaxpayer(reqXml);
        }else{
        	log.error("不能识别的接口编码");
        	throw new Exception("不能识别的接口编码");
        }
        return resXml;
	}
}
