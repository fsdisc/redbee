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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.redbee.Window;

public class Router implements Filter {

    private static Logger logger = Logger.getLogger(Router.class);
	
    public static final String MAGIC = "50aed7ad58e47";
    public static final String MAGIC2 = "50aed7b733a99";
    
    public static String LOCAL_HOST = "";
    
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest)request;
			HttpServletResponse resp = (HttpServletResponse)response;
			try {
				if (req.getServerName().equals(LOCAL_HOST)) {
					String path = req.getRequestURI();
					if (req.getRemoteAddr().equals(LOCAL_HOST)) {
						if (("/" + MAGIC).equals(path)) {
							req.getSession(true).setAttribute(MAGIC, MAGIC);
							resp.sendRedirect(Window.HOME_URL);
							return;
						} else if (("/" + MAGIC).equals(path)) {
							req.getSession(true).setAttribute(MAGIC, null);
							resp.sendRedirect(Window.HOME_URL);
							return;
						} else if (MAGIC.equals(req.getSession(true).getAttribute(MAGIC))) {
							Page page = null;
							if ("/".equals(path) || "/home.jsp".equals(path)) {
								page = new HomePage(req, resp);
							}
							if ("/search.jsp".equals(path)) {
								page = new SearchPage(req, resp);
							}
							if ("/bookmark.jsp".equals(path)) {
								page = new BookmarkPage(req, resp);
							}
							if ("/file.jsp".equals(path)) {
								page = new FilePage(req, resp);
							}
							if ("/bookmark-list.jsp".equals(path)) {
								page = new BookmarkListPage(req, resp);
							}
							if (page != null) {
								page.execute();
								return;
							}
							if (path.length() > 0 && !path.endsWith(".vm") && !path.endsWith(".lang") && !path.endsWith("/")) {
								try {
									InputStream is = Router.class.getResourceAsStream("/com/redbee/resource" + path);
									byte[] buf = new byte[1024];
									int read = is.read(buf, 0, 1024);
									while (read > 0) {
										resp.getOutputStream().write(buf, 0, read);
										read = is.read(buf, 0, 1024);
									}
									is.close();
									resp.getOutputStream().close();
									return;
								} catch (Exception e) {
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				sendError(resp, e);
			}
		}		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	protected void sendError(HttpServletResponse res, Throwable ex) {
		try {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			String trace = "\n\n" + sw.toString();
			res.sendError(500, ex.getMessage() + trace);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
}
