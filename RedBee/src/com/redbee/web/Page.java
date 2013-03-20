/*
 *  Red Bee Browser
 *
 *  Copyright (c) 2013 Tran Dinh Thoai <dthoai@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.redbee.web;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public abstract class Page {

	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	public Page(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public abstract void execute() throws Exception;

	protected boolean isPost() {
		return "post".equalsIgnoreCase(request.getMethod());
	}
	
	protected String getParameter(String name) {
		String tag = request.getParameter(name);
		if (tag == null) tag = "";
		return tag;
	}

	protected Map newArgs() {
		return newArgs("en");
	}
	
	protected Map newArgs(String lang) {
		Map tag = new HashMap();
		tag.put("lang", loadLanguage(lang + ".lang"));
		tag.put("tool", new PageTool(tag));
		return tag;
	}

	protected Map loadLanguage(String path) {
		return loadLanguage(path, "/languages/");
	}
	
	protected Map loadLanguage(String path, String langRoot) {
		Map tag = new HashMap();
		try {
			String input = new String(loadResource(langRoot + path), "UTF-8");
			String[] lines = input.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.trim().startsWith("#")) continue;
				int pos = line.indexOf("=");
				if (pos < 0) continue;
				String key = line.substring(0, pos).trim();
				String value = line.substring(pos + 1).trim();
				tag.put(key, value);
			}
		} catch (Exception e) {
		}
		return tag;
	}
	
	protected Object getSession(String name) {
		return request.getSession(true).getAttribute(name);
	}
	
	protected void setSession(String name, Object value) {
		request.getSession(true).setAttribute(name, value);
	}
	
	protected void loadPage(String path, Map args) throws Exception {
		String output = merge(loadTemplate(path), args);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.getOutputStream().write(output.getBytes("UTF-8"));
	}

	protected void loadPage(String path, String tplRoot, Map args) throws Exception {
		String output = merge(loadTemplate(path, tplRoot), args);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.getOutputStream().write(output.getBytes("UTF-8"));
	}
	
    protected String merge(String template, Map args) throws Exception {
    	VelocityEngine engine = new VelocityEngine();
    	engine.init();
    	VelocityContext ctx = new VelocityContext();
    	for (Object key : args.keySet()) {
    		ctx.put(key + "", args.get(key));
    	}
    	Writer writer = new StringWriter();
    	engine.evaluate(ctx, writer, "", template);
    	return writer.toString();
    }
    
    protected String merge(byte[] template, Map args) throws Exception {
    	return merge(new String(template, "UTF-8"), args);
    }

	protected byte[] loadTemplate(String path) throws Exception {
		return loadTemplate(path, "/templates/");
	}
	
	protected byte[] loadTemplate(String path, String tplRoot) throws Exception {
		String filename = tplRoot + path;
		return loadResource(filename);
	}
    
	protected byte[] loadResource(String path) throws Exception {
		InputStream is =  Router.class.getResourceAsStream("/com/redbee/resource" + path);
		List<Byte> data = new ArrayList<Byte>();
		byte[] buffer = new byte[1024];
		int read = is.read(buffer, 0, 1024);
		while (read > 0) {
			for (int i = 0; i < read; i++) {
				data.add(buffer[i]);
			}
			read = is.read(buffer, 0, 1024);
		}
		is.close();
		byte[] tag = new byte[data.size()];
		for (int i = 0; i < data.size(); i++) {
			tag[i] = data.get(i);
		}
		return tag;
	}
	
	protected String getTrace(Throwable ex) {
		try {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			String trace = "\n\n" + sw.toString();
			return ex.getMessage() + trace;
		} catch (Exception e) {
			return "";
		}
	}
	
	public class PageTool {
	
		private Map args;
		
		public PageTool(Map args) {
			this.args = args;
		}
		
		public String include(String path, String tplRoot) {
			String tag = "";
			try {
				tag = merge(loadTemplate(path, tplRoot), args);
			} catch (Exception e) {
				tag = getTrace(e);
			}
			return tag;
		}

		public String include(String path) {
			return include(path, "/templates/");
		}
		
	}
	
}
