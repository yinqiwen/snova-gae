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

/**
 * @author wqy
 *
 */
@EventType(GAEConstants.SHARE_APPID_EVENT_TYPE)
@EventVersion(1)
public class ShareAppIDEvent  extends Event
{
	public static final int SHARE = 0;
	public static final int UNSHARE = 1;
	public int operation;
	public String appid;
	public String email;
	@Override
    protected boolean onDecode(Buffer buffer)
    {
		try
        {
			operation = BufferHelper.readVarInt(buffer);
	        appid = BufferHelper.readVarString(buffer);
	        email = BufferHelper.readVarString(buffer);
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
		BufferHelper.writeVarInt(buffer, operation);
	    BufferHelper.writeVarString(buffer, appid);
	    BufferHelper.writeVarString(buffer, email);
	    return true;
    }
	
}
