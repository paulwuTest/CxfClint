package com.aisino.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface IDataChangeService {
	@WebMethod
	/**
	 * 注册接口
	 */
	public String regTaxpayer(String xml);
	
	/**
	 * 制证接口
	 * @param xml
	 * @return
	 */
	public String makeCa(String xml);
	
	/**
	 * 制章接口
	 * @param xml
	 * @return
	 */
	public String makeSing(String xml);
}
