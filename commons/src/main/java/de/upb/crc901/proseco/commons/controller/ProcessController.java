package de.upb.crc901.proseco.commons.controller;

import java.util.Map;

import de.upb.crc901.proseco.commons.processstatus.EProcessState;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;

public interface ProcessController {

	/**
	 * Create a new prototype with the given ID if available, Create a new prototype
	 * if the given ID is null
	 * 
	 * @param processId
	 * @throws InvalidStateTransitionException 
	 */
	//TODO createNew with empty params
	public void createNew(String processId) throws ProcessIdAlreadyExistsException, InvalidStateTransitionException;

	/**
	 * Attach to an existing process
	 * 
	 * @param processId
	 * @throws InvalidStateTransitionException 
	 */
	public void attach(String processId) throws ProcessIdDoesNotExistException, InvalidStateTransitionException;

	/**
	 * 
	 * @param domain
	 * @throws InvalidStateTransitionException 
	 */
	public void fixDomain(String domain) throws CannotFixDomainInThisProcessException, InvalidStateTransitionException;

	/**
	 * 
	 */
	public PROSECOSolution startComposition(int timeoutInSeconds) throws NoStrategyFoundASolutionException, InvalidStateTransitionException, PrototypeCouldNotBeExtractedException;

	/**
	 * 
	 * @return
	 */
	public String getProcessId();

	/**
	 * @throws GroundingNotSuccessfulForAnyStrategyException 
	 * 
	 */
	public void chooseAndDeploySolution(PROSECOSolution solution) throws InvalidStateTransitionException, GroundingNotSuccessfulForAnyStrategyException;

	public void updateInterview(Map<String, String> answers) throws InvalidStateTransitionException;
	
	public PROSECOProcessEnvironment getProcessEnvironment() throws InvalidStateTransitionException;

	public EProcessState getProcessState();


}
