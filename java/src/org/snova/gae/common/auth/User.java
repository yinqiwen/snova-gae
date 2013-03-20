/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Authorization.java 
 *
 * @author yinqiwen [ 2010-4-6 | 09:18:10 PM ]
 *
 */
package org.snova.gae.common.auth;

/**
 *
 */
public class User
{
	public String getPasswd()
	{
		return passwd;
	}
	
	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
	
	private String	user;
	@Override
    public String toString()
    {
	    return "User [user=" + user + ", passwd=" + passwd + "]";
    }
	private String	passwd;

	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((passwd == null) ? 0 : passwd.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (passwd == null)
		{
			if (other.passwd != null)
				return false;
		}
		else if (!passwd.equals(other.passwd))
			return false;
		if (user == null)
		{
			if (other.user != null)
				return false;
		}
		else if (!user.equals(other.user))
			return false;
		return true;
	}
	
	public String getUser()
	{
		return user;
	}
	
	public User(String passwd, String user)
    {
	    super();
	    this.passwd = passwd;
	    this.user = user;
    }

	public void setUser(String user)
	{
		this.user = user;
	}
}
