package de.gdv.bsm.intern.szenario;

/**
 * Lesen von double-Werten für Szenarien.
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
public class Parse {
	static double parseDouble(final String data, final int semikolon) {
		if (semikolon == 1 && data.charAt(0) == '0')
			return 0.0;
		final String s = (semikolon < 0 ? data : data.substring(0, semikolon)).trim();
		if (s.isEmpty())
			return Double.NaN;
		if (s.equals("-"))
			return 0.0;
		return Double.parseDouble(s);
	}

	static double parseDouble2(final StringBuilder buffer, final int start, final int semikolon) {
		if ((semikolon - start) == 1 && (buffer.charAt(start) == '0' || buffer.charAt(start) == '-'))
			return 0.0;
		if (semikolon == start)
			return Double.NaN;
		return Double.parseDouble(buffer.substring(start, semikolon));
	}

}
