/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package com.aqua.anttask.jsystem;

import jsystem.utils.StringUtils;

import org.apache.tools.ant.BuildException;

/**
 * A wrapper for the Ant for to allow replacing of reference parameters
 * 
 * @author Nizan Freedman and Itai Agmon
 * 
 */
public class JSystemForTask extends PropertyReaderTask {

	private static final String DEFAULT_LIST_VALUE = "a;b;c;d";
	private static final String DEFAULT_PARAM_VALUE = "myVar";

	public void execute() throws BuildException {

		if (!JSystemAntUtil.doesContainerHaveEnabledTests(getUuid())) {
			return;
		}
		if (!isScenarioCreatedByOlderVersion()) {
			setParametersFromProperties();
		}
		super.execute();
	}

	private void setParametersFromProperties() {
		setList(getParameterFromProperties("list", DEFAULT_LIST_VALUE));
		setParam(getParameterFromProperties("loop value", DEFAULT_PARAM_VALUE));
	}

	/**
	 * Checks if the scenario was created with JSystem in version 6.0.01 or
	 * older. In this case the parameters were saved in the XML file instead of
	 * in the properties file.
	 * 
	 * @return true if and only if the parameters in the XML file is different
	 *         from the default ones.
	 */
	private boolean isScenarioCreatedByOlderVersion() {
		final String list = getList();
		if (!StringUtils.isEmpty(list) && !list.equals(DEFAULT_LIST_VALUE)) {
			return true;
		}

		final String param = getParam();
		if (!StringUtils.isEmpty(param) && !param.equals(DEFAULT_PARAM_VALUE)) {
			return true;
		}
		return false;
	}

}
