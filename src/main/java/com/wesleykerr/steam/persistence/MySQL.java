package com.wesleykerr.steam.persistence;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQL {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQL.class);

    // TODO: think about connection pooling and how we can inject
    // the implementation into this class.
    private Connection conn;

    // TODO: if we are doing proper enterprise development
    // these would go into a DAO and the implementation of the
    // DAO would know about the connection.

    private String host;
    private int port;

    private String db;

    private String username;
    private String password;

    public MySQL() { 

    }

    public Connection getConnection() { 
        return conn;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 
     * @return
     */
    public String getDb() { 
        return db;
    }

    /**
     * 
     * @param db
     */
    public void setDb(String db) { 
        this.db = db;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Connect to the database.
     * @throws RuntimeException if connection fails.
     */
    public void connect() { 
        try { 
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) { 
            throw new RuntimeException(e.getMessage());
        }

        try { 
            String connStr = "jdbc:mysql://" + host + ":" + port + "/" + db;
            conn = DriverManager.getConnection(connStr, username, password);
        } catch (SQLException e) { 
            throw new RuntimeException("Connection Failed! " + e.getMessage());
        }

        // now we are going to prepare some statements
        // so that they are ready for use and will make the queries
        // that much quicker.

    }

    public void disconnect() { 
        try { 
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Stream a result set to disk without 
     * using up all of the machines resources.
     * @param query
     * @param out
     * @throws Exception
     */
    public void streamResultSet(String query, Writer out) throws Exception { 
    	try (Statement stmt = conn.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)) { 
        	stmt.setFetchSize(Integer.MIN_VALUE);
        	
        	int count = 0;
        	try (ResultSet rs = stmt.executeQuery(query)) { 
            	LOGGER.info("...0 records processed");
            	
            	long accumResultSet = 0;
            	long accumFileIO = 0;
        		while (rs.next()) { 
        			long start = System.nanoTime();
        			String s = rs.getString(1);
        			accumResultSet += (System.nanoTime() - start);

        			start = System.nanoTime();
        			out.write(s);
        			out.write("\n");
        			accumFileIO += (System.nanoTime() - start);
        			
        			++count;
        			if (count % 10000 == 0) {
        				LOGGER.info("..." + count + " records processed");
        				LOGGER.info("..... " + accumResultSet + " ... " + accumFileIO);
        			}
        		}
            	LOGGER.info("..." + count + " records processed");
        	}
    	}
    }

    public static MySQL getLocalhost() { 
        MySQL mySQL = new MySQL();
        mySQL.setHost("localhost");
        mySQL.setPort(3306);
        mySQL.setDb("game_recommender");
        mySQL.setUsername("root");
        mySQL.connect();
        return mySQL;
    }

    public static MySQL getDreamhost() { 
    	return getDatabase("config/mysql.properties");
    }
    
    public static MySQL getDatabase(String configFile) { 
        Properties prop = null;
        try {
            prop = new Properties();
            InputStream input = new FileInputStream(configFile);
            prop.load(input);
        } catch (Exception e) {
            LOGGER.error("Unable to load: " + configFile);
            throw new RuntimeException(e);
        }
        
        return getDatabase(prop);
    }
    
    public static MySQL getDatabase(Properties prop) {
        try { 
            MySQL mySQL = new MySQL();
            mySQL.setHost(prop.getProperty("host"));
            mySQL.setPort(Integer.parseInt(prop.getProperty("port")));
            mySQL.setDb(prop.getProperty("db"));
            mySQL.setUsername(prop.getProperty("username"));
            mySQL.setPassword(prop.getProperty("password"));
            mySQL.connect();
            return mySQL;
        } catch (Exception e) { 
            e.printStackTrace();
            LOGGER.error("Unable to load the database");
            LOGGER.error("host: " + prop.getProperty("host"));
            LOGGER.error("port: " + prop.getProperty("port"));
            LOGGER.error("db: " + prop.getProperty("db"));
            LOGGER.error("username: " + prop.getProperty("username"));
            LOGGER.error("password: " + prop.getProperty("password"));
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws Exception { 
    }
}
