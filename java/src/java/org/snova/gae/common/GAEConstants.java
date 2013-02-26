/**
 * 
 */
package org.snova.gae.common;

/**
 * @author qiyingwang
 *
 */
public interface GAEConstants
{
	public static final String CLIENT_CONF_NAME = "gae-client.conf";
	public static final String SERVER_CONF_NAME = "gae-server.xml";
	public static final String HTTP_INVOKE_PATH = "/invoke";
	public static final String BACKEND_INSTANCE_NAME = "worker";
	public static final String XMPP_CLIENT_NAME = "snova-gae";
	public static final String RESERVED_GOOGLECN_IP_MAPPING = "GoogleCNIP";
	public static final String RESERVED_GOOGLEHTTPS_HOST_MAPPING = "GoogleHttps";
	public static final String PREFERED_GOOGLE_PROXY = "PreferedGoogleProxy";
	
	public static final int APPENGINE_HTTP_BODY_LIMIT = 20000000; //2MB
	public static final int APPENGINE_XMPP_BODY_LIMIT = 4096; //4KB
	
	public static final int FETCH_FAILED = -2;
	public static final int RESPONSE_TOO_LARGE = -3;
	public static final int RESPONSE_TIMEOUT = -4;
	
	public static final int AUTH_REQUEST_EVENT_TYPE = 2000;
	public static final int AUTH_RESPONSE_EVENT_TYPE = 2001;
	public static final int USER_OPERATION_EVENT_TYPE = 2010;
	public static final int GROUP_OPERATION_EVENT_TYPE = 2011;
	public static final int USER_LIST_REQUEST_EVENT_TYPE = 2012;
	public static final int GROUOP_LIST_REQUEST_EVENT_TYPE = 2013;
	public static final int USER_LIST_RESPONSE_EVENT_TYPE = 2014;
	public static final int GROUOP_LIST_RESPONSE_EVENT_TYPE = 2015;
	public static final int BLACKLIST_OPERATION_EVENT_TYPE = 2016;
	public static final int REQUEST_SHARED_APPID_EVENT_TYPE = 2017;
	public static final int REQUEST_SHARED_APPID_RESULT_EVENT_TYPE = 2018;
	public static final int SHARE_APPID_EVENT_TYPE = 2019;
	public static final int ADMIN_RESPONSE_EVENT_TYPE = 2020;
	public static final int REQUEST_ALL_SHARED_APPID_EVENT_TYPE = 2021;
	public static final int SERVER_CONFIG_EVENT_TYPE = 2050;
	
	public static final String SNOVA_MASTER_APPID = "snova-master";
	
	public static final String USER_ENTITY_NAME = "ProxyUser";
	public static final String GROUP_ENTITY_NAME = "ProxyGroup";
	public static final String SHARED_APPID_ENTITY_NAME = "SharedAppID";
	public static final String ROOT_NAME = "root";
	public static final String ROOT_GROUP_NAME = "root";

	public static final String PUBLIC_GROUP_NAME = "public";

	public static final String ANONYMOUSE_NAME = "anonymouse";
	public static final String ANONYMOUSE_GROUP_NAME = "anonymouse";

	public static final String AUTH_FAILED = "You have no authorization for this operation!";
	public static final String USER_NOTFOUND = "User not found!";
	public static final String USER_EXIST = "User already exist!";
	public static final String GRP_NOTFOUND = "Group not found!";
	public static final String GRP_EXIST = "Group already exist!";
	public static final String PASS_NOT_MATCH = "The old password is not match!";
}
