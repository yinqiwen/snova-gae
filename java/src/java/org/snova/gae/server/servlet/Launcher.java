/**
 * 
 */
package org.snova.gae.server.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.event.GAEEvents;
import org.snova.gae.server.handler.ServerEventHandler;
import org.snova.gae.server.service.MasterNodeService;
import org.snova.gae.server.service.ServerConfigurationService;

/**
 * @author yinqiwen
 * 
 */
public class Launcher extends HttpServlet
{

	protected static Logger logger = LoggerFactory.getLogger(Launcher.class);

	public static void initServer() throws ServletException
	{
		try
		{
			ServerEventHandler handler = new ServerEventHandler();
			GAEEvents.init(handler, true);
			if(ServerConfigurationService.getServerConfig().isMasterNode())
			{
				MasterNodeService.init();
			}
			if (logger.isInfoEnabled())
			{
				logger.info("hyk-proxy v2 GAE Server init success!");
			}
		}
		catch (Exception e)
		{
			logger.error("Error occured when init launch servlet!", e);
			throw new ServletException(e);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		initServer();
	}
}
