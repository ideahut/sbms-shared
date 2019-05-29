package com.github.ideahut.sbms.shared.tag;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

@SuppressWarnings("serial")
public class StaticTag extends TagSupport {

	private String item;
	
	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	@Override
	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		JspWriter writer = pageContext.getOut();
		if (item != null) {
			try {
				writer.print(request.getContextPath() + "/" + item);
			} catch (IOException e) {
				throw new JspException(e);
			}
			return EVAL_BODY_INCLUDE;
		}
		return super.doStartTag();
	}

	@Override
	public int doEndTag() throws JspException {
		
		return super.doEndTag();
	}	
	
}
