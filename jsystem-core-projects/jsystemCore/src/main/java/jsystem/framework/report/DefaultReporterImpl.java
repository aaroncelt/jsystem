/*
 * Created on 28/07/2006
 *
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.framework.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsystem.framework.FrameworkOptions;
import jsystem.framework.JSystemProperties;
import jsystem.framework.common.CommonResources;
import jsystem.utils.FileUtils;
import jsystem.utils.StringUtils;
import junit.framework.JSystemJUnit4ClassRunner;
import junit.framework.SystemTest;
import junit.framework.Test;

public abstract class DefaultReporterImpl implements Reporter {
	
	private Logger log = Logger.getLogger(DefaultReporterImpl.class.getName());

	protected boolean failToWarning = false;

	protected boolean failToPass = false;

	protected String date = null;

	protected Test currentTest = null;
	
	protected SystemTest systemTest;
	
	protected boolean printBufferdReportsInRunTime = false;

	/**
	 * Used to buffer reports when startBufferReports is called
	 */
	protected ArrayList<ReportElement> reportsBuffer = null;

	protected boolean buffering = false;

	public void report(String title, String message, boolean status, boolean bold) {
		int stat;
		if (status) {
			stat = Reporter.PASS;
		} else {
			stat = Reporter.FAIL;
		}
		report(title, message, stat, bold, false, false, false);
	}

	public void report(String title, String message, boolean status) {
		report(title, message, status, false);

	}

	public void report(String title, boolean status) {
		report(title, null, status);
	}

	public void report(String title) {
		report(title, true);
	}

	public void step(String stepDescription) {
		report(stepDescription, null, Reporter.PASS, false, false, true, false);
	}

	public void report(String title, Throwable t) {
		report(title, StringUtils.getStackTrace(t), false);
	}

	public void reportHtml(String title, String html, boolean status) {
		int stat;
		if (status) {
			stat = Reporter.PASS;
		} else {
			stat = Reporter.FAIL;
		}

		report(title, html, stat, false, true, false, false);
	}

	public void addLink(String title, String link) {		
		report(title, link, Reporter.PASS, false, false, false, true);
	}

	public void report(String title, int status) {
		report(title, null, status, false, false, false, false);
	}
	
	public void report(String title, String message, int status) {
		report(title, message, status, false, false, false, false);
	}
	
	public void report(String title, String message, int status, boolean bold) {
		report(title, message, status, bold, false, false, false);
	}

	public void report(String title, String message, int status, boolean bold, boolean html, boolean step, boolean link) {
		report(title, message, status, bold, html, step, link, System.currentTimeMillis());
	}
	
	public void report(String title, ReportAttribute attribute){
		report(title,null, Reporter.PASS, attribute);
	}
	
	public void report(String title, String message, ReportAttribute attribute){
		report(title,message, Reporter.PASS, attribute);
	}
	
	public void report(String title, String message, int status, ReportAttribute attribute){
		report(title, message, status, attribute == ReportAttribute.BOLD, attribute == ReportAttribute.HTML, attribute == ReportAttribute.STEP, attribute == ReportAttribute.LINK);
	}

	public boolean isFailToPass() {
		return failToPass;
	}

	public void setFailToPass(boolean failToPass) {
		this.failToPass = failToPass;
	}

	public boolean isFailToWarning() {
		return failToWarning;
	}

	public void setFailToWarning(boolean failToWarning) {
		this.failToWarning = failToWarning;
	}

	public void startReport(String methodName, String parameters) {
		startReport(methodName, parameters, null, null);
	}

	public synchronized void endReport() {
		endReport(null, null);
	}

	public synchronized String getCurrentTestFolder() {
		String logDir = JSystemProperties.getInstance().getPreference(FrameworkOptions.LOG_FOLDER);
		String testDir = getValueFromTmpFile("test.dir.last");
		if (testDir != null) {
			File file = new File(logDir + File.separator + "current" + File.separator + testDir);
			if (!file.exists()) {
				file.mkdirs();
			}
			return logDir + File.separator + "current" + File.separator + testDir;
		}
		return logDir + File.separator + "current";
	}
	
	public String getLastReportFile() {
		try{
			Thread.sleep(200);
		}catch (Exception e) {
			log.log(Level.WARNING,"Thread sleep interrupted",e);
		}
		try {
			Properties p = FileUtils.loadPropertiesFromFile(CommonResources.TEST_INNER_TEMP_FILENAME);
			String value = p.getProperty(CommonResources.LAST_REPORT_NAME);
			return value == null? "" : value;
		} catch (IOException e) {
			log.log(Level.WARNING,"Failed reading last report file name",e);
			return "";
		}
	}
	
	private String getValueFromTmpFile(String key){
		Properties currentProperties = new Properties();
		try {
			currentProperties = FileUtils.loadPropertiesFromFile(CommonResources.TEST_INNER_TEMP_FILENAME);
			String value = currentProperties.getProperty(key);
			return value;
		} catch (Exception e) {
			log.warning("couldn't read the "+CommonResources.TEST_INNER_TEMP_FILENAME+" file");
		}
		return null;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<ReportElement> getReportsBuffer() {
		return reportsBuffer;
	}

	public void clearReportsBuffer() {
		boolean runGC = (reportsBuffer != null);
		reportsBuffer = null;
		if(runGC){
			System.gc();
		}
	}

	public void startBufferingReports() {
		startBufferingReports(printBufferdReportsInRunTime);
	}
	
	public void startBufferingReports(boolean printBufferdReportsInRunTime) {
		this.printBufferdReportsInRunTime = printBufferdReportsInRunTime;
		if (printBufferdReportsInRunTime){
			try {
				startLevel("Reports in buffer", 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		buffering = true;		
	}
	
	public void startLevel(String level) throws IOException{
		startLevel(level, Reporter.CurrentPlace);
	}

	public void stopBufferingReports() {
		buffering = false;
		if (printBufferdReportsInRunTime){
			try {
				stopLevel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void report(ReportElement report) {
		if(report.isStartLevel()){
			try {
				startLevel(report.getTitle(), report.getLevelPlace());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(report.isStopLevel()){
			try {
				stopLevel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			report(report.getTitle(), report.getMessage(), report.getStatus(), report.isBold(), report.isHtml(), report
					.isStep(), report.isLink(), report.getTime());
		}		
	}
	
	protected void updateCurrentTest(Test test){
		currentTest = test;
		systemTest = null;
		if (currentTest instanceof SystemTest){
			systemTest = (SystemTest) currentTest;
		}else if (currentTest instanceof JSystemJUnit4ClassRunner.TestInfo){
			systemTest = ((JSystemJUnit4ClassRunner.TestInfo)currentTest).getSystemTest();
		}
		
	}

}
