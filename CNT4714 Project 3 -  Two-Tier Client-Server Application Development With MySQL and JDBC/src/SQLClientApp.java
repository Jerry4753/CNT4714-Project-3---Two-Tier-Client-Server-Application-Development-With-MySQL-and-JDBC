import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.mysql.cj.jdbc.MysqlDataSource;

public class SQLClientApp extends JFrame{
	private JButton connectBtn, clearQuery, executeBtn, clearWindow;
	private ResultSetTable tableModel;
	private JLabel queryLabel, userLabel, passwordLabel, statusLabel, windowLabel, dbInfoLabel, jdbcLabel;
	private JTextArea queryArea;
	private JTextField userField;
	private JLabel blankLabel;
	private JPasswordField passwordField;
	private JComboBox <String> propertiesCombo, urlCombo;
	private Connection connection;
	private TableModel empty;
	private JTable resultTable;
	
	// GUI Constructor
	public SQLClientApp(){
	   setName("Project 3 - SQL Client App - CNT4714 - Spring 2022");
	   setSize(1000, 580) ;
	   blankLabel = new JLabel();
	   
	   // Initialize the drop down menus
	   String[] PropertiesItems = {"root.properties", "client.properties", "db3.properties", "db4.properties", "oplog.properties"};
	   String[] URLItems = {"jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC",
			   "jdbc:mysql://localhost:3306/bikedb?useTimezone=true&serverTimezone=UTC",
			   "jdbc:mysql://localhost:3306/test?useTimezone=true&serverTimezone=UTC"};
	   
	   // Construct GUI components
	   queryArea = new JTextArea("");
	   queryArea.setEnabled(false);
	   userField = new JTextField("");
	   passwordField = new JPasswordField("");
	   
	   connectBtn = new JButton("Connect to Database");
	   connectBtn.setFont(new Font("Arial", Font.BOLD, 12));
	   connectBtn.setForeground(Color.yellow);
	   connectBtn.setBackground(Color.blue);
	   
	   clearQuery = new JButton("Clear SQL Command");
	   clearQuery.setFont(new Font("Arial", Font.BOLD, 12));
	   clearQuery.setForeground(Color.red);
	   clearQuery.setBackground(Color.white);
	   clearQuery.setEnabled(false);
	   
	   executeBtn = new JButton("Execute SQL Command");
	   executeBtn.setFont(new Font("Arial", Font.BOLD, 12));
	   executeBtn.setForeground(Color.black);
	   executeBtn.setBackground(Color.green);
	   executeBtn.setEnabled(false);
	   
	   clearWindow = new JButton("Clear Result Window");
	   clearWindow.setFont(new Font("Arial", Font.BOLD, 12));
	   clearWindow.setForeground(Color.black);
	   clearWindow.setBackground(Color.yellow);
	   clearWindow.setEnabled(false);
	   
	   
	   queryLabel = new JLabel("Enter an SQL Command");
	   queryLabel.setFont(new Font("Arial", Font.BOLD, 12));
	   queryLabel.setForeground(Color.blue);
	   
	   dbInfoLabel = new JLabel("Connections Details");
	   dbInfoLabel.setFont(new Font("Arial", Font.BOLD, 12));
	   
	   jdbcLabel = new JLabel("Properties File");
	   jdbcLabel.setOpaque(true);
	   jdbcLabel.setForeground(Color.black);
	   jdbcLabel.setBackground(Color.LIGHT_GRAY);
	   
	   propertiesCombo = new JComboBox();
	   for (int i = 0; i < PropertiesItems.length; i++) {
		   propertiesCombo.addItem(PropertiesItems[i]);
	   }
	   
	   userLabel = new JLabel("Username");
	   userLabel.setForeground(Color.black);
	   userLabel.setBackground(Color.LIGHT_GRAY);
	   userLabel.setOpaque(true);
	   
	   urlCombo = new JComboBox();
	   passwordLabel = new JLabel("Password");
	   passwordLabel.setForeground(Color.black);
	   passwordLabel.setBackground(Color.LIGHT_GRAY);
	   passwordLabel.setOpaque(true);
	   
	   statusLabel = new JLabel("No Connection Now");
	   statusLabel.setForeground(Color.red);
	   statusLabel.setBackground(Color.BLACK);
	   statusLabel.setOpaque(true);

	   windowLabel = new JLabel ("SQL Execution Result Window");
	   windowLabel.setFont(new Font("Arial", Font.BOLD, 12));
	   windowLabel.setForeground(Color.blue);
	   
//	   Box square = Box.createHorizontalBox();
//	   resultTable = new JTable(tableModel);
//	   square.setBackground(Color.WHITE);
//	   square.add(new JScrollPane(resultTable));
	   
	   resultTable = new JTable(tableModel);
	   JScrollPane square = new JScrollPane(resultTable);
	   square.setOpaque(true);
	   square.setBackground(Color.WHITE);
	   
	   // set buttons
	   connectBtn.setBounds(20, 187,165,25);
	   clearQuery.setBounds(470, 150, 165,25);
	   executeBtn.setBounds(680, 150, 170,25);
	   clearWindow.setBounds(45,500,165,25);
	   
	   // set labels, fields
	   userLabel.setBounds(10,78,125,25);
	   passwordLabel.setBounds(10,107,125,24);
	   jdbcLabel.setBounds(10,21,125,25);
	   statusLabel.setBounds(200,187,690,25);
	   windowLabel.setBounds(45,231,220,25);
	   square.setBounds(45,254,841,220);
	   queryLabel.setBounds(450,0,215,25);
	   queryArea.setBounds(450,21,420,120);
	   //square.setBounds(45,254,841,220);
	   //sqlSquare.setBounds(450,22,438,125);
	   propertiesCombo.setBounds(135,21,290,25);
	   urlCombo.setBounds(135,49,290,25);
	   
	   userField.setBounds(135,78,290,25);
	   passwordField.setBounds(135,106,290,25);
	   
	   // add buttons, texts, and labels;
	   add(connectBtn);
	   add(clearQuery);
	   add(executeBtn);
	   
	   add(queryLabel);
	   add(square);
	   add(queryArea);
	   add(dbInfoLabel);
	   add(jdbcLabel);
	   add(propertiesCombo);
	   add(userLabel);
	   add(userField);
	   add(passwordLabel);
	   add(passwordField);
	   add(statusLabel);
	   add(windowLabel);
	   add(clearWindow);
	   add(blankLabel);
	   
	   
	   /***********************************************
	    * Connect Button
	    ***********************************************/
	   connectBtn.addActionListener(new ActionListener() {
		   @SuppressWarnings("deprecation")
		public void actionPerformed(ActionEvent event) {
			   boolean usernameMatch = false;
			   boolean passwordMatch = false;
			   try {
				   // if already connected, close previous connection
				   if(connection != null){
					   connection.close();
				   }
				   statusLabel.setText("No Connection Now");
				   
				   Properties properties = new Properties();
					FileInputStream filein = null;
					MysqlDataSource dataSource = null;
					Connection connectionToOpLog = null;
					// read properties file
					try {
						filein = new FileInputStream((String)propertiesCombo.getSelectedItem());
						properties.load(filein);
						
						// set the parameters
						dataSource = new MysqlDataSource();
						// get url
						dataSource.setUrl(properties.getProperty("MYSQL_DB_URL"));
						
						//match username and password with properties file values
						if(userField.getText().equals((String)properties.getProperty("MYSQL_DB_USERNAME"))){
							usernameMatch = true;
							if(passwordField.getText().equals((String)properties.getProperty("MYSQL_DB_PASSWORD"))){
								passwordMatch = true;
							}
						}
						
						if(passwordMatch && usernameMatch) {
							dataSource.setUser((String)properties.getProperty("MYSQL_DB_USERNAME"));
							dataSource.setPassword((String)properties.getProperty("MYSQL_DB_PASSWORD"));
							
							//establish a connection
							connection = dataSource.getConnection();
							// update status label
							statusLabel.setText("Connected to " + (String)properties.getProperty("MYSQL_DB_URL"));
							clearQuery.setEnabled(true);
							executeBtn.setEnabled(true);
							clearWindow.setEnabled(true);
							queryArea.setEnabled(true);

							
							userField.setEditable(false);
							passwordField.setEditable(false);
							connectBtn.setEnabled(false);
							propertiesCombo.setEnabled(false);
						}
						else {
							statusLabel.setText("NOT CONNECTED - User Credentials Do Not Match Properties File");
						}
					} // end try
					catch (SQLException e) {
						JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
					} // end catch

			   }// end try
				catch(IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
				}// end catch
			   catch (SQLException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
				}// end catch
		   }
	   });
	   
	   /***********************************************
	    * Clear Query Button
	    ***********************************************/
	   clearQuery.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   queryArea.setText("");
		   }
	   });
	   
	   /***********************************************
	    * Clear Result Button
	    ***********************************************/
	   clearWindow.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   empty = new DefaultTableModel();
			   resultTable.setModel(empty);
		   }
	   });
	   
	   /***********************************************
	    * Execute Button
	    ***********************************************/
	   executeBtn.addActionListener(new ActionListener() {
		   public void actionPerformed(ActionEvent event) {
			   try {
				   // activate result table
				   resultTable.setEnabled(true);
				   //scrolling
				   resultTable.setAutoscrolls(true);
				   // connect TableModel for results
				   tableModel = new ResultSetTable(connection, queryArea.getText());
				   
				   // if select command is used, use setQuery method
				   // all other commands will use setUpdate method
				   if(queryArea.getText().toLowerCase().startsWith("select")) {
					   tableModel.setQuery(queryArea.getText());
					   resultTable.setModel(tableModel);
				   }
				   else {
					   tableModel.setUpdate(queryArea.getText());
					   empty = new DefaultTableModel();
					   resultTable.setModel(empty);
				   }
				
				   
			   }catch (SQLException e) { // catch database error
				   JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
			   }catch(ClassNotFoundException NotFound) { // catch driver error
				   JOptionPane.showMessageDialog(null, "MySQL driver not found", "Driver not found", JOptionPane.ERROR_MESSAGE);
			   }
		   }
	   });

   }
   

	public static void main(String[] args) {
		SQLClientApp project = new SQLClientApp();
		project.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		project.setVisible(true);
	}

}

