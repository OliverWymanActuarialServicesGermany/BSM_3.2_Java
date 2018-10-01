package de.gdv.bsm.vu.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zuständigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschließen, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende Rückmeldungen würde der GDV
 * begrüßen. Der GDV übernimmt aber keine Haftung für die fehlerfreie Funktionalität des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschließlich aller Tabellen, Grafiken und Erläuterungen sind urheberrechtlich
 * geschützt. Die ausschließlichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell © GDV 2016</b>
 *
 */
public class BSMLog {
	/**
	 * Der Pfad zur Log-Datei
	 * 
	 */
	public static String logPath;

	/**
	 * logLevel bestimmt Informationen welchen Levels geloggt werden.
	 * Es werden alle Informationen mit Level niedriger als das Log-Level geloggt 
	 * Standard Log-Level ist: 1: Default
	 * anpassbar auf "0: Warning/Error" für silent mode bzw. "2: Debug", wenn auch Debugging Infos geloggt werden sollen
	 */
	public static String logLevel="1: Default";
	
	
	/**
	 * @param msg die Lognachricht
	 */
	public static void writeLog(Object msg){
		writeLog(msg,"1: Default");
	}
	/**
	 * @param msg die Lognachricht
	 * @param level das Loglevel
	 */
	public static void writeLog(Object msg, String level){

		try {			
			if (Integer.parseInt(level.substring(0,1))<=Integer.parseInt(logLevel.substring(0,1))){
				System.out.println(msg.toString());
				File logFileName = new File(logPath);
				logFileName.getParentFile().mkdirs();
				logFileName.createNewFile();
				String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss: ").format(new Date(System.currentTimeMillis()));
				final PrintStream log = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFileName, true), 8 * 1024));
				log.println(timeStamp+msg.toString());
				log.close();
			}
		}
		catch(Exception e) {
			System.out.println("Logging Exception: " + e);
			// TODO: prüfen, ob System.exit hier immer gewünscht ist
			System.exit(1);
		}
		
	}
	
	/**
	 * 	BSMLog.logHeap(); Will Print current free/total and used/max heap size in GB to the log
	 */
	public static void logHeap(){
		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory(); 
		// Get maximum size of heap in bytes. 
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		 // Get amount of free memory within the heap in bytes. 
		long heapFreeSize = Runtime.getRuntime().freeMemory(); 
		BSMLog.writeLog(Thread.currentThread().getStackTrace()[2]+" Free Heap Size (GB) / Total Heap (GB): " +  heapFreeSize/1E9 + "/" + heapSize/1E9 + ". Used heap (GB) / Max Heap Size: " + (heapSize-heapFreeSize)/1E9 + "/" +  heapMaxSize/1E9);
	}
	
	
	/**
	 * 	Creates a byte Stream of the object an measures its size to estimate the total heap size consumed by the object and included objects in bytes
	 *  The size in MB is written to the log
	 *  make sure the object to be analyzed is serializable
	 *	For analysis purposes only, make sure that this is not used in production code!
	 * @param obj the serializable Object, of which the size has to be determined
	 * @throws IOException 
	 */
	public static void logSerializedObjectSize(Object obj) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(obj);
	    BSMLog.writeLog(Thread.currentThread().getStackTrace()[2]+" Serialisation size: "+baos.toByteArray().length/1E6 + " MB for: "+obj.toString());
	}
	
}
