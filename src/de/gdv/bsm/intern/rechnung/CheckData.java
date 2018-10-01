package de.gdv.bsm.intern.rechnung;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gdv.bsm.intern.applic.TableField;

/**
 * Überprüfung von Double-Feldern auf illegale Werte.
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
 *
 */
public class CheckData {
	// Map mit allen zu prüfenden Feldern pro Klasse
	private static final Map<Class<?>, List<Field>> doubleFields = new HashMap<>();

	/**
	 * Prüfe alle als {@link TableField} markierten double-Felder auf gültige Werte.
	 * <p/>
	 * Alle diese Werte werden geprüft, ob sie finit sind. Wenn mindestens ein Feld nicht finit ist, wird eine Liste
	 * aller falschen Felder zurückgeliefert. Ansonsten wird eine leere Liste zurückgeliefert.
	 * <p/>
	 * Durch setzen von {@link TableField#checkFinite()} auf false können einzelne Felder von dieser Prüfung
	 * ausgeschlossen werden.
	 * 
	 * @param data
	 *            die zu prüfenden Daten
	 * @return die Liste der fehlerhaften Felder, oder eine leere Liste
	 */
	public synchronized static List<String> checkFinite(final Object data) {
		if (!doubleFields.containsKey(data.getClass())) {
			cashFields(data);
		}
		final List<String> errors = new ArrayList<>();
		for (Field field : doubleFields.get(data.getClass())) {
			final boolean accessible = field.isAccessible();
			field.setAccessible(true);
			try {
				final double value = field.getDouble(data);
				if (!Double.isFinite(value)) {
					errors.add(data.getClass().getSimpleName() + "." + field.getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException("Fehler beim Zugriff", e);
			}
			field.setAccessible(accessible);
		}
		return errors;
	}

	private static void cashFields(Object data) {
		final Class<?> c = data.getClass();
		final List<Field> fields = new ArrayList<>();
		for (Field field : c.getDeclaredFields()) {
			final TableField tf = field.getAnnotation(TableField.class);
			if (tf != null && tf.checkFinite()) {
				if (field.getType() == double.class) {
					fields.add(field);
				}
			}
		}
		doubleFields.put(c, fields);
	}
}
