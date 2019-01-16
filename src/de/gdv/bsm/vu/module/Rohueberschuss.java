package de.gdv.bsm.vu.module;

import static de.gdv.bsm.vu.module.Functions.inProzent;
import static de.gdv.bsm.vu.module.Functions.nanZero;
import de.gdv.bsm.intern.params.VUHistorie;

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
public class Rohueberschuss {

    /**
     * YG 03.04.2014 Berechnet die Zinszusatzreserve für die Rechnungszinsgeneration rz zum Zeitpunkt t
     * 
     * @param altNeu
     *            alt_neu - Kennzeichen Alt- / Neubestand
     * @param rz
     *            Rechnungszinsgeneration in Basispunkten
     * @param ref
     *            Referenzzinssatz
     * @param ref2M
     *            Referenzzinssatz, 2M
     * @param aufwand
     *            Aufwand für die Zinszusatzreserve in Basispunkten bezogen auf die HGB-Deckungsrückstellung
     * @param dr
     *            HGB-Deckungsrückstellung
     * @param zzrMethode
     *            ZZR Methode für Altbestand bzw. Korridormethode für den Gesamtbestand
     * @param korrekturRechnungsgrundl
     *            Korrekturterm für den Referenzzins aufgrund reduzierter Margen in Rechnungsgrundlagen Biometrie und
     *            Kosten
     * @param refStartR
     *            Startwert Referenzzins
     * @return der Wert
     */
    public static double zzrJ(final String altNeu, final double rz, final double ref, final double ref2M,
            final double aufwand, final double dr, final String zzrMethode, final double korrekturRechnungsgrundl,
            final double refStartR) {
        double ref_zins;
        if (altNeu.equals("a")) {
            switch (zzrMethode) {
                case "Aktuell":
                    ref_zins = ref;
                    break;
                case "2M":
                    ref_zins = ref2M;
                    break;
                case "Korridor":
                    ref_zins = ref2M;
                    break;
                default:
                    ref_zins = ref;
            }
        } else {
            switch (zzrMethode) {
                case "Aktuell":
                    ref_zins = ref;
                    break;
                case "2M":
                    ref_zins = ref;
                    break;
                case "Korridor":
                    ref_zins = ref2M;
                    break;
                default:
                    ref_zins = ref;
            }
        }

        return Math.max(
                Math.max(rz - 10000.0 * Math.min(Math.max(refStartR, ref_zins), ref_zins + korrekturRechnungsgrundl),
                        0.0) * aufwand * dr,
                0.0);
    }

    /**
     * Addiert alle Leistungen auf. <br/>
     * Funktionsname in Excel: L_garantiert.
     * 
     * @param lGarantiertOSonstErl
     *            Garantierte Leistungen, ohne sonstige Erlebnsfallleistungen
     * @param lGarantiertSonstErl
     *            sonstige Erlebensfallleistungen
     * @param vgLaKapWahlXsAggr
     *            relative Leistungsanpassung der sonstigen Erlebensfallleistungen durch dynamische Kapitalwahl
     * @return Leistungen
     */
    public static double lGarantiert(final double lGarantiertOSonstErl, final double lGarantiertSonstErl,
            final double vgLaKapWahlXsAggr) {
        return lGarantiertOSonstErl + (1.0 + nanZero(vgLaKapWahlXsAggr)) * lGarantiertSonstErl;
    }

    /**
     * Berechnet die determinischen garantierten Leistungen. <br/>
     * Funktionsname in Excel: L_garantiert_det.
     * 
     * @param lGarantiertOSonstErl
     *            Garantierte Leistungen, ohne sonstige Erlebnsfallleistungen
     * @param sonstigeErlebensfallLeistungen
     *            sonstige Erlebensfallleistungen
     * @return Leistungen
     */
    public static double lGarantiertDet(final double lGarantiertOSonstErl,
            final double sonstigeErlebensfallLeistungen) {
        return lGarantiertOSonstErl + sonstigeErlebensfallLeistungen;
    }

    /**
     * Berechnet die Aufwendungen für den tariflichen Rechnungszins. <br/>
     * Funktionsname in Excel: rmZ_Tarif.
     * 
     * @param rmzTarif
     *            Rechnnungsmäßiger Zinsaufwand aus der deterministischen Projektion
     * @param leAggrRz
     *            Garantierte Leistungserhöhung aus dem Jahr t-1 für die RZG rz
     * @param lbwT
     *            Leistungsbarwert aus der deterministischer Projektion, Jahr t
     * @param lbwT1
     *            Leistungsbarwert aus der deterministischer Projektion, Jahr t-1
     * @param lt
     *            Gesamtleistung, determinisch, Jahr t
     * @param t
     *            Aktueller Zeitpunkt
     * @param rz
     *            Rechnungszins
     * @param sueAfV
     *            SÜAF Entnahme
     * @param barAus
     *            Barauszahlung
     * @param rkwXs
     *            Rückkaufswerte Excess Betrag
     * @param lErlT
     *            Sonstige Erlebens-fallleistungen
     * @param kaXs
     *            Kapitalabfindung Excess Betrag
     * @param vgLaKapWahlXsAggr
     *            Leistungsanpassung Kapitalwahl
     * @param vgLbwSonstErl
     *            Leistungsbarwert SonstErll, deterministisch
     * @param vuZeitpunkt
     *            Monat der Auszahlung
     * @param vgLambda
     *            Gesamt Storno
     * @param beitraegeStoch
     *            Beiträge, stochastisch
     * @param beitraegeDet
     *            Prämien
     * @param kategorie
     *            Kategorie
     * @return Aufwendungen
     */
    public static double rmZTarif(final double rmzTarif, final double leAggrRz, final double lbwT, final double lbwT1,
            final double lt, final int t, final double rz, final double sueAfV, final double barAus, final double rkwXs,
            final double lErlT, final double kaXs, final double vgLaKapWahlXsAggr, final double vgLbwSonstErl,
            final double vuZeitpunkt, final double vgLambda, final double beitraegeStoch, final double beitraegeDet,
            final String kategorie) {

        double rmZTarif;
        if (t > 0 && lbwT1 > 0.001) {
            final double rzProzent = inProzent(rz);
            final double alpha = Math.pow(1.0 + rzProzent, 1.0 - vuZeitpunkt / 12.0) - 1.0;
            final double beta = Math.pow(1.0 + rzProzent, vuZeitpunkt / 12.0) - 1.0;

            rmZTarif = vgLambda * rmzTarif + nanZero(leAggrRz) * (lbwT1 * rzProzent - vgLambda * alpha * lt)
                    + rzProzent * vgLambda * nanZero(vgLaKapWahlXsAggr) * vgLbwSonstErl
                    - alpha * (rkwXs + (1 + nanZero(leAggrRz)) * kaXs + vgLambda * nanZero(vgLaKapWahlXsAggr) * lErlT)
                    + (nanZero(sueAfV) + nanZero(barAus)) * beta;

            if (kategorie.equals("FLV")) {
                rmZTarif = rmZTarif + (beitraegeStoch - vgLambda * beitraegeDet) * alpha;
            }
        } else {
            rmZTarif = 0.0;
        }
        return rmZTarif;
    }

    /**
     * Berechnet ZZR gesamt. <br/>
     * Funktionsname in Excel: ZZR_gesamt.
     * 
     * @param zzrUebAlt
     *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Altbestandes
     * @param zzrUebNeu
     *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen des Neubestandes
     * @param zzrNueb
     *            summierte Zinszusatzreserve der einzelnen Bestandsgruppen eine Geschäftzweiges
     * @return der Wert
     */
    public static double zzrGesamt(final double zzrUebAlt, final double zzrUebNeu, final double zzrNueb) {
        return zzrUebAlt + zzrUebNeu + zzrNueb;
    }

    /**
     * Berechnet die Veränderung der Zinszusatzreserve aus der Zinszusatzreserve aus dem aktuellen und aus der
     * Zinszusatzreserve des Vorjahres. <br/>
     * Funktionsname in Excel: deltaZZR.
     * 
     * @param zzr
     *            Zinszusatzreserve aus dem aktuellen Jahr
     * @param zzrVorjahr
     *            Zinszusatzreserve des Vorjahres
     * @return Veränderung der Zinszusatzreserve
     */
    public static double deltaZzr(final double zzr, final double zzrVorjahr) {
        return zzr - zzrVorjahr;
    }

    /**
     * Berechnet den SÜAF, gesamt. <br/>
     * Funktionsname in Excel: SUEAF.
     * 
     * @param sueAfAlt
     *            Summierter Schlussüberschussfondsanteil der einzelnen Bestandsgruppen des Altbestandes
     * @param sueAfNeu
     *            Summierter Schlussüberschussfondsanteil der einzelnen Bestandsgruppen des Neubestandes
     * @return der Wert
     */
    public static double sueAf(final double sueAfAlt, final double sueAfNeu) {
        return sueAfAlt + sueAfNeu;
    }

    /**
     * Berechnet die rechnungsmäßigen Zinsen rmZ_gesamt. <br/>
     * Funktionsname in Excel: rmZ_Gesamt.
     * 
     * @param rmzTarif
     *            tariflicher Rechnungszins
     * @param deltaZzr
     *            Veränderung der Zinszusatzreserve
     * @return rechnungsmäßige Zinsen
     */
    public static double rmZGesamt(final double rmzTarif, final double deltaZzr) {
        return rmzTarif + deltaZzr;
    }

    /**
     * Berechnet die anrechenbaren Kapitalerträge, indem die Nettoverzinsung auf die mittleren zinstrangenden Passiva
     * von Jahresanfang und Jahresende angewendet wird zzgl. nicht festgelegte RfB. <br/>
     * Funktionsname in Excel: Kapitalertrag_anrechenbar.
     * 
     * @param t
     *            Zeitpunkt
     * @param nvz
     *            Nettoverzinsung
     * @param drVorDeklUeb
     *            Deckungsrückstellung für das überschussberechtigte Geschäft des aktuellen Geschäftsjahres vor
     *            Deklaration
     * @param drLockInUebV
     *            Deckungsrückstellung für das überschussberechtigte Geschäft zum Zeitpunkt t - 1
     * @param zzrUeb
     *            Zinszusatzreserve des überschussberechtigen Geschäftes in t
     * @param zzrUebV
     *            Zinszusatzreserve des überschussberechtigen Geschäftes in t-1
     * @param nfRfBv
     *            Nicht festgelegt Rückstellungen für Beitragsrückerstattung zum Zeitpunkt t - 1
     * @return Kapitalerträge
     */
    public static double kapitalertragAnrechenbar(final int t, final double nvz, final double drVorDeklUeb,
            final double drLockInUebV, final double zzrUeb, final double zzrUebV, final double nfRfBv) {
        if (t == 0) {
            return 0.0;
        } else {
            return nvz * (0.5 * (drVorDeklUeb + nanZero(drLockInUebV) + zzrUeb + nanZero(zzrUebV)) + nanZero(nfRfBv));
        }
    }

    /**
     * Bestimmt das übrige Ergebnisse für das überschussberechtigte, klassische Geschäft unter Berücksichtigung der
     * Managementregeln. <br/>
     * Funktionsname in Excel: Kostenueberschuss_Bestand.
     * 
     * @param uebrErg
     *            Übrigens Ergebnis für das klassische, überschussberechtigte Geschäft
     * @param qNG
     *            Managementregel, die festlegt, welcher Teil des übrigen Ergebnisses zur Finanzierung des künftigen
     *            Geschäfts herangezogen wird
     * @return Ergebnisse
     */
    public static double kostenueberschussBestand(final double uebrErg, final double qNG) {
        return Math.min((1.0 - qNG) * uebrErg, uebrErg);
    }

    /**
     * Berechnet den Mindestkapitalertrag unter Berücksichtigung des LVRG und der Trennung in Alt-/Neubestand. <br/>
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
     * @param rmzGesamtNueb
     *            Rechnungsmäßige Zinsen des nicht überschussberechtigten Geschäfts
     * @param reUebAlt
     *            Risikoüberschuss des überschussberechtigten Geschäfts, Altbestand
     * @param reUebNeu
     *            Risikoüberschuss des überschussberechtigten Geschäfts, Neubestand
     * @param reNueb
     *            Risikoüberschuss des nicht überschussberechtigten Geschäfts
     * @param ueeBestandUebAlt
     *            Kostenüberschuss (Bestand) des überschussberechtigten Geschäfts, Altbestand
     * @param ueeBestandUebNeu
     *            Kostenüberschuss (Bestand) des überschussberechtigten Geschäfts, Neubestand
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
    public static double mindestkapitalertragLvrgAltNeu(final double iLVRG, final double kePlan,
            final double rmzGesamtUebAlt, final double rmzGesamtUebNeu, final double rmzGesamtNueb,
            final double reUebAlt, final double reUebNeu, final double reNueb, final double ueeBestandUebAlt,
            final double ueeBestandUebNeu, final double ueeNueb, final double jueZiel, final double ziRaZu,
            final double zinsenGrk) {
        final double Mindestkapitalertrag_LVRG_alt_neu;

        if (kePlan < (rmzGesamtUebAlt + rmzGesamtUebNeu + rmzGesamtNueb) && iLVRG > 0.0) {

            final double JUE_re_uee = reUebNeu - 0.9 * Math.max(reUebNeu, 0.0) + ueeBestandUebNeu
                    - 0.5 * Math.max(ueeBestandUebNeu, 0.0) + reUebAlt - 0.9 * Math.max(reUebAlt, 0) + ueeBestandUebAlt
                    - 0.5 * Math.max(ueeBestandUebAlt, 0) + reNueb + ueeNueb;

            Mindestkapitalertrag_LVRG_alt_neu = rmzGesamtUebAlt + rmzGesamtUebNeu
                    - iLVRG * (0.9 * Math.max(reUebAlt, 0.0) + 0.5 * Math.max(ueeBestandUebAlt, 0.0))
                    + -iLVRG * (0.9 * Math.max(reUebNeu, 0.0) + 0.5 * Math.max(ueeBestandUebNeu, 0.0)) + rmzGesamtNueb
                    - Math.max(JUE_re_uee - jueZiel, 0.0);
        } else {
            final double erfuelleMindZV = rmzGesamtUebAlt + rmzGesamtUebNeu + 0.9 * Math.max(reUebAlt, 0.0) - reUebAlt
                    + 0.9 * Math.max(reUebNeu, 0.0) - reUebNeu + 0.5 * Math.max(ueeBestandUebAlt, 0.0)
                    - ueeBestandUebAlt + 0.5 * Math.max(ueeBestandUebNeu, 0.0) - ueeBestandUebNeu + rmzGesamtNueb
                    - reNueb - ueeNueb;

            final double rmZErh = (rmzGesamtUebAlt + rmzGesamtUebNeu) / 0.9 + rmzGesamtNueb;

            Mindestkapitalertrag_LVRG_alt_neu = Math.min(rmZErh, erfuelleMindZV + jueZiel);
        }

        return Mindestkapitalertrag_LVRG_alt_neu - ziRaZu + zinsenGrk;
    }

    /**
     * Berechnet den Rohüberschuss. <br/>
     * Funktionsname in Excel: RohUeb.
     * 
     * @param ke
     *            Kapitalertrag
     * @param rmzGesamtUeb
     *            Rechnungsmäßiger Zinsaufwand für das überschussberechtigte Geschäft
     * @param rmzGesamtNueb
     *            Rechnungsmäßiger Zinsaufwand für das nicht-überschussberechtigte Geschäft
     * @param risikoUeberschussUeb
     *            Risikoüberschuss aus dem überschussberechtigten Geschäft
     * @param risikoUeberschussNueb
     *            Risikoüberschuss aus dem nicht-überschussberechtigten Geschäft
     * @param kostenUeberschussBestandUeb
     *            Kostenüberschuss aus dem überschussberechtigten Geschäft
     * @param kostenUeberschussNueb
     *            Kostenüberschuss aus dem nicht-überschussberechtigten Geschäft
     * @param kaAufwendungen
     *            Aufwendugen für Kapitalanlagen
     * @param zinsenGrk
     *            Zinsen für Nachrangsdarlehen und Genussrechtkapital
     * @return Rohüberschuss
     */
    public static double rohueb(final double ke, final double rmzGesamtUeb, final double rmzGesamtNueb,
            final double risikoUeberschussUeb, final double risikoUeberschussNueb,
            final double kostenUeberschussBestandUeb, final double kostenUeberschussNueb, final double kaAufwendungen,
            final double zinsenGrk) {
        return ke - kaAufwendungen - rmzGesamtUeb + risikoUeberschussUeb + kostenUeberschussBestandUeb - rmzGesamtNueb
                + risikoUeberschussNueb + kostenUeberschussNueb - zinsenGrk;
    }

    /**
     * Berechnet die Mindestbeteiligung entsprechend der Mindestzuführungsverordnung<br/>
     * Funktionsname in Excel: MindZf.
     * 
     * @param aKE
     *            Anrechenbare Kapitalerträge
     * @param rmZAlt
     *            rmZ UEB, alt + Delta ZZR UEB, alt
     * @param reAlt
     *            Risikoergebnis UEB, alt
     * @param ueeAlt
     *            Übriges Ergebnis Bestand UEB , alt
     * @param drAltV
     *            Deckungsrückstellung, Lockin, ÜEB, alt
     * @param zzrAltV
     *            ZZR UEB, alt
     * @param sueafAltV
     *            SÜAF, UEB, alt
     * @param rmZNeu
     *            rmZ UEB, alt + Delta ZZR UEB, neu
     * @param reNeu
     *            Risikoergebnis UEB, neu
     * @param ueeNeu
     *            Übriges Ergebnis Bestand UEB , neu
     * @param drNeuV
     *            Deckungsrückstellung, Lockin, ÜEB, neu
     * @param zzrNeuV
     *            ZZR UEB, neu
     * @param sueafNeuV
     *            SÜAF, UEB, neu
     * @return der Wert
     */
    public static double mindZf(final double aKE, final double rmZAlt, final double reAlt, final double ueeAlt,
            final double drAltV, final double zzrAltV, final double sueafAltV, final double rmZNeu, final double reNeu,
            final double ueeNeu, final double drNeuV, final double zzrNeuV, final double sueafNeuV) {
        double aKETb = 0.0;
        double zinstraegerGes = drAltV + zzrAltV + sueafAltV + drNeuV + zzrNeuV + sueafNeuV;
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
        return MindZf;

    }

    /**
     * Berechnet Mindestbeteiligung Kürzungskonto. <br/>
     * Funktionsname in Excel: MindZf_KK.
     * 
     * @param t
     *            aktueller Zeitpunkt
     * @param mindZfKkV
     *            Mindestzuführung Kürzungskonto Vorjahr
     * @param mindZf
     *            Mindestzuführung
     * @param rfBZuf
     *            RfB-Zuführung
     * @return Mindestbeteiligung
     */
    public static double mindZfKk(final int t, final double mindZfKkV, final double mindZf, final double rfBZuf) {
        if (t > 0) {
            return Math.max(nanZero(mindZfKkV) + mindZf - rfBZuf, 0.0);
        } else {
            return 0.0;
        }
    }

    /**
     * Berechnet die Mindestbeteiligung der Versicherungsnehmner mit Hilfe eines Zuführungsplans. <br/>
     * Funktionsname in Excel: MindZf_ges.
     * 
     * @param mindZf
     *            Mindestbeteiligung gemäß Mindestzuführungsverordnung
     * @param vgMindZfKk
     *            Mindestbeteiligung Kürzungskonto zum Zeitpunkt t - 1
     * @return Mindestbeteiligung
     */
    public static double mindZfGes(final double mindZf, final double vgMindZfKk) {
        return mindZf + nanZero(vgMindZfKk);
    }

    /**
     * Berechnet den Zieljahresüberschuss. <br/>
     * Funktionsname in Excel: JUE_Ziel.
     * 
     * @param strategie
     *            Strategie zur Ermittlung der Beteiligung der Versicherungsnehmer und Jahresüberschuss
     *            (1=Zielverzinsung, 2=Zielbeteiligung)
     * @param t
     *            Zeitpunkt
     * @param ekVj
     *            Eigenkapital vom Vorjahr
     * @param rEk
     *            Managementregel r_EK zum Zeitpunkt T
     * @param steuersatz
     *            Steuersatz
     * @param ijUEZ
     *            Schalter, Jahreszielerhöhung
     * @param jueErhoehungV
     *            Jahresüberschusszielerhöhung, Vorjahr
     * @return Zieljahresüberschuss
     */
    public static double jueZiel(final int strategie, final int t, final double ekVj, final double rEk,
            final double steuersatz, final boolean ijUEZ, final double jueErhoehungV) {
        double jueZiel = 0.0;
        switch (strategie) {
            case 1: // Zielverzinsung
                jueZiel = rEk / (1 - (steuersatz)) * ekVj + (ijUEZ ? Functions.nanZero(jueErhoehungV) : 0.0);
                break;
            case 2:
            case 3: // 'Zielbeteiligung, dynamisch Zielbeteiligung
                jueZiel = 0.0;
                break;
        }
        return jueZiel;
    }

    /**
     * Berechnet die Erhöhung eines Zieljahresüberschusses<br/>
     * Funktionsname in Excel: JUE_Zielerhoehung.
     * 
     * @param t
     *            Zeitpunkt
     * @param iJuez
     *            Schalter, Berechung der Erhöhung des Zieljahresüberschuss
     * @param jueZiel
     *            - Zieljahresüberschuss
     * @param jUe
     *            Jahresüberschuss
     * @param strategie
     *            ??
     * @return der Wert
     */
    public static double jUeZielerhoehung(final int t, final boolean iJuez, final double jueZiel, final double jUe,
            final int strategie) {

        switch (strategie) {
            case 1:
                if (t == 0) {
                    return 0.0;
                }
                if (iJuez) {
                    return Math.max(jueZiel - jUe, 0.0);
                } else {
                    return 0.0;
                }
            default:
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
     */
    public static double jUERfBZuf(final int ergebnis, final int t, final double[] rohueberschuss,
            final double mindZfGes, final double mindZf, final double jueZiel, final double ueeBestand,
            final double freieRfbVj, final double[] rfbZufVj, final VUHistorie vuHistorie, final double pRohueb,
            final double i56bfRfB, final double i56bSueaf, final double sueafVj, final int strategie,
            final double[] rfb56bVj, final double drVorDeklUeb, final double drLockInUebV, final double ke,
            final double aKe) {
        double JUe = 0.0, rfbZuf = 0.0, RfB56b = 0.0;

        double KE_EK;
        double JUE_Ziel_3;

        // kein Unterschied bei Strategie
        // Ermittlung der Hilfekennzahlen
        final double sumRfb10 = mittlRfbZuf10J(t, vuHistorie, // RfB_Historie,
                rfbZufVj, rfb56bVj);
        final double sumRohueb10 = mittlRohueb10J(t, vuHistorie, // Rohueb_Historie,
                rohueberschuss);

        final double mBeteiligungVN;
        if (sumRohueb10 > 1.0) {
            mBeteiligungVN = Math.max(Math.min(sumRfb10 / sumRohueb10, 0.9), 0.0);
        } else {
            mBeteiligungVN = 0.9;
        }

        final double rohUeb = nanZero(rohueberschuss[t]);

        // a
        if (rohUeb > mindZfGes) {
            switch (strategie) {
                case 1: // Steuerung über die VN-Zielbeteiligung
                    JUe = Math.min(jueZiel, rohUeb - mindZfGes);
                    rfbZuf = rohUeb - JUe;
                    RfB56b = 0.0;
                    break;
                case 2: // Steuerung über eine Zielverzinsung des Eigenkapitals
                    rfbZuf = Math.max(pRohueb * rohUeb, mindZfGes);
                    JUe = rohUeb - rfbZuf;
                    RfB56b = 0.0;
                    break;
                case 3: // KE_EK = NVZ * (EK + BW_GRK + DR_NUEB) + ZiRaZu
                    KE_EK = ke - aKe;
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
     * Berechnet die Deckungsrückstellung des aktuellen Geschäftsjahres vor Deklaration. <br/>
     * Funktionsname in Excel: DR_vor_Dekl.
     * 
     * @param t
     *            Zeitpunkt
     * @param dr
     *            Deckungsrückstellung des letzten Jahres
     * @param leAggrV
     *            Leistungserhöhung, aggregiert, aus dem Vorjahr
     * @param laKwXsAggr
     *            Leistungsanpassung Kapitalwahl
     * @param lbwGarSonErl
     *            Leistungsbarwert SonstErll, deterministisch
     * @param lbw
     *            Leistungsbarwert
     * @param lambda
     *            Gesamt Storno
     * @return Deckungsrückstellung
     */
    public static double drVorDekl(final int t, final double dr, final double leAggrV, final double laKwXsAggr,
            final double lbwGarSonErl, final double lbw, final double lambda) {
        if (t == 0) {
            return dr;
        } else {
            return (dr + laKwXsAggr * lbwGarSonErl) * lambda + (Double.isNaN(leAggrV) ? 0.0 : leAggrV) * lbw;
        }
    }

    /**
     * Berechnet den ausgeschütteten Unternehmensgewinn aus dem erzielten Jahresüberschuss und dem freiwerdenden bzw.
     * aufzubauenden Eigenkapital. <br/>
     * Funktionsname in Excel: ZAG.
     * 
     * @param t
     *            Zeitpunkt
     * @param jue
     *            Jahresüberschuss
     * @param steuer
     *            Steuer
     * @param ek
     *            Eigenkapital
     * @param ekVorjahr
     *            Eigenkapital aus dem Vorjahr
     * @param omega
     *            Projektionslänge
     * @return Unternehmensgewinn
     */
    public static double zag(final int t, final double jue, final double steuer, final double ek,
            final double ekVorjahr, final int omega) {
        if (t == 0 || t >= omega) {
            return 0.0;
        } else {
            return jue - steuer + (ekVorjahr - ek);
        }
    }

    /**
     * Berechnet den zusätzlichen ZAG im letzten Zeitpunkt. <br/>
     * Funktionsname in Excel: ZAG_Endzahlung.
     * 
     * @param t
     *            Zeitpunkt
     * @param omega
     *            Projektionslänge
     * @param cf
     *            Cashflow, das nachdem aller verbindlichkeiten gedeckt sind, übrig bleibt
     * @param steuer
     *            Ertragssteuer
     * @return ZAG
     */
    public static double zagEndzahlung(final int t, final double omega, final double cf, final double steuer) {
        if (t == omega) {
            return cf - steuer;
        } else {
            return 0.0;
        }
    }

    /**
     * Berechnet zum Zeitpunkt T den Referenzzinssatz der letzten 10 Jahre (d.h der Jahre T-(10-1),..., T). <br/>
     * Funktionsname in Excel: ReferenzZinssatz
     * 
     * @param t
     *            Aktueller Zeitpunkt
     * @param zzrSpotEsgArr
     *            Renditen von AAA-Anleihen der Jahre -9,...,0
     * @param zinsArr
     *            Rendite einer 10 Jährigen Anleihe der Jahre 0,1,2,..., T
     * @return Referenzzinssatz
     */
    public static double referenzZinssatz(final int t, final double[] zzrSpotEsgArr, final double[] zinsArr) {
        double referenzZinssatz = 0.0;
        for (int i = 0; i <= 9; ++i) {
            if (t - i > 0) {
                referenzZinssatz += zzrSpotEsgArr[i];
            } else {
                referenzZinssatz += zinsArr[i];
            }
        }
        return referenzZinssatz /= 10.0;

    }

    /**
     * Berechnet den Refernzzinssatz 2M. <br/>
     * Funktionsname in Excel: refZins_2M
     * 
     * @param refZinsV
     *            Referenzzinssatz, Vorjahr
     * @param refZins
     *            Referenzzinssatz
     * @param refZins2Mv
     *            Referenzzinssatz 2M, Vorjahr
     * @param basisZins
     *            Basiszinssatz
     * @param t
     *            Zeit
     * @param anwendungsDifferenzAnteilig
     *            Parameter für Methode 2M
     * @return der Wert
     * @param startRefZins
     *            Startreferenzzinssatz für Korridor/2M, falls diese schon seit dem Vorjahr oder früher angewendet werden
     */
    public static double refZins2M(final double refZinsV, final double refZins, final double refZins2Mv,
            final double basisZins, final int t, double anwendungsDifferenzAnteilig, double startRefZins) {
        final double refzinsVergleich;
        double refZins2m = 0.0;
        double differenz1 = 0.0;
        double differenz2 = 0.0;
        if (t == 1) {
            if (Double.isNaN(startRefZins)) {
                refzinsVergleich = refZinsV;
            } else {
                refzinsVergleich = startRefZins;
            }
        } else {
            refzinsVergleich = nanZero(refZins2Mv);
        }
        differenz1 = Math.signum(refZins - refzinsVergleich)
                * Math.round(10000.0 * (Math.abs(refZins - refzinsVergleich) + 0.0000499999999)) / 10000.0;
        differenz2 = Math.signum(basisZins - refzinsVergleich)
                * Math.round(10000.0
                        * (anwendungsDifferenzAnteilig * Math.abs(basisZins - refzinsVergleich) + 0.0000499999999))
                / 10000.0;
        if (Math.signum(differenz1) == Math.signum(differenz2)) {
            refZins2m = refzinsVergleich
                    + Math.signum(differenz1) * Math.min(Math.abs(differenz1), Math.abs(differenz2));
        } else {
            refZins2m = refzinsVergleich;
        }
        return refZins2m;
    }

    /**
     * Berechnet zum Zeitpunkt T die mittlere RfB Zuführung der letzten 10 Jahre (d.h der Jahre T-10,..., T-1). Name in
     * Excel: mittlRfBZuf_10J.
     * 
     * @param t
     *            Aktueller Zeitpunkt
     * @param vuHistorie
     *            historische Werte VU
     * @param rfbZufHistProj
     *            Zuführung zur RfB der Jahre -9,-8,...,0
     * @param rfb56bHistProj
     *            Zuführung zur RfB der Jahre 0,1,2,3
     * @return
     */
    private static double mittlRfbZuf10J(final int t, final VUHistorie vuHistorie, // final double[] RfB_Zuf_hist,
            final double[] rfbZufHistProj, final double[] rfb56bHistProj) {

        double mittlRfbZuf10J = 0.0;
        int zeit = t - 1;
        for (int i = 0; i <= 9; ++i) {
            if (zeit > 0) {
                mittlRfbZuf10J += rfbZufHistProj[zeit] - rfb56bHistProj[zeit];
            } else {
                mittlRfbZuf10J += vuHistorie.get(zeit).getRfBZufuehrung(); // RfB_Zuf_hist[10 + t - i - 1];
            }
            --zeit;
        }
        return mittlRfbZuf10J / 10.0;
    }

    /**
     * Berechnet zum Zeitpunkt T den mittleren Rohüberschuss der letzten 10 Jahre (d.h der Jahre T-10,..., T-1). Name in
     * Excel: mittlRohueb_10J
     * 
     * @param t
     *            Aktueller Zeitpunkt
     * @param vuHistorie
     *            historische Werte VU
     * @param rohuebhistProj
     *            Rohüberschuss der Jahre 0,1,2,3
     * @return
     */
    private static double mittlRohueb10J(final int t, final VUHistorie vuHistorie, final double[] rohuebhistProj) {
        double mittlRohueb10J = 0.0;
        int zeit = t - 1;
        for (int i = 0; i <= 9; ++i) {
            if (zeit > 0) {
                // 'mittlRohueb_10J = mittlRohueb_10J + WorksheetFunction.Max(Rohueb_hist_proj(t - i, 1), 0)
                mittlRohueb10J += rohuebhistProj[zeit];
            } else {
                // 'mittlRohueb_10J = mittlRohueb_10J + WorksheetFunction.Max(Rohueb_hist(10 + t - i - 1, 1), 0)
                mittlRohueb10J += vuHistorie.get(zeit).getRohueberschuss(); // Rohueb_hist[10 + t - i - 1];
            }
            --zeit;
        }
        return mittlRohueb10J / 10.0;
    }

    /**
     * Schreibt das Eigenkapital fort. <br/>
     * Funktionsname in Excel: Eigenkapital_Fortschreibung.
     * 
     * @param t
     *            Zeitpunkt
     * @param ekZiel
     *            Managementregel (Wie hoch soll das Eigenkapital in Prozent der Deckungsrückstellung ausfallen?)
     * @param dr
     *            Deckungsrückstellung
     * @return Eigenkapital
     */
    public static double eigenkapitalFortschreibung(final int t, final double ekZiel, final double dr) {
        if (t > 0) {
            return ekZiel * dr;
        } else {
            // Dieser Fall darf nicht eintreten.
            return 0.0;
        }
    }

    /**
     * Schreibt das Genussrechkapital und Nachrangsdarlehen fort. <br/>
     * Funktionsname in Excel: GR_NRD.
     * 
     * @param t
     *            Zeitpunkt
     * @param vgGrNrd
     *            Genussrechtkapital und Nachrangdarlehen aus dem Vorjahr
     * @param rueckZahlung
     *            Rückzahlung des Genussrechtkapitals und Nachrangdarlehen im Jahr t
     * @return Wert
     */
    public static double grNrd(int t, final double vgGrNrd, final double rueckZahlung) {
        if (t > 0) {
            return nanZero(vgGrNrd) - rueckZahlung;
        } else {
            // Dieser Fall darf nicht eintreten.
            return 0.0;
        }
    }

    /**
     * Berechnet die abzuführende Ertragssteuer. <br/>
     * Funktionsname in Excel: Ertragssteuer.
     * 
     * @param jue
     *            Jahresüberschuss
     * @param steuersatz
     *            Steuersatz
     * @param vv
     *            Verlustvortrag
     * @return Ertragssteuer
     */
    public static double ertragssteuer(final double jue, final double steuersatz, final double vv) {
        // 'Teil des Verlustes zum anrechnen mit Gewinnen im Jahr T
        final double VV_x = Math.min(nanZero(vv), 0.4 * Math.min(1000000, nanZero(jue)) + 0.6 * nanZero(jue));
        // 'Steuerberechnung
        return Math.max(jue - VV_x, 0) * steuersatz;
    }

    /**
     * Berechnet die abzuführende Ertragssteuer nach der LS-Korrektur<br/>
     * Funktionsname in Excel: Ertragssteuer_LS.
     * 
     * @param ertragssteuer
     *            Ertragssteuer
     * @param lsHgb
     *            Latente Steuer HGB im Zeitpunkt t
     * @return der Wert
     */
    public static double ertragssteuerLs(final double ertragssteuer, final double lsHgb) {
        return Math.max(ertragssteuer - lsHgb, 0.0);
    }

    /**
     * Berechnet HGB-Latente Steuer<br/>
     * Funktionsname in Excel: LS_HGB
     * 
     * @param lsHgbV
     *            HGB Latente Steuer, Vorjahr
     * @param ertragssteuer
     *            Steuer, festgelegt
     * @param ertragssteuerLs
     *            Steuer, nach der LS-Korrektur
     * @return der Wert
     */
    public static double lsHgb(final double lsHgbV, final double ertragssteuer, final double ertragssteuerLs) {
        return lsHgbV - (ertragssteuer - ertragssteuerLs);
    }

    /**
     * Berechnet den Verlustvortrag (Sammelkonto für Verluste aus den Vorjahren). <br/>
     * Funktionsname in Excel: VV.
     * 
     * @param vgVv
     *            Verlustvortrag aus dem Vorjahr
     * @param jue
     *            Jahresüberschuss
     * @param ertragssteuer
     *            Ertragssteuer im Jahr t
     * @param steuersatz
     *            Steuersatz im Jahr t
     * @return Verlustvortrag
     */
    public static double vv(final double vgVv, final double jue, final double ertragssteuer, final double steuersatz) {
        if (steuersatz == 0) {
            return nanZero(vgVv) - jue;
        } else {
            return nanZero(vgVv) + (ertragssteuer / steuersatz - jue);
        }
    }

    /**
     * Berechnet die nicht festgelegten Rückstellungen für Beitragsrückerstattung. <br/>
     * Funktionsname in Excel: nfRfB.
     * 
     * @param sueAfAgg
     *            Schlussüberschussfonds
     * @param fRfBFrei
     *            freie Rückstellungen für Beitragsrückerstattung
     * @return Rückstellung
     */
    public static double nfRfB(final double sueAfAgg, final double fRfBFrei) {
        return sueAfAgg + fRfBFrei;
    }

    /**
     * Berechnet den ZAG zum Auszahlungszeitpunkt.<br/>
     * Funktionsname in Excel: ZAG_faellig.
     * 
     * @param t
     *            Zeitpunkt
     * @param vgZag
     *            ZAG aus dem t-1
     * @return ZAG
     */
    public static double zagFaellig(final int t, final double vgZag) {
        return (t == 1 ? 0.0 : vgZag);
    }

}
