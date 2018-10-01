package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * HGB Bilanzdaten des Unternehmens. Abbild des Blattes <code>HGB Bilanzdaten</code>.
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
public class HgbBilanzdaten {
	private final Map<String, Map<String, HgbBilanzdatenZeile>> zeilen = new HashMap<>();

	/**
	 * Erstelle eine Instanz aus einer csv-Datei.
	 * 
	 * @param dataFile
	 *            Name der csv-Datei
	 * @throws IOException
	 *             Dateifehler
	 * @throws LineFormatException
	 *             Formatfehler in der Datei
	 */
	public HgbBilanzdaten(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			csv.readLine();

			CsvZeile zeile;
			while ((zeile = csv.readLine()) != null) {
				final HgbBilanzdatenZeile z = new HgbBilanzdatenZeile(zeile);
				if (z.aktivaPassiva == null || z.aktivaPassiva.isEmpty() || z.position == null
						|| z.position.isEmpty()) {
					// leere Zeilen werden ignoriert
					continue;
				}
				if (!zeilen.containsKey(z.aktivaPassiva))
					zeilen.put(z.aktivaPassiva, new HashMap<>());
				if (zeilen.get(z.aktivaPassiva).containsKey(z.position)) {
					throw new LineFormatException("Die Position " + z.position + " ist doppelt definiert!");
				}
				zeilen.get(z.aktivaPassiva).put(z.position, z);
			}
		}
	}

	/**
	 * EK Buchwert. $D$2.
	 * 
	 * @return die Zahl
	 */
	public double getEkBuchwert() {
		return zeilen.get("P").get("EK").buchwert;
	}

	/**
	 * GR_NRD. $D$3.
	 * 
	 * @return die Zahl
	 */
	public double getGrNrdBuchwert() {
		return zeilen.get("P").get("GR_NRD").buchwert;
	}

	/**
	 * Freie RfB. $D$8.
	 * 
	 * @return die Zahl
	 */
	public double getFreieRfbBuchwert() {
		return zeilen.get("P").get("freie RfB").buchwert;
	}

	/**
	 * Passiva, Lat.Steuer. $D$9.
	 * 
	 * @return der Wert
	 */
	public double getLatSteuerPassiva() {
		return zeilen.get("P").get("Lat.Steuer").buchwert;
	}

	/**
	 * SP. $D$12.
	 * 
	 * @return die Zahl
	 */
	public double getSpBuchwert() {
		return zeilen.get("P").get("SP").buchwert;
	}

	/**
	 * Freie RfB. $D$13.
	 * 
	 * @return die Zahl
	 */
	public double getEqReFiBuchwert() {
		return zeilen.get("A").get("EQ_RE_FI").buchwert;
	}

	/**
	 * EQ. $D$14.
	 * 
	 * @return die Zahl
	 */
	public double getEqBuchwert() {
		return zeilen.get("A").get("EQ").buchwert;
	}

	/**
	 * RE. $D$15.
	 * 
	 * @return die Zahl
	 */
	public double getReBuchwert() {
		return zeilen.get("A").get("RE").buchwert;
	}

	/**
	 * RE. $D$16.
	 * 
	 * @return die Zahl
	 */
	public double getFiBuchwert() {
		return zeilen.get("A").get("FI").buchwert;
	}

	/**
	 * Aktiva, Lat.Steuer. $D$17.
	 * 
	 * @return der Wert
	 */
	public double getLatSteuerAktiva() {
		return zeilen.get("A").get("Lat.Steuer").buchwert;
	}

	/**
	 * SA+F_VV. $D$18.
	 * 
	 * @return die Zahl
	 */
	public double getSaFvvBuchwert() {
		return zeilen.get("A").get("SA+F_VV").buchwert;
	}

	/**
	 * RAP: Zinsen. $D$20.
	 * 
	 * @return die Zahl
	 */
	public double getRapZinsenBuchwert() {
		return zeilen.get("A").get("RAP: Zinsen").buchwert;
	}

	/**
	 * RAP: Mieten. $D$21.
	 * 
	 * @return die Zahl
	 */
	public double getRapMietenBuchwert() {
		return zeilen.get("A").get("RAP: Mieten").buchwert;
	}

}
