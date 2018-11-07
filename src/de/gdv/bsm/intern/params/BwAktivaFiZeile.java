package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Dateinzeile der Struktur {@link BwAktivaFi}.
 * 
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zust�ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlie�en, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende R�ckmeldungen w�rde der GDV
 * begr��en. Der GDV �bernimmt aber keine Haftung f�r die fehlerfreie Funktionalit�t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlie�lich aller Tabellen, Grafiken und Erl�uterungen sind urheberrechtlich
 * gesch�tzt. Die ausschlie�lichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell � GDV 2016</b>
 *
 */
public class BwAktivaFiZeile {
	private final String risikoKategorie;
	private final int zeit;
	// MIL_W.Schalesi - Array
	private double[] cashflowFi;
	private double[] ertrag;
	private boolean[] hrESznrRechnen;
	private final int anzahlSznr = 26;
	private final int startSpalte = 5;

	/**
	 * Erstelle eine Datenzeile.
	 * 
	 * @param zeile
	 *            die aufbereitete Zeile der csv-Datei
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Zeile
	 */
	public BwAktivaFiZeile(final CsvZeile zeile, boolean[] hrESznrRechnen) throws LineFormatException {

		// MIL_W.Schalesi
		// Lesen ab der zweiten Zeile
		this.hrESznrRechnen = hrESznrRechnen;

		risikoKategorie = zeile.getString(0);
		zeit = zeile.getInt(1);

		// MIL_W.Schalesi
		cashflowFi = new double[anzahlSznr + 1];
		ertrag = new double[anzahlSznr + 1];
		double c, e;
		for (int sznr = 0; sznr <= anzahlSznr; sznr++) {

			c = zeile.getDouble(sznr * 2 + startSpalte);
			cashflowFi[sznr] = Double.isNaN(c) ? 0.0 : c;

			e = zeile.getDouble(sznr * 2 + startSpalte + 1);
			ertrag[sznr] = Double.isNaN(e) ? 0.0 : e;
		}
	}

	/**
	 * Die Risikokategorie. Spalte A.
	 * 
	 * @return die Kategorie
	 */
	public String getRisikoKategorie() {
		return risikoKategorie;
	}

	/**
	 * Die Zeit dieser Zeile. Spalte B.
	 * 
	 * @return die Zeit
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Der Cashflow. MIL_W.Schalesi
	 * 
	 * @return der Wert
	 */
	public double getCashflowFi(int sznr) {
		if (sznr <= 26) {
			if (isHrERechnen(sznr))
				return cashflowFi[sznr];
			else// Sonst standard cashflow
				return cashflowFi[0];
		} else// Sonst standard cashflow
			return cashflowFi[0];
	}

	/**
	 * Der Ertrag. MIL_W.Schalesi
	 * 
	 * @return der Wert
	 */
	public double getErtrag(int sznr) {
		if (sznr <= 26) {
			if (isHrERechnen(sznr))
				return ertrag[sznr];
			else // Sonst standard cashflow
				return ertrag[0];
		} else// Sonst standard cashflow
			return ertrag[0];
	}

	/**
	 * W.Schalesi: Soll mit stressszenario abhaengigen BW Aktiva gerechnet
	 * werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isHrERechnen(int sznr) {
		if (hrESznrRechnen[sznr])
			return true;
		else
			return false;
	}
}
