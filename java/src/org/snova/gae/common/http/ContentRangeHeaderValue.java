/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ContentRangeHeader.java 
 *
 * @author yinqiwen [ Feb 2, 2010 | 2:42:47 PM ]
 *
 */
package org.snova.gae.common.http;

/**
 *
 */
public class ContentRangeHeaderValue implements HttpHeaderValue
{
	private static final String BYTES_UNIT = "bytes";
	
	private long firstBytePos;
	private long lastBytePos;
	private long instanceLength;
	
	public ContentRangeHeaderValue(long firstBytePos, long lastBytePos, long instanceLength)
	{
		this.firstBytePos = firstBytePos;
		this.lastBytePos = lastBytePos;
		this.instanceLength = instanceLength;
	}
	
	public void setLastBytePos(long lastBytePos)
	{
		this.lastBytePos = lastBytePos;
	}

	public ContentRangeHeaderValue(String value)
	{
		String left = value.substring(BYTES_UNIT.length()).trim();
		String[] split = left.split("/");
		String[] split2 = split[0].split("-");
		firstBytePos =  Long.parseLong(split2[0].trim());
		lastBytePos =  Long.parseLong(split2[1].trim());
		instanceLength =  Long.parseLong(split[1].trim());
	}
	
	public long getFirstBytePos()
	{
		return firstBytePos;
	}

	public long getLastBytePos()
	{
		return lastBytePos;
	}

	public long getInstanceLength()
	{
		return instanceLength;
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(BYTES_UNIT).append(" ").append(firstBytePos).append("-").append(lastBytePos).append("/").append(instanceLength);
		return buffer.toString();
	}
}
