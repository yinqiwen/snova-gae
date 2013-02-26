/**
 * 
 */
package org.snova.gae.common.config;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.buffer.CodecObject;
import org.arch.event.misc.CompressorType;
import org.arch.event.misc.EncryptType;

/**
 * @author qiyingwang
 * 
 */
public class GAEServerConfiguration implements CodecObject
{
	private int fetchRetryCount = 1;
	private int maxXMPPDataPackageSize = 40960;
	private int rangeFetchLimit = 256 * 1024;

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

	private CompressorType compressor = CompressorType.SNAPPY;
	private EncryptType encrypter = EncryptType.SE1;
	private boolean isMasterNode = false;

	public boolean isMasterNode() 
	{
		return isMasterNode;
	}

	public void setMasterNode(boolean proxyEnable) 
	{
		this.isMasterNode = proxyEnable;
	}

	private Set<String> compressFilter = new HashSet<String>();

	// private boolean trafficStatEnable = false;
	//
	// public boolean isTrafficStatEnable()
	// {
	// return trafficStatEnable;
	// }
	//
	// public void setTrafficStatEnable(boolean trafficStatEnable)
	// {
	// this.trafficStatEnable = trafficStatEnable;
	// }

	public GAEServerConfiguration()
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

	public int getMaxXMPPDataPackageSize()
	{
		return maxXMPPDataPackageSize;
	}

	public void setMaxXMPPDataPackageSize(int maxXMPPDataPackageSize)
	{
		this.maxXMPPDataPackageSize = maxXMPPDataPackageSize;
	}

	public int getFetchRetryCount()
	{
		return fetchRetryCount;
	}

	public void setFetchRetryCount(int fetchRetryCount)
	{
		this.fetchRetryCount = fetchRetryCount;
	}

	@Override
	public boolean encode(Buffer buffer)
	{
		BufferHelper.writeVarInt(buffer, fetchRetryCount);
		BufferHelper.writeVarInt(buffer, maxXMPPDataPackageSize);
		BufferHelper.writeVarInt(buffer, rangeFetchLimit);
		BufferHelper.writeVarInt(buffer, compressor.getValue());
		BufferHelper.writeVarInt(buffer, encrypter.getValue());
		BufferHelper.writeBoolean(buffer, isMasterNode);
		BufferHelper.writeSet(buffer, compressFilter);

		return true;
	}

	@Override
	public boolean decode(Buffer buffer)
	{
		try
		{
			fetchRetryCount = BufferHelper.readVarInt(buffer);
			maxXMPPDataPackageSize = BufferHelper.readVarInt(buffer);
			rangeFetchLimit = BufferHelper.readVarInt(buffer);
			compressor = CompressorType
			        .fromInt(BufferHelper.readVarInt(buffer));
			encrypter = EncryptType.fromInt(BufferHelper.readVarInt(buffer));
			isMasterNode = BufferHelper.readBool(buffer);
			compressFilter = BufferHelper.readSet(buffer, String.class);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public void print(PrintStream ps)
	{
		String colu1 = "FetchRetryCount";
		String colu2 = "MaxXMPPDataPackageSize";
		String colu3 = "RangeFetchLimit";
		String colu4 = "Compressor";
		String colu5 = "Encrypter";
		String colu6 = "IsMaster";
		String colu7 = "CompressFilter";
		final String formater = "%" + colu1.length() + "s %" + colu2.length()
		        + "s %" + colu3.length() + "s %" + colu4.length() + "s %"
		        + colu5.length() + "s %" + colu6.length() + "s"+ colu7.length() + "s";
		String header = String.format(formater, "FetchRetryCount",
		        "MaxXMPPDataPackageSize", "RangeFetchLimit", "Compressor",
		        "Encrypter","IsMaster", "CompressFilter");
		ps.println(header);
		String output = String.format(formater, "" + fetchRetryCount, ""
		        + maxXMPPDataPackageSize, "" + rangeFetchLimit,
		        compressor.toString(), encrypter.toString(), Boolean.toString(isMasterNode),
		        getCompressFilter().toString());
		ps.println(output);
	}

}
