package CxfProcotoclUtil;

import java.util.Map;

import com.aisino.cxf.CxfClient;
import com.aisino.procotocl.FirstLayerXml;
import com.aisino.procotocl.RES;
import com.aisino.procotocl.outbean.GlobalInfo;
import com.aisino.procotocl.util.XmlPar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	/**  报文解析
    	String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
"<interface xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd\" version=\"DZFP1.0\" >"+
"  <globalInfo>"+
"<terminalCode>1</terminalCode>"+
"<appId>DZFP</appId>"+
"<version></version>"+
"    <interfaceCode>ECXML.MAKERSA.CA</interfaceCode>"+
"    <userName>P0000001</userName>"+
"    <passWord>1822147350YESaWe0xSDPGcnOKfyQZew==</passWord>"+
"    <taxpayerId></taxpayerId>"+
"    <authorizationCode></authorizationCode>"+
"    <requestCode></requestCode>"+
"    <requestTime></requestTime>"+
"    <responseCode></responseCode>"+
"    <dataExchangeId></dataExchangeId>"+
"  </globalInfo>"+
"  <returnStateInfo>"+
"    <returnCode>返回代码</returnCode>"+
"    <returnMessage>base64 返回描述</returnMessage>"+
"  </returnStateInfo>"+
"  <Data>"+
"    <dataDescription>"+
"      <zipCode>1</zipCode>"+
"      <encryptCode>0</encryptCode>"+
"      <codeType>0</codeType>"+
"    </dataDescription>"+
"    <content>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</content>"+
"  </Data>"+
"</interface>";
    	Map<String,Object> map = FirstLayerXml.instantiation().praseFirstLayerXml(xml);
    	System.out.println(map.size());
    	**/
    	/**报文封装及调用cxf服务**/
    	GlobalInfo glo = new GlobalInfo();
    	glo.setAppId("123");
    	glo.setAuthorizationCode("123");
    	glo.setInterfaceCode(XmlPar.INTERFACE_CODE_RSA);
    	glo.setPassWord("0026770690fWKLYmW4Hf0ul1vUE9yDhA==");
    	RES RES = new RES();
    	RES.setAA("12312312sdf");
    	String xml = FirstLayerXml.instantiation().makeFirstLayerXml(glo, RES);
    	try {
			CxfClient.doCxfClent("http://192.168.8.131:8080/taxpayerReg/webservice/dataChangeWS", xml, glo.getInterfaceCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        assertTrue( true );
    }
}
