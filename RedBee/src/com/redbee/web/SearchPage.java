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

public class SearchPage extends Page {

    private static Logger logger = Logger.getLogger(SearchPage.class);
	
	public SearchPage(HttpServletRequest request, HttpServletResponse response) {
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
		if (pageno < 1) pageno = 1;
		String engine = getParameter("e");
		if ("|g|b|".indexOf("|" + engine + "|") < 0) {
			engine = "g";
		}
		args.put("pageno", pageno);
		args.put("engine", engine);
		search(engine, query, pageno, args);
		loadPage("Search.vm", args);
	}

	private void search(String engine, String query, int pageno, Map args) {
		List results = new ArrayList();
		if (query.trim().length() > 0) {
			List<Result> items = new ArrayList<Result>(); 
			if ("g".equals(engine)) {
				items = Google(query, pageno);
			}
			if ("b".equals(engine)) {
				items = Bing(query, pageno);
			}
			for (int i = 0; i < items.size(); i++) {
				Result it = items.get(i);
				Map et = new HashMap();
				et.put("title", it.Title);
				et.put("titleF", it.TitleF);
				et.put("desc", it.Description);
				et.put("descF", it.DescriptionF);
				et.put("link", it.Link);
				et.put("no", it.No);
				results.add(et);
			}
		}
		args.put("results", results);
		List<Integer> pagelist = new ArrayList<Integer>();
		int maxpage = 100;
		for (int i = 1; i <= maxpage; i++) {
			if ((i >= 1 && i <= 5) || (i >= pageno - 5 && i <= pageno + 5) || (i >= maxpage - 4 && i <= maxpage)) {
				pagelist.add(i);
			}
		}
		args.put("pagelist", pagelist);
	}
	
	private java.util.List<Result> Google(String query, int pageno) {
		java.util.List<Result> tag = new java.util.ArrayList<Result>();
		try {
			String url = "http://google.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&start=" + ((pageno - 1) * 10);
			org.jsoup.Connection conn = org.jsoup.Jsoup.connect(url);
			conn.timeout(60000);
			conn.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
			org.jsoup.nodes.Document doc = conn.get();
			org.jsoup.select.Elements nodes = doc.select("#rso .g");
			for (int i = 0; i < nodes.size(); i++) {
				org.jsoup.nodes.Element node = nodes.get(i);
				org.jsoup.nodes.Element child = node.select(".vsc .r .l").first();
				if (child == null) continue;
				Result rs = new Result();
				rs.No = (pageno - 1) * 10 + i + 1;
				rs.Link = new java.net.URL(new java.net.URL(url), child.attr("href")).toString();
				rs.Title = child.text();
				rs.TitleF = child.html();
				child = node.select(".vsc .s .st").first();
				if (child != null) {
					rs.Description = child.text();
					rs.DescriptionF = child.html();
				}
				tag.add(rs);
			}
			
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	private java.util.List<Result> Bing(String query, int pageno) {
		java.util.List<Result> tag = new java.util.ArrayList<Result>();
		try {
			String url = "http://www.bing.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&first=" + ((pageno - 1) * 10 + 1);
			org.jsoup.Connection conn = org.jsoup.Jsoup.connect(url);
			conn.timeout(60000);
			conn.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
			org.jsoup.nodes.Document doc = conn.get();
			org.jsoup.select.Elements nodes = doc.select("#results .sa_wr");
			for (int i = 0; i < nodes.size(); i++) {
				org.jsoup.nodes.Element node = nodes.get(i);
				org.jsoup.nodes.Element child = node.select(".sa_cc .sa_mc .sb_tlst a").first();
				if (child == null) continue;
				Result rs = new Result();
				rs.No = (pageno - 1) * 10 + i + 1;
				rs.Link = new java.net.URL(new java.net.URL(url), child.attr("href")).toString();
				rs.Title = child.text();
				rs.TitleF = child.html();
				child = node.select(".sa_cc .sa_mc p").last();
				if (child != null) {
					rs.Description = child.text();
					rs.DescriptionF = child.html();
				}
				tag.add(rs);
			}
			
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	private static class Result {
		public int No = 0;
		public String Link = "";
		public String Title = "";
		public String Description = "";
		public String TitleF = "";
		public String DescriptionF = "";
		
		public void saveLog() {
			String output = "\r\n";
			output += "\r\nNo: " + No;
			output += "\r\nLink: " + Link;
			output += "\r\nTitle: " + Title;
			output += "\r\nDescription: \r\n" + Description;
			output += "\r\nFormatted Title: " + TitleF;
			output += "\r\nFormatted Description: \r\n" + DescriptionF;
			logger.info(output + "\r\n");
		}
		
		public static void saveLog(java.util.List<Result> results) {
			for (int i = 0; i < results.size(); i++) {
				results.get(i).saveLog();
			}
		}
	}
	
}
