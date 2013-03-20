/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RangeHeaderValue.java 
 *
 * @author yinqiwen [ Feb 2, 2010 | 2:48:32 PM ]
 *
 */
package org.snova.gae.common.http;

/**
 *
 */
public class RangeHeaderValue  implements HttpHeaderValue
{
	private static final String BYTES_UNIT = "bytes";


	private long firstBytePos;

	private long lastBytePos = -1;
	
	public RangeHeaderValue(long firstBytePos, long lastBytePos)
	{
		this.firstBytePos = firstBytePos;
		this.lastBytePos = lastBytePos;
	}
	
	public RangeHeaderValue(String value)
	{
		String left = value.substring(BYTES_UNIT.length()).trim();
		left = left.substring("=".length()).trim();
		String[] split = left.split("-");
		
		firstBytePos = Integer.parseInt(split[0]);
		if(split.length > 1)
		{
			lastBytePos = Integer.parseInt(split[1]);
		}	
	}
	
	public long getFirstBytePos() 
	{
		return firstBytePos;
	}

	public long getLastBytePos() 
	{
		return lastBytePos;
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(BYTES_UNIT).append("=").append(firstBytePos).append("-").append(lastBytePos);
		return buffer.toString();
	}
	
	
}
