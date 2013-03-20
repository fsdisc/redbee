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

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Labels {

    private static Logger logger = Logger.getLogger(Labels.class);
	
	private static Properties props;
	
	private static void setup() {
		if (props != null) return;
		try {
			InputStream is = Labels.class.getResourceAsStream("/com/redbee/Labels.properties");
			props = new Properties();
			props.load(is);
			is.close();
		} catch (Exception e) {
			logger.error("", e);
			props = null;
		}
	}
	
	public static String get(String name) {
		setup();
		String tag = "";
		if (props != null && props.containsKey(name)) {
			tag = props.getProperty(name);
		}
		return tag;
	}
	
	public static String get(String name, Object... args) {
		String tag = get(name);
		tag = MessageFormat.format(tag, args);
		return tag;
	}
	
}
