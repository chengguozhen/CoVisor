package edu.princeton.cs.policy.ofwrapper;

import java.util.Arrays;

import org.openflow.protocol.OFMatch;
import org.openflow.util.HexString;

public class OFMatchWrapper {
	
	public final class ByteArrayWrapper
	{
	    public final byte[] data;

	    public ByteArrayWrapper(byte[] data)
	    {
	        if (data == null)
	        {
	            throw new NullPointerException();
	        }
	        this.data = data;
	    }

	    @Override
	    public boolean equals(Object other)
	    {
	        if (!(other instanceof ByteArrayWrapper))
	        {
	            return false;
	        }
	        return Arrays.equals(data, ((ByteArrayWrapper)other).data);
	    }

	    @Override
	    public int hashCode()
	    {
	        return Arrays.hashCode(data);
	    }
	}
	
	public OFMatch match;
	public ByteArrayWrapper dataLayerSourceWrapper;
    public ByteArrayWrapper dataLayerDestinationWrapper;
    
    public OFMatchWrapper(OFMatch match) {
    	this.match = match;
    	this.dataLayerSourceWrapper = new ByteArrayWrapper(match.getDataLayerSource());
    	this.dataLayerDestinationWrapper = new ByteArrayWrapper(match.getDataLayerDestination());
    }
    
}
