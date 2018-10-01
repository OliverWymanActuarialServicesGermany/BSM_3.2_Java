package de.gdv.bsm.vu.module;

/**
 * Funktionen des Moduls <code>Esg</code>.
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
public class EsgFormeln {

	/**
	 * Berechnet den mittleren Zins. <br/>
	 * Funktionsname in Excel: jaehrl_Zins_ESG.
	 * 
	 * @param vgDiskontEsg
	 *            Diskontfunktion (Vorgänger)
	 * @param diskontEsg
	 *            Diskontfunktion
	 * @return mittleren Zins
	 */
	public static double jaehrlZinsEsg(final double vgDiskontEsg, final double diskontEsg) {
		return vgDiskontEsg / diskontEsg - 1.0;
	}

}
