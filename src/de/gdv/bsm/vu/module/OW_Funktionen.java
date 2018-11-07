package de.gdv.bsm.vu.module;


public class OW_Funktionen {

	/**
	 * OW_D.Hohmann Funktionen des Excel-Moduls <code>OW_Funktionen</code>.
	 * 
	 * @param aKE
	 *            Anrechenbare Kapitalerträge
	 * @param rmZAlt
	 *            rmZ UEB, alt + Delta ZZR UEB, alt
	 * @param rmZAlt_sl
	 *            rmZ UEB, alt_sl + Delta ZZR UEB, alt_sl
	 * @param reAlt
	 *            Risikoergebnis UEB, alt
	 * @param reAlt_sl
	 *            Risikoergebnis UEB, alt_sl
	 * @param ueeAlt
	 *            Übriges Ergebnis Bestand UEB , alt
	 * @param ueeAlt_sl
	 *            Übriges Ergebnis Bestand UEB , alt_sl 
	 * @param drAltV
	 *            Deckungsrückstellung, Lockin, ÜEB, alt
	 * @param drAltV_sl
	 *            Deckungsrückstellung, Lockin, ÜEB, alt_sl
	 * @param zzrAltV
	 *            ZZR UEB, alt
	 * @param zzrAltV_sl
	 *            ZZR UEB, alt_sl
	 * @param sueafAltV
	 *            SÜAF, UEB, alt
	 * @param sueafAltV_sl
	 *            SÜAF, UEB, alt_sl
	 * @param rmZNeu
	 *            rmZ UEB, alt + Delta ZZR UEB, neu
	 * @param rmZNeu_sl
	 *            rmZ UEB, alt_sl + Delta ZZR UEB, neu_sl
	 * @param reNeu
	 *            Risikoergebnis UEB, neu
	 * @param reNeu_sl
	 *            Risikoergebnis UEB, neu_sl
	 * @param ueeNeu
	 *            Übriges Ergebnis Bestand UEB , neu
	 * @param ueeNeu_sl
	 *            Übriges Ergebnis Bestand UEB , neu_sl
	 * @param drNeuV
	 *            Deckungsrückstellung, Lockin, ÜEB, neu
	 * @param drNeuV_sl
	 *            Deckungsrückstellung, Lockin, ÜEB, neu_sl
	 * @param zzrNeuV
	 *            ZZR UEB, neu
	 * @param zzrNeuV_sl
	 *            ZZR UEB, neu_sl
	 * @param sueafNeuV
	 *            SÜAF, UEB, neu
	 * @param sueafNeuV_sl
	 *            SÜAF, UEB, neu_sl
	 * @return der Wert
	 */
	public static double mindZf(final double aKE, final double rmZAlt, final double reAlt, final double ueeAlt,
			final double drAltV, final double zzrAltV, final double sueafAltV, final double rmZAlt_sl, final double reAlt_sl, final double ueeAlt_sl,
			final double drAltV_sl, final double zzrAltV_sl, final double sueafAltV_sl, final double rmZNeu, final double reNeu,
			final double ueeNeu, final double drNeuV, final double zzrNeuV, final double sueafNeuV, final double rmZNeu_sl, final double reNeu_sl,
			final double ueeNeu_sl, final double drNeuV_sl, final double zzrNeuV_sl, final double sueafNeuV_sl) {
		double aKETb = 0.0;
		double zinstraegerGes = drAltV + zzrAltV + sueafAltV + drAltV_sl + zzrAltV_sl + sueafAltV_sl + drNeuV + zzrNeuV + sueafNeuV + drNeuV_sl + zzrNeuV_sl + sueafNeuV_sl;
		if (zinstraegerGes > 0.001) {
			aKETb = aKE * (drAltV + zzrAltV + sueafAltV) / zinstraegerGes;
		}
		double mindZf_KE = Math.max(0.9 * aKETb - rmZAlt, 0) + Math.min(aKETb - rmZAlt, 0.0);
		double MindZf = Math.max(mindZf_KE + 0.9 * Math.max(reAlt, 0) + 0.5 * Math.max(ueeAlt, 0.0), 0.0);
		aKETb = 0.0;
		if (zinstraegerGes > 0.001) {
			aKETb = aKE * (drNeuV + zzrNeuV + sueafNeuV) / zinstraegerGes;
		}
		mindZf_KE = Math.max(0.9 * aKETb - rmZNeu, 0) + Math.min(aKETb - rmZNeu, 0.0);
		MindZf = MindZf + Math.max(mindZf_KE + 0.9 * Math.max(reNeu, 0.0) + 0.5 * Math.max(ueeNeu, 0.0), 0.0);
		if (zinstraegerGes > 0.001) {
			aKETb = aKE * (drAltV_sl + zzrAltV_sl + sueafAltV_sl) / zinstraegerGes;
		}
		mindZf_KE = Math.max(0.9 * aKETb - rmZAlt_sl, 0) + Math.min(aKETb - rmZAlt_sl, 0.0);
		MindZf = MindZf + Math.max(mindZf_KE + 0.9 * Math.max(reAlt_sl, 0) + 0.5 * Math.max(ueeAlt_sl, 0.0), 0.0);
		aKETb = 0.0;
		if (zinstraegerGes > 0.001) {
			aKETb = aKE * (drNeuV_sl + zzrNeuV_sl + sueafNeuV_sl) / zinstraegerGes;
		}
		mindZf_KE = Math.max(0.9 * aKETb - rmZNeu_sl, 0) + Math.min(aKETb - rmZNeu_sl, 0.0);
		MindZf = MindZf + Math.max(mindZf_KE + 0.9 * Math.max(reNeu_sl, 0.0) + 0.5 * Math.max(ueeNeu_sl, 0.0), 0.0);

	return MindZf;

	}		

	/**
	 * Berechnet ZZR gesamt. <br/>
	 * Funktionsname in Excel: ZZR_gesamt.
	 * 
	 * @param zzrUebAlt
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Altbestandes
	 * @param zzrUebNeu
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Neubestandes
	 * @param zzrUebAlt_sl
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Altbestandes_sl
	 * @param zzrUebNeu_sl
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Neubestandes_sl
	 * @param zzrNueb
	 *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen eine Geschäftzweiges
	 * @return der Wert
	 */
	public static double zzrGesamt(final double zzrUebAlt, final double zzrUebNeu, final double zzrUebAlt_sl, final double zzrUebNeu_sl, final double zzrNueb) {
		return zzrUebAlt + zzrUebNeu + zzrUebAlt_sl + zzrUebNeu_sl + zzrNueb;
	}


	/**
	 * Berechnet den SÜAF, gesamt. <br/>
	 * Funktionsname in Excel: SUEAF.
	 * 
	 * @param sueAfAlt
	 *            Summierter Schlussüberschussfondsanteil der einzelnen Bestandsgruppen des Altbestandes
	 * @param sueAfNeu
	 *            Summierter Schlussüberschussfondsanteil der einzelnen Bestandsgruppen des Neubestandes
	 * @param sueAfAlt_sl
	 *            Summierter Schlussüberschussfondsanteil der einzelnen Bestandsgruppen des Altbestandes_sl
	 * @param sueAfNeu_sl
	 *            Summierter Schlussüberschussfondsanteil der einzelnen Bestandsgruppen des Neubestandes_sl 
	 * @return der Wert
	 */
	public static double sueAf(final double sueAfAlt, final double sueAfNeu, final double sueAfAlt_sl, final double sueAfNeu_sl ) {
		return sueAfAlt + sueAfNeu + sueAfAlt_sl + sueAfNeu_sl;
	}

	/**
	 * Berechnet den Mindestkapitalertrag unter Berücksichtigung des LVRG und der Trennung in Alt-/Neubestand (inkl. _sl). <br/>
	 * Funktionsname in Excel: Mindestkapitalertrag_LVRG_alt_neu.
	 * 
	 * @param iLVRG
	 *            Schalter Verrechnungsmöglichkeit nach LVRG (1 = komplett verwenden, 0 = nicht verwenden)
	 * @param kePlan
	 *            Kapitalertrag vor der Realisierung von Realwerten
	 * @param rmzGesamtUebAlt
	 *            Rechnungsmäßige Zinsen des überschussberechtigten Geschäfts, Altbestand
	 * @param rmzGesamtUebNeu
	 *            Rechnungsmäßige Zinsen des überschussberechtigten Geschäfts, Neubestand
	 * @param rmzGesamtUebAlt_sl
	 *            Rechnungsmäßige Zinsen des überschussberechtigten Geschäfts, Altbestand_sl
	 * @param rmzGesamtUebNeu_sl
	 *            Rechnungsmäßige Zinsen des überschussberechtigten Geschäfts, Neubestand_sl
	 * @param rmzGesamtNueb
	 *            Rechnungsmäßige Zinsen des nicht überschussberechtigten Geschäfts
	 * @param reUebAlt
	 *            Risikoüberschuss des überschussberechtigten Geschäfts, Altbestand
	 * @param reUebNeu
	 *            Risikoüberschuss des überschussberechtigten Geschäfts, Neubestand_sl
	 * @param reUebAlt_sl
	 *            Risikoüberschuss des überschussberechtigten Geschäfts, Altbestand_sl
	 * @param reUebNeu_sl
	 *            Risikoüberschuss des überschussberechtigten Geschäfts, Neubestand
	 * @param reNueb
	 *            Risikoüberschuss des nicht überschussberechtigten Geschäfts
	 * @param ueeBestandUebAlt
	 *            Kostenüberschuss (Bestand) des überschussberechtigten Geschäfts, Altbestand
	 * @param ueeBestandUebNeu
	 *            Kostenüberschuss (Bestand) des überschussberechtigten Geschäfts, Neubestand
	 * @param ueeBestandUebAlt_sl
	 *            Kostenüberschuss (Bestand) des überschussberechtigten Geschäfts, Altbestand_sl
	 * @param ueeBestandUebNeu_sl
	 *            Kostenüberschuss (Bestand) des überschussberechtigten Geschäfts, Neubestand_sl
	 * @param ueeNueb
	 *            Kostenüberschuss des nicht überschussberechtigten Geschäfts
	 * @param jueZiel
	 *            Zieljahresüberschuss
	 * @param ziRaZu
	 *            Zinsratenzuschlag
	 * @param zinsenGrk
	 *            Zinsen für Nachrangsdarlehen und Genussrechtkapital
	 * @return der Wert
	 */
	public static double mindestkapitalertragLvrgAltNeu(final double iLVRG, final double kePlan, final double rmzGesamtUebAlt, final double rmzGesamtUebNeu, final double rmzGesamtUebAlt_sl, final double 
 			rmzGesamtUebNeu_sl, final double rmzGesamtNueb, final double reUebAlt, final double reUebNeu, final double reUebAlt_sl, final double reUebNeu_sl,						
 			final double reNueb, final double ueeBestandUebAlt, final double ueeBestandUebNeu, final double ueeBestandUebAlt_sl, final double ueeBestandUebNeu_sl, final double ueeNueb, final double jueZiel, final double ziRaZu,
			final double zinsenGrk) {
		final double Mindestkapitalertrag_LVRG_alt_neu;

		if (kePlan < (rmzGesamtUebAlt + rmzGesamtUebAlt_sl + rmzGesamtUebNeu + rmzGesamtUebNeu_sl + rmzGesamtNueb) && iLVRG > 0.0) {

			final double JUE_re_uee = reUebNeu - 0.9 * Math.max(reUebNeu, 0.0) + ueeBestandUebNeu
					- 0.5 * Math.max(ueeBestandUebNeu, 0.0) + reUebAlt - 0.9 * Math.max(reUebAlt, 0) + ueeBestandUebAlt
					- 0.5 * Math.max(ueeBestandUebAlt, 0)+ reUebNeu_sl - 0.9 * Math.max(reUebNeu_sl, 0.0) + ueeBestandUebNeu_sl
					- 0.5 * Math.max(ueeBestandUebNeu_sl, 0.0) + reUebAlt_sl - 0.9 * Math.max(reUebAlt_sl, 0) + ueeBestandUebAlt_sl
					- 0.5 * Math.max(ueeBestandUebAlt_sl, 0) + reNueb + ueeNueb;

			Mindestkapitalertrag_LVRG_alt_neu = rmzGesamtUebAlt + rmzGesamtUebNeu + rmzGesamtUebAlt_sl + rmzGesamtUebNeu_sl
					- iLVRG * (0.9 * Math.max(reUebAlt, 0.0) + 0.5 * Math.max(ueeBestandUebAlt, 0.0))
					- iLVRG * (0.9 * Math.max(reUebNeu, 0.0) + 0.5 * Math.max(ueeBestandUebNeu, 0.0))
					- iLVRG * (0.9 * Math.max(reUebAlt_sl, 0.0) + 0.5 * Math.max(ueeBestandUebAlt_sl, 0.0))
					- iLVRG * (0.9 * Math.max(reUebNeu_sl, 0.0) + 0.5 * Math.max(ueeBestandUebNeu_sl, 0.0)) +  rmzGesamtNueb
					- Math.max(JUE_re_uee - jueZiel, 0.0);
		} else {
			final double erfuelleMindZV = rmzGesamtUebAlt + rmzGesamtUebNeu + rmzGesamtUebAlt_sl + rmzGesamtUebNeu_sl + 0.9 * Math.max(reUebAlt, 0.0) - reUebAlt + 0.9 * Math.max(reUebAlt_sl, 0.0) - reUebAlt_sl
					+ 0.9 * Math.max(reUebNeu, 0.0) - reUebNeu + 0.9 * Math.max(reUebNeu_sl, 0.0) - reUebNeu_sl + 0.5 * Math.max(ueeBestandUebAlt, 0.0)
					- ueeBestandUebAlt + 0.5 * Math.max(ueeBestandUebAlt_sl, 0.0)
					- ueeBestandUebAlt_sl + 0.5 * Math.max(ueeBestandUebNeu, 0.0) - ueeBestandUebNeu + 0.5 * Math.max(ueeBestandUebNeu_sl, 0.0) - ueeBestandUebNeu_sl + rmzGesamtNueb
					- reNueb - ueeNueb;

			final double rmZErh = (rmzGesamtUebAlt + rmzGesamtUebNeu + rmzGesamtUebAlt_sl + rmzGesamtUebNeu_sl) / 0.9 + rmzGesamtNueb;

			Mindestkapitalertrag_LVRG_alt_neu = Math.min(rmZErh, erfuelleMindZV + jueZiel);
		}

		return Mindestkapitalertrag_LVRG_alt_neu - ziRaZu + zinsenGrk;
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
	 * @param ubrErgAlt_sl
	 *            übrig. Ergebnis des Altbestandes_sl
	 * @param ubrErgNeu_sl
	 *            übrig. Ergebnis des Neubestandes_sl
	 * @param ubrErgBestandAlt_sl
	 *            Bestand (ohne GCR) des Altbestands_sl
	 * @param ubrErgBestandNeu_sl
	 *            Bestand (ohne GCR) des Neubestands_sl
	 * @return der Wert
	 */
	public static double cfUebrEng(final double ubrErgAlt, final double ubrErgNeu, final double ubrErgBestandAlt,final double ubrErgBestandNeu, final double ubrErgAlt_sl, final double ubrErgNeu_sl, final double ubrErgBestandAlt_sl,final double ubrErgBestandNeu_sl) {
		return ubrErgAlt + ubrErgAlt_sl + ubrErgNeu + ubrErgNeu_sl - (ubrErgBestandAlt + ubrErgBestandAlt_sl + ubrErgBestandNeu + ubrErgBestandNeu_sl);
	}
	
	
}
