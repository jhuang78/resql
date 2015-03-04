package idv.jhuang.sql4j;

import static com.google.common.base.Preconditions.checkArgument;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sql {
	private static final Logger log = LogManager.getLogger(Sql.class);
	
	private Connection conn;
	public Sql(Connection conn) {
		this.conn = conn;
	}
	
	public void resetDatabase(String name) throws SQLException {
		try(Statement stmt = conn.createStatement()) {
			String sql;
			
			sql = String.format("DROP DATABASE IF EXISTS %s;", name);
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
			
			sql = String.format("CREATE DATABASE %s;", name);
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
			
			sql = String.format("USE %s;", name);
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
		}
	}
	
	public void createTable(String table, String column, String type, boolean pk, boolean auto) throws SQLException {
		checkArgument(!(auto && !pk), "A column cannot be auto-generated but not primary key");
		
		try(Statement stmt = conn.createStatement()) {
			
			String sql;
			
			if(auto) {
				sql = String.format("CREATE TABLE %s(%s %s AUTO_INCREMENT, PRIMARY KEY(%s))", table, column, type, column);
			} else if(pk) {
				sql = String.format("CREATE TABLE %s(%s %s, PRIMARY KEY(%s));", table, column, type, column);
			} else {
				sql = String.format("CREATE TABLE %s(%s %s);", table, column, type);
			}
					
					
			
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
		}
	}
	
	public void alterTableAdd(String table, String column, String type, boolean unique) throws SQLException {
		try(Statement stmt = conn.createStatement()) {
			String sql;
			
			sql = String.format("ALTER TABLE %s ADD %s %s;", table, column, type);
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
			
			if(unique) {
				sql = String.format("ALTER TABLE %s ADD UNIQUE(%s);", table, column);
				log.debug("SQL> {}", sql);
				stmt.execute(sql);
			}
		}
	}
	
	public void alterTableAdd(String table, String fkName, String fkType, String refTable, String refName, boolean unique) throws SQLException {
		try(Statement stmt = conn.createStatement()) {
			String sql;
			
			sql = String.format("ALTER TABLE %s ADD %s %s;", table, fkName, fkType);
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
			
			sql = String.format("ALTER TABLE %s ADD FOREIGN KEY(%s) REFERENCES %s(%s);",
					table, fkName, refTable, refName);
			log.debug("SQL> {}", sql);
			stmt.execute(sql);
			
			if(unique) {
				sql = String.format("ALTER TABLE %s ADD UNIQUE(%s);", table, fkName);
				log.debug("SQL> {}", sql);
				stmt.execute(sql);
			}
			
		}
	}
	
	public int insertInto(String table, List<String> columns, List<String> types, List<Object> values) throws SQLException {
		String sql = String.format("INSERT INTO %s(%s) VALUES (%s);", table, 
				String.join(", ", columns),
				String.join(", ", Collections.nCopies(columns.size(), "?")));
		
		int idx = -1;
		try(PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			for(int i = 0; i < columns.size(); i++) {
				setParameter(pstmt, i + 1, values.get(i), types.get(i));
			}
			log.debug("SQL> {}", sql);
			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();
			if(rs.next())
				idx = rs.getInt(1);
		}
		
		return idx;
	}
	
	private void setParameter(PreparedStatement pstmt, int idx, Object value, String type) throws SQLException {
		if(type.startsWith("ENUM("))
			type = "ENUM";
		
		switch(type) {
		case "INTEGER":
			if(value == null)
				pstmt.setNull(idx, Types.INTEGER);
			else
				pstmt.setInt(idx, (Integer)value);
			break;
		case "ENUM":
		case "VARCHAR(255)":
			if(value == null)
				pstmt.setNull(idx, Types.VARCHAR);
			else
				pstmt.setString(idx, (String)value);
			break;
		case "DOUBLE":
			if(value == null)
				pstmt.setNull(idx, Types.DOUBLE);
			else
				pstmt.setDouble(idx, (Double)value);
			break;
		case "BOOLEAN":
			if(value == null)
				pstmt.setNull(idx, Types.BOOLEAN);
			else
				pstmt.setBoolean(idx, (Boolean)value);
			break;
		case "DATE":
			if(value == null)
				pstmt.setNull(idx, Types.DATE);
			else
				pstmt.setDate(idx, Date.valueOf((String)value));
			break;
		default:
			throw new IllegalArgumentException("Unsuppoted SQL type: " + type + ".");
		}
	}
	
	
}
