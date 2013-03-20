/**
 * 
 */
package org.snova.gae.common;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;

/**
 * @author qiyingwang
 *
 */
public class EventHeaderTags
{
	
	private static final short magicNumber = (short) 0xCAFE;
	public String token;
	
	public static boolean readHeaderTags(Buffer buffer, EventHeaderTags tags)
	{
		if(buffer.readableBytes() < 8)
		{
			return false;
		}
		short magic = BufferHelper.readFixInt16(buffer, true);
		if(magic != magicNumber)
		{
			return false;
		}
		try
        {
	        tags.token = BufferHelper.readVarString(buffer);
        }
        catch (IOException e)
        {
        	e.printStackTrace();
	        return false;
        }
		return true;
	}
	
	public void encode(Buffer buffer)
	{
		BufferHelper.writeFixInt16(buffer, magicNumber, true);
		BufferHelper.writeVarString(buffer, token);
	}
	
}
