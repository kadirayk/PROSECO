package de.upb.crc901.proseco.view.app.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.upb.crc901.proseco.commons.util.ToJSONStringUtil;

/**
 *
 * @author kadirayk
 *
 */
public class LogResponseBody {

	String msg;

	List<LogPair> logList;

	public String getMsg() {
		return this.msg;
	}

	public void setMsg(final String msg) {
		this.msg = msg;
	}

	public List<LogPair> getLogList() {
		return this.logList;
	}

	public void setLogList(final List<LogPair> logList) {
		this.logList = logList;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("msg", this.msg);
		fields.put("logList", this.logList);
		return ToJSONStringUtil.toJSONString("log", fields);
	}
}