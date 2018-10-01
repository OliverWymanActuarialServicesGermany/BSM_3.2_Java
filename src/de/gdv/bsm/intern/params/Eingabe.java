package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.gdv.bsm.intern.applic.Pair;
import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Vorgabeparameter für die Berechnung aus Excel bzw. dem Dialog.
 * 
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
 */
public class Eingabe {
	private boolean alleSzenarien = false;
	private int szenario = 1;
	private int pfadVon = 0;
	private int pfadBis = 0;
	private boolean flvRechnen = false;
	private String pfadSzenariensatz = "";
	private boolean negAusfallwk = false;
	private boolean ausgabe = false;

	private final List<Pair<Integer, String>> szenarienList;

	/**
	 * Erstelle einen default-Parametersatz ohne explizite Eingaben.
	 * 
	 * @param vuParameter
	 *            die eingelesen Daten aus Excel
	 */
	public Eingabe(final VuParameter vuParameter) {
		szenarienList = vuParameter.getSzenarioMapping().getList();
	}

	/**
	 * Erstelle einen Parametersatz aus den exportierten Excel-Daten.
	 * 
	 * @param vuParameter
	 *            bereits gelesene Parameter
	 * @param dataFile
	 *            der Eingabedaten
	 * @throws IOException
	 *             bei IO-Fehlern
	 * @throws LineFormatException
	 *             bei Fehlern in der csv-Datei
	 */
	public Eingabe(final VuParameter vuParameter, final File dataFile) throws IOException, LineFormatException {
		this(vuParameter);
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();

			final String alleSzenarienString = csv.readLine().getString(1);
			if (alleSzenarienString.equals("x") || alleSzenarienString.equals("X")) {
				setAlleSzenarien(true);
			} else {
				setAlleSzenarien(false);
			}

			setSzenario(csv.readLine().getInt(1));
			pfadVon = csv.readLine().getInt(1);
			pfadBis = csv.readLine().getInt(1);

			final String flvString = csv.readLine().getString(1).toUpperCase();
			if (flvString.trim().equals("WAHR") || flvString.trim().equals("TRUE")) {
				flvRechnen = true;
			} else {
				flvRechnen = false;
			}

			pfadSzenariensatz = csv.readLine().getString(1);

			final String nagAusfallwk = csv.readLine().getString(1).toUpperCase();
			if (nagAusfallwk.trim().equals("WAHR") || nagAusfallwk.trim().equals("TRUE")) {
				negAusfallwk = true;
			} else {
				negAusfallwk = false;
			}

			final String ausgabeString = csv.readLine().getString(1).toUpperCase();
			if (ausgabeString.trim().equals("WAHR") || ausgabeString.trim().equals("TRUE")) {
				ausgabe = true;
			} else {
				ausgabe = false;
			}
		}

	}

	/**
	 * Sollen alle Szenarien gerechnet werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isAlleSzenarien() {
		return alleSzenarien;
	}

	/**
	 * Sollen alle Szenarien gerechnet werden?
	 * 
	 * @param alleSzenarien
	 *            ja oder nein
	 */
	public void setAlleSzenarien(boolean alleSzenarien) {
		this.alleSzenarien = alleSzenarien;
	}

	/**
	 * Zu rechnendes Szenario (wenn nicht alle).
	 * 
	 * @return die Szenario-ID
	 */
	public int getSzenario() {
		return szenario;
	}

	/**
	 * Zu rechnendes Szenario (wenn nicht alle).
	 * 
	 * @param szenario
	 *            die Szenario-ID
	 */
	public void setSzenario(int szenario) {
		this.szenario = szenario;
	}

	/**
	 * Erster zu rechnender Pfad.
	 * 
	 * @return der Pfad
	 */
	public int getPfadVon() {
		return pfadVon;
	}

	/**
	 * Setze den ersten zu rechnenden Pfad.
	 * 
	 * @param pfadVon
	 *            der Pfad
	 */
	public void setPfadVon(int pfadVon) {
		this.pfadVon = pfadVon;
	}

	/**
	 * Letzter zu rechnender Pfad.
	 * 
	 * @return der Pfad
	 */
	public int getPfadBis() {
		return pfadBis;
	}

	/**
	 * Setze den letzten zu rechnenden Pfad.
	 * 
	 * @param pfadBis
	 *            der Pfad
	 */
	public void setPfadBis(int pfadBis) {
		this.pfadBis = pfadBis;
	}

	/**
	 * Soll mit FLV gerechnet werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isFlvRechnen() {
		return flvRechnen;
	}

	/**
	 * Setze, ob mit FLV gerechnet werden soll.
	 * 
	 * @param flvRechnen
	 *            ja oder nein
	 */
	public void setFlvRechnen(boolean flvRechnen) {
		this.flvRechnen = flvRechnen;
	}

	/**
	 * Mapping der Szeniarien-IDs auf die Namen der Szenarien.
	 * 
	 * @return die Map
	 */
	public List<Pair<Integer, String>> getSzenarienList() {
		return szenarienList;
	}

	/**
	 * Pfad zu den Szenariensätzen
	 * 
	 * @return den Pfad
	 */
	public String getPfadSzenariensatz() {
		return pfadSzenariensatz;
	}

	/**
	 * Setzt den Pfad zu den Szenariensätzen
	 * 
	 * @param pfadSzenariensatz
	 */
	public void setPfadSzenariensatz(String pfadSzenariensatz) {
		this.pfadSzenariensatz = pfadSzenariensatz;
	}

	/**
	 * Ist eine negative Ausfallwahrscheinlichkeit errlaubt?
	 * 
	 * @return die Antwort
	 */
	public boolean isNegAusfallwk() {
		return negAusfallwk;
	}

	/**
	 * Setzt, ob eine negative AUsfallwahrscheinlichkeit erlaubt ist.
	 * 
	 * @param negAusfallwk
	 */
	public void setNegAusfallwk(boolean negAusfallwk) {
		this.negAusfallwk = negAusfallwk;
	}

	/**
	 * Sollen Zwischenwerte in agg und rzg ausgegeben werden?
	 * 
	 * @return die Ausgabe
	 */
	public boolean isAusgabe() {
		return ausgabe;
	}

}
