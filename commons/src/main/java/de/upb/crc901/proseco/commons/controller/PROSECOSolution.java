package de.upb.crc901.proseco.commons.controller;

import java.io.File;
import java.util.TreeMap;

public class PROSECOSolution {
	String processId;
	String winningStrategyId;
	Double winningScore;
	TreeMap<Double, String> backupStrategies; // keep an ordered list of backup Strategies by their score
	File winningStrategyFolder;
	TreeMap<Double, File> backupStrategyFolders;

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

	public TreeMap<Double, File> getBackupStrategyFolders() {
		return backupStrategyFolders;
	}

	public void setBackupStrategyFolders(TreeMap<Double, File> backupStrategyFolders) {
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

	public TreeMap<Double, String> getBackupStrategies() {
		return backupStrategies;
	}

	public void setBackupStrategies(TreeMap<Double, String> backupStrategies) {
		this.backupStrategies = backupStrategies;
	}

}
