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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import com.redbee.web.Router;

public class MainWindow {

    private static Logger logger = Logger.getLogger(MainWindow.class);
    
	private Shell shell;
	private Display display;
	private Tray tray;
	private TrayItem trayItem;
	private Menu menu;
	
	public MainWindow() {
		display = Display.getDefault();
		shell = new Shell(display,  SWT.NO_TRIM | SWT.ON_TOP);
		decorate();
	}

	protected void decorate() {
		MenuItem mi;
		
		shell.setSize(0, 0);
		shell.setImage(Images.get("Icon.16x16"));
		shell.setVisible(false);

		tray = display.getSystemTray();
		trayItem = new TrayItem (tray, SWT.NONE);
		trayItem.setToolTipText(Labels.get("MainWindow.Title"));		
		trayItem.setImage(Images.get("Icon.16x16"));
		trayItem.setHighlightImage(Images.get("Icon.16x16"));
		
		menu = new Menu(shell, SWT.POP_UP);
		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText(Labels.get("Tray.NewWindow"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							new Window(Window.HOME_URL).open();
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
		mi.setText(Labels.get("Tray.Exit"));
		mi.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				shell.close();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});

		HomeTask task = new HomeTask();
		task.start();
	}
	
	public void open() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
	
	private class HomeTask extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				logger.error("", e);
			}
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						Window w = new Window();
						w.setUrl(Window.HOME_URL + "/" + Router.MAGIC);
						w.open();
					} catch (Exception e) {
						UITool.errorBox(shell, e);
					}
				}
			});
		}
	}
	
}
