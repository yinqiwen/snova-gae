/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SimpleNameValueListHeader.java 
 *
 * @author yinqiwen [ 2010-2-27 | 04:43:25 PM ]
 *
 */
package org.snova.gae.common.http;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SimpleNameValueListHeader implements HttpHeaderValue
{
	private Map<String, String> nameValues = new HashMap<String, String>();
	private String rawString;
	
	public SimpleNameValueListHeader(String value)
	{
		this.rawString = value;
		String[] splits = value.split(",");
		for(String v:splits)
		{
			String[] nameValue = v.split("=");
			if(nameValue.length > 1)
			{
				nameValues.put(nameValue[0].trim(), nameValue[1].trim());
			}
			else
			{
				nameValues.put(nameValue[0].trim(), null);
			}
		}
	}
	
	public String getValue(String name)
	{
		return nameValues.get(name);
	}
	
	public boolean containsName(String name)
	{
		return nameValues.containsKey(name);
	}
	
	public String toString()
	{
		return rawString;
	}
	
//	public static void main(String[] args)
//	{
//		SimpleNameValueListHeader v = new SimpleNameValueListHeader("no-cache, no-store, must-revalidate, max-age=24,pre-check=0, post-check=0");
//		System.out.println(v.getValue("max-age"));
//		System.out.println(v.containsName("no-cache"));
//	}
}
