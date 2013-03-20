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

public class PageSaver {

	private final static int MAX_LEVEL = 5;
	
	private String link;
	private String folder;
	private java.util.Map<String, String> map;
	private int level;

	public PageSaver(String link, String folder) {
		this.link = link;
		this.folder = folder;
		this.map = new java.util.HashMap<String, String>();
		this.level = 1;
	}
	
	public PageSaver(String link, String folder, int level) {
		this.link = link;
		this.folder = folder;
		this.map = new java.util.HashMap<String, String>();
		this.level = level;
	}
	
	public void run() throws Exception {
		new java.io.File(folder).mkdirs();
		org.jsoup.Connection conn = org.jsoup.Jsoup.connect(link);
		conn.timeout(60000);
		conn.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
		org.jsoup.nodes.Document doc = conn.get();
		link = conn.request().url().toString();
		org.jsoup.select.Elements children = doc.select("a");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			String url = child.attr("href");
			try {
				java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
				url = turl.toString();
			} catch (Exception e) {
				continue;
			}
			child.attr("href", url);
		}
		children = doc.select("img");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			String url = child.attr("src");
			String url2 = "";
			try {
				java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
				url = turl.toString();
				url2 = url;
				String q = turl.getQuery();
				if (q != null) {
					url2 = url.substring(0, url.length() - q.length());
					if (url2.endsWith("?")) {
						url2 = url2.substring(0, url2.length() - 1);
					}
				}
			} catch (Exception e) {
				continue;
			}
			String filename = suniqid();
			int pos1 = url2.lastIndexOf(".");
			int pos2 = url2.lastIndexOf("/");
			if (pos1 > pos2) {
				String ext = toLetterDigit(url2.substring(pos1 + 1));
				if (ext.equalsIgnoreCase("com") || ext.equalsIgnoreCase("exe")) {
					ext += "_";
				}
				if (ext.length() > 0) {
					filename += "." + ext;
				}
			}
			try {
				if (map.containsKey(url)) {
					filename = map.get(url);
				} else {
					save(new java.io.File(folder, filename).getAbsolutePath(), url);
					map.put(url, filename);
				}
				child.attr("src", filename);
			} catch (Exception e) {
				child.attr("src", url);
			}
		}
		parseStyleAttr(doc);
		children = doc.select("style");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			child.html(parseStyleUrl(child.html()));
		}
		children = doc.select("link");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			if ("stylesheet".equalsIgnoreCase(child.attr("rel"))) {
				String url = child.attr("href");
				String url2 = "";
				try {
					java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
					url = turl.toString();
					url2 = url;
					String q = turl.getQuery();
					if (q != null) {
						url2 = url.substring(0, url.length() - q.length());
						if (url2.endsWith("?")) {
							url2 = url2.substring(0, url2.length() - 1);
						}
					}
				} catch (Exception e) {
					continue;
				}
				String filename = suniqid();
				int pos1 = url2.lastIndexOf(".");
				int pos2 = url2.lastIndexOf("/");
				if (pos1 > pos2) {
					String ext = toLetterDigit(url2.substring(pos1 + 1));
					if (ext.equalsIgnoreCase("com") || ext.equalsIgnoreCase("exe")) {
						ext += "_";
					}
					if (ext.length() > 0) {
						filename += "." + ext;
					}
				}
				try {
					if (map.containsKey(url)) {
						filename = map.get(url);
					} else {
						save(new java.io.File(folder, filename).getAbsolutePath(), url);
						String style = new String(read(new java.io.File(folder, filename).getAbsolutePath()), "UTF-8");
						style = parseStyleUrl(style);
						save(new java.io.File(folder, filename).getAbsolutePath(), style.getBytes("UTF-8"));
						map.put(url, filename);
					}
					child.attr("href", filename);
				} catch (Exception e) {
					child.attr("href", url);
				}
			} else {
				String url = child.attr("href");
				try {
					java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
					url = turl.toString();
				} catch (Exception e) {
					continue;
				}
				child.attr("href", url);
			}
		}
		children = doc.select("script");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			String url = child.attr("src");
			if (url.trim().length() == 0) continue;
			String url2 = "";
			try {
				java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
				url = turl.toString();
				url2 = url;
				String q = turl.getQuery();
				if (q != null) {
					url2 = url.substring(0, url.length() - q.length());
					if (url2.endsWith("?")) {
						url2 = url2.substring(0, url2.length() - 1);
					}
				}
			} catch (Exception e) {
				continue;
			}
			String filename = suniqid();
			int pos1 = url2.lastIndexOf(".");
			int pos2 = url2.lastIndexOf("/");
			if (pos1 > pos2) {
				String ext = toLetterDigit(url2.substring(pos1 + 1));
				if (ext.equalsIgnoreCase("com") || ext.equalsIgnoreCase("exe")) {
					ext += "_";
				}
				if (ext.length() > 0) {
					filename += "." + ext;
				}
			}
			try {
				if (map.containsKey(url)) {
					filename = map.get(url);
				} else {
					save(new java.io.File(folder, filename).getAbsolutePath(), url);
					map.put(url, filename);
				}
				child.attr("src", filename);
			} catch (Exception e) {
				child.attr("src", url);
			}
		}
		children = doc.select("frame");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			String url = child.attr("src");
			try {
				java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
				url = turl.toString();
			} catch (Exception e) {
				continue;
			}
			String filename = suniqid();
			try {
				if (level < MAX_LEVEL) {
					new PageSaver(url, new java.io.File(folder, filename).getAbsolutePath(), level + 1).run();
					child.attr("src", filename + "/index.html");
				} else {
					child.attr("src", url);
				}
			} catch (Exception e) {
				child.attr("src", url);
			}
		}
		children = doc.select("iframe");
		for (int i = 0; i < children.size(); i++) {
			org.jsoup.nodes.Element child = children.get(i);
			String url = child.attr("src");
			try {
				java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
				url = turl.toString();
			} catch (Exception e) {
				continue;
			}
			String filename = suniqid();
			try {
				if (level < MAX_LEVEL) {
					new PageSaver(url, new java.io.File(folder, filename).getAbsolutePath(), level + 1).run();
					child.attr("src", filename + "/index.html");
				} else {
					child.attr("src", url);
				}
			} catch (Exception e) {
				child.attr("src", url);
			}
		}
		children = doc.select("base");
		for (int i = children.size() - 1; i >= 0; i--) {
			org.jsoup.nodes.Element child = children.get(i);
			child.remove();
		}
		
		String filename = new java.io.File(folder, "index.html").getAbsolutePath();
		save(filename, doc.html().getBytes("UTF-8"));
	}
	
	private String parseStyleUrl(String style) {
		style = style.replaceAll("<!--", "");
		style = style.replaceAll("-->", "");
		String styleL = style.toLowerCase();
		String styleT = "";
		int oldpos = 0;
		int pos1 = styleL.indexOf("url(");
		while (pos1 >= 0) {
			int pos2 = style.indexOf(")", pos1 + 4);
			if (pos2 >= 0) {
				String url = style.substring(pos1 + 4, pos2);
				if (url.startsWith("\"")) {
					url = url.substring(1);
				} else if (url.startsWith("'")) {
					url = url.substring(1);
				}
				if (url.endsWith("\"")) {
					url = url.substring(0, url.length() - 1);
				} else if (url.endsWith("'")) {
					url = url.substring(0, url.length() - 1);
				}
				String url2 = "";
				try {
					java.net.URL turl = new java.net.URL(new java.net.URL(link), url);
					url = turl.toString();
					url2 = url;
					String q = turl.getQuery();
					if (q != null) {
						url2 = url.substring(0, url.length() - q.length());
						if (url2.endsWith("?")) {
							url2 = url2.substring(0, url2.length() - 1);
						}
					}
				} catch (Exception e) {
				}
				String filename = suniqid();
				int posA = url2.lastIndexOf(".");
				int posB = url2.lastIndexOf("/");
				if (posA > posB) {
					String ext = toLetterDigit(url2.substring(posA + 1));
					if (ext.equalsIgnoreCase("com") || ext.equalsIgnoreCase("exe")) {
						ext += "_";
					}
					if (ext.length() > 0) {
						filename += "." + ext;
					}
				}
				try {
					if (map.containsKey(url)) {
						filename = map.get(url);
					} else {
						save(new java.io.File(folder, filename).getAbsolutePath(), url);
						map.put(url, filename);
					}
					styleT += style.substring(oldpos, pos1 + 4) + "'" + filename + "'";
				} catch (Exception e) {
					styleT += style.substring(oldpos, pos1 + 4) + url;
				}
				
				oldpos = pos2;
				pos1 = styleL.indexOf("url(", oldpos);
			} else {
				pos1 = styleL.indexOf("url(", pos1 + 4);
			}
		}
		styleT += style.substring(oldpos);
		return styleT;
	}
	
	private void parseStyleAttr(org.jsoup.nodes.Element parent) {
		String style = parent.attr("style");
		if (style != null && style.length() > 0) {
			parent.attr("style", parseStyleUrl(style));
		}

		for (int i = 0; i < parent.children().size(); i++) {
			parseStyleAttr(parent.child(i));
		}
	}
	
	private String toLetterDigit(String src) {
		String tag = "";
		for (int i = 0; i < src.length(); i++) {
			if (Character.isLetterOrDigit(src.charAt(i))) {
				tag += src.charAt(i) + "";
			}
		}
		return tag;
	}
	
	private void save(String filename, String url) throws Exception {
		org.jsoup.Connection conn = org.jsoup.Jsoup.connect(url);
		conn.timeout(60000);
		conn.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
		conn.ignoreContentType(true);
		byte[] data = conn.execute().bodyAsBytes();
		save(filename, data);
	}
	
	private byte[] read(String filename) throws Exception {
		java.io.InputStream is = new java.io.FileInputStream(filename);
		byte[] data = new byte[is.available()];
		is.read(data);
		is.close();
		return data;
	}
	
	private void save(String filename, byte[] data) throws Exception {
		java.io.OutputStream os = new java.io.FileOutputStream(filename);
		os.write(data);
		os.close();
	}
	
    private String suniqid() {
        java.util.Random random = new java.util.Random();
        return Long.toString(Math.abs(random.nextLong()), 36);
    }
	
}
