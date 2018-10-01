package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der Marktwerte.
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
public class MWZeile {

	private final String stressSzenario;
	private final int stressSzenarioId;
	private final String kapitAlanlageKlasse;
	private final String produktkategorie;
	private final double marktwert;

	/**
	 * Erstelle eine Zeile aus der Zeile der csv-Datei.
	 * 
	 * @param zeile
	 *            die Zeile
	 * @throws LineFormatException
	 *             bei Formatfehlern
	 */
	public MWZeile(final CsvZeile zeile) throws LineFormatException {
		stressSzenario = zeile.getString(0);
		stressSzenarioId = zeile.getInt(1);
		kapitAlanlageKlasse = zeile.getString(2);
		produktkategorie = zeile.getString(3);
		marktwert = zeile.getDouble(4);
	}

	/**
	 * Stressszenario. Spalte A.
	 * 
	 * @return der Wert
	 */
	public String getStressSzenario() {
		return stressSzenario;
	}

	/**
	 * Stressszenario ID. Spalte B.
	 * 
	 * @return der Wert
	 */
	public int getStressSzenarioId() {
		return stressSzenarioId;
	}

	/**
	 * Kapitalanlageklasse. Spalte C.
	 * 
	 * @return der Wert
	 */
	public String getKapitalAnlageKlasse() {
		return kapitAlanlageKlasse;
	}

	/**
	 * Produktkategorie. Spalte D.
	 * 
	 * @return der Wert .
	 */
	public String getProduktkategorie() {
		return produktkategorie;
	}

	/**
	 * Marktwert. Spalte E.
	 * 
	 * @return der Wert
	 */
	public double getMarktwert() {
		return marktwert;
	}

}
