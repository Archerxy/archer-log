package com.archer.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * @author xuyi
 */
final class LogRecoder extends Thread {
	
	private static LogRecoder recoder;
	
	private static Buffer buf = new Buffer();
	
	public static void doRecord(LogFileWriter writer, byte[] content) {
		buf.write(content);
		if(recoder == null || !recoder.isAlive()) {
			recoder = new LogRecoder(writer);
			recoder.start();
		}
	}
	
	private LogFileWriter writer;
	
	public LogRecoder(LogFileWriter writer) {
		this.writer = writer;
	}
	
	@Override
	public void run() {
		File log = writer.getLogFile();
		if(!log.exists()) {
			writer.createNewAndRemoveOld(log);
		}
		try {
			Files.write(log.toPath(), buf.read(), StandardOpenOption.APPEND);
		} catch (IOException ignore) {}
	}
	
	static class Buffer {
		byte[] buf;
		int read;
		int write;
		Object lock = new Object();
		
		public Buffer() {
			this.buf = new byte[1024 * 1024];
			this.read = 0;
			this.write = 0;
		}
		
		public byte[] read() {
			synchronized(lock) {
				int len = write - read;
				byte[] ret = Arrays.copyOfRange(buf, read, read + len);
				read = read + len;
				return ret;
			}
		}
		
		public void write(byte[] in) {
			synchronized(lock) { 
				int len = in.length;
				if(buf.length - write + read < len) {
					int newLen = buf.length << 1;
					while(newLen - write + read < len) {
						newLen <<= 1;
					}
					byte[] tmp = new byte[newLen];
					System.arraycopy(buf, read, tmp, 0, write - read);
					buf = tmp;
					write = write - read;
					read = 0;
				} else if(buf.length - write < len) {
					System.arraycopy(buf, read, buf, 0, write - read);
					write = write - read;
					read = 0;
				}
				System.arraycopy(in, 0, buf, write, len);
				write += len;
			}
		}
	}
}
