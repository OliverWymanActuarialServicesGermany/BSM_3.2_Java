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
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * VT Klassik Daten des Unternehmens. Abbild des Blattes <code>VT Klassik</code>.
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
public class VtKlassik {
	// maps Szenario-ID -> LoB -> Zins -> Zeit-Array
	private final Map<Integer, Map<String, Map<Integer, Map<String, List<VtKlassikZeile>>>>> daten = new HashMap<>();
	// listen pro Szenario-ID in der originalen Reihenfolge:
	private final Map<Integer, List<VtKlassikZeile>> datenListe = new HashMap<>();
	// Liste der LoB in Reihenfolge des Auftretens:
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
	public VtKlassik(final File dataFile) throws IOException, LineFormatException {
		// temporäre Map:
		final Map<Integer, Map<String, Map<Integer, Map<String, List<VtKlassikZeile>>>>> map = new HashMap<>();
		// erkannte LoB:
		final Set<String> lobSet = new HashSet<>();
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// Titelzeile überlesen
			csv.readLine();

			CsvZeile zeile;
			while ((zeile = csv.readLine()) != null) {
				VtKlassikZeile z;
				try {
					z = new VtKlassikZeile(zeile);
					if (!map.containsKey(z.getSzenarioId())) {
						map.put(z.getSzenarioId(),
								new HashMap<String, Map<Integer, Map<String, List<VtKlassikZeile>>>>());
					}
					final Map<String, Map<Integer, Map<String, List<VtKlassikZeile>>>> lobMap = map
							.get(z.getSzenarioId());
					if (!lobSet.contains(z.getLob())) {
						lobSet.add(z.getLob());
						lobs.add(z.getLob());
					}
					if (!lobMap.containsKey(z.getLob())) {
						lobMap.put(z.getLob(), new HashMap<Integer, Map<String, List<VtKlassikZeile>>>());
					}
					final Map<Integer, Map<String, List<VtKlassikZeile>>> zinsMap = lobMap.get(z.getLob());
					if (!zinsMap.containsKey(z.getZinsGeneration())) {
						zinsMap.put(z.getZinsGeneration(), new HashMap<String, List<VtKlassikZeile>>());
					}
					final Map<String, List<VtKlassikZeile>> altNeuMap = zinsMap.get(z.getZinsGeneration());
					if (!altNeuMap.containsKey(z.getAltNeuBestand())) {
						altNeuMap.put(z.getAltNeuBestand(), new ArrayList<VtKlassikZeile>());
					}

					final List<VtKlassikZeile> list = altNeuMap.get(z.getAltNeuBestand());
					if (z.getZeit() != list.size())
						throw new IllegalStateException("Zeit nicht fortlaufend!");
					list.add(z);

					if (!datenListe.containsKey(z.getSzenarioId())) {
						datenListe.put(z.getSzenarioId(), new ArrayList<>());
					}
					datenListe.get(z.getSzenarioId()).add(z);
				} catch (EmptyLineException e) {
					// leerzeilen in der csv-Datei werden ignoriert.
				}
			}
		}
		int zeitHorizont = -1;
		for (int szId : map.keySet()) {
			Map<String, Map<Integer, Map<String, List<VtKlassikZeile>>>> szMap = map.get(szId);
			for (String lob : szMap.keySet()) {
				Map<Integer, Map<String, List<VtKlassikZeile>>> lobMap = szMap.get(lob);
				for (int zins : lobMap.keySet()) {
					Map<String, List<VtKlassikZeile>> altNeuMap = lobMap.get(zins);
					for (String altNeu : altNeuMap.keySet()) {
						int zh = altNeuMap.get(altNeu).size() - 1;
						if (zeitHorizont < 0) {
							zeitHorizont = zh;
						} else {
							if (zeitHorizont != zh) {
								throw new IllegalStateException("Differierender Zeithorizont");
							}
						}
					}
				}
			}
		}
		// mache die Map nicht modifizierbar:
		for (int szenarioId : map.keySet()) {
			final Map<String, Map<Integer, Map<String, List<VtKlassikZeile>>>> lobMap = map.get(szenarioId);
			for (String lob : lobMap.keySet()) {
				final Map<Integer, Map<String, List<VtKlassikZeile>>> zinsMap = lobMap.get(lob);
				for (int zins : zinsMap.keySet()) {
					final Map<String, List<VtKlassikZeile>> altNeuMap = zinsMap.get(zins);
					for (String altNeu : altNeuMap.keySet()) {
						altNeuMap.put(altNeu, Collections.unmodifiableList(altNeuMap.get(altNeu)));
					}
					zinsMap.put(zins, Collections.unmodifiableMap(altNeuMap));
				}
				lobMap.put(lob, Collections.unmodifiableMap(lobMap.get(lob)));
			}
			daten.put(szenarioId, Collections.unmodifiableMap(map.get(szenarioId)));
		}
	}

	/**
	 * Liefert die Map mit den Daten. Schlüssel sind der LoB, die Zinsgeneration und Alt/Neubestand.
	 * 
	 * @param szenarioId
	 *            des gewünschten Szenarios.
	 * @return die Map mit den Daten
	 */
	public Map<String, Map<Integer, Map<String, List<VtKlassikZeile>>>> getSzenario(final int szenarioId) {
		return daten.get(szenarioId);
	}

	/**
	 * Liefert die Daten eines Szenarios als Liste in Originalreihenfolge.
	 * 
	 * @param szenarioId
	 *            des Szenarios
	 * @return die Liste
	 */
	public List<VtKlassikZeile> getSzenarioListe(final int szenarioId) {
		if (!datenListe.containsKey(szenarioId)) {
			throw new IllegalArgumentException(
					"VtKlassik Daten für Stressszenario " + szenarioId + " konnten nicht gefunden werden.");
		}
		return Collections.unmodifiableList(datenListe.get(szenarioId));
	}

	/**
	 * LoBs in Reihenfolge ihres Auftretens.
	 * 
	 * @return die Lobs
	 */
	public List<String> getLobs() {
		return Collections.unmodifiableList(lobs);
	}
	
	/**
	 * XX
	 * 
	 * @return die Daten
	 */
	public Map<Integer, List<VtKlassikZeile>>  getDatenListe() {
		return datenListe; 
	}
	
}
