package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Referenzzinssätze für die Jahre vor dem ersten gerechneten Zeitschritt. Abbild des Blattes
 * <code>Referenzzinssatz</code>.
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
public class ReferenzZinssatz {
	private final Map<Integer, Double> zeilen = new HashMap<>();

	/**
	 * Erstelle die Tabelle mit den Referenzzinssätzen.
	 * 
	 * @param dataFile
	 *            Name der Eingabedatei
	 * @throws IOException
	 *             Dateifehler
	 * @throws LineFormatException
	 *             Formatfehler in der csv-Datei
	 */
	public ReferenzZinssatz(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// Titelzeile weglesen
			csv.readLine();

			CsvZeile zeile;
			int zeitPrev = Integer.MAX_VALUE;
			while ((zeile = csv.readLine()) != null) {
				final int zeit = zeile.getInt(0);
				if (zeitPrev != Integer.MAX_VALUE && zeit != zeitPrev + 1) {
					throw new LineFormatException("Zeit nicht chronologisch absteigend.");
				}
				final double zins = zeile.getDouble(1);
				zeilen.put(zeit, zins);

				zeitPrev = zeit;
			}
		}
	}

	/**
	 * Hole den Zins für einen Zeitpunkt.
	 * 
	 * @param zeit
	 *            Zeitpunkt von 0 bis -9.
	 * @return der Zins
	 */
	public double getZins(final int zeit) {
		return zeilen.get(zeit);
	}

}
