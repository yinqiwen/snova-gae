/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: IndexServlet.java 
 *
 * @author qiying.wang [ Apr 12, 2010 | 1:40:25 PM ]
 *
 */
package org.snova.gae.server.servlet.page;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.snova.gae.common.GAEPluginVersion;



/**
 *
 */
public class IndexServlet extends HttpServlet
{
	
	private static  String INDEX_PAGE  = null;
	
	static
	{
		InputStream is = IndexServlet.class.getResourceAsStream("index.html.template");
		byte[] buffer = new byte[4096];
		try
		{
			int len = is.read(buffer);
			String format = new String(buffer, 0, len);
			INDEX_PAGE = String.format(format, GAEPluginVersion.value, GAEPluginVersion.value);
			is.close();
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setStatus(200);
		resp.setContentLength(INDEX_PAGE.length());
		resp.getWriter().write(INDEX_PAGE);
		//super.doGet(req, resp);
	}
}
