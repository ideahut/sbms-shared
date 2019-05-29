package com.github.ideahut.sbms.shared.util;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import com.github.ideahut.sbms.common.util.StringUtil;

public abstract class WebUtil {
	
	private static final String PARAMETER_ENCRYPT = "_enc_";
	
	
	/*
	 * ENCODE TEXT
	 */
	public static String encodeText(String text) {
		String str = text != null ? new String (text) : "";
		String key = String.valueOf(System.nanoTime());
		String result = key + "$" + Base64.encodeBase64URLSafeString((key + str).getBytes(Charset.defaultCharset()));
		return result;
	}
	
	/*
	 * DECODE TEXT
	 */
	public static String decodeText(String text) {
		if (text == null) {
			return null;
		}
		int i = text.indexOf("$");
		if (i == -1) {
			return null;
		}
		String key = text.substring(0, i);
		String res = text.substring(i + 1);
		res = new String(Base64.decodeBase64(res), Charset.defaultCharset());
		if (!res.startsWith(key)) {
			return null;
		}
		res = res.substring(key.length());
		return res;
	}
	
	
	/*
	 * ENCODE URL
	 */
	public static String encodeUrlQuery(String url, boolean useEncryptParam, String query) {
		String mUrl = new String(url);
		String mQuery = "";
		int i = mUrl.indexOf("?");
		if (i != -1) {
			mQuery = mUrl.substring(i + 1, mUrl.length());
			mUrl = mUrl.substring(0, i);			
		}
		if (query != null && !query.isEmpty()) {
			mQuery += (!mQuery.isEmpty() ? "&" : "") + query;
		}
		if (mQuery.isEmpty()) {
			return mUrl;
		}
		String enc = encodeText(mQuery);
		return mUrl + "?" + (useEncryptParam ? PARAMETER_ENCRYPT + "=" : "") + enc;
	}
	
	public static String encodeUrlQuery(String url, String query) {
		return encodeUrlQuery(url, true, query);
	}
		
	public static String encodeUrlParameters(String url, boolean useEncryptParam, Map<String, String> parameters) {
		String query = "";
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				String value = parameters.get(key);
				if (value == null) continue;
				query += key + "=" + value + "&";
			}
			if (!query.isEmpty()) {
				query = query.substring(0, query.length() - 1);
			}
		}
		return encodeUrlQuery(url, useEncryptParam, query);
	}
	
	public static String encodeUrlParameters(String url, Map<String, String> parameters) {
		return encodeUrlParameters(url, true, parameters);
	}
	
	public static String encodeUrl(String url, boolean useEncryptParam, String...parameters) {
		String query = "";
		for (String p : parameters) {
			query += p + "&";			
		}
		if (!query.isEmpty()) {
			query = query.substring(0, query.length() - 1);
		}
		return encodeUrlQuery(url, useEncryptParam, query);
	}
	
	public static String encodeUrl(String url, String...parameters) {
		return encodeUrl(url, true, parameters);
	}
	
	public static String encodeQuery(String query) {
		return encodeText(query);
	}
	
	public static String encodeParameters(Map<String, String> parameters) {
		String query = "";
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				String value = parameters.get(key);
				if (value == null) continue;
				query += key + "=" + value + "&";
			}
			if (!query.isEmpty()) {
				query = query.substring(0, query.length() - 1);
			}
		}
		return encodeText(query);
	}
	
	
	/*
	 * DECODE PARAMETER
	 */
	public static Map<String, String> decodeParameters(HttpServletRequest request) {
		String enc = request.getParameter(PARAMETER_ENCRYPT);
		boolean useEncryptParam = enc != null;
		if (!useEncryptParam) {
			enc = request.getQueryString();
		}
		String txt = decodeText(enc);
		if (txt == null) {
			return null;
		}
		Map<String, String> result = StringUtil.parseToMap(txt, "&", "=");
		if (result == null) {
			return null;
		}
		if (useEncryptParam) {
			Enumeration<String> en = request.getParameterNames();
			while(en.hasMoreElements()) {
				String name = en.nextElement();
				if (!PARAMETER_ENCRYPT.equals(name) && !result.containsKey(name)) {
					result.put(name, request.getParameter(name));
				}
			}
		}
		return Collections.unmodifiableMap(result);
	}
	
}
