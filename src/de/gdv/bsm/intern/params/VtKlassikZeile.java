package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der VT Klassik Daten des Unternehmens. Abbild einer Zeile des Blattes <code>VT Klassik</code>.
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
public class VtKlassikZeile {
	private final String szenario;
	private final int szenarioId;
	private final String lob;
	private final int zeit;
	private final int zinsGeneration;
	private final String altNeuBestand;
	private final double kosten;
	private final double praemien;
	private final double leistungBeiTod;
	private final double kapitalAbfindung;
	private final double sonstigeErlebensfallLeistungen;
	private final double rueckKauf;
	private final double risikoErgebnis;
	private final double uebrigesErgebnis;
	private final double cfEvuRvu;
	private final double zinsratenZuschlag;
	private final double zinsaufwand;
	private final double drDet;
	private final double uebertragFestgelegteInFreieRfB;

	/**
	 * Erzeuge eine Zeile aus einer aufbereiteten Zeile der csv-Datei.
	 * 
	 * @param zeile
	 *            die Zeile der csv-Datei
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 * @throws EmptyLineException
	 *             bei leeren Zeilen
	 */
	public VtKlassikZeile(final CsvZeile zeile) throws LineFormatException, EmptyLineException {
		szenario = zeile.getString(0).trim();
		try {
			szenarioId = zeile.getInt(1);
		} catch (LineFormatException e) {
			if (zeile.getString(1) == null || zeile.getString(1).isEmpty()) {
				throw new EmptyLineException();
			} else {
				throw e;
			}
		}
		lob = zeile.getString(2).trim();
		zeit = zeile.getInt(3);
		zinsGeneration = zeile.getInt(4);
		altNeuBestand = zeile.getString(5);
		kosten = zeile.getDouble(6);
		praemien = zeile.getDouble(7);
		leistungBeiTod = zeile.getDouble(8);
		kapitalAbfindung = zeile.getDouble(9);
		sonstigeErlebensfallLeistungen = zeile.getDouble(10);
		rueckKauf = zeile.getDouble(11);
		risikoErgebnis = zeile.getDouble(12);
		uebrigesErgebnis = zeile.getDouble(13);
		cfEvuRvu = zeile.getDouble(14);
		zinsratenZuschlag = zeile.getDouble(15);
		zinsaufwand = zeile.getDouble(16);
		drDet = zeile.getDouble(17);
		uebertragFestgelegteInFreieRfB=zeile.getDouble(18);
	}

	/**
	 * Der Name des Stressszenarios. Spalte A.
	 * 
	 * @return der Name
	 */
	public String getSzenario() {
		return szenario;
	}

	/**
	 * Die ID des Stressszenarions. Spalte B.
	 * 
	 * @return die ID
	 */
	public int getSzenarioId() {
		return szenarioId;
	}

	/**
	 * Line of Business. Spalte C.
	 * 
	 * @return LoB
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * Nummer des Zeitschrittes. Spalte D.
	 * 
	 * @return die Zeit
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Die Rechnungszinsgeneration. Spalte E.
	 * 
	 * @return der Wert
	 */
	public int getZinsGeneration() {
		return zinsGeneration;
	}

	/**
	 * Alt-/Neubestand. Spalten F.
	 * 
	 * @return a = Altbestand; n = Neubestand
	 */
	public String getAltNeuBestand() {
		return altNeuBestand;
	}

	/**
	 * Kosten des Unternehmens. Spalten G.
	 * 
	 * @return der Wert
	 */
	public double getKosten() {
		return kosten;
	}

	/**
	 * Die Prämien. Spalte H.
	 * 
	 * @return der Wert
	 */
	public double getPraemien() {
		return praemien;
	}

	/**
	 * Leistung beim Tod. Spalte I.
	 * 
	 * @return der Wert
	 */
	public double getLeistungBeiTod() {
		return leistungBeiTod;
	}

	/**
	 * Kapitalabfindungen, nur Rentenversicherung. Spalte J.
	 * 
	 * @return der Wert
	 */
	public double getKapitalAbfindung() {
		return kapitalAbfindung;
	}

	/**
	 * Sonstige Erlebensfallleistungen. Spalte K.
	 * 
	 * @return der Wert
	 */
	public double getSonstigeErlebensfallLeistungen() {
		return sonstigeErlebensfallLeistungen;
	}

	/**
	 * Rückkaufswert. Spalte L.
	 * 
	 * @return der Wert
	 */
	public double getRueckKauf() {
		return rueckKauf;
	}

	/**
	 * Das Risikoergebnis. Spalte M.
	 * 
	 * @return der Wert
	 */
	public double getRisikoErgebnis() {
		return risikoErgebnis;
	}

	/**
	 * Das übrige Ergebnis. Spalte N.
	 * 
	 * @return der Wert
	 */
	public double getUebrigesErgebnis() {
		return uebrigesErgebnis;
	}

	/**
	 * CF EVU -> RVU. Spalte O.
	 * 
	 * @return der Wert
	 */
	public double getCfEvuRvu() {
		return cfEvuRvu;
	}

	/**
	 * Der Zinsratenzuschlag. Spalte P.
	 * 
	 * @return der Wert
	 */
	public double getZinsratenZuschlag() {
		return zinsratenZuschlag;
	}

	/**
	 * Rechnungsmäßiger Zinsaufwand. Spalte Q.
	 * 
	 * @return der Wert
	 */
	public double getZinsaufwand() {
		return zinsaufwand;
	}

	/**
	 * HGB DRSt inkl. Ansammlungsguthaben und festgelegte RfB ohne ZZR, , abzüglich Aktivierte Ansprüche. Spalte R.
	 * 
	 * @return der Wert
	 */
	public double getDrDet() {
		return drDet;
	}
	
	/**
	 *  Übertrag festgelegte RfB (Im BSM als Teil der HGB DRSt repräsentiert) in freie RfB . Spalte S.
	 * 
	 * @return der Wert
	 */
	public double getUebertragFestgelegteInFreieRfB () {
		return uebertragFestgelegteInFreieRfB;
	}
}
