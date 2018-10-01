package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der VT FLV Daten des Unternehmens. Abbild einer Zeile des Blattes <code>VT FLV</code>.
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
public class VtFlvZeile {
	private final String stressSzenario;
	private final int stressSzenarioId;
	private final String lob;
	private final int zeit;
	private final int rechnungszinsgeneration;
	private final String altNeuBestand;

	private final double kostenRentenbezug;
	private final double praemienVerrentendesKapital;
	private final double leistungenBeimTodRentenbezug;
	private final double sonstigeErlebensFallLeistungenRentenbezug;
	private final double riskoErgebnisKlassischeRenteRentenbezug;
	private final double uebrigesErgebnisKlassischeRentenRentenbezug;
	private final double rechnungsmaeßigerZinsaufwandRentenbezug;
	private final double hgbDrStAnsammlungsguthabenRentenbezug;
	private final double kostenVuAufschubzeit;
	private final double praemienAufschubzeit;
	private final double leistungenBeiTodAufschubzeit;
	private final double sonstigeErlebensFallLeistungenAufschubzeit;
	private final double kapitalabfindungenAufschubzeit;
	private final double rueckkaufAufschubzeit;
	private final double riskoergebnisFlvAufschubzeit;
	private final double uebrigesErgebnisFlvAufschubzeit;
	private final double kostenderFondsverwaltung;
	private final double aktienanteilImFond;
	private final double immobilienanteilImFond;
	private final double fiAnteilImFond;
	private final double fondsentwicklung;

	/**
	 * Erzeuge eine Zeile aus einer aufbereiteten Zeile der csv-Datei.
	 * 
	 * @param zeile
	 *            die Zeile der csv-Datei
	 * @throws LineFormatException
	 *             bei Formatfehlern in der Datei
	 * @throws EmptyLineException
	 *             bei leeren Zeilen
	 */
	public VtFlvZeile(final CsvZeile zeile) throws LineFormatException {
		stressSzenario = zeile.getString(0);
		stressSzenarioId = zeile.getInt(1);
		lob = zeile.getString(2);
		zeit = zeile.getInt(3);
		rechnungszinsgeneration = zeile.getInt(4);
		altNeuBestand = zeile.getString(5);

		kostenRentenbezug = zeile.getDouble(6);
		praemienVerrentendesKapital = zeile.getDouble(7);
		leistungenBeimTodRentenbezug = zeile.getDouble(8);
		sonstigeErlebensFallLeistungenRentenbezug = zeile.getDouble(9);
		riskoErgebnisKlassischeRenteRentenbezug = zeile.getDouble(10);
		uebrigesErgebnisKlassischeRentenRentenbezug = zeile.getDouble(11);
		rechnungsmaeßigerZinsaufwandRentenbezug = zeile.getDouble(12);
		hgbDrStAnsammlungsguthabenRentenbezug = zeile.getDouble(13);
		kostenVuAufschubzeit = zeile.getDouble(14);
		praemienAufschubzeit = zeile.getDouble(15);
		leistungenBeiTodAufschubzeit = zeile.getDouble(16);
		sonstigeErlebensFallLeistungenAufschubzeit = zeile.getDouble(17);
		kapitalabfindungenAufschubzeit = zeile.getDouble(18);
		rueckkaufAufschubzeit = zeile.getDouble(19);
		riskoergebnisFlvAufschubzeit = zeile.getDouble(20);
		uebrigesErgebnisFlvAufschubzeit = zeile.getDouble(21);
		kostenderFondsverwaltung = zeile.getDouble(22);
		aktienanteilImFond = zeile.getDouble(23);
		immobilienanteilImFond = zeile.getDouble(24);
		fiAnteilImFond = zeile.getDouble(25);
		fondsentwicklung = zeile.getDouble(26);
	}

	/**
	 * Name des Szenarios. Spalte A.
	 * 
	 * @return das Szenario
	 */
	public String getStressSzenario() {
		return stressSzenario;
	}

	/**
	 * ID des Stressszenarios. Spalte B.
	 * 
	 * @return die Id
	 */
	public int getStressSzenarioId() {
		return stressSzenarioId;
	}

	/**
	 * Line of Business. Spalte C.
	 * 
	 * @return die Line
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * Zeitschritt dieses Datensatzes. Spalte D.
	 * 
	 * @return die Zeit
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Rechnungszinsgeneration dieser Zeile. Splate E.
	 * 
	 * @return die Zinsgeneration
	 */
	public int getRechnungsZinsGeneration() {
		return rechnungszinsgeneration;
	}

	/**
	 * Alt- / Neubestand (a / n) Spalte F
	 * 
	 * @return Alt- / Neubestand (a / n)
	 */
	public String getAltNeuBestand() {
		return altNeuBestand;
	}

	/**
	 * Kosten (Rentenbezug), Euro. Spalte G.
	 * 
	 * @return der Wert
	 */
	public double getKostenRentenbezug() {
		return kostenRentenbezug;
	}

	/**
	 * Prämien = Verrentendes Kapital (aus Ende Aufschub des Fonds), Euro. Spalte H.
	 * 
	 * @return der Wert
	 */
	public double getPraemienVerrentendesKapital() {
		return praemienVerrentendesKapital;
	}

	/**
	 * Leistungen beim Tod (in Rentenbezug), Euro. Spalte I.
	 * 
	 * @return der Wert
	 */
	public double getLeistungenBeimTodRentenbezug() {
		return leistungenBeimTodRentenbezug;
	}

	/**
	 * Sonstige Erlebens-fallleistungen (laufende Renten), Rentenbezug, Euro. Spalte J.
	 * 
	 * @return der Wert
	 */
	public double getSonstigeErlebensFallLeistungenRentenbezug() {
		return sonstigeErlebensFallLeistungenRentenbezug;
	}

	/**
	 * Riskoergebnis klassische Renten, Rentenbezug, Euro. Spalte K.
	 * 
	 * @return der Wert
	 */
	public double getRiskoErgebnisKlassischeRenteRentenbezug() {
		return riskoErgebnisKlassischeRenteRentenbezug;
	}

	/**
	 * Übriges Ergebnis Klassische Renten, Rentenbezug, Euro. Spalte L.
	 * 
	 * @return der Wert
	 */
	public double getUebrigesErgebnisKlassischeRentenRentenbezug() {
		return uebrigesErgebnisKlassischeRentenRentenbezug;
	}

	/**
	 * Rechnungsmäßiger Zinsaufwand, , Rentenbezug, Euro. Spalte M.
	 * 
	 * @return der Wert
	 */
	public double getRechnungsmaeßigerZinsaufwandRentenbezug() {
		return rechnungsmaeßigerZinsaufwandRentenbezug;
	}

	/**
	 * HGB DRSt inkl. Ansammlungsguthaben und festgelegte RfB ohne ZZR, Rentenbezug, Euro. Spalte N.
	 * 
	 * @return der Wert
	 */
	public double getHgbDrStAnsammlungsguthabenRentenbezug() {
		return hgbDrStAnsammlungsguthabenRentenbezug;
	}

	/**
	 * Kosten VU, Aufschubzeit, Euro. Spalte O.
	 * 
	 * @return der Wert
	 */
	public double getKostenVuAufschubzeit() {
		return kostenVuAufschubzeit;
	}

	/**
	 * Prämien (Brutto, in den Fonds), Aufschubzeit, Euro. Spalte P.
	 * 
	 * @return der Wert
	 */
	public double getPraemienAufschubzeit() {
		return praemienAufschubzeit;
	}

	/**
	 * Leistungen bei Tod (aus Fonds), Aufschubzeit, Euro. Spalte Q.
	 * 
	 * @return der Wert
	 */
	public double getLeistungenBeiTodAufschubzeit() {
		return leistungenBeiTodAufschubzeit;
	}

	/**
	 * Sonstige Erlebens-fallleistungen, Aufschubzeit, Euro. Spalte R.
	 * 
	 * @return der Wert
	 */
	public double getSonstigeErlebensFallLeistungenAufschubzeit() {
		return sonstigeErlebensFallLeistungenAufschubzeit;
	}

	/**
	 * Kapital-abfindungen, nur Renten-versicherung, Aufschubzeit, Euro. Spalte S.
	 * 
	 * @return der Wert
	 */
	public double getKapitalabfindungenAufschubzeit() {
		return kapitalabfindungenAufschubzeit;
	}

	/**
	 * Rückkauf, Aufschubzeit, Euro. Spalte T.
	 * 
	 * @return der Wert
	 */
	public double getRueckkaufAufschubzeit() {
		return rueckkaufAufschubzeit;
	}

	/**
	 * Riskoergebnis FLV, Aufschubzeit, Euro. Spalte U.
	 * 
	 * @return der Wert
	 */
	public double getRiskoergebnisFlvAufschubzeit() {
		return riskoergebnisFlvAufschubzeit;
	}

	/**
	 * Übriges Ergebnis - FLV, Aufschubzeit, Euro. Spalte V.
	 * 
	 * @return der Wert
	 */
	public double getUebrigesErgebnisFlvAufschubzeit() {
		return uebrigesErgebnisFlvAufschubzeit;
	}

	/**
	 * Kosten der Fondsverwaltung (bei 2 % Wertentwicklung vor Kosten), Euro. Spalte W.
	 * 
	 * @return der Wert
	 */
	public double getKostenderFondsverwaltung() {
		return kostenderFondsverwaltung;
	}

	/**
	 * Aktienanteil im Fond, %. Spalte X.
	 * 
	 * @return der Wert
	 */
	public double getAktienanteilImFond() {
		return aktienanteilImFond;
	}

	/**
	 * Immobilienanteil im Fond, %. Spalte Y.
	 * 
	 * @return der Wert
	 */
	public double getImmobilienanteilImFond() {
		return immobilienanteilImFond;
	}

	/**
	 * FI-anteil im Fond, %. Spalte Z.
	 * 
	 * @return der Wert
	 */
	public double getFiAnteilImFond() {
		return fiAnteilImFond;
	}

	/**
	 * Fondsentwicklung, Euro (bei 2 % Wertentwicklung vor Kosten für Fondsverwaltung). Spalte AA.
	 * 
	 * @return der Wert
	 */
	public double getFondsentwicklung() {
		return fondsentwicklung;
	}

}
