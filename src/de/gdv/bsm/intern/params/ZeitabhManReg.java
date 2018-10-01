package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Zeitabhängige Managemen Daten des Unternehmens. Abbild des Blattes <code>zeitabh.ManReg</code>.
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
public class ZeitabhManReg {
	private final List<ZeitabhManRegZeile> zeilen = new ArrayList<>();

	/**
	 * Erstelle die Datenstruktur aus einer Eingangsdatei.
	 * 
	 * @param dataFile
	 *            Dateiname der Eingangsdatei
	 * @throws IOException
	 *             bei Ein-/Ausgabefehlern
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 */
	public ZeitabhManReg(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// Header weglesen
			csv.readLine();

			CsvZeile line;
			int count = 0;
			while ((line = csv.readLine()) != null) {
				final ZeitabhManRegZeile z = new ZeitabhManRegZeile(line);
				zeilen.add(z);
				++count;
				if (z.getZeit() != count)
					throw new IllegalArgumentException("Zeit nicht fortlaufend!");
			}
		}
	}

	/**
	 * Ermittle die Managementparameter für die angegeben Zeit.
	 * 
	 * @param zeit
	 *            die Zeit
	 * @return die Management Parameter
	 */
	public ZeitabhManRegZeile get(final int zeit) {
		return zeilen.get(zeit - 1);
	}
	
	/**
	 * Ermittle die Liste an ZeitabhManRegZeile
	 * 
	 * @return die Management Parameter Liste
	 */
	public List<ZeitabhManRegZeile> getList() {
		return zeilen;
	}

}
