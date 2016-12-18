package com.project.ai.db;
import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteDataSource;

//Extend this class in all the IMPL class and implement the corresponding interface
//Call getConnetion to access DB
public class DBConnectionManager {

	private static Connection mDatabaseConnection = null;
	private String dbPath = "";
	private void setConnection() throws Exception{

		SQLiteDataSource dataSource = new SQLiteDataSource();
		dataSource.setUrl("jdbc:sqlite:"+dbPath);

		mDatabaseConnection = dataSource.getConnection();
	}
	
	public void setDatabasePath(String path) {
		dbPath = path;
	}

	protected Connection getConnection() {
		
		if(mDatabaseConnection == null) {
			try {
				setConnection();
			} catch (Exception e) {
				System.out.println("Failed to create connection to Database!");
				System.out.println("Path to database file must be set using command line argument!");
				e.printStackTrace();
			}
			return mDatabaseConnection;
		} else {
			return mDatabaseConnection;
		}
	}
	
	void closeConnection() throws SQLException {
		if(mDatabaseConnection != null) {
			mDatabaseConnection.close();
		}
	}

}
