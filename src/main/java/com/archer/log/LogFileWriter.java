package com.archer.log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

class LogFileWriter {

	private static final int MAX_KEEP_DAYS = 365;
	private static final String FILE_TAIL = ".log";
	
	private static final String LOG_PATH = "logs";
	private static final String FILE_NAME = "log";
	private static final int KEEP_DAYS = 7;
	
    private final LinkedList<String> logFiles = new LinkedList<>();

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

    private String getLogFileName(LocalDateTime time) {
    	return fileName + "-" +
    			time.format(DateTimeFormatter.ISO_LOCAL_DATE) +
    			FILE_TAIL;
    }

    protected File getLogFile() {
    	return new File(logPath.toString(), getLogFileName(LocalDateTime.now()));
    }

    protected void createNewAndRemoveOld(File log) {
		try {
			Files.createFile(log.toPath());
		} catch (IOException ignore) {}


		LocalDateTime now = LocalDateTime.now();
		logFiles.add(getLogFileName(now));
		if(logFiles.size() == 1) {
			logFiles.add(getLogFileName(now));
			for(int i = 1; i < keepDays; i++) {
				logFiles.add(getLogFileName(now.plusDays(-i)));
			}
		}
		while(logFiles.size() >= keepDays) {
			logFiles.removeLast();
		}
		File[] sysLogFiles = logPath.toFile().listFiles();
		if(sysLogFiles != null) {
			for(File f: sysLogFiles) {
				if(!logFiles.contains(f.getName())) {
					f.delete();
				}
			}
		}
    }
}
