package de.upb.crc901.proseco.view.app.controller;

import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;

public interface ProcessController {
	
	/**
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
