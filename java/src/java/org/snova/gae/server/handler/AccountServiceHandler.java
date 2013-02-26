/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AccountServiceImpl.java 
 *
 * @author yinqiwen [ 2010-4-8 | ÏÂÎç07:51:42 ]
 *
 */
package org.snova.gae.server.handler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.arch.event.Event;
import org.arch.util.RandomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.EventHeaderTags;
import org.snova.gae.common.GAEConstants;
import org.snova.gae.common.auth.Group;
import org.snova.gae.common.auth.Operation;
import org.snova.gae.common.auth.User;
import org.snova.gae.common.event.AdminResponseEvent;
import org.snova.gae.common.event.AuthRequestEvent;
import org.snova.gae.common.event.AuthResponseEvent;
import org.snova.gae.common.event.BlackListOperationEvent;
import org.snova.gae.common.event.GroupOperationEvent;
import org.snova.gae.common.event.ListGroupResponseEvent;
import org.snova.gae.common.event.ListUserResponseEvent;
import org.snova.gae.common.event.UserOperationEvent;
import org.snova.gae.server.service.UserManagementService;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.utils.SystemProperty;

/**
 *
 */
public class AccountServiceHandler
{
	protected static Logger logger = LoggerFactory.getLogger(AccountServiceHandler.class);
	private static CapabilitiesService capabilities = CapabilitiesServiceFactory
	        .getCapabilitiesService();

	public AccountServiceHandler()
	{
		checkDefaultAccount();
	}

	private Event handleUserOperationEvent(UserOperationEvent ev)
	{
		Object[] attach = (Object[]) ev.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		User oprUser = UserManagementService.getUserWithToken(tags.token);
		User user = ev.user;
		String res = null;
		switch (ev.opr)
		{
			case ADD:
			{
				res = createUser(oprUser, user.getEmail(), user.getGroup(),
				        user.getPasswd());
				break;
			}
			case DELETE:
			{
				res = deleteUser(oprUser, user.getEmail());
				break;
			}
			case MODIFY:
			{
				res = modifyPassword(oprUser, user.getEmail(), user.getPasswd());
				break;
			}
			default:
			{
				res = "Not supported!";
				break;
			}
		}

		return new AdminResponseEvent("Success", res, 0);
	}

	private Event handleGroupOperationEvent(GroupOperationEvent ev)
	{
		Object[] attach = (Object[]) ev.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		User oprUser = UserManagementService.getUserWithToken(tags.token);
		Group group = ev.grp;
		String res = null;
		switch (ev.opr)
		{
			case ADD:
			{
				res = createGroup(oprUser, group.getName());
				break;
			}
			case DELETE:
			{
				res = deleteGroup(oprUser, group.getName());
				break;
			}
			default:
			{
				res = "Not supported!";
				break;
			}
		}

		return new AdminResponseEvent("Success", res, 0);
	}

	private Event handleBlackListOperationEvent(BlackListOperationEvent ev)
	{
		Object[] attach = (Object[]) ev.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		User oprUser = UserManagementService.getUserWithToken(tags.token);
		String res = null;
		if (null != ev.username)
		{
			res = operationOnUserBlackList(oprUser, ev.username, ev.host,
			        ev.opr);
		}
		else
		{
			res = operationOnGroupBlackList(oprUser, ev.groupname, ev.host,
			        ev.opr);
		}
		return new AdminResponseEvent("Success", res, 0);
	}

	public Event handleEvent(int type, Event ev)
	{
		Object[] attach = (Object[]) ev.getAttachment();
		EventHeaderTags tags = (EventHeaderTags) attach[0];
		User oprUser = UserManagementService.getUserWithToken(tags.token);
		switch (type)
		{
			case GAEConstants.AUTH_REQUEST_EVENT_TYPE:
			{
				AuthRequestEvent req = (AuthRequestEvent) ev;
				User user = UserManagementService.getUserWithName(req.user);
				if (null != user && user.getPasswd().equals(req.passwd))
				{
					return new AuthResponseEvent(req.appid,
					        user.getAuthToken(), null);
				}
				else
				{
					return new AuthResponseEvent(req.appid, null,
					        "Invalid user/passwd.");
				}
			}
			case GAEConstants.USER_OPERATION_EVENT_TYPE:
			{
				UserOperationEvent event = (UserOperationEvent) ev;
				return handleUserOperationEvent(event);
			}
			case GAEConstants.GROUP_OPERATION_EVENT_TYPE:
			{
				GroupOperationEvent event = (GroupOperationEvent) ev;
				return handleGroupOperationEvent(event);
			}
			case GAEConstants.USER_LIST_REQUEST_EVENT_TYPE:
			{
				ListUserResponseEvent res = new ListUserResponseEvent();
				res.users = new LinkedList<User>();
				String ret = getUsersInfo(oprUser, res.users);
				if (ret != null)
				{
					return new AdminResponseEvent("Failed", ret, 0);
				}
				return res;
			}
			case GAEConstants.GROUOP_LIST_REQUEST_EVENT_TYPE:
			{
				ListGroupResponseEvent res = new ListGroupResponseEvent();
				res.groups = new LinkedList<Group>();
				String ret = getGroupsInfo(oprUser, res.groups);
				if (ret != null)
				{
					return new AdminResponseEvent("Failed", ret, 0);
				}
				return res;
			}
			case GAEConstants.BLACKLIST_OPERATION_EVENT_TYPE:
			{
				BlackListOperationEvent event = (BlackListOperationEvent) ev;
				return handleBlackListOperationEvent(event);
			}
		}
		return null;
	}

	protected String assertRootAuth(User user)
	{
		if (!user.getEmail().equals(GAEConstants.ROOT_NAME))
		{
			return GAEConstants.AUTH_FAILED;
		}
		return null;
	}

	protected void sendAccountMail(String to, String passwd, boolean isCreate)
	{
		String appid = SystemProperty.applicationId.get();
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try
		{
			Message msg = new MimeMessage(session);
			StringBuffer buffer = new StringBuffer();
			buffer.append("Hi, ").append(to).append("\r\n\r\n");
			if (isCreate)
			{

				buffer.append("You account on ").append(appid + ".appspot.com")
				        .append(" has been created.").append("\r\n");
				buffer.append("    Username:").append(to).append("\r\n");
				buffer.append("    Password:").append(passwd).append("\r\n");
				msg.setSubject("Your account has been activated");
			}
			else
			{
				msg.setSubject("Your account has been deleted.");
				buffer.append("You account on ").append(appid + ".appspot.com")
				        .append(" has been deleted.").append("\r\n");
			}

			buffer.append("Thanks again for registering, admin@" + appid
			        + ".appspot.com");
			String msgBody = buffer.toString();

			msg.setFrom(new InternetAddress("admin@" + appid
			        + ".appspotmail.com"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to,
			        "Mr. User"));

			// msg.set
			msg.setText(msgBody);
			Transport.send(msg);
		}
		catch (Exception e)
		{
			logger.error("Failed to send mail to user:" + to, e);
		}
	}

	protected static boolean createGroupIfNotExist(String groupName)
	{
		Group group = UserManagementService.getGroup(groupName);
		if (null == group)
		{
			group = new Group();
			group.setName(groupName);
			UserManagementService.saveGroup(group);
			return false;
		}
		return true;
	}

	private static String generateAuthToken()
	{
		String token = null;
		do
		{
			token = RandomHelper.generateRandomString(10);
		}
		while (UserManagementService.getUserWithToken(token) != null);
		return token;
	}

	protected static boolean createUserIfNotExist(String email, String groupName)
	{
		User user = UserManagementService.getUserWithName(email);
		if (null == user)
		{
			user = new User();
			user.setEmail(email);
			user.setGroup(groupName);
			if (email.equals(GAEConstants.ANONYMOUSE_NAME))
			{
				user.setPasswd(GAEConstants.ANONYMOUSE_NAME);
			}
			else
			{
				user.setPasswd(RandomHelper.generateRandomString(10));
			}
			user.setAuthToken(generateAuthToken());
			UserManagementService.saveUser(user);
		}
		else
		{
			if (user.getAuthToken() == null)
			{
				user.setAuthToken(generateAuthToken());
				UserManagementService.saveUser(user);
			}
		}
		return true;
	}

	/**
	 * Create it if it not exist
	 */
	public static void checkDefaultAccount()
	{
		if(capabilities.getStatus(Capability.MEMCACHE).getStatus().equals(CapabilityStatus.DISABLED)
				|| capabilities.getStatus(Capability.DATASTORE).getStatus().equals(CapabilityStatus.DISABLED))
		{
			logger.error("Just return since memcach/datastrore is not available.");
			return ;
		}
		createGroupIfNotExist(GAEConstants.ROOT_GROUP_NAME);
		createUserIfNotExist(GAEConstants.ROOT_NAME,
		        GAEConstants.ROOT_GROUP_NAME);
		createGroupIfNotExist(GAEConstants.PUBLIC_GROUP_NAME);
		createGroupIfNotExist(GAEConstants.ANONYMOUSE_NAME);
		createUserIfNotExist(GAEConstants.ANONYMOUSE_NAME,
		        GAEConstants.ANONYMOUSE_GROUP_NAME);

	}

	public static User getRootUser()
	{
		return UserManagementService.getUserWithName(GAEConstants.ROOT_NAME);
	}

	public String createGroup(User user, String groupname)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}
		Group g = UserManagementService.getGroup(groupname);
		if (null != g)
		{
			return GAEConstants.GRP_EXIST;
		}
		g = new Group();
		g.setName(groupname);
		UserManagementService.saveGroup(g);
		// ServerUtils.cacheGroup(g);
		return null;
	}

	public String createUser(User user, String username, String groupname,
	        String passwd)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}
		try
		{
			Group g = UserManagementService.getGroup(groupname);
			if (null == g)
			{
				return GAEConstants.GRP_NOTFOUND;
			}
			User u = UserManagementService.getUserWithName(username);
			if (null != u)
			{
				return GAEConstants.USER_EXIST;
			}
			Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
			Matcher m = p.matcher(username);
			boolean matchFound = m.matches();
			if (!matchFound)
			{
				return "Username MUST be a email address!";
			}
			u = new User();
			u.setEmail(username);
			u.setGroup(groupname);
			u.setPasswd(passwd);
			String token = null;
			do
			{
				token = RandomHelper.generateRandomString(10);
			}
			while (UserManagementService.getUserWithToken(token) != null);
			u.setAuthToken(token);
			UserManagementService.saveUser(u);
			// ServerUtils.cacheUser(u);
			sendAccountMail(username, passwd, true);
		}
		catch (Throwable e)
		{
			logger.error("Failed to create user.", e);
			return "Failed to create user." + e.getMessage();
		}

		return null;
	}

	public String modifyPassword(User user, String username, String newPass)
	{
		if (user.getEmail().equals(GAEConstants.ANONYMOUSE_NAME))
		{
			return "Can't modify anonymous user's password!";
		}
		if (user.getEmail().equals(GAEConstants.ROOT_NAME)
		        || user.getEmail().equals(username))
		{
			User modifyuser = UserManagementService.getUserWithName(username);
			if (null == modifyuser)
			{
				return GAEConstants.USER_NOTFOUND;
			}
			if (null == newPass)
			{
				return "New password can't be empty!";
			}
			modifyuser.setPasswd(newPass);
			UserManagementService.saveUser(modifyuser);
			return null;
		}
		return GAEConstants.AUTH_FAILED;
	}

	public String getGroupsInfo(User user, List<Group> grps)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}
		List<Group> groups = UserManagementService.getAllGroups();
		grps.addAll(groups);
		return null;

	}

	public String getUsersInfo(User user, List<User> users)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}

		List<User> ret = UserManagementService.getAllUsers();
		users.addAll(ret);
		return null;

	}

	public String deleteGroup(User user, String groupname)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}

		if (groupname.equals(GAEConstants.ROOT_NAME)
		        || groupname.equals(GAEConstants.ANONYMOUSE_NAME)
		        || groupname.equals(GAEConstants.PUBLIC_GROUP_NAME))
		{
			return "Deletion not allowed!";
		}

		Group g = UserManagementService.getGroup(groupname);
		if (null == g)
		{
			return GAEConstants.GRP_NOTFOUND;
		}
		UserManagementService.deleteGroup(g);
		return null;

	}

	public String deleteUser(User user, String username)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}

		if (username.equals(GAEConstants.ROOT_NAME)
		        || username.equals(GAEConstants.ANONYMOUSE_NAME))
		{
			return "Deletion not allowed!";
		}
		try
		{
			User u = UserManagementService.getUserWithName(username);
			if (null == u)
			{
				return GAEConstants.USER_NOTFOUND;
			}
			// ServerUtils.removeUserCache(u);
			sendAccountMail(u.getEmail(), u.getPasswd(), false);
			// pm.deletePersistent(u);
			UserManagementService.deleteUser(u);
			return null;
		}
		catch (Throwable e)
		{
			logger.error("Failed to delete user.", e);
			return "Failed to delete user." + e.getMessage();
		}
	}

	public String operationOnGroupBlackList(User user, String groupname,
	        String host, Operation operation)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}

		Group g = UserManagementService.getGroup(groupname);
		if (null == g)
		{
			return GAEConstants.GRP_NOTFOUND;
		}
		Set<String> blacklist = g.getBlacklist();
		if (null == blacklist)
		{
			blacklist = new HashSet<String>();
		}
		switch (operation)
		{
			case ADD:
			{
				blacklist.add(host);
				break;
			}
			case DELETE:
			{
				blacklist.remove(host);
				break;
			}
		}
		g.setBlacklist(blacklist);
		UserManagementService.saveGroup(g);
		return null;
	}

	public String operationOnUserBlackList(User user, String username,
	        String host, Operation operation)
	{
		if (assertRootAuth(user) != null)
		{
			return assertRootAuth(user);
		}

		User u = UserManagementService.getUserWithName(username);
		if (null == u)
		{
			return GAEConstants.USER_NOTFOUND;
		}
		Set<String> blacklist = u.getBlacklist();
		if (null == blacklist)
		{
			blacklist = new HashSet<String>();
		}
		switch (operation)
		{
			case ADD:
			{
				blacklist.add(host);
				break;
			}
			case DELETE:
			{
				blacklist.remove(host);
				break;
			}
		}
		u.setBlacklist(blacklist);
		UserManagementService.saveUser(u);
		return null;

	}

}
