/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: SignoutRedirectServlet.java 
 *
 * @author qiying.wang [ Apr 12, 2010 | 2:02:44 PM ]
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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 *
 */
public class SignoutRedirectServlet extends HttpServlet
{
	private static String	INDEX_PAGE	= null;

	static
	{
		InputStream is = IndexServlet.class.getResourceAsStream("sign.html.template");
		byte[] buffer = new byte[4096];
		try
		{
			int len = is.read(buffer);
			INDEX_PAGE = new String(buffer, 0, len);
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
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		String out = String.format(INDEX_PAGE, GAEPluginVersion.value, user.getNickname(), userService.createLogoutURL("/admin"));
		resp.setStatus(200);
		resp.setContentLength(out.length());
		resp.getWriter().write(out);
	}
}
