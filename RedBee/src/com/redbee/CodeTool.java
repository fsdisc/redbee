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

public class CodeTool {

    public static String replace(String src, String find, String replacement) {
    	String tag = "";
    	int posA = 0;
    	int posB = src.indexOf(find, posA);
    	while (posB >= 0) {
    		tag += src.substring(posA, posB) + replacement;
    		posA = posB + find.length();
    		posB = src.indexOf(find, posA);
    	}
    	tag += src.substring(posA);
    	return tag;
    }
	
}
