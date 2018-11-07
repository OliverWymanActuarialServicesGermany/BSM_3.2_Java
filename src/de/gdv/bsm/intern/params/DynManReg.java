package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

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

	public DynManReg(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// skip header
			csv.readLine();

			CsvZeile zeileZwei = csv.readLine();
			final String rWTriggerString = zeileZwei.getString(1).toUpperCase();
			if (rWTriggerString.equals("WAHR")) {
				rWTriggerRechnen = true;
			} else {
				rWTriggerRechnen = false;
			}
			p_RohUebWerte.put(zeileZwei.getInt(3), zeileZwei.getDouble(5));

			CsvZeile zeileDrei = csv.readLine();
			final String uebTrigger_aString = zeileDrei.getString(1).toUpperCase();
			if (uebTrigger_aString.equals("WAHR")) {
				uebTrigger_aRechnen = true;
			} else {
				uebTrigger_aRechnen = false;
			}
			p_RohUebWerte.put(zeileDrei.getInt(3), zeileDrei.getDouble(5));

			CsvZeile zeileVier = csv.readLine();
			final String uebTrigger_bString = zeileVier.getString(1).toUpperCase();
			if (uebTrigger_bString.equals("WAHR")) {
				maxRefZins = true;
			} else {
				maxRefZins = false;
			}
			p_RohUebWerte.put(zeileVier.getInt(3), zeileVier.getDouble(5));

			CsvZeile zeileFuenf = csv.readLine();
			final String p_RohUebString = zeileFuenf.getString(1).toUpperCase();
			if (p_RohUebString.equals("WAHR")) {
				p_RohUebTriggerRechnen = true;
			} else {
				p_RohUebTriggerRechnen = false;
			}
			p_RohUebWerte.put(zeileFuenf.getInt(3), zeileFuenf.getDouble(5));

			CsvZeile zeileSechs = csv.readLine();
			final String dynTauString = zeileSechs.getString(1).toUpperCase();
			if (dynTauString.equals("WAHR")) {
				dynTauTriggerRechnen = true;
			} else {
				dynTauTriggerRechnen = false;
			}
			p_RohUebWerte.put(zeileSechs.getInt(3), zeileSechs.getDouble(5));

			CsvZeile zeileSieben = csv.readLine();
			untereSchranke = zeileSieben.getDouble(1);
			p_RohUebWerte.put(zeileSieben.getInt(3), zeileSieben.getDouble(5));

			CsvZeile zeileAcht = csv.readLine();
			obereSchranke = zeileAcht.getDouble(1);
			p_RohUebWerte.put(zeileAcht.getInt(3), zeileAcht.getDouble(5));

			// Zeile 9 - Zeile 40
			for (int i = 8; i <= 39; i++) {
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

}