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

import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private static MessageReaderRunnable command;

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
	}

	public static void createCanvas(final Class<? extends Application> appClass) {
		final AppSettings settings = new AppSettings(true);
		settings.setWidth(640);
		settings.setHeight(480);

		JmeSystem.setLowPermissions(true);

		try {
			app = appClass.newInstance();
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
		try {
			final String portName;
			if (args.length > 0) {
				portName = args[0];
			} else {
				final ArrayList<String> ports = new ArrayList<String>();

				// From Java 1.4 compatible library
				@SuppressWarnings("unchecked")
				final Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier
						.getPortIdentifiers();
				ports.add("<none>");
				while (portIdentifiers.hasMoreElements()) {
					ports.add(portIdentifiers.nextElement().getName());
				}
				final String chosen = (String) JOptionPane.showInputDialog(null,
						"Choose a port to connect to", "Serial port", JOptionPane.PLAIN_MESSAGE, null,
						ports.toArray(), null);
				if ("<none>".equals(chosen)) {
					portName = null;
				} else {
					portName = chosen;
				}
			}

			final HexapodControlPanel[] handler = new HexapodControlPanel[1];
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					JPopupMenu.setDefaultLightWeightPopupEnabled(false);
					createCanvas(HexapodJME.class);
					createFrame();
					frame.getContentPane().add(canvas);
					final HexapodNode hexapod = ((HexapodJME) app).getHexapod();
					handler[0] = new HexapodControlPanel(new HexapodConverter(hexapod));
					frame.getContentPane().add(handler[0], BorderLayout.EAST);

					final JLabel movingLabel = new JLabel("...");
					movingLabel.setOpaque(true);
					hexapod.addPropertyChangeListener(HexapodNode.PROPERTY_MOVING,
							new PropertyChangeListener() {

								@Override
								public void propertyChange(final PropertyChangeEvent evt) {
									if ((Boolean) evt.getNewValue()) {
										// MOVING
										movingLabel.setBackground(Color.RED);
									} else {
										// STOPPED
										movingLabel.setBackground(Color.GREEN);
										command.notifyHexapodStopped();
									}
								}
							});
					frame.getContentPane().add(movingLabel, BorderLayout.SOUTH);

					frame.pack();
					startApp();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				}
			});

			if (portName != null) {
				final ExecutorService executor = Executors.newSingleThreadExecutor();
				command = new MessageReaderRunnable(portName, handler[0].getSpinModels());

				executor.execute(command);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(final WindowEvent e) {
						executor.shutdownNow();
					}
				});
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
