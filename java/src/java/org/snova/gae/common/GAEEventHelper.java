/**
 * 
 */
package org.snova.gae.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventDispatcher;
import org.arch.event.EventSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class GAEEventHelper
{
	protected static Logger logger = LoggerFactory
	        .getLogger(GAEEventHelper.class);

	public static Event parseEvent(Buffer buffer) throws Exception
	{
		EventHeaderTags tags = new EventHeaderTags();
		return parseEvent(buffer, tags);
	}

	public static Event parseEvent(Buffer buffer, EventHeaderTags tags)
	        throws Exception
	{
		// EventHeaderTags tags = new EventHeaderTags();
		if (!EventHeaderTags.readHeaderTags(buffer, tags))
		{
			logger.error("Failed to read event header tags.");
			return null;
		}
		return EventDispatcher.getSingletonInstance().parse(buffer);
	}

	public static Buffer[] splitEventBuffer(Buffer msgbuffer, int hash,
	        int splitsize, EventHeaderTags tags)
	{
		if (msgbuffer.readableBytes() > splitsize)
		{
			int total = msgbuffer.readableBytes() / splitsize;
			if (msgbuffer.readableBytes() % splitsize != 0)
			{
				total++;
			}
			Buffer[] bufs = new Buffer[total];
			int i = 0;
			while (msgbuffer.readable())
			{
				EventSegment segment = new EventSegment();
				segment.setHash(hash);
				segment.sequence = i;
				segment.total = total;
				segment.content = new Buffer(splitsize);
				segment.content.write(msgbuffer, splitsize);
				Buffer buf = GAEEventHelper.encodeEvent(tags, segment);
				segment.encode(buf);
				bufs[i] = buf;
				i++;
			}
			return bufs;
		}
		else
		{
			return new Buffer[] { msgbuffer };
		}
	}

	public static Buffer encodeEvent(EventHeaderTags tags, Event event)
	{
		Buffer buf = new Buffer(256);
		tags.encode(buf);
		Buffer content = new Buffer(256);
		event.encode(content);
		buf.write(content, content.readableBytes());
		return buf;
	}

	private static Map<Integer, ArrayList<EventSegment>> sessionBufferTable = new HashMap<Integer, ArrayList<EventSegment>>();

	public static void releaseSessionBuffer(Integer sessionID)
	{
		sessionBufferTable.remove(sessionID);
	}
}
