/**
 * 
 */
package org.snova.gae.common.event;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.snova.gae.common.GAEConstants;
import org.snova.gae.common.config.GAEServerConfiguration;

/**
 * @author qiyingwang
 * 
 */
@EventType(GAEConstants.SERVER_CONFIG_EVENT_TYPE)
@EventVersion(1)
public class ServerConfigEvent extends Event
{
	public static final int GET_CONFIG_REQ = 1;
	public static final int GET_CONFIG_RES = 2;
	public static final int SET_CONFIG_REQ = 3;
	public static final int SET_CONFIG_RES = 4;
	public int opreration;
	public GAEServerConfiguration cfg;

	@Override
	protected boolean onDecode(Buffer buffer)
	{
		try
        {
	        opreration = BufferHelper.readVarInt(buffer);
	        if(buffer.readable())
	        {
	        	cfg = new GAEServerConfiguration();
	        	return cfg.decode(buffer);
	        }
	        return true;
        }
        catch (IOException e)
        {
	        return false;
        }
	}

	@Override
	protected boolean onEncode(Buffer buffer)
	{
		BufferHelper.writeVarInt(buffer, opreration);
		if(null != cfg)
		{
			cfg.encode(buffer);
		}
		return true;
	}

}
