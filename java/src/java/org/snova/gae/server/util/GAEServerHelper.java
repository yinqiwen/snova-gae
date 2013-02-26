/**
 * 
 */
package org.snova.gae.server.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.arch.common.KeyValuePair;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.http.HTTPResponseEvent;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;

/**
 * @author qiyingwang
 * 
 */
public class GAEServerHelper
{
	public static HTTPRequest toHTTPRequest(HTTPRequestEvent exchange)
	        throws MalformedURLException
	{
		URL requrl = new URL(exchange.url);
		// HTTPMethod.
		FetchOptions fetchOptions = FetchOptions.Builder.withDeadline(10)
		        .disallowTruncate().doNotFollowRedirects();
		HTTPRequest req = new HTTPRequest(requrl,
		        HTTPMethod.valueOf(exchange.method), fetchOptions);
		for (KeyValuePair<String, String> header : exchange.getHeaders())
		{
			req.addHeader(new HTTPHeader(header.getName(), header.getValue()));
		}
		req.setPayload(exchange.content.toArray());
		return req;
	}

	public static HTTPResponseEvent toHttpResponseExchange(HTTPResponse res)
	{
		HTTPResponseEvent exchange = new HTTPResponseEvent();
		exchange.statusCode = res.getResponseCode();
		List<HTTPHeader> headers = res.getHeaders();
		for (HTTPHeader header : headers)
		{
			exchange.addHeader(header.getName(), header.getValue());
		}
		if(null != res.getContent())
		{
			exchange.content.ensureWritableBytes(res.getContent().length);
			exchange.content.write(res.getContent());
			exchange.addHeader("content-length", "" + res.getContent().length);
		}
		else
		{
			exchange.addHeader("content-length", "0");
		}

		URL url = res.getFinalUrl();
		if (null != url)
		{
			//exchange.setRedirectURL(url.toString());
		}
		return exchange;
	}
}
