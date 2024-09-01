package com.lying.client.config;

import java.io.FileWriter;
import java.util.Properties;

import com.lying.config.Config;

public class ClientConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private boolean seatbeltDefault = false;
	private boolean narrateAAC = true;
	
	public ClientConfig(String fileIn) { super(fileIn); }
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		seatbeltDefault = parseBoolOr(valuesIn.getProperty("Seatbelt"), false);
		narrateAAC = parseBoolOr(valuesIn.getProperty("NarrateAAC"), true);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeBool(writer, "Seatbelt", seatbeltDefault);
		writeBool(writer, "NarrateAAC", narrateAAC);
	}
	
	public boolean seatbeltAtBoot() { return seatbeltDefault; }
	
	public boolean shouldNarrateAAC() { return narrateAAC; }
	
	static
	{
		DEFAULT_SETTINGS.setProperty("Seatbelt", "0");
		DEFAULT_SETTINGS.setProperty("NarrateAAC", "1");
	}
}
