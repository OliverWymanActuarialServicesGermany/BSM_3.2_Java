package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.vu.module.Functions;

public class DynManReg {
	/**
	 * MIL_W.Schalesi lesen vom Tabellenblatt "Dyn.ManReg"
	 */
	private boolean rWTriggerRechnen = false;
	private boolean uebTrigger_aRechnen = false;
	private boolean maxRefZins = false;
	private boolean p_RohUebTriggerRechnen = false;
	private boolean dynTauTriggerRechnen = false;
	private final Map<Integer, Double> p_RohUebWerte = new HashMap<>();
	private double untereSchranke;
	private double obereSchranke;
	private boolean fiNAmehrereRLZ = false;
	private final Map<Integer, Integer> fiNaRlzStandard = new HashMap<>();
	private final Map<Integer, Double> fiNaAnteilStandard = new HashMap<>();
	private final Map<Integer, Integer> fiNaRlzAlternativ = new HashMap<>();
	private final Map<Integer, Double> fiNaAnteilAlternativ = new HashMap<>();
	private boolean flexUEBRechnen = false;
	private final Map<Integer, Double> flexUEBGrenzen = new HashMap<>();
	private final Map<Integer, Double> flexUEBAnteile = new HashMap<>();
	private boolean p_RohUebTrigger_Op_An_Trigger;
	private double UEB_Trigger_Crisis_Property;
	private double UEB_Trigger_Crisis_Equity;
	private Integer UEB_Laufzeit_Crisis_Interest;
	private double UEB_Trigger_Crisis_Interest;
	private Integer UEB_Dauer_Crisis_Property;
	private Integer UEB_Dauer_Crisis_Equity;
	private Integer UEB_Dauer_Crisis_Interest;
	private Double UEB_Satz_Crisis_Property;
	private Double UEB_Satz_Crisis_Equity;
	private Double UEB_Satz_Crisis_Interest;
	private Boolean Crisis_Property_Stress_PJStart;
	private Boolean Crisis_Equity_Stress_PJStart;
	private Boolean Crisis_Interest_Stress_PJStart;
	private Boolean Crisis_Spread_Stress_PJStart;
	private Boolean Anteil_NG_am_uebrigen_Ergebnis;

	public DynManReg(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// skip header
			csv.readLine();

			CsvZeile zeileZwei = csv.readLine();
			final String rWTriggerString = zeileZwei.getString(1).toUpperCase();
			rWTriggerRechnen = Functions.booleanValueOfString(rWTriggerString);
			p_RohUebWerte.put(zeileZwei.getInt(3), zeileZwei.getDouble(5));

			CsvZeile zeileDrei = csv.readLine();
			final String uebTrigger_aString = zeileDrei.getString(1).toUpperCase();
			uebTrigger_aRechnen = Functions.booleanValueOfString(uebTrigger_aString);
			p_RohUebWerte.put(zeileDrei.getInt(3), zeileDrei.getDouble(5));

			CsvZeile zeileVier = csv.readLine();
			final String uebTrigger_bString = zeileVier.getString(1).toUpperCase();
			maxRefZins = Functions.booleanValueOfString(uebTrigger_bString);
			p_RohUebWerte.put(zeileVier.getInt(3), zeileVier.getDouble(5));

			CsvZeile zeileFuenf = csv.readLine();
			final String p_RohUebString = zeileFuenf.getString(1).toUpperCase();
			p_RohUebTriggerRechnen = Functions.booleanValueOfString(p_RohUebString);
			p_RohUebWerte.put(zeileFuenf.getInt(3), zeileFuenf.getDouble(5));

			CsvZeile zeileSechs = csv.readLine();
			final String dynTauString = zeileSechs.getString(1).toUpperCase();
			dynTauTriggerRechnen = Functions.booleanValueOfString(dynTauString);
			p_RohUebWerte.put(zeileSechs.getInt(3), zeileSechs.getDouble(5));

			CsvZeile zeileSieben = csv.readLine();
			untereSchranke = zeileSieben.getDouble(1);
			p_RohUebWerte.put(zeileSieben.getInt(3), zeileSieben.getDouble(5));

			CsvZeile zeileAcht = csv.readLine();
			obereSchranke = zeileAcht.getDouble(1);
			p_RohUebWerte.put(zeileAcht.getInt(3), zeileAcht.getDouble(5));


			CsvZeile zeileNeun = csv.readLine();
			final String FiNAmehrereRLZString = zeileNeun.getString(1).toUpperCase();
			fiNAmehrereRLZ = Functions.booleanValueOfString(FiNAmehrereRLZString);
			p_RohUebWerte.put(zeileNeun.getInt(3), zeileNeun.getDouble(5));
			
			// Zeile 10 - Zeile 13
			for (int i = 9; i <= 12; i++) {
				CsvZeile zeile = csv.readLine();
				fiNaRlzStandard.put(i-8, zeile.getInt(1));
				fiNaAnteilStandard.put(i-8, zeile.getDouble(2));
				p_RohUebWerte.put(zeile.getInt(3), zeile.getDouble(5));
			}
			
			// Zeile 14 - Zeile 17
			for (int i = 13; i <= 16; i++) {
				CsvZeile zeile = csv.readLine();
				fiNaRlzAlternativ.put(i-12, zeile.getInt(1));
				fiNaAnteilAlternativ.put(i-12, zeile.getDouble(2));
				p_RohUebWerte.put(zeile.getInt(3), zeile.getDouble(5));
			}
			
			CsvZeile zeileAchtzehn = csv.readLine();
			final String flexUEBString = zeileAchtzehn.getString(1).toUpperCase();
			flexUEBRechnen = Functions.booleanValueOfString(flexUEBString);
			p_RohUebWerte.put(zeileAchtzehn.getInt(3), zeileAchtzehn.getDouble(5));
			
			// Zeile 19 - Zeile 22
			for (int i = 18; i <= 21; i++) {
				CsvZeile zeile = csv.readLine();
				flexUEBGrenzen.put(i-17, zeile.getDouble(1));
				flexUEBAnteile.put(i-17, zeile.getDouble(2));
				p_RohUebWerte.put(zeile.getInt(3), zeile.getDouble(5));
			}

			// Zeile 23
			CsvZeile dreiundzwanzig = csv.readLine();
			final String p_RohUebTrigger_Op_An_TriggerString = dreiundzwanzig.getString(1).toUpperCase();
			p_RohUebTrigger_Op_An_Trigger = Functions.booleanValueOfString(p_RohUebTrigger_Op_An_TriggerString);
			p_RohUebWerte.put(dreiundzwanzig.getInt(3), dreiundzwanzig.getDouble(5));
			
			// Zeile 24
			CsvZeile vierundzwanzig = csv.readLine();
			UEB_Trigger_Crisis_Property = vierundzwanzig.getDouble(1);
			p_RohUebWerte.put(vierundzwanzig.getInt(3), vierundzwanzig.getDouble(5));
			
			// Zeile 25
			CsvZeile fünfundzwanzig = csv.readLine();
			UEB_Trigger_Crisis_Equity = fünfundzwanzig.getDouble(1);
			p_RohUebWerte.put(fünfundzwanzig.getInt(3), fünfundzwanzig.getDouble(5));
			
			// Zeile 26
			CsvZeile sechsundzwanzig = csv.readLine();
			UEB_Laufzeit_Crisis_Interest = sechsundzwanzig.getInt(1);
			UEB_Trigger_Crisis_Interest = sechsundzwanzig.getDouble(2);
			p_RohUebWerte.put(sechsundzwanzig.getInt(3), sechsundzwanzig.getDouble(5));
			
			// Zeile 27
			CsvZeile siebenundzwanzig = csv.readLine();
			UEB_Dauer_Crisis_Property = siebenundzwanzig.getInt(1);
			p_RohUebWerte.put(siebenundzwanzig.getInt(3), siebenundzwanzig.getDouble(5));
			
			// Zeile 28
			CsvZeile achtundzwanzig = csv.readLine();
			UEB_Dauer_Crisis_Equity = achtundzwanzig.getInt(1);
			p_RohUebWerte.put(achtundzwanzig.getInt(3), achtundzwanzig.getDouble(5));
						
			// Zeile 29
			CsvZeile neunundzwanzig = csv.readLine();
			UEB_Dauer_Crisis_Interest = neunundzwanzig.getInt(1);
			p_RohUebWerte.put(neunundzwanzig.getInt(3), neunundzwanzig.getDouble(5));
			
			// Zeile 30
			CsvZeile dreissig = csv.readLine();
			UEB_Satz_Crisis_Property = dreissig.getDouble(1);
			p_RohUebWerte.put(dreissig.getInt(3), dreissig.getDouble(5));
			
			// Zeile 31
			CsvZeile einunddreissig = csv.readLine();
			UEB_Satz_Crisis_Equity = einunddreissig.getDouble(1);
			p_RohUebWerte.put(einunddreissig.getInt(3), einunddreissig.getDouble(5));
						
			// Zeile 32
			CsvZeile zweiunddreissig = csv.readLine();
			UEB_Satz_Crisis_Interest = zweiunddreissig.getDouble(1);
			p_RohUebWerte.put(zweiunddreissig.getInt(3), zweiunddreissig.getDouble(5));
			
			// Zeile 33
			CsvZeile dreiunddreissig = csv.readLine();			
			final String Crisis_Property_Stress_PJStartString = dreiunddreissig.getString(1).toUpperCase();
			Crisis_Property_Stress_PJStart = Functions.booleanValueOfString(Crisis_Property_Stress_PJStartString);
			p_RohUebWerte.put(dreiunddreissig.getInt(3), dreiunddreissig.getDouble(5));
			
			// Zeile 34
			CsvZeile vierunddreissig = csv.readLine();
			final String Crisis_Equity_Stress_PJStartString = vierunddreissig.getString(1).toUpperCase();
			Crisis_Equity_Stress_PJStart = Functions.booleanValueOfString(Crisis_Equity_Stress_PJStartString);
			p_RohUebWerte.put(vierunddreissig.getInt(3), vierunddreissig.getDouble(5));
			
			// Zeile 35
			CsvZeile fuenfunddreissig = csv.readLine();
			final String Crisis_Interest_Stress_PJStartString = fuenfunddreissig.getString(1).toUpperCase();
			Crisis_Interest_Stress_PJStart = Functions.booleanValueOfString(Crisis_Interest_Stress_PJStartString);
			p_RohUebWerte.put(fuenfunddreissig.getInt(3), fuenfunddreissig.getDouble(5));
			
			// Zeile 36
			CsvZeile sechsunddreissig = csv.readLine();
			final String Crisis_Spread_Stress_PJStartString = sechsunddreissig.getString(1).toUpperCase();
			Crisis_Spread_Stress_PJStart = Functions.booleanValueOfString(Crisis_Spread_Stress_PJStartString);
			p_RohUebWerte.put(sechsunddreissig.getInt(3), sechsunddreissig.getDouble(5));

			// Zeile 37
			CsvZeile siebenunddreissig = csv.readLine();
			final String Anteil_NG_am_uebrigen_ErgebnisString = siebenunddreissig.getString(1).toUpperCase();
			if (Anteil_NG_am_uebrigen_ErgebnisString.equals("WAHR")) {
				Anteil_NG_am_uebrigen_Ergebnis = true;
			} else {
				Anteil_NG_am_uebrigen_Ergebnis = false;
			}
			
			p_RohUebWerte.put(siebenunddreissig.getInt(3), siebenunddreissig.getDouble(5));
			// Zeile 38 - Zeile 40
			for (int i = 37; i <= 39; i++) {
				CsvZeile zeile = csv.readLine();
				p_RohUebWerte.put(zeile.getInt(3), zeile.getDouble(5));
			}
		}
	}

	/**
	 * Soll der Mindestanteil FI interpoliert werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isRWTriggerRechnen() {
		return rWTriggerRechnen;
	}

	/**
	 * Soll mit Ueberschussbeteiligung Variante a gerechnet werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isUebTrigger_aRechnen() {
		return uebTrigger_aRechnen;
	}

	/**
	 * Soll der Referenzzinssatz mit 0 maximiert werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isMaxRefZinsRechnen() {
		return maxRefZins;
	}

	/**
	 * Soll mit p_RohUeb stressszenario abhaengig gerechnet werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isP_RohUebTriggerRechnen() {
		return p_RohUebTriggerRechnen;
	}

	/**
	 * Soll mit Tau Alternativ gerechnet werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isDynTauTriggerRechnen() {
		return dynTauTriggerRechnen;
	}

	/**
	 * p_Rohueb Werte stressszenario abhaengig
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Double> getp_RohUebWerte() {
		return p_RohUebWerte;
	}

	/**
	 * Untere Schranke fuer RW Trigger
	 * 
	 * @return der Wert
	 */
	public double getUntereSchranke() {
		return untereSchranke;
	}

	/**
	 * Obere Schranke fuer RW Trigger
	 * 
	 * @return der Wert
	 */
	public double getObereSchranke() {
		return obereSchranke;
	}
	
	/**
	 * Soll mit mehreren FI Neuanlage RLZ gerechnet werden
	 * 
	 * @return der Wert
	 */
	public boolean isFiNAmehrereRLZ() {
		return fiNAmehrereRLZ;
	}
	
	/**
	 * Fi NA RLZ für den Standardfall
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Integer> getfiNaRlzStandard() {
		return fiNaRlzStandard;
	}
	
	/**
	 * Fi NA Anteil für den Standardfall
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Double> getfiNaAnteilStandard() {
		return fiNaAnteilStandard;
	}
	
	/**
	 * Fi NA RLZ für den Alternativfall
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Integer> getfiNaRlzAlternativ() {
		return fiNaRlzAlternativ;
	}
	
	/**
	 * Fi NA Anteil für den Alternativfall
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Double> getfiNaAnteilAlternativ() {
		return fiNaAnteilAlternativ;
	}
	
	/**
	 * Soll mit flexibler Überschussbeteiligung gerechnet werden
	 * 
	 * @return der Wert
	 */
	public boolean isflexUEBRechnen() {
		return flexUEBRechnen;
	}
	
	/**
	 * Grenzen für die flexible Überschussbeteiligung
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Double> getflexUEBGrenzen() {
		return flexUEBGrenzen;
	}
	
	/**
	 * Anteile für die flexible Überschussbeteiligung
	 * 
	 * @return die HashMap
	 */
	public Map<Integer, Double> getflexUEBAnteile() {
		return flexUEBAnteile;
	}
	
	/**
	 * Soll die Zusatzoption-Method für die Berechnung der Szenarienabhängigen Überschussätze verwerndet werden
	 * 
	 * @return ja oder nein
	 */
	public boolean isP_RohUebTrigger_Op_An_Trigger() {
		return p_RohUebTrigger_Op_An_Trigger;
	}

	/**
	 * Grenze ab wann ein Immobilienschock angenommen wird
	 * 
	 * @return der Wert
	 */
	public double getUEB_Trigger_Crisis_Property() {
		return UEB_Trigger_Crisis_Property;
	}

	/**
	 * Grenze ab wann ein Aktienschock angenommen wird
	 * 
	 * @return der Wert
	 */
	public double getUEB_Trigger_Crisis_Equity() {
		return UEB_Trigger_Crisis_Equity;
	}

	/**
	 * Restlaufzeit der Spotrate die zur Untersuchung des Zinsschocks herangezogen wird
	 * 
	 * @return der Wert
	 */
	public Integer getUEB_Laufzeit_Crisis_Interest() {
		return UEB_Laufzeit_Crisis_Interest;
	}

	/**
	 * Grenze ab wann ein Zinsschock angenommen wird
	 * 
	 * @return der Wert
	 */
	public double getUEB_Trigger_Crisis_Interest() {
		return UEB_Trigger_Crisis_Interest;
	}
	
	/**
	 * Dauer für die Anwendung der alternativen Überschussbeteiligung im Immobilienschock
	 * 
	 * @return der Wert
	 */
	public Integer getUEB_Dauer_Crisis_Property() {
		return UEB_Dauer_Crisis_Property;
	}
	
	/**
	 * Dauer für die Anwendung der alternativen Überschussbeteiligung im Aktienschock
	 * 
	 * @return der Wert
	 */
	public Integer getUEB_Dauer_Crisis_Equity() {
		return UEB_Dauer_Crisis_Equity;
	}
	
	/**
	 * Dauer für die Anwendung der alternativen Überschussbeteiligung im Zinsschock
	 * 
	 * @return der Wert
	 */
	public Integer getUEB_Dauer_Crisis_Interest() {
		return UEB_Dauer_Crisis_Interest;
	}
	
	/**
	 * Alternativer Überschussbeteiligungssatz im Immobilienschock
	 * 
	 * @return der Wert
	 */
	public Double getUEB_Satz_Crisis_Property() {
		return UEB_Satz_Crisis_Property;
	}
	
	/**
	 * Alternativer Überschussbeteiligungssatz im Aktienschock
	 * 
	 * @return der Wert
	 */
	public Double getUEB_Satz_Crisis_Equity() {
		return UEB_Satz_Crisis_Equity;
	}
	
	/**
	 * Alternativer Überschussbeteiligungssatz im Zinsschock
	 * 
	 * @return der Wert
	 */
	public Double getUEB_Satz_Crisis_Interest() {
		return UEB_Satz_Crisis_Interest;
	}
	
	/**
	 * Soll der Trigger in den ersten Jahren im Immoszenario ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getCrisis_Property_Stress_PJStart() {
		return Crisis_Property_Stress_PJStart;
	}
	
	/**
	 * Soll der Trigger in den ersten Jahren im Aktienszenario ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getCrisis_Equity_Stress_PJStart() {
		return Crisis_Equity_Stress_PJStart;
	}
	
	/**
	 * Soll der Trigger in den ersten Jahren im Zinsdownszenario ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getCrisis_Interest_Stress_PJStart() {
		return Crisis_Interest_Stress_PJStart;
	}
	
	/**
	 * Soll der Trigger in den ersten Jahren im Spreadszenario ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getCrisis_Spread_Stress_PJStart() {
		return Crisis_Spread_Stress_PJStart;
	}

	/**
	 * Soll der Trigger in den ersten Jahren im Spreadszenario ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getAnteil_NG_am_uebrigen_Ergebnis() {
		return Anteil_NG_am_uebrigen_Ergebnis;
	}
}