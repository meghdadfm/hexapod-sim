package net.isammoc.hexapod.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ProtocolException;

public class HexapodMessageInputStream extends InputStream {

	private final InputStream in;

	public HexapodMessageInputStream(final InputStream in) {
		this.in = in;
	}

	@Override
	public int read()
			throws IOException {
		return this.in.read();
	}

	@Override
	public int available()
			throws IOException {
		return this.in.available();
	}

	@Override
	public void close()
			throws IOException {
		this.in.close();
	}

	@Override
	public synchronized void mark(final int readlimit) {
		this.in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return this.in.markSupported();
	}

	@Override
	public int read(final byte[] b)
			throws IOException {
		return this.in.read(b);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
			throws IOException {
		return this.in.read(b, off, len);
	}

	@Override
	public synchronized void reset()
			throws IOException {
		this.in.reset();
	}

	@Override
	public long skip(final long n)
			throws IOException {
		return this.in.skip(n);
	}

	public LegMessage readMessage()
			throws IOException {
		final LegMessage msg = new LegMessage();

		int readDebut;
		do {
			readDebut = this.in.read();
			if (Thread.interrupted()) {
				throw new InterruptedIOException();
			}
		} while (readDebut != 255);
		if (readDebut != 255) {
			throw new ProtocolException();
		}
		byte sum = 0;
		for (int i = 0; i < 21; i++) {
			final byte read = (byte) this.in.read();
			msg.setByte(i, read);
			sum += read;
		}
		final byte realSum = (byte) this.in.read();
		if (realSum != sum) {
			throw new ProtocolException();
		}
		return msg;
	}
}
