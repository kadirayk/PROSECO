package de.upb.crc901.proseco.view.app.model;

import java.util.List;

/**
 * 
 * @author kadirayk
 *
 */
public class LogResponseBody {

	String msg;

	List<LogPair> logList;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<LogPair> getLogList() {
		return logList;
	}

	public void setLogList(List<LogPair> logList) {
		this.logList = logList;
	}

}