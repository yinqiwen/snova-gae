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
import org.snova.gae.common.auth.Group;
import org.snova.gae.common.auth.Operation;

/**
 * @author qiyingwang
 *
 */
@EventType(GAEConstants.GROUP_OPERATION_EVENT_TYPE)
@EventVersion(1)
public class GroupOperationEvent extends Event
{
	public Group grp;
	public Operation opr;
	@Override
    protected boolean onDecode(Buffer buffer)
    {
		grp = new Group();
		if(grp.decode(buffer))
		{
			try
            {
	            opr = Operation.fromInt(BufferHelper.readVarInt(buffer));
            }
            catch (IOException e)
            {
	            return false;
            }
			return true;
		}
	    return false;
    }
	@Override
    protected boolean onEncode(Buffer buffer)
    {
		grp.encode(buffer);
		BufferHelper.writeVarInt(buffer, opr.getValue());
		return true;
    }
}
