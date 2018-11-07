package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Tabelle BW Aktiva FI. Abbild des Blattes <code>BW Aktiva FI</code>.
 * <p>
 * Leere Zellen in den Spalten Cashflow FI und Handelsrechtlicher Ertrag werden als Null interpretiert.
 * 
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zust�ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlie�en, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende R�ckmeldungen w�rde der GDV
 * begr��en. Der GDV �bernimmt aber keine Haftung f�r die fehlerfreie Funktionalit�t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlie�lich aller Tabellen, Grafiken und Erl�uterungen sind urheberrechtlich
 * gesch�tzt. Die ausschlie�lichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell � GDV 2016</b>
 *
 */
public class BwAktivaFi {
	private final int maxZeitCashflowFi;
	private final List<BwAktivaFiZeile> zeilen = new ArrayList<>();

	// MIL_W.Schalesi
	private boolean[] hrESznrRechnen;
	private boolean hrERechnen;
	private final int anzahlSznr = 26; 	// plus das Standardszenario s.u.
	private final int startSpalte = 8;	// Spalte fuer den Boolean wert des Szenario 1

	/**
	 * Erstelle die Daten aus einer csv-Datei.
	 * 
	 * @param dataFile
	 *            die csv-Datei
	 * @throws IOException
	 *             bei Ein-/Ausgabefehlern
	 * @throws LineFormatException
	 *             bei Fomatfehlern in der Datei
	 */
	public BwAktivaFi(final File dataFile) throws IOException, LineFormatException {
		int maxZeitCashflowFi = 0;
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// csv.readLine(); - MIL_W.Schalesi lesen vom header
			CsvZeile header = csv.readLine();

			hrESznrRechnen = new boolean[anzahlSznr+1]; // Plus das Standardszenario "0"
			String hrEString;
			for (int i = 0; i < anzahlSznr; i++) {
				hrEString = header.getString(i * 2 + startSpalte).toUpperCase();
				if (hrEString.equals("WAHR")) {
					hrERechnen = true;
				} else {
					hrERechnen = false;
				}
				hrESznrRechnen[i+1] = hrERechnen; // Verschoben um das Standardszenario "0"
			}
			
			//MIL_M.Bartnicki
			hrESznrRechnen[0] = true;
			
			int count = 0;
			CsvZeile line;
			while ((line = csv.readLine()) != null) {
				final BwAktivaFiZeile z = new BwAktivaFiZeile(line, hrESznrRechnen);
				zeilen.add(z);
				// W.Schalesi: Check nur fuer standard cashflow
				if (z.getCashflowFi(anzahlSznr + 1) != 0.0 && maxZeitCashflowFi < z.getZeit()) {
					maxZeitCashflowFi = z.getZeit();
				}
				++count;
				if (z.getZeit() != count)
					throw new IllegalArgumentException("Zeit nicht fortlaufend!");
			}
		}
		this.maxZeitCashflowFi = maxZeitCashflowFi;
	}

	/**
	 * Ermittle die Datenzeile f�r eine feste Zeit.
	 * 
	 * @param zeit
	 *            die Zeit
	 * @return die Datenzeile
	 */
	public BwAktivaFiZeile get(final int zeit) {
		return zeilen.get(zeit - 1);
	}

	/**
	 * Die maximale Zeit mit einem nicht verschwindenden Cashflow.
	 * 
	 * @return die Zeit
	 */
	public int getMaxZeitCashflowFi() {
		return maxZeitCashflowFi;
	}
	
	/**
	 * Liste von eingelesen Zeilen
	 * 
	 * @return die Liste von BwAktivaFiZeile
	 */
	public List<BwAktivaFiZeile> getList() {
		return zeilen;
	}

}
