/**
 * 
 */
package org.snova.gae.server.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.arch.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.GAEConstants;
import org.snova.gae.common.auth.Group;
import org.snova.gae.common.auth.User;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author qiyingwang
 * 
 */
public class UserManagementService
{
	protected static Logger logger = LoggerFactory
	        .getLogger(UserManagementService.class);
	private static CapabilitiesService capabilities = CapabilitiesServiceFactory
	        .getCapabilitiesService();
	private static DatastoreService datastore = DatastoreServiceFactory
	        .getDatastoreService();
	private static AsyncDatastoreService asyncdatastore = DatastoreServiceFactory
	        .getAsyncDatastoreService();
	protected static AsyncMemcacheService asyncCache = MemcacheServiceFactory
	        .getAsyncMemcacheService();
	protected static MemcacheService cache = MemcacheServiceFactory
	        .getMemcacheService();
	protected static Map<String, Object> localMemCache = new ConcurrentHashMap<String, Object>();

	public static void saveUser(User user)
	{
		Entity usrEntity = new Entity(GAEConstants.USER_ENTITY_NAME,
		        user.getEmail());
		usrEntity.setProperty("Name", user.getEmail());
		usrEntity.setProperty("Passwd", user.getPasswd());
		usrEntity.setProperty("Group", user.getGroup());
		usrEntity.setProperty("AuthToken", user.getAuthToken());
		usrEntity.setProperty("BlackList", user.getBlacklistString());
		datastore.put(usrEntity);
		Buffer buffer = new Buffer(128);
		user.encode(buffer);
		byte[] content = buffer.toArray();
		asyncCache.put(
		        GAEConstants.USER_ENTITY_NAME + ":" + user.getAuthToken(),
		        content);
		asyncCache.put(GAEConstants.USER_ENTITY_NAME + ":" + user.getEmail(),
		        content);
		localMemCache
		        .put(GAEConstants.USER_ENTITY_NAME + ":" + user.getAuthToken(),
		                user);
		localMemCache.put(
		        GAEConstants.USER_ENTITY_NAME + ":" + user.getEmail(), user);
	}

	public static Group getGroup(String groupName)
	{
		String key = GAEConstants.GROUP_ENTITY_NAME + ":" + groupName;
		Group grp = (Group) localMemCache.get(key);
		if (null != grp)
		{
			return grp;
		}
		byte[] content = (byte[]) cache.get(key);
		if (null != content)
		{
			Buffer buf = Buffer.wrapReadableContent(content);
			grp = new Group();
			if (grp.decode(buf))
			{
				localMemCache.put(key, grp);
				return grp;
			}
			else
			{
				asyncCache.delete(key);
			}
		}

		Key groupKey = KeyFactory.createKey(GAEConstants.GROUP_ENTITY_NAME,
		        groupName);
		try
		{
			Entity grpEntity = datastore.get(groupKey);
			grp = new Group();
			grp.setName(groupName);
			grp.setBlacklistString((String) grpEntity.getProperty("BlackList"));
			Buffer buffer = new Buffer(128);
			grp.encode(buffer);
			content = buffer.toArray();
			asyncCache.put(key, content);
			localMemCache.put(key, grp);
			return grp;
		}
		catch (EntityNotFoundException e)
		{
			logger.error("Failed to get group entity for name:" + groupName, e);
			return null;
		}

	}

	public static void saveGroup(Group group)
	{
		Entity usrEntity = new Entity(GAEConstants.GROUP_ENTITY_NAME,
		        group.getName());
		usrEntity.setProperty("Name", group.getName());
		usrEntity.setProperty("BlackList", group.getBlacklistString());
		datastore.put(usrEntity);
		// datastore.
		Buffer buffer = new Buffer(128);
		group.encode(buffer);
		byte[] content = buffer.toArray();
		String key = GAEConstants.GROUP_ENTITY_NAME + ":" + group.getName();
		asyncCache.put(key, content);
		localMemCache.put(key, group);
	}

	private static User getUserFromCache(String name)
	{
		String key = GAEConstants.USER_ENTITY_NAME + ":" + name;
		User user = (User) localMemCache.get(key);
		if (null != user)
		{
			return user;
		}
		byte[] content = (byte[]) cache.get(key);
		if (null != content)
		{
			Buffer buf = Buffer.wrapReadableContent(content);
			user = new User();
			if (user.decode(buf))
			{
				localMemCache.put(key, user);
				return user;
			}
			else
			{
				asyncCache.delete(key);
			}
		}
		return null;
	}

	public static User getUserWithName(String email)
	{
		User user = getUserFromCache(email);
		if (null == user)
		{
			Key usrKey = KeyFactory.createKey(GAEConstants.USER_ENTITY_NAME,
			        email);
			try
			{
				Entity usrEntity = datastore.get(usrKey);
				user = new User();
				user.setEmail(email);
				user.setPasswd((String) usrEntity.getProperty("Passwd"));
				user.setAuthToken((String) usrEntity.getProperty("AuthToken"));
				user.setGroup((String) usrEntity.getProperty("Group"));
				user.setBlacklistString((String) usrEntity
				        .getProperty("BlackList"));
				Buffer buffer = new Buffer(128);
				user.encode(buffer);
				byte[] content = buffer.toArray();
				String key = GAEConstants.USER_ENTITY_NAME + ":" + email;
				asyncCache.put(key, content);
				localMemCache.put(key, user);
			}
			catch (EntityNotFoundException e)
			{
				logger.error("Failed to get user entity for name:" + email, e);
				return null;
			}
		}
		return user;
	}

	public static User getUserWithToken(String token)
	{
		User user = getUserFromCache(token);
		if (null == user)
		{
			Query q = new Query(GAEConstants.USER_ENTITY_NAME);
			q.addFilter("AuthToken", Query.FilterOperator.EQUAL, token);
			PreparedQuery pq = datastore.prepare(q);

			for (Entity result : pq.asIterable())
			{
				user = new User();
				user.setEmail((String) result.getProperty("Name"));
				user.setPasswd((String) result.getProperty("Passwd"));
				user.setAuthToken((String) result.getProperty("AuthToken"));
				user.setGroup((String) result.getProperty("Group"));
				user.setBlacklistString((String) result
				        .getProperty("BlackList"));
				Buffer buffer = new Buffer(128);
				user.encode(buffer);
				byte[] content = buffer.toArray();
				String key = GAEConstants.USER_ENTITY_NAME + ":" + token;
				asyncCache.put(key, content);
				localMemCache.put(key, user);
			}
		}
		return user;
	}

	public static List<Group> getAllGroups()
	{
		List<Group> grps = new LinkedList<Group>();
		Query q = new Query(GAEConstants.GROUP_ENTITY_NAME);
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable())
		{
			Group grp = new Group();
			if(null != result.getProperty("Name"))
			{
				grp.setName((String) result.getProperty("Name"));
			}
			else
			{
				grp.setName(result.getKey().getName());
			}
			
			grp.setBlacklistString((String) result.getProperty("BlackList"));
			Buffer buffer = new Buffer(128);
			grp.encode(buffer);
			byte[] content = buffer.toArray();
			String key = GAEConstants.GROUP_ENTITY_NAME + ":" + grp.getName();
			asyncCache.put(key, content);
			localMemCache.put(key, grp);
			grps.add(grp);
		}
		return grps;
	}

	public static List<User> getAllUsers()
	{
		List<User> users = new LinkedList<User>();
		Query q = new Query(GAEConstants.USER_ENTITY_NAME);
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable())
		{
			User user = new User();
			user.setEmail((String) result.getProperty("Name"));
			user.setPasswd((String) result.getProperty("Passwd"));
			user.setAuthToken((String) result.getProperty("AuthToken"));
			user.setGroup((String) result.getProperty("Group"));
			user.setBlacklistString((String) result.getProperty("BlackList"));
			Buffer buffer = new Buffer(128);
			user.encode(buffer);
			byte[] content = buffer.toArray();
			String key = GAEConstants.USER_ENTITY_NAME + ":" + user.getEmail();
			String key1 = GAEConstants.USER_ENTITY_NAME + ":"
			        + user.getAuthToken();
			asyncCache.put(key, content);
			asyncCache.put(key1, content);
			localMemCache.put(key, user);
			localMemCache.put(key1, user);
			users.add(user);
		}
		return users;
	}

	public static void deleteGroup(Group g)
	{
		datastore.delete(KeyFactory.createKey(GAEConstants.GROUP_ENTITY_NAME,
		        g.getName()));
		String key = "Group:" + g.getName();
		localMemCache.remove(key);
		asyncCache.delete(key);
	}

	public static void deleteUser(User u)
	{
		datastore.delete(KeyFactory.createKey(GAEConstants.USER_ENTITY_NAME,
		        u.getEmail()));
		String key = "User:" + u.getEmail();
		String key1 = "User:" + u.getAuthToken();
		localMemCache.remove(key);
		asyncCache.delete(key);
		localMemCache.remove(key1);
		asyncCache.delete(key1);
	}

	public static boolean userAuthServiceAvailable(String token)
	{
		String key = GAEConstants.USER_ENTITY_NAME + ":" + token;
		if (localMemCache.containsKey(key))
		{
			return true;
		}
		CapabilityStatus status = capabilities.getStatus(Capability.MEMCACHE)
		        .getStatus();
		if (status.equals(CapabilityStatus.DISABLED))
		{
			return false;
		}

		status = capabilities.getStatus(Capability.DATASTORE).getStatus();
		if (status.equals(CapabilityStatus.DISABLED))
		{
			return false;
		}
		return true;
	}
	
	public static boolean isRootUser(String token)
	{
		User user = getUserWithToken(token);
		if(user != null && user.getEmail().equals(GAEConstants.ROOT_NAME))
		{
			return true;
		}
		return false;
	}
}
