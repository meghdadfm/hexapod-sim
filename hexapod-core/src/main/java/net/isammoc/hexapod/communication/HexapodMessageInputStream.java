package net.isammoc.hexapod.communication;

import java.io.IOException;
import java.io.InputStream;
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

		System.out.println("Debut lecture");
		int readDebut;
		do {
			readDebut = this.in.read();
		} while (readDebut != 255);
		if (readDebut != 255) {
			System.out.println("Pas 255 au début ??? : " + readDebut);
			throw new ProtocolException();
		}
		System.out.println("Début du message");
		byte sum = 0;
		for (int i = 0; i < 21; i++) {
			final byte read = (byte) this.in.read();
			msg.setByte(i, read);
			sum += read;
		}
		final byte realSum = (byte) this.in.read();
		if (realSum != sum) {
			System.out.println("Pas la bonne somme");
			throw new ProtocolException();
		}
		System.out.println("Message lu");
		return msg;
	}
}
