package com.puttysoftware.audio.wav;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

class WAVResource extends WAVPlayer {
	private final URL soundURL;

	public WAVResource(final URL resURL) {
		super();
		this.soundURL = resURL;
	}

	@Override
	public void play() {
		if (this.soundURL != null) {
			try (InputStream inputStream = this.soundURL.openStream()) {
				new Thread() {
					@Override
					public void run() {
						try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream)) {
							final AudioFormat format = audioInputStream.getFormat();
							final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
							try (SourceDataLine auline = (SourceDataLine) AudioSystem.getLine(info)) {
								auline.open(format);
								auline.start();
								int nBytesRead = 0;
								final byte[] abData = new byte[WAVPlayer.EXTERNAL_BUFFER_SIZE];
								try {
									while (nBytesRead != -1) {
										nBytesRead = audioInputStream.read(abData, 0, abData.length);
										if (nBytesRead >= 0) {
											auline.write(abData, 0, nBytesRead);
										}
									}
								} catch (final IOException e) {
									throw new WAVException(e);
								} finally {
									auline.drain();
								}
							} catch (final LineUnavailableException e) {
								throw new WAVException(e);
							}
						} catch (final UnsupportedAudioFileException e) {
							throw new WAVException(e);
						} catch (final IOException e) {
							throw new WAVException(e);
						}
					}
				}.start();
			} catch (final IOException e) {
				throw new WAVException(e);
			}
		}
	}
}
