package com.wesleykerr.steam.persistence;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        try { 
            Properties prop = new Properties();
            InputStream input = new FileInputStream("config/mysql.properties");
            prop.load(input);

            MySQL mySQL = new MySQL();
            mySQL.setHost(prop.getProperty("host"));
            mySQL.setPort(Integer.parseInt(prop.getProperty("port")));
            mySQL.setDb(prop.getProperty("db"));
            mySQL.setUsername(prop.getProperty("username"));
            mySQL.setPassword(prop.getProperty("password"));
            mySQL.connect();
            return mySQL;
        } catch (Exception e) { 
            LOGGER.error("Unable to load the database");
            throw new RuntimeException(e);
        }
    }
}
