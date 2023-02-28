import java.io.FileInputStream;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import com.mysql.cj.jdbc.MysqlDataSource;


public class ResultSetTable extends AbstractTableModel{
	private Statement statement;
	private ResultSet resultSet;
	private ResultSetMetaData metaData;
	private int numOfRows;
	
	// keep track of database connection status
	private boolean connectedToDb = false;
	
	public ResultSetTable (Connection c, String query) throws SQLException, ClassNotFoundException{
		// create a statement
		statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					
		// update database connection status
		connectedToDb = true;
		
	}
	
	// get class that represents column type
	public Class getColumnClass (int column) throws IllegalStateException{
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Datase");
		}
		
		// determine Java class of column
		try {
			String className = metaData.getColumnClassName(column + 1);
			
			// return Class object that represents className
			return Class.forName(className);
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
		
		return Object.class;
	} // end method getColumnClass
	

	// return number of rows in ResultSet
	public int getRowCount() throws IllegalStateException{
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Datase");
		}
		
		return numOfRows;
	}// end method getRowCount

	// get number of columns in ResultSet
	public int getColumnCount() throws IllegalStateException{
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Datase");
		}
		
		try {
			return metaData.getColumnCount();
		}
		catch (SQLException sqlException){
			sqlException.printStackTrace();
		}
		return 0;
	}// end method getColumnCount

	 // get name of a particular column in ResultSet
	public String getColumnName(int column) throws IllegalStateException{
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Datase");
		}
		
		// determine the column name
		try {
			return metaData.getColumnName(column + 1);
		}
		catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		return "";
	}// end method getColumnName
	
	// obtain value at particular row and column
	public Object getValueAt(int rowIndex, int columnIndex) throws IllegalStateException{
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Datase");
		}
		
		//obtain a value at specified resultSet row and column
		try {
			resultSet.next(); // fixes a bug in MySQL/Java with date format
			resultSet.absolute(rowIndex + 1);
			return resultSet.getObject(columnIndex + 1);
		}
		catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return ""; // if problem, return empty string object
	}// end method getValueAt
	
	
	// set new database query string
	public void setQuery(String query) throws SQLException, IllegalStateException{
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Datase");
		}
		
		// specify query and execute it
		resultSet = statement.executeQuery(query);
		
		// obtain meta data for resultSet
		metaData = resultSet.getMetaData();
		
		// determine number of rows in ResultSet
		resultSet.last(); // move to the last row
		numOfRows = resultSet.getRow(); // get current row number
		
		// update operations log db as a root user client: +1 to num_queries
		updateOpLog("num_queries");
		
		// notify JTable that model has changed
		fireTableStructureChanged();
	}// end method setQuery
	
	
	// set new database update-query string
	public void setUpdate(String query) throws SQLException, IllegalStateException{
		int res;
		// ensure the database is connected
		if (!connectedToDb) {
			throw new IllegalStateException("Not connected to Database");
		}
		
		// specify query and execute it
		res = statement.executeUpdate(query);
		
		// update operations log db as a root user client: +1 to num_updates
		updateOpLog("num_updates");
		
		// notify JTable that model has changed
		fireTableStructureChanged();
	}// end method setQuery
	
	// connect to operationslog db, update needed parameter and close connection
	public void updateOpLog(String para) {
		// update operations log db as a root user client: +1 to num_updates
		Properties properties = new Properties();
		FileInputStream filein = null;
		MysqlDataSource dataSource = null;
		Connection connectionToOpLog = null;
		// read properties file
		try {
			filein = new FileInputStream("oplog.properties");
			properties.load(filein);
			
			// set the parameters
			dataSource = new MysqlDataSource();
			dataSource.setUrl(properties.getProperty("MYSQL_DB_URL"));
			dataSource.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
			dataSource.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
			
			//establish a connection to the operationslog db
			connectionToOpLog = dataSource.getConnection();
			
			// create a statement
			Statement opLogsStatement = connectionToOpLog.createStatement();
			opLogsStatement.executeUpdate("UPDATE operationscount set " + para + " = "  + para +" + 1");
			
			// close the connection to operationlogs db
			connectionToOpLog.close();
		}
		catch (SQLException sqlException) {
			sqlException.printStackTrace();
			System.exit(1);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	// close statement and connection

}
