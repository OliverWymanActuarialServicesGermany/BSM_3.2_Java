package de.gdv.bsm.intern.rechnung;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.kennzahlen.MittelwerteNurCe;
import de.gdv.bsm.vu.kennzahlen.MittelwerteUndCe;

/**
 * Ausgabe und Anzeige der berechneten Mittelwerte. Dies sind die {@link MittelwerteUndCe} und {@link MittelwerteNurCe},
 * wobei die ersteren auch über alle Pfade gemittelt werden.
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
public class MittelwerteUndCEZeitschrittig {
	private final String szenario;
	private final int szenarioId;
	private final String lob;
	private final int zeit;

	//private int anzahl = 0;
	private List<Field> sumFields = new ArrayList<>();
	private List<String> title = new ArrayList<>();
	// TODO: Prüfen ob hier static Definition sinvoll ist für Überschriften, die für alle gleich sind 
	private List<String> titleDrZeile1 = new ArrayList<>();
	private List<String> titleDrZeile2 = new ArrayList<>();
	private List<Double> values = new ArrayList<>();

	/**
	 * Erstelle die Grunddaten einer Zeile.
	 * @param szenarioNm 
	 * @param szenarioId 
	 * @param lob 
	 * @param zeit 
	 * @param values 
	 * 
	 * @param mwUndCe
	 *            die Grunddaten
	 * @param addierePfad0
	 *            sollen die Daten für Pfad 0 mit addiert werden?
	 * @throws IllegalArgumentException
	 *             bei Systemfehlern
	 * @throws IllegalAccessException
	 *             bei Systemfehlern
	 */
	public MittelwerteUndCEZeitschrittig(String szenarioNm, int szenarioId , String lob, int zeit, ArrayList<Double> values) {
		this.szenario = szenarioNm;
		this.szenarioId = szenarioId;
		this.lob = lob;
		this.zeit = zeit;
		this.values=values;

		for (Field field : MittelwerteUndCe.class.getDeclaredFields()) {
			final TableField tf = field.getAnnotation(TableField.class);
			if (tf != null) {
				sumFields.add(field);
				title.add("<html><p>" + tf.columnName() + "</p><p>Mittelwert</p></html>");
				titleDrZeile1.add(tf.columnName());
				titleDrZeile2.add("Mittelwert");
			}
		}
		for (int i = 0; i < sumFields.size(); ++i) {
			final Field f = sumFields.get(i);
			
			final boolean accessible = f.isAccessible();
			f.setAccessible(true);
			final TableField tf = f.getAnnotation(TableField.class);
			title.add("<html><p>" + tf.columnName() + "</p><p>CE</p></html>");
			titleDrZeile1.add(tf.columnName());
			titleDrZeile2.add("CE");
			f.setAccessible(accessible);
		}
		for (Field f : MittelwerteNurCe.class.getDeclaredFields()) {
			final TableField tf = f.getAnnotation(TableField.class);
			if (tf != null) {
				final boolean accessible = f.isAccessible();
				f.setAccessible(true);
				title.add("<html><p>" + tf.columnName() + "</p><p>CE</p></html>");
				titleDrZeile1.add(tf.columnName());
				titleDrZeile2.add("CE");
				f.setAccessible(accessible);
			}
		}

	}



	private final DecimalFormat df = new DecimalFormat("#.##############################");

	/**
	 * Gebe die Daten dieser Zeile im csv-Format aus.
	 * 
	 * @param printStream
	 *            der Ausgabestrom
	 */
	public void writeZeile(final PrintStream printStream) {
		printStream.print(szenario + ";" + szenarioId + ";" + lob + ";" + zeit);
		int i = 0;
		for (double v : values) {
			if (i < sumFields.size()) {
				printStream.print(";" + df.format(v));
			} else {
				printStream.print(";" + df.format(v));
			}
			++i;
		}
		printStream.println();
	}

	/**
	 * @return the title für das Tablemodel
	 */
	public List<String> getTitle() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("<html><p>Szenario</p><br/><p></p></html>");
		alleTitel.add("<html><p>Szenario ID</p><br/><p></p></html>");
		alleTitel.add("<html><p>LoB</p><br/><p></p></html>");
		alleTitel.add("<html><p>Zeit</p><br/><p></p></html>");
		alleTitel.addAll(title);

		return alleTitel;
	}

	/**
	 * @return the title für die csv_Datei - Zeile 1
	 */
	public List<String> getTitleDruckZeile1() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("Szenario");
		alleTitel.add("Szenario ID");
		alleTitel.add("LoB");
		alleTitel.add("Zeit");
		alleTitel.addAll(titleDrZeile1);

		return alleTitel;
	}

	/**
	 * @return the title für die csv_Datei - Zeile 2
	 */
	public List<String> getTitleDruckZeile2() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("");
		alleTitel.add("");
		alleTitel.add("");
		alleTitel.add("");
		alleTitel.addAll(titleDrZeile2);

		return alleTitel;
	}

	/**
	 * Gebe den Wert eines Feldes zurück.
	 * 
	 * @param index
	 *            des Feldes
	 * @return der Wert
	 */
	public double getValue(final int index) {
		if (index < sumFields.size()) {
			return values.get(index);
		} else {
			return values.get(index);
		}
	}

	/**
	 * Das zugrunde liegende Szenario.
	 * 
	 * @return the szenario
	 */
	public String getSzenario() {
		return szenario;
	}

	/**
	 * ID des zugrunde liegenden Szenarios.
	 * 
	 * @return the szenarioId
	 */
	public int getSzenarioId() {
		return szenarioId;
	}

	/**
	 * Line of Business.
	 * 
	 * @return the lob
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * Der Zeitpunkt.
	 * 
	 * @return the zeit
	 */
	public int getZeit() {
		return zeit;
	}

}
