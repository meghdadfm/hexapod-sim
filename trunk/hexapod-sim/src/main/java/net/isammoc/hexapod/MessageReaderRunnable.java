package net.isammoc.hexapod;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.Map;

import javax.swing.SpinnerNumberModel;

import net.isammoc.hexapod.communication.HexapodMessageInputStream;
import net.isammoc.hexapod.communication.LegMessage;

public class MessageReaderRunnable implements Runnable {
	private final String portName;
	private final Map<HexapodLeg, Map<HexapodArticulation, SpinnerNumberModel>> model;
	private boolean waiting;

	private static final int ACK = 49; // '1' ASCII
	private static final int NACK = 48; // '0' ASCII

	public MessageReaderRunnable(final String portName,
			final Map<HexapodLeg, Map<HexapodArticulation, SpinnerNumberModel>> model) {
		this.portName = portName;
		this.model = model;
	}

	@Override
	public void run() {
		try {
			SerialPort serial;
			final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.portName);
			final CommPort commPort = portIdentifier.open(HexapodFrame.class.getName(), 2000);
			try {
				if (!(commPort instanceof SerialPort)) {
					commPort.close();
					throw new ClassCastException("portName '" + this.portName
							+ "' does not refer to a serial port");
				}

				serial = (SerialPort) commPort;
				serial.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				final HexapodMessageInputStream in = new HexapodMessageInputStream(serial.getInputStream());
				final OutputStream out = serial.getOutputStream();
				while (true) {
					System.out.print("Lecture message...");
					try {
						final LegMessage readMessage = in.readMessage();
						this.waiting = true;
						for (final HexapodLeg leg : HexapodLeg.values()) {
							for (final HexapodArticulation articulation : HexapodArticulation.values()) {
								// TODO
								this.model.get(leg).get(articulation)
										.setValue(readMessage.getUnsignedByte(leg, articulation));
							}
						}
						while (in.read() != -1) {
						}
						synchronized (this) {
							if (this.waiting) {
								System.out.print("Attente de fin d'action...");
								this.wait(5000);
							}
							if (this.waiting) {
								System.out.println("Failed");
								out.write(NACK);
							} else {
								System.out.println("OK");
								out.write(ACK);
							}
							out.flush();
						}
					} catch (final ProtocolException e) {
						System.out.println("Failed");
						out.write(NACK);
					}
				}
			} finally {
				commPort.close();
			}
		} catch (final InterruptedIOException e) {
			// Nothing... only finish thread.
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void notifyHexapodStopped() {
		if (this.waiting) {
			synchronized (this) {
				this.waiting = false;
				this.notifyAll();
			}
		}
	}
}
