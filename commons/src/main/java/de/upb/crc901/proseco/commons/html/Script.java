package de.upb.crc901.proseco.commons.html;

/**
 * HTML inline script element &lt;script\&gt;
 *
 * @author Kadiray
 *
 */
public class Script extends UIElement {

	/**
	 *
	 */
	private static final long serialVersionUID = -5964138383187552433L;
	private static final String TAG = "script";

	public Script() {
		this.setTag(TAG);
	}

	public Script(String content) {
		this.setTag(TAG);
		this.setContent(content);
	}

	@Override
	public String toHTML() {
		StringBuilder html = new StringBuilder("<");
		html.append(this.getTag());
		html.append(">\n");
		html.append(this.getContent());
		html.append("\n</").append(this.getTag()).append(">");
		return html.toString();
	}
}