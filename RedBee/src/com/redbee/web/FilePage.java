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
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redbee.Controller;

public class FilePage extends Page {

	public FilePage(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void execute() throws Exception {
		String drive = getParameter("drive");
		String kind = getParameter("kind");
		if ("buffer".equals(drive)) {
			if ("preview".equals(kind)) {
				String code = getParameter("code");
				boolean found = false;
				File file = new File(Controller.getInstance().getBufferDir(), code + ".png");
				if (file.exists()) {
					found = true;
				}
				if (!found) {
					file = new File(Controller.getInstance().getBufferDir2(), code + ".png");
					if (file.exists()) {
						found = true;
					}
				}
				if (found) {
					InputStream is = new FileInputStream(file);
					byte[] data = new byte[is.available()];
					is.read(data);
					is.close();
					response.getOutputStream().write(data);
				}
			}
		} else if ("bookmark".equals(drive)) {
			if ("full-preview".equals(kind)) {
				String code = getParameter("code");
				String previewDir = new File(Controller.getInstance().getDatDir(), "files").getAbsolutePath();
				previewDir = new File(previewDir, "bookmark").getAbsolutePath();
				String fullDir = new File(previewDir, "full-preview").getAbsolutePath();
				File file = new File(fullDir, code + ".png");
				if (file.exists()) {
					InputStream is = new FileInputStream(file);
					byte[] data = new byte[is.available()];
					is.read(data);
					is.close();
					response.getOutputStream().write(data);
				}
			}
			if ("small-preview".equals(kind)) {
				String code = getParameter("code");
				String previewDir = new File(Controller.getInstance().getDatDir(), "files").getAbsolutePath();
				previewDir = new File(previewDir, "bookmark").getAbsolutePath();
				String smallDir = new File(previewDir, "small-preview").getAbsolutePath();
				File file = new File(smallDir, code + ".png");
				if (file.exists()) {
					InputStream is = new FileInputStream(file);
					byte[] data = new byte[is.available()];
					is.read(data);
					is.close();
					response.getOutputStream().write(data);
				}
			}
		}
	}
	
}
