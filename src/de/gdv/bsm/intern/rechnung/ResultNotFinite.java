package de.gdv.bsm.intern.rechnung;

/**
 * Fehlersignalisierung für unendliche oder NaN Ergebnisse.
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
@SuppressWarnings("serial")
public class ResultNotFinite extends Error {
	private final String header;
	private final String felder;

	/**
	 * Erstelle eine Fehlermeldung.
	 * 
	 * @param message
	 *            die Meldung insgesamt
	 * @param header
	 *            der Beginn der Meldung
	 * @param felder
	 *            die betroffenen Felder
	 */
	public ResultNotFinite(final String message, final String header, final String felder) {
		super(message);
		this.header = header;
		this.felder = felder;
	}

	/**
	 * Liste der fehlerhaften Felder
	 * 
	 * @return die Felder
	 */
	public String getFelder() {
		return felder;
	}

	/**
	 * Header-Text-
	 * 
	 * @return der Text
	 */
	public String getHeader() {
		return header;
	}
}
