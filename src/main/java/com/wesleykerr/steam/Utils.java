package com.wesleykerr.steam;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	/**
	 * Bolierplate code for sleep
	 * @param millis
	 */
	public static void delay(long millis) { 
		try { 
			Thread.sleep(millis);
		} catch (Exception e) { 
			LOGGER.warn(e.getMessage());
		}
	}
}
