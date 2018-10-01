package de.gdv.bsm.intern.applic;

/**
 * Fortschritt, der nichts tut (zu Testzwecken).
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
public class DummyFortschritt implements RechenFortschrittInterface {

	@Override
	public void berechnungBeendet(final BerechnungResultat berechnung) {
	}

	@Override
	public void berechnungGechrashed(Throwable reason) {
	}

	@Override
	public void setSznrPercent(int id, int i) {
	}

	@Override
	public void setBerechnungPercent(int id, int lastPercent) {
	}

	@Override
	public boolean isAbbruch() {
		return false;
	}

	@Override
	public void setAusgabePercent(Class<?> klasse, int percent) {
	}

}
