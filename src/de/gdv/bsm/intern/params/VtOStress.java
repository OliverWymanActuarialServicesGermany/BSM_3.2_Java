package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * VT ohne Stress Daten des Unternehmens. Abbild des Blattes <code>VT o.Stress</code>.
 * <p>
 * Die Daten werden als aufbereitete Map vom Lob über die Zinsgeneration auf ein Array mit den Zeilen bereitgestellt.
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
public class VtOStress {
	// maps LoB -> altNeu -> Zins -> Zeit-Array
	private final Map<String, Map<String, Map<Integer, List<VtOStressZeile>>>> lobMap;

	/**
	 * Erzeuge die Datenstruktur aus einer csv-Datei.
	 * 
	 * @param dataFile
	 *            Name der csv-Datei
	 * @throws IOException
	 *             bei Ein-/Ausgabefehlern
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 */
	public VtOStress(final File dataFile) throws IOException, LineFormatException {
		final Map<String, Map<String, Map<Integer, List<VtOStressZeile>>>> map = new HashMap<>();
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// Titelzeile weglesen:
			csv.readLine();
			CsvZeile line;
			while ((line = csv.readLine()) != null) {
				final VtOStressZeile z = new VtOStressZeile(line);
				// leere Zeilen werden ignoriert
				if (z.getLob() == null || z.getLob().isEmpty())
					continue;
				if (!map.containsKey(z.getLob())) {
					map.put(z.getLob(), new HashMap<String, Map<Integer, List<VtOStressZeile>>>());
				}
				final Map<String, Map<Integer, List<VtOStressZeile>>> altNeuMap = map.get(z.getLob());
				if (!altNeuMap.containsKey(z.getAltNeuBestand())) {
					altNeuMap.put(z.getAltNeuBestand(), new HashMap<Integer, List<VtOStressZeile>>());
				}
				final Map<Integer, List<VtOStressZeile>> zinsMap = altNeuMap.get(z.getAltNeuBestand());
				if (!zinsMap.containsKey(z.getZinsGeneration())) {
					zinsMap.put(z.getZinsGeneration(), new ArrayList<VtOStressZeile>(101));
				}
				final List<VtOStressZeile> list = zinsMap.get(z.getZinsGeneration());
				if (z.getZeit() != list.size())
					throw new IllegalStateException("VT o.Stress: Zeit nicht fortlaufend!");
				list.add(z);
			}
		}
		// map nicht modifizierbar machen:
		for (String lob : map.keySet()) {
			final Map<String, Map<Integer, List<VtOStressZeile>>> altNeuMap = map.get(lob);
			for (String altNeu : altNeuMap.keySet()) {
				final Map<Integer, List<VtOStressZeile>> zinsMap = altNeuMap.get(altNeu);
				for (int zins : zinsMap.keySet()) {
					zinsMap.put(zins, Collections.unmodifiableList(zinsMap.get(zins)));
				}
				altNeuMap.put(altNeu, Collections.unmodifiableMap(zinsMap));
			}
			map.put(lob, Collections.unmodifiableMap(altNeuMap));
		}
		lobMap = map;
	}

	/**
	 * Die Datenstrukur in Form einer Map von Lob, alt/neu und Zinsgeneration auf die chronologische Liste der Zeilen.
	 * 
	 * @return die Map
	 */
	public Map<String, Map<String, Map<Integer, List<VtOStressZeile>>>> getMap() {
		return lobMap;
	}

}
