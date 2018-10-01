package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile des Szenario-Mappings.
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
public class Save2csvZeile {
	private final int stressSzenarioId;
	private final int pfad;
	private final String name;
	private final String blatt;
	private final String spalte;
	private final int decpre; 

	/**
	 * Ertselle eine Zeile des Szenario-Mappings.
	 * 
	 * @param felder
	 *            Liste der Felder dieser Zeile
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 */
	Save2csvZeile(final CsvZeile felder) throws LineFormatException {
		stressSzenarioId = felder.getInt(0);
		pfad = felder.getInt(1);
		name = felder.getString(2);
		blatt = felder.getString(3);
		spalte = felder.getString(4);
		decpre = felder.getInt(5);
	}

	/**
	 * Auszugebendes Stress-Szenario. (Excel Spalte save2CSV!A)
	 * 
	 * @return ID des Szenarios
	 */
	public int getStressSzenarioId() {
		return stressSzenarioId;
	}

	/**
	 * Auszugebender pfad. (Excel Spalte save2CSV!B)
	 * 
	 * @return die Pfadnummer
	 */
	public int getPfad() {
		return pfad;
	}

	/**
	 * Feldname für Titelzeile der csv-Datei. (Excel Spalte save2CSV!C)
	 * 
	 * @return der Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Name des Blattes (agg oder rzg). (Excel Spalte save2CSV!D)
	 * 
	 * @return der Name des Blattes
	 */
	public String getBlatt() {
		return blatt;
	}

	/**
	 * Die gewünschte Spalte (A, B, C, ...). (Excel Spalte save2CSV!E)
	 * 
	 * @return die Spalte
	 */
	public String getSpalte() {
		return spalte.toUpperCase();
	}
	
	/**
	 * Die gewünschte Anzahl Nachkommastellen für die aktuelle Spalte  (Excel Spalte save2CSV!F)
	 * 
	 * @return die Anzahl Nachkommastellen
	 */
	public int getDecPre() {
		return decpre;
	}

}
