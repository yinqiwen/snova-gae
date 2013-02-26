/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: ServerConfigurationService.java 
 *
 * @author yinqiwen [ 2011-12-3 | ÏÂÎç03:14:28 ]
 *
 */
package org.snova.gae.server.service;

import java.util.HashSet;
import java.util.Set;

import org.arch.event.Event;
import org.arch.event.misc.CompressorType;
import org.arch.event.misc.EncryptType;
import org.snova.gae.common.EventHeaderTags;
import org.snova.gae.common.config.GAEServerConfiguration;
import org.snova.gae.common.event.AdminResponseEvent;
import org.snova.gae.common.event.ServerConfigEvent;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 *
 */
public class ServerConfigurationService
{
	private static DatastoreService datastore = DatastoreServiceFactory
	        .getDatastoreService();
	private static AsyncDatastoreService asyncdatastore = DatastoreServiceFactory
	        .getAsyncDatastoreService();
	private static GAEServerConfiguration cfg;

	private static Entity toEntity(GAEServerConfiguration cfg)
	{
		Entity entity = new Entity("ServerConfig", 1);
		entity.setProperty("FetchRetryCount", "" + cfg.getFetchRetryCount());
		entity.setProperty("MaxXMPPDataPackageSize", "" + cfg.getMaxXMPPDataPackageSize());
		entity.setProperty("RangeFetchLimit", "" + cfg.getRangeFetchLimit());
		entity.setProperty("Compressor", "" + cfg.getCompressor().toString());
		entity.setProperty("Encrypter", "" + cfg.getEncrypter().toString());
		entity.setProperty("IsMaster", cfg.isMasterNode() ? "1":"0");
		//entity.setProperty("TrafficStatEnable", "" + cfg.isTrafficStatEnable());
		Set<String> set = cfg.getCompressFilter();
		StringBuilder buffer = new StringBuilder();
		if(null != set)
		{
			for(String s:set)
			{
				if(!s.isEmpty())
				{
					buffer.append(s).append(";");
				}
			}
		}
		entity.setProperty("CompressFilter", buffer.toString());
		return entity;
	}
	
	private static GAEServerConfiguration fromEntity(Entity entity)
	{
		GAEServerConfiguration cfg = new GAEServerConfiguration();
		if(null != entity.getProperty("FetchRetryCount"))
		{
			cfg.setFetchRetryCount(Integer.parseInt((String) entity.getProperty("FetchRetryCount")));
		}
		if(null != entity.getProperty("MaxXMPPDataPackageSize"))
		{
			cfg.setMaxXMPPDataPackageSize(Integer.parseInt((String) entity.getProperty("MaxXMPPDataPackageSize")));
		}
		if(null != entity.getProperty("RangeFetchLimit"))
		{
			cfg.setRangeFetchLimit(Integer.parseInt((String) entity.getProperty("RangeFetchLimit")));
		}
		if(null != entity.getProperty("Compressor"))
		{
			cfg.setCompressor(CompressorType.valueOf((String) entity.getProperty("Compressor")));
		}
		if(null != entity.getProperty("Encrypter"))
		{
			cfg.setEncrypter(EncryptType.valueOf((String) entity.getProperty("Encrypter")));
		}
		if(null != entity.getProperty("IsMaster"))
		{
			cfg.setMasterNode(Integer.valueOf((String) entity.getProperty("IsMaster")) == 1);
		}
	
		String str = (String) entity.getProperty("CompressFilter");
		if(null != str)
		{
			String[] ss = str.split(";");
			Set<String> set  = new HashSet<String>();
			for(String s:ss)
			{
				s = s.trim();
				if(!s.isEmpty())
				{
					set.add(s);
				}
			}
			cfg.setCompressFilter(set);
		}
		return cfg;
	}
	
	public static GAEServerConfiguration getServerConfig()
	{
		if(null == cfg)
		{
			Key key = KeyFactory.createKey("ServerConfig", 1);
			try
            {
                Entity entity = datastore.get(key);
                cfg = fromEntity(entity);
            }
            catch (EntityNotFoundException e)
            {
            	saveServerConfig(new GAEServerConfiguration());    
            	cfg = getServerConfig();
            }
			
		}
		return cfg;
	}
	
	public static Event handleServerConfig(EventHeaderTags tags,ServerConfigEvent event)
	{
		if(!UserManagementService.isRootUser(tags.token))
		{
			return new AdminResponseEvent("", "User must be root.", -1);
		}
		
		switch (event.opreration)
        {
	        case ServerConfigEvent.GET_CONFIG_REQ:
	        {   
	        	GAEServerConfiguration cfg = getServerConfig();
	        	ServerConfigEvent res = new ServerConfigEvent();
	        	res.opreration = ServerConfigEvent.GET_CONFIG_RES;
	        	res.cfg = cfg;
		        return res;
	        }
	        case ServerConfigEvent.SET_CONFIG_REQ:
	        {
	        	saveServerConfig(event.cfg);
	        	ServerConfigEvent res = new ServerConfigEvent();
	        	res.opreration = ServerConfigEvent.SET_CONFIG_RES;
	        	res.cfg = getServerConfig();
		        return res;
	        }
	        default:
	        {
		        return new AdminResponseEvent("", "Unsupported config operation:" + event.opreration, 0);
	        }
        }
	}

	private static void saveServerConfig(GAEServerConfiguration cfg)
	{
		ServerConfigurationService.cfg = cfg;
    	Entity entity = toEntity(cfg);
    	asyncdatastore.put(entity);
	}

}
