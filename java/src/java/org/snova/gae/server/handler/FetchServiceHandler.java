/**
 * 
 */
package org.snova.gae.server.handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.arch.buffer.Buffer;
import org.arch.common.KeyValuePair;
import org.arch.event.Event;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.gae.common.http.RangeHeaderValue;
import org.snova.gae.server.config.ServerConfiguration;

/**
 * @author qiyingwang
 * 
 */
public class FetchServiceHandler
{
	protected Logger	logger	= LoggerFactory.getLogger(getClass());
	
	public FetchServiceHandler()
	{
	}
	
	private void fillErrorResponse(HTTPResponseEvent errorResponse, String cause)
	{
		String str = "You are not allowed to visit this site via proxy because %s.";
		String ret = String.format(str, cause);
		errorResponse.setHeader("Content-Type", "text/plain");
		errorResponse.setHeader("Content-Length", "" + ret.length());
		errorResponse.content.write(ret.getBytes());
	}
	
	private HTTPResponseEvent fetchResponse(HTTPRequestEvent req)
	        throws IOException
	{
		HTTPResponseEvent errorResponse = new HTTPResponseEvent();
		if (ServerConfiguration.getServerConfig().isInBlacklist(
		        req.getHeader("Host")))
		{
			fillErrorResponse(errorResponse,
			        "This site:" + req.getHeader("Host") + " is in blacklist.");
			return errorResponse;
		}
		
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(req.url);
			connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);
			if (req.content.readable())
			{
				connection.setDoOutput(true);
			}
			connection.setRequestMethod(req.method);
			for (KeyValuePair<String, String> header : req.getHeaders())
			{
				connection.addRequestProperty(header.getName(),
				        header.getValue());
			}
			connection.connect();
			if (req.content.readable())
			{
				connection.getOutputStream()
				        .write(req.content.getRawBuffer(),
				                req.content.getReadIndex(),
				                req.content.readableBytes());
			}
		}
		catch (Exception e)
		{
			errorResponse.statusCode = 400;
			fillErrorResponse(errorResponse, "Invalid fetch url:" + req.url);
			return errorResponse;
		}
		HTTPResponseEvent responseEvent = new HTTPResponseEvent();
		responseEvent.statusCode = connection.getResponseCode();
		
		if (responseEvent.statusCode == 302 && req.containsHeader("Range"))
		{
			responseEvent.addHeader("X-Range", req.getHeader("Range"));
		}
		Map<String, List<String>> rh = connection.getHeaderFields();
		for (String name : rh.keySet())
		{
			if (null != name && name.length() > 0)
			{
				List<String> vs = rh.get(name);
				for (String v : vs)
				{
					responseEvent.addHeader(name, v);
				}
			}
		}
		Buffer buffer = new Buffer(4096);
		byte[] tmp = new byte[65536];
		while (true)
		{
			try
			{
				int n = connection.getInputStream().read(tmp);
				if (n > 0)
				{
					buffer.write(tmp, 0, n);
				}
				else
				{
					break;
				}
			}
			catch (Exception e)
			{
				break;
			}
		}
		responseEvent.content = buffer;
		if (responseEvent.content.readable())
		{
			responseEvent.setHeader("Content-Length",
			        "" + buffer.readableBytes());
		}
		else
		{
			responseEvent.setHeader("Content-Length", "0");
		}
		return responseEvent;
	}
	
	public Event fetch(HTTPRequestEvent req)
	{
		ServerConfiguration cfg = ServerConfiguration.getServerConfig();
		HTTPResponseEvent response = new HTTPResponseEvent();
		try
		{
			response = fetchResponse(req);
		}
		catch (Exception e)
		{
			logger.error("Failed to fetch URL:" + req.url, e);
			if (e.getClass().getName().contains("ResponseTooLargeException"))
			{
				String rangeHeader = req.getHeader("Range");
				int rangeStart = 0;
				if (null != rangeHeader)
				{
					RangeHeaderValue v = new RangeHeaderValue(rangeHeader);
					rangeStart = (int) v.getFirstBytePos();
				}
				req.addHeader("Range", new RangeHeaderValue(rangeStart,
				        rangeStart + cfg.getRangeFetchLimit() - 1).toString());
				try
				{
					response = fetchResponse(req);
				}
				catch (Exception e1)
				{
					response.statusCode = 408;
				}
			}
			else if (e.getClass().getName().contains("OverQuotaException"))
			{
				response.statusCode = 408;
			}
			else
			{
				response.statusCode = 503;
			}
		}
		return response;
	}
}
