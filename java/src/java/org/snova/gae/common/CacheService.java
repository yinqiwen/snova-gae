/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CacheService.java 
 *
 * @author yinqiwen [ 2011-12-3 | обнГ04:26:31 ]
 *
 */
package org.snova.gae.common;

/**
 *
 */
public interface CacheService
{
	public Object get(Object key);
	public Object put(Object key, Object value);
}
