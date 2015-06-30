package YuxinBookstore;

import java.sql.*;

public class Connector {
	public Connection con;
	public Statement stmt;
	public Connector() throws Exception {
		try{
//		 	String userName = "fudanu33";
//	   		String password = "irmtst89";
//            String url = "jdbc:mysql://10.141.208.26/fudandbd33?useUnicode=true&characterEncoding=utf-8";
			String userName = "fudandb";
			String password = "fudandbpj";
			String url = "jdbc:mysql://chenyux.in/fudandb?useUnicode=true&characterEncoding=utf-8";
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
            con = DriverManager.getConnection (url, userName, password);

			//DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
        	//stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt = con.createStatement();
			//stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch(Exception e) {
			System.err.println("Unable to open mysql jdbc connection. The error is as follows,\n");
			System.err.println(e.getMessage());
			throw(e);
		}

		//System.err.println("Created a new connection");
	}

	public void newStatement() throws Exception {
		try {
			stmt = con.createStatement();
		} catch(Exception e) {
			System.err.println("Unable to create a new statement");
			throw(e);
		}

	}
	
	public void closeConnection() {
		try {
			con.close();
		} catch (Exception e) {

		}
	}
}
