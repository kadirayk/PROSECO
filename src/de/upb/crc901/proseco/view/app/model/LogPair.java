package de.upb.crc901.proseco.view.app.model;

/**
 * 
 * @author kadirayk
 *
 */
public class LogPair {

	private String prototypeName;
	private String strategyName;
	private String systemOutLog;
	private String systemErrorLog;

	public LogPair(String prototypeName, String strategyName, String systemOutLog, String systemErrorLog) {
		this.prototypeName = prototypeName;
		this.strategyName = strategyName;
		this.systemOutLog = systemOutLog;
		this.systemErrorLog = systemErrorLog;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getSystemOutLog() {
		return systemOutLog;
	}

	public void setSystemOutLog(String systemLog) {
		this.systemOutLog = systemLog;
	}

	public String getSystemErrorLog() {
		return systemErrorLog;
	}

	public void setSystemErrorLog(String errorLog) {
		this.systemErrorLog = errorLog;
	}

	public String getPrototypeName() {
		return prototypeName;
	}

	public void setPrototypeName(String prototypeName) {
		this.prototypeName = prototypeName;
	}

}
