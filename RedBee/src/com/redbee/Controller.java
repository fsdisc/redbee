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
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.redbee.schema.Entity;
import com.redbee.schema.LuceneHandler;

public class Controller {

    private static Logger logger = Logger.getLogger(Controller.class);

    private Config config;
    private Config buffer;
    private String appDir;
    private String cfgDir;
    private String logDir;
    private String cfgFile;
    private String datDir;
    private String extDir;

    private static Controller instance;
    
    public Controller() {
    	config = new Config();
    	buffer = new Config();
    	appDir = System.getProperty("user.dir");
    	cfgDir = new File(appDir, "cfg").getAbsolutePath();
    	logDir = new File(appDir, "log").getAbsolutePath();
    	datDir = new File(appDir, "dat").getAbsolutePath();
    	extDir = new File(appDir, "ext").getAbsolutePath();
    	cfgFile = new File(cfgDir, "config.properties").getAbsolutePath();
    	loadConfig();
    }

    public static void setInstance(Controller src) {
    	instance = src;
    }
    
    public static Controller getInstance() {
    	return instance;
    }
    
    public Config getConfig() {
    	return config;
    }
    
    public Config getBuffer() {
    	return buffer;
    }
    
    public String getAppDir() {
    	return appDir;
    }
    
    public String getCfgDir() {
    	return cfgDir;
    }
    
    public String getLogDir() {
    	return logDir;
    }

    public String getExtDir() {
    	return extDir;
    }
    
    public String getCfgFile() {
    	return cfgFile;
    }
    
    public String getDatDir() {
    	String dir = config.getString(Config.DATA);
    	if (dir.length() == 0) {
        	return datDir;
    	} else {
    		return dir;
    	}
    }
    
    public void saveConfig() {
    	config.save(cfgFile);
    }
    
    public void loadConfig() {
    	config.load(cfgFile);
    }
    
    public String uniqid() {
    	return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public String suniqid() {
        Random random = new Random();
        return Long.toString(Math.abs(random.nextLong()), 36);
    }
    
    public int getPort() {
    	int port = config.getInt(Config.PORT);
    	if (port <= 0) {
    		port = 80;
    	}
    	return port;
    }
    
    public String getTempRoot() {
    	String t_dat = getDatDir();
    	String t_root = new File(t_dat, "temp").getAbsolutePath();
    	new File(t_root).mkdirs();
    	return t_root;
    }
    
    public String getTempDir() {
    	String t_root = getTempRoot();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	String t_date = new File(t_root, sdf.format(new Date())).getAbsolutePath();
    	String t_temp = new File(t_date, uniqid()).getAbsolutePath();
    	new File(t_temp).mkdirs();
    	return t_temp;
    }
    
    public String getBufferDir() {
    	String t_root = getTempRoot();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	String t_date = new File(t_root, sdf.format(new Date())).getAbsolutePath();
    	String t_temp = new File(t_date, "buffer").getAbsolutePath();
    	new File(t_temp).mkdirs();
    	return t_temp;
    }

    public String getBufferDir2() {
    	String t_root = getTempRoot();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_MONTH, -1);
    	String t_date = new File(t_root, sdf.format(cal.getTime())).getAbsolutePath();
    	String t_temp = new File(t_date, "buffer").getAbsolutePath();
    	new File(t_temp).mkdirs();
    	return t_temp;
    }

    public Entity newEntity(String path) {
    	Entity tag = null;
    	try {
    		String schemaDir = new File(getDatDir(), "schema").getCanonicalPath();
    		String instanceDir = new File(schemaDir, path).getCanonicalPath();
    		if (instanceDir.startsWith(schemaDir)) {
    			tag = new Entity(new LuceneHandler(instanceDir));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    		tag = null;
    	}
    	return tag;
    }
    
}
