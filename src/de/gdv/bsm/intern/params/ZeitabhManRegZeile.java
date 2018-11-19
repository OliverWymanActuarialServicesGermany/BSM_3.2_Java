package de.gdv.bsm.intern.params;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

/**
 * Eine Zeile der zeitabh√§ngigen Management-Regeln des Unternehmens. Abbild einer Zeile des Blattes
 * <code>zeitabh.ManReg</code>.
 * 
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zust√§ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlie√üen, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende R√ºckmeldungen w√ºrde der GDV
 * begr√º√üen. Der GDV √ºbernimmt aber keine Haftung f√ºr die fehlerfreie Funktionalit√§t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlie√ülich aller Tabellen, Grafiken und Erl√§uterungen sind urheberrechtlich
 * gesch√ºtzt. Die ausschlie√ülichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell ¬© GDV 2016</b>
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
	// OW_F.Wellens
	private final double[] anteilUebrigenErgebnisseNeugeschaeft = new double[39];

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
		// OW_F.Wellens
		for (int i = 0; i < 38; ++i) {
			anteilUebrigenErgebnisseNeugeschaeft[i] = zeile.getDouble(14+i);
		}
		detProjektionFlv = zeile.getDouble(54);
		rlzNeuAnl = zeile.getInt(55);
		fiBwr = zeile.getInt(56);
		// MIL_W.Schalesi
		deltaNvz = zeile.getDouble(57);
		fiBwrStandard = zeile.getInt(58);
		fiBwrAlternative = zeile.getInt(59);

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
	 * Grund¸berschuss aus Risiko- und ¸brigen Ergebnissen, relevent f¸r Bestimmung der restlichen Deklaration, in %
	 * Spalte B
	 * 
	 * @return Grund¸berschuss aus Risiko- und ¸brigen Ergebnissen
	 */
	public double getGrundUeberschuss() {
		return grundUeberschuss;
	}

	/**
	 * Ziel-Barauszahlung in Anteilen des Beitrages, Klassisches Gesch√§ft. Spalte C
	 * 
	 * @return Ziel-Barauszahlung in Anteilen des Beitrages, Klassisches Gesch√§ft
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
	 * Ziel-Zuf¸hrung zum S√úAF bei der Deklaration p_S‹Fzuf_min. Spalte E.
	 * 
	 * @return der Wert
	 */
	public double getSueafZufMin() {
		return sueafZufMin;
	}

	/**
	 * Anteil der S‹AF-Zuf¸hrung an der Deklaration gesamt p_S‹AFZuf. Spalte F.
	 * 
	 * @return der Wert
	 */
	public double getSueafZuf() {
		return sueafZuf;
	}

	/**
	 * MIL_W.Schalesi: Anteil der alternativen S‹AF-Zuf¸hrung an der Deklaration gesamt p_S‹AFZuf. Spalte G.
	 * 
	 * @return der Wert
	 */
	public double getSueafZufAlternative() {
		return sueafZufAlternative;
	}
	
	/**
	 * Anteil des p_RfB-‹berlaufs, der zu Erhˆhung der Zieldeklaration verwendet
	 * wird p_fRfB_‹berlauf. Spalte H.
	 * 
	 * @return der Wert
	 */
	public double getFrfbUeberlauf() {
		return frfbUeberlauf;
	}

	/**
	 * MIL_W.Schalesi: Anteil der alternativen des p_RfB-‹berlaufs, der zu Erhˆhung der Zieldeklaration verwendet
	 * wird p_fRfB_‹berlauf. Spalte I.
	 * 
	 * @return der Wert
	 */
	public double getFrfbUeberlaufAlternative() {
		return frfbUeberlaufAlternative;
	}
	
	/**
	 * ¬ß56b VAG fRfB- Entnahme (Ja =1, Nein =0). Spalte J.
	 * 
	 * @return der Wert
	 */
	public int getRfbEntnahme() {
		return rfbEntnahme;
	}

	/**
	 * ¬ß56b VAG S‹AF- Entnahme (Ja =1, Nein =0). Spalte K.
	 * 
	 * @return der Wert
	 */
	public int getSueafEntnahme() {
		return sueafEntnahme;
	}

	/**
	 * Zielanteil des Eigenkapitals an der Deckungsr¸ckstellung EKZiel. Spalte L.
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
	 * Zielbeteiligung am Roh¸berschuss in % vom Roh¸berschuss p_Roh¸b. Spalte N.
	 * 
	 * @return der Wert
	 */
	public double getRohUeb() {
		return rohUeb;
	}

	/**
	 * Anteil des ¸brigen Ergebnisses f¸r das Neugesch‰ft. Spalte O.
	 * 
	 * @return der Wert
	 */
	// OW_F.Wellens
	public double[] getAnteilUebrigenErgebnisseNeugeschaeft() {
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
	 * Verrechnungszeitraum f√ºr FI-BWR œÑ_Verechn. Spalte R.
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
