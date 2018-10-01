package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Tabelle Genuss und Nachrang. Abbild des Blattes <code>Genuss+Nachrang</code>.
 * <p>
 * Leere Zellen in den Spalten Zinsen und Rückzahlung werden als Null interpretiert.
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
public class GenussNachrang {
	private final List<GenussNachrangZeile> zeilen = new ArrayList<>();
	private final Map<Integer, GenussNachrangZeile> leerZeilen = new HashMap<>();

	/**
	 * Erstelle die Datenstruktur aus einer Datei.
	 * 
	 * @param dataFile
	 *            die Datei
	 * @throws IOException
	 *             bei Ein-/Ausgabefehlern
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 */
	public GenussNachrang(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();
			int count = 0;
			CsvZeile line;
			while ((line = csv.readLine()) != null) {
				final GenussNachrangZeile z = new GenussNachrangZeile(line);
				zeilen.add(z);
				++count;
				if (z.getZeit() != count)
					throw new IllegalArgumentException("Zeit nicht fortlaufend!");
			}
		}
	}

	/**
	 * Ermittle eine Datenzeile anhand der Zeit.
	 * 
	 * @param zeit
	 *            die Zeit
	 * @return die Zeile
	 */
	public GenussNachrangZeile get(final int zeit) {
		if (zeilen.size() <= zeit - 1) {
			if (!leerZeilen.containsKey(zeit))
				leerZeilen.put(zeit, new GenussNachrangZeile(zeit));
			return leerZeilen.get(zeit);
		}
		return zeilen.get(zeit - 1);
	}

}
