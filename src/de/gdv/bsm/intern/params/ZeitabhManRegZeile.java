package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der zeitabhängigen Management-Regeln des Unternehmens. Abbild einer Zeile des Blattes
 * <code>zeitabh.ManReg</code>.
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
public class ZeitabhManRegZeile {
	private final int zeit;

	private final double grundUeberschuss;
	private final double zielBarauszahlungKlassisch;
	private final double zielBarauszahlungFlv;

	private final double sueafZufMin;
	private final double sueafZuf;
	private final double frfbUeberlauf;
	private final int rfbEntnahme;
	private final int sueafEntnahme;
	private final double ekZiel;
	private final double rEk;

	private final double rohUeb;
	private final double anteilUebrigenErgebnisseNeugeschaeft;

	private final double detProjektionFlv;
	private final int rlzNeuAnl;
	private final int fiBwr;

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
	public ZeitabhManRegZeile(final CsvZeile zeile) throws LineFormatException {
		zeit = zeile.getInt(0);
		grundUeberschuss = zeile.getDouble(1);
		zielBarauszahlungKlassisch = zeile.getDouble(2);
		zielBarauszahlungFlv = zeile.getDouble(3);
		sueafZufMin = zeile.getDouble(4);
		sueafZuf = zeile.getDouble(5);
		frfbUeberlauf = zeile.getDouble(6);
		rfbEntnahme = zeile.getInt(7);
		sueafEntnahme = zeile.getInt(8);
		ekZiel = zeile.getDouble(9);
		rEk = zeile.getDouble(10);
		rohUeb = zeile.getDouble(11);
		anteilUebrigenErgebnisseNeugeschaeft = zeile.getDouble(12);
		detProjektionFlv = zeile.getDouble(13);
		rlzNeuAnl = zeile.getInt(14);
		fiBwr = zeile.getInt(15);
	}

	/**
	 * Zeit. Spalte A.
	 * 
	 * @return der Wert
	 */
	public int getZeit() {
		return zeit;
	}

	/**
	 * Grundüberschuss aus Risiko- und übrigen Ergebnissen, relevent für Bestimmung der restlichen Deklaration, in %
	 * Spalte B
	 * 
	 * @return Grundüberschuss aus Risiko- und übrigen Ergebnissen
	 */
	public double getGrundUeberschuss() {
		return grundUeberschuss;
	}

	/**
	 * Ziel-Barauszahlung in Anteilen des Beitrages, Klassisches Geschäft. Spalte C
	 * 
	 * @return Ziel-Barauszahlung in Anteilen des Beitrages, Klassisches Geschäft
	 */
	public double getZielBarauszahlungKlassisch() {
		return zielBarauszahlungKlassisch;
	}

	/**
	 * Ziel-Barauszahlung in Anteilen des Beitrages, FLV. Spalte D
	 * 
	 * @return Ziel-Barauszahlung in Anteilen des Beitrages, FLV
	 */
	public double getZielBarauszahlungFlv() {
		return zielBarauszahlungFlv;
	}

	/**
	 * Ziel-Zuführung zum SÜAF bei der Deklaration p_SÜAFzuf_min. Spalte E.
	 * 
	 * @return der Wert
	 */
	public double getSueafZufMin() {
		return sueafZufMin;
	}

	/**
	 * Anteil der SÜAF-Zuführung an der Deklaration gesamt p_SÜAFZuf. Spalte F.
	 * 
	 * @return der Wert
	 */
	public double getSueafZuf() {
		return sueafZuf;
	}

	/**
	 * Anteil des „RfB-Überlaufs, der zu Erhöhung der Zieldeklaration verwendet wird p_fRfB_Überlauf. Spalte G.
	 * 
	 * @return der Wert
	 */
	public double getFrfbUeberlauf() {
		return frfbUeberlauf;
	}

	/**
	 * §56b VAG fRfB- Entnahme (Ja =1, Nein =0). Spalte H.
	 * 
	 * @return der Wert
	 */
	public int getRfbEntnahme() {
		return rfbEntnahme;
	}

	/**
	 * §56b VAG SÜAF- Entnahme (Ja =1, Nein =0). Spalte I.
	 * 
	 * @return der Wert
	 */
	public int getSueafEntnahme() {
		return sueafEntnahme;
	}

	/**
	 * Zielanteil des Eigenkapitals an der Deckungsrückstellung EKZiel. Spalte J.
	 * 
	 * @return der Wert
	 */
	public double getEkZiel() {
		return ekZiel;
	}

	/**
	 * Verzinsungsanforderung des handelsrechtlichen Eigenkapital r_EK. Spalte K.
	 * 
	 * @return der Wert
	 */
	public double getREk() {
		return rEk;
	}

	/**
	 * Zielbeteiligung am Rohüberschuss in % vom Rohüberschuss p_Rohüb. Spalte L.
	 * 
	 * @return der Wert
	 */
	public double getRohUeb() {
		return rohUeb;
	}

	/**
	 * Anteil des übrigen Ergebnisses für das Neugeschäft. Spalte M.
	 * 
	 * @return der Wert
	 */
	public double getAnteilUebrigenErgebnisseNeugeschaeft() {
		return anteilUebrigenErgebnisseNeugeschaeft;
	}

	/**
	 * deterministische Projektion FLV. Spalte N.
	 *
	 * @return der Wert
	 */
	public double getDetProjektionFlv() {
		return detProjektionFlv;
	}

	/**
	 * FI Neuanlage Restlaufzeit RLZ_neuAnl. Spalte O.
	 * 
	 * @return der Wert
	 */
	public int getRlzNeuAnl() {
		return rlzNeuAnl;
	}

	/**
	 * Verrechnungszeitraum für FI-BWR τ_Verechn. Spalte P.
	 * 
	 * @return der Wert
	 */
	public int getFiBwr() {
		return fiBwr;
	}

}
