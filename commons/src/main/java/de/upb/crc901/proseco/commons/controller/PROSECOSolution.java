package de.upb.crc901.proseco.commons.controller;

import java.io.File;
import java.util.SortedMap;

public class PROSECOSolution {
	private String processId;
	private String winningStrategyId;
	private Double winningScore;
	private SortedMap<Double, String> backupStrategies; // keep an ordered list of backup Strategies by their score
	private File winningStrategyFolder;
	private SortedMap<Double, File> backupStrategyFolders;

	public Double getWinningScore() {
		return winningScore;
	}

	public void setWinningScore(Double winningScore) {
		this.winningScore = winningScore;
	}

	public File getWinningStrategyFolder() {
		return winningStrategyFolder;
	}

	public void setWinningStrategyFolder(File winningStrategyFolder) {
		this.winningStrategyFolder = winningStrategyFolder;
	}

	public SortedMap<Double, File> getBackupStrategyFolders() {
		return backupStrategyFolders;
	}

	public void setBackupStrategyFolders(SortedMap<Double, File> backupStrategyFolders) {
		this.backupStrategyFolders = backupStrategyFolders;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getWinningStrategyId() {
		return winningStrategyId;
	}

	public void setWinningStrategyId(String winningStrategyId) {
		this.winningStrategyId = winningStrategyId;
	}

	public SortedMap<Double, String> getBackupStrategies() {
		return backupStrategies;
	}

	public void setBackupStrategies(SortedMap<Double, String> backupStrategies) {
		this.backupStrategies = backupStrategies;
	}

}
