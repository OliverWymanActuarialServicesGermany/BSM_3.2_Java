package de.gdv.bsm.vu.module;

/**
 * Funktionen des Excel-Moduls <code>T_SonstigeFormeln</code>.
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
public class SonstigeFormeln {

	/**
	 * Berechnet den Barwert des Cashflows CF zum Ende des Jahres T rekursiv. Dabei wird davon ausgegangen, dass die
	 * zukünfftigen Zahlungen in den Jahren T+1,..., omega im Monat "Monat" eingehen.
	 * 
	 * @param zins
	 *            Zins im Folgejahr (T+1) in Basispunkten
	 * @param bwN
	 *            Barwert im Folgejahr (T+1)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            Projektionsende
	 * @param cf
	 *            Cashflow im Folgejahr (T+1)
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return der Wert
	 */
	public static double barwert(final double zins, final double bwN, final int t, final int omega, final double cf,
			final double monat) {
		if (t == omega) {
			return 0.0;
		}
		return cf * Math.pow(1 + Functions.inProzent(zins), -monat / 12.0) + bwN / (1.0 + Functions.inProzent(zins));
	}

}
