/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: HttpProxyServlet.java 
 *
 * @author yinqiwen [ 2010-1-29 | pm09:58:01 ]
 *
 */
package org.snova.gae.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.EventHeaderTags;
import org.snova.gae.common.GAEEventHelper;
import org.snova.gae.server.service.EventSendService;


/**
 *
 */
public class HttpInvokeServlet extends HttpServlet
{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void doPost(HttpServletRequest req, final HttpServletResponse resp)
	        throws IOException
	{
		try
		{
			int bodylen = req.getContentLength();
			if (bodylen > 0)
			{
				Buffer content = new Buffer(bodylen);
				int len = content.read(req.getInputStream());
				if (len > 0)
				{
					EventHeaderTags tags = new EventHeaderTags();
					Event event = GAEEventHelper.parseEvent(content, tags);
					
					EventSendService sendService = new EventSendService()
					{
                        public int getMaxDataPackageSize()
                        {
	                        return -1;
                        }
                        public void send(Buffer buf)
                        {
                    		if(logger.isDebugEnabled())
                    		{
                    			logger.debug("Send result back with body len:" + buf.readableBytes());
                    		}
      
                    		resp.setStatus(200);
                    		resp.setContentType("application/octet-stream");
                    		resp.setContentLength(buf.readableBytes());
                    		
                    		try
                            {
	                            resp.getOutputStream().write(buf.getRawBuffer(), buf.getReadIndex(), buf.readableBytes());
	                            resp.getOutputStream().flush();
                            }
                            catch (IOException e)
                            {
                            	logger.error("Failed to send HTTP response.", e);
                            }
                    		
                        }
					};
					event.setAttachment(new Object[] { tags, sendService });
					EventDispatcher.getSingletonInstance().dispatch(event);
				}
			}
		}
		catch (Throwable e)
		{
			logger.warn("Failed to process message", e);
		}
	}

}
