package com.project.ai.db;
import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

//Extend this class in all the IMPL class and implement the corresponding interface
//Call getConnetion to access DB
public class DBConnectionManager {

	private static Connection mDatabaseConnection = null;
	
	private void setConnection() throws Exception{

		boolean initialize = SQLiteJDBCLoader.initialize();

		SQLiteDataSource dataSource = new SQLiteDataSource();
		dataSource.setUrl("jdbc:sqlite:C://school/ai/PMPI/database.sqlite/database.sqlite");

		mDatabaseConnection = dataSource.getConnection();
	}

	Connection getConnection() {
		
		if(mDatabaseConnection == null) {
			try {
				setConnection();
			} catch (Exception e) {
				System.out.println("Failed to create connection to Database!");
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
