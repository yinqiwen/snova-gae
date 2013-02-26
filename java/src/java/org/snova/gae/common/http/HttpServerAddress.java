/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpServerAddress.java 
 *
 * @author yinqiwen [ Jan 29, 2010 | 10:45:02 AM ]
 *
 */
package org.snova.gae.common.http;


/**
 *
 */
public class HttpServerAddress
{
	private String host;
	private int port;
	private String path;
	private boolean isSecure;
	
	public HttpServerAddress()
	{
		//do nothing
	}

	public HttpServerAddress(String host, String path, int port, boolean isSecure)
	{
		this.host = host;
		this.port = port;
		this.path = path;
		this.isSecure = isSecure;
	}

    public HttpServerAddress(String host, String path)
	{
		this(host, path, false);
	}
    
    public HttpServerAddress(String host, String path, boolean isSecure)
	{
		this(host, path, isSecure?443:80, isSecure);
	}
	
	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public String getPath()
	{
		return path;
	}
	
	public void trnasform2Https()
	{
		if(!isSecure)
		{
			isSecure = true;
			port = 443;
		}
	}
	
	public boolean isSecure()
	{
		return isSecure;
	}

	public String toPrintableString()
	{
		return "http" + (isSecure?"s":"") + "://" + host  + ":" + port  + path;
	}
	
	public String toString()
	{
		return toPrintableString();
	}

}
