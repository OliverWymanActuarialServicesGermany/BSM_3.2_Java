package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Zeile eines Satzes in der {@link VUHistorie}.
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
public class VUHistorieZeile {

	private final int zeit;
	/** RfB Zuführung B. */
	private final double rfB_Zuführung;
	/** Rohüberschuss C. */
	private final double rohueberschuss;
	/** HGB DRSt inkl. Ansammlungs-guthaben ohne ZZR D. */
	private final double hGB_DRSt_InklAnsammlungsguthaben_ohne_ZZR;

	/**
	 * Erstelle eine Zeile aus einer aufbereiteten csv-Zeile
	 * 
	 * @param zeile
	 *            die Zeile
	 * @throws LineFormatException
	 *             bei csv-Formatfehlern
	 * @throws EmptyLineException
	 *             wenn die Zeile leer ist
	 */
	public VUHistorieZeile(final CsvZeile zeile) throws LineFormatException, EmptyLineException {
		if (zeile.getString(0) == null || zeile.getString(0).isEmpty())
			throw new EmptyLineException();
		zeit = zeile.getInt(0);
		rfB_Zuführung = zeile.getDouble(1);
		rohueberschuss = zeile.getDouble(2);
		hGB_DRSt_InklAnsammlungsguthaben_ohne_ZZR = zeile.getDouble(3);
	}

	/**
	 * Die Zeit. Spalte A.
	 * 
	 * @return der Wert
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Die RfB Zuführung. Spalte B.
	 * 
	 * @return der Wert
	 */
	public double getRfBZufuehrung() {
		return rfB_Zuführung;
	}

	/**
	 * Der Rohüberschuss. Spalte C.
	 * 
	 * @return der Wert
	 */
	public double getRohueberschuss() {
		return rohueberschuss;
	}

	/**
	 * Die HGB DRSt inkl. Ansammlungsguthaben ohne ZZR. Spalte D.
	 * 
	 * @return der Wert
	 */
	public double getHgbDrst() {
		return hGB_DRSt_InklAnsammlungsguthaben_ohne_ZZR;
	}

}
