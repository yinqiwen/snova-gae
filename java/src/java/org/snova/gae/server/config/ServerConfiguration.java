/**
 * 
 */
package org.snova.gae.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.arch.config.IniProperties;
import org.arch.event.misc.CompressorType;
import org.arch.event.misc.EncryptType;
import org.arch.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.auth.User;

/**
 * @author qiyingwang
 * 
 */
public class ServerConfiguration
{
	protected static Logger	logger	        = LoggerFactory
	                                                .getLogger(ServerConfiguration.class);
	private int	            rangeFetchLimit	= 256 * 1024;
	
	public CompressorType getCompressor()
	{
		return compressor;
	}
	
	public void setCompressor(CompressorType compressor)
	{
		this.compressor = compressor;
	}
	
	public EncryptType getEncrypter()
	{
		return encrypter;
	}
	
	public void setEncrypter(EncryptType encypter)
	{
		this.encrypter = encypter;
	}
	
	private CompressorType	compressor	  = CompressorType.LZ4;
	private EncryptType	   encrypter	  = EncryptType.SE1;
	
	private Set<String>	   compressFilter	= new HashSet<String>();
	private Set<String>	   blacklist	  = new HashSet<String>();
	private Set<User>	   allusers	      = new HashSet<User>();
	
	private ServerConfiguration()
	{
		compressFilter.add("audio");
		compressFilter.add("video");
		compressFilter.add("image");
		compressFilter.add("/zip");
		compressFilter.add("/x-gzip");
		compressFilter.add("/x-zip-compressed");
		compressFilter.add("/x-compress");
		compressFilter.add("/x-compressed");
		compressFilter.add("/x-msdos-program");
	}
	
	public Set<User> getAllUser()
	{
		return allusers;
	}
	
	public boolean isValidUser(User user)
	{
		return allusers.contains(user);
	}
	
	public boolean isInBlacklist(String host)
	{
		for (String rule : blacklist)
		{
			if (host.indexOf(rule) != -1)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isContentTypeInCompressFilter(String type)
	{
		type = type.toLowerCase();
		for (String filter : compressFilter)
		{
			if (type.indexOf(filter) != -1)
			{
				return true;
			}
		}
		return false;
	}
	
	public Set<String> getCompressFilter()
	{
		return compressFilter;
	}
	
	public void setCompressFilter(Set<String> compressFilter)
	{
		this.compressFilter = compressFilter;
	}
	
	public int getRangeFetchLimit()
	{
		return rangeFetchLimit;
	}
	
	public void setRangeFetchLimit(int rangeFetchLimit)
	{
		this.rangeFetchLimit = rangeFetchLimit;
	}
	
	private static ServerConfiguration	cfg	= new ServerConfiguration();
	
	public static ServerConfiguration getServerConfig()
	{
		return cfg;
	}
	
	private ServerConfiguration fromIni(IniProperties ini)
	{
		cfg.setRangeFetchLimit(ini.getIntProperty("Misc", "RangeFetchLimit",
		        262144));
		cfg.setCompressor(CompressorType.valueOf(ini.getProperty("Compress",
		        "Compressor", "Snappy").toUpperCase()));
		cfg.setEncrypter(EncryptType.valueOf(ini.getProperty("Misc",
		        "Encrypter", "SE1").toUpperCase()));
		
		String str = ini.getProperty("Compress", "Filter");
		if (!StringHelper.isEmptyString(str))
		{
			String[] ss = str.split("\\|");
			Set<String> set = new HashSet<String>();
			for (String s : ss)
			{
				s = s.trim();
				if (!s.isEmpty())
				{
					set.add(s);
				}
			}
			cfg.setCompressFilter(set);
		}
		str = ini.getProperty("Auth", "Blacklist");
		if (!StringHelper.isEmptyString(str))
		{
			String[] ss = str.split("\\|");
			Set<String> set = new HashSet<String>();
			for (String s : ss)
			{
				s = s.trim();
				if (!s.isEmpty())
				{
					set.add(s);
				}
			}
			cfg.blacklist = set;
		}
		str = ini.getProperty("Auth", "Users");
		if (!StringHelper.isEmptyString(str))
		{
			String[] ss = str.split("\\|");
			for (String s : ss)
			{
				String[] xs = s.split(":");
				if (xs.length == 2)
				{
					cfg.allusers.add(new User(xs[1], xs[0]));
				}
			}
		}
		return cfg;
	}
	
	public static ServerConfiguration initServerConfig(ServletContext ctx)
	{
		IniProperties ini = new IniProperties();
		try
		{
			InputStream is = ctx.getResourceAsStream("/WEB-INF/snova.conf");
			ini.load(is);
			cfg.fromIni(ini);
		}
		catch (IOException e)
		{
			logger.error("Failed to init cfg.", e);
		}
		return cfg;
	}
}
