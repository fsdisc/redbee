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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Images {

	private static Map<String, String> map;
	
	private static void setup() {
		if (map != null) return;
		map = new HashMap<String, String>();
		map.put("Icon.16x16", "/images/icon-16x16.png");
	}
	
	public static Image get(String name) {
		setup();
		return new Image(Display.getDefault(), Images.class.getResourceAsStream("/com/redbee/resource" + map.get(name)));
	}
	
}
