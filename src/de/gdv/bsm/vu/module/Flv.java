package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;

import de.gdv.bsm.vu.berechnung.FlvZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;

/**
 * Rechenfunktionen für die FLV.
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
public class Flv {

	/**
	 * Ermittelt die relative kapitalmarktabhängige Wertentwicklung des Fonds im 'Jahr t in % des Fondguthabens. <br/>
	 * Funktionsname in Excel: WertEntwicklung_stoch.
	 * 
	 * @param forwardrate1j
	 *            Die einjährige, kapitalmarktabhängige Forwardrate im Jahr t
	 * @return die Wertentwicklung
	 */
	public static double wertEntwicklungStoch(final double forwardrate1j) {
		if (Double.isNaN(forwardrate1j)) {
			return 1.0;
		} else {
			return 1.0 + forwardrate1j;
		}
	}

	/**
	 * Deterministisches Fondguthaben zum Zeitpunkt t. Entspricht dem stochastischen Fondguthaben zum Startzeitpunkt der
	 * Projektion. Fondguthaben_det<br/>
	 * Funktionsname in Excel: .
	 * 
	 * @param fondguthabenDet
	 * @return Deterministisches Fondguthaben zum Zeitpunkt t
	 */
	public static double fondguthabenDet(final double fondguthabenDet) {
		return fondguthabenDet;
	}

	/**
	 * Berechnet die Entwicklung des Fondguthabens. Berechnung entsprechend der durch die Kapitalmarktszenarien
	 * induzierten Wertentwicklungen und den stochastisch projizierten Beitrags- und Leistungs-Cashflows. <br/>
	 * Funktionsname in Excel: Fondguthaben_stoch.
	 * 
	 * @param fondguthabenStochZp
	 *            Stochastisches Fondguthaben im vorherigen Jahr
	 * @param wertEntwicklung
	 *            Einjährige Wertentwicklung des Fonds, ohne Berücksichtigung der Cashflows
	 * @param beitragRenteStoch
	 *            Stochastisch projiziertes zur Verrentung kommenendenes Kapital im Jahr t
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return das Guthaben
	 */
	public static double fondguthabenStoch(final double fondguthabenStochZp, final double wertEntwicklung,
			final double beitragRenteStoch, final double monat) {
		return (fondguthabenStochZp - beitragRenteStoch) * Math.pow(wertEntwicklung, (1 - monat / 12));
	}

	/**
	 * Berechnet die Entwicklung des Fondguthabens. Berechnung entsprechend der durch die Kapitalmarktszenarien
	 * induzierten Wertentwicklungen und den stochastisch projizierten Beitrags- und Leistungs-Cashflows zum VU
	 * individuellen ZP. <br/>
	 * Funktionsname in Excel: Fondguthaben_stoch_ZP.
	 * 
	 * @param fondguthabenStochV
	 *            Stochastisches Fondguthaben im vorherigen Jahr
	 * @param wertEntwicklung
	 *            Einjährige Wertentwicklung des Fonds, ohne Berücksichtigung der Cashflows
	 * @param beitragAufschubStoch
	 *            Stochastisch projizierte Beiträge in der Aufschubsphase im Jahr t
	 * @param kostenAufschubStoch
	 *            Stochastisch projizierte Kosten in der Aufschubsphase im Jahr t
	 * @param kFvAufschubStoch
	 *            Stochastisch projizierte Kosten für die Fondverwaltung im Jahr t
	 * @param lTodAufschubStoch
	 *            Stochastisch projizierte Todesfallleistungen in der Aufschubsphase im Jahr t
	 * @param lSonstErlAufschubStoch
	 *            Stochastisch projizierte Sonstige Erlebensfallleistungen in der Aufschubsphase im Jahr t
	 * @param lKaAufschubStoch
	 *            Stochastisch projizierte Kapitalabfindungen in der Aufschubsphase im Jahr t
	 * @param lRkwAufschubStoch
	 *            Stochastisch projizierte Rückkaufsleistungen in der Aufschubsphase im Jahr t
	 * @param reAufschub
	 *            Risikoergebnis in der Aufschubsphase im Jahr t
	 * @param ueEAufschub
	 *            Übriges Ergebnis in der Aufschubsphase im Jahr t
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return das Fondguthaben
	 */
	public static double fondguthabenStochZp(final double fondguthabenStochV, final double wertEntwicklung,
			final double beitragAufschubStoch, final double kostenAufschubStoch, final double kFvAufschubStoch,
			final double lTodAufschubStoch, final double lSonstErlAufschubStoch, final double lKaAufschubStoch,
			final double lRkwAufschubStoch, final double reAufschub, final double ueEAufschub, final double monat) {
		return fondguthabenStochV * Math.pow(wertEntwicklung, (monat / 12))
				- kFvAufschubStoch / Math.pow(wertEntwicklung, (1 - monat / 12)) + beitragAufschubStoch
				- kostenAufschubStoch - lTodAufschubStoch - lSonstErlAufschubStoch - lKaAufschubStoch
				- lRkwAufschubStoch - reAufschub - ueEAufschub;
	}

	/**
	 * Stochastisch projizierte Kosten für die Fondverwaltung. <br/>
	 * Funktionsname in Excel: K_FV_Aufschub_stoch.
	 * 
	 * @param kFvAufschubDet
	 *            Deterministisch projizierte Kosten für die Fondverwaltung zum Zeitpunkt t
	 * @param fondguthabenDet
	 *            Deterministisch projiziertes Fondguthaben zum Zeitpunkt t
	 * @param fondguthabenStoch
	 *            Stochastisch projiziertes Fondguthaben zum Zeitpunkt t
	 * @param wertEntwicklungDet
	 *            Deterministische Wertentwicklung des Fonds im Jahr t
	 * @param wertEntwicklungStoch
	 *            Stochastische Wertentwicklung des Fonds ohne Berücksichtigung der Cashflows im Jahr t
	 * @return die Kosten
	 */
	public static double kFvAufschubStoch(final double kFvAufschubDet, final double fondguthabenDet,
			final double fondguthabenStoch, final double wertEntwicklungDet, final double wertEntwicklungStoch) {
		final double detWE = 1 + wertEntwicklungDet; // Deterministische 2%-Wertentwicklung
		if (fondguthabenDet >= 0.001) {
			return kFvAufschubDet * fondguthabenStoch * wertEntwicklungStoch / (fondguthabenDet * detWE);
		}
		return 0.0;
	}

	/**
	 * Stochastische Fondentwicklung. Relativ zur deterministischen 2% Projektion zum Monat der Zahlungseingänge. <br/>
	 * Funktionsname in Excel: Fondentwicklung_rel_stoch_VU_ZP.
	 * 
	 * @param fondguthabenDet
	 *            Deterministisch projiziertes Fondguthaben zum Zeitpunkt t
	 * @param fondguthabenStoch
	 *            Stochastisch projiziertes Fondguthaben zum Zeitpunkt t
	 * @param wertEntwicklungDet
	 *            Deterministische Wertentwicklung des Fonds
	 * @param wertEntwicklungStoch
	 *            Stochastische Wertentwicklung des Fonds ohne Berücksichtigung der Cashflows im Jahr t
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return die Entwicklung
	 */
	public static double fondentwicklungRelStochVuZp(final double fondguthabenDet, final double fondguthabenStoch,
			final double wertEntwicklungDet, final double wertEntwicklungStoch, final double monat) {
		final double detWE = 1 + wertEntwicklungDet;
		if (fondguthabenDet >= 0.001) {
			return (fondguthabenStoch / fondguthabenDet) * Math.pow(wertEntwicklungStoch / detWE, monat / 12);
		}
		return 0.0;
	}

	/**
	 * Stochastisch projizierte Todesfallleistungen in der Aufschubsphase im Jahr t. <br/>
	 * Funktionsname in Excel: L_Tod_Aufschub_stoch.
	 * 
	 * @param lTodAufschubDet
	 *            Deterministisch projizierte Todesfallleistungen in der Aufschubsphase im Jahr t
	 * @param fondentwicklungRelStochVuZp
	 *            Stochastische Fondentwicklung. Relativ zur deterministischen 2% Projektion zum Monat der
	 *            Zahlungseingänge
	 * @return die Todesfallleistung
	 */
	public static double lTodAufschubStoch(final double lTodAufschubDet, final double fondentwicklungRelStochVuZp) {
		return lTodAufschubDet * fondentwicklungRelStochVuZp;
	}

	/**
	 * Stochastisch projizierte Sonstige Erlebensfallleistungen in der Aufschubsphase im Jahr t. <br/>
	 * Funktionsname in Excel: L_SonstErl_Aufschub_stoch.
	 * 
	 * @param lSonstErlAufschubDet
	 *            Deterministisch projizierte Sonstige Erlebensfallleistungen in der Aufschubsphase im Jahr t
	 * @param fondentwicklungRelStochVuZp
	 *            Stochastische Fondentwicklung. Relativ zur deterministischen 2% Projektion zum Monat der
	 *            Zahlungseingänge
	 * @return die Leistungen
	 */
	public static double lSonstErlAufschubStoch(final double lSonstErlAufschubDet,
			final double fondentwicklungRelStochVuZp) {
		return lSonstErlAufschubDet * fondentwicklungRelStochVuZp;
	}

	/**
	 * Stochastisch projizierte Kapitalabfindungen in der Aufschubsphase im Jahr t. <br/>
	 * Funktionsname in Excel: L_KA_Aufschub_stoch.
	 * 
	 * @param lKaAufschubDet
	 *            Deterministisch projizierte Kapitalabfindungen in der Aufschubsphase im Jahr t
	 * @param fondentwicklungRelStochVuZp
	 *            Stochastische Fondentwicklung. Relativ zur deterministischen 2% Projektion zum Monat der
	 *            Zahlungseingänge
	 * @return die Kapitalabfindung
	 */
	public static double lKaAufschubStoch(final double lKaAufschubDet, final double fondentwicklungRelStochVuZp) {
		return lKaAufschubDet * fondentwicklungRelStochVuZp;
	}

	/**
	 * Stochastisch projizierte Rückkaufsleistungen in der Aufschubsphase im Jahr t. <br/>
	 * Funktionsname in Excel: L_RKW_Aufschub_stoch.
	 * 
	 * @param lRkwAufschubDet
	 *            Deterministisch projizierte Rückkaufsleistungen in der Aufschubsphase im Jahr t
	 * @param fondentwicklungRelStochVuZp
	 *            Stochastische Fondentwicklung. Relativ zur deterministischen 2% Projektion zum Monat der
	 *            Zahlungseingänge
	 * @return die Rückkaufsleistungen
	 */
	public static double lRkwAufschubStoch(final double lRkwAufschubDet, final double fondentwicklungRelStochVuZp) {
		return lRkwAufschubDet * fondentwicklungRelStochVuZp;
	}

	/**
	 * Stochastisch projiziertes zur Verrentung kommenendenes Kapital im Jahr t. <br/>
	 * Funktionsname in Excel: Beitrag_Rente_stoch.
	 * 
	 * @param beitragRenteDet
	 *            Deterministisch projiziertes zur Verrentung kommenendenes Kapital im Jahr t
	 * @param fondguthabenStochZp
	 *            Stochastisch projiziertes Fondguthabens
	 * @param fondguthabenDet
	 *            Deterministisch projiziertes Fondguthaben zum Zeitpunkt t
	 * @param wertEntwicklungDet
	 *            Deterministische Wertentwicklung des Fonds im Jahr t
	 * @param t
	 *            Zeitpunkt
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return der Beitrag
	 */
	public static double beitragRenteStoch(final double beitragRenteDet, final double fondguthabenStochZp,
			final double fondguthabenDet, final double wertEntwicklungDet, final long t, double monat) {
		if (t > 0 && beitragRenteDet >= 0.001) {
			final double FG_det = fondguthabenDet / Math.pow((1 + wertEntwicklungDet), (1 - monat / 12))
					+ beitragRenteDet;
			return beitragRenteDet * fondguthabenStochZp / FG_det;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Cashflow der stochastischen Beitraege im Jahr T. Berücksichtigt dabei kapitalmarktabhängiges
	 * Stornoverhalten.<br/>
	 * Funktionsname in Excel : Beitraege_stoch
	 *
	 * @param Lambda_v
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @param Beitrag_det
	 *            Beitraege im Jahr T, deterministisch
	 * @param kategorie
	 *            "Klassik" für das Klassiche Geschäft, "FLV" für das Fondgebundene Geschäft
	 * @param deckungsstock
	 *            Deckungsstock
	 * @param zeile
	 *            zugehörige Flvzeile
	 * @return der Wert
	 */
	public static double beitraegeStoch(final double Lambda_v, final double Beitrag_det, final String kategorie,
			final String deckungsstock, final FlvZeile zeile) {

		if (!kategorie.equals("FLV")) {
			return Lambda_v * Beitrag_det;
		}
		if (deckungsstock.equals(RzgZeile.DECKUNGS_STOCK_KDS)) {
			return zeile.getBeitragRenteStoch();

		}
		return 0.0;
	}

	/**
	 * Aggregierte Anpassung der anfänglich garantierten Leistungen durch Abweichung der Fondentwicklung von der
	 * deterministischen 2%-Projektion. Für das klassische Geschäft wird dieser Wert auf null gesetzt. <br/>
	 * Funktionsname in Excel: LeistungsAnpassung_FLV.
	 * 
	 * @param leistungsAnpassungFlvV
	 *            Garantierte Leistungsanpassung durch Abweichung der Fondentwicklung von der deterministischen
	 *            Projektion zum ZP T-1
	 * @param beitraegeDet
	 *            Deterministische Projektion des zur Verrentung kommenenden Kapitals
	 * @param beitraegeStoch
	 *            Kapitalmarktabhängige Projektion des zur Verrentung kommenenden Kapitals
	 * @param lbw
	 *            Der Barwert der garantierten Leistungscashflows, gebildet mit dem Rechnungszins
	 * @param rz
	 *            Rechnungszins der Bestandsgruppe
	 * @param kategorie
	 *            "Klassik" für das Klassiche Geschäft, "FLV" für das Fondgebundene Geschäft
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @return der Wert
	 */
	public static double leistungsAnpassungFlv(final double leistungsAnpassungFlvV, final double beitraegeDet,
			final double beitraegeStoch, final double lbw, final double rz, final String kategorie, final double monat,
			final int t) {
		if (kategorie.equals("FLV") && lbw > 0.001 && t > 0) {
			return ((beitraegeStoch - beitraegeDet) * Math.pow(1.0 + inProzent(rz), (1.0 - monat / 12.0)) / lbw)
					+ nanZero(leistungsAnpassungFlvV);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die (aggregierte) garantierte Anpassung der anfänglich garantierten Leistungen zum Zeitpunkt T. Enthält
	 * Leistungserhöhung aus Lock-In und Leistungsanpassung durch stochastische Fondentwicklung. <br/>
	 * Funktionsname in Excel: LE_LockIn_Aggr_FLV.
	 * 
	 * @param LELockInAggr
	 *            Garantierte Leistungserhöhung durch LockIn zum Zeitpunkt T
	 * @param leistungsAnpassungFlv
	 *            Garantierte Leistungsanpassung durch stochastische Fondentwicklung
	 * @return der Wert
	 */
	public static double leLockInAggrFlv(final double LELockInAggr, final double leistungsAnpassungFlv) {
		return LELockInAggr + leistungsAnpassungFlv;
	}
}
