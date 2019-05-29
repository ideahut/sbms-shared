package com.github.ideahut.sbms.shared.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

@SuppressWarnings("serial")
public class BlockTag extends BodyTagSupport {
	
	private static final String ATTRIBUTE = BlockTag.class.getName();
	
	private static final String PREFIX = ATTRIBUTE + "_";

	private String name;
	
	private String target;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return "BlockTag [name=" + name + ", target=" + target + "]";
	}
	
	@SuppressWarnings("resource")
	@Override
	public int doEndTag() throws JspException {
		BodyContent content = getBodyContent();
		if (name != null) {
			Map<String, List<String>> map = this.getMap();
			if (!map.containsKey(PREFIX + name)) {
				map.put(PREFIX + name, new ArrayList<String>());
			}		
			try {
				JspWriter out = content != null ? content.getEnclosingWriter() : pageContext.getOut();
				out.print("<" + PREFIX + name + "/>");
			} catch (IOException e) {
				throw new JspException(e);
			}
		}		
		else if (target != null) {
			Map<String, List<String>> map = this.getMap();
			List<String> list = map.get(PREFIX + target);
			if (list == null) {
				list = new ArrayList<String>();
				map.put(PREFIX + target, list);
			}
			list.add(content.getString());	
		}
		else {
			Map<String, List<String>> map = this.getMap();
			if (!map.isEmpty() && content != null) {
				String text = content.getString();
				for (String key : map.keySet()) {
					List<String> list = map.get(key);
					if (list == null) {
						continue;
					}
					StringBuilder sb = new StringBuilder();
					for (String str : list) {
						sb.append("\n").append(str);
					}
					text = text.replace("<" + key + "/>", sb.toString());
				}
				try {
					JspWriter out = content.getEnclosingWriter();
					out.print(text);
				} catch (IOException e) {
					throw new JspException(e);
				}
			}
		}
		return super.doEndTag();
	}	
	
	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getMap() {
		Map<String, List<String>> map = (Map<String, List<String>>)pageContext.getAttribute(ATTRIBUTE);
		if (map == null) {
			map = new HashMap<String, List<String>>();
			pageContext.setAttribute(ATTRIBUTE, map);
		}
		return map;
	}
	
}
