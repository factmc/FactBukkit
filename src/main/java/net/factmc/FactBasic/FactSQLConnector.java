package net.factmc.FactBasic;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.google.common.io.Files;

public class FactSQLConnector {
	
	public static FactSQLConnector mysql = new FactSQLConnector();
	public static String statsTable, hubTable, cosmeticsTable, achievementsTable, friendsTable;
	
	private Connection connection;
	private String host, database, params, username, password;
	private int port;
	
	public boolean isConnected() {
		return connection != null;
	}
	
	
	public void setup(String host, int port, String database, String params, String username, String password) {
		
		this.host = host;
		this.port = port;
		this.database = database;
		this.params = params;
		this.username = username;
		this.password = password;
		
		statsTable = this.database + ".`" + "statistics" + "`";
		hubTable = this.database + ".`" + "hub" + "`";
		cosmeticsTable = this.database + ".`" + "cosmetics" + "`";
		achievementsTable = this.database + ".`" + "achievements" + "`";
		friendsTable = this.database + ".`" + "friends" + "`";
		
		if (!openConnection()) return;
		
		createDefaultTables();
		
	}
	public void setup(File credentials) {
		
		//FileConfiguration config = Main.getPlugin().getConfig();
		if (!credentials.exists()) {
			System.out.println("[FactSQLConnector] No credentials file found, creating it...");
			try {
				
				credentials.getParentFile().mkdirs();
				credentials.createNewFile();
				try (PrintStream ps = new PrintStream(credentials)) {
					ps.print("localhost,3306,factdata,useSSL=false,username,password");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String[] info = new String[] {"localhost", "3306", "factdata", "useSSL=false", "username", "password"};
		try {
			info = Files.readFirstLine(credentials, Charset.defaultCharset()).split(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			String host = info[0];
			int port = Integer.parseInt(info[1]);
			String database = info[2];
			String params = info[3];
			String username = info[4];
			String password = info[5];
			
			setup(host, port, database, params, username, password);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private boolean openConnection() {
		
		try {
			
			synchronized (this) {
				
				if (getConnection() != null && !getConnection().isClosed()) {
					return false;
				}
				
				Class.forName("com.mysql.jdbc.Driver");
				setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/"
						+ this.database + "?"+this.params, this.username, this.password));
				
				System.out.println("[FactSQLConnector] Successfully Connected to MySQL Database");
				return true;
				
			}
			
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("[FactSQLConnector] Failed to Connect to MySQL Database:");
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public boolean closeConnection() {
		
		try {
			getConnection().close();
			setConnection(null);
			System.out.println("[FactSQLConnector] Successfully Disconnected from MySQL Database");
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	
	
	public static void changePoints(UUID uuid, int amount) {
		
		int points = getPoints(uuid);
		points += amount;
		
		setValue(statsTable, uuid, "POINTS", points);
		
	}
	
	public static void setPoints(UUID uuid, int amount) {
		setValue(statsTable, uuid, "POINTS", amount);
	}
	
	public static int getPoints(UUID uuid) {
		return getIntValue(statsTable, uuid, "POINTS");
	}
	
	
	public static void setValue(String table, UUID uuid, String col, Object value) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("UPDATE " + table + " SET `" + col + "`=? WHERE `UUID`=?");
			statement.setObject(1, value);
			statement.setString(2, uuid.toString());
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String[] getStringValue(String table, UUID uuid, String... cols) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("SELECT * FROM " + table + " WHERE `UUID`=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			results.next();
			
			String[] result = new String[cols.length];
			for (int i = 0; i < cols.length; i++) {
				result[i] = results.getString(cols[i]);
			}
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	public static String getStringValue(String table, UUID uuid, String col) {
		return getStringValue(table, uuid, new String[]{col})[0];
	}
	
	public static boolean[] getBooleanValue(String table, UUID uuid, String... cols) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("SELECT * FROM " + table + " WHERE `UUID`=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			results.next();
			
			boolean[] result = new boolean[cols.length];
			for (int i = 0; i < cols.length; i++) {
				result[i] = results.getBoolean(cols[i]);
			}
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	public static boolean getBooleanValue(String table, UUID uuid, String col) {
		return getBooleanValue(table, uuid, new String[]{col})[0];
	}
	
	public static int[] getIntValue(String table, UUID uuid, String... cols) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("SELECT * FROM " + table + " WHERE `UUID`=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			results.next();
			
			int[] result = new int[cols.length];
			for (int i = 0; i < cols.length; i++) {
				result[i] = results.getInt(cols[i]);
			}
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	public static int getIntValue(String table, UUID uuid, String col) {
		return getIntValue(table, uuid, new String[]{col})[0];
	}
	
	public static long[] getLongValue(String table, UUID uuid, String... cols) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("SELECT * FROM " + table + " WHERE `UUID`=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			results.next();
			
			long[] result = new long[cols.length];
			for (int i = 0; i < cols.length; i++) {
				result[i] = results.getLong(cols[i]);
			}
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	public static long getLongValue(String table, UUID uuid, String col) {
		return getLongValue(table, uuid, new String[]{col})[0];
	}
	
	
	public static UUID getUUID(String username) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("SELECT * FROM " + statsTable + " WHERE `NAME`=?");
			statement.setString(1, username);
			
			ResultSet results = statement.executeQuery();
			if (!results.next()) {
				return null;
			}
			String uuid = results.getString("UUID");
			return UUID.fromString(uuid);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static String getName(UUID uuid) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.mysql.getConnection().prepareStatement("SELECT * FROM " + statsTable + " WHERE `UUID`=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			if (!results.next()) {
				return null;
			}
			String name = results.getString("NAME");
			return name;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	
	
	public void createDefaultTables() {
		
		try {
			if (!getConnection().getMetaData().getTables(null, this.database, "statistics", null).next()) {
				
				PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE " + statsTable + " (" +
						"`UUID` TEXT NULL, `NAME` TEXT NULL, `DISCORD` BIGINT NULL, `POINTS` INT NULL, `PLAYTIME` BIGINT NULL, `FIRSTJOIN` TIMESTAMP NULL, "
						+ "`TOTALVOTES` INT NULL, `PARKOURTIME` INT NULL)");
				statement.executeUpdate();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			if (!getConnection().getMetaData().getTables(null, this.database, "hub", null).next()) {
				
				PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE " + hubTable + " (" +
						"`UUID` TEXT NULL, `HIDEPLAYERS` BIT NULL, `TRAIL` TEXT NULL, `CLOAK` TEXT NULL, `SUIT` TEXT NULL, `SUITCOLOR` TEXT NULL, "
						+ "`SUITHEAD` TEXT NULL, `PET` TEXT NULL, `PETNAME` TEXT NULL, `PETBABY` BIT NULL, `PETAUTOSPAWN` BIT NULL, "
						+ "`PETCOLOR` TEXT NULL, `PETSTYLE` TEXT NULL, `MORPHBABY` BIT NULL, `MORPHVIEWSELF` BIT NULL, `MORPHCOLOR` TEXT NULL)");
				statement.executeUpdate();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			if (!getConnection().getMetaData().getTables(null, this.database, "cosmetics", null).next()) {
				
				PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE " + cosmeticsTable + "(" +
						"`UUID` TEXT NULL, `CATEGORY` TEXT NULL, `NAME` TEXT NULL)");
				statement.executeUpdate();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			if (!getConnection().getMetaData().getTables(null, this.database, "achievements", null).next()) {
				
				PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE " + achievementsTable + "(" +
						"`UUID` TEXT NULL, `SERVER` TEXT NULL, `NAME` TEXT NULL, `PROGRESS` TEXT NULL)");
				statement.executeUpdate();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			if (!getConnection().getMetaData().getTables(null, this.database, "friends", null).next()) {
				
				PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE " + friendsTable + " (" +
						"`UUID` TEXT NULL, `FRIEND` TEXT NULL)");
				statement.executeUpdate();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
}