package de.gdv.bsm.intern.szenario;

import java.util.List;

/**
 * Daten eines Pfades.
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
public class Pfad {
	private final int pfadNummer;
	private final List<PfadZeile> zeilen;

	/**
	 * Konstruiere einen Pfad aus den Zeilen.
	 * 
	 * @param pfadNummer
	 *            des Pfades
	 * @param zeilen
	 *            des Pfades
	 */
	public Pfad(final int pfadNummer, final List<PfadZeile> zeilen) {
		this.pfadNummer = pfadNummer;
		this.zeilen = zeilen;
	}

	/**
	 * Die Pfadnummer.
	 * 
	 * @return die Zahl
	 */
	public int getPfadNummer() {
		return pfadNummer;
	}

	/**
	 * Die Zeile.
	 * 
	 * @param zeilenNummer
	 *            entspricht der Zeit
	 * @return die Zeile
	 */
	public PfadZeile getPfadZeile(final int zeilenNummer) {
		return zeilen.get(zeilenNummer);
	}
	
	/**
	 * Die zeilenliste
	 * 
	 * @return die Liste
	 */
	public List<PfadZeile> getList() {
		return zeilen;
	}
}
