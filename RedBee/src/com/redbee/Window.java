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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Window {

    private static Logger logger = Logger.getLogger(Window.class);
    
    public static String HOME_URL = "http://google.com";
    
	private Shell shell;
	
	private Browser browser;
	private Text urlText;
	private Label statusLabel;
	private String homeUrl;
	private Menu menu;

	public Window() {
		homeUrl = "";
		shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		decorate();
	}

	public Window(String homeUrl) {
		this.homeUrl = homeUrl.trim();
		shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		decorate();
	}
	
	public Browser getBrowser() {
		return browser;
	}
	
	protected void decorate() {
		FormData fd;
		Label label;
		Text text;
		MenuItem mi;
		
		shell.setSize(800, 600);
		UITool.placeCentered(shell, 800, 600);
		shell.setText(Labels.get("MainWindow.Title"));
		shell.setImage(Images.get("Icon.16x16"));
		shell.setLayout(new FormLayout());

		String xulDir = new File(System.getProperty("user.dir"), "ext/xulrunner").getAbsolutePath();
        System.setProperty("org.eclipse.swt.browser.XULRunnerPath", xulDir);
        
        text = new Text(shell, SWT.BORDER);
        urlText = text;
		fd = new FormData();
		fd.top = new FormAttachment(0, 1);
		fd.left = new FormAttachment(0, 1);
		fd.right = new FormAttachment(100, -1);
		text.setLayoutData(fd);

		text.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent ke) {
				if (ke.character == 13) {
					try {
						setUrl(urlText.getText());
					} catch (Exception e) {
						UITool.errorBox(shell, e);
					}
				}
			}
			@Override
			public void keyPressed(KeyEvent ke) {
			}
		});
        
		label = new Label(shell, SWT.BORDER);
		statusLabel = label;
		fd = new FormData();
		fd.top = new FormAttachment(100, -16);
		fd.left = new FormAttachment(0, 0);
		fd.bottom = new FormAttachment(100, 0);
		fd.right = new FormAttachment(100, 0);
		label.setLayoutData(fd);
		
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		fd = new FormData();
		fd.top = new FormAttachment(0, 22);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		label.setLayoutData(fd);
        
		browser = new Browser(shell, SWT.MOZILLA);
		fd = new FormData();
		fd.top = new FormAttachment(0, 24);
		fd.left = new FormAttachment(0, 0);
		fd.bottom = new FormAttachment(100, -18);
		fd.right = new FormAttachment(100, 0);
		browser.setLayoutData(fd);
		
		browser.addLocationListener(new LocationListener() {
			@Override
			public void changing(LocationEvent le) {
			}
			@Override
			public void changed(LocationEvent le) {
				try {
					urlText.setText(le.location);
				} catch (Exception e) {
				}
			}
		});
		
		browser.addStatusTextListener(new StatusTextListener() {
			@Override
			public void changed(StatusTextEvent ste) {
				try {
					statusLabel.setText(ste.text);
				} catch (Exception e) {
				}
			}
		});
		
		browser.addTitleListener(new TitleListener() {
			@Override
			public void changed(TitleEvent te) {
				try {
					shell.setText(te.title);
				} catch (Exception e) {
				}
			}
		});
		
		browser.addCloseWindowListener(new CloseWindowListener() {
			@Override
			public void close(WindowEvent we) {
				shell.close();
			}
		});
		
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent we) {
				final WindowEvent twe = we;
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							Window w = new Window();
							twe.browser = w.getBrowser();
							twe.browser.getShell().open();
						} catch (Exception e) {
							UITool.errorBox(shell, e);
						}
					}
				});
			}
		});
		
		new CustomFunction(browser, "system_close");
		
		menu = new Menu(shell, SWT.POP_UP);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.NewWindow"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				String url = HOME_URL;
				try {
					url = new URL(statusLabel.getText()).toString();
				} catch (Exception e) {
					
				}
				final String turl = url;
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							new Window(turl).open();
						} catch (Exception e) {
							UITool.errorBox(shell, e);
						}
					}
				});
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.SEPARATOR);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.Back"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					browser.back();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.Forward"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					browser.forward();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.Reload"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					browser.refresh();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.Stop"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					browser.stop();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.Home"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					setUrl(HOME_URL);
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.SEPARATOR);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.BookmarkPage"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					String link = browser.getUrl();
					try {
						link = new URL(link).toString();
					} catch (Exception e) {
						link = "";
					}
					if (link.length() == 0) return;
					if (link.startsWith(HOME_URL)) return;
					String url = HOME_URL + "/bookmark.jsp?l=" + java.net.URLEncoder.encode(link, "UTF-8");
					OpenWindowTask task = new OpenWindowTask(url);
					task.start();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.BookmarkLink"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					String link = "";
					try {
						link = new URL(statusLabel.getText()).toString();
					} catch (Exception e) {
						link = "";
					}
					if (link.length() == 0) return;
					if (link.startsWith(HOME_URL)) return;
					String url = HOME_URL + "/bookmark.jsp?l=" + java.net.URLEncoder.encode(link, "UTF-8");
					OpenWindowTask task = new OpenWindowTask(url);
					task.start();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.BookmarkPagePreview"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					String link = browser.getUrl();
					try {
						link = new URL(link).toString();
					} catch (Exception e) {
						link = "";
					}
					if (link.length() == 0) return;
					if (link.startsWith(HOME_URL)) return;
					String url = HOME_URL + "/bookmark.jsp?l=" + java.net.URLEncoder.encode(link, "UTF-8") + "&c=1";
					OpenWindowTask task = new OpenWindowTask(url);
					task.start();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.BookmarkLinkPreview"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					String link = "";
					try {
						link = new URL(statusLabel.getText()).toString();
					} catch (Exception e) {
						link = "";
					}
					if (link.length() == 0) return;
					if (link.startsWith(HOME_URL)) return;
					String url = HOME_URL + "/bookmark.jsp?l=" + java.net.URLEncoder.encode(link, "UTF-8") + "&c=1";
					OpenWindowTask task = new OpenWindowTask(url);
					task.start();
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.SEPARATOR);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.CopyLink"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					String url = "";
					try {
						url = new URL(statusLabel.getText()).toString();
					} catch (Exception e) {
						url = "";
					}
					if (url.length() > 0) {
						Clipboard clipboard = new Clipboard(shell.getDisplay());
						TextTransfer transfer = TextTransfer.getInstance();
						clipboard.setContents(new Object[] { url }, new TextTransfer[] { transfer });
						clipboard.dispose();
					}
					
				} catch (Exception e) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.SaveLink"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				String url = "";
				try {
					url = new URL(statusLabel.getText()).toString();
				} catch (Exception e) {
					url = "";
				}
				if (url.length() == 0) return;
				if (url.startsWith(HOME_URL)) return;
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setText(Labels.get("Window.DirDlg.Title"));
				dlg.setMessage(Labels.get("Window.DirDlg.Message"));
				String folder = dlg.open();
				if (folder == null) return;
				
            	Cursor cursor = shell.getCursor();
            	shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT));
				try {
					String link = url;
					PageSaver saver = new PageSaver(link, folder);
					saver.run();
				} catch (Exception e) {
					UITool.errorBox(shell, e);
				}
            	shell.setCursor(cursor);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.SavePage"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				if (browser.getUrl().startsWith(HOME_URL)) return;
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setText(Labels.get("Window.DirDlg.Title"));
				dlg.setMessage(Labels.get("Window.DirDlg.Message"));
				String folder = dlg.open();
				if (folder == null) return;
				
            	Cursor cursor = shell.getCursor();
            	shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT));
				try {
					String link = urlText.getText();
					PageSaver saver = new PageSaver(link, folder);
					saver.run();
				} catch (Exception e) {
					UITool.errorBox(shell, e);
				}
            	shell.setCursor(cursor);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.SEPARATOR);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.CaptureArea"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.png" });
				dlg.setFilterNames(new String[] { "PNG (*.png)" });
				String filename = dlg.open();
				if (filename == null) return;

				Cursor cursor = shell.getCursor();
            	shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT));
				try {
					Thread.sleep(1000);
					GC source = new GC(browser);
					Image image = new Image(shell.getDisplay(), browser.getClientArea());
					source.copyArea(image, 0, 0);
					ImageLoader io = new ImageLoader();
				    io.data = new ImageData[] {image.getImageData()};
				    io.save(filename, SWT.IMAGE_PNG);
				} catch (Exception e) {
				}
            	shell.setCursor(cursor);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.CapturePage"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				String link = browser.getUrl();
				if (link.startsWith(HOME_URL)) return;
				
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.png" });
				dlg.setFilterNames(new String[] { "PNG (*.png)" });
				String filename = dlg.open();
				if (filename == null) return;

				Cursor cursor = shell.getCursor();
            	shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT));
				try {
					Thread.sleep(1000);
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
				} catch (Exception e) {
					logger.error("", e);
					UITool.errorBox(shell, e);
				}
            	shell.setCursor(cursor);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.CaptureLink"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				String link = "";
				try {
					link = new URL(statusLabel.getText()).toString();
				} catch (Exception e) {
					link = "";
				}
				if (link.length() == 0) return;
				if (link.startsWith(HOME_URL)) return;
				
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.png" });
				dlg.setFilterNames(new String[] { "PNG (*.png)" });
				String filename = dlg.open();
				if (filename == null) return;

				Cursor cursor = shell.getCursor();
            	shell.setCursor(new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT));
				try {
					Thread.sleep(1000);
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
				} catch (Exception e) {
					logger.error("", e);
					UITool.errorBox(shell, e);
				}
            	shell.setCursor(cursor);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		mi = new MenuItem(menu, SWT.SEPARATOR);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Window.OpenFile"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				FileDialog dlg = new FileDialog(shell);
				dlg.setFilterExtensions(new String[] { "*.*" });
				String filename = dlg.open();
				if (filename == null) return;
				filename = filename.replace("\\\\", "/");
				filename = "file://" + filename;
				setUrl(filename);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		
		browser.setMenu(menu);
		
		if (homeUrl.length() > 0) {
			setUrl(homeUrl);
		}
		
		browser.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent ke) {
				if (ke.keyCode == 108) {
					if ((ke.stateMask & SWT.CTRL) > 0) {
						urlText.setFocus();
						urlText.setSelection(0, urlText.getText().length());
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent ke) {
			}
		});
		urlText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent ke) {
				if (ke.keyCode == 108) {
					if ((ke.stateMask & SWT.CTRL) > 0) {
						urlText.setFocus();
						urlText.setSelection(0, urlText.getText().length());
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent ke) {
			}
		});
	}
	
	public void setUrl(String url) {
		try {
			urlText.setText(url);
			browser.setUrl(urlText.getText());
		} catch (Exception e) {
			UITool.errorBox(shell, e);
		}
	}
	
	public void open() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
	}
	
	private class OpenWindowTask extends Thread {
		
		private String link;
		
		public OpenWindowTask(String link) {
			this.link = link;
		}
		
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						new Window(link).open();
					} catch (Exception e) {
						UITool.errorBox(shell, e);
					}
				}
			});
		}
		
	}
	
	private class CustomFunction extends BrowserFunction {

		private String name;
		
		public CustomFunction(Browser browser, String name) {
			super(browser, name);
			this.name = name;
		}

        public Object function(Object[] arguments) {
        	if ("system_close".equals(name)) {
        		Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
		        		shell.close();
					}
        		});
        		return null;
        	}
        	return null;
        }
        
	}
}
