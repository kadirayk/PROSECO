package de.upb.crc901.proseco.view.util;

public class LogLine {

	private String strategyName;

	private Integer outLineNumber;
	private Integer errLineNumber;
	private Integer allLineNumber;

	public LogLine(String strategyName) {
		this.strategyName = strategyName;
		this.outLineNumber = 0;
		this.errLineNumber = 0;
		this.allLineNumber = 0;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public Integer getOutLineNumber() {
		return outLineNumber;
	}

	public void setOutLineNumber(Integer outLineNumber) {
		this.outLineNumber = outLineNumber;
	}

	public Integer getErrLineNumber() {
		return errLineNumber;
	}

	public void setErrLineNumber(Integer errLineNumber) {
		this.errLineNumber = errLineNumber;
	}

	public Integer getAllLineNumber() {
		return allLineNumber;
	}

	public void setAllLineNumber(Integer allLineNumber) {
		this.allLineNumber = allLineNumber;
	}

}
