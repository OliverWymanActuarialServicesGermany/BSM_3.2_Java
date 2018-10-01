package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;

/**
 * Funktionen des Excel-Moduls <code>Kundenverhalten</code>.
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
public class Kundenverhalten {

	/**
	 * Berechnet die durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T einer Rechnungszinsgeneration. <br/>
	 * Funktionsname in Excel: s_basis.
	 * 
	 * @param lRkw
	 *            Garantierte Rückkaufleistungen im Jahr T
	 * @param drDet
	 *            Deterministische Deckungsrückstellung im Jahr T
	 * @param zinsGeneration
	 *            Rechnungszins in Basispunkten
	 * @param monat
	 *            Monat indem die Rückkaufleistungen anfallen (VU-Zeitpunkt)
	 * @return Stornowahrscheinlichkeit
	 */
	public static double sBasis(final double lRkw, final double drDet, final int zinsGeneration, final double monat) {
		if (drDet > 0.001) {
			return lRkw * Math.pow(1 + inProzent(zinsGeneration), 1 - monat / 12.0) / drDet;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Zinsabstand zwischen der Gesamtverzinsung einer Bestandsgruppe und dem 10 jährigen Zins einer
	 * Neuanlage zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: Delta_i.
	 * 
	 * @param zinsZehnjaehr
	 *            10 jähriger Zins einer Neuanlage
	 * @param rmZ
	 *            rechnungsmäßiger Zins
	 * @param rz
	 *            Rechnungszins in Basispunkten
	 * @param dekl
	 *            Deklaration
	 * @param drLockInV
	 *            Lock-In-Deckungsrückstellung Vorjahr
	 * @param t
	 *            Zeitpunkt
	 * @return der Wert
	 */
	public static double deltaI(final double zinsZehnjaehr, final double rmZ, final double rz, final double dekl,
			final double drLockInV, int t) {
		if (t == 0) {
			return zinsZehnjaehr - inProzent(rz);
		}
		if (drLockInV > 0.001) {
			return zinsZehnjaehr - (rmZ + dekl) / drLockInV;
		}
		return 0.0;
	}

	/**
	 * Berechnet die prozentuale Veränderung des Basisstornos zum Ende des Jahres T. <br/>
	 * Funktionsname in Excel: lambda_storno.
	 * 
	 * @param zinsSensitiv
	 *            Kennzeichen zinssensitiv
	 * @param vgDeltaI
	 *            Zinsabstand zwischen der Gesamtverzinsung einer Rechnungszinsgeneration ' und dem 10 jährigen Zins
	 *            einer Neuanlage zum Zeitpunkt T-1
	 * @param deltaTol
	 *            Toleranzschwelle für den Zinsausschlag
	 * @param deltaStorno
	 *            Intensität der Stornoveränderung in Abhängigkeit vom Zinsumfeld einer Rechnungszinsgeneration
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T
	 * @param drDet
	 *            Deterministische Deckungsrückstellung zum Zeitpunkt T
	 * @return Basisstorno
	 */
	public static double lambdaStorno(final String zinsSensitiv, final double vgDeltaI, final double deltaTol,
			final double deltaStorno, final double sBasis, final double drDet) {
		if (drDet <= 0.001 || !zinsSensitiv.equals("j")) {
			return 0.0;
		}
		double lambdaStorno = 100 * Math.signum(vgDeltaI) * Math.max(Math.abs(vgDeltaI) - deltaTol, 0.0) * deltaStorno;
		// ..und projeziere auf erlaubtes Intervall
		lambdaStorno = Math.max(lambdaStorno, -1.0);
		if (sBasis > 0.0001) {
			lambdaStorno = Math.min(lambdaStorno, 1 / sBasis);
		} else {
			lambdaStorno = 0.0;
		}

		return lambdaStorno;
	}

	/**
	 * Berechnet die prozentuale Veränderung der Kapitalwahl zum Ende des Jahres T. <br/>
	 * Funktionsname in Excel: lambda_KA.
	 * 
	 * @param zinssensitiv
	 *            Kennzeichen zinssensitiv
	 * @param deltaIV
	 *            Zinsabstand zwischen der Gesamtverzinsung einer Rechnungszinsgeneration und dem 10 jährigen Zins einer
	 *            Neuanlage zum Zeitpunkt T-1
	 * @param deltaTol
	 *            Toleranzschwelle für den Zinsausschlag
	 * @param DeltaKa
	 *            Intensität der Veränderung der Kapitalwahl in Abhängigkeit vom Zinsumfeld
	 * @param drDet
	 *            Deterministische Deckungsrückstellung im Jahr T-1
	 * @param ka
	 *            Kapitalabfindungen (nur von Rentenversicherungen) zum Zeitpunkt T
	 * @param lambdaStorno
	 *            Prozentuale Veränderung des Basisstornos zum Ende des Jahres T
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T
	 * @param LaKapWahlXsTm1
	 *            Leistungsanpassung Kapitalwahl Vorjahr
	 * @param LbwGarSonstErl
	 *            Leistungsbarwert sonstige Erlebensfallleistung
	 * @param rz
	 *            Rechnungszinsgeneration
	 * @param vuZeit
	 *            VU_Zahlungszeitpunkt Versicherungstechnik
	 * @return den Wert
	 */
	public static double lambdaKa(final String zinssensitiv, final double deltaIV, final double deltaTol,
			final double DeltaKa, final double drDet, final double ka, final double lambdaStorno, final double sBasis, final double LaKapWahlXsTm1, final double LbwGarSonstErl, final double rz, final double vuZeit) {
		double lambdaKa = 0.0;
		if (drDet <= 0.001) {
			return 0.0;
		}
		if (zinssensitiv.equals("j")) {
			lambdaKa = 100.0 * Math.signum(deltaIV) * Math.max(Math.abs(deltaIV) - deltaTol, 0.0) * DeltaKa;
			lambdaKa = Math.max(-1.0, lambdaKa);
			if (ka > 0.001) {
				lambdaKa = Math.min(lambdaKa, Math.max(((1.0 - lambdaStorno * sBasis) * Math.min(drDet, (1.0 + LaKapWahlXsTm1) * LbwGarSonstErl / Math.pow((1.0 + inProzent(rz)), (1.0 - vuZeit / 12.0)))) / ka, 0));
			}
		}
		return lambdaKa;
	}

	/**
	 * Berechnet die relative Bestandsänderung durch Storno zum Zeitpunkt T. <br/>
	 * Funktionsname in Excel: Lambda.
	 * 
	 * @param zinsSensitiv
	 *            Kennzeichen zinssensitiv
	 * @param vgLambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @param lambdaStorno
	 *            Prozentuale Veränderung des Basisstornos zum Ende des Jahres T
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T einer Rechnungszinsgeneration
	 * @return Bestandsänderung
	 */
	public static double lambda(final String zinsSensitiv, final double vgLambda, final double lambdaStorno,
			final double sBasis) {
		final double lambda;
		if (zinsSensitiv.equals("j")) {
			lambda = vgLambda * (1.0 - lambdaStorno * sBasis);
		} else {
			lambda = vgLambda;
		}
		return lambda < 0.00001 ? 0.0 : lambda;
	}

	/**
	 * Gibt den Startwert für die relative Bestandsänderung durch Storno aus (Startpunkt =Zeitpunkt 0). <br/>
	 * Funktionsname in Excel: Lambda_Startwert
	 * 
	 * @return den Wert
	 */
	public static double lambdaStartwert() {
		return 1.0;
	}

	/**
	 * Berechnet den Excess Betrag für die garantierten Rückkaufsleistunen zum Zeitpunkt T, d.h. den durch zusätzliche
	 * Stornierung im Jahr T zu zahlenden Betrag an garantierten Rückkaufsleistungen. <br/>
	 * Funktionsname in Excel: RKW_XS.
	 * 
	 * @param sBasis
	 *            Durchschnittliche Stornowahrscheinlichkeit zum Zeitpunkt T
	 * @param lambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T
	 * @param vgLambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @param lambdaStorno
	 *            Prozentuale Veränderung des Basisstornos zum Ende des Jahres T
	 * @param drDet
	 *            Deckungsrückstellung für änfänglich garantierte Leistungen im Jahr T
	 * @param vgLeLockInAggrFLV
	 *            Garantierte Erhöhung der anfänglich garantierten Leistungen zum Zeitpunkt T
	 * @param lbwGar
	 *            Barwert der zukünftigen, deterministisch projezierten Leistungen zum Ende des Jahres T
	 * @param vgLaKapWahlXsAggr
	 *            Leistungsanpassung der zukünftigen Erlebensfallleistungen durch pfadabhängige Kapitalwahländerung bis
	 *            zum Jahr T
	 * @param lbwSonstErl
	 *            Barwert der zukünftigen, deterministisch projezierten sonstigen Erlebensfallleistungen zum Ende des
	 *            Jahres T
	 * @param zinsGeneration
	 *            Rechnungszins in Basispunkten
	 * @param monat
	 *            Monat der Zahlungseingänge (VU-Zeitpunkt)
	 * @return Betrag
	 */
	public static double rkwXs(final double sBasis, final double lambda, final double vgLambda,
			final double lambdaStorno, final double drDet, final double vgLeLockInAggrFLV, final double lbwGar,
			final double vgLaKapWahlXsAggr, final double lbwSonstErl, final double zinsGeneration, final double monat) {
		if (lambda != 0.0) {
			return vgLambda * lambdaStorno * sBasis
					* (drDet + (Double.isNaN(vgLeLockInAggrFLV) ? 0.0 : vgLeLockInAggrFLV) * lbwGar / lambda
							+ (Double.isNaN(vgLaKapWahlXsAggr) ? 0.0 : vgLaKapWahlXsAggr) * lbwSonstErl)
					/ Math.pow(1 + inProzent(zinsGeneration), 1 - monat / 12.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Excess Betrag für die garantierten Leistungen für Kapitalabfindungen zum Zeitpunkt T, d.h. den
	 * durch zusätzliche Kapitalwahl im Jahr T zu zahlenden Betrag an garantierten Kapitalabfindungen. <br/>
	 * Funktionsname in Excel: KA_gar_XS.
	 * 
	 * @param kaGar
	 *            Garantierte Leistungen für Kapitalabfindungen im Jahr T
	 * @param vgLambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T
	 * @param lambdaKa
	 *            Prozentuale Veränderung der Kapitalwahl zum Ende des Jahres T
	 * @return Betrag
	 */
	public static double kaGarXs(final double kaGar, final double vgLambda, final double lambdaKa) {
		return kaGar * vgLambda * lambdaKa;
	}

	/**
	 * Berechnet die relative Leistungsanpassung der zukünftigen Erlebensfallleistungen durch pfadabhängige
	 * Kapitalwahländerung bis zum Jahr T. <br/>
	 * Funktionsname in Excel: LA_KapWahlXS_Aggr.
	 * 
	 * @param vgLaKapWahlXsAggr
	 *            Leistungsanpassung der zukünftigen Erlebensfallleistungen durch pfadabhängige Kapitalwahländerung bis
	 *            zum Jahr T-1
	 * @param kaGarXs
	 *            Excess Betrag für die garantierten Rückkaufsleistunen zum Zeitpunkt T
	 * @param lbwGarSonstErl
	 *            Barwert der zukünftigen, deterministisch projezierten sonstigen Erlebensfallleistungen
	 * @param rz
	 *            Rechnungszins in Basispunkten
	 * @param lambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T
	 * @param monat
	 *            Monat der Zahlungseingänge (VU-Zeitpunkt)
	 * @return Leistungsanpassung
	 */
	public static double laKapWahlXsAggr(final double vgLaKapWahlXsAggr, final double kaGarXs,
			final double lbwGarSonstErl, final double rz, final double lambda, final double monat) {
		if (lbwGarSonstErl > 0.001 && lambda != 0) {
			return nanZero(vgLaKapWahlXsAggr)
					- kaGarXs * Math.pow(1.0 + inProzent(rz), 1.0 - monat / 12.0) / (lambda * lbwGarSonstErl);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Cashflow der stochastischen Kosten im Jahr T. <br/>
	 * Funktionsname in Excel: Kosten_stoch.
	 * 
	 * @param kostenDet
	 *            Kosten im Jahr T, deterministisch
	 * @param lambdaV
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @return Cashflow
	 */
	public static double kostenStoch(final double kostenDet, final double lambdaV) {
		return kostenDet * lambdaV;
	}

	/**
	 * Berechnet den Cashflow der stochastischen Risikoüberschüsse im Jahr T. <br/>
	 * Funktionsname in Excel: RisikoUeb_stoch.
	 * 
	 * @param risikoUebDet
	 *            Risikoüberschüsse im Jahr T, deterministisch
	 * @param lambdaV
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @return Cashflow
	 */
	public static double risikoUebStoch(final double risikoUebDet, final double lambdaV) {
		return risikoUebDet * lambdaV;
	}

	/**
	 * Berechnet den Cashflow der stochastischen Kostenüberschüsse im Jahr T. <br/>
	 * Funktionsname in Excel: KostenUeb_stoch.
	 * 
	 * @param uebrigesErgebnis
	 *            Kostenüberschüsse im Jahr T, deterministisch
	 * @param vgLambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @return Cashflow
	 */
	public static double kostenUebStoch(final double uebrigesErgebnis, final double vgLambda) {
		return uebrigesErgebnis * vgLambda;
	}

	/**
	 * Berechnet den stochastischen CF von Erstversicherer an den Rückversicherer im Jahr T.<br/>
	 * Funktionsname in Excel: CF_RV_Stoch.
	 * 
	 * @param cfRvDet
	 *            CF EVU -> RVU (nicht LE-abhängig)
	 * @param lambdaV
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @return der Wert
	 */
	public static double cfRvStoch(final double cfRvDet, final double lambdaV) {
		return cfRvDet * lambdaV;
	}

	/**
	 * Berechnet den stochastischen Zinsratenzuschläge im Jahr T. <br/>
	 * Funktionsname in Excel: ZiRaZu_stoch.
	 * 
	 * @param zinsratenZuschlag
	 *            Zinsratenzuschläge im Jahr T, deterministisch
	 * @param vgLambda
	 *            Relative Bestandsänderung durch Storno zum Zeitpunkt T-1
	 * @return Zinsratenzuschläge
	 */
	public static double ziRaZuStoch(final double zinsratenZuschlag, final double vgLambda) {
		return zinsratenZuschlag * vgLambda;
	}

	/**
	 * Berechnet den Barwert der zukünftigen, deterministisch projezierten sonstigen Erlebensfallleistungen bezüglich
	 * des Rechnungszinses zum Ende des Jahres T (deterministischer Leistungsbarwert). Dabei wird davon ausgegangen,
	 * dass die zukünfftigen Zahlungen in den Jahren T+1,..., omega im Monat monat eingehen. <br/>
	 * Funktionsname in Excel: LBW_sonstErl.
	 * 
	 * @param rz
	 *            Rechnungszins
	 * @param nfLbwSonstErl
	 *            Deterministischer Leistungsbarwert im Folgejahr (T+1)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            Projektionsende
	 * @param lwbSonstErlN
	 *            Deterministisch projezierte, garantierte sonstigen Erlebensfallleistungen im Folgejahr (T+1)
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return Barwert
	 */
	public static double lbwSonstErl(final double rz, final double nfLbwSonstErl, final int t, final int omega,
			final double lwbSonstErlN, final double monat) {
		return SonstigeFormeln.barwert(rz, nfLbwSonstErl, t, omega, lwbSonstErlN, monat);
	}

	/**
	 * Berechnet die garantierten Leistungen, ohne sonstige Erlebensfallleistungen. <br/>
	 * Funktionsname in Excel: L_garantiert_o_sonstErl.
	 * 
	 * @param lTod
	 *            Garantierte Leistungen im Todesfall
	 * @param lKa
	 *            Garantierte Leistungen für Kapitalabfindungen
	 * @param lRkw
	 *            Garantierte Rückkaufswerte
	 * @return garantierte Leistungen
	 */
	public static double lGarantiertOSonstErl(final double lTod, final double lKa, final double lRkw) {
		return lTod + lKa + lRkw;
	}

	/**
	 * Berechnet den Barwert der zukünftigen, deterministisch projezierten garantierten Leistungen, ohne sonstige
	 * Erlebensfallleistungen bezüglich des Rechnungszinses zum Ende des Jahres T (deterministischer Leistungsbarwert).
	 * Dabei wird davon ausgegangen, dass die zukünfftigen Zahlungen in den Jahren T+1,..., omega im Monat monat
	 * eingehen. <br/>
	 * Funktionsname in Excel: LBW_gar_o_SonstErl.
	 * 
	 * @param rz
	 *            Rechnungszins
	 * @param lwbSonstErlB
	 *            Deterministischer Leistungsbarwert im Folgejahr (T+1)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param omega
	 *            Projektionsende
	 * @param lGarantiertOSonstErl
	 *            Deterministisch projezierte, garantierte Leistungen, ohne sonstige Erlebensfallleistungen im Folgejahr
	 *            (T+1)
	 * @param monat
	 *            Monat der Zahlungseingänge
	 * @return Barwert
	 */
	public static double lbwGarOSonstErl(final double rz, final double lwbSonstErlB, final int t, final int omega,
			final double lGarantiertOSonstErl, final double monat) {
		return SonstigeFormeln.barwert(rz, lwbSonstErlB, t, omega, lGarantiertOSonstErl, monat);
	}

}
