package com.isep.fr.lab3.httpd;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main implements Runnable {
	private static final String DEFAULT_FILE = "index.html";
	private static final String FILE_NOT_FOUND = "404.html";
	private static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	private static int PORT;
	private static File WEB_ROOT = null;
	private static String config;
	private static Logger log = Logger.getLogger(Main.class);
	public static final String path = "src/resources/log4j.properties";
	// launcher logger file
	public static Httplog logFile = new Httplog();
	// Client Connection via Socket Class
	private Socket connect;

	public Main(Socket c) {
		connect = c;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// logger
		PropertyConfigurator.configure(path);
		// Check arguments
		if (args.length == 0) {
			log.error("please add the name of config file in arguments");
			System.exit(-1);
		} else {
			config = "src/resources/" + args[0] + ".properties";
		}
		// Config properties loader
		ConfigReader configLoader = new ConfigReader(config);
		WEB_ROOT = new File(configLoader.getWebRoot());
		PORT = configLoader.getPort();

		// launch server socket
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			log.info("Server started.\nListening for connections on port : " + PORT + " ...\n");
			while (true) {
				Main myServer = new Main(serverConnect.accept());
				log.info("Connecton opened. (" + new Date() + ")");
				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}
		} catch (IOException e) {
			log.error("Server Connection error : " + e.getMessage());
		}
	}

	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String fileRequested = null;

		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			String input = in.readLine();

			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			fileRequested = parse.nextToken().toLowerCase();

			if (!method.equals("GET") && !method.equals("HEAD")) {

				log.error("501 Not Implemented : " + method + " method.");

				// return file not supported
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				// read content to return to client
				byte[] fileData = readFileData(file, fileLength);

				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("JavaHttpd / 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line
				out.flush(); // flush character
				logFile.add(this.connect.getInetAddress(), method + fileRequested, 501);

				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();

			} else {
				// GET or HEAD method
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE;
				}

				File file = new File(WEB_ROOT, fileRequested);
				int fileLength = (int) file.length();
				String content = getContentType(fileRequested);
				// Log this
				logFile.add(this.connect.getInetAddress(), method + fileRequested, 200);

				if (method.equals("GET")) {
					byte[] fileData = readFileData(file, fileLength);

					out.println("HTTP/1.1 200 OK");
					out.println("JavaHttpd / 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println();
					out.flush();
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
				log.error("File " + fileRequested + " of type " + content + " returned");

			}
		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				log.error("Error with file not found exception : " + ioe.getMessage());
			}

		} catch (IOException ioe) {
			log.error("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // close socket connection
			} catch (Exception e) {
				log.error("Error closing stream : " + e.getMessage());
			}

			log.info("Connection closed.\n");
		}

	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		out.println("HTTP/1.1 404 File Not Found");
		out.println("JavaHttpd / 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println();
		out.flush(); // flush character
		logFile.add(this.connect.getInetAddress(), "GET" + fileRequested, 404);
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		log.error("File " + fileRequested + " not found");
	}

	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}
}
