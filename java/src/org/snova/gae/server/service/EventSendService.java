/**
 * 
 */
package org.snova.gae.server.service;

import org.arch.buffer.Buffer;

/**
 * @author qiyingwang
 *
 */
public interface EventSendService
{
	public int getMaxDataPackageSize();
	public void send(Buffer buf);
}
