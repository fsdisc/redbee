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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;

import com.redbee.Controller;
import com.redbee.schema.Entity;

public class BookmarkListPage extends Page {

    private static Logger logger = Logger.getLogger(BookmarkListPage.class);
	
	public BookmarkListPage(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void execute() throws Exception {
		Map args = newArgs();
		String query = getParameter("q");
		args.put("query", query);
		args.put("queryR", query.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
		args.put("queryE", URLEncoder.encode(query, "UTF-8"));
		int pageno = 1;
		try {
			pageno = Integer.parseInt(getParameter("p"));
		} catch (Exception e) {
			pageno = 1;
		}
		search(query, pageno, args);
		loadPage("BookmarkList.vm", args);
	}

	private void search(String query, int pageno, Map args) {
		Entity pat = Controller.getInstance().newEntity("bookmark");
		int pagesize = 10;
	    int pagecount = 1;
		int count = 0;
		List<Entity> rs;
		Query qry = pat.newMatchAllDocsQuery();
		if ("".equals(query)) {
			count = pat.count("link", qry, Integer.MAX_VALUE);
		} else {
			try {
				qry = pat.parseQuery(query, new String[] { "title", "desc" }, new org.apache.lucene.search.BooleanClause.Occur[] { pat.occurShould(), pat.occurShould() });
			} catch (Exception e) {
				logger.error("", e);
			}
			count = pat.count("link", qry, Integer.MAX_VALUE);
		}
	    if (count > 0) {
	    	pagecount = ((count - 1 - ((count - 1) % pagesize)) / pagesize) + 1;
	    }
	    if (pageno < 1) pageno = 1;
	    if (pageno > pagecount) {
	    	pageno = pagecount;
	    }
		args.put("pageno", pageno);
		if ("".equals(query)) {
			rs = pat.search("link", qry, pat.newSort(pat.newSortField(pat.CREATED, pat.sortFieldLong(), true)), pagesize, pageno);
		} else {
			rs = pat.search("link", qry, pat.newSort(pat.sortFieldScore(), pat.newSortField(pat.CREATED, pat.sortFieldLong(), true)), pagesize, pageno);
		}

		List results = new ArrayList();
		for (int i = 0; i < rs.size(); i++) {
			Entity et = rs.get(i);
			Map it = new HashMap();
			it.put("preview", et.getString("preview"));
			it.put("link", et.getString("link"));
			it.put("title", et.getString("title"));
			it.put("desc", et.getString("desc"));
			if ("".equals(query)) {
				it.put("titleF", et.getString("title").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
				String descF = et.getString("desc");
				int maxlength = 200;
				if (descF.length() > maxlength) {
					int pos = descF.lastIndexOf(" ", maxlength);
					if (pos < 0) {
						descF = descF.substring(0, maxlength) + " ...";
					} else {
						descF = descF.substring(0, pos) + " ...";
					}
				}
				it.put("descF", descF.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
			} else {
			    try {
			        String titleF = pat.highlight(qry, et.getString("title"), "title", 100, 3, " (...) "); 
					it.put("titleF", titleF.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&lt;B&gt;", "<b>").replaceAll("&lt;/B&gt;", "</b>"));
			    } catch (Exception e) {
					it.put("titleF", et.getString("title").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
			    }
			    try {
			        String descF = pat.highlight(qry, et.getString("desc"), "desc", 100, 3, " (...) ");
					it.put("descF", descF.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&lt;B&gt;", "<b>").replaceAll("&lt;/B&gt;", "</b>"));
			    } catch (Exception e) {
					String descF = et.getString("desc");
					int maxlength = 200;
					if (descF.length() > maxlength) {
						int pos = descF.lastIndexOf(" ", maxlength);
						if (pos < 0) {
							descF = descF.substring(0, maxlength) + " ...";
						} else {
							descF = descF.substring(0, pos) + " ...";
						}
					}
					it.put("descF", descF.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
			    }
			}
			results.add(it);
		}
		args.put("results", results);
		
		List<Integer> pagelist = new ArrayList<Integer>();
		int maxpage = pagecount;
		for (int i = 1; i <= maxpage; i++) {
			if ((i >= 1 && i <= 5) || (i >= pageno - 5 && i <= pageno + 5) || (i >= maxpage - 4 && i <= maxpage)) {
				pagelist.add(i);
			}
		}
		args.put("pagelist", pagelist);
	}
	
}
