package de.gdv.bsm.vu.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
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
public class AuxiliaryFunctions {

	/**
	 * @param a Liste a
	 * @param b Liste b
	 * @return Liste mit Elementweise Liste a + Liste b, wobei die k�rzere Liste f�r die L�nge ma�geblich ist
	 */
	public static List<Double> addListsWithDoubles(List<Double> a, List<Double> b ) {
		return IntStream.range(0, Math.min(a.size(),b.size())).mapToObj(i -> a.get(i)+b.get(i)).collect(Collectors.toList());
	}
	/**
	 * @param a Liste a
	 * @param b Liste b
	 * @return Liste mit Elementweise Liste a * Liste b, wobei die k�rzere Liste f�r die L�nge ma�geblich ist
	 */
	public static List<Double> multiplyListsWithDoubles(List<Double> a, List<Double> b ) {
		return IntStream.range(0, Math.min(a.size(),b.size())).mapToObj(i -> a.get(i)*b.get(i)).collect(Collectors.toList());
	}
}
