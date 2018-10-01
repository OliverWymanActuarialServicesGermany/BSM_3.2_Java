package de.gdv.bsm.vu.db;

import static de.gdv.bsm.vu.module.DiskontFunktion.dfVu;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.rechnung.MittelwerteUndCEZeitschrittig;
import de.gdv.bsm.vu.util.BSMLog;

public class BSMDB {
	private final Connection dbCon;
	
	// default Constructor
	public BSMDB(boolean startWithCleanDatabase) {
		// Load JavaDB 
		this(getDefaultJavaDBConnection(), startWithCleanDatabase);
	}
	
	public BSMDB(String dbName,boolean startWithCleanDatabase) {
		// Load JavaDB 
		this(getDefaultJavaDBConnectionForDBName(dbName), startWithCleanDatabase);
	}
	
	// Constructor with arbitrary database connection
	public BSMDB (Connection dbCon, boolean startWithCleanDatabase){
		this.dbCon=dbCon;
		
		if(startWithCleanDatabase) {
			// Check if tables have to be created	
		    try {
			    DatabaseMetaData metaDataDB = dbCon.getMetaData();
			    ResultSet  tables = metaDataDB.getTables(dbCon.getCatalog(), "CALC", "KENNZAHLEN_ZEITSCHRITTIG", null);
		    	if(!tables.next()){
			        String createCalcKennzahlenZeitschrittig= "CREATE TABLE CALC.KENNZAHLEN_ZEITSCHRITTIG (     "
			        											+" DATA_ID		INTEGER NOT NULL,      			"
												        		+" SZENARIO_ID  INTEGER NOT NULL,      			"
												        		+" SZENARIO_NM	VARCHAR(255) NOT NULL, 			"
												        		+" PFAD 		INTEGER NOT NULL,      			"
												        		+" LOB			VARCHAR(255) NOT NULL, 			"
												        		+" ZEIT			INT NOT NULL,          			"
												        		+" bv   		DOUBLE PRECISION,      			"
												        		+" bj   		DOUBLE PRECISION,      			"
												        		+" bk   		DOUBLE PRECISION,      			"
												        		+" af   		DOUBLE PRECISION,      			"
												        		+" ag   		DOUBLE PRECISION,      			"
												        		+" ak   		DOUBLE PRECISION,       		"
												        		+" aq   		DOUBLE PRECISION,       		"
												        		+" l	   		DOUBLE PRECISION,       		"
												        		+" m	   		DOUBLE PRECISION,       		"
												        		+" aqOrig  		DOUBLE PRECISION,       		"
												        		+" PRIMARY KEY (DATA_ID, SZENARIO_ID, LOB, ZEIT, PFAD)  	"
												        	  +" )                               			    ";
			        dbCon.createStatement().execute(createCalcKennzahlenZeitschrittig);
		    	}
		    	else {    // Clean the database if necessary
		    		String deleteCalcKennzahlenZeitschrittig= "TRUNCATE TABLE CALC.KENNZAHLEN_ZEITSCHRITTIG"; 		
			        dbCon.createStatement().execute(deleteCalcKennzahlenZeitschrittig);		    		
		    	}
		    } catch (SQLException ex) {
		        BSMLog.writeLog("FEHLER: Erstellen / Zugriff / Löschen der Datenbanktabellen nicht möglich.","0: Warning/Error");
		        ex.printStackTrace();
		    }

		}
		
		// Starting from here an existing and set up database is assumed
		// ...	
	}
	
	// Erzeugte für eine Tabelle z.B. CALC.KENNZAHLEN_ZEITSCHRITTIG   eine neue Data_ID
	public int getNewDataID(String table) {
		try{
			PreparedStatement pst = dbCon.prepareStatement(""
					+"SELECT CASE WHEN MAX(DATA_ID) IS NULL THEN 0 ELSE MAX(DATA_ID) END FROM " + table);
			ResultSet rs =pst.executeQuery();
			if(rs.next()){
				return rs.getInt(1)+1;
			}
			else {
				return 1;
			}
		}
		catch(SQLException ex){
	        BSMLog.writeLog("Es konnte keine neue DATA_ID für die Tabelle "+table+" erstellt werden.","0: Warning/Error");
	        ex.printStackTrace();
	        return -1;
		}
	}
	
	public void insertIntoCalcKennzahlenZeitschrittig (int data_id, int SZENARIO_ID, String SZENARIO_NM,int PFAD,String LOB,int ZEIT,double bv,double bj, double bk,double af,double ag,double ak, double aq, double l, double m, double aqOrig){
		try {
			PreparedStatement pst = dbCon.prepareStatement("INSERT INTO CALC.KENNZAHLEN_ZEITSCHRITTIG (DATA_ID,SZENARIO_ID,SZENARIO_NM,PFAD,LOB,ZEIT,bv,bj,bk,af,ag,ak, aq, l, m, aqOrig) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			pst.setInt(2, data_id);
			pst.setInt(2, SZENARIO_ID);
			pst.setString(3, SZENARIO_NM);
			pst.setInt(4, PFAD);
			pst.setString(5, LOB);
			pst.setInt(6, ZEIT);
			pst.setDouble(7, bv);
			pst.setDouble(8, bj);
			pst.setDouble(9, bk);
			pst.setDouble(10, af);
			pst.setDouble(11, ag);
			pst.setDouble(12, ak);
			pst.setDouble(13, aq);
			pst.setDouble(14, l);
			pst.setDouble(15, m);
			pst.setDouble(16, aqOrig);
			pst.executeUpdate();

		}
		catch(SQLException ex){
	        BSMLog.writeLog("FEHLER: Einfügen in CALC.KENNZAHLEN_ZEITSCHRITTIG fehlgeschlagen.","0: Warning/Error");
	        ex.printStackTrace();
		}
	}

	//TODO: Erstelle eine Funktion, die basierend auf den Schlüsseln SZENARIO_ID, LOB, ZEIT und der Info ob pfad 0 berücksichtigt werden soll oder nicht, die Mittelwerte über alle Pfade der einzelnen Datenfelder bestimmt
	//TODO: Statt void sollte Return ein Objekt der klasse MittelwerteZeitschrittig (vgl aktuell Mitttelung) genannte Klasse sein
	private ResultSet getMittelwertZeitschrittig(int DATA_ID, int SZENARIO_ID, String LOB,int ZEIT, boolean includeCEPath){
		try {
			PreparedStatement pst = dbCon.prepareStatement(""
					+"SELECT AVG(bv),AVG(bj),AVG(bk),AVG(af),AVG(ag),AVG(ak)  FROM CALC.KENNZAHLEN_ZEITSCHRITTIG  "
					+"WHERE DATA_ID=? AND SZENARIO_ID=? AND LOB=? AND ZEIT=? AND PFAD >= ?                                                                                     "
					+"GROUP BY DATA_ID,SZENARIO_ID,SZENARIO_NM,LOB,ZEIT                                                                                       " 
					+"ORDER BY SZENARIO_ID ASC, LOB ASC, ZEIT ASC");
			pst.setInt(1, DATA_ID);
			pst.setInt(2, SZENARIO_ID);
			pst.setString(3, LOB);
			pst.setInt(4, ZEIT);
			pst.setInt(5, includeCEPath == true ? 0 : 1 );
			return pst.executeQuery();

		}
		catch(SQLException ex){
	        BSMLog.writeLog("FEHLER: Beim Auslesehn von CALC.KENNZAHLEN_ZEITSCHRITTIG.","0: Warning/Error");
	        ex.printStackTrace();
	        return null;
		}
	}
	
	private ResultSet getCEZeitschrittig(int DATA_ID, int SZENARIO_ID, String LOB,int ZEIT){
		try {
			PreparedStatement pst = dbCon.prepareStatement(""
					+"SELECT SZENARIO_NM, bv, bj, bk, af, ag, ak, aq, l, m, aqOrig  FROM CALC.KENNZAHLEN_ZEITSCHRITTIG  "
					+"WHERE DATA_ID=? AND SZENARIO_ID=? AND LOB=? AND ZEIT=? AND PFAD = 0" 
					+"ORDER BY SZENARIO_ID ASC, LOB ASC, ZEIT ASC");
			pst.setInt(1, DATA_ID);
			pst.setInt(2, SZENARIO_ID);
			pst.setString(3, LOB);
			pst.setInt(4, ZEIT);
			return pst.executeQuery();


		}
		catch(SQLException ex){
	        BSMLog.writeLog("FEHLER: Beim Auslesen von CALC.KENNZAHLEN_ZEITSCHRITTIG.","0: Warning/Error");
	        ex.printStackTrace();
	        return null;
		}
	}
	
	public MittelwerteUndCEZeitschrittig getMittelwerteUndCEZeitschrittig(int DATA_ID,int SZENARIO_ID, String LOB,int ZEIT, boolean includeCEPath){
		
		try {	
			
			ResultSet rs1 = getMittelwertZeitschrittig(DATA_ID,SZENARIO_ID, LOB,ZEIT,includeCEPath);
			ResultSet rs2 = getCEZeitschrittig(DATA_ID,SZENARIO_ID, LOB,ZEIT);
			if(rs1.next() && rs2.next()) {
				return new MittelwerteUndCEZeitschrittig(
						rs2.getString("SZENARIO_NM"),
						SZENARIO_ID, 
						LOB,
						ZEIT,
						new ArrayList<Double>(){
							{
								add(rs1.getDouble(1));
								add(rs1.getDouble(2));
								add(rs1.getDouble(3));
								add(rs1.getDouble(4));
								add(rs1.getDouble(5));
								add(rs1.getDouble(6));
								add(rs2.getDouble("bv"));
								add(rs2.getDouble("bj"));
								add(rs2.getDouble("bk"));
								add(rs2.getDouble("af"));
								add(rs2.getDouble("ag"));
								add(rs2.getDouble("ak"));
								add(rs2.getDouble("aq"));
								add(rs2.getDouble("l"));
								add(rs2.getDouble("m"));
								add(rs2.getDouble("aqOrig"));
							}
						}
					);
			}
			else{
				return null;
			}
		}
		catch(NullPointerException ex){
			BSMLog.writeLog("FEHLER: Mittelwerte und CE zeitschrittig kann nicht erstellt werden.","0: Warning/Error");
	        ex.printStackTrace();
	        return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			BSMLog.writeLog("FEHLER: Mittelwerte und CE zeitschrittig kann nicht erstellt werden.","0: Warning/Error");
			e.printStackTrace();
			return null;
		}
	}
	
	
	// create default javaDB Connection with org.apache.derby.jdbc.EmbeddedDriver. derby.jar is required in the build path. This is part of the jdk subfolder db
	private static Connection getDefaultJavaDBConnection (){
		String url = "jdbc:derby:bsmDB;create=true";
		try {
			return DriverManager.getConnection(url);
		} catch (SQLException ex) {
		    BSMLog.writeLog("FEHLER: Java Datenbanktreiber konnte nicht geladen werden.","0: Warning/Error");
		    ex.printStackTrace();
		    return null;
		}
	}
	private static Connection getDefaultJavaDBConnectionForDBName (String dbName){
		String url = "jdbc:derby:"+dbName+";create=true";
		try {
			return DriverManager.getConnection(url);
		} catch (SQLException ex) {
		    BSMLog.writeLog("FEHLER: Java Datenbanktreiber konnte nicht geladen werden.","0: Warning/Error");
		    ex.printStackTrace();
		    return null;
		}
	}
	
}
