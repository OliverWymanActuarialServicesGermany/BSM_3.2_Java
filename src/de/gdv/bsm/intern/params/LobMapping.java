package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Mapping der verschiedenen LOB. Abbild des Blattes <code>LoB-mapping</code>. Jede Zeile enthält die Angaben für ein
 * Mapping.
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
public class LobMapping {
	/** Konstante für UEB. */
	public static final String UEB = "UEB";
	/** Konstante für NUEB. */
	public static final String NUEB = "NUEB";

	private final List<String> header;
	private final Map<String, LobMappingZeile> zeilen = new HashMap<>();

	/**
	 * Erzeuge die Mapping-Struktur aus einer csv-Datei.
	 * 
	 * @param dataFile
	 *            vollständiger Name der Eingabedatei
	 * @throws IOException
	 *             bei Dateifehlern
	 * @throws LineFormatException
	 *             bei Formatfehlern in der csv-Datei
	 */
	public LobMapping(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();
			header = csv.getTitel();

			CsvZeile zeile;
			while ((zeile = csv.readLine()) != null) {
				LobMappingZeile lobZeile = new LobMappingZeile(zeile);
				if (lobZeile.getKuerzel() == null || lobZeile.getKuerzel().trim().isEmpty())
					continue;
				if (zeilen.containsKey(lobZeile.getKuerzel())) {
					throw new LineFormatException(
							"Doppeltes LoB Kürzel in Zeile " + zeile.getZeilenNummer() + ": " + lobZeile.getKuerzel());
				}
				zeilen.put(lobZeile.getKuerzel(), lobZeile);
			}
		}
	}

	/**
	 * Die Namen der Titelfelder.
	 * 
	 * @return die Namen
	 */
	public List<String> getHeader() {
		return Collections.unmodifiableList(header);
	}

	/**
	 * Ermittle zu einem LoB Kürzel das passende Mapping.
	 * 
	 * @param kuerzel
	 *            des LoB
	 * @return das Mapping
	 */
	public LobMappingZeile getLobMapping(final String kuerzel) {
		return zeilen.get(kuerzel);
	}

}
