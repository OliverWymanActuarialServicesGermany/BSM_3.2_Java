package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * VT Flv Daten des Unternehmens. Abbild des Blattes <code>VT FLV</code>.
 * <p>
 * Zu einer Szenario-Id kann die Map mit allen Daten ermittelt werden. Diese ist dann gegeben als Map vom Lob über die
 * Zinsgeneration auf ein Array mit den Zeilen für die einzelnen Zeiten.
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
public class VtFlv {
	// Zeilen pro Szenario-ID in Reihenfolge des Auftretens
	private final Map<Integer, List<VtFlvZeile>> datenListe = new HashMap<>();
	private final Map<Integer, Map<String, Map<Integer, Map<String, List<VtFlvZeile>>>>> stressSzenarioMap;
	// Lobs in Reihenfolge des Auftretens
	private final List<String> lobs = new ArrayList<>();

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
	public VtFlv(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// Header weglesen:
			csv.readLine();

			final Map<Integer, Map<String, Map<Integer, Map<String, List<VtFlvZeile>>>>> tmpMap = new HashMap<>();
			final Set<String> lobSet = new HashSet<>();

			CsvZeile line;
			while ((line = csv.readLine()) != null) {
				final VtFlvZeile z = new VtFlvZeile(line);

				if (!datenListe.containsKey(z.getStressSzenarioId())) {
					datenListe.put(z.getStressSzenarioId(), new ArrayList<>());
				}
				datenListe.get(z.getStressSzenarioId()).add(z);

				if (!tmpMap.containsKey(z.getStressSzenarioId())) {
					tmpMap.put(z.getStressSzenarioId(),
							new HashMap<String, Map<Integer, Map<String, List<VtFlvZeile>>>>());
				}
				final Map<String, Map<Integer, Map<String, List<VtFlvZeile>>>> lobMap = tmpMap
						.get(z.getStressSzenarioId());
				if (!lobSet.contains(z.getLob())) {
					lobSet.add(z.getLob());
					lobs.add(z.getLob());
				}
				if (!lobMap.containsKey(z.getLob())) {
					lobMap.put(z.getLob(), new HashMap<Integer, Map<String, List<VtFlvZeile>>>());
				}

				final Map<Integer, Map<String, List<VtFlvZeile>>> idMap = lobMap.get(z.getLob());
				if (!idMap.containsKey(z.getRechnungsZinsGeneration())) {
					idMap.put(z.getRechnungsZinsGeneration(), new HashMap<String, List<VtFlvZeile>>());
				}

				final Map<String, List<VtFlvZeile>> altNeuMap = idMap.get(z.getRechnungsZinsGeneration());
				if (!altNeuMap.containsKey(z.getAltNeuBestand())) {
					altNeuMap.put(z.getAltNeuBestand(), new ArrayList<VtFlvZeile>());
				}

				final List<VtFlvZeile> list = altNeuMap.get(z.getAltNeuBestand());

				if (z.getZeit() != list.size())
					throw new IllegalStateException("Zeit nicht fortlaufend!");
				list.add(z);
			}

			for (int szenario : tmpMap.keySet()) {
				final Map<String, Map<Integer, Map<String, List<VtFlvZeile>>>> lobMap = tmpMap.get(szenario);
				for (String lob : lobMap.keySet()) {
					final Map<Integer, Map<String, List<VtFlvZeile>>> zinsMap = lobMap.get(lob);
					for (int zins : zinsMap.keySet()) {
						final Map<String, List<VtFlvZeile>> altNeuMap = zinsMap.get(zins);
						for (String altNeu : altNeuMap.keySet()) {
							altNeuMap.put(altNeu, Collections.unmodifiableList(altNeuMap.get(altNeu)));
						}
						zinsMap.put(zins, Collections.unmodifiableMap(altNeuMap));
					}
					lobMap.put(lob, Collections.unmodifiableMap(zinsMap));
				}
				tmpMap.put(szenario, Collections.unmodifiableMap(lobMap));
			}
			stressSzenarioMap = Collections.unmodifiableMap(tmpMap);
		}
	}

	/**
	 * Liefert die Map mit den Daten. Schlüssel sind der LoB, die Zinsgeneration sowie Alt/Neubestand.
	 * 
	 * @param szenarioId
	 *            des gewünschten Szenarios.
	 * @return die Map mit den Daten
	 */
	public Map<String, Map<Integer, Map<String, List<VtFlvZeile>>>> getSzenario(final int szenarioId) {
		return stressSzenarioMap.get(szenarioId);
	}

	/**
	 * Alle Zeilen eines Szenarios in Reihenfolge des Auftretens.
	 * 
	 * @param szenario
	 *            ID des Szenarios
	 * @return die Zeilen
	 */
	public List<VtFlvZeile> getSzenarioZeilen(final int szenario) {
		return datenListe.get(szenario);
	}

	/**
	 * Lobs in der Reihenfolge des Auftretens.
	 * 
	 * @return die Lobs
	 */
	public List<String> getLobs() {
		return Collections.unmodifiableList(lobs);
	}
}
