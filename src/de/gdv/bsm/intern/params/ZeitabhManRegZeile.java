package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der zeitabhÃ¤ngigen Management-Regeln des Unternehmens. Abbild einer Zeile des Blattes
 * <code>zeitabh.ManReg</code>.
 * 
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zustÃ¤ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlieÃŸen, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende RÃ¼ckmeldungen wÃ¼rde der GDV
 * begrÃ¼ÃŸen. Der GDV Ã¼bernimmt aber keine Haftung fÃ¼r die fehlerfreie FunktionalitÃ¤t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlieÃŸlich aller Tabellen, Grafiken und ErlÃ¤uterungen sind urheberrechtlich
 * geschÃ¼tzt. Die ausschlieÃŸlichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell Â© GDV 2016</b>
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
	
	// MIL_W.Schalesi
	private final double sueafZufAlternative;
	private final double frfbUeberlaufAlternative;
	private final double deltaNvz;
	private final int fiBwrStandard;
	private final int fiBwrAlternative;

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
		// MIL_W.Schalesi
		sueafZufAlternative = zeile.getDouble(6);

		frfbUeberlauf = zeile.getDouble(7);
		// MIL_W.Schalesi
		frfbUeberlaufAlternative = zeile.getDouble(8);
		
		rfbEntnahme = zeile.getInt(9);
		sueafEntnahme = zeile.getInt(10);
		ekZiel = zeile.getDouble(11);
		rEk = zeile.getDouble(12);
		rohUeb = zeile.getDouble(13);
		anteilUebrigenErgebnisseNeugeschaeft = zeile.getDouble(14);
		detProjektionFlv = zeile.getDouble(15);
		rlzNeuAnl = zeile.getInt(16);
		fiBwr = zeile.getInt(17);
		// MIL_W.Schalesi
		deltaNvz = zeile.getDouble(18);
		fiBwrStandard = zeile.getInt(19);
		fiBwrAlternative = zeile.getInt(20);

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
	 * Ziel-Barauszahlung in Anteilen des Beitrages, Klassisches GeschÃ¤ft. Spalte C
	 * 
	 * @return Ziel-Barauszahlung in Anteilen des Beitrages, Klassisches GeschÃ¤ft
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
	 * Ziel-Zuführung zum SÃœAF bei der Deklaration p_SÜFzuf_min. Spalte E.
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
	 * MIL_W.Schalesi: Anteil der alternativen SÜAF-Zuführung an der Deklaration gesamt p_SÜAFZuf. Spalte G.
	 * 
	 * @return der Wert
	 */
	public double getSueafZufAlternative() {
		return sueafZufAlternative;
	}
	
	/**
	 * Anteil des p_RfB-Überlaufs, der zu Erhöhung der Zieldeklaration verwendet
	 * wird p_fRfB_Überlauf. Spalte H.
	 * 
	 * @return der Wert
	 */
	public double getFrfbUeberlauf() {
		return frfbUeberlauf;
	}

	/**
	 * MIL_W.Schalesi: Anteil der alternativen des p_RfB-Überlaufs, der zu Erhöhung der Zieldeklaration verwendet
	 * wird p_fRfB_Überlauf. Spalte I.
	 * 
	 * @return der Wert
	 */
	public double getFrfbUeberlaufAlternative() {
		return frfbUeberlaufAlternative;
	}
	
	/**
	 * Â§56b VAG fRfB- Entnahme (Ja =1, Nein =0). Spalte J.
	 * 
	 * @return der Wert
	 */
	public int getRfbEntnahme() {
		return rfbEntnahme;
	}

	/**
	 * Â§56b VAG SÜAF- Entnahme (Ja =1, Nein =0). Spalte K.
	 * 
	 * @return der Wert
	 */
	public int getSueafEntnahme() {
		return sueafEntnahme;
	}

	/**
	 * Zielanteil des Eigenkapitals an der Deckungsrückstellung EKZiel. Spalte L.
	 * 
	 * @return der Wert
	 */
	public double getEkZiel() {
		return ekZiel;
	}

	/**
	 * Verzinsungsanforderung des handelsrechtlichen Eigenkapital r_EK. Spalte M.
	 * 
	 * @return der Wert
	 */
	public double getREk() {
		return rEk;
	}

	/**
	 * Zielbeteiligung am Rohüberschuss in % vom Rohüberschuss p_Rohüb. Spalte N.
	 * 
	 * @return der Wert
	 */
	public double getRohUeb() {
		return rohUeb;
	}

	/**
	 * Anteil des übrigen Ergebnisses für das Neugeschäft. Spalte O.
	 * 
	 * @return der Wert
	 */
	public double getAnteilUebrigenErgebnisseNeugeschaeft() {
		return anteilUebrigenErgebnisseNeugeschaeft;
	}

	/**
	 * deterministische Projektion FLV. Spalte P.
	 *
	 * @return der Wert
	 */
	public double getDetProjektionFlv() {
		return detProjektionFlv;
	}

	/**
	 * FI Neuanlage Restlaufzeit RLZ_neuAnl. Spalte Q.
	 * 
	 * @return der Wert
	 */
	public int getRlzNeuAnl() {
		return rlzNeuAnl;
	}

	/**
	 * Verrechnungszeitraum fÃ¼r FI-BWR Ï„_Verechn. Spalte R.
	 * 
	 * @return der Wert
	 */
	public int getFiBwr() {
		return fiBwr;
	}

	/**
	 * MIL_W.Schalesi: Delta NVZ - BE Szenario, Pfad 0 und r_EK Spalte S. Spalte
	 * S
	 * 
	 * @return der Wert
	 */
	public double getdeltaNvz() {
		return deltaNvz;
	}

	/**
	 * MIL_W.Schalesi: fiBwrStandard. Spalte T
	 * 
	 * @return der Wert
	 */
	public int getFiBwrStandard() {
		return fiBwrStandard;
	}

	/**
	 * MIL_W.Schalesi: fiBwrAlternative. Spalte U
	 * 
	 * @return der Wert
	 */
	public int getFiBwrAlternative() {
		return fiBwrAlternative;
	}

}
