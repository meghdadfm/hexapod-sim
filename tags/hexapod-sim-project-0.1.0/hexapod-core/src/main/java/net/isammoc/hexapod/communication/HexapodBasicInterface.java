package net.isammoc.hexapod.communication;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import net.isammoc.hexapod.HexapodException;

public class HexapodBasicInterface {

	private SerialPort serial;

	public HexapodBasicInterface(final String portName)
			throws HexapodException {
		try {
			final CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier(portName);
			try {
				final CommPort commPort = portIdentifier.open(this.getClass()
						.getName(), 2000);
				if (!(commPort instanceof SerialPort)) {
					commPort.close();
					throw new ClassCastException("portName '" + portName
							+ "' does not refer to a serial port");
				}

				this.serial = (SerialPort) commPort;
				try {
					this.serial.setSerialPortParams(9600,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} catch (final UnsupportedCommOperationException e) {
					this.serial.close();
					throw new HexapodException("Can not configure port", e);
				}
			} catch (final PortInUseException e) {
				throw new HexapodException("Port '" + portName
						+ "' is already owned by : "
						+ portIdentifier.getCurrentOwner(), e);
			}
		} catch (final NoSuchPortException e) {
			throw new HexapodException("port '" + portName + "' not found", e);
		}
	}

	public void close() {
		if (this.serial != null) {
			this.serial.close();
			this.serial = null;
		}
	}

	public void sendMessage(final IHexapodMessage msg)
			throws HexapodException {
		if (this.serial == null) {
			throw new IllegalArgumentException(
					"Cannot send message to closed port");
		}
		synchronized (this.serial) {
			try {
				this.serial.getOutputStream().write(255);
				byte sum = 0x00;
				for (final byte b : msg.toByteArray()) {
					this.serial.getOutputStream().write(b);
					sum += b;
				}
				this.serial.getOutputStream().write(sum);
			} catch (final IOException e) {
				throw new HexapodException(
						"IO error during send message to the hexapod", e);
			}
		}
	}

	public static int byteToUnsignedByte(final byte signed) {
		return signed & 0xff;
	}

	public static byte unsignedByteToByte(final int unsigned)
			throws IllegalArgumentException {
		if ((unsigned < 0) || (unsigned > 255)) {
			throw new IllegalArgumentException(
					"unsigned must be between 0 and 255 (inclusive)");
		}
		return (byte) unsigned;
	}
}
