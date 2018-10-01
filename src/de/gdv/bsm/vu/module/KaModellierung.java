package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.nanZero;

import java.util.List;

import de.gdv.bsm.intern.params.ZeitabhManReg;
import de.gdv.bsm.intern.szenario.PfadZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;

/**
 * Funktionen des Excel-Moduls <code>KA-Modellierung</code>.
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
public class KaModellierung {

	/**
	 * Berechnet zum Zeitpunkt t Lock-in-Aufschlag auf die zum Bewertungsstichtag garantierten Leistugnen. <br/>
	 * Funktionsname in Excel: Lock_in_faktor.
	 * 
	 * @param t
	 *            Projektionszeitpunkt
	 * @param lTod
	 *            Leistungen beim Tod
	 * @param lKa
	 *            Kapitalabfindungen, nur Rentenversicherung
	 * @param lErlFall
	 *            Sonstige Erlebensfallleistungen
	 * @param lRkw
	 *            Rückkauf
	 * @param lGarLockIn
	 *            Garantierten Leistungen inkl. Lock-In
	 * @return Lock-in-Aufschlag
	 */
	public static double lockInFaktor(final int t, final double lTod, final double lKa, final double lErlFall,
			final double lRkw, final double lGarLockIn) {
		if (t == 0) {
			return 1.0;
		} else {
			if (lGarLockIn > 0.0) {
				return lGarLockIn / (lTod + lKa + lErlFall + lRkw);
			} else {
				return 1.0;
			}
		}
	}

	/**
	 * berechnet zum Zeitpunkt t Anpassungsfaktor auf die zum Bewertungsstichtag garantierten restlichen Cashflow. Der
	 * Anpassungsfaktor verändert sich aufgrund des stochastischen Kundenverhalten
	 * 
	 * @param t
	 *            Projektionszeitpunkt
	 * @param K
	 *            Kosten
	 * @param B
	 *            Beiträge
	 * @param K_stoch
	 *            Risikoergebnis
	 * @param B_stoch
	 *            übriges Ergebnis
	 * @return den Wert
	 */
	public static double aVn(final int t, final double K, final double B, final double K_stoch, final double B_stoch) {
		if ((K + B) > 0.001) {
			return (K_stoch + B_stoch) / (K + B);
		} else {
			return 1.0;
		}
	}

	/**
	 * Berechnet zum Zeitpunkt t stoch. Anpassungsfaktor für KA-Aufwendungen. Der Anpassungsfaktor verändert sich
	 * aufgrund des stochastischen Kapitalmarktpfaden. <br/>
	 * Funktionsname in Excel: A_KA_Aufwendungen.
	 * 
	 * @param t
	 *            Projektionszeitpunkt
	 * @param drDet
	 *            det. Deckungsrückstellung
	 * @param drVorDekl
	 *            Deckungsrückstellung vor Deklaration
	 * 
	 * @return Anpassungsfaktor für KA-Aufwendungens
	 */
	public static double aKaAufwendungen(final long t, final double drDet, final double drVorDekl) {
		if (drDet != 0.0) {
			return drVorDekl / drDet;
		} else {
			return 1.0;
		}
	}

	/**
	 * Berechnet den Diskontfaktor zu einem Spotzins und der zugehörigen Laufzeit. <br/>
	 * Funktionsname in Excel: DF.
	 * 
	 * @param vgSpot
	 *            Spotzins Vorjahr, jährlicher Zins bei Anlage von T Jahren
	 * @param spot
	 *            Spotzins, jährlicher Zins bei Anlage von T Jahren
	 * @param t
	 *            Laufzeit
	 * @param vuZeitpunkt
	 *            Zeitpunkt, zu welchem die CFs fällig sind
	 * @return Diskontfaktor
	 */
	public static double df(final double vgSpot, final double spot, final int t, final double vuZeitpunkt) {
		if (t == 1) {
			return 1.0 / Math.pow(1.0 + spot, t * vuZeitpunkt / 12.0);
		} else {
			return 1.0 / Math.pow(1.0 + vgSpot, (t - 1) * (1 - vuZeitpunkt / 12.0)) * 1
					/ Math.pow(1 + spot, t * vuZeitpunkt / 12.0);
		}
	}

	/**
	 * Berechnet den Aufzinsungsfaktor bis zum Endes des Jahres. <br/>
	 * Funktionsname in Excel: df_halbesjahr.
	 * 
	 * @param vgSpot
	 *            Spotzins Vorjahr, jährlicher Zins bei Anlage von T Jahren
	 * @param spot
	 *            Spotzins, jährlicher Zins bei Anlage von T Jahren
	 * @param t
	 *            Laufzeit
	 * @param vuZeitpunkt
	 *            Zeitpunkt, zu welchem die CFs fällig sind
	 * @return Aufzinsungsfaktor
	 */
	public static double dfHalbesjahr(final double vgSpot, final double spot, final int t, final double vuZeitpunkt) {
		if (t == 1)
			return Math.pow(1 + spot, t - vuZeitpunkt / 12.0);
		else
			return Math.pow(Math.pow(1 + spot, t) / Math.pow(1 + vgSpot, t - 1), 1.0 - vuZeitpunkt / 12.0);
	}

	/**
	 * Berechnet den Cashflow der FI Titel unter Einbezug der Ausfallwahrscheinlichkeit. <br/>
	 * Funktionsname in Excel: CF_FI_mitAusfall.
	 * 
	 * @param cfFioAusfall
	 *            Cashflow der FI Titel
	 * @param pAusfall
	 *            Jährliche Ausfallwahrscheinlichkeit
	 * @param t
	 *            Zeitpunkt
	 * @param vuZeitpunkt
	 *            Zeitpunkz, zu welchem die CFs fällig sind
	 * @return Cashflow
	 */
	public static double cfFiMitAusfall(final double cfFioAusfall, final double pAusfall, final int t,
			final double vuZeitpunkt) {
		return cfFioAusfall * Math.pow(1.0 - pAusfall, t - 1.0 + vuZeitpunkt / 12.0);
	}

	/**
	 * Berechnet den Kapitalertrag unter Einbezug der Ausfallwahrscheinlichkeit. <br/>
	 * Funktionsname in Excel: KE_mitAusfallAbschr.
	 * 
	 * @param kfFioAusfall
	 *            Mittlere Erträge aus dem aktuellen FI-Bestand
	 * @param bwFiAktoAusfall
	 *            Buchwert der FI Titel ohne Ausfall
	 * @param q
	 *            Ausfallwahrscheinlichkeit
	 * @param t
	 *            Zeit
	 * @param vuZeitpunkt
	 *            VU-spezifischer Zeitpunkt für KA
	 * @return Kapitalertrag
	 */
	public static double kemitAusfallAbschr(final double kfFioAusfall, final double bwFiAktoAusfall, final double q,
			final int t, final double vuZeitpunkt) {
		if (t == 1)
			return kfFioAusfall * Math.pow(1.0 - q, t - 1.0 + vuZeitpunkt / 12.0)
					- (1.0 - Math.pow(1 - q, vuZeitpunkt / 12.0)) * bwFiAktoAusfall;
		else
			return kfFioAusfall * Math.pow(1.0 - q, t - 1.0 + vuZeitpunkt / 12.0)
					- q * (Math.pow(1.0 - q, t - 2.0 + vuZeitpunkt / 12.0)) * bwFiAktoAusfall;
	}

	/**
	 * Berechnet den Kapitaertrag am Ende des Jahres aus dem aktuellen FI-Bestand. <br/>
	 * Funktionsname in Excel: KE_aktBestand_JE.
	 * 
	 * @param kemitAusfallAbschr
	 *            Kapitalertrag aus dem akt.FI-Bestand
	 * @param cfFimitAusfall
	 *            Cashflow mit Ausfall, VU spezifischer Zeitpunkt
	 * @param cfFimitAusfallJahresende
	 *            Cashflow mit Ausfall, Ende des Jahres
	 * @param rapZinsen
	 *            Rechnungsabgrenzungsposten: Stückzins aus dem Vorjahr (des Vorjahres)
	 * @param rapT
	 *            Rechnungsabgrenzungsposten: Stückzins aus dem Vorjahr
	 * @return Kapitaertrag
	 */
	public static double keaktBestandJe(final double kemitAusfallAbschr, final double cfFimitAusfall,
			final double cfFimitAusfallJahresende, final double rapZinsen, final double rapT) {

		return kemitAusfallAbschr + cfFimitAusfallJahresende - cfFimitAusfall - rapZinsen + rapT;
	}

	/**
	 * Berechnet die Rechnungsabgrenzungsposten: Stückzins aus dem Vorjahr. <br/>
	 * Funktionsname in Excel: RAP_zinsen.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param vuZeitpunkt
	 *            Fälligkeitszeitpunkt des Kapitalertrages
	 * @param rapZinsenBuchwert
	 *            Rechnungsabgrenzungsposten: Stückzinsen aus dem Vorjahr
	 * @param kfFioAusfall
	 *            Kapitalertrag, ohne Berücksichtigung der RAP
	 * @param q
	 *            Ausfallwahrscheinlichkeit
	 * @param schalter
	 *            Rechnungsabgrenzung für aktuellen FI-Bestand im BSM rechnen?
	 * @return Rechnungsabgrenzungsposten
	 */
	public static double rapZinsen(final int t, final double vuZeitpunkt, final double rapZinsenBuchwert,
			final double kfFioAusfall, final double q, final boolean schalter) {
		if (t == 1) {
			return rapZinsenBuchwert;
		} else {
			if (schalter) {
				final double keOhneAbschr = kfFioAusfall * Math.pow((1.0 - q), Math.max(t - 1.0 + vuZeitpunkt / 12.0, 0));
				return (1.0 - vuZeitpunkt / 12.0) * keOhneAbschr;
			} else {
				return 0.0;
			}
		}
	}

	/**
	 * Berechnet den Cashflow der FI Titel unter Einbezug der Ausfallwahrscheinlichkeit am Ende des Jares. <br/>
	 * Funktionsname in Excel: CF_FI_mitAusfall_jahresende.
	 * 
	 * @param cfFimitAusfall
	 *            Cashflow der FI Titel unter Einbezug der Ausfallwahrscheinlichkeit
	 * @param dfHalbesjahr
	 *            Aufzinsungsfaktor zum Ende des Jahres
	 * @return Cashflow
	 */
	public static double cfFimitAusfallJahresende(final double cfFimitAusfall, final double dfHalbesjahr) {
		return cfFimitAusfall * dfHalbesjahr;
	}

	/**
	 * Berechnet den Buchwert der aktuellen FI-Bestand im Zeitpunkt t. <br/>
	 * Funktionsname in Excel: BW_FI_Akt.
	 * 
	 * @param t
	 *            Zeitpunkt der Auswertung
	 * @param bwFiAktVj
	 *            Buchwert des akt. FI-Bestand, Vorjahr
	 * @param cfFiAkt
	 *            Cashflow aus dem aktuellen FI-Bestand, VU-Zeitpunkt
	 * @param keFiAkt
	 *            Kapitalertrag aus dem aktuellen FI-Bestand, VU-Zeitpunkt
	 * @param bwFi0
	 *            Buchwert des FI-Portfolios zum Bewertungsstichtag
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param bwRe0
	 *            Buchwert der Immobilien zum Bewertungsstichtag
	 * @param bwEq
	 *            Buchwert der Aktien zum Bewertungsstichtag, nach der Verrechnung von Sonstigen Aktiva und Passiva
	 * @param bwRe
	 *            Buchwert der Immobilien zum Bewertungsstichtag, nach der Verrechnung von Sonstigen Aktiva und Passiva
	 * @param bwSaSp
	 *            Buchwert-Differenz zwischen den Sonstigen Aktiva und Passiva
	 * @param rapT
	 *            RAP: Zinsen
	 * @param rapVt
	 *            RAP: Zinsen, Vorjahr
	 * @return Buchwert der aktuellen FI-Bestand
	 */
	public static double bwFiAkt(final int t, final double bwFiAktVj, final double cfFiAkt, final double keFiAkt,
			final double bwFi0, final double bwEq0, final double bwRe0, final double bwEq, final double bwRe,
			final double bwSaSp, final double rapT, final double rapVt) {
		if (t == 0) {
			return bwFi0 + (bwSaSp - (bwEq + bwRe - bwEq0 - bwRe0));
		} else if (t > 0) {
			return nanZero(bwFiAktVj) - nanZero(cfFiAkt) + (nanZero(keFiAkt) + nanZero(rapVt) - nanZero(rapT));
		}
		return 0.0;

	}

	/**
	 * Berechnet den Buchwert des aktuellen FI-Bestandes im Zeitpunkt t, ohne Ausfall zu berücksichtigen. <br/>
	 * Funktionsname in Excel: BW_FI_Akt_oAusfall.
	 * 
	 * @param zeit
	 *            Zeitpunkt der Auswertung
	 * @param fiBuchwertBestand
	 *            Buchwert, Zinstitel im Zeitpunkt 0
	 * @param vgCfFioAusfall
	 *            Cashflow FI, korrigiert um SP-SA-Verrechnung
	 * @param vgBwFiAktoAusfall
	 *            Buchwert des akt. FI-Bestand, Vorjahr
	 * @param cfFIAkt
	 *            Cashflow aus dem aktuellen FI-Bestand, VU-Zeitpunkt
	 * @param vgKfFioAusfall
	 *            Kapitalertrag aus dem aktuellen FI-Bestand, VU-Zeitpunkt
	 * @return Buchwert
	 */
	public static double bwFiAktoAusfall(final int zeit, final double fiBuchwertBestand, final double cfFIAkt,
			final double vgBwFiAktoAusfall, final double vgCfFioAusfall, final double vgKfFioAusfall) {
		if (zeit == 1) {
			return fiBuchwertBestand * cfFIAkt;
		} else {
			return vgBwFiAktoAusfall - vgCfFioAusfall + vgKfFioAusfall;
		}
	}

	/**
	 * Berechnet den Martktwert der Zinstitel zum Anfang des Jahres. <br/>
	 * Funktionsname in Excel: MW_FI_AnfangJ.
	 * 
	 * @param mwFiEndeJ
	 *            Marktwert der Zinstitel zum Ende des des Vorjahres
	 * @param bwFiNeuAn
	 *            FI-Buchwert und Marktwert bei der Neuanlage
	 * @param t
	 *            Zeitpunkt
	 * @param mwFi0
	 *            Marktwert des FI-Portfolios zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param mwRe0
	 *            Marktwert der Immobilien zum Bewertungsstichtag
	 * @param mwEq
	 *            Marktwert der Aktien zum Bewertungsstichtag, nach der Verrechnung von Sonstigen Aktiva und Passiva
	 * @param mwRe
	 *            Marktwert der Immobilien zum Bewertungsstichtag, nach der Verrechnung von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Marktwert-Differenz zwischen den Sonstigen Aktiva und Passiva
	 * @return Martktwert der Zinstitel
	 */
	public static double mwFiAnfangJ(final double mwFiEndeJ, final double bwFiNeuAn, final int t, final double mwFi0,
			final double mwEq0, final double mwRe0, final double mwEq, final double mwRe, final double mwSaSp) {
		if (t == 1) {
			return mwFi0 + (mwSaSp - (mwEq + mwRe - mwEq0 - mwRe0));
		} else if (t > 1) {
			return nanZero(mwFiEndeJ) + bwFiNeuAn;
		}
		return 0.0;
	}

	/**
	 * Berechnet den EQ-Buchwert nach der Neuanlage zum Jahresanfang. <br/>
	 * Funktionsname in Excel: BW_EQ.
	 * 
	 * @param bwEqRlsIIv
	 *            Buchwert der Aktien aus dem Vorjahr nach der 2. Realisierung
	 * @param cfNeuanlageV
	 *            Cashflow, der für die Neuanlage zur Verfügung steht
	 * @param bwFiNeuan
	 *            FIBuchwert und Marktwert bei der Neuanlage
	 * @param bwReNeuAnl
	 *            Volumen der Neuanlage in Immobilien gemessen am Buchwert
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param bwSaSp
	 *            Differenz der Buchwerte von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Differenz der Marktwerte von Sonstigen Aktiva und Passiva
	 * @return der Wert
	 */
	public static double bwEq(final double bwEqRlsIIv, final double cfNeuanlageV, final double bwFiNeuan,
			final double bwReNeuAnl, final int t, final double bwEq0, final double mwEq0, final double bwSaSp,
			final double mwSaSp) {

		if (t == 1) {
			if (bwEq0 <= -bwSaSp && mwEq0 <= -mwSaSp) {
				return 0.0;
			} else {
				return Math.max(bwEq0 + bwSaSp, 1.0);
			}
		} else {
			return nanZero(bwEqRlsIIv) + (nanZero(cfNeuanlageV) - bwFiNeuan - bwReNeuAnl);
		}

	}

	/**
	 * Berechnet den RE-Buchwert nach der Neuanlage zum Jahresanfang. <br/>
	 * Funktionsname in Excel: BW_RE.
	 * 
	 * @param bwReNachRlsV
	 *            Buchwert der Immobilien aus dem Vorjahr nach der Realisierung
	 * @param bwReNeuAnl
	 *            Volumen der Neuanlage in Immobilien gemessen am Buchwert
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param bwRe0
	 *            Buchwert der Immobilien zum Bewertungsstichtag
	 * @param mwRe0
	 *            Marktwert der Immobilien zum Bewertungsstichtag
	 * @param bwSaSp
	 *            Differenz der Buchwerte von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Differenz der Marktwerte von Sonstigen Aktiva und Passiva
	 * @return der Wert
	 */
	public static double bwRe(final double bwReNachRlsV, final double bwReNeuAnl, final int t, final double bwEq0,
			final double mwEq0, final double bwRe0, final double mwRe0, final double bwSaSp, final double mwSaSp) {
		if (t == 1) {
			if (bwEq0 <= -bwSaSp && mwEq0 <= -mwSaSp) {
				if (bwRe0 <= -bwSaSp - bwEq0 && mwRe0 <= -mwSaSp - mwEq0) {
					return 0.0;
				} else {
					return Math.max(bwRe0 + bwSaSp + bwEq0, 1.0);
				}
			} else {
				return bwRe0;
			}
		} else {
			return nanZero(bwReNachRlsV) + bwReNeuAnl;
		}
	}

	/**
	 * Berechnet den Anschaffungswert der Aktien nach der Neuanlage zum Jahresanfang. <br/>
	 * Funktionsname in Excel: AW_EQ.
	 * 
	 * @param aEqRlsIv
	 *            im Vorjahr planmäßig realisierter Anteil der Aktien
	 * @param awEqV
	 *            Anschaffungswert der Aktien aus dem Vorjahr
	 * @param mwEqVorRlsV
	 *            EQ Marktwert aus dem Vorjahr vor der planmäßigen Realisierung und Neuanlage
	 * @param cfEqRlsIIV
	 *            Cashflow aus der 2. Realisierung der Aktien im Vorjahr
	 * @param cfNeuanlageV
	 *            Cashflow, der für die Neuanlage zur Verfügung steht
	 * @param bwFiNeuan
	 *            FI-Buchwert und Marktwert bei der Neuanlage
	 * @param bwReNeuAnl
	 *            Volumen der Neuanlage in Immobilien gemessen am Buchwert
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param bwSaSp
	 *            Differenz der Buchwerte von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Differenz der Marktwerte von Sonstigen Aktiva und Passiva
	 * @return der Wert
	 */
	public static double awEq(final double aEqRlsIv, final double awEqV, final double mwEqVorRlsV,
			final double cfEqRlsIIV, final double cfNeuanlageV, final double bwFiNeuan, final double bwReNeuAnl,
			final int t, final double bwEq0, final double mwEq0, final double bwSaSp, final double mwSaSp) {
		if (t == 1) {
			if (bwEq0 <= -bwSaSp && mwEq0 <= -mwSaSp) {
				return 0.0;
			} else {
				return Math.max(bwEq0 + bwSaSp, 1.0);
			}
		} else {
			double awEq = 0.0;
			if (nanZero(mwEqVorRlsV) > 0.001) {
				awEq = ((1.0 - nanZero(aEqRlsIv)) * nanZero(awEqV) + nanZero(aEqRlsIv) * nanZero(mwEqVorRlsV))
						* (1.0 - nanZero(cfEqRlsIIV) / nanZero(mwEqVorRlsV));
			}
			return awEq + (nanZero(cfNeuanlageV) - bwFiNeuan - bwReNeuAnl);
		}
	}

	/**
	 * Berechnet den Anschaffungswert der Immobilien nach der Neuanlage zum Jahresanfang. <br/>
	 * Funktionsname in Excel: AW_RE.
	 * 
	 * @param bwReNeuAnl
	 *            Volumen der Neuanlage in Immobilien gemessen am Buchwert
	 * @param aReRlsV
	 *            im Vorjahr realisierter Anteil der Immobilien
	 * @param awReV
	 *            Anschaffungswert der Immobilien aus dem Vorjahr
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param bwRe0
	 *            Buchwert der Immobilien zum Bewertungsstichtag
	 * @param mwRe0
	 *            Marktwert der Immobilien zum Bewertungsstichtag
	 * @param bwSaSp
	 *            Differenz der Buchwerte von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Differenz der Marktwerte von Sonstigen Aktiva und Passiva
	 * @param aReRlsPlanV
	 *            planmäßig realisierter Anteil der Immobilien im Vorjahr
	 * @param mwReVorRlsV
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage im Vorjahr
	 * @return der Wert
	 */
	public static double awRe(final double bwReNeuAnl, final double aReRlsV, final double awReV, int t,
			final double bwEq0, final double mwEq0, final double bwRe0, final double mwRe0, final double bwSaSp,
			final double mwSaSp, final double aReRlsPlanV, final double mwReVorRlsV) {
		if (t == 1) {
			if (bwEq0 <= -bwSaSp && mwEq0 <= -mwSaSp) {
				if (bwRe0 <= -bwSaSp - bwEq0 && mwRe0 <= -mwSaSp - mwEq0) {
					return 0.0;
				} else {
					return Math.max(bwRe0 + bwSaSp + bwEq0, 1.0);
				}
			} else {
				return bwRe0;
			}
		} else {
			return bwReNeuAnl + (1.0 - aReRlsV) * ((1 - aReRlsPlanV) * awReV + aReRlsPlanV * mwReVorRlsV);
		}
	}

	/**
	 * Berechnet den EQ-Marktwert nach der Neuanlage zum Jahresanfang. <br/>
	 * Funktionsname in Excel: mw_eq.
	 * 
	 * @param mwEqRlsIIv
	 *            Marktwert der Aktien aus dem Vorjahr nach der 2. Realisierung
	 * @param cfNeuanlageV
	 *            Cashflow, der für die Neuanlage zur Verfügung steht
	 * @param bwFiNeuan
	 *            FI-Buchwert und Marktwert bei der Neuanlage
	 * @param bwReNeuAnl
	 *            Volumen der Neuanlage in Immobilien gemessen am Buchwert
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param bwSaSp
	 *            Differenz der Buchwerte von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Differenz der Marktwerte von Sonstigen Aktiva und Passiva
	 * @return der Wert
	 */
	public static double mwEq(final double mwEqRlsIIv, final double cfNeuanlageV, final double bwFiNeuan,
			final double bwReNeuAnl, final int t, final double bwEq0, final double mwEq0, final double bwSaSp,
			final double mwSaSp) {
		if (t == 1) {
			if (bwEq0 <= -bwSaSp && mwEq0 <= -mwSaSp) {
				return 0.0;
			} else {
				return Math.max(mwEq0 + mwSaSp, 1.0);
			}
		} else {
			return mwEqRlsIIv + (cfNeuanlageV - bwFiNeuan - bwReNeuAnl);
		}
	}

	/**
	 * Berechnet den RE-Marktwert nach der Neuanlage zum Jahresanfang. <br/>
	 * Funktionsname in Excel: mw_re.
	 * 
	 * @param mwReNachRlsV
	 *            Marktwert der Immobilien aus dem Vorjahr nach der Realisierung
	 * @param bwReNeuAnl
	 *            Volumen der Neuanlage in Immobilien gemessen am Buchwert
	 * @param zeit
	 *            aktueller Zeitpunkt
	 * @param bwEq0
	 *            Buchwert der Aktien zum Bewertungsstichtag
	 * @param mwEq0
	 *            Marktwert der Aktien zum Bewertungsstichtag
	 * @param bwRe0
	 *            Buchwert der Immobilien zum Bewertungsstichtag
	 * @param mwRe0
	 *            Marktwert der Immobilien zum Bewertungsstichtag
	 * @param bwSaSp
	 *            Differenz der Buchwerte von Sonstigen Aktiva und Passiva
	 * @param mwSaSp
	 *            Differenz der Marktwerte von Sonstigen Aktiva und Passiva
	 * @return der Wert
	 */
	public static double mwRe(final double mwReNachRlsV, final double bwReNeuAnl, final int zeit, final double bwEq0,
			final double mwEq0, final double bwRe0, final double mwRe0, final double bwSaSp, final double mwSaSp) {
		if (zeit == 1) {
			if (bwEq0 <= -bwSaSp && mwEq0 <= -mwSaSp) {
				if (bwRe0 <= -bwSaSp - bwEq0 && mwRe0 <= -mwSaSp - mwEq0) {
					return 0.0;
				} else {
					return Math.max(mwRe0 + mwSaSp + mwEq0, 1);
				}
			} else {
				return mwRe0;
			}
		} else {
			return mwReNachRlsV + bwReNeuAnl;
		}

	}

	/**
	 * Berechnet den FI Buchwert Neuanlage im Zeitpunkt t. <br/>
	 * Funktionsname in Excel: BW_FI_NeuAn.
	 * 
	 * @param vgBwKaJe
	 *            Buchwert der Kapitalanlagen am Ende der Projektion
	 * @param vgBwFiVerechnungJe
	 *            Buchwert der Kapitalanlagen am Ende der Projektion
	 * @param vgCfNeuAnlage
	 *            Cash Flow zur Neuanlage, Endes des Vorjahres
	 * @param vgAFiZielDaa
	 *            Zielanteil des FI-Portfolios
	 * @param rlz
	 *            Restlaufzeit
	 * @param t
	 *            Zeitpunkt
	 * @return FI Buchwert Neuanlage
	 */
	public static double bwFiNeuAn(final double vgBwKaJe, final double vgBwFiVerechnungJe, final double vgCfNeuAnlage,
			final double vgAFiZielDaa, final int rlz, final int t) {
		if ((rlz == 0 && t > 0) || t == 1) {
			// bei Zeit = 1 sind alle einfließenden Größen 0 (außer vgAFiZielDaa), also auch bei t == 1
			return 0.0;
		} else if (t > 1) {
			double bw_Neuanlage_Ziel = Math.max(vgAFiZielDaa * vgBwKaJe - vgBwFiVerechnungJe, 0.0);
			if (bw_Neuanlage_Ziel > vgCfNeuAnlage) {
				return vgCfNeuAnlage;
			} else {
				return bw_Neuanlage_Ziel;
			}
		}
		return 0.0;
	}

	/**
	 * Berechnet den FI Cash Flow aus dem Neubestand. <br/>
	 * Funktionsname in Excel: CF_FI_NeuAnl.
	 * 
	 * @param keFiNeuAnl
	 *            FI Kapitalertrag des Neubestands
	 * @param t
	 *            Aktuelles Jahr
	 * @param faelligkeit
	 *            Fälligkeit der in t vorgenommenen FI Neuanlage
	 * @param bwFiNeuAnl
	 *            FI Buchwert Neuanlage im Zeitpunkt t
	 * @return FI Cash Flow
	 */
	public static double cfFiNeuAnl(final double keFiNeuAnl, final int t, final int[] faelligkeit,
			final double[] bwFiNeuAnl) {
		double cfFiNeuAnl = 0.0;
		for (int i = 0; i < bwFiNeuAnl.length; i++) {
			if (faelligkeit[i] == t) {
				cfFiNeuAnl += nanZero(bwFiNeuAnl[i]);
			}
		}
		return cfFiNeuAnl + keFiNeuAnl;
	}

	/**
	 * Berechnet den FI Kapitalertrag des Neubestands. <br/>
	 * Funktionsname in Excel: KE_FI_NeuAnl.
	 * 
	 * @param bwFiNeuAnl
	 *            FI Buchwert Neuanlage im Zeitpunkt t
	 * @param kupon
	 *            FI Kupon der Neuanlage in t
	 * @param t
	 *            Aktuelles Jahr
	 * @param omega
	 *            Länge der Projektion der Deckungsrückstellung
	 * @param faelligkeit
	 *            Zeitpunkte der Fälligkeit der Neuanlage
	 * @return FI Kapitalertrag
	 */
	public static double keFiNeuAnl(final double[] bwFiNeuAnl, final double[] kupon, final int t,
			final int omega, final int[] faelligkeit) {
		double keFiNeuAnl = 0.0;
		if (t <= omega) {
			for (int i = 0; i < bwFiNeuAnl.length; i++) {
				if (faelligkeit[i] >= t) {
					keFiNeuAnl += nanZero(bwFiNeuAnl[i]) * nanZero(kupon[i]);
				}
			}
		}
		return keFiNeuAnl;
	}

	/**
	 * Berechnet den Buchungswert des Fixed Income nach Verrechnung, dazu werden die Summen der Kapitalertragsdefizite
	 * aus dem Geschäftsjahr und dem Vorjahr verrechnet. Der Verrechnungszeitraum kann mit dem Parameter V vorgegeben
	 * werden. Der Parameter T wird nur benötigt, damit auch die Formel für t == 0 verwendet werden kann. <br/>
	 * Funktionsname in Excel: BW_FI_Verrechnung.
	 * 
	 * @param t
	 *            aktuelles Jahr
	 * @param omega
	 *            Länge der Projektion der Deckungsrückstellung
	 * @param rlz
	 *            Restlaufzeit (Übergabe als Range)
	 * @param bwFiAktBestand
	 *            FI Buchwert akt. Bestand
	 * @param bwFiNeuAn
	 *            FI Buchwert
	 * @param kedVerrechnung
	 *            Spalte, in der die Kapitalertragsdefizite zum Verrechnen stehen
	 * @param kedVjVerrechnen
	 *            Spalte, in der die Kapitalertragsdefizite aus den Vorjahren zum Verrechnen stehen.
	 * @return Buchungswert des Fixed Income nach Verrechnung
	 */
	public static double bwFiVerrechnung(final int t, final int omega, final int rlz[], final double bwFiAktBestand,
			final double bwFiNeuAn[], final double[] kedVerrechnung, final double[] kedVjVerrechnen) {

		double bwFiNeuAnAgg = 0.0;

		for (int i = 0; i < t; i++) {
			if (rlz[i + 1] >= t) {
				bwFiNeuAnAgg = bwFiNeuAnAgg + bwFiNeuAn[i + 1];
			}
		}
		double bwFi = bwFiNeuAnAgg + bwFiAktBestand;

		if (t > omega) {
			bwFi = 0.0;
		}

		if (t == 0) {
			return bwFi;
		} else {

			double bwFiVerrechnung = 0.0;
			for (int i = 0; i < kedVjVerrechnen.length - 1; i++) {
				bwFiVerrechnung += nanZero(kedVerrechnung[i]) - nanZero(kedVjVerrechnen[i]);
			}
			return bwFi + bwFiVerrechnung;
		}
	}

	/**
	 * Berechnet den FI Cash Flow gesamt. <br/>
	 * Funktionsname in Excel: CF_FI.
	 * 
	 * @param cfFiNeuAnl
	 *            FI Cash Flow aus dem Neubestand
	 * @param cfFiAltBest
	 *            FI Cash Flow akt. Bestand
	 * @return FI Cash Flow
	 */
	public static double cfFi(final double cfFiNeuAnl, final double cfFiAltBest) {
		return cfFiNeuAnl + cfFiAltBest;
	}

	/**
	 * Berechnet den Cashflow aus dem aktuellen FI-Bestand im Zeitpunkt t, ohne Ausfall zu berücksichtigen. <br/>
	 * Funktionsname in Excel: CF_FI_oAusfall.
	 * 
	 * @param bwAppassung
	 *            Anpassung des Buchwertes durch SP-SA-Verrechnung
	 * @param cfFIAkt
	 *            Cashflow aus dem aktuellen FI-Bestand, VU-Zeitpunkt
	 * @return Cashflow
	 */
	public static double cfFioAusfall(final double bwAppassung, final double cfFIAkt) {
		return bwAppassung * cfFIAkt;
	}

	/**
	 * Berechnet den Kapitalertrag aus dem aktuellen FI-Bestand im Zeitpunkt t, ohne Ausfall zu berücksichtigen. <br/>
	 * Funktionsname in Excel: KE_FI_oAusfall.
	 * 
	 * @param bwAppassung
	 *            Anpassung des Buchwertes durch SP-SA-Verrechnug
	 * @param keFIAkt
	 *            Kapitalertrag aus dem aktuellen FI-Bestand, VU-Zeitpunkt
	 * @return Kapitalertrag
	 */
	public static double kfFioAusfall(final double bwAppassung, final double keFIAkt) {
		return bwAppassung * keFIAkt;
	}

	/**
	 * Berechnet den FI Kapitalertrag gesamt. <br/>
	 * Funktionsname in Excel: KE_FI.
	 * 
	 * @param keFiNeuAnl
	 *            FI Kapitalertrag des Neubestands
	 * @param keFiAkt
	 *            FI Kapitalertrag akt. Bestand
	 * @param kedVjVerrechnen
	 *            die Kapitalertragsdefizite aus den Vorjahren zum Verrechnen stehen
	 * @return FI Kapitalertrag
	 */
	public static double keFi(final double keFiAkt, final double keFiNeuAnl, final double kedVjVerrechnen) {
		return keFiNeuAnl + keFiAkt - kedVjVerrechnen;
	}

	/**
	 * Berechnet den EQ Marktwert vor der planmäßigen Realisierung und Neuanlage. <br/>
	 * Funktionsname in Excel: MW_EQ_vorRls
	 * 
	 * @param pEqV
	 *            EQ Preis, Vorjahr
	 * @param pEq
	 *            EQ Preis
	 * @param mwEq
	 *            EQ Marktwert, Anfang des Jahres
	 * @return der Wert
	 * 
	 */
	public static double mwEqVorRls(final double pEqV, final double pEq, final double mwEq) {
		return (pEq / pEqV) * mwEq;

	}

	/**
	 * Berechnet den RE Marktwert vor der eventuellen Realisierung und Neuanlage. <br/>
	 * Funktionsname in Excel: MW_RE_vorRls
	 * 
	 * @param pReV
	 *            RE Preis, Vorjahr
	 * @param pRe
	 *            RE Preis
	 * @param mwRe
	 *            RE Marktwert, Anfang des Jahres
	 * @return der Wert
	 */
	public static double mwReVorRls(double pReV, double pRe, double mwRe) {
		return (pRe / pReV) * mwRe;

	}

	/**
	 * Berechnet den laufenden Kapitalertrag für Aktien aus Dividenden. In Excel wird die Position im Szenariensatz
	 * übergeben, hier verwenden wir direkt den Wert. <br/>
	 * Funktionsname in Excel: KE_Div
	 * 
	 * @param dividendenEsg
	 *            Dividenden aus dem Szenariensatz (abweichend von Excel)
	 * @param pEqV
	 *            EQ Preis, Vorjahr
	 * @param mwEQAj
	 *            EQ Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @return der Wert
	 */
	public static double keDiv(final double dividendenEsg, final double pEqV, final double mwEQAj) {
		return dividendenEsg / pEqV * mwEQAj;
	}

	/**
	 * Berechnet den laufenden Kapitalertrag für Immobilien aus Mieten. In Excel wird die Position im Szenariensatz
	 * übergeben, hier verwenden wir direkt den Wert. <br/>
	 * Fuktionsname in Excel: KE_Mieten.
	 * 
	 * @param zeit
	 *            Zeitpunkt an dem der Wert der Aktienpreis ausgegeben werden soll
	 * @param mietenEsg
	 *            Mieten aus dem Szenariensatz (abweichend von Excel)
	 * @param pReV
	 *            RE Preis, Vorjahr
	 * @param mwReAj
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @param rapMieten
	 *            Rechnungsabgrenzungsposten: Mieten
	 * @return der Wert
	 */
	public static double keMieten(final int zeit, final double mietenEsg, final double pReV, final double mwReAj,
			final double rapMieten) {
		final double keMieten = mietenEsg / nanZero(pReV) * mwReAj;
		if (zeit == 1) {
			return keMieten - rapMieten;
		}
		return keMieten;
	}

	/**
	 * Berechnet den EQ Kapitalertrag aus Ab- und Zuschreibungen. <br/>
	 * Funktionsname in Excel: KE_EQ_Ab_und_Zuschreibung.
	 * 
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @param bwEq
	 *            EQ Buchwert
	 * @param awEq
	 *            EQ Anschaffungswert
	 * @param grenzeAbschreibung
	 *            Grenze der Abschreibung
	 * @return der Wert
	 */
	public static double keEqAbUndZuschreibung(final double mwEqVorRls, final double bwEq, final double awEq,
			final double grenzeAbschreibung) {
		return Math.min(awEq, Math.min(mwEqVorRls / grenzeAbschreibung, Math.max(bwEq, mwEqVorRls))) - bwEq;
	}

	/**
	 * Berechnet den RE Kapitalertrag aus Ab- und Zuschreibungen. <br/>
	 * Funktionsname in Excel: KE_RE_Ab_und_Zuschreibung.
	 * 
	 * @param mwReVorRls
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @param bwRe
	 *            RE Buchwert
	 * @param awRe
	 *            RE Anschaffungswert
	 * @param grenzeAbschreibung
	 *            Grenze der Abschreibung
	 * @return der Wert
	 */
	public static double keReAbUndZuschreibung(final double mwReVorRls, final double bwRe, final double awRe,
			final double grenzeAbschreibung) {
		return Math.min(awRe, Math.min(mwReVorRls / grenzeAbschreibung, Math.max(bwRe, mwReVorRls))) - bwRe;
	}

	/**
	 * Berechnet den EQ Buchwert nach Ab- und Zuschreibungen. <br/>
	 * Name in Excel: BW_EQ_nach_Ab_und_Zuschreibung.
	 * 
	 * @param bwEq
	 *            EQ Buchwert
	 * @param keEqAbUndZuschreibung
	 *            EQ Kapitalertrag aus Ab- und Zuschreibungen
	 * @return der Wert
	 */
	public static double bwEqNachAbUndZuschreibung(final double bwEq, final double keEqAbUndZuschreibung) {
		return bwEq + keEqAbUndZuschreibung;
	}

	/**
	 * Berechnet den RE Buchwert nach Ab- und Zuschreibungen. <br/>
	 * Name in Excel: BW_RE_nach_Ab_und_Zuschreibung.
	 * 
	 * @param bwRe
	 *            RE Buchwert
	 * @param keReAbUndZuschreibung
	 *            RE Kapitalertrag aus Ab- und Zuschreibungen
	 * @return der Wert
	 */
	public static double bwReNachAbUndZuschreibung(final double bwRe, final double keReAbUndZuschreibung) {
		return bwRe + keReAbUndZuschreibung;
	}

	/**
	 * Berechnet den laufenden Kapitalertrag für Aktien aus Dividenden und Ab-/Zuschreibung. <br/>
	 * Name in Excel: KE_EQ_laufend.
	 * 
	 * @param keDiv
	 *            Kapitalertrag aus Dividenden
	 * @param keEqAbUndZuschreibung
	 *            EQ Kapitalertrag aus Ab- und Zuschreibungen
	 * @return der Wert
	 */
	public static double keEqLaufend(final double keDiv, final double keEqAbUndZuschreibung) {
		return keDiv + keEqAbUndZuschreibung;
	}

	/**
	 * Berechnet den laufenden Kapitalertrag für Immobilien aus Mieten und Ab-/Zuschreibung. <br/>
	 * Name in Excel: KE_RE_laufend.
	 * 
	 * @param keMiete
	 *            laufender Kapitalertrag aus Mieten
	 * @param keReAbUndZuschreibung
	 *            RE Kapitalertrag aus Ab- und Zuschreibungen
	 * @return der Wert
	 */
	public static double keReLaufend(final double keMiete, final double keReAbUndZuschreibung) {
		return keMiete + keReAbUndZuschreibung;
	}

	/**
	 * Berechnet den Kapitalertrag aus der Aufzinsung von Leistungen, Kosten und Beiträgen. <br/>
	 * Name in Excel: KE_CF_Aufzinsung.
	 * 
	 * @param leistungGesVu
	 *            Gesamtleistung zum VU-Zeitpunkt
	 * @param beitragVu
	 *            stoch. Beitrag zum VU-Zeitpunkt
	 * @param kostenVu
	 *            stoch. Kosten zum VU-Zeitpunkt
	 * @param cfRvVu
	 *            stoch. Cashflow von EVU an RVU zm VU-Zeitpunkt
	 * @param zagVorjahr
	 *            ZAG Vorjahr, VU-Zeitpunkt
	 * @param steuerVorjahr
	 *            Ertragssteuer Vorjahr, VU-Zeitpunkt
	 * @param leistungGesJahresEnde
	 *            Gesamtleistung zum Ende des Jahres aufgezinst
	 * @param beitragJahresEnde
	 *            stoch. Beitrag zum Ende des Jahres aufgezinst
	 * @param kostenJahresEnde
	 *            stoch. Kosten zum Ende des Jahres aufgezinst
	 * @param cfRvJahresEnde
	 *            stoch. Cashflow von EVU an RVU zum Jahresende aufgezinst
	 * @param zagAufzins
	 *            ZAG des Vorjahres, aufgezinst
	 * @param steuerAufzins
	 *            Ertragssteuer Vorjahr, aufgezinst
	 * @param t
	 *            Zeit
	 * @param omega
	 *            Projektionslänge Versicherungstechnik
	 * @param reUeFlvFondsVu
	 *            Risiko- und übriges Ergebnis FLV Deckungsstock Fonds zum VU-Zeitpunkt
	 * @param reUeFlvFondsJEnde
	 *            Risiko- und übriges Ergebnis FLV Deckungsstock Fonds zum Ende des Jahres
	 * @return der Wert
	 */
	public static double keCfAufzinsung(final double leistungGesVu, final double beitragVu, final double kostenVu,
			final double cfRvVu, final double zagVorjahr, final double steuerVorjahr,
			final double leistungGesJahresEnde, final double beitragJahresEnde, final double kostenJahresEnde,
			final double cfRvJahresEnde, final double zagAufzins, final double steuerAufzins, final long t,
			final long omega, final double reUeFlvFondsVu, final double reUeFlvFondsJEnde) {
		if (t > omega) {
			return 0.0;
		}
		return (beitragJahresEnde - kostenJahresEnde - cfRvJahresEnde - leistungGesJahresEnde - zagAufzins
				- steuerAufzins + reUeFlvFondsJEnde)
				- (beitragVu - kostenVu - cfRvVu - leistungGesVu - zagVorjahr - nanZero(steuerVorjahr)
						+ reUeFlvFondsVu);
	}

	/**
	 * Berechnet die EQ-BWR für die planmäßige Realisierung. <br/>
	 * Funktionsname in Excel: BWR_EQ_vorRls.
	 * 
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der planmäßigen Realisierung der Bewertungsreserven
	 * @param bwEqNachAbUndZuschreibung
	 *            EQ Buchwert nach Ab- und Zuschreibungen
	 * @return der Wert
	 */
	public static double bwrEqVorRls(final double mwEqVorRls, final double bwEqNachAbUndZuschreibung) {
		if (bwEqNachAbUndZuschreibung < 0.001) {
			return 0.0;
		} else {
			return mwEqVorRls / bwEqNachAbUndZuschreibung - 1.0;
		}
	}

	/**
	 * Berechnet die RE-BWR für die planmäßige Realisierung. <br/>
	 * Funktionsname in Excel: BWR_RE_vorRls.
	 * 
	 * @param mwReVorRls
	 *            RE Marktwert vor der planmäßigen Realisierung der Bewertungsreserven
	 * @param bwReNachAbUndZuschreibung
	 *            RE Buchwert nach Ab- und Zuschreibungen
	 * @return der Wert
	 */
	public static double bwrReVorRls(final double mwReVorRls, final double bwReNachAbUndZuschreibung) {
		if (bwReNachAbUndZuschreibung < 0.001) {
			return 0.0;
		} else {
			return mwReVorRls / bwReNachAbUndZuschreibung - 1.0;
		}
	}

	/**
	 * Berechnet die EQ Ziel-BWR in % des Buchwertes. <br/>
	 * Funktionsname in Excel: BWR_EQ_Ziel.
	 * 
	 * @param bwrEqVorRls
	 *            EQ BWR vor der planmäßigen Realisierung in % des Buchwertes
	 * @param bwrGrenze
	 *            BWR-Grenze, ab welcher die EQ realisiert werden (entspricht X% im Fachkonzept)
	 * @param bwrY
	 *            Anteil an EQ-BWR, die realisiert werden (entspricht Y% im Fachkonzept)
	 * @return der Wert
	 */
	public static double bwrEqZiel(final double bwrEqVorRls, final double bwrGrenze, final double bwrY) {
		if (bwrEqVorRls > bwrGrenze) {
			return bwrEqVorRls - bwrY * (bwrEqVorRls - bwrGrenze);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die RE Ziel-BWR in % des Buchwertes. <br/>
	 * Funktionsname in Excel: BWR_RE_Ziel.
	 * 
	 * @param bwrReVorRls
	 *            EQ BWR vor der planmäßigen Realisierung in % des Buchwertes
	 * @param bwrGrenze
	 *            BWR-Grenze, ab welcher die RE realisiert werden (entspricht X% im Fachkonzept)
	 * @param bwrY
	 *            Anteil an RE-BWR, die realisiert werden (entspricht Y% im Fachkonzept)
	 * @return der Wert
	 */
	public static double bwrReZiel(final double bwrReVorRls, final double bwrGrenze, final double bwrY) {
		if (bwrReVorRls > bwrGrenze) {
			return bwrReVorRls - bwrY * (bwrReVorRls - bwrGrenze);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet EQ Anteil zur planmäßigen Realisierung in %. <br/>
	 * Funktionsname in Excel: a_EQ_Rls_I
	 * 
	 * @param bwrEqVorRls
	 *            EQ BWR vor der planmäßigen Realisierung in % des Buchwertes
	 * @param bwrEqZiel
	 *            EQ Ziel-BWR in % des Buchwertes
	 * @param bwrGrenze
	 *            BWR-Grenze, ab welcher die EQ realisiert werden (entspricht X% im Fachkonzept)
	 * @return der Wert
	 */
	public static double aEqRlsI(final double bwrEqVorRls, final double bwrEqZiel, final double bwrGrenze) {
		if (bwrEqVorRls > bwrGrenze) {
			return (bwrEqVorRls - bwrEqZiel) / (bwrEqVorRls * (1 + bwrEqZiel));
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet RE Anteil zur planmäßigen Realisierung in %. <br/>
	 * Funktionsname in Excel: a_RE_Rls_plan
	 * 
	 * @param bwrReVorRls
	 *            RE BWR vor der planmäßigen Realisierung in % des Buchwertes
	 * @param bwrReZiel
	 *            RE Ziel-BWR in % des Buchwertes
	 * @param bwrGrenze
	 *            BWR-Grenze, ab welcher die RE realisiert werden (entspricht X% im Fachkonzept)
	 * @return der Wert
	 */
	public static double aReRlsPlan(final double bwrReVorRls, final double bwrReZiel, final double bwrGrenze) {
		if (bwrReVorRls > bwrGrenze) {
			return (bwrReVorRls - bwrReZiel) / (bwrReVorRls * (1.0 + bwrReZiel));
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den EQ Buchwert nach der planmäßigen Realisierung der EQ-BWR. <br/>
	 * Funktionsname in Excel: BW_EQ_Rls_I
	 * 
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @param bwEqNachAbUndZuschreibung
	 *            EQ Buchwert nach Ab- und Zuschreibungen
	 * @param aEqRlsI
	 *            EQ Anteil zur planmäßigen Realisierung in % des Buchwertes
	 * @return der Wert
	 */
	public static double bwEqRlsI(final double mwEqVorRls, final double bwEqNachAbUndZuschreibung,
			final double aEqRlsI) {
		return (1 - aEqRlsI) * bwEqNachAbUndZuschreibung + aEqRlsI * mwEqVorRls;
	}

	/**
	 * Berechnet den RE Buchwert nach der planmäßigen Realisierung der RE-BWR. <br/>
	 * Funktionsname in Excel: BW_RE_Rls_plan
	 * 
	 * @param mwReVorRls
	 *            RE Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @param bwReNachAbUndZuschreibung
	 *            RE Buchwert nach Ab- und Zuschreibungen
	 * @param aReRlsPlan
	 *            RE Anteil zur planmäßigen Realisierung in % des Buchwertes
	 * @return der Wert
	 */
	public static double bwReRlsPlan(final double mwReVorRls, final double bwReNachAbUndZuschreibung,
			final double aReRlsPlan) {
		return (1.0 - aReRlsPlan) * bwReNachAbUndZuschreibung + aReRlsPlan * mwReVorRls;
	}

	/**
	 * Berechnet den Kapitalertrag aus der planmäßigen Realisierung der BWR auf Aktien. <br/>
	 * Funktionsname in Excel: KE_EQ_Rls_I
	 * 
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @param bwEqNachAbUndZuschreibung
	 *            EQ Buchwert nach Ab- und Zuschreibungen
	 * @param aEqRlsI
	 *            EQ Anteil zur planmäßigen Realisierung in % des Buchwertes
	 * @return der Wert
	 * 
	 */
	public static double keEqRlsI(final double mwEqVorRls, final double bwEqNachAbUndZuschreibung,
			final double aEqRlsI) {
		return aEqRlsI * (mwEqVorRls - bwEqNachAbUndZuschreibung);

	}

	/**
	 * Berechnet den Kapitalertrag aus der planmäßigen Realisierung der BWR auf Immobilien. <br/>
	 * Funktionsname in Excel: KE_RE_Rls_plan
	 * 
	 * @param mwReVorRls
	 *            RE Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @param bwReNachAbUndZuschreibung
	 *            RE Buchwert nach Ab- und Zuschreibungen
	 * @param aReRlsPlan
	 *            RE Anteil zur planmäßigen Realisierung in % des Buchwertes
	 * @return der Wert
	 */
	public static double keReRlsPlan(final double mwReVorRls, final double bwReNachAbUndZuschreibung,
			final double aReRlsPlan) {
		return aReRlsPlan * (mwReVorRls - bwReNachAbUndZuschreibung);
	}

	/**
	 * Berechnet den Buchwert der FI-Titel zum Jahresende. <br/>
	 * Funktionsname in Excel: BW_FI_gesamt_JE.
	 * 
	 * @param bwFiVerrechnung
	 *            FI Buchwert mit Verrechnung, Jahresanfange
	 * @param cfFi
	 *            Cash Flow aus FI-Geschäft
	 * @param keFi
	 *            Kapitalertrag aus FI-Geschäft
	 * @param rapT
	 *            RAP: Zinsen
	 * @param rapVT
	 *            RAP: Zinsen, Vorjahr
	 * @return Buchwert
	 */
	public static double bwFiGesamtJe(final double bwFiVerrechnung, final double cfFi, final double keFi,
			final double rapT, final double rapVT) {

		return bwFiVerrechnung - cfFi + (keFi + nanZero(rapVT) - rapT);

	}

	/**
	 * Berechnet den Buchwert der Kapitalanlagen vor der 2. Realisierung zum Jahresende. <br/>
	 * Funktionsname in Excel: BW_vorRls.
	 * 
	 * @param bwFiGesamtNe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param bwReRlsPlan
	 *            RE Buchwert nach planmäßiger Realisierung der RE-BWR
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @param cfFI
	 *            Cash Flow aus FI-Geschäft
	 * @param keMiete
	 *            Kapitalertrag aus Mieten
	 * @param rapMiete
	 *            RAP, Miete
	 * @param keDiv
	 *            Kapitalertrag aus Dividenden
	 * @param cfohneKA
	 *            gesamte Cash-out-flows, ohne KA-Cashflows
	 * @return der Wert
	 */
	public static double bwVorRls(final double bwFiGesamtNe, final double bwReRlsPlan, final double bwEqRlsI,
			final double cfFI, final double keMiete, final double rapMiete, final double keDiv, final double cfohneKA) {
		return bwFiGesamtNe + bwReRlsPlan + bwEqRlsI + Math.max(cfFI + keDiv + rapMiete + keMiete - cfohneKA, 0.0);
	}

	/**
	 * Berechnet den Marktwert der Kapitalanlagen vor der 2. Realisierung zum Jahresende. <br/>
	 * Funktionsname in Excel: MW_vorRls.
	 * 
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param mwReVorRls
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der planmäßigen Realisierung und Neuanlage
	 * @param cfFi
	 *            Cash Flow aus FI-Geschäft
	 * @param keMiete
	 *            Kapitalertrag aus Mieten
	 * @param rapMiete
	 *            RAP, Miete
	 * @param keDiv
	 *            Kapitalertrag aus Dividenden
	 * @param cfOhneKa
	 *            gesamte Cash-out-flows, ohne KA-Cashflows
	 * @return der Wert
	 */
	public static double mwVorRls(final double mwFiJahresende, final double mwReVorRls, final double mwEqVorRls,
			final double cfFi, final double keMiete, final double rapMiete, final double keDiv, final double cfOhneKa) {
		return mwFiJahresende + mwReVorRls + mwEqVorRls + Math.max(cfFi + keDiv + rapMiete + keMiete - cfOhneKa, 0.0);
	}

	/**
	 * Berechnet den Kapitalertrag vor der 2. Realisierung. <br/>
	 * Funktionsname in Excel: KE_Rls_I.
	 * 
	 * @param keFi
	 *            FI Kapitalertrag gesamt
	 * @param keReLaufend
	 *            laufender Kapitalertrag für RE aus Mieten und Ab-/Zuschreibung
	 * @param keEqLaufend
	 *            laufender Kapitalertrag für EQ aus Dividenden und Ab-/Zuschreibung
	 * @param keEqRlsI
	 *            Kapitalertrag aus planmäßiger Realisierung der EQ-BWR
	 * @param keReRlsPlan
	 *            Kapitalertrag aus planmäßiger Realisierung der RE-BWR
	 * @param aKredit
	 *            Kapitalaufwand für Kredit
	 * @param keCfAufzinsung
	 *            Kapitalertrag aus der Aufzinsung von Leistungen, Kosten und Beiträgen
	 * @return der Wert
	 */
	public static double keRlsI(final double keFi, final double keReLaufend, final double keEqLaufend,
			final double keEqRlsI, final double keReRlsPlan, final double aKredit, final double keCfAufzinsung) {
		return keFi + keReLaufend + keEqLaufend + keEqRlsI + keReRlsPlan - aKredit + keCfAufzinsung;
	}

	/**
	 * Gibt den Zielanteil der RE-Titel an den Kapitalanlagen an. Wird für dynamische Asset Allokation (DAA) verwendet.
	 * <br/>
	 * Funktionsname in Excel: a_RE_Ziel_daa.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Länge der Projektion der Deckungsrückstellung
	 * @param methode
	 *            Steuerungsmethode Asset Allokation
	 * @param rzMittl
	 *            Mittlere Rechnungszins zum Zeitpunkt T
	 * @param rEq
	 *            DAA-Faktor auf RW
	 * @param rFi
	 *            DAA-Faktor auf FI-BWR
	 * @param rEqVerlust
	 *            DAA-Faktor auf RW-Verluste
	 * @param rResDaa
	 *            DAA-Faktor: Untergrenze der passiven und aktiven Reserven
	 * @param aReZielKonst
	 *            Zielanteil der RE-Titel, wenn DAA "gute wirtschaftl. Situation" auslöst
	 * @param aReMax
	 *            Zielanteil der RE-Titel, wenn DAA "schlechte wirtschaftl. Situation" auslöst
	 * @param aReZielVj
	 * 			Anteil der RE-Titel an gesamten Kapitalanlagen im Vorjahr
	 * @param aReZielInitial
	 * 			Initialwert a_Re_Ziel(s, 0) für die Umschichtung
	 * @param keRlsI
	 *            Kapitalertrag vor der 2. Realisierung
	 * @param bwVorRls
	 *            Buchwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param mwVorRls
	 *            Marktwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param bwFiGesamtJe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @param bwrPas
	 *            Passive Bewertungsreserven/Lasten
	 * @param drVorDekl
	 *            Deckungsrückstellung vor Deklaration
	 * @param hZiel
	 * 			Schrittweite für RE-Titel in Richtung Zielanteil, wenn DAA gute wirtschaftl. Lage auslöst
	 * @param hMax
	 * 			Schrittweite für RE-Titel in Richtung Maximalanteil, wenn DAA schlechte wirtschaftl. Lage auslöst
	 * @return der Wert
	 */
	public static double aReZielDaa(final int t, final int omega, final int methode, final double rzMittl,
			final double rEq, final double rFi, final double rEqVerlust, final double rResDaa,
			final double aReZielKonst, final double aReMax, final double aReZielVj, final double aReZielInitial, 
			final double keRlsI, final double bwVorRls, final double mwVorRls, final double bwFiGesamtJe, 
			final double mwFiJahresende, final double bwEqRlsI, final double mwEqVorRls, final double bwrPas, 
			final double drVorDekl, final double hZiel, final double hMax) {
		final double mwGesamt = mwVorRls;
		final double bwGesamt = bwVorRls;

		if (t == 0 || Math.abs(drVorDekl) < 0.001 || t > omega || mwGesamt <= 0.0 || bwGesamt <= 0) {
			return aReZielInitial;
		} else {
			switch (methode) {
			case 0: // Statische Asset Allokation
				return aReZielKonst;
			case 1: // Dynamische Asset Allokation Methode 1: mittl. RZ vs. KE
				final double test = (keRlsI + rEq * (mwEqVorRls - bwEqRlsI)
						+ rFi * Math.max(mwFiJahresende - bwFiGesamtJe, 0)) / bwGesamt
						- rEqVerlust * mwEqVorRls / mwGesamt;
				if (test > rzMittl) {
					if (aReZielKonst > aReZielVj) {
						return Math.min(aReZielKonst, aReZielVj + hZiel);
					} else {
						return Math.max(aReZielKonst, aReZielVj - hZiel);
					}
				} else {
					if (aReMax > aReZielVj) {
						return Math.min(aReMax, aReZielVj + hMax);
					} else {
						return Math.max(aReMax, aReZielVj - hMax);
					}
				}
			case 2: // Dynamische Asset Allokation Methode 2: Anteil der passiven und aktiven Reserven
				if ((bwrPas + mwGesamt - bwGesamt) / drVorDekl > rResDaa) {
					if (aReZielKonst > aReZielVj) {
						return Math.min(aReZielKonst, aReZielVj + hZiel);
					} else {
						return Math.max(aReZielKonst, aReZielVj - hZiel);
					}
				} else {
					if (aReMax > aReZielVj) {
						return Math.min(aReMax, aReZielVj + hMax);
					} else {
						return Math.max(aReMax, aReZielVj - hMax);
					}
				}
			default:
				return 0.0;
			}
		}
	}

	/**
	 * Berechnet den zu realisierenden Anteil der Immobilien. <br/>
	 * Funktionsname in Excel: a_RE_Rls.
	 * 
	 * @param bwReRlsPlan
	 *            RE Buchwert nach planmäßiger Realisierung der RE-BWR
	 * @param mwReVorRls
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @param aReZiel
	 *            Zielanteil der Immobilien am Gesamtportfolio
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @param bwFiGesamtJe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param bwVorRls
	 *            Buchwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param mwVorRls
	 *            Marktwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param keRlsI
	 *            Kapitalertrag vor der 2. Realisierung
	 * @param zielKe
	 *            Ziel-Kapitalertrag (im Fachkonzept KE^Mindest)
	 * @param kaAufwendungen
	 *            Kapitalanlageaufwendungen (im Fachkonzept K_KA^EndeJ)
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param aFiMin
	 *            ?
	 * @param omega
	 *            Projektionslänge Versicherungstechnik
	 * @return der Wert
	 */
	public static double aReRls(final double bwReRlsPlan, final double mwReVorRls, final double aReZiel,
			final double bwEqRlsI, final double mwEqVorRls, final double bwFiGesamtJe, final double mwFiJahresende,
			final double bwVorRls, final double mwVorRls, final double keRlsI, final double zielKe,
			final double kaAufwendungen, final int t, final double aFiMin, final int omega) {
		final double bwrRe = mwReVorRls - bwReRlsPlan;
		final double bwrEq = mwEqVorRls - bwEqRlsI;
		final double bwrFi = mwFiJahresende - bwFiGesamtJe;

		final double aReAngepasst;
		if (t == 0 || t > omega || mwVorRls <= 0.001 || bwVorRls <= 0.001) {
			aReAngepasst = aReZiel;
		} else {
			aReAngepasst = Math.min(aReZiel, bwReRlsPlan / bwVorRls);
		}

		double keGap = (keRlsI + Math.max(bwrEq, 0.0) + Math.max(bwrFi, 0.0)) - (zielKe + kaAufwendungen);
		keGap = Math.max(keGap, 0.0);

		final double aReRls1;
		if (bwrRe < -0.001) {
			aReRls1 = Math.min((-keGap) / (Math.min(bwrRe, 0.0)), 1.0);
		} else {
			aReRls1 = 1.0;
		}

		final double aReRls2;
		if (bwReRlsPlan + aReAngepasst * bwrRe != 0) {
			aReRls2 = Math.min((bwReRlsPlan - aReAngepasst * bwVorRls) / (bwReRlsPlan + aReAngepasst * bwrRe), 1.0);
		} else {
			aReRls2 = 0.0;
		}

		final double aReRls3 = bwReRlsPlan < 0.001 ? 1.0 : 1 - (1 - aFiMin) * bwVorRls / bwReRlsPlan;

		double aReRls = Math.max(Math.min(Math.min(Math.max(aReRls1, aReRls3), aReRls2), 1.0), 0.0);

		if (t >= omega) {
			aReRls = 1.0;
		}
		return aReRls;
	}

	/**
	 * Berechnet den Kapitalertrag aus der Realisierung der Immobilien. <br/>
	 * Funktionsname in Excel: KE_RE_Rls.
	 * 
	 * @param aReRls
	 *            zu realisierender Anteil der Immobilien
	 * @param mwReVorRls
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @param bwReRlsPlan
	 *            RE Buchwert nach planmäßiger Realisierung der RE-BWR
	 * @return der Wert
	 */
	public static double keReRls(final double aReRls, final double mwReVorRls, final double bwReRlsPlan) {
		return aReRls * (mwReVorRls - bwReRlsPlan);
	}

	/**
	 * Berechnet den Cashflow aus der Realisierung der Immobilien. <br/>
	 * Funktionsname in Excel: CF_RE_Rls.
	 * 
	 * @param aReRls
	 *            zu realisierender Anteil der Immobilien
	 * @param mwReVorRls
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @return der Wert
	 */
	public static double cfReRls(final double aReRls, final double mwReVorRls) {
		return aReRls * mwReVorRls;
	}

	/**
	 * Berechnet den Buchwert der Immobilien nach der Realisierung. <br/>
	 * Funktionsname in Excel: BW_RE_nachRls.
	 * 
	 * @param aReRls
	 *            zu realisierender Anteil der Immobilien
	 * @param bwReRlsPlan
	 *            RE Buchwert nach planmäßiger Realisierung der RE-BWR
	 * @return der Wert
	 */
	public static double bwReNachRls(final double aReRls, final double bwReRlsPlan) {
		return (1.0 - aReRls) * bwReRlsPlan;
	}

	/**
	 * Berechnet den Marktwert der Immobilien nach der Realisierung. <br/>
	 * Funktionsname in Excel: MW_RE_nachRls.
	 * 
	 * @param aReRls
	 *            zu realisierender Anteil der Immobilien
	 * @param mwReVorRls
	 *            RE Marktwert vor der eventuellen Realisierung und Neuanlage
	 * @return der Wert
	 */
	public static double mwReNachRls(final double aReRls, final double mwReVorRls) {
		return (1.0 - aReRls) * mwReVorRls;
	}

	/**
	 * Berechnet den zu realisierenden Anteil der Aktien für die 2. Realisierung. <br/>
	 * Funktionsname in Excel: a_EQ_Rls_II.
	 * 
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @param bwFiGesamtJe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param bwReNachRls
	 *            Buchwert der Immobilien nach der Realisierung
	 * @param bwVorRls
	 *            Buchwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param aReZiel
	 *            Zielanteil der Immobilien am Gesamtportfolio
	 * @param aFiZiel
	 *            Zielanteil der FI-Titel am Gesamtportfolio
	 * @param aFiMin
	 *            Mindestanteil der FI-Titel am Gesamtportfolio
	 * @param keReRls
	 *            Kapitalertrag aus der Realisierung der Immobilien
	 * @param keRlsI
	 *            Kapitalertrag vor der 2. Realisierung
	 * @param zielKe
	 *            Ziel-Kapitalertrag (im Fachkonzept KE^Mindest)
	 * @param kaAufwendungen
	 *            Kapitalanlageaufwendungen (im Fachkonzept K_KA hoch EndeJ)
	 * @param keDiv
	 *            Kapitalertrag aus Dividenden
	 * @param keMiete
	 *            Kapitalertrag aus Mieten
	 * @param rapMiete
	 *            RAP, Miete
	 * @param cfFi
	 *            Cash Flow aus FI-Geschäft
	 * @param cfReRls
	 *            Cashflow aus der Realisierung der Immobilien
	 * @param cfOhneKA
	 *            Cashflow ohne KA
	 * @param omega
	 *            Projektionslänge Versicherungstechnik
	 * @param zeit
	 *            aktueller Zeitpunkt
	 * @return der Wert
	 */
	public static double aEqRlsII(final double bwEqRlsI, final double mwEqVorRls, final double bwFiGesamtJe,
			final double mwFiJahresende, final double bwReNachRls, final double bwVorRls, final double aReZiel,
			final double aFiZiel, final double aFiMin, final double keReRls, final double keRlsI, final double zielKe,
			final double kaAufwendungen, final double keDiv, final double keMiete, final double rapMiete,
			final double cfFi, final double cfReRls, final double cfOhneKA, final int omega, final int zeit) {
		if (zeit < omega && bwEqRlsI > 0.001 && mwEqVorRls > 0.001) {

			// A) Berechne Hilfsgrößen BWR_EQ, BWR_FI, KE_Gap_angepasst und CF_FreiRE_Plus
			final double bwrEq = mwEqVorRls - bwEqRlsI;
			final double bwrFi = mwFiJahresende - bwFiGesamtJe;
			final double keGapAngepasst = (keRlsI + keReRls + bwrFi) - (zielKe + kaAufwendungen);
			final double cfFreiRePlus = Math.max(cfFi + keDiv + keMiete + rapMiete + cfReRls - cfOhneKA, 0);

			// B) Berechne Komponenten a_EQ_Rls_* für a_EQ_Rls_II
			final double aEqRls1 = ((aFiZiel + aReZiel) * (bwVorRls + keReRls)
					- (bwFiGesamtJe + bwReNachRls + cfFreiRePlus)) / (mwEqVorRls - (aFiZiel + aReZiel) * bwrEq);
			final double aEqRls2 = (aFiZiel * (bwVorRls + keReRls) - (bwFiGesamtJe + cfFreiRePlus))
					/ (mwEqVorRls - aFiZiel * bwrEq);
			final double aEqRls3 = (aReZiel * (bwVorRls + keReRls) - (bwReNachRls + cfFreiRePlus))
					/ (mwEqVorRls - aReZiel * bwrEq);
			final double aEqRls6 = (aFiMin * (bwVorRls + keReRls) - (bwFiGesamtJe + cfFreiRePlus))
					/ (mwEqVorRls - aFiMin * bwrEq);

			final double aEqRls4, aEqRls5;
			if (bwrEq > 0) {
				aEqRls4 = -Math.min(keGapAngepasst, 0.0) / bwrEq;
				aEqRls5 = 0.0;
			} else {
				aEqRls4 = 0.0;
				if (Math.abs(bwrEq) < 1.0) {
					aEqRls5 = 0.0;
				} else {
					aEqRls5 = -Math.max(keGapAngepasst, 0.0) / bwrEq;
				}
			}

			// C) Berechne a_EQ_Rls_II
			if (bwrEq < 0) {
				return Math.min(1.0, Math.min(Math.max(aEqRls5, aEqRls6),
						Math.max(aEqRls1, Math.max(aEqRls2, Math.max(aEqRls3, Math.max(aEqRls6, 0.0))))));
			} else {
				return Math.min(1.0, Math.max(aEqRls1,
						Math.max(aEqRls2, Math.max(aEqRls3, Math.max(aEqRls4, Math.max(aEqRls6, 0.0))))));
			}

		} else if (zeit >= omega) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Kapitalertrag aus der 2. Realisierung der Aktien. <br/>
	 * Funktionsname in Excel: KE_EQ_Rls_II.
	 * 
	 * @param aEqRlsII
	 *            zu realisierender Anteil der Aktien bei der 2. Realisierung
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @return der Wert
	 */
	public static double keEqRlsII(final double aEqRlsII, final double mwEqVorRls, final double bwEqRlsI) {
		return aEqRlsII * (mwEqVorRls - bwEqRlsI);
	}

	/**
	 * Berechnet den Cashflow aus der 2. Realisierung der Aktien<br/>
	 * Funktionsname in Excel: CF_EQ_Rls_II.
	 * 
	 * @param aEqRlsII
	 *            zu realisierender Anteil der Aktien bei der 2. Realisierung
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @return der Wert
	 */
	public static double cfEqRlsII(final double aEqRlsII, final double mwEqVorRls) {
		return aEqRlsII * mwEqVorRls;
	}

	/**
	 * Berechnet den Buchwert der Aktien nach der 2. Realisierungn<br/>
	 * Funktionsname in Excel: BW_EQ_Rls_II.
	 * 
	 * @param aEqRlsII
	 *            zu realisierender Anteil der Aktien bei der 2. Realisierung
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @return der Wert
	 */
	public static double bwEqRlsII(final double aEqRlsII, final double bwEqRlsI) {
		return (1.0 - aEqRlsII) * bwEqRlsI;
	}

	/**
	 * Berechnet den Marktwert der Aktien nach der 2. Realisierung. <br/>
	 * Funktionsname in Excel: MW_EQ_Rls_II
	 * 
	 * @param aEqRlsII
	 *            zu realisierender Anteil der Aktien bei der 2. Realisierung
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @return der Wert
	 */
	public static double mwEqRlsII(final double aEqRlsII, final double mwEqVorRls) {
		return (1.0 - aEqRlsII) * mwEqVorRls;
	}

	/**
	 * Berechnet den Buchwert der Kapitalanlagen nach der 2. Realisierung zum Jahresende. <br/>
	 * Funktionsname in Excel: BW_Rls_II.
	 * 
	 * @param bwFiGesamtJe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param bwReNachRls
	 *            RE Buchwert nach der Realisierung
	 * @param bwEqRlsII
	 *            EQ Buchwert nach der 2. Realisierung
	 * @param cfFi
	 *            Cash Flow aus FI-Geschäft
	 * @param keMiete
	 *            Kapitalertrag aus Mieten
	 * @param keDiv
	 *            Kapitalertrag aus Dividenden
	 * @param cfReRls
	 *            Cashflow aus der Realisierung der Immobilien
	 * @param cfEqRlsII
	 *            Cashflow aus der 2. Realisierung der Aktien
	 * @param cfOhneKa
	 *            gesamte Cash-out-flows, ohne KA-Cashflows = CF_VT_ZAG_Steuer - Cash Flow aus Versicherungstechnik, ZAG
	 *            und Steuer inkl. CF_Kredit
	 * @param cfEndzahlung
	 *            Cash Flow, Endzahlung der Leistungen
	 * @param rapMiete
	 *            Rechnungsabgrenzungsposten: Mieten
	 * @return der Wert
	 */
	public static double bwRlsII(final double bwFiGesamtJe, final double bwReNachRls, final double bwEqRlsII,
			final double cfFi, final double keMiete, final double keDiv, final double cfReRls, final double cfEqRlsII,
			final double cfOhneKa, final double cfEndzahlung, final double rapMiete) {
		return bwFiGesamtJe + bwReNachRls + bwEqRlsII
				+ Math.max(cfFi + keDiv + keMiete + rapMiete + cfReRls + cfEqRlsII - cfOhneKa - cfEndzahlung, 0.0);
	}

	/**
	 * Berechnet den Marktwert der Kapitalanlagen nach der 2. Realisierung zum Jahresende. <br/>
	 * Funktionsname in Excel: MW_Rls_II.
	 * 
	 * 
	 * @param mwFIJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param mwReNachRls
	 *            RE Marktwert nach der Realisierung
	 * @param mwEqRlsII
	 *            EQ Marktwert nach der 2. Realisierung
	 * @param cfFi
	 *            Cash Flow aus FI-Geschäft
	 * @param keMiete
	 *            Kapitalertrag aus Mieten
	 * @param keDiv
	 *            Kapitalertrag aus Dividenden
	 * @param cfReRls
	 *            Cashflow aus der Realisierung der Immobilien
	 * @param cfEqRlsII
	 *            Cashflow aus der 2. Realisierung der Aktien
	 * @param cfOhneKA
	 *            gesamte Cash-out-flows, ohne KA-Cashflows = CF_VT_ZAG_Steuer - Cash Flow aus Versicherungstechnik, ZAG
	 *            und Steuer inkl. CF_Kredit
	 * @param rapMiete
	 *            Rechnungsabgrenzungsposten: Mieten
	 * @param cfEndzahlung
	 *            Cash Flow, Endzahlung der Leistungen
	 * @return der Wert
	 */
	public static double mwRlsII(final double mwFIJahresende, final double mwReNachRls, final double mwEqRlsII,
			final double cfFi, final double keMiete, final double keDiv, final double cfReRls, final double cfEqRlsII,
			final double cfOhneKA, final double rapMiete, final double cfEndzahlung) {
		return mwFIJahresende + mwReNachRls + mwEqRlsII
				+ Math.max(cfFi + keDiv + keMiete + rapMiete + cfReRls + cfEqRlsII - cfOhneKA - cfEndzahlung, 0);
	}

	/**
	 * Berechnet den Kapitalertrag nach der 2. Realisierung. <br/>
	 * Funktionsname in Excel: KE_Rls_II.
	 * 
	 * @param keRlsI
	 *            Kapitalertrag vor der 2. Realisierung
	 * @param keReRls
	 *            Kapitalertrag aus der Realisierung der Immobilien
	 * @param keEqRlsII
	 *            Kapitalertrag aus der 2. Realisierung der Aktien
	 * @return der Wert
	 */
	public static double keRlsII(final double keRlsI, final double keReRls, final double keEqRlsII) {
		return keRlsI + keReRls + keEqRlsII;
	}

	/**
	 * Berechnet den Gesamt-Cashflow inklusive zusätzlicher RW-Realisierungen. <br/>
	 * Funktionsname in Excel: CF_vorKredit.
	 * 
	 * @param cfFi
	 *            Cash Flow vor RW-Realisierung
	 * @param cfReRls
	 *            Cashflow aus der Realisierung der Immobilien
	 * @param cfEqRlsII
	 *            Cashflow aus der 2. Realisierung der Aktien
	 * @param cfVtZagSteuer
	 *            Cash Flow aus Versicherungstechnik, ZAG und Steuer inkl. CF_Kredit
	 * @param cfEndZahlung
	 *            Cash Flow, Endzahlung der Leistungen
	 * @param KE_Div
	 *            Kapitalertrag aus Dividenden
	 * @param KE_Miete
	 *            Kapitalertrag aus Mieten
	 * @param RAP_Miete
	 *            Rechnungsabgrenzungsposten: Mieten
	 * @return Gesamt-Cashflow
	 */
	public static double cfVorKredit(final double cfFi, final double cfReRls, final double cfEqRlsII,
			final double cfVtZagSteuer, final double cfEndZahlung, final double KE_Div, final double KE_Miete,
			final double RAP_Miete) {
		return cfFi + KE_Div + KE_Miete + RAP_Miete + cfReRls + cfEqRlsII - cfVtZagSteuer - cfEndZahlung;
	}

	/**
	 * Berechneten den bisher benötigten Kredit um die Liquidität aufrecht zu erhalten. <br/>
	 * Funktionsname in Excel: k.
	 * 
	 * @param cfVorKredit
	 *            Cash Flow vor Kreditaufnahme
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Projektionshorizont
	 * @return der Wert
	 */
	public static double k(final double cfVorKredit, final double t, final double omega) {
		if (t < omega) {
			return Math.max(-cfVorKredit, 0.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechneten den Kapitalaufwand des Kredites. <br/>
	 * Funktionsname in Excel: AK.
	 * 
	 * @param vgK
	 *            bisher benötigter Kredit um die Liquidität aufrecht zu erhalten
	 * @param vgKuponEsgII
	 *            injähriger Zins für das nächste Jahr zum Jahresende
	 * @param t
	 *            aktueller Zeitpunkt
	 * @return Kapitalaufwand
	 */
	public static double ak(final double vgK, final double vgKuponEsgII, final int t) {
		if (t == 0) {
			return 0.0;
		} else {
			return nanZero(vgK) * nanZero(vgKuponEsgII);
		}
	}

	/**
	 * Berechnet den zurückzuzahlenden Kredit inklusive Zinsen fuer das nächste Jahr. <br/>
	 * Funktionsname in Excel: CF_Kredit.
	 * 
	 * @param vgK
	 *            bisher benötigter Kredit um die Liquidität aufrecht zu erhalten
	 * @param zinsEinjaehrig
	 *            einjähriger Zins für das nächste Jahr zum Jahresende
	 * @param t
	 *            Zeitpunkt
	 * @return Kredit
	 */
	public static double cfKredit(final double vgK, final double zinsEinjaehrig, final int t) {
		if (t == 0) {
			return 0.0;
		} else {
			return nanZero(vgK) * (1.0 + zinsEinjaehrig);
		}

	}

	/**
	 * Berechnet den Gesamt-Cashflow inklusive zusätzlicher RW-Realisierungen. <br/>
	 * Funktionsname in Excel: CF_Neuanlage.
	 * 
	 * @param cfVorKredit
	 *            Gesamt-Cashflow
	 * @param krLiq
	 *            der bisher benötigte Kredit um die Liquidität aufrecht zu erhalten
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Projektionshorizont
	 * @return Gesamt-Cashflow
	 */
	public static double cfNeuanlage(final double cfVorKredit, final double krLiq, final double t, final double omega) {
		if (omega > t) {
			return cfVorKredit + krLiq;
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet das Volumen der Neuanlage in Immobilien gemessen am Buchwert. <br/>
	 * Funktionsname in Excel: BW_RE_NeuAnl.
	 * 
	 * @param aReZielV
	 *            Zielanteil der Immobilien am Gesamtportfolio aus dem Vorjahr
	 * @param bwVerrechnungJeV
	 *            BW der Kapitalanlagen nach Realisierung und Verrechnung des Kapitalertragsdefizits, Ende des Vorjahres
	 * @param bwReV
	 *            RE Buchwert
	 * @param cfNeuanlageV
	 *            Cashflow, der für die Neuanlage zur Verfügung steht
	 * @param bwFiNeuan
	 *            FI-Buchwert und Marktwert bei der Neuanlage
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Projektionshorizont
	 * @return der Wert
	 */
	public static double bwReNeuAnl(final double aReZielV, final double bwVerrechnungJeV, final double bwReV,
			final double cfNeuanlageV, final double bwFiNeuan, final int t, final int omega) {
		if (t < omega) {
			return Math.min(Math.max(0.0, nanZero(aReZielV) * nanZero(bwVerrechnungJeV) - nanZero(bwReV)),
					nanZero(cfNeuanlageV) - bwFiNeuan);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet die Nettoverzinsung. <br/>
	 * Funktionsname in Excel: NVZ.
	 * 
	 * @param ke
	 *            Gesamt-Kapitalertrag inklusive zusätzlicher Realisierungen
	 * @param kaAufwendungen
	 *            Kapitalanlagenaufwendungen
	 * @param kreditAufwendungen
	 *            Zinsaufwendungen für Liquditätskredit aus dem Vorjahr
	 * @param ziRaZu
	 *            Zinsratenzuschlag
	 * @param drVorD
	 *            Deckungsrückstellung vor Deklaration
	 * @param zzr
	 *            Zinszusatzreserve
	 * @param grk
	 *            Buchwert des Genussrechtkapitals
	 * @param drLockInV
	 *            Deckungsrückstellung, Lock-in, Vorjahr
	 * @param ekV
	 *            Eigenkapital, Vorjahr
	 * @param zzrV
	 *            Zinszusatzreserve, Vorjahr
	 * @param grkV
	 *            Genussrechtkapital
	 * @param zagTv
	 *            ZAG festgelegt, Vorjahr
	 * @param nfRfBv
	 *            nicht festgelegte RfB, Vorjahr
	 * @param steuerV
	 *            Steuer, Vorjahr
	 * @param kreditV
	 *            Liquditätskredit Vorjahr
	 * @param vuZeitpunkt
	 *            Fälligkeitsmonat der Cashflows
	 * @return der Wert
	 */
	public static double nvz(final double ke, final double kaAufwendungen, final double kreditAufwendungen,
			final double ziRaZu, final double drVorD, final double zzr, final double grk, final double drLockInV,
			final double ekV, final double zzrV, final double grkV, final double zagTv, final double nfRfBv,
			final double steuerV, final double kreditV, final double vuZeitpunkt) {
		final double hgb = drVorD + zzr + grk;
		final double hgbV = drLockInV + zzrV + grkV;

		if (hgb > 0.001 || hgbV > 0.001) {
			return (ke + kreditAufwendungen - kaAufwendungen - ziRaZu) / (0.5 * (hgb + hgbV) + ekV + nanZero(kreditV)
					+ nanZero(nfRfBv) + (nanZero(zagTv) + nanZero(steuerV)) * (vuZeitpunkt / 12.0));
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den Zeitpunkt der Fälligkeit der zum aktuellen Zeitpunkt angelegten FI-Anlagen. <br/>
	 * Funktionsname in Excel: ZP_Faelligkeit.
	 * 
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param rlz
	 *            Restlaufzeit
	 * @return Zeitpunkt der Fälligkeit
	 */
	public static int zpFaelligkeit(final int t, final int rlz) {
		return t + rlz - 1;
	}

	/**
	 * Restlaufzeit. <br/>
	 * Funktionsname in Excel: RLZ.
	 * 
	 * @param rlzZiel
	 *            Restlaufzeit der aktuellen Anlage
	 * @param omega
	 *            Projektionshorizont
	 * @param t
	 *            aktueller Zeitpunkt
	 * @return Restlaufzeit
	 */
	public static int rlz(final int rlzZiel, final int omega, final int t) {
		if (omega >= t) {
			return Math.min(rlzZiel, omega - t + 1);
		} else {
			return 0;
		}
	}

	/**
	 * Berechnet das Kapitalertragsdefizit aus den Vorjahren zum Verrechnen. <br/>
	 * Funktionsname in Excel: KED_VJ_Verrechnen.
	 * 
	 * @param t
	 *            aktuelles Jahr
	 * @param manRec
	 *            die Zeitabh. manRec für Spalte mt den Verrechnungszeiträumen
	 * @param kedVerrechnung
	 *            Spalte, in der die Kapitalertragsdefizite zum Verrechnen stehen
	 * @param omega
	 *            Länge der Projektionszeitraumes
	 * @return Kapitalertragsdefizit aus den Vorjahren zum Verrechnen
	 */
	public static double kedVjVerrechnen(final int t, final ZeitabhManReg manRec, final double[] kedVerrechnung,
			final int omega) {
		double kedVjVerrechnen = 0.0;
		if (t < 2) {
			return kedVjVerrechnen;
		}
		if (t < omega) {
			for (int i = 1; i < t; i++) {
				int vzr = manRec.get(i).getFiBwr();
				if (i + vzr >= t) {
					kedVjVerrechnen = kedVjVerrechnen + (nanZero(kedVerrechnung[i]) / vzr);
				}
			}
		} else {
			for (int i = 1; i <= omega - 1; i++) {
				int vzr = manRec.get(i).getFiBwr();
				if (i + vzr >= omega) {
					kedVjVerrechnen = kedVjVerrechnen + ((vzr - omega + i + 1) * nanZero(kedVerrechnung[i] / vzr));
				}
			}
		}

		return kedVjVerrechnen;
	}

	/**
	 * Berechnet das Kapitalertragsdefizit zum Verrechnen zu einem gewählten Zeitpunkt. <br/>
	 * Funktionsname in Excel: KED_Verrechnung.
	 * 
	 * @param zielKe
	 *            Ziel-Kapitalertrag
	 * @param keRlsII
	 *            Kapitalertrag Gesamt nach der 2. Realisierung
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param bwFiGesamtJe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param kaAufwendungen
	 *            Kapitalanlagenaufwendungen
	 * @return Kapitalertragsdefizit zum Verrechnen
	 */
	public static double kedVerrechnung(final double zielKe, final double keRlsII, final double mwFiJahresende,
			final double bwFiGesamtJe, final double kaAufwendungen) {
		final double bwrFi = mwFiJahresende - bwFiGesamtJe;
		final double keDiff = zielKe + kaAufwendungen - keRlsII;

		if (keDiff > 0 && bwrFi > 0) {
			return Math.min(keDiff, bwrFi);
		} else {
			if (keDiff < 0 && bwrFi < 0) {
				return Math.max(keDiff, bwrFi);
			}
			return 0.0;
		}
	}

	/**
	 * Berechnet den Kapitalertrag nach Verrechnung zu einem gewählten Zeitpunkt. <br/>
	 * Funktionsname in Excel: KE_Verrechnung.
	 * 
	 * @param keRlsII
	 *            Kapitalertrag Gesamt nach der 2. Realisierung
	 * @param kedVerrechnung
	 *            Kapitalertragdefizit zum Verrechnen zu einem gewählten Zeitpunkt
	 * @param ziRaZuStochAgg
	 *            Zinsartenzuschlag
	 * @return Kapitalertrag nach Verrechnung
	 */
	public static double keVerrechnung(final double keRlsII, final double kedVerrechnung, final double ziRaZuStochAgg) {
		return keRlsII + kedVerrechnung + ziRaZuStochAgg;
	}

	/**
	 * Berechnet den gesamten Buchwert der Kapitalanlagen nach Realisierung und Verrechnung des Kapitalertragsdefizits.
	 * <br/>
	 * Funktionsname in Excel: BW_Verrechnung_JE.
	 * 
	 * @param bwRlsII
	 *            Buchwert der Kapitalanlagen nach der 2. Realisierung
	 * @param kedVerrechnung
	 *            Kapitalertragsdefizit zum Verrechnen
	 * @return der Wert
	 */
	public static double bwVerrechnungJe(final double bwRlsII, final double kedVerrechnung) {
		return bwRlsII + kedVerrechnung;
	}

	/**
	 * Berechnet den Buchwert der Zinstitel am Ende des Jahres nach der Verechnung. <br/>
	 * Funktionsname in Excel: BW_FI_Verechnung_JE.
	 * 
	 * @param bwFiGesamtJe
	 *            Buchwert der Zinstitel am Ende des Jahres
	 * @param kedVerrechnung
	 *            Kapitalertragsdefizit zum Verrechnen
	 * @return Buchwert
	 */
	public static double bwFiVerechnungJe(final double bwFiGesamtJe, final double kedVerrechnung) {
		return bwFiGesamtJe + kedVerrechnung;
	}

	/**
	 * Berechnet die CF FIs zur selben Zeit. Indiziert über die Restlaufzeit. <br/>
	 * Funktionsname in Excel: CF_FI_s.
	 * 
	 * @param rlz
	 *            Restlaufzeit
	 * @param maxLZ
	 *            maximale Laufzeit der Neuanlage
	 * @param bwFiNeuAn
	 *            Buchwert des Fixed Income
	 * @param kuponEsg
	 *            Kupon zum Fixed Income
	 * @param t
	 *            aktueller Zeitpunkt
	 * @param cfFiJe
	 *            CF mit Berücksichtigung des Ausfalls, mit Aufzinsung
	 * @return CF_FIs
	 */
	public static double cfFis(final int rlz, final int maxLZ, final double bwFiNeuAn, final double kuponEsg,
			final int t, final double cfFiJe) {
		if (t == 0 && rlz == 0) {
			return 0.0;
		} else if (t == 0) {
			return cfFiJe;
		} else {
			if (rlz + 1 < maxLZ) {
				return bwFiNeuAn * kuponEsg;
			} else if (rlz + 1 == maxLZ) {
				return bwFiNeuAn * kuponEsg + bwFiNeuAn;
			} else {
				return 0.0;
			}
		}
	}

	/**
	 * Berechnet den Barwert eines Cash Flow mit einer vorgegebenen Zinsstrukturkurve. <br/>
	 * Funktionsname in Excel: MW_FI_Jahresende.
	 * 
	 * @param rlz
	 *            FI Restlaufzeit der Neuanlage
	 * @param cfFiZeitschrittig
	 *            Zeilenvektor, der den Cash Flow zu den vorgegebenen Zeitpunkten enthält
	 * @param pfadZeile
	 *            Zeile eines Pfades
	 * @param pfad
	 *            gewünschter Pfad
	 * @param zeit
	 *            Zeit
	 * @param bwAktivaFiCf
	 *            FI Cash Flow aus Blatt BW Aktiva FI
	 * @param fiNeuanlageRestlaufzeit
	 *            FI Neuanlage Restlaufzeit
	 * @return Barwert
	 */
	public static double mwFiJahresende(final int rlz, final double[] cfFiZeitschrittig, final PfadZeile pfadZeile,
			final int pfad, final int zeit, final int bwAktivaFiCf, final int fiNeuanlageRestlaufzeit) {

		int max = Math.max(bwAktivaFiCf, fiNeuanlageRestlaufzeit);
		double mwFiJahresende = 0.0;
		for (int t = 1; t <= max; ++t) {
			mwFiJahresende += cfFiZeitschrittig[t] / Math.pow((1.0 + pfadZeile.getSpotRlz(t)), t);
		}
		return mwFiJahresende;
	}

	/**
	 * Berechnet den Barwert eines Cash Flow mit einer vorgegebenen Zinsstrukturkurve. <br/>
	 * Funktionsname in Excel: MW_VT.
	 * 
	 * @param rlz
	 *            FI Restlaufzeit der Neuanlage
	 * @param leistCf
	 *            Zeilenvektor, der den garantierten Leistungscashflows zu den vorgegebenen Zeitpunkten enthält
	 * @param restCf
	 *            Zeilenvektor, der den restlichen garantierten Cashflows zu den vorgegebenen Zeitpunkten enthält
	 * @param kaAaufwendungen
	 *            Zeilenvektor, der den restlichen garantierten Cashflows zu den vorgegebenen Zeitpunkten enthält
	 * @param le
	 *            Faktor, Leistungserhöhung
	 * @param aVn
	 *            Faktor, stoch. Anpassung durch VN-Verhalten
	 * @param aKa
	 *            Faktor, stoch. Anpassung der KA-Aufwendungen
	 * @param pfadZeile
	 *            Zeile eines Pfades
	 * @param pfad
	 *            aktueller Pfad
	 * @param zeit
	 *            Zeit
	 * @param omega
	 *            Zeitspanne
	 * @param vuZeit
	 *            VU_Zeitpunkt für Versicherungstechnik
	 * 
	 * @return Barwert
	 */
	public static double mwVt(final int rlz, final double[] leistCf, final double[] restCf,
			final double[] kaAaufwendungen, final double le, final double aVn, final double aKa,
			final PfadZeile pfadZeile, final int pfad, final int zeit, final int omega, final double vuZeit) {
		double mwVt = 0.0;
		final double vu = vuZeit / 12.0;
		double dummyV = 0.0;

		for (int t = 1; t <= omega - zeit; ++t) {
			final double spot = pfadZeile.getSpotRlz(t);
			double dummy = 1.0 / (1.0 + spot);
			final double df = Math.pow(dummy, t);

			final double dfVu;
			if (t == 1) {
				dfVu = 1 / Math.pow(1.0 + spot, t * vu);
			} else {
				dfVu = Math.pow(dummyV, (t - 1.0) * (1.0 - vu)) * Math.pow(dummy, t * vu);
				dummyV = 1.0 / (1.0 + spot);
			}

			mwVt += (le * leistCf[t + zeit - 1] + aVn * restCf[t + zeit - 1]) * dfVu
					+ aKa * kaAaufwendungen[t + zeit - 1] * df;
			dummyV = 1.0 / (1.0 + spot);
		}
		return mwVt;
	}

	/**
	 * Passive Bewertungsreserven/-laseten. <br/>
	 * Funktionsname in Excel :BWR_Pas.
	 * 
	 * @param drstVorDekl
	 *            Deckungsrückstellung vor Deklaration
	 * @param zzr
	 *            Zinszusatzreserve
	 * @param mwVt
	 *            Marktwert der versicherungstechnungen Rückstellungen
	 * @return Passive Bewertungsreserven/-laseten
	 */
	public static double bwrPas(final double drstVorDekl, final double zzr, final double mwVt) {
		return drstVorDekl + zzr - mwVt;
	}

	/**
	 * Bestimmt die maximale Länge des Projektionszeitraums je nach Deckungsstock für KDS: letzter Zeitpunkt mit
	 * Deckungsrückstellung > 0 für Fonds: letzter Zeitpunkt mit Risiko-/übrigem Ergebnis > 0. <br/>
	 * Funktionsname in Excel : Laenge_Projektion_DR
	 * 
	 * @param Deckungsstock
	 *            Deckungsstock
	 * @param zeilen
	 *            Alle rzg-Zeilen mit gleichem lob, zins, altNeu und deckungsStock
	 * 
	 * @return den Wert
	 * 
	 */
	public static int laengeProjektionDR(final String Deckungsstock, final List<RzgZeile> zeilen) {
		int index = 0;
		switch (Deckungsstock) {
		case RzgZeile.DECKUNGS_STOCK_KDS:
			for (RzgZeile z : zeilen) {
				if (z.getDrDet() != 0.0 || (z.getRisikoErgebnis() + z.getUebrigesErgebnis() != 0.0)) {
					index = z.getZeit();
				}
			}
			break;
		case RzgZeile.DECKUNGS_STOCK_FONDS:
			for (RzgZeile z : zeilen) {
				if (z.getRisikoErgebnis() + z.getUebrigesErgebnis() != 0.0) {
					index = z.getZeit();
				}
			}
			break;
		default:
			throw new IllegalArgumentException("unbekanter Deckungsstock");

		}
		return index;
	}

	/**
	 * Zins den Cashflow aufs Jahresende mit dem mittleren ESG Zins im Jahr T auf. <br/>
	 * Funktionsname in Excel: Aufzinsung.
	 * 
	 * @param cashflow
	 *            ein Cash Flow zum Aufzinsen
	 * @param jaehrlZins
	 *            jährlicher ESG-Zins zum Aufzinsen
	 * @param monat
	 *            Monat
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Länge der Projektion der DRSt
	 * @return Cashflow
	 */
	public static double aufzinsung(final double cashflow, final double jaehrlZins, final double monat, final int t,
			final int omega) {
		if (t <= omega) {
			return nanZero(cashflow) * Math.pow(1.0 + jaehrlZins, 1.0 - monat / 12.0);
		} else {
			return 0.0;
		}
	}

	/**
	 * berechnet den Cashflow aus übrigem Ergebnis an das Neugeschäft. <br/>
	 * Funktionsname in Excel: CF_uebrE_NG.
	 * 
	 * @param ubrErgAlt
	 *            übrig. Ergebnis des Altbestandes
	 * @param ubrErgNeu
	 *            übrig. Ergebnis des Neubestandes
	 * @param ubrErgBestandAlt
	 *            Bestand (ohne GCR) des Altbestands
	 * @param ubrErgBestandNeu
	 *            Bestand (ohne GCR) des Neubestands
	 * @return der Wert
	 */
	public static double cfUebrEng(final double ubrErgAlt, final double ubrErgNeu, final double ubrErgBestandAlt,
			final double ubrErgBestandNeu) {
		return ubrErgAlt + ubrErgNeu - (ubrErgBestandAlt + ubrErgBestandNeu);
	}

	/**
	 * Ohne Kommentar. <br/>
	 * Funktionsname in Excel: CfGcrRzg.
	 * 
	 * @param kennzeichenUeb
	 *            ?
	 * @param uE
	 *            ?
	 * @param ueAlt
	 *            ?
	 * @param ueNeu
	 *            ?
	 * @param gcrAgg
	 *            ?
	 * @return der Wert
	 */
	public static double cfGcrRzg(final String kennzeichenUeb, final double uE, final double ueAlt, final double ueNeu,
			final double gcrAgg) {
		if (kennzeichenUeb.equals("UEB") && ueAlt + ueNeu > 0.001) {
			return uE / (ueAlt + ueNeu) * gcrAgg;
		}
		return 0.0;
	}

	/**
	 * Berechnet die Aufwendungen für die Kapitalanlage <br/>
	 * Funktionsname in Excel: KA_Aufwendungen
	 * 
	 * @param bwFi
	 *            Buchwert der Zinstitel
	 * @param bwRe
	 *            Buchwert der Immobilien
	 * @param bwEq
	 *            Buchwert der Aktien
	 * @param kv
	 *            Liquiditätskredit aus dem Vorjahr
	 * @param kaFaktor
	 *            Faktor: Aufwendungen für Kapitalanlagen
	 * @param kaFaktorStress
	 *            Faktor: Aufwendungen für Kapitalanlagen im Kostenstress
	 * @param anteilLobsKaStress
	 *            Anteil jener Lobs, deren KA-Kosten gestresst werden
	 * @return der Wert
	 * 
	 */
	public static double kaAufwendungen(final double bwFi, final double bwRe, final double bwEq, final double kv,
			final double kaFaktor, final double kaFaktorStress, final double anteilLobsKaStress) {

		return (anteilLobsKaStress * kaFaktorStress + (1.0 - anteilLobsKaStress) * kaFaktor)
				* (bwFi + bwEq + bwRe - nanZero(kv));

	}

	/**
	 * Berechnet den Anteil am Deckungsstock, der zu den LoBs gehört, deren Kapitalanlagekosten im aktuellen Szenario
	 * gestresst werden. Im Gegensatz zu Excel wird hier ein Array mit den rzg zur selben Zeit übergeben. <br/>
	 * Funktionsname in Excel: AnteilLobsKaStress
	 * 
	 * @param rzgZurSelbenZeit
	 *            Liste aller rzg-Zeilen zur selben Zeit wie die der aktuell zu berechnenden agg-Zeile
	 * @param sznr
	 *            aktuelles Stress-Szenario
	 * @param drLockInV
	 *            Lock-In-Drst Gesamtbestand, Vorjahr
	 * @param sueAfV
	 *            SÜAF Gesamtbestand, Vorjahr
	 * @param zzrV
	 *            ZZR Gesamtbestand, Vorjahr
	 * @return der Wert
	 */
	public static double anteilLobsKaStress(final List<RzgZeile> rzgZurSelbenZeit, final int sznr,
			final double drLockInV, final double sueAfV, final double zzrV) {
		double AnteilLobsKaStress = 0.0;
		for (RzgZeile rzg : rzgZurSelbenZeit) {
			if (sznr == rzg.getKaKostenstressDerLob()) {
				AnteilLobsKaStress += rzg.getVg().getDrLockInRzg() + rzg.getVg().getsUeAfRzg() + rzg.getVg().getZzrJ();
			}
		}
		if (drLockInV + sueAfV + zzrV > 0.001) {
			return AnteilLobsKaStress / (drLockInV + sueAfV + zzrV);
		} else {
			return 0.0;
		}
	}

	/**
	 * Berechnet den mittleren Rechnungszins. <br/>
	 * Funktionsname in Excel: rz_mittel.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Länge der Projektion der DRSt
	 * @param rmzUebAlt
	 *            Rechnungsmäßige Zinsen aggregiert über alle Bestandsgruppen des Altbestandes
	 * @param rmzUebNeu
	 *            Rechnungsmäßige Zinsen aggregiert über alle Bestandsgruppen des Neubestandes
	 * @param rmzNueb
	 *            Rechnungsmäßige Zinsen eines Geschäftszweiges über die einzelnen Bestandsgruppen, welche zu dem
	 *            Geschäftszweig gehören
	 * @param drstDet
	 *            Deterministisch projezierte Deckungsrückstellung
	 * @param drstV
	 *            Deckungsrückstellung für die garantierten Leistungen inkl. Lock-In
	 * @param drstVorDekl
	 *            Deckungsrückstellung vor Deklaration zum Ende des Jahres t
	 * @return Rechnungszins
	 */
	public static double rzMittel(final double t, final double omega, final double rmzUebAlt, final double rmzUebNeu,
			final double rmzNueb, final double drstDet, final double drstV, final double drstVorDekl) {
		if (t == 1) {
			return (rmzUebAlt + rmzUebNeu + rmzNueb) * 2 / (drstDet + drstVorDekl);
		} else if (t < omega) {
			return (rmzUebAlt + rmzUebNeu + rmzNueb) * 2 / (drstV + drstVorDekl);
		} else {
			return 0.0;
		}
	}

	/**
	 * Gibt den Zielanteil der FI-Titel an den Kapitalanlagen an. Wird für dynamische Asset Allokation (DAA) verwendet.
	 * <br/>
	 * Funktionsname in Excel: a_FI_Ziel_daa.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Länge der Projektion der Deckungsrückstellung
	 * @param methode
	 *            Steuerungsmethode Asset Allokation
	 * @param rzMittel
	 *            Mittlere Rechnungszins zum Zeitpunkt T
	 * @param rEq
	 *            DAA-Faktor auf EQ-BWR
	 * @param rFi
	 *            DAA-Faktor auf FI-BWR
	 * @param rEqVerluste
	 *            DAA-Faktor auf EQ-Verluste
	 * @param rResDaa
	 *            Grenzwert aktive und passive Reserven für gute ökonomische Lage
	 * @param aFiZielKonst
	 *            Zielanteil der FI-Titel, wenn DAA "gute wirtschaftl. Lage" auslöst
	 * @param aFiMax
	 *            Zielanteil der FI-Titel, wenn DAA "schlechte wirtschaftl. Lage" auslöst
	 * @param aFiZielVj
	 * 			Anteil der FI-Tiel an gesamten Kapitalanlagen im Vorjahr
	 * @param aFiZielInitial
	 * 			Initialwert a_FI_Ziel(s, 0) für die Umschichtung
	 * @param keRlsI
	 *            Kapitalertrag vor der 2. Realisierung
	 * @param bwVorRls
	 *            Buchwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param mwVorRls
	 *            Marktwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param bwFiGesamtJe
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param bwEqRlsI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @param bwrPas
	 *            Passive Bewertungsreserven/Lasten
	 * @param drVorDekl
	 *            Deckungsrückstellung vor Deklaration
	 * @param hZiel
	 * 			Schrittweite für FI-Titel in Richtung Zielanteil, wenn DAA gute wirtschaftl. Lage auslöst
	 * @param hMax
	 * 			Schrittweite für FI-Titel in Richtung Maximalanteil, wenn DAA schlechte wirtschaftl. Lage auslöst
	 * @return Zielanteil
	 */
	public static double aFiZielDaa(final int t, final int omega, final int methode, final double rzMittel,
			final double rEq, final double rFi, final double rEqVerluste, final double rResDaa,
			final double aFiZielKonst, final double aFiMax, final double aFiZielVj, final double aFiZielInitial, 
			final double keRlsI, final double bwVorRls, final double mwVorRls, final double bwFiGesamtJe, 
			final double mwFiJahresende, final double bwEqRlsI, final double mwEqVorRls, final double bwrPas, final double drVorDekl, final double hZiel, final double hMax) {
		double aFiZielDaa = 0.0;
		final double mwGesamt = mwVorRls;
		final double bwGesamt = bwVorRls;

		if (t == 0 || Math.abs(drVorDekl) < 0.001 || t > omega || mwGesamt <= 0 || bwGesamt <= 0) {
			aFiZielDaa = aFiZielInitial;
		} else {
			switch (methode) {
			case 0: // Statische Asset Allokation
				aFiZielDaa = aFiZielKonst;
				break;
			case 1: // Dynamische Asset Allokation Methode 1: mittl. RZ
					// vs. KE
				final double ls = (keRlsI + +rEq * (mwEqVorRls - bwEqRlsI)
						+ rFi * Math.max(mwFiJahresende - bwFiGesamtJe, 0.0)) / bwGesamt
						- rEqVerluste * mwEqVorRls / mwGesamt;
				if (ls > rzMittel) {
					if (aFiZielKonst > aFiZielVj) {
						aFiZielDaa = Math.min(aFiZielKonst, aFiZielVj + hZiel);
					} else {
						aFiZielDaa = Math.max(aFiZielKonst, aFiZielVj - hZiel);
					}
				} else {
					if (aFiMax > aFiZielVj) {
						aFiZielDaa = Math.min(aFiMax, aFiZielVj + hMax);
					} else {
						aFiZielDaa = Math.max(aFiMax, aFiZielVj - hMax);
					}
				}
				break;
			case 2: // Dynamische Asset Allokation Methode 1: Anteil der
					// passiven und aktiven Reserven
				if ((bwrPas + mwGesamt - bwGesamt) / drVorDekl > rResDaa) {
					if (aFiZielKonst > aFiZielVj) {
						aFiZielDaa = Math.min(aFiZielKonst, aFiZielVj + hZiel);
					} else {
						aFiZielDaa = Math.max(aFiZielKonst, aFiZielVj - hZiel);
					}
				} else {
					if (aFiMax > aFiZielVj) {
						aFiZielDaa = Math.min(aFiMax, aFiZielVj + hMax);
					} else {
						aFiZielDaa = Math.max(aFiMax, aFiZielVj - hMax);
					}
				}
				break;
			}
		}
		return aFiZielDaa;
	}

	/**
	 * Gibt den Mindstanteil der FI-Titel an den Kapitalanlagen an. Wird für dynamische Asset Allokation (DAA)
	 * verwendet. Funktionsname in Excel: a_FI_min_daa.
	 * 
	 * @param t
	 *            Zeitpunkt
	 * @param omega
	 *            Länge der Projektion der Deckungsrückstellung
	 * @param methode
	 *            Steuerungsmethode Asset Allokation
	 * @param rzMittl
	 *            Mittlere Rechnungszins zum Zeitpunkt T
	 * @param rEq
	 *            DAA-Faktor auf EQ-BWR
	 * @param rFi
	 *            DAA-Faktor auf FI-BWR
	 * @param rEqVerlust
	 *            DAA-Faktor auf EQ-Verluste
	 * @param rResDaa
	 *            Grenzwert aktive und passive Reserven für gute ökonomische Lage
	 * @param aFiMinKonst
	 *            Mindestanteil der FI-Titel, wenn DAA "gute wirtschaftl. Lage" auslöst
	 * @param aFiMax
	 *            Mindestanteil der FI-Titel, wenn DAA "schlechte wirtschaftl. Lage" auslöst
	 * @param aFiMinVj
	 * 			Anteil der FI-Titel an der gesamten Kapitalanlagen im Vorjahr
	 * @param aFiMinInitial
	 * 			Initialwert a_FI_Min(s, 0) für die Umschichtung
	 * @param keRlsI
	 *            Kapitalertrag vor der 2. Realisierung
	 * @param bwVorRls
	 *            Buchwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param mwVorRls
	 *            Marktwert der gesamten Kapitalanlagen vor der 2. Realisierung
	 * @param bwFiGesamtJE
	 *            Buchwert des FI-Bestandes zum Ende des Jahres
	 * @param mwFiJahresende
	 *            Marktwert des FI-Bestandes zum Ende des Jahres
	 * @param bwEqRlsqI
	 *            EQ Buchwert nach planmäßiger Realisierung der EQ-BWR
	 * @param mwEqVorRls
	 *            EQ Marktwert vor der Neuanlage
	 * @param bwrPas
	 *            Passive Bewertungsreserven/Lasten
	 * @param drVorDekl
	 *            Deckungsrückstellung vor Deklaration
	 * @param hZiel
	 * 			Schrittweite für FI-Titel in Richtung Zielanteil, wenn DAA gute wirtschaftl. Lage auslöst
	 * @param hMax
	 * 			Schrittweite für FI-Titel in Richtung Maximalanteil, wenn DAA schlechte wirtschaftl. Lage auslöst
	 * @return der Wert
	 */
	public static double aFiMinDaa(final int t, final int omega, final int methode, final double rzMittl,
			final double rEq, final double rFi, final double rEqVerlust, final double rResDaa, final double aFiMinKonst,
			final double aFiMax, final double aFiMinVj, final double aFiMinInitial, final double keRlsI, 
			final double bwVorRls, final double mwVorRls, final double bwFiGesamtJE, final double mwFiJahresende, 
			final double bwEqRlsqI, final double mwEqVorRls, final double bwrPas, 
			final double drVorDekl, final double hZiel, final double hMax) {

		double mwGesamt = mwVorRls;
		double bwGesamt = bwVorRls;

		if (t == 0 || Math.abs(drVorDekl) < 0.001 || t > omega || mwGesamt <= 0.0 || bwGesamt <= 0.0) {
			return aFiMinInitial;
		} else {
			switch (methode) {
			case 0: // 'Statische Asset Allokation
				return aFiMinKonst;
			case 1:// 'Dynamische Asset Allokation Methode 1: mittl. RZ vs. KE
				final double vgl = (keRlsI + rEq * (mwEqVorRls - bwEqRlsqI)
						+ rFi * Math.max(mwFiJahresende - bwFiGesamtJE, 0)) / bwGesamt
						- rEqVerlust * mwEqVorRls / mwGesamt;
				if (vgl > rzMittl) {
					if (aFiMinKonst > aFiMinVj) { 
						return Math.min(aFiMinKonst, aFiMinVj + hZiel);
					} else {
						return Math.max(aFiMinKonst, aFiMinVj - hZiel);
					}
				} else {
					if (aFiMax > aFiMinVj) { 
						return Math.min(aFiMax, aFiMinVj + hMax);
					} else {
						return Math.max(aFiMax, aFiMinVj - hMax);
					}
				}
			case 2:// 'Dynamische Asset Allokation Methode 1: Anteil der passiven und aktiven Reserven
				if ((bwrPas + mwGesamt - bwGesamt) / drVorDekl > rResDaa) {
					if (aFiMinKonst > aFiMinVj) { 
						return Math.min(aFiMinKonst, aFiMinVj + hZiel);
					} else {
						return Math.max(aFiMinKonst, aFiMinVj - hZiel);
					}
				} else {
					if (aFiMax > aFiMinVj) { 
						return Math.min(aFiMax, aFiMinVj + hMax);
					} else {
						return Math.max(aFiMax, aFiMinVj - hMax);
					}
				}
			default:
				return 0.0;
			}
		}
	}

	/**
	 * Undokumentiert, in Excel steht nur =GI6-GJ6+GK6+GM6+GN6+GO6-GP6+AM6+AN6+DH6+ED6+DJ6. <br/>
	 * Funktionsname in Excel: CF_ohneKA.
	 * 
	 * @param lGes
	 *            undokumentiert
	 * @param bStoch
	 *            undokumentiert
	 * @param kStoch
	 *            undokumentiert
	 * @param zag
	 *            undokumentiert
	 * @param steuer
	 *            undokumentiert
	 * @param rv
	 *            undokumentiert
	 * @param reUeAufschub
	 *            undokumentiert
	 * @param zinsGrnrd
	 *            undokumentiert
	 * @param rueckzahlungGrnrd
	 *            undokumentiert
	 * @param rueckzahlungKredit
	 *            undokumentiert
	 * @param gcr
	 *            undokumentiert
	 * @param kaKosten
	 *            undokumentiert
	 * @return der Wert
	 */
	public static double cfOhneKa(final double lGes, final double bStoch, final double kStoch, final double zag,
			final double steuer, final double rv, final double reUeAufschub, final double zinsGrnrd,
			final double rueckzahlungGrnrd, final double rueckzahlungKredit, final double gcr, final double kaKosten) {

		// '
		return lGes - bStoch + kStoch + zag + steuer + rv - reUeAufschub + zinsGrnrd + rueckzahlungGrnrd
				+ rueckzahlungKredit + gcr + kaKosten;
	}

	/**
	 * Berechnet den Anteil der Kosten für die Kapitalanlage, der auf die Bestandsgruppe entfällt. <br/>
	 * Funktionsname in Excel: Kosten_KA_rzg.
	 * 
	 * @param drJ
	 *            Deckungsrückstellung Lock-In der Bestandsgruppe
	 * @param drGes
	 *            Deckungsrückstellung Lock-In gesamt (aggregiert über alle Bestandsgruppen)
	 * @param kaKostenGes
	 *            Gesamte Kosten für die Kapitalanlage
	 * @return Anteil der Kosten
	 */
	public static double kostenKaRzg(final double drJ, final double drGes, final double kaKostenGes) {
		if (Math.abs(drGes) > 0.001 && kaKostenGes != 0.0) {
			return drJ * kaKostenGes / drGes;
		} else {
			return 0.0;
		}
	}

}

