package com.archer.log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class LogFileWriter {

	private static final int MAX_KEEP_DAYS = 365;
	private static final String FILE_TAIL = ".log";
	
	private static final String LOG_PATH = "logs";
	private static final String FILE_NAME = "log";
	private static final int KEEP_DAYS = 7;

	private int keepDays;
	private Path logPath;
	private String fileName;

    public LogFileWriter(int keepDays, String logPath, String fileName) {
    	if(keepDays <= 0 || keepDays > MAX_KEEP_DAYS) {
    		this.keepDays = KEEP_DAYS;
    	} else {
    		this.keepDays = keepDays;
    	} 
		this.logPath = getLogPath(logPath);
    	if(fileName == null) {
    		this.fileName = FILE_NAME;
    	} else {
    		this.fileName = fileName;
    	}
    }

    public void record(String msg) {
    	byte[] content = (msg+"\n").getBytes(StandardCharsets.UTF_8);
    	LogRecoder.doRecord(this, content);
    }

    private Path getLogPath(String path) {
    	if(path == null) {
    		path = LOG_PATH;
    	}
    	Path dst = Paths.get(path);
    	if(!dst.isAbsolute()) {
    		dst = Paths.get(LogUtil.getCurrentWorkDir(), path);
    	}
    	if(!Files.exists(dst)) {
    		try {
				Files.createDirectories(dst);
			} catch (IOException e) {
				throw new RuntimeException("can not mkdir " + dst.toString());
			}
    	}
		return dst;
    }

    private String getLogFileName() {
    	return fileName + "-" +
    			LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) +
    			FILE_TAIL;
    }

    protected File getLogFile() {
    	File logFile = new File(logPath.toString(), getLogFileName());
    	if(!logFile.exists()) {
    		try {
    			Files.createFile(logFile.toPath());
    		} catch (IOException ex) {
    			System.err.println("can not create file '" + logFile.toString() +
    					"', due to " + ex.getLocalizedMessage());
    			return null;
    		}
    		removeOldLogs();
    	}
    	return logFile;
    }

    protected void removeOldLogs() {
    	File[] allLogFiles = logPath.toFile().listFiles();
    	LocalDate now = LocalDate.now();
    	for(File log: allLogFiles) {
    		String name = log.getName();
    		String timeStr = name.substring(fileName.length() + 1, name.length() - FILE_TAIL.length());
    		LocalDate theTime = LocalDate.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE);
    		if(theTime.plusDays(keepDays).isBefore(now)) {
    			try {
					Files.delete(log.toPath());
				} catch (IOException ignore) {}
    		}
    	}
    }
}
