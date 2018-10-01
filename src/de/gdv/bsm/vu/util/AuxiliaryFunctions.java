package de.gdv.bsm.vu.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
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
public class AuxiliaryFunctions {

	/**
	 * @param a Liste a
	 * @param b Liste b
	 * @return Liste mit Elementweise Liste a + Liste b, wobei die kürzere Liste für die Länge maßgeblich ist
	 */
	public static List<Double> addListsWithDoubles(List<Double> a, List<Double> b ) {
		return IntStream.range(0, Math.min(a.size(),b.size())).mapToObj(i -> a.get(i)+b.get(i)).collect(Collectors.toList());
	}
	/**
	 * @param a Liste a
	 * @param b Liste b
	 * @return Liste mit Elementweise Liste a * Liste b, wobei die kürzere Liste für die Länge maßgeblich ist
	 */
	public static List<Double> multiplyListsWithDoubles(List<Double> a, List<Double> b ) {
		return IntStream.range(0, Math.min(a.size(),b.size())).mapToObj(i -> a.get(i)*b.get(i)).collect(Collectors.toList());
	}
}
