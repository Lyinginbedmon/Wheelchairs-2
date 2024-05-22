package com.lying.wheelchairs.config;

import java.io.FileWriter;
import java.util.Properties;

public class ClientConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private boolean seatbeltDefault = false;
	
	public ClientConfig(String fileIn) { super(fileIn); }
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		seatbeltDefault = parseBoolOr(valuesIn.getProperty("Seatbelt"), false);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeBool(writer, "Seatbelt", seatbeltDefault);
	}
	
	public boolean seatbeltAtBoot() { return seatbeltDefault; }
	
	static
	{
		DEFAULT_SETTINGS.setProperty("Seatbelt", "0");
	}
}
