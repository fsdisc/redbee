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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redbee.Controller;
import com.redbee.schema.Entity;

public class HomePage extends Page {

	public HomePage(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void execute() throws Exception {
		Map args = newArgs();
		
		Entity pat = Controller.getInstance().newEntity("bookmark");
		List<Entity> rs = pat.search("link", pat.newMatchAllDocsQuery(), pat.newSort(pat.newSortField(pat.CREATED, pat.sortFieldLong(), true)) , 50);
		List bookmarks = new ArrayList();
		for (int i = 0; i < rs.size(); i++) {
			Entity et = rs.get(i);
			Map bm = new HashMap();
			bm.put("title", et.getString("title"));
			bm.put("link", et.getString("link"));
			bm.put("preview", et.getString("preview"));
			bookmarks.add(bm);
		}
		args.put("bookmarks", bookmarks);
		loadPage("Home.vm", args);
	}
	
}
