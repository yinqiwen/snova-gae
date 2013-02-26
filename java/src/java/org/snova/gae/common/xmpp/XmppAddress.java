/**
 * 
 */
package org.snova.gae.common.xmpp;


/**
 * @author yinqiwen
 *
 */
public class XmppAddress{

	private String jid;
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public XmppAddress()
	{
		
	}
	
	public XmppAddress(String jid)
	{
		this.jid = jid;
	}

	public String toPrintableString()
	{
		return "[" + jid + "]";
	}
	
	public String toString()
	{
		return toPrintableString();
	}
	
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof XmppAddress) {
			XmppAddress anotherServerSessionID = (XmppAddress) anObject;

			return jid.equals(anotherServerSessionID.jid);
		}
		return false;
	}

	public int hashCode() {
		return jid.hashCode();
	}
}
