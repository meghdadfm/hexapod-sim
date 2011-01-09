package net.isammoc.hexapod;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InterruptedIOException;
import java.util.Map;

import javax.swing.SpinnerNumberModel;

import net.isammoc.hexapod.communication.HexapodMessageInputStream;
import net.isammoc.hexapod.communication.LegMessage;

public class MessageReaderRunnable implements Runnable {
	private final String portName;
	private final Map<HexapodLeg, Map<HexapodArticulation, SpinnerNumberModel>> model;

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
				while (true) {
					final LegMessage readMessage = in.readMessage();
					for (final HexapodLeg leg : HexapodLeg.values()) {
						for (final HexapodArticulation articulation : HexapodArticulation.values()) {
							// TODO
							this.model.get(leg).get(articulation)
									.setValue(readMessage.getUnsignedByte(leg, articulation));
						}
					}
				}
			} finally {
				commPort.close();
			}
		} catch (final InterruptedIOException e) {
			// Nothing... only finish thread.
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
