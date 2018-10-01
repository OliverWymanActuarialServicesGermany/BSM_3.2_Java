package de.gdv.bsm.intern.applic;

/**
 * Ein Paar von Objekten.
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
 * @param <A>
 *            Typ des ersten Wertes
 * @param <B>
 *            Typ des zweiten Wertes
 */
public class Pair<A, B> {
	/** Erster Wert des Paares. */
	public final A a;
	/** Zweiter Wert des Paares. */
	public final B b;

	/**
	 * Erstelle ein Paar.
	 * 
	 * @param a
	 *            erster Eintrag
	 * @param b
	 *            zweiter Eintrag
	 */
	public Pair(final A a, final B b) {
		this.a = a;
		this.b = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (a == null ? "null" : a.toString()) + " " + (b == null ? "null" : b.toString());
	}

}
