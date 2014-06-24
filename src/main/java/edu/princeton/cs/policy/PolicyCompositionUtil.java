package edu.princeton.cs.policy;

import org.openflow.protocol.OFFlowMod;

public class PolicyCompositionUtil {

	public static OFFlowMod parallelComposition(OFFlowMod fm1, OFFlowMod fm2) {
		
		OFFlowMod composedFm = new OFFlowMod();
		/*composedFm.setMatch(match);
		composedFm.setCookie(0);
		composedFm.setCommand(OFFlowMod.OFPFC_ADD);
		composedFm.setIdleTimeout(0);
		composedFm.setHardTimeout(0);
		composedFm.setPriority(fm1.getPriority() + fm2.getPriority());*/
		
		
		return composedFm;
	}
	
	
}
