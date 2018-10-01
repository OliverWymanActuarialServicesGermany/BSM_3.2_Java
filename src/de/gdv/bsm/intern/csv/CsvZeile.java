package de.gdv.bsm.intern.csv;

import java.util.List;

/**
 * Modell einer Zeile einer csv-Datei. Die Zugriffe
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
public class CsvZeile {
	// Zeilennummer dieser Zeile
	private final int zeilenNummer;
	// Titelzeile einer csv-Datei
	private final List<String> titelZeile;
	// Die Spalten einer Zeile als String
	private final List<String> zeile;

	CsvZeile(final int zeilenNummer, final List<String> titelZeile, final List<String> zeile) {
		this.zeilenNummer = zeilenNummer;
		this.titelZeile = titelZeile;
		this.zeile = zeile;
	}

	/**
	 * Gebe den Inhalt den gewünschten Spalte als Integer zurück.
	 * 
	 * @param spalte
	 *            die Spalte zerobased
	 * @return die Zahl
	 * @throws LineFormatException
	 *             falls das Feld keine ganze Zahl enthält
	 */
	public int getInt(final int spalte) throws LineFormatException {
		checkExists(spalte);
		try {
			return Integer.parseInt(zeile.get(spalte));
		} catch (NumberFormatException e) {
			final String feldName = getfeldName(spalte);
			throw new LineFormatException(
					"Das Feld " + feldName + " ist keine ganze Zahl sondern " + zeile.get(spalte));
		}
	}

	/**
	 * Gebe den Inhalt den gewünschten Spalte als Double zurück.
	 * 
	 * @param spalte
	 *            die Spalte zerobased
	 * @return die Zahl; oder 0.0, wenn die Zeile leer ist
	 * @throws LineFormatException
	 *             falls das Feld keine Fließpunktzahl enthält
	 */
	public double getDouble(final int spalte) throws LineFormatException {
		checkExists(spalte);
		try {
			if (zeile.get(spalte) == null || zeile.get(spalte).isEmpty()) {
				// es wird 0 zurückgeliefert, und nicht mehr NaN:
				return 0.0;
			}
			return Double.parseDouble(zeile.get(spalte).replace(',', '.'));
		} catch (NumberFormatException e) {
			final String feldName = getfeldName(spalte);
			throw new LineFormatException(
					"Das Feld " + feldName + " ist keine Fließpunktzahl sondern " + zeile.get(spalte));
		}
	}
	
	/**
	 * Gebe den Inhalt den gewünschten Spalte als Double zurück, 
	 * es sei denn die Spalte ist leer, dann gebe NaN zurück.
	 * 
	 * @param spalte
	 *            die Spalte zerobased
	 * @return die Zahl; oder NaN, wenn die Zelle leer ist
	 * @throws LineFormatException
	 *             falls das Feld keine Fließpunktzahl enthält
	 */
	public double getDoubleOrNaN(final int spalte) throws LineFormatException {
		checkExists(spalte);
		try {
			if (zeile.get(spalte) == null || zeile.get(spalte).isEmpty()) {
				// es wird NaN zurückgeliefert:
				return Double.NaN;
			}
			return Double.parseDouble(zeile.get(spalte).replace(',', '.'));
		} catch (NumberFormatException e) {
			final String feldName = getfeldName(spalte);
			throw new LineFormatException(
					"Das Feld " + feldName + " ist keine Fließpunktzahl sondern " + zeile.get(spalte));
		}
	}

	/**
	 * Gebe den Inhalt den gewünschten Spalte als Boolean zurück. Dabei wird ein kleines x als wahr gewertet, alles
	 * andere als falsch.
	 * 
	 * @param spalte
	 *            die Spalte zerobased
	 * @return ja oder nein
	 * @throws LineFormatException
	 *             falls das Feld keine ganze Zahl enthält
	 */
	public boolean getBoolean(final int spalte) throws LineFormatException {
		checkExists(spalte);
		return zeile.get(spalte) != null
				&& (zeile.get(spalte).trim().equals("x") || zeile.get(spalte).trim().toLowerCase().equals("j"));
	}

	/**
	 * Gebe den Inhalt den gewünschten Spalte als String zurück. Dabei werden illegale Zeichen und Zeilenumbrüche
	 * eliminiert.
	 * 
	 * @param spalte
	 *            die Spalte zerobased
	 * @return der String
	 * @throws LineFormatException
	 *             falls das Feld keine ganze Zahl enthält oder nicht existiert
	 */
	public String getString(final int spalte) throws LineFormatException {
		checkExists(spalte);
		return cleanString(zeile.get(spalte));
	}

	/**
	 * Enthält die Zeile am Anfang nur leere Einträge?
	 * 
	 * @return ja oder nein
	 */
	public boolean isEmpty() {
		return zeile.stream().limit(6).map(v -> (v == null || v.isEmpty())).reduce(true, (a, b) -> a && b);
	}

	/**
	 * Anzahl der gefundenen Felder in dieser Zeile.
	 * 
	 * @return die Anzahl
	 */
	public int size() {
		return zeile.size();
	}

	/**
	 * Die Zeilennummer dieser Zeile.
	 * 
	 * @return die Nummer gezählt ab 1
	 */
	public int getZeilenNummer() {
		return zeilenNummer;
	}

	private String getfeldName(final int spalte) {
		return (spalte < titelZeile.size() ? titelZeile.get(spalte) : "in Spalte " + spalte) + " in Zeile "
				+ zeilenNummer;
	}

	private void checkExists(final int spalte) throws LineFormatException {
		if (spalte >= zeile.size()) {
			final String feldName = getfeldName(spalte);
			throw new LineFormatException("Das Feld " + feldName + " existiert nicht in dieser Zeile");
		}
	}

	/**
	 * Bereinige einen String. Es werden Zeilenumbrüche in Spaces umgewandelt und illegale Zeichen eliminiert.
	 * 
	 * @param in
	 *            der Ausgangsstring
	 * @return der modifizierte String
	 */
	public static String cleanString(final String in) {
		return in.replace('\n', ' ').replace("\uFFFD", "");
	}
}
