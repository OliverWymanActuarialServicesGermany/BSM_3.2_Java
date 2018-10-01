/**
 * 
 */
package de.gdv.bsm.intern.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Classe zum Lesen von csv-Dateien. Dies ist analog zu einem BufferedReader, wobbei jedoch jeder
 * {@linkplain #readLine()} einen Vektor von Strings mit den gefundenen Feldern zurückliefert. Falls beim Lesen einer
 * Zeile ein Fehler auftritt, wird eine {@linkplain CsvReader.LineFormatException} geworfen.
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
 * 
 */
public class CsvReader implements AutoCloseable {
	/**
	 * Reader für die physikalische Datei.
	 */
	private BufferedReader csvReader;
	/**
	 * Der Feldtrenner, üblicherweise ein Semikolon.
	 */
	private char fieldSeparator;
	/**
	 * Der Trenner für Strings, gewöhnlich ein doppeltes Hochkomma.
	 */
	private char stringSeparator;
	/**
	 * Titelzeile für Fehlermeldungen.
	 */
	private List<String> titel = null;
	private int zeilenNummer = 0;

	/**
	 * Öffne einen Leser für eine CSV-Datei.
	 * 
	 * @param csvDatei
	 *            die zu lesende Datei.
	 * @param fieldSeparator
	 *            der Feldtrenner.
	 * @param stringSeparator
	 *            der Trenner für Strings
	 * @throws FileNotFoundException
	 *             falls die Datei nicht existiert.
	 */
	public CsvReader(File csvDatei, char fieldSeparator, char stringSeparator) throws FileNotFoundException {
		csvReader = new BufferedReader(new FileReader(csvDatei), 1024 * 32);
		this.fieldSeparator = fieldSeparator;
		this.stringSeparator = stringSeparator;
	}

	/**
	 * Lese eine Zeile aus der csv-Datei und gebe die Felder als Vektor zurück. Die Zeile muss folgenden Konventionen
	 * entsprechen:
	 * <ul>
	 * <li>beginnt ein Feld mit einem Trenner für Strings, so werden doppelte Feldtrenner als ein entsprechendes Zeichen
	 * des Strings interpretiert.</li>
	 * <li>beginnt ein Feld mit einem Trenner für Strings, so muss nach einem einfachen solchen Trenner entweder sofort
	 * ein Feldtrenner folgen, oder das Ende der Zeile erreicht sein. Das Feld wird dann als beendet gewertet.
	 * <li>jeder Feldtrenner, der nicht innerhalb eines Strings (also ein Feld, dass in Trenner für Strings
	 * eingeschlossen ist), beendet ein Feld.</li>
	 * <li>Feldtrenner am Ende einer Zeile erzeugen ein neues Feld, das aus einem leeren String besteht.
	 * </ul>
	 * 
	 * @return den Vektor der Felder, oder null, wenn die Datei zu ende ist.
	 * @throws IOException
	 *             bei Lesefehlern der Eingabedatei
	 * @throws LineFormatException
	 *             bei Syntaxfehlern in der Zeile
	 */
	public CsvZeile readLine() throws IOException, LineFormatException {
		String line = csvReader.readLine();
		if (line == null) {
			return null;
		}
		return splitLine(line);
	}

	private CsvZeile splitLine(String line) throws LineFormatException, IOException {
		boolean fieldStart = true;
		boolean quotedString = false;
		String currentField = "";
		Vector<String> result = new Vector<String>();
		int fieldCount = 1;
		boolean ready = false;
		do {
			for (int i = 0; i < line.length(); ++i) {
				char c = line.charAt(i);
				if (c == fieldSeparator) {
					// next char is field separator:
					if (quotedString) {
						// field separator inside quoted string: add field
						// separator to result
						currentField += fieldSeparator;
					} else {
						// save current field and reset for new field
						result.add(currentField);
						++fieldCount;
						currentField = "";
						fieldStart = true;
						quotedString = false;
					}
				} else if (c == stringSeparator) {
					// next char is string separator:
					if (fieldStart) {
						// string separator at field start: string is quoted
						// separator character is ignored
						quotedString = true;
						fieldStart = false;
					} else {
						// string separator inside field
						if (quotedString) {
							// string separator inside quoted field
							if (i < line.length() - 1) {
								// not at end of line:
								char next = line.charAt(i + 1);
								if (next == stringSeparator) {
									// next char is also a string separator
									// ignore next char and append string
									// separator to the result
									++i;
									currentField += stringSeparator;
								} else if (next == fieldSeparator) {
									// next char is a field separator
									// remove field separator and save field
									++i;
									result.add(currentField);
									++fieldCount;
									currentField = "";
									fieldStart = true;
									quotedString = false;
								} else {
									// simple string separator inside quoted
									// field:
									// this is an error
									throw new LineFormatException("Feld " + fieldCount
											+ ": einzelner Zeichenketten Begrenzer innerhalb eines quoted Strings");
								}
							} else {
								// Feldtrenner am Ende einer Zeile: String ist zu Ende
								quotedString = false;
							}
						} else {
							throw new LineFormatException("Feld " + fieldCount
									+ ": einzelner Zeichenketten Begrenzer innerhalb eines nonquoted Strings at: "
									+ line.substring(i));
						}
					}
				} else {
					fieldStart = false;
					currentField += c;
				}
			}
			if (quotedString) {
				// wir müssen noch eine Zeile lesen, da der Text da drin stehen kann
				line = csvReader.readLine();
				currentField += '\n';
				if (line == null) {
					throw new LineFormatException(
							"Feld " + fieldCount + ": fehlender Zeichenketten Begrenzer am Dateiende");
				}
			} else {
				ready = true;
			}
		} while (!ready);
		result.add(currentField);
		if (this.titel == null) {
			final List<String> titel = new ArrayList<>();
			for (String s : result) {
				titel.add(CsvZeile.cleanString(s));
			}
			this.titel = titel;
		}
		++zeilenNummer;
		return new CsvZeile(this.zeilenNummer, this.titel, result);
	}

	/**
	 * Ermittle die Titelzeile dieser csv-Datei.
	 * 
	 * @return die Titelzeile
	 */
	public List<String> getTitel() {
		return titel;
	}

	/**
	 * Schließen der Eingabedatei.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		csvReader.close();
	}

}
