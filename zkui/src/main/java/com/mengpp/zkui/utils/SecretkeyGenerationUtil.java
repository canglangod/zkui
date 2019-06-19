package com.mengpp.zkui.utils;

import org.jasypt.util.text.BasicTextEncryptor;

/**
 * SecretkeyGenerationUtil 配置文件加密工具类
 * 
 * @author mengpp
 * @date 2019年5月20日10:38:21
 */
public class SecretkeyGenerationUtil {

	private static final String S = "ENC(${data})";

	public static String encryption(String data, String pass) {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword(pass);
		data = basicTextEncryptor.encrypt(data);
		return S.replace("${data}", data);
	}

}
