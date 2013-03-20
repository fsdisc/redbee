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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class TaskManager extends Thread {

    private static Logger logger = Logger.getLogger(TaskManager.class);
	
    private Controller controller;
    private boolean stopped = false;
	
    public TaskManager(Controller controller) {
    	this.controller = controller;
    }

    public void setStopped(boolean src) {
    	stopped = src;
    }
    
    public boolean getStopped() {
    	return stopped;
    }
    
    public void run() {
    	try {
    		try {
        		new CleanThread().start();
    		} catch (Exception e) {
    			logger.error("", e);
    		}
    		while (!getStopped()) {
    			try {
    			} catch (Exception e) {
    	    		logger.error("", e);
    			}
    			try {
    				Thread.sleep(60000);
    			} catch (Exception e) {
    	    		logger.error("", e);
    			}
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }

    private class CleanThread extends Thread {
    
    	public void run() {
    		while (!getStopped()) {
    			try {
    				String root = controller.getTempRoot();
    				File froot = new File(root);
    				File[] children = froot.listFiles();
    				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    				Calendar cal = Calendar.getInstance();
    				cal.add(Calendar.DATE, -1);
    				String today = sdf.format(cal.getTime());
    				for (int i = 0; i < children.length; i++) {
    					File child = children[i];
    					if (!child.isDirectory()) continue;
    					if (today.compareTo(child.getName()) > 0) {
    						try {
    							IOTool.deleteFolder(child);
    						} catch (Exception e) {
    							logger.error("", e);
    						}
    					}
    				}
    			} catch (Exception e) {
    				logger.error("", e);
    			}
    			try {
    				Thread.sleep(1000 * 60 * 60 * 24);
    			} catch (Exception e) {
    				logger.error("", e);
    			}
    		}
    	}
    	
    }
    
}
