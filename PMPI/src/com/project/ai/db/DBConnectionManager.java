package com.project.ai.db;
import java.sql.Connection;
import java.sql.DriverManager;

//Extend this class in all the IMPL class and implement the corresponding interface
//Call getConnetion to access DB
public class DBConnectionManager {

	private static Connection conn = null;
	
	private void setConnection() throws Exception{

		Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		String url = "";
		String user = "";
		String password = "";
		conn = DriverManager.getConnection(url, user, password);
	}

	protected Connection getConnection() {
		
		if(conn == null) {
			try {
				setConnection();
			} catch (Exception e) {
				System.out.println("Failed to create connection to Database!");
				e.printStackTrace();
			}
			return conn;
		} else {
			return conn;
		}
	}

}
