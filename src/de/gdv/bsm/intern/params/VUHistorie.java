package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * VU-Historie für chronologisch zurückliegende Zeitschritte. Abbild des Blattes <code>VU Historie</code>.
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
public class VUHistorie {
	private final List<VUHistorieZeile> zeilen = new ArrayList<>();

	/**
	 * Erstelle die Tabelle aus der exportierten csv-Datei.
	 * 
	 * @param dataFile
	 *            Name der Eingabedatei
	 * @throws IOException
	 *             Dateifehler
	 * @throws LineFormatException
	 *             Formatfehler in der csv-Datei
	 */

	public VUHistorie(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();
			CsvZeile line;
			int zeitPrev = Integer.MAX_VALUE;
			while ((line = csv.readLine()) != null) {
				try {
					final VUHistorieZeile z = new VUHistorieZeile(line);
					zeilen.add(z);

					if (zeitPrev != Integer.MAX_VALUE && z.getZeit() != zeitPrev + 1) {
						throw new IllegalArgumentException("Zeit nicht chronologisch fortlaufend.");
					}
					zeitPrev = z.getZeit();
				} catch (EmptyLineException e) {
					// leere Zeilen werden ignoriert
				}
			}
		}
		Collections.reverse(zeilen);
	}

	/**
	 * Ermittle die Werte für einen Zeitschritt
	 * 
	 * @param zeit
	 *            der Zeitschritt <= 0
	 * @return die Zeile
	 */
	public VUHistorieZeile get(final int zeit) {
		return zeilen.get(-zeit);
	}
	
	/**
	 * Gebe die Liste der Zeilen zurück
	 * 
	 * @return die Liste der Zeilen
	 */
	public List<VUHistorieZeile> getZeilen() {
		return zeilen;
	}
	

}
