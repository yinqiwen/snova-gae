/**
 * 
 */
package org.snova.gae.common.event;

import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.event.EventHandler;
import org.arch.event.EventSegment;
import org.arch.event.NamedEventHandler;
import org.arch.event.http.HTTPErrorEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.arch.event.misc.CompressEvent;
import org.arch.event.misc.EncryptEvent;

/**
 * @author qiyingwang
 * 
 */
public class GAEEvents
{
	private static void registerEventHandler(Class<? extends Event> clazz,
	        EventHandler handler)
	{
		try
		{
			EventDispatcher.getSingletonInstance().register(clazz, handler);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			
		}
	}
	
	public static void init(EventHandler handler, boolean isServer)
	{
		try
		{
			registerEventHandler(HTTPResponseEvent.class, handler);
			registerEventHandler(HTTPErrorEvent.class, handler);
			registerEventHandler(AuthResponseEvent.class, handler);
			registerEventHandler(AdminResponseEvent.class, handler);
			registerEventHandler(ListGroupResponseEvent.class, handler);
			
			registerEventHandler(ListUserResponseEvent.class, handler);
			
			registerEventHandler(EventSegment.class, handler);
			
			registerEventHandler(CompressEvent.class, handler);
			registerEventHandler(EncryptEvent.class, handler);
			registerEventHandler(ServerConfigEvent.class, handler);
			registerEventHandler(RequestSharedAppIDResultEvent.class, handler);
			registerEventHandler(RequestSharedAppIDEvent.class, handler);
			registerEventHandler(RequestAllSharedAppIDEvent.class, handler);
			if (isServer)
			{
				EventDispatcher.getSingletonInstance().register(
				        AuthRequestEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        HTTPRequestEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        BlackListOperationEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        GroupOperationEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        ListGroupRequestEvent.class, handler);
				
				EventDispatcher.getSingletonInstance().register(
				        ListUserRequestEvent.class, handler);
				EventDispatcher.getSingletonInstance().register(
				        UserOperationEvent.class, handler);
				
				EventDispatcher.getSingletonInstance().register(
				        RequestSharedAppIDEvent.class, handler);
			}
			else
			{
				if (null != handler && handler instanceof NamedEventHandler)
				{
					EventDispatcher.getSingletonInstance()
					        .registerNamedEventHandler(
					                (NamedEventHandler) handler);
				}
			}
			
		}
		catch (Exception e)
		{
			//
		}
		
	}
}
