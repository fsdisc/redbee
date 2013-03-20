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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.redbee.Controller;
import com.redbee.schema.Entity;

public class BookmarkPage extends Page {

    private static Logger logger = Logger.getLogger(BookmarkPage.class);
	
	public BookmarkPage(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void execute() throws Exception {
		Map args = newArgs();
		String link = getParameter("l");
		String title = getParameter("title");
		String desc = getParameter("desc");
		String preview = getParameter("preview");
		String capture = getParameter("c");
		String message = "";
		boolean finished = false;
		if (isPost()) {
			if (message.length() == 0 && title.trim().length() == 0) {
				message = "TitleRequired";
			}
			if (message.length() == 0) {
				Entity pat = Controller.getInstance().newEntity("bookmark");
				List<Entity> rs = pat.search("link", pat.newTermQuery(pat.newTerm("link", link)), 1);
				if (rs.size() == 0) {
					pat.setKind("link");
					pat.setSchema("a|title|a|desc|s|link|s|preview");
					pat.setId(Controller.getInstance().uniqid());
					pat.setString("link", link);
					pat.setString("preview", "");
				} else {
					pat = rs.get(0);
				}
				if ("1".equals(capture)) {
					try {
						boolean found = false;
						File file = new File(Controller.getInstance().getBufferDir(), preview + ".png");
						if (file.exists()) {
							found = true;
						}
						if (!found) {
							file = new File(Controller.getInstance().getBufferDir2(), preview + ".png");
							if (file.exists()) {
								found = true;
							}
						}
						if (found) {
							String previewDir = new File(Controller.getInstance().getDatDir(), "files").getAbsolutePath();
							previewDir = new File(previewDir, "bookmark").getAbsolutePath();
							String fullDir = new File(previewDir, "full-preview").getAbsolutePath();
							String smallDir = new File(previewDir, "small-preview").getAbsolutePath();
							new File(fullDir).mkdirs();
							new File(smallDir).mkdirs();
							InputStream is = new FileInputStream(file);
							byte[] data = new byte[is.available()];
							is.read(data);
							is.close();
							OutputStream os = new FileOutputStream(new File(fullDir, preview + ".png"));
							os.write(data);
							os.close();
							
							Image srcImg = new Image(Display.getDefault(), new FileInputStream(file));
							Image tagImg = new Image(Display.getDefault(), new Rectangle(0, 0, 800, 600));
							GC gc = new GC(tagImg);
							gc.drawImage(srcImg, 0, 0);
							ImageLoader io = new ImageLoader();
						    io.data = new ImageData[] {tagImg.getImageData()};
						    io.save(new FileOutputStream(new File(smallDir, preview + ".png")), SWT.IMAGE_PNG);

							pat.setString("preview", preview);
						}
					} catch (Exception e) {
						logger.error("", e);
					}
				}				
				pat.setString("title", title);
				pat.setString("desc", desc);
				pat.save();
				finished = true;
			}
		} else {
			try {
				org.jsoup.Connection conn = org.jsoup.Jsoup.connect(link);
				conn.timeout(60000);
				conn.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
				org.jsoup.nodes.Document doc = conn.get();
				link = conn.request().url().toString();
				title = doc.title();
				desc = doc.text();
			} catch (Exception e) {
				logger.error("", e);
			}
			if ("1".equals(capture)) {
				try {
					String id = Controller.getInstance().uniqid();
					String filename = new File(Controller.getInstance().getBufferDir(), id + ".png").getAbsolutePath();
					String url = "http://api.snapito.com/?url=" + java.net.URLEncoder.encode(link, "UTF-8");
					org.jsoup.Connection conn = org.jsoup.Jsoup.connect(link);
					conn.timeout(60000);
					conn.ignoreContentType(true);
					conn.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
					conn.execute();
					url = "http://cache.snapito.com/api/image?_cache_redirect=true&url=" + java.net.URLEncoder.encode(link, "UTF-8") + "&type=png";
					conn.url(url);
					byte[] data = conn.execute().bodyAsBytes();
					OutputStream os = new FileOutputStream(filename);
					os.write(data);
					os.close();
					preview = id;
				} catch (Exception e) {
					logger.error("", e);
					preview = "";
				}
			}
		}
		args.put("capture", capture);
		args.put("preview", preview);
		args.put("finished", finished);
		args.put("message", message);
		args.put("link", link);
		args.put("title", title);
		args.put("desc", desc);
		args.put("linkR", link.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
		args.put("titleR", title.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
		args.put("descR", desc.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
		loadPage("Bookmark.vm", args);
	}

}
