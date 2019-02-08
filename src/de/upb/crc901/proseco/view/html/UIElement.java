package de.upb.crc901.proseco.view.html;
 
import java.io.Serializable;
import java.util.Map;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
 
/**
 *  
 * @author kadirayk
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = Input.class, name = "Input"),
        @JsonSubTypes.Type(value = Select.class, name = "Select"),
        @JsonSubTypes.Type(value = Option.class, name = "Option"),
        @JsonSubTypes.Type(value = Script.class, name = "Script")})
public abstract class UIElement implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5890195807308722546L;
    private String tag;
    private String content;
    private Map<String, String> attributes;
 
    public String getTag() {
        return tag;
    }
 
    protected void setTag(String tag) {
        this.tag = tag;
    }
 
    public String getContent() {
        return content;
    }
 
    public void setContent(String content) {
        this.content = content;
    }
 
    public Map<String, String> getAttributes() {
        return attributes;
    }
 
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
 
    public UIElement() {
    }
 
    public String toHTML() {
        StringBuilder html = new StringBuilder("<");
        html.append(tag);
        if (attributes != null) {
            boolean isFile = false;
            if ("file".equals(attributes.get("type"))) {
                isFile = true;
            }
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                if (!isFile && entry.getKey().equals("name")) {
                    entry.setValue("response");
                } else if (isFile && entry.getKey().equals("name")) {
                    entry.setValue("file");
                }
                html.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
            }
        }
        html.append(">");
        if (content != null) {
            html.append(content);
        }
        html.append("</").append(tag).append(">");
        return html.toString();
    }
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UIElement other = (UIElement) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }
 
}