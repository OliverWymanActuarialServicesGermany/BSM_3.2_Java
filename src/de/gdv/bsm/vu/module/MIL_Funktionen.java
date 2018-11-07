package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.nanZero;

import de.gdv.bsm.intern.params.VUHistorie;

public class MIL_Funktionen {

	/**
	 * MIL_W.Schalesi Funktionen des Excel-Moduls <code>MIL_Funfktionen</code>.
	 * 
	 * @param aFiMinDaa
	 *            Mindestanteil FI
	 * @param aFiMinDaaOld
	 *            Mindestanteil FI Zeitpunkt vorher
	 * @param aFiZielDaa
	 *            Zielanteil FI
	 * @param zehnJZins
	 *            Zins einer 10 -jährigen Nullkupon-Anleihe
	 * @param untereSchranke
	 *            Untere Schranke fuer RW Trigger
	 * @param obereSchranke
	 *            Obere Schranke fuer RW Trigger
	 */
	public static double mindestanteilFI_Stetig(double zehnJZins, double untereSchranke, double obereSchranke,
			double aFiMinDaa, double aFiZielDaa) {

		double m;

		if (zehnJZins > obereSchranke)
			return aFiMinDaa;

		if (zehnJZins < untereSchranke)
			return aFiZielDaa;
		if (zehnJZins >= untereSchranke && zehnJZins <= obereSchranke) {
			m = (aFiMinDaa - aFiZielDaa) / (obereSchranke - untereSchranke);
			return (aFiZielDaa + (zehnJZins - untereSchranke) * m);
		} else
			return 0;

	}

	/**
	 * Berechnet den realisierten Jahresüberschuss und die Rückstellungen für
	 * Beitragsrückerstattungszuführung und gibt das gewünschte Ergebnis zurück.
	 * Name in Excel: JUE_RfBZuf.
	 * 
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rohueberschuss
	 *            Rohüberschuss
	 * @param mindZfGes
	 *            Mindestzuführung gesamt
	 * @param mindZf
	 *            Mindestzuführung
	 * @param jueZiel
	 *            Ziel-Jahresüberschuss
	 * @param ueeBestand
	 *            Kostenüberschuss Bestand
	 * @param freieRfbVj
	 *            freie RfB zum Zeitpunkt t - 1
	 * @param rfbZufVj
	 *            Zuführung zur Rückstellung für Beitragsrückerstattung aus den
	 *            Vorjahren
	 * @param vuHistorie
	 *            historische Werte für die RfB Zuführung und den Rohüberschuss
	 *            (in Excel zwei Parameter!)
	 * @param pRohueb
	 *            Managementregel (Prozentwert der angibt, wie hoch die
	 *            Beteiligung der VN am Rohüberschuss sein soll)
	 * @param i56bfRfB
	 *            Managementregel (Soll im Fall einer negativen
	 *            Rohüberschussbeteiligung eine Entnahme aus der freien RfB
	 *            vorgenommen werden
	 * @param i56bSueaf
	 *            Managementregel (Soll im Fall einer negativen
	 *            Rohüberschussbeteiligung eine Entnahme aus dem // SÜAF
	 *            vorgenommen werden
	 * @param sueafVj
	 *            SÜAF aus dem Vorjahr
	 * @param strategie
	 *            Strategie zur Ermittlung der Beteiligung der
	 *            Versicherungsnehmer und Jahresüberschuss (1 = Zielverzinsung,
	 *            2 = Zielbeteiligung)
	 * @param ergebnis
	 *            Marker, der angibt, welches Ergebnis zurückgegeben werden soll
	 *            (1 - realisierter Jahresüberschuss, 2 - RfB Zuf, 3 - RfB
	 *            56b-Entnahme)
	 * @param ek
	 *            Wert des Eigenkapitals im Jahr t-1
	 * @param bwGrk
	 *            Buchwert des Genussrechtkapitals und Nachrangsdarlehen
	 * @param nvz
	 *            Nettoverzinsung des aktuellen Jahres
	 * @param drVorDeklUeb
	 *            Deckungsrückstellung vor Deklaration zum Ende des Jahres t
	 * @param drLockInUebV
	 *            Deckungsrückstellung für garantierte Leistungen zum Ende des
	 *            Jahres t-1
	 * @param rfb56bVj
	 *            Höhe der Entnahme aus der nicht festgelegten RfB gemäß §56b
	 *            VAG zum Zeitpunkt t-1
	 * @param ziRaZu
	 *            Zinsratenzuschlag
	 * @param drNueb
	 *            Deckungsrückstellung, NÜB
	 * @param deltaNvz
	 *            Differenz zwischen NVZ vom Pfad von 0 im BE und
	 *            Verzinsungsanforderung des handelsrechtlichen Eigenkapital
	 *            r_EK zeitabh.ManReg Spalte K
	 * @param vgEigenkapitalFortschreibung
	 *            Eigenkapital vom Vorjahr
	 * @param steuersatz
	 *            Steuersatz
	 * @param ijUEZ
	 *            Schalter, Jahreszielerhöhung
	 * @param vgJUEZielerhoehung
	 *            Jahresüberschusszielerhöhung, Vorjahr
	 * 
	 * @return der Wert
	 */
	public static double OLD_v2_1_mil_jUERfBZuf(final int t, final double[] rohueberschuss, final double mindZfGes,
			final double mindZf, final double jueZiel, final double ueeBestand, final double freieRfbVj,
			final double[] rfbZufVj, final VUHistorie vuHistorie, final double pRohueb, final double i56bfRfB,
			final double i56bSueaf, final double sueafVj, final int strategie, final int ergebnis, final double ek,
			final double bwGrk, final double nvz, final double drVorDeklUeb, final double drLockInUebV,
			final double[] rfb56bVj, final double ziRaZu, final double drNueb, final double deltaNvz,
			final double vgEigenkapitalFortschreibung, final double steuersatz, final boolean ijUEZ,
			final double vgJUEZielerhoehung) {
		double jue = 0.0, rfbZuf = 0.0, rfB56b = 0.0;

		// 'kein Unterschied bei Strategie
		// 'Ermittlung der Hilfekennzahlen
		final double sumRfb10 = Rohueberschuss.mittlRfbZuf10J(t, vuHistorie, // RfB_Historie,
				rfbZufVj, rfb56bVj);
		final double sumRohueb10 = Rohueberschuss.mittlRohueb10J(t, vuHistorie, // Rohueb_Historie,
				rohueberschuss);

		final double mBeteiligungVN;
		if (sumRohueb10 > 1.0) {
			mBeteiligungVN = Math.max(Math.min(sumRfb10 / sumRohueb10, 0.9), 0.0);
		} else {
			mBeteiligungVN = 0.9;
		}

		final double rohUeb = Functions.nanZero(rohueberschuss[t]);
		// MIL_W.Schalesi
		final double keEk, jueZiel3, jueZiel4, rfbZufVariante1, rfbZufVariante2;

		// 'a
		if (rohUeb > mindZfGes) {
			switch (strategie) {
			// MIL_W.Schalesi: Maximum aus Case 1 und 2
			case 1: // 'Steuerung über die VN-Zielbeteiligung
				keEk = nvz / (1 - (steuersatz)) * vgEigenkapitalFortschreibung
						+ (ijUEZ ? (Double.isNaN(vgJUEZielerhoehung) ? 0.0 : vgJUEZielerhoehung) : 0.0);
				jueZiel3 = Math.max((1 - pRohueb) * (rohUeb - keEk), 0) + keEk;
				jueZiel4 = Math.max(jueZiel3, jueZiel);
				jue = Math.min(jueZiel4, rohUeb - mindZfGes);
				rfbZufVariante1 = rohUeb - jue;
				rfbZufVariante2 = Math.max(pRohueb * rohUeb, mindZfGes);
				rfbZuf = Math.min(rfbZufVariante1, rfbZufVariante2);
				rfB56b = 0.0;
				break;
			case 2: // 'Steuerung über eine Zielverzinsung des Eigenkapitals
				rfbZuf = Math.max(pRohueb * rohUeb, mindZfGes);
				jue = rohUeb - rfbZuf;
				rfB56b = 0.0;
				break;
			// MIL_W.Schalesi:(3) Dynamisch Zielbeteiligung
			case 3:
				keEk = (nvz + deltaNvz) / (1 - (steuersatz)) * vgEigenkapitalFortschreibung
						+ (ijUEZ ? (Double.isNaN(vgJUEZielerhoehung) ? 0.0 : vgJUEZielerhoehung) : 0.0);

				jueZiel3 = Math.max((1 - pRohueb) * (rohUeb - keEk), 0) + keEk;
				rfbZuf = Math.max(rohUeb - jueZiel3, mindZfGes);
				jue = rohUeb - rfbZuf;
				break;
			}
			// 'b
		} else if (Math.min(0.0, ueeBestand) <= rohUeb && rohUeb <= mindZfGes) {
			// 'b1
			if (mindZf <= rohUeb && rohUeb <= mindZfGes) {
				// 'kein Unterschied bei Strategie
				jue = 0.0;
				rfbZuf = rohUeb;
				rfB56b = 0.0;
				// 'b2
			} else {
				// 'kein Unterschied bei Strategie
				rfbZuf = Math.min(rohUeb - Math.min(ueeBestand, 0), mindZf);
				jue = rohUeb - rfbZuf;
				rfB56b = 0.0;
			}
			// 'c
		} else {
			rfbZuf = 0.0;
			rfB56b = Math.min(-1 * mBeteiligungVN * rohUeb, i56bfRfB * Math.max(Functions.nanZero(freieRfbVj), 0)
					+ i56bSueaf * Math.max(Functions.nanZero(sueafVj), 0));
			jue = rohUeb + rfB56b;
		}

		if (Functions.nanZero(drLockInUebV) == 0.0 && drVorDeklUeb == 0.0) {
			rfbZuf = 0.0;
			jue = jue + rfbZuf;
		}

		switch (ergebnis) {
		case 1:
			return jue;
		case 2:
			return rfbZuf;
		case 3:
			return rfB56b;
		default:
			// 'dieser Fall darf eigentlich nicht eintreten
			return 0.0;
		}
	}
	
	
	
	/**
	 * Berechnet den realisierten Jahresüberschuss und die Rückstellungen für Beitragsrückerstattungszuführung und gibt
	 * das gewünschte Ergebnis zurück. <br/>
	 * Name in Excel: JUE_RfBZuf.
	 * 
	 * @param ergebnis
	 *            Marker, der angibt, welches Ergebnis zurückgegeben werden soll (1 - realisierter Jahresüberschuss, 2 -
	 *            RfB Zuf, 3 - RfB 56b-Entnahme)
	 * @param t
	 *            Aktueller Zeitpunkt
	 * @param rohueberschuss
	 *            Rohüberschuss
	 * @param mindZfGes
	 *            Mindestzuführung gesamt (inkl. Kürzungskonto)
	 * @param mindZf
	 *            Mindestzuführung
	 * @param jueZiel
	 *            Ziel-Jahresüberschuss
	 * @param ueeBestand
	 *            Kostenüberschuss Bestand
	 * @param freieRfbVj
	 *            freie RfB zum Zeitpunkt t - 1
	 * @param rfbZufVj
	 *            Zuführung zur Rückstellung für Beitragsrückerstattung aus den Vorjahren
	 * @param vuHistorie
	 *            historische Werte für die RfB Zuführung und den Rohüberschuss (in Excel zwei Parameter!)
	 * @param pRohueb
	 *            Managementregel (Prozentwert der angibt, wie hoch die Beteiligung der VN am Rohüberschuss sein soll)
	 * @param i56bfRfB
	 *            Managementregel (Soll im Fall einer negativen Rohüberschussbeteiligung eine Entnahme aus der freien
	 *            RfB vorgenommen werden
	 * @param i56bSueaf
	 *            Managementregel (Soll im Fall einer negativen Rohüberschussbeteiligung eine Entnahme aus dem // SÜAF
	 *            vorgenommen werden
	 * @param sueafVj
	 *            SÜAF aus dem Vorjahr
	 * @param strategie
	 *            Strategie zur Ermittlung der Beteiligung der Versicherungsnehmer und Jahresüberschuss (1 =
	 *            Zielverzinsung, 2 = Zielbeteiligung)
	 * @param rfb56bVj
	 *            Höhe der Entnahme aus der nicht festgelegten RfB gemäß §56b VAG zum Zeitpunkt t-1
	 * @param drVorDeklUeb
	 *            Deckungsrückstellung vor Deklaration zum Ende des Jahres t
	 * @param drLockInUebV
	 *            Deckungsrückstellung für garantierte Leistungen zum Ende des Jahres t-1
	 * @param ke
	 *            Kapitalertrag
	 * @param aKe
	 *            Anrechenbare Kapitalerträge
	 * @return der Wert
	 * @param deltaNvz
	 *            Differenz zwischen NVZ vom Pfad von 0 im BE und
	 *            Verzinsungsanforderung des handelsrechtlichen Eigenkapital
	 *            r_EK zeitabh.ManReg Spalte K
	 * @param vgEigenkapitalFortschreibung
	 *            Eigenkapital vom Vorjahr
	 * @param steuersatz
	 *            Steuersatz
	 * @param ijUEZ
	 *            Schalter, Jahreszielerhöhung
	 * @param vgJUEZielerhoehung
	 *            Jahresüberschusszielerhöhung, Vorjahr
	 * 
	 * @return der Wert
	 * @param nvz
	 *            Nettoverzinsung des aktuellen Jahres
	 */
	public static double mil_jUERfBZuf(final int ergebnis, final int t, final double[] rohueberschuss,
			final double mindZfGes, final double mindZf, final double jueZiel, final double ueeBestand,
			final double freieRfbVj, final double[] rfbZufVj, final VUHistorie vuHistorie, final double pRohueb,
			final double i56bfRfB, final double i56bSueaf, final double sueafVj, final int strategie,
			final double[] rfb56bVj, final double drVorDeklUeb, final double drLockInUebV, final double ke,
			final double aKe,
			final double deltaNvz,
			final double vgEigenkapitalFortschreibung, final double steuersatz, final boolean ijUEZ,
			final double vgJUEZielerhoehung, final double nvz) {
		double JUe = 0.0, rfbZuf = 0.0, RfB56b = 0.0;

		double KE_EK;
		double JUE_Ziel_3;

		// kein Unterschied bei Strategie
		// Ermittlung der Hilfekennzahlen
		final double sumRfb10 = Rohueberschuss.mittlRfbZuf10J(t, vuHistorie, // RfB_Historie,
				rfbZufVj, rfb56bVj);
		final double sumRohueb10 = Rohueberschuss.mittlRohueb10J(t, vuHistorie, // Rohueb_Historie,
				rohueberschuss);

		final double mBeteiligungVN;
		if (sumRohueb10 > 1.0) {
			mBeteiligungVN = Math.max(Math.min(sumRfb10 / sumRohueb10, 0.9), 0.0);
		} else {
			mBeteiligungVN = 0.9;
		}

		final double rohUeb = nanZero(rohueberschuss[t]);
		// MIL_W.Schalesi
		final double jueZiel3, jueZiel4, rfbZufVariante1, rfbZufVariante2;

		// a
		if (rohUeb > mindZfGes) {
			switch (strategie) {
			// MIL_W.Schalesi: Maximum aus Case 1 und 2
			case 1: // Steuerung über die VN-Zielbeteiligung
				KE_EK = nvz / (1 - (steuersatz)) * vgEigenkapitalFortschreibung
						+ (ijUEZ ? (Double.isNaN(vgJUEZielerhoehung) ? 0.0 : vgJUEZielerhoehung) : 0.0);
				jueZiel3 = Math.max((1 - pRohueb) * (rohUeb - KE_EK), 0) + KE_EK;
				jueZiel4 = Math.max(jueZiel3, jueZiel);
				JUe = Math.min(jueZiel4, rohUeb - mindZfGes);
				rfbZufVariante1 = rohUeb - JUe;
				rfbZufVariante2 = Math.max(pRohueb * rohUeb, mindZfGes);
				rfbZuf = Math.min(rfbZufVariante1, rfbZufVariante2);
				RfB56b = 0.0;
				break;
			case 2: // Steuerung über eine Zielverzinsung des Eigenkapitals
				rfbZuf = Math.max(pRohueb * rohUeb, mindZfGes);
				JUe = rohUeb - rfbZuf;
				RfB56b = 0.0;
				break;
			// MIL_W.Schalesi:(3) Dynamisch Zielbeteiligung
			case 3: // KE_EK = NVZ * (EK + BW_GRK + DR_NUEB) + ZiRaZu
				KE_EK = (nvz + deltaNvz) / (1 - (steuersatz)) * vgEigenkapitalFortschreibung
						+ (ijUEZ ? (Double.isNaN(vgJUEZielerhoehung) ? 0.0 : vgJUEZielerhoehung) : 0.0);
				//KE_EK = ke - aKe;
				JUE_Ziel_3 = Math.max((1.0 - pRohueb) * (rohUeb - KE_EK), 0) + KE_EK;
				rfbZuf = Math.max(rohUeb - JUE_Ziel_3, mindZfGes);
				JUe = rohUeb - rfbZuf;
				break;
			}
			// b
		} else if (Math.min(0, ueeBestand) <= rohUeb && rohUeb <= mindZfGes) {
			// b1
			if (mindZf <= rohUeb && rohUeb <= mindZfGes) {
				// kein Unterschied bei Strategie
				JUe = 0.0;
				rfbZuf = rohUeb;
				RfB56b = 0.0;
				// b2
			} else {
				// kein Unterschied bei Strategie
				rfbZuf = Math.min(rohUeb - Math.min(ueeBestand, 0), mindZf);
				JUe = rohUeb - rfbZuf;
				RfB56b = 0.0;
			}
			// c
		} else {
			rfbZuf = 0.0;
			RfB56b = Math.min(-1.0 * mBeteiligungVN * rohUeb,
					i56bfRfB * Math.max(freieRfbVj, 0.0) + i56bSueaf * Math.max(sueafVj, 0.0));
			JUe = rohUeb + RfB56b;
		}

		if (drLockInUebV == 0.0 && drVorDeklUeb == 0.0) {
			JUe = JUe + rfbZuf;
			rfbZuf = 0.0;
		}

		switch (ergebnis) {
		case 1:
			return JUe;
		case 2:
			return rfbZuf;
		case 3:
			return RfB56b;
		default:
			// dieser Fall darf eigentlich nicht eintreten
			return Double.NaN;
		}
	}

	
	
	
	
	
	
	
	

	/**
	 * 
	 * Berechnet das Kapitalertragsdefizit aus den Vorjahren zum Verrechnen.
	 * <br/>
	 * Funktionsname in Excel: MIL_KED_VJ_Verrechnen.
	 * 
	 * @param t
	 *            aktuelles Jahr
	 * @param v
	 *            Verrechnungszeitraum
	 * @param kedVerrechnung
	 *            Spalte, in der die Kapitalertragsdefizite zum Verrechnen
	 *            stehen
	 * @param omega
	 *            Länge der Projektionszeitraumes
	 * @return Kapitalertragsdefizit aus den Vorjahren zum Verrechnen
	 */
	public static double mil_kedVjVerrechnen(final int t, final double v, final double[] kedVerrechnung,
			final int omega) {
		double mil_kedVjVerrechnen = 0.0;
		if (t == 0) {
			mil_kedVjVerrechnen = 0.0;
		} else if (t > 0 && t <= (int) v) {
			for (int i = 0; i < kedVerrechnung.length - 1; i++) {
				mil_kedVjVerrechnen += Functions.nanZero(kedVerrechnung[i]);
			}
			mil_kedVjVerrechnen /= v;
		} else if (t > (int) v && t < omega) {
			for (int i = (t - (int) v); i < kedVerrechnung.length - 1; i++) {
				mil_kedVjVerrechnen += Functions.nanZero(kedVerrechnung[i]);
			}
			mil_kedVjVerrechnen /= v;
		} else if (t == omega) {
			for (int i = 1; i <= (int) v; ++i) {
				mil_kedVjVerrechnen = mil_kedVjVerrechnen
						+ i * Functions.nanZero(kedVerrechnung[omega - (int) v + i - 1]) / v;
			}
		}
		return mil_kedVjVerrechnen;
	}

	/**
	 * Funktionsname in Excel: MIL_BW_FI_Verrechnung. <br/>
	 * Berechnet den Buchungswert des Fixed Income nach Verrechnung, dazu werden
	 * die Summen der Kapitalertragsdefizite aus dem Geschäftsjahr und dem
	 * Vorjahr verrechnet. Der Verrechnungszeitraum kann mit dem Parameter V
	 * vorgegeben werden. Der Parameter T wird nur benötigt, damit auch die
	 * Formel für
	 * 
	 * @param t
	 *            aktuelles Jahr
	 * @param bwFi
	 *            Buchwert des Fixed Incomes zum Zeitpunkt T
	 * @param verrechnungszeitraum
	 *            Verrechnungszeitraum
	 * @param kedVerrechnung
	 *            Spalte, in der die Kapitalertragsdefizite zum Verrechnen
	 *            stehen
	 * @param kedVjVerrechnen
	 *            Spalte, in der die Kapitalertragsdefizite aus den Vorjahren
	 *            zum Verrechnen stehen.
	 * @return Buchungswert des Fixed Income nach Verrechnung
	 */
	public static double mil_bwFiVerrechnung(final int t, final double bwFi, final double verrechnungszeitraum,
			final double[] kedVerrechnung, final double[] kedVjVerrechnen) {
		double bwFiVerrechnung = 0.0;
		if (t == 0) {
			return bwFi;
		} else {
			for (int i = 0; i < kedVjVerrechnen.length - 1; i++) {
				bwFiVerrechnung += Functions.nanZero(kedVerrechnung[i]) - Functions.nanZero(kedVjVerrechnen[i]);
			}
			return bwFiVerrechnung + bwFi;

		}
	}
}