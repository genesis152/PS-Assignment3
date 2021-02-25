package DataAccessLayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class ConnectionBuilder {
	private static final Logger LOGGER = Logger.getLogger(ConnectionBuilder.class.getName());
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String DBURL = "jdbc:mysql://localhost:3306/mydb";
	private static final String USER = "root";
	private static final String PASS = "";
	
	private static ConnectionBuilder instance = new ConnectionBuilder();
	private ConnectionBuilder() {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot find the JDBC driver");
			e.printStackTrace();
		}
	}
	
	/**
	 * creates a connection to the database
	 * @return the connection
	 */
	private Connection createConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DBURL,USER,PASS);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Connection failed!");
			e.printStackTrace();
		}
		return connection;
	}
	/** 
	 * invokes createConnection and returns a connection
	 * @return the connection
	 */
	public static Connection getConnection() {
		return instance.createConnection();
	}
	
	/**
	 * closes a connection
	 * @param connection the connection to be closed
	 */
	public static void close(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not close connection!");
			e.printStackTrace();
		}
	}
	
	/**
	 * closes a statement
	 * @param statement the statement to be closed
	 */
	public static void close(Statement statement) {
		try {
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not close statement!");
			e.printStackTrace();
		}
	}
	/**
	 * closes a result set
	 * @param resultSet the result set to be closed
	 */
	public static void close(ResultSet resultSet) {
		try {
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not close resultSet!");
			e.printStackTrace();
		}	
	}
}
