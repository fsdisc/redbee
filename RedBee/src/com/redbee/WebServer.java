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

package com.redbee;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

import com.redbee.web.Router;

public class WebServer {

    private static Logger logger = Logger.getLogger(WebServer.class);
	
    private Controller controller;
    private Server server;
    
    public WebServer(Controller controller) {
    	this.controller = controller;
    }
    
    private void grabPort() {
    	int port = controller.getPort();
    	boolean found = false;
    	int no = 9999;
    	while (!found) {
    		try {
        		Server svr = new Server(port);
    			svr.start();
    			svr.stop();
    			found = true;
    		} catch (Exception e) {
    		}
    		if (!found) {
    			port = no;
    			no--;
    		}
    	}
    	controller.getBuffer().setValue(Config.PORT, port);
    }
    
    public void start() throws Exception {
    	grabPort();
    	int port = controller.getBuffer().getInt(Config.PORT);
    	server = new Server(port);
        Context root = new Context(server, "/", Context.SESSIONS);
        root.addServlet(Servlet404.class, "/*");
        root.addFilter(Router.class, "*", Handler.REQUEST);
        server.start();
        try {
    		InetAddress addr = InetAddress.getLocalHost();
    		String host = addr.getHostAddress();
    		Router.LOCAL_HOST = host;
    		String url = "";
    		if (port == 80) {
    			url = "http://" + host + "";
    		} else {
    			url = "http://" + host + ":" + port + "";
    		}
    		Window.HOME_URL = url;
        } catch (Exception e) {
        	logger.error("", e);
        }
    }
    
    public void stop() throws Exception {
    	server.stop();
    }
    
    public void join() throws Exception {
    	server.join();
    }
    
    public static class Servlet404 extends HttpServlet {
    	@Override
    	public void service(HttpServletRequest req, HttpServletResponse res ) throws IOException {
    		res.sendError(404, "Can not find: " + req.getRequestURI());
    	}
    }
    
}
