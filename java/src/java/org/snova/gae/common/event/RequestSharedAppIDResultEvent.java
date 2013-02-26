/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: RequestSharedAppIDEvent.java 
 *
 * @author yinqiwen [ 2011-12-7 | ÏÂÎç10:18:10 ]
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

/**
 *
 */
@EventType(GAEConstants.REQUEST_SHARED_APPID_RESULT_EVENT_TYPE)
@EventVersion(1)
public class RequestSharedAppIDResultEvent extends Event
{
	public String[] appids = new String[0];

	@Override
	protected boolean onDecode(Buffer buffer)
	{
		try
        {
	        int len = BufferHelper.readVarInt(buffer);
	        appids = new String[len];
	        for (int i = 0; i < len; i++)
            {
	        	appids[i] = BufferHelper.readVarString(buffer);
            }
        }
        catch (IOException e)
        {
	        return false;
        }
		return true;
	}

	@Override
	protected boolean onEncode(Buffer buffer)
	{
		BufferHelper.writeVarInt(buffer, appids.length);
		for (int i = 0; i < appids.length; i++)
        {
			BufferHelper.writeVarString(buffer, appids[i]);
        }
		return true;
	}

}
