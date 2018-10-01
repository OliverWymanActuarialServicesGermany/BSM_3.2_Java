package de.gdv.bsm.vu.module;

import de.gdv.bsm.vu.berechnung.AggZeile;

/**
 * Funktionen zur Diskontierung der Kennzahlen und Mittelwerte.
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
 *
 */
public class DiskontFunktion {
	/**
	 * Multiplikation mit der Diskontfunktion.
	 * 
	 * @param value
	 *            der zu diskontierende Wert
	 * @param aggZeile
	 *            Zeile mit dem Diskontwert
	 * @return der diskontierte Wert
	 */
	public static double df(double value, AggZeile aggZeile) {
		return Functions.nanZero(value * aggZeile.getDiskontEsg());
	}

	/**
	 * Diskontierung zum VU-Zeitpunkt.
	 * 
	 * @param value
	 *            der zu diskontierende Wert
	 * @param aggZeile
	 *            Zeile mit dem Diskontwert
	 * @param monat
	 *            VU-Zeitpunkt
	 * @return der diskontierte Wert
	 */
	public static double dfVu(double value, AggZeile aggZeile, final double monat) {
		return df(value, aggZeile) * Math.pow(1.0 + Functions.nanZero(aggZeile.getJaehrlZinsEsg()), 1.0 - monat / 12.0);
	}

}
