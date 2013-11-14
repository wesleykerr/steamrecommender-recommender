package com.wesleykerr.steam.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wesleykerr.steam.persistence.dao.CounterDAO;

public class CounterDAOImpl implements CounterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CounterDAOImpl.class);

	private Connection conn;

	private PreparedStatement insert;
	
	/**
	 * Instanciate a new CounterDAO object and give it
	 * the database connection necessary.
	 * @param conn
	 */
	public CounterDAOImpl(Connection conn) {
		this.conn = conn;
		
		try { 
			insert = conn.prepareStatement(INSERT);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Finished up with this DAO so close out any open statements 
	 */
	public void finish() { 
		try { 
			insert.close();
		} catch (SQLException e) { 
			LOGGER.error("Error closing prepared statement: " + e.getMessage());
		}
	}
	
	/**
	 * Set the counter to the initial value
	 * @param value
	 */
	public void setCounter(int value) { 
		try { 
			insert.setInt(1, value);
			insert.executeUpdate();
		} catch (SQLException e) { 
			throw new RuntimeException(e.getMessage());
		}
	}

	public int getCounter() {
		Statement s = null;
		ResultSet rs = null;
		try { 
			s = conn.createStatement();
			rs = s.executeQuery(SELECT);
			
			if (rs.next()) { 
				return rs.getInt(1);
			} else { 
				setCounter(0);
				return 0;
			}
		} catch (SQLException e) { 
			throw new RuntimeException(e.getMessage());
		} finally {
			if (s != null) { 
				try { 
					s.close();
				} catch (SQLException e) {
					LOGGER.error("ERROR - " + e.getMessage());
				}
			}
			
			if (rs != null) { 
				try { 
					rs.close();
				} catch (SQLException e) { 
					LOGGER.error("ERROR - " + e.getMessage());
				}
			}
		}
	}

	public int incrCounter() {
		Statement s = null;
		try { 
			s = conn.createStatement();
			s.executeUpdate(UPDATE);
		} catch (Exception e) { 
			throw new RuntimeException(e.getMessage());
		}
		
		return getCounter();
	}
	
	public void reset() { 
	    setCounter(0);
	}

	private static final String UPDATE = 
			"update game_recommender.counter set counter = counter+1";
	private static final String INSERT = 
			"insert into game_recommender.counter (`counter`) values (?)";
	private static final String SELECT = 
			"select counter from game_recommender.counter";
	
}
