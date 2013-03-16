/**
 * 
 */
package org.snova.gae.server.handler;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventConstants;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.event.TypeVersion;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.arch.event.misc.CompressEvent;
import org.arch.event.misc.CompressorType;
import org.arch.event.misc.EncryptEvent;
import org.arch.util.RandomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.EventHeaderTags;
import org.snova.gae.common.GAEConstants;
import org.snova.gae.common.GAEEventHelper;
import org.snova.gae.common.auth.User;
import org.snova.gae.common.event.AuthRequestEvent;
import org.snova.gae.common.event.AuthResponseEvent;
import org.snova.gae.server.config.ServerConfiguration;
import org.snova.gae.server.service.EventSendService;

/**
 * @author qiyingwang
 * 
 */
public class ServerEventHandler implements EventHandler
{
	protected Logger	          logger	     = LoggerFactory
	                                                     .getLogger(getClass());
	
	protected FetchServiceHandler	fetchHandler	= new FetchServiceHandler();
	
	// protected MasterNodeService masterNode = new MasterNodeService();
	
	private Event handleRecvEvent(EventHeaderTags tags, Event event)
	{
		Event response = null;
		TypeVersion tv = Event.getTypeVersion(event.getClass());
		if (null == tv)
		{
			logger.error("Failed to find registry type&version for class:"
			        + event.getClass().getName());
		}
		int type = tv.type;
		switch (type)
		{
			case GAEConstants.AUTH_REQUEST_EVENT_TYPE:
			{
				AuthRequestEvent auth = (AuthRequestEvent) event;
				User user = new User(auth.passwd, auth.user);
				AuthResponseEvent res = new AuthResponseEvent();
				res.appid = auth.appid;
				if (ServerConfiguration.getServerConfig().isValidUser(user))
				{
					res.token = RandomHelper.generateRandomString(8);
				}
				else
				{
					res.error = "Invalid user/passwd.";
				}
				response = res;
				break;
			}
			case HTTPEventContants.HTTP_REQUEST_EVENT_TYPE:
			{
				response = fetchHandler.fetch((HTTPRequestEvent) event);
				break;
			}
			case EventConstants.COMPRESS_EVENT_TYPE:
			{
				((CompressEvent) event).ev.setAttachment(event.getAttachment());
				response = handleRecvEvent(tags, ((CompressEvent) event).ev);
				break;
			}
			case EventConstants.ENCRYPT_EVENT_TYPE:
			{
				((EncryptEvent) event).ev.setAttachment(event.getAttachment());
				response = handleRecvEvent(tags, ((EncryptEvent) event).ev);
				break;
			}
			default:
			{
				logger.error("Unsupported event type:" + type);
				break;
			}
		}
		return response;
	}
	
	public void onEvent(EventHeader header, Event event)
	{
		Object[] attach = (Object[]) event.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		EventSendService sendService = (EventSendService) attach[1];
		Event response = handleRecvEvent(tags, event);
		if (null != response)
		{
			response.setHash(event.getHash());
			ServerConfiguration cfg = ServerConfiguration.getServerConfig();
			CompressorType compressType = cfg.getCompressor();
			if (response instanceof HTTPResponseEvent
			        && !compressType.equals(CompressorType.NONE))
			{
				HTTPResponseEvent httpRes = (HTTPResponseEvent) response;
				String contentType = httpRes.getHeader("content-type");
				if (null != contentType
				        && cfg.isContentTypeInCompressFilter(contentType))
				{
					compressType = CompressorType.NONE;
				}
			}
			CompressEvent compress = new CompressEvent(compressType, response);
			compress.setHash(event.getHash());
			EncryptEvent enc = new EncryptEvent(cfg.getEncrypter(), compress);
			enc.setHash(event.getHash());
			Buffer buf = GAEEventHelper.encodeEvent(tags, enc);
			sendService.send(buf);
		}
		else
		{
			logger.error("Failed to handle event[" + header.toString() + "]");
		}
	}
	
}
