package de.upb.crc901.proseco.commons.controller;

import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;

public interface ProcessController {
	
	/**
	 * 
	 * This method must also create the process.json file within the process folder!
	 * 
	 * @param domain
	 * @return the construction process environment. No assumptions about the layout of this identifier must be made
	 */
	public PROSECOProcessEnvironment createConstructionProcessEnvironment(String domain) throws Exception;
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public PROSECOProcessEnvironment getConstructionProcessEnvironment(String processId) throws Exception; 
}
