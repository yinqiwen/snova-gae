package org.snova.gae.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.misc.crypto.base64.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.EventHeaderTags;
import org.snova.gae.common.GAEEventHelper;
import org.snova.gae.common.config.GAEServerConfiguration;
import org.snova.gae.server.service.EventSendService;
import org.snova.gae.server.service.ServerConfigurationService;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.MessageType;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@SuppressWarnings("serial")
public class XmppInvokeServlet extends HttpServlet
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected XMPPService xmpp = XMPPServiceFactory.getXMPPService();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException
	{
		Message message = xmpp.parseMessage(req);

		try
		{
			final JID jid = message.getFromJid();
			byte[] raw = Base64.decodeFast(message.getBody());
			Buffer buffer = Buffer.wrapReadableContent(raw);
			EventHeaderTags tags = new EventHeaderTags();
			Event event = GAEEventHelper.parseEvent(buffer, tags);
			final GAEServerConfiguration cfg = ServerConfigurationService.getServerConfig();
			EventSendService sendService = new EventSendService()
			{
				public int getMaxDataPackageSize()
				{
					return cfg.getMaxXMPPDataPackageSize();
				}

				public void send(Buffer buf)
				{
					Message msg = new MessageBuilder()
					        .withRecipientJids(jid)
					        .withMessageType(MessageType.CHAT)
					        .withBody(
					                Base64.encodeToString(buf.getRawBuffer(),
					                        buf.getReadIndex(),
					                        buf.readableBytes(), false))
					        .build();
					{
						int retry = ServerConfigurationService.getServerConfig().getFetchRetryCount();
						while (SendResponse.Status.SUCCESS != xmpp
						        .sendMessage(msg).getStatusMap().get(jid)
						        && retry-- > 0)
						{
							logger.error("Failed to send response, try again!");
						}
					}
				}

			};
			event.setAttachment(new Object[] { tags, sendService });
			EventDispatcher.getSingletonInstance().dispatch(event);
		}
		catch (Throwable e)
		{
			logger.warn("Failed to process message", e);
		}

	}
}
