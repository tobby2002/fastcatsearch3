package org.fastcatsearch.http.action;

import java.io.Writer;

import org.fastcatsearch.util.JSONPResultWriter;
import org.fastcatsearch.util.JSONResultWriter;
import org.fastcatsearch.util.ResultWriter;
import org.fastcatsearch.util.XMLResultWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceAction extends HttpAction {
	public static final String DEFAULT_ROOT_ELEMENT = "response";
	public static final String DEFAULT_CHARSET = "utf-8";
	public static enum Type { json, xml, jsonp, html };
	
	public ServiceAction(){ 
	}
	
	protected void writeHeader(ActionResponse response) {
		writeHeader(response, DEFAULT_CHARSET);
	}
	protected void writeHeader(ActionResponse response, String responseCharset) {
		response.setStatus(HttpResponseStatus.OK);
		logger.debug("resultType > {}",resultType);
		if (resultType == Type.json) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.jsonp) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.xml) {
			response.setContentType("text/xml; charset=" + responseCharset);
		} else if (resultType == Type.html) {
			response.setContentType("text/html; charset=" + responseCharset);
		} else {
			response.setContentType("application/json; charset=" + responseCharset);
		}
	}
	protected ResultWriter getDefaultResultWriter(Writer writer){
		return getResultWriter(writer, DEFAULT_ROOT_ELEMENT, true, null);
	}
	
	protected ResultWriter getResultWriter(Writer writer, String rootElement, boolean isBeautify, String jsonCallback) {
		ResultWriter resultWriter = null;
		if (resultType == Type.json) {
			resultWriter = new JSONResultWriter(writer, isBeautify);
		} else if (resultType == Type.jsonp) {
			resultWriter = new JSONPResultWriter(writer, jsonCallback, isBeautify);
		} else if (resultType == Type.xml) {
			resultWriter = new XMLResultWriter(writer, rootElement, isBeautify);
		}
		return resultWriter;
	}

}
