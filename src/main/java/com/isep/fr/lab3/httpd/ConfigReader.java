package com.isep.fr.lab3.httpd;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
public class ConfigReader {
	private static Logger log = Logger.getLogger(ConfigReader.class);
	public static final String path = "src/resources/log4j.properties";
	Properties prop = new Properties();
	InputStream input = null;

	private int port;
	private String webRoot;

	public ConfigReader(String config) {
		PropertyConfigurator.configure(path);
		try {
			log.debug(config);

			input = new FileInputStream(config);
			// load a properties file
			prop.load(input);

			// get the property value and log it
			log.debug(prop.getProperty("Port"));
			log.debug(prop.getProperty("WebRoot"));
			setPort((Integer.parseInt(prop.getProperty("Port"))));
			setWebRoot(prop.getProperty("WebRoot"));

		} catch (IOException ex) {
			log.error("Error to laod config file" + ex);
		}
	}

	public String getWebRoot() {
		return webRoot;
	}

	public void setWebRoot(String webRoot) {
		this.webRoot = webRoot;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
