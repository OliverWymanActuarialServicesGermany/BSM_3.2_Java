package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import de.gdv.bsm.intern.applic.Pair;
import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;

/**
 * Liste der auszugebenden Felder aus agg und rzg pro Szenario und Pfad. Dies entspricht dem Blatt <code>save2CSV</code>
 * in Excel.
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
public class Save2csv {

	private final Map<Integer, Map<Integer, List<Pair<String, Pair<String, Pair<String, Integer>>>>>> fields;

	private final Map<String, Field> aggFields;
	private final Map<String, Field> rzgFields;

	/**
	 * Einlesen des Blattes Save2csv
	 * 
	 * @param dataFile
	 * @throws IOException
	 * @throws LineFormatException
	 */
	public Save2csv(final File dataFile) throws IOException, LineFormatException {
		final Map<String, Field> aggDefinedFields = new TreeMap<>();
		for (Field f : AggZeile.class.getDeclaredFields()) {
			final TableField t = f.getAnnotation(TableField.class);
			if (t != null) {
				aggDefinedFields.put(t.testColumn(), f);
			}
		}
		final Map<String, Field> rzgDefinedFields = new TreeMap<>();
		for (Field f : RzgZeile.class.getDeclaredFields()) {
			final TableField t = f.getAnnotation(TableField.class);
			if (t != null) {
				rzgDefinedFields.put(t.testColumn(), f);
			}
		}

		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {

			fields = new TreeMap<>();
			aggFields = new HashMap<>();
			rzgFields = new HashMap<>();
			csv.readLine();

			CsvZeile zeile;
			while ((zeile = csv.readLine()) != null) {
				final Save2csvZeile smZeile = new Save2csvZeile(zeile);

				if (!fields.containsKey(smZeile.getStressSzenarioId())) {
					fields.put(smZeile.getStressSzenarioId(), new TreeMap<>());
				}
				final Map<Integer, List<Pair<String, Pair<String, Pair<String, Integer>>>>> fields2 = fields
						.get(smZeile.getStressSzenarioId());

				if (!fields2.containsKey(smZeile.getPfad())) {
					fields2.put(smZeile.getPfad(), new Vector<>());
				}
				final List<Pair<String, Pair<String, Pair<String, Integer>>>> fields3 = fields2.get(smZeile.getPfad());

				Pair<String, Integer> p1 = new Pair<String, Integer>(smZeile.getSpalte().toUpperCase().trim(),
						smZeile.getDecPre());
				Pair<String, Pair<String, Integer>> p2 = new Pair<String, Pair<String, Integer>>(smZeile.getBlatt().toLowerCase().trim(), p1);
				Pair<String, Pair<String, Pair<String, Integer>>> p3 = new Pair<String, Pair<String, Pair<String, Integer>>>(smZeile.getName(), p2);
				fields3.add(p3);

				switch (smZeile.getBlatt().toLowerCase()) {
				case "agg":

					if (aggDefinedFields.containsKey(smZeile.getSpalte())) {
						aggFields.put(smZeile.getSpalte(), aggDefinedFields.get(smZeile.getSpalte()));
					} else {
						throw new LineFormatException("Spalte " + smZeile.getSpalte() + " nicht in agg gefunden");
					}
					break;
				case "rzg":
					if (rzgDefinedFields.containsKey(smZeile.getSpalte())) {
						rzgFields.put(smZeile.getSpalte(), rzgDefinedFields.get(smZeile.getSpalte()));
					} else {
						throw new LineFormatException("Spalte " + smZeile.getSpalte() + " nicht in rzg gefunden");
					}
					break;
				default:
					throw new LineFormatException("Das Blatt " + smZeile.getBlatt() + " nicht erlaubt");
				}

			}
		}
	}

	/**
	 * Alle ausgzugebenden Spalten nach Szenario und Pfad. Die Liste besteht aus Namen, Blatt und Spalte
	 * 
	 * 
	 * @return Map
	 */
	public Map<Integer, Map<Integer, List<Pair<String, Pair<String, Pair<String, Integer>>>>>> getFields() {
		return fields;
	}

	/**
	 * Das agg-Feld zur Spalte
	 * 
	 * @param spalte
	 * 
	 * @return Map
	 */
	public Field getAggField(String spalte) {
		return aggFields.get(spalte);
	}

	/**
	 * Das rzg-Feld zur Spalte.
	 * 
	 * @param spalte
	 * 
	 * @return Map
	 */
	public Field getRzgField(String spalte) {
		return rzgFields.get(spalte);
	}

}
