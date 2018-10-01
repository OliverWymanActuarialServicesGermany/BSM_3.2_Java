package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der Tabelle <code>Genuss+Nachrang</code>.
 * <p>
 * Leere Zellen in den Spalten Zinsen und Rückzahlung werden als Null interpretiert.
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
public class GenussNachrangZeile {
	private final int zeit;
	private final double zinsen;
	private final double rueckzahlung;

	/**
	 * Erstelle die Daten aus einer aufbereiteten Zeile des csv-Datei.
	 * 
	 * @param zeile
	 *            die Zeile
	 * @throws LineFormatException
	 *             Formatfehler in der Datei
	 */
	public GenussNachrangZeile(final CsvZeile zeile) throws LineFormatException {
		zeit = zeile.getInt(0);
		final double z = zeile.getDouble(1);
		zinsen = Double.isNaN(z) ? 0.0 : z;
		final double r = zeile.getDouble(2);
		rueckzahlung = Double.isNaN(r) ? 0.0 : r;
	}

	/**
	 * Erstelle eine leere Zeile.
	 * 
	 * @param zeit
	 *            die Zeile
	 * @throws LineFormatException
	 *             Formatfehler in der Datei
	 */
	public GenussNachrangZeile(final int zeit) {
		this.zeit = zeit;
		zinsen = 0.0;
		rueckzahlung = 0.0;
	}

	/**
	 * Der Zeitschritt. Spalte A.
	 * 
	 * @return der Wert
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Die Zinsen. Saplte B.
	 * 
	 * @return der Wert
	 */
	public double getZinsen() {
		return zinsen;
	}

	/**
	 * Die Rückzahlung. Spalte C.
	 * 
	 * @return der Wert
	 */
	public double getRueckzahlung() {
		return rueckzahlung;
	}

}
