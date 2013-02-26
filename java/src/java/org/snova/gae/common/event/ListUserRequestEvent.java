/**
 * 
 */
package org.snova.gae.common.event;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.snova.gae.common.GAEConstants;

/**
 * @author qiyingwang
 *
 */
@EventType(GAEConstants.USER_LIST_REQUEST_EVENT_TYPE)
@EventVersion(1)
public class ListUserRequestEvent extends Event
{
	@Override
    protected boolean onDecode(Buffer buffer)
    {
	    return true;
    }

	@Override
    protected boolean onEncode(Buffer buffer)
    {
		return true;
    }
}
