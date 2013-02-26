/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: MasterNodeService.java 
 *
 * @author yinqiwen [ 2011-12-7 | ÏÂÎç10:24:13 ]
 *
 */
package org.snova.gae.server.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.arch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.GAEConstants;
import org.snova.gae.common.event.AdminResponseEvent;
import org.snova.gae.common.event.RequestSharedAppIDResultEvent;
import org.snova.gae.common.event.ShareAppIDEvent;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.utils.SystemProperty;

/**
 *
 */
public class MasterNodeService
{
	protected static Logger	              logger	       = LoggerFactory
	                                                               .getLogger(MasterNodeService.class);
	private static DatastoreService	      datastore	       = DatastoreServiceFactory
	                                                               .getDatastoreService();
	private static AsyncDatastoreService	asyncdatastore	= DatastoreServiceFactory
	                                                               .getAsyncDatastoreService();
	protected static Vector<AppIdShareItem>	sharedItems	   = null;
	
	public static class AppIdShareItem
	{
		private String	appid;
		
		public String getAppid()
		{
			return appid;
		}
		
		public void setAppid(String appid)
		{
			this.appid = appid;
		}
		
		public String getGmail()
		{
			return email;
		}
		
		public void setGmail(String gmail)
		{
			this.email = gmail;
		}
		
		private String	email;
		
		public void fromEntity(Entity entity)
		{
			appid = (String) entity.getProperty("AppID");
			email = (String) entity.getProperty("Email");
		}
		
		public Entity toEntity()
		{
			Entity usrEntity = new Entity(GAEConstants.SHARED_APPID_ENTITY_NAME, appid);
			usrEntity.setProperty("AppID", appid);
			usrEntity.setProperty("Email", email);
			return usrEntity;
		}
	}
	
	public static void init()
	{
		if (null == sharedItems)
		{
			Vector<AppIdShareItem> items = new Vector<AppIdShareItem>();
			Query q = new Query(GAEConstants.SHARED_APPID_ENTITY_NAME);
			PreparedQuery pq = datastore.prepare(q);
			
			for (Entity result : pq.asIterable())
			{
				AppIdShareItem item = new AppIdShareItem();
				item.fromEntity(result);
				items.add(item);
			}
			sharedItems = items;
		}
	}
	
	public static Event randomRetrieveAppIds()
	{
		if (null != sharedItems && !sharedItems.isEmpty())
		{
			Random rnd = new Random();
			RequestSharedAppIDResultEvent ev = new RequestSharedAppIDResultEvent();
			ev.appids = new String[1];
			ev.appids[0] = sharedItems.get(rnd.nextInt(sharedItems.size()))
			        .getAppid();
			return ev;
		}
		
		return new AdminResponseEvent("", "No shared appid.", -1);
	}
	
	private static boolean verifyAppId(String appid)
	{
		try
		{
			String urlstr = "http://" + appid + ".appspot.com";
			URL url = new URL(urlstr);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
			        url.openStream()));
			StringBuffer buf = new StringBuffer();
			String line;
			
			while ((line = reader.readLine()) != null)
			{
				buf.append(line);
			}
			reader.close();
			return buf.toString().contains("snova");
			
		}
		catch (Exception e)
		{
			// ...
		}
		return false;
	}
	
	public static Event HandleShareEvent(ShareAppIDEvent ev)
	{
		return ev.operation == ShareAppIDEvent.SHARE ? shareMyAppId(ev.appid,
		        ev.email) : unshareMyAppid(ev.appid, ev.email);
	}
	
	public static Event shareMyAppId(String appid, String gmail)
	{
		AppIdShareItem share = getSharedItem(appid);
		if (null != share)
		{
			return new AdminResponseEvent(null,
			        "This AppId is already shared!", -1);
		}
		if (!verifyAppId(appid))
		{
			return new AdminResponseEvent(
			        null,
			        "Invalid AppId or Invalid snova-gae-server for this AppId!",
			        -1);
		}
		// currently, no check for gmail
		share = new AppIdShareItem();
		share.setAppid(appid);
		share.setGmail(gmail);
		saveSharedItem(share);
		if (null != gmail)
		{
			sendMail(gmail, "Thanks for sharing AppID:" + appid + "!",
			        "Thank you for sharing your appid!");
		}
		return new AdminResponseEvent("Share AppId Success!", null, 0);
	}
	
	public static Event unshareMyAppid(String appid, String gmail)
	{
		AppIdShareItem share = getSharedItem(appid);
		if (null == share)
		{
			return new AdminResponseEvent(null,
			        "This appid is not shared before!", -1);
		}
		if (null != share.getGmail())
		{
			if (!share.getGmail().equals(gmail))
			{
				return new AdminResponseEvent(
				        null,
				        "The input email address is not equal the share email address.",
				        -1);
			}
			else
			{
				sendMail(gmail, "Unsharing AppID:" + appid + "!", "Your appid:"
				        + appid + " is unshared from snova master.");
			}
		}
		deleteSharedItem(share);
		return new AdminResponseEvent("Unshare AppId Success!", null, 0);
	}
	
	private static AppIdShareItem getSharedItem(String appid)
	{
		if(null != sharedItems)
		{
			for(AppIdShareItem temp : sharedItems)
			{
				if(temp.appid .equals(appid))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	private static void saveSharedItem(AppIdShareItem item)
	{
		if(null == sharedItems)
		{
			sharedItems = new Vector<MasterNodeService.AppIdShareItem>();
		}
		sharedItems.add(item);
		Entity usrEntity = item.toEntity();
		asyncdatastore.put(usrEntity);
	}
	
	private static void deleteSharedItem(AppIdShareItem item)
	{
		if(null != sharedItems)
		{
			for(AppIdShareItem temp : sharedItems)
			{
				if(temp.appid .equals(item.appid))
				{
					sharedItems.remove(temp);
					asyncdatastore.delete(KeyFactory.createKey(GAEConstants.SHARED_APPID_ENTITY_NAME,
							temp.appid));
					break;
				}
			}
		}
	}
	
	private static void sendMail(String toAddress, String subject,
	        String content)
	{
		try
		{
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);
			StringBuffer buffer = new StringBuffer();
			buffer.append("Hi, ").append("\r\n\r\n");
			buffer.append(content);
			
			buffer.append("Thanks again. admin@"
			        + SystemProperty.applicationId.get() + ".appspot.com");
			String msgBody = buffer.toString();
			msg.setSubject(subject);
			msg.setFrom(new InternetAddress("admin@"
			        + SystemProperty.applicationId.get() + ".appspotmail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
			        toAddress, "Mr/Ms. User"));
			msg.addRecipient(Message.RecipientType.CC, new InternetAddress(
			        "yinqiwen@gmail.com", "Mr. Admin"));
			// msg.set
			msg.setText(msgBody);
			Transport.send(msg);
		}
		catch (Exception e)
		{
			logger.error("Failed to send mail to user:" + toAddress, e);
		}
	}
	
}
