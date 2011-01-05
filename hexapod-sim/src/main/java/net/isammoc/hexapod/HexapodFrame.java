/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.isammoc.hexapod;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;

public class HexapodFrame {

	private static JmeCanvasContext context;
	private static Canvas canvas;
	private static Application app;
	private static JFrame frame;

	private static void createFrame() {
		frame = new JFrame("Test");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				app.stop();
			}
		});

		final JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		final JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);

		final JMenuItem itemRemoveCanvas = new JMenuItem("Remove Canvas");
		menuFile.add(itemRemoveCanvas);
		itemRemoveCanvas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (itemRemoveCanvas.getText().equals("Remove Canvas")) {
					frame.getContentPane().remove(canvas);

					// force OS to repaint over canvas ..
					// this is needed since AWT does not handle
					// that when a heavy-weight component is removed.
					frame.setVisible(false);
					frame.setVisible(true);
					frame.requestFocus();

					itemRemoveCanvas.setText("Add Canvas");
				} else if (itemRemoveCanvas.getText().equals("Add Canvas")) {
					frame.getContentPane().add(canvas);

					itemRemoveCanvas.setText("Remove Canvas");
				}
			}
		});

		final JMenuItem itemKillCanvas = new JMenuItem("Stop/Start Canvas");
		menuFile.add(itemKillCanvas);
		itemKillCanvas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.getContentPane().remove(canvas);
				app.stop(true);

				final String appClass = "jme3test.model.shape.TestBox";
				createCanvas(appClass);
				frame.getContentPane().add(canvas);
				frame.pack();
				startApp();
			}
		});

		final JMenuItem itemExit = new JMenuItem("Exit");
		menuFile.add(itemExit);
		itemExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				frame.dispose();
				app.stop();
			}
		});

		final JMenu menuEdit = new JMenu("Edit");
		menuBar.add(menuEdit);
		final JMenuItem itemDelete = new JMenuItem("Delete");
		menuEdit.add(itemDelete);

		final JMenu menuView = new JMenu("View");
		menuBar.add(menuView);
		final JMenuItem itemSetting = new JMenuItem("Settings");
		menuView.add(itemSetting);

		final JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);
	}

	public static void createCanvas(final String appClass) {
		final AppSettings settings = new AppSettings(true);
		settings.setWidth(640);
		settings.setHeight(480);

		JmeSystem.setLowPermissions(true);

		try {
			final Class<? extends Application> clazz = (Class<? extends Application>) Class
					.forName(appClass);
			app = clazz.newInstance();
		} catch (final ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (final InstantiationException ex) {
			ex.printStackTrace();
		} catch (final IllegalAccessException ex) {
			ex.printStackTrace();
		}

		app.setPauseOnLostFocus(false);
		app.setSettings(settings);
		app.createCanvas();

		context = (JmeCanvasContext) app.getContext();
		canvas = context.getCanvas();
		canvas.setSize(settings.getWidth(), settings.getHeight());
	}

	public static void startApp() {
		app.startCanvas();
		app.enqueue(new Callable<Void>() {
			@Override
			public Void call() {
				if (app instanceof SimpleApplication) {
					final SimpleApplication simpleApp = (SimpleApplication) app;
					simpleApp.getFlyByCamera().setDragToRotate(true);
				}
				return null;
			}
		});

	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);

				// final String appClass = "jme3test.model.shape.TestBox";

				createCanvas(HexapodJME.class.getName());
				createFrame();
				frame.getContentPane().add(canvas);
				frame.pack();
				startApp();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
