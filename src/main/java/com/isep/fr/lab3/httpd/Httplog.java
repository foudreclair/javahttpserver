package com.isep.fr.lab3.httpd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Date;

public class Httplog {

	private FileOutputStream fileStream;
	private static String pathFile = "file.txt";
	private File fichierLog = null;

	public void httpLog(String pathFile) throws IOException {
		// Initiate file log !
		try(PrintWriter output = new PrintWriter(new FileWriter(pathFile,true))) 
		{
		    output.printf("%s\r\n", "Start");
		} 
		catch (Exception e) {}
	}

	public void add(InetAddress inetAddress, String request, int status) {
		String write = inetAddress + " " + new Date() + " " + request + " " + status;
		try(PrintWriter output = new PrintWriter(new FileWriter(pathFile,true))) 
		{
		    output.printf("%s\r\n", write);
		} 
		catch (Exception e) {}
	}
}
