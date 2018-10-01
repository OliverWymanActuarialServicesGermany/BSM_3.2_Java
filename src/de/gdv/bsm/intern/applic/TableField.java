package de.gdv.bsm.intern.applic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;

/**
 * Annotation für Felder, die in Tabellen angezeigt werden sollen.
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
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
	/** Optionen für den Test von csv-Dateien mit berechneten Werten. */
	public static enum TestOption {
		/** Dieses Feld wird nicht geprüft. */
		NO,
		/** Für dieses Feld werden nur die Vorgegebenen Zeitschritte geprüft. */
		START,
		/** Dieses Feld wird insgesamt geprüft. */
		FULL
	}

	/**
	 * Anzahl der gewünschten Nachkommastellen.
	 * 
	 * @return die Stellen
	 */
	public int nachKomma() default 1;

	/**
	 * Soll die Zahl als Prozentzahl angezeigt werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean percent() default false;

	/**
	 * Soll das Feld angezeigt werden? Wird benutzt, um Spalten zu unterdrücken!
	 * 
	 * @return ja oder nein
	 */
	public boolean suppress() default false;

	/**
	 * Soll dieses Feld bei der Endlichkeitsprüfung berücksichtigt werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean checkFinite() default true;

	/**
	 * Feldname in {@link KennzahlenPfadweise} des CV Feldes. Für Felder aus {@link KennzahlenPfadweise} und
	 * {@link KennzahlenPfadweiseLoB} wird dies als CV verwendet (siehe die Definition im Excel-Blatt
	 * StrgTab_Mittelwerte).
	 * 
	 * @return der Feldname
	 */
	public String cvKennzahlen() default "mwPassiva";

	/**
	 * Soll dieses Feld getestet werden?
	 * 
	 * @return eine der Optionen
	 */
	public TestOption testOption() default TestOption.FULL;

	/**
	 * Column-Bezeichnung in Excel, falls von Reihenfolge in File abweichend.
	 * 
	 * @return die Bezeichnung
	 */
	public String testColumn() default "";
	
	/**
	 * Spalten-Bezeichnung für Export, falls von Java Variablenbennung abweichend.
	 * 
	 * @return die Benennung der Spalte
	 */
	public String columnName() default "";
	
	/**
	 * Spalten-Reihenfolge beim CSV Export. Default 1.0, gleiche Positionen werden entsprechend ihrer Deklarationsreihenfolge gelistet, kleinere Positionen werden als erstes dargestellt.
	 * 
	 * @return die Position für den Export
	 */
	public float exportPosition() default 1.0f;
}
