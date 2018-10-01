package de.gdv.bsm.vu.util;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.vu.db.BSMDB;

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
public class DiffTool {
	
	private final File pfad1; 
	private final File pfad2;
	private final File pfadAusgabe; 
	private final double precision;
	private final boolean precisionRelative;
	
	/**
	 * @param pfad1
	 * @param pfad2
	 * @param pfadAusgabe
	 * @param precision
	 * @param precisionRelative
	 */
	public DiffTool(File pfad1, File pfad2, File pfadAusgabe, double precision, boolean precisionRelative) {
		this.pfad1= pfad1; 
		this.pfad2=pfad2;
		this.pfadAusgabe= pfadAusgabe; 
		this.precision=precision;
		this.precisionRelative=precisionRelative;
		
		// Setze Log-Datei
		BSMLog.logPath=pfadAusgabe.getPath()+"/"+new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_").format(new Date(System.currentTimeMillis()))+"AbgleichAnalyse.txt";
		// Setze zu verwendende Datenbank
		
		BSMDB db=new  BSMDB("bsmDiffTool",true);
		
		
		List<String> fileNamesPath1 = Stream.of(pfad1.listFiles()).map(x -> x.getName()).collect(Collectors.toList());
		List<String> fileNamesPath2 = Stream.of(pfad2.listFiles()).map(x -> x.getName()).collect(Collectors.toList());
		List<String> filesToCompare= intersection (new ArrayList<String>() {{addAll(fileNamesPath1);}},new ArrayList<String>() {{addAll(fileNamesPath2);}});
		fileNamesPath1.removeAll(filesToCompare);
		fileNamesPath2.removeAll(filesToCompare);
		if (!fileNamesPath1.isEmpty()) {BSMLog.writeLog("Die folgenden Dateien befinden sich nur in " + pfad1.getPath() + ":\n\t"+fileNamesPath1.stream().collect(Collectors.joining(", \n\t")));}
		if(!fileNamesPath2.isEmpty()){BSMLog.writeLog("Die folgenden Dateien befinden sich nur in " + pfad2.getPath() + ":\n\t"+fileNamesPath2.stream().collect(Collectors.joining(", \n\t")));}
		
		
		//Initialise alle Testergebnise als falsch
		boolean testResultsOk [][]=new boolean [filesToCompare.size()][3];
		for (boolean [] row : testResultsOk ) {
			Arrays.fill(row , Boolean.FALSE);
		}
		
		// Abgleich der Dateien, die in beiden Ordnern vorkommen.
		for (String name : filesToCompare) {

			//Abgleich der Headerzeilen
			try {		
				CsvReader read1= new CsvReader(new File(pfad1.getPath()+"/"+ name),';','\'');
				CsvReader read2= new CsvReader(new File(pfad2.getPath()+"/"+ name),';','\'');
				read1.readLine();
				List<String> t1=read1.getTitel();
				read2.readLine();
				List<String> t2=read2.getTitel();
				if(t1.size() !=t2.size()){BSMLog.writeLog("Die Anzahl an Spalten stimmt für die Dateien: "+name + " nicht überein!");}
				else {
					// Erster Test ok
					testResultsOk [filesToCompare.indexOf(name)][0]=true;
				}
				for (int i=0;i<t1.size() ;i++) {
					if(!sameElement(t1,t2,i)) {
						int pos=t2.indexOf(t1.get(i));
						BSMLog.writeLog(name + " Spalte " + (i+1) + " "+t1.get(i)+ " in " + pfad1.getPath() +" ist in "  + pfad2.getPath() +  (pos==-1 ? " nicht vorhanden" : " Spalte "+(pos+1)));	
						// zweiter Test fehlgeschlagen
						testResultsOk [filesToCompare.indexOf(name)][1]=false;
					}
					else {
						// zweiter Test zunächst ok
						if(i==0) {
							testResultsOk [filesToCompare.indexOf(name)][1]=true;
						}
						else {
							// zweiter Test ok, falls nicht vorher schon fehlgeschlagen
							testResultsOk [filesToCompare.indexOf(name)][1]=true & testResultsOk [filesToCompare.indexOf(name)][1];
						}
					}
				}
				for (int i=0;i<t2.size() ;i++) {
					if(!sameElement(t1,t2,i)) {
						int pos=t1.indexOf(t2.get(i));
						BSMLog.writeLog(name + " Spalte " + (i+1) + " "+t2.get(i)+  " in " + pfad2.getPath() +" ist in "  + pfad1.getPath() +  (pos==-1 ? " nicht vorhanden" : " Spalte "+(pos+1)));	
						// zweiter Test fehlgeschlagen
						testResultsOk [filesToCompare.indexOf(name)][1]=false;
					}
					// Zweiter Test ok, wenn nicht schon auf false gesetzt --> kein Bedarf für Code hier
				}


				
				
			}
			catch(LineFormatException err){ BSMLog.writeLog("Datei: "+ name+" Line Format Fehler: "+ err,"0: Warning/Error");}
			catch(IOException err ){BSMLog.writeLog("Datei: "+ name+" IO Fehler Fehler:" + err,"0: Warning/Error");}
		}
			
		

		// TODO: Abgleich der Zeilenanzahl
		// TODO: *.csv Dateien, die in beiden Pfaden vorkommen einlesen z.B. in die Datenbank
		// TODO: Am besten Abgleich SPaltenweise auf Basis gleicher Überschiften unter Berücksichtigung von isNumeric
		// TODO: Überlegen, ob für einige Dateien Abgleich auf Grundlage bekannter Schlüsselspalten erfolgen kann / soll ggf. Überschriften als Parameter übergeben!
		// TODO: Ausgabe: Files_DIFF.csv: Liste der *.csv Dateien (Dateiname, Vorhanden in Pfad 1?, Vorhanden in Pfad2?, Abweichende Überschriften / Reihenfolge im Vergeleich?), die sich in pfad1 oder pfad2 befinden 
		// TODO: Ausgabe nach pfadAusgabe <in beiden Pfaden gefundener Dateiname>_DIFF.csv:
		//		 Alle Alphanumerischen Spalten mit Abweichungen mit Spaltenüberschrift ausgeben in der Form (Wert Pfad1, Wert Pfad 2)
		//		 Alle Numerischen Spalten mit (precisionRelative ? Relativer : Absoluter )  Abweichungen größer als precision ausgeben in der Form  (Wert Pfad1, Wert Pfad 2, Differenz Absolut / Relativ)
		
		
		
	}
	
	private List<String> intersection(List<String> A, List<String> B) {
		A.retainAll(B);
		B.retainAll(A);
		return B;
	}
	private boolean sameElement(List<String> A, List<String> B, int index) {
		try {
			return A.get(index).equals(B.get(index));
		}
		catch(IndexOutOfBoundsException e){
			return false;	
		}
	}

}
