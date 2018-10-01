package de.gdv.bsm.intern.applic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import de.gdv.bsm.intern.rechnung.RechenThread;


/**
 * Ausgabethreads für Daten, die mit der Annotation {@link TableField} die Felder markiert haben. Es werden genau die
 * markierten Felder in die Datei ausgegeben.
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
public class AusgabeThreadTableField implements AusgabeThread {
	private final RechenThread master;
	private final RechenFortschrittInterface rechenFortschritt;
	private final Class<?> klasse;
	private final List<?> data;
	private final File fileName;
	private final List<Field> dataFields;

	private Optional<Throwable> error = Optional.empty();

	/**
	 * Erstelle ein Runnable.
	 * 
	 * @param master
	 *            kontrollierender Prozess
	 * @param rechenFortschritt
	 *            für die Ausgabe von Prozentangaben
	 * @param klasse
	 *            Basisklasse für die {@link TableField}-Annotationen
	 * @param data
	 *            die Liste mit den auszugebenden Datensätzen
	 * @param fileName
	 *            die Ausgabedatei
	 */
	public AusgabeThreadTableField(final RechenThread master, final RechenFortschrittInterface rechenFortschritt,
			final Class<?> klasse, final List<?> data, final File fileName) {
		this.master = master;
		this.rechenFortschritt = rechenFortschritt;
		this.klasse = klasse;
		this.data = data;
		this.fileName = fileName;

		final List<Field> dataFields = new ArrayList<>();
		// Note: getDeclaredFields only includes Fields of Class without taking into account inheritance 
		// Note: getAnnotation includes also annotations in super classes thus taking into account inheritance		
		ArrayList<Field> allFields=new ArrayList<Field>();
		allFields.addAll(Arrays.asList(klasse.getDeclaredFields()));
		allFields.addAll(Arrays.asList(klasse.getSuperclass().getDeclaredFields()));
		for (Field f : allFields) {
			final TableField tf = f.getAnnotation(TableField.class);
			if (tf != null) {
				dataFields.add(f);
			}
		}
		
		
		// Sortiere die Listenelemente nach getAnnotation(TableField.class).exportPosition()
		Collections.sort(dataFields,
					new Comparator<Field>() {
						@Override
						public int compare(final Field f1, final Field f2) {
						      return Float.compare(f1.getAnnotation(TableField.class).exportPosition(),f2.getAnnotation(TableField.class).exportPosition());
						      }
					}
		);
		
		
		this.dataFields = Collections.unmodifiableList(dataFields);
	}

	@Override
	public void run() {
		try (final PrintStream out = new PrintStream(
				new BufferedOutputStream(new FileOutputStream(fileName), 8 * 1024))) {
			final List<Boolean> accessible = new ArrayList<>();

			boolean first = true;
			for (Field f : dataFields) {
				accessible.add(f.isAccessible());
				f.setAccessible(true);
				out.print((first ? "" : ";") + (f.getAnnotation(TableField.class).columnName().equals("") ?  f.getName() : f.getAnnotation(TableField.class).columnName()));
				first = false;
			}
			out.println();

			final DecimalFormat df = new DecimalFormat("#.##############################");

			int lastPercent = 0;
			int anzahl = 0;
			for (Object zeile : data) {
				first = true;
				for (Field f : dataFields) {
					if (!first)
						out.print(';');
					final TableField tf = f.getAnnotation(TableField.class);
					if (tf == null || !tf.suppress()) {
						final Object data = f.get(zeile);
						first = false;
						if (data instanceof Double) {
							double d = ((Double) data).doubleValue();
							if (Double.isFinite(d))
								out.print(df.format(d));
							else
								out.print("0");
						}
						else if (data instanceof Boolean){
							if ((Boolean)data)
								out.print("1");
							else
								out.print("0");
						}
						
						else {
							try{
								out.print(data.toString());
							}
							catch(NullPointerException e) {}
						}
					}
				}
				out.println();
				++anzahl;
				final int percent = (anzahl * 100) / data.size();
				if (lastPercent != percent) {
					rechenFortschritt.setAusgabePercent(klasse, percent);
					lastPercent = percent;
				}
			}

			// zugriffsmöglichkeit wieder zurücksetzen:
			for (int i = 0; i < dataFields.size(); ++i) {
				dataFields.get(i).setAccessible(accessible.get(i));
			}

		} catch (

		Throwable e)

		{
			error = Optional.of(e);
		} finally

		{
			master.ausgabeReady(this);
		}

	}

	/**
	 * Ist bei der Berechnung ein Fehler aufgetreten?
	 * 
	 * @return wenn nicht empty, ist der Wert der Fehlern; andernfalls ist kein Fehler aufgetreten
	 */
	public Optional<Throwable> getError() {
		return error;
	}
}
