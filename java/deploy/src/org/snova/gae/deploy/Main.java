/**
 * 
 */
package org.snova.gae.deploy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author yinqiwen
 * 
 */
public class Main
{
	
	private static void replaceDest(String host, String replaceHost)
	        throws NoSuchMethodException, SecurityException,
	        IllegalAccessException, IllegalArgumentException,
	        InvocationTargetException, UnknownHostException
	{
		Method m = InetAddress.class.getDeclaredMethod("cacheAddresses",
		        String.class, InetAddress[].class, boolean.class);
		m.setAccessible(true);
		m.invoke(null, host,
		        new InetAddress[] { InetAddress.getByName(replaceHost) },
		        Boolean.TRUE);
	}
	
	private static void output(String str)
	{
		System.out.println(str);
	}
	
	private static String input(String str)
	{
		System.out.print(str);
		byte[] buf = new byte[4096];
		try
		{
			int len = System.in.read(buf);
			return new String(buf, 0, len);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private static String readEmailArgs()
	{
		String x = input(
		        "Specify google email account first? (y/n, default n):").trim();
		if (x.equalsIgnoreCase("y"))
		{
			String email = input("Email:").trim();
			if (email.length() > 0)
			{
				return "--email=" + email;
			}
		}
		return null;
	}
	
	private static String readActionArgs()
	{
		String x = input("Enter your action?(0:update/1:rollback, default 0):")
		        .trim();
		if (x.equals("0") || x.length() == 0)
		{
			return "update";
		}
		else if (x.equals("1"))
		{
			return "rollback";
		}
		output(String
		        .format("[WARN]:Invalid action choice:%s, use default 'update' instead",
		                x));
		return "update";
	}
	
	private static String getProxyArgs()
	{
		String x = input(
		        "Want to set snova as proxy for deployer?(y/n, default n):")
		        .trim();
		if (x.equalsIgnoreCase("y"))
		{
			return "--proxy_https=127.0.0.1:48100";
		}
		return null;
	}
	
	private static boolean isReachable(String host, int port)
	{
		try
		{
			Socket s = new Socket();
			s.connect(new InetSocketAddress(host, port), 2000);
			s.close();
			return true;
		}
		catch (Exception e)
		{
			
		}
		return false;
	}
	
	private static String selectReachableGoogleHost()
	{
		String[] googlehosts = new String[] { "www.google.com.hk",
		        "maps.google.com", "www.google.co.jp", "www.google.co.kr",
		        "www.google.com.tw" };
		for (String host : googlehosts)
		{
			if (isReachable(host, 443))
			{
				return host;
			}
		}
		return null;
	}
	
	private static void adjustHostSetting() throws NoSuchMethodException,
	        SecurityException, IllegalAccessException,
	        IllegalArgumentException, InvocationTargetException,
	        UnknownHostException
	{
		
		if (!isReachable("appengine.google.com", 443))
		{
			String host = selectReachableGoogleHost();
			if (null != host)
			{
				replaceDest("appengine.google.com", host);
			}
		}
	}
	
	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws UnknownHostException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void main(String[] args) throws NoSuchMethodException,
	        SecurityException, IllegalAccessException,
	        IllegalArgumentException, InvocationTargetException,
	        UnknownHostException
	{
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			
			public void run()
			{
				input("Input any key to exit:");
			}
		}));
		ArrayList<String> appcfgArgs = new ArrayList<String>();
		if (args.length == 0)
		{
			output("==================Snova(Java)Deployer v0.22.0===================");
			String proxy = getProxyArgs();
			if (null != proxy)
			{
				appcfgArgs.add(proxy);
			}
			String email = readEmailArgs();
			if (null != email)
			{
				appcfgArgs.add(email);
			}
			appcfgArgs.add("--passin");
			String action = readActionArgs();
			output("Enter appid, use ',' as separator if you have more than 1 appid.");
			String tmp = input("AppID:").trim();
			String[] appids = tmp.split(",");
			String[] newargs = new String[appcfgArgs.size() + 4];
			appcfgArgs.toArray(newargs);
			int newargssize = newargs.length;
			try
			{
				for (String appid : appids)
				{
					String[] ss = appid.split("\\.");
					String version = "1";
					if (ss.length == 2)
					{
						appid = ss[0];
						version = ss[1];
					}
					newargs[newargssize - 4] = "--application=" + appid;
					newargs[newargssize - 3] = "--version=" + version;
					newargs[newargssize - 2] = action;
					newargs[newargssize - 1] = "./war";
					args = newargs;
					output(String.format(
					        "==============Start %s AppID:%s===============",
					        action, appid));
					if (null == proxy)
					{
						adjustHostSetting();
					}
					com.google.appengine.tools.admin.AppCfg.main(args);
					output(String.format(
					        "==============End %s AppID:%s===============",
					        action, appid));
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				output("Oops!  Exception happens when deploy snova server.");
			}
			return;
		}
		com.google.appengine.tools.admin.AppCfg.main(args);
	}
}
