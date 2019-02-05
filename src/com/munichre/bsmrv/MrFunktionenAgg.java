/*
HAFTUNGSAUSSCHLUSS
Dieses Excel Sheet (einschließlich des beigefügten Java Codes, zusammen das „BSM Tool“) wurde von der Münchener Rückversicherungs-Gesellschaft AG („Munich Re“) 
ausschließlich für die Rheinland Lebensversicherung AG zum Zwecke der eigenen Modellierung der Rückversicherungs-Effekte unter Solvency II 
(die „Modellierung“) erstellt. Inhalt und Umfang dieses BSM Tools sind lediglich als Information und Grundlage für weiterführende 
Gespräche mit Ihnen anzusehen. 
Die in diesem BSM Tool enthaltenen Informationen sind Eigentum der Munich Re und vertraulich. 
Kein Teil dieses BSM Tools darf auf irgendeine Weise ohne Erlaubnis der Munich Re reproduziert oder weitergegeben werden. 
Durch Entgegennahme dieses BSM Tools sichern Sie der Munich Re zu, die darin enthaltenen sowie weitere zur Verfügung gestellte 
vertrauliche Informationen nur zur Modellierung zu verwenden. Sie dürfen die Ergebnisse der Modellierung und die Details des BSM Tools 
nur jenen eigenen Organen und Mitarbeitern zugänglich machen, die die vertraulichen Informationen zur weiteren Analyse möglicher 
Transaktionen mit der Munich Re benötigen.
Die in dem BSM Tool enthaltenen Information und Parameters wurden sorgfältig auf der Basis öffentlich zugänglicher Informationen 
zusammengestellt und in Bezug auf Rückversicherungsthemen von der Munich Re erweitert. 
Eine Gewähr für die Richtigkeit und Vollständigkeit kann jedoch nicht übernommen werden, ebenso wenig besteht eine Verpflichtung, 
die Programmierung des BSM Tools zu aktualisieren oder sie an zukünftige Ereignisse oder Entwicklungen anzupassen. 
Eine Haftung der Munich Re für die Ergebnisse der Modellierung und für die erfolgreiche Nutzung des BSM Tools ist ausgeschlossen.                                      
*/

package com.munichre.bsmrv;

public class MrFunktionenAgg {

    private MrFunktionenAgg() {
        throw new IllegalStateException(String.format("%s is not meant to be instantiated", this.getClass().getName()));
    }

    /**
     * Berechnet die zedierte Delta ZZR für den betrachteten Bestand unter Anwendung des ZZR-Caps. <br>
     * Sind Alt- und Neubestand im ZZR-Aufbau und der Cap-Wert wird erreicht, werden beide Bestände proportional zedierten Delta ZZR der Bestände gekappt.
     * Ist nur ein Bestand im ZZR-Aufbau und der Cap-Wert wird erreicht, wird lediglich die Delta ZZR dieses Bestandes gekappt.<br/>
     * Excel-Funktion: zedDeltaZzrCapBestand()
     * @param zedDeltaZzrBestand
     *      Zedierte Delta ZZR, betrachteter Bestand
     * @param zedDeltaZzrAlt
     *      Zedierte Delta ZZR, Altbestand
     * @param zedDeltaZzrNeu
     *      Zedierte Delta ZZR, Neubestand
     * @param zedZzrVorjahr
     *      Zedierte ZZR (Outstanding), Vorjahr
     * @param zzrCap
     *      Cap-Wert für die zedierte ZZR (Outstanding)
     * @return der Wert
     */
    public static double zedDeltaZzrCapBestand(double zedDeltaZzrBestand, double zedDeltaZzrAlt, double zedDeltaZzrNeu,
            double zedZzrVorjahr, double zzrCap) {
        double zedDeltaZzr;
        double cap;

        zedDeltaZzr = zedDeltaZzrAlt + zedDeltaZzrNeu;
        cap = Math.max(0, zedZzrVorjahr + zedDeltaZzr - zzrCap);

        if (cap > 0) {

            if (Math.signum(zedDeltaZzrAlt) + Math.signum(zedDeltaZzrNeu) == 0) { // Nur Bestand mit ZZR-Aufbau kappen
                if (zedDeltaZzrBestand < 0) {
                    return zedDeltaZzrBestand;
                } else {
                    return zedDeltaZzrBestand - cap;
                }

            } else { // quotale Aufteilung des Cap
                return zedDeltaZzrBestand - cap * zedDeltaZzrBestand / zedDeltaZzr;
            }

        } else { // Keine Anwendung Cap
            return zedDeltaZzrBestand;
        }

    }

    /**
     * Berechnet die laufende Summe der zedierten Delta ZZR des Teilbestand (gekappt) <br/>
     * Excel-Funktion: zedZzrBestand()
     * @param t
     *      Aktueller Zeitpunkt
     * @param zedZzrVorjahr
     *      Zedierte ZZR des Bestands (Outstanding), Vorjahr
     * @param zedDeltaZzr
     *      Zedierte Delta ZZR des Bestands (gekappt)
     * @param adjZedDeltaZzrVorjahr
     *      Adjustierung der zedierten Delta ZZR im letzten Rückzahlungsjahr, Vorjahr
     * @param zedZzrGesamt
     *      Zedierte ZZR (Outstanding), Gesamtbestand
     * @return der Wert
     */
    public static double zedZzrBestand(int t, double zedZzrVorjahr, double zedDeltaZzr, double adjZedDeltaZzrVorjahr,
            double zedZzrGesamt) {

        double zedZzrBestand = 0.0;

        if (t > 0 && Math.abs(zedZzrGesamt) > 0.001) {
            zedZzrBestand = zedZzrVorjahr + zedDeltaZzr + adjZedDeltaZzrVorjahr;
        }

        return (Math.abs(zedZzrBestand) < 0.001 ? 0.0d : zedZzrBestand);
    }

    /**
     * Berechnet die laufende Summe der zedierten Delta ZZR (gekappt) <br/>
     * Excel-Funktion: zedZzr()
     * @param t
     *      Aktueller Zeitpunkt
     * @param zedZzrVorjahr
     *      Zedierte ZZR (Outstanding), Vorjahr
     * @param zedDeltaZzr
     *      Zedierte Delta ZZR (gekappt)
     * @param adjZedDeltaZzrVorjahr
     *      Adjustierung der zedierten Delta ZZR im letzten Rückzahlungsjahr, Vorjahr
     * @return der Wert
     */
    public static double zedZzr(int t, double zedZzrVorjahr, double zedDeltaZzr, double adjZedDeltaZzrVorjahr) {

        double zedZzr = 0.0;

        if (t > 0) {
            zedZzr = zedZzrVorjahr + zedDeltaZzr + adjZedDeltaZzrVorjahr;
        }

        return (Math.abs(zedZzr) < 0.001 ? 0.0d : zedZzr);
    }

    /**
     * Berechnet einen Indikator für die erste Phase des ZZR Auf- und Abbaus, an der der RV beteiligt ist.</br>
     * TRUE, FALSE ab dem Zeitpunkt t, zu dem die Schattenrechnung der zedierten ZZR zum ersten Mal auf oder unter Null fällt. 
     * @param zedZzr
     *      Zedierte ZZR (Outstanding / Schattenrechnung)
     * @param zedZzrVj
     *      Zedierte ZZR (Outstanding / Schattenrechnung), Vorjahr
     * @param flagErsteZedZzrPhaseVj
     *      Wert der Indikatorvariable, Vorjahr
     * @return den Wert
     */
    public static boolean flagErsteZedZzrPhase(double zedZzr, double zedZzrVj, boolean flagErsteZedZzrPhaseVj) {

        if (zedZzr < 0.001 && zedZzrVj > 1) {
            return false;
        } else {
            return flagErsteZedZzrPhaseVj;
        }
    }

    /**
     * Berechnet eine Adjustierung der zedierten Delta ZZR, falls im letzten Jahr der Partizipation des RV am Abbau mehr als das Outstanding rückgezahlt wurde. <br/> 
     * @param zedZzr
     *      Zedierte ZZR (Outstanding)
     * @param zedDeltaZzrBestand
     *      Zedierte Delta ZZR des betrachteten Bestands
     * @param zedDeltaZzrAlt
     *      Zedierte Delta ZZR, Altbestand
     * @param zedDeltaZzrNeu
     *      Zedierte Delta ZZR, Neubestand
     * @return der Wert
     */
    public static double adjZedDeltaZZR(double zedZzr, double zedDeltaZzrBestand, double zedDeltaZzrAlt,
            double zedDeltaZzrNeu) {

        double adjZedDeltaZZR;
        double zedDeltaZzr = 0.0;
        zedDeltaZzr = zedDeltaZzrAlt + zedDeltaZzrNeu;

        if (zedZzr < 0 && zedDeltaZzr != 0) {
            adjZedDeltaZZR = (zedDeltaZzrBestand / zedDeltaZzr) * (-zedZzr);
            return (Math.abs(adjZedDeltaZZR) < 0.001 ? 0.0d : adjZedDeltaZZR);
        } else {
            return 0.0;
        }
    }

    /**
     * Berechnet die Rückversicherungsgebühr
     * @param t
     *      Aktueller Zeitpunkt
     * @param gebuehrRvAbsolut
     *      Rückversicherungsgebühr, absolut
     * @param zedZzrVorjahr
     *      Zedierte ZZR, Vorjahr
     * @param gebuehrRvSatz
     *      Satz der laufenden Rückversicherungsgebühr auf die zedierte ZZR (bp)
     * @param schalterQuotenRv
     *      Schalter Quoten-RV
     * @return den Wert
     */
    public static double gebuehrRv(int t, double gebuehrRvAbsolut, double zedZzrVorjahr, double kumZeVj,
            double gebuehrRvSatz, boolean schalterQuotenRv) {


        double gebuehrRelativ;

        if (schalterQuotenRv && (zedZzrVorjahr > 1 || kumZeVj > 1 || t == 1)) {
            gebuehrRelativ = gebuehrRvSatz / 10000.0 * zedZzrVorjahr;

            return Math.max(gebuehrRvAbsolut, gebuehrRelativ);

        } else {
            return 0.0d;
        }
    }

    /**
     * Teilt die Rückversicherungsgebühr auf Alt- und Neubestand nach zedierter ZZR auf. <br/>
     * Ist ein Bestand bereits im ZZR-Abbau, geht die RV-Gebühr voll zulasten des Bestand, der sich noch im ZZR-Aufbau befindet.
     * @param gebuehrRv
     *      Rückversicherungsgebühr, gesamt
     * @param zedZzr
     *      Zedierte ZZR (Outstanding), Vorjahr
     * @param zedZzrBestand
     *      Zedierte ZZR (Outstanding), betrachteter Bestand, Vorjahr
     * @param zedZzrAlt
     *      Zedierte ZZR (Outstanding), Altbestand, Vorjahr
     * @param zedZzrNeu
     *      Zedierte ZZR (Outstanding), Neubestand, Vorjahr
     * @return den Wert
     */
    public static double gebuehrRvBestand(int t, double gebuehrRv, double zedZzrVorjahr, double zedZzrBestandVorjahr,
            double zedZzrAltVorjahr, double zedZzrNeuVorjahr) {

        double gebuehrRvBestand;
        gebuehrRvBestand = 0.0;

        if (Math.signum(zedZzrAltVorjahr) != Math.signum(zedZzrNeuVorjahr)) { // Wenn Altbestand bereits negatives Outstanding hat, Gebuehr voll in Neubestand
            if (zedZzrBestandVorjahr <= 0) {
                gebuehrRvBestand = 0.0;
            } else {
                gebuehrRvBestand = gebuehrRv;
            }
        } else if (zedZzrVorjahr > 0) { // Proportionale Aufteilung der Gebühr nach zedierter ZZR
            gebuehrRvBestand = gebuehrRv * zedZzrBestandVorjahr / zedZzrVorjahr;
        } else if (t == 1) {
            gebuehrRvBestand = 0.5 * gebuehrRv;
        }

        return gebuehrRvBestand;
    }

    /**
     * Berechnet das Technische Ergebnis
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-RV
     * @param biometrieCap
     *      Cap-Wert des Loss Carry Forward auf dem Risikoergebnis
     * @param zedRe
     *      Zediertes Risikoergebnis
     * @param lcfVorjahr
     *      Loss Carry Forward, Vorjahr
     * @return den Wert
     */
    public static double technischesErgebnis(int t, int lfzQuotenRv, double biometrieCap, double zedRe,
            double lcfVorjahr) {

        if (t > 0 && t <= lfzQuotenRv) {
            return Math.max(-biometrieCap, lcfVorjahr + zedRe);
        } else {
            return 0.0;
        }
    }

    /**
     * Berechnet den Loss Carry Forward auf dem Risikoergebnis mit Cap des LCF
     * @param t
     *       Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-RV
     * @param zedRe
     *      Zediertes Risikoergebnis
     * @param lcfVorjahr
     *      Loss Carry Forward, Vorjahr
     * @param lcfCap
     *      Cap-Wert des Loss Carry Forward
     * @return den Wert
     */
    public static double lossCarryFwd(int t, int lfzQuotenRv, double zedRe, double lcfVorjahr, double lcfCap) {

        if (t > 0 && t <= lfzQuotenRv) {
            return Math.min(0, Math.max(-lcfCap, lcfVorjahr + zedRe));
        } else {
            return 0.0d;
        }
    }

    /**
     * Berechnet die Gewinnbeteiligung auf das Technische Ergebnis
     * @param gwbSatzTechnErgebnis
     *      Gewinnbeteiligungssatz, der auf das Technische Ergebnis angewendet wird
     * @param technErgebnis
     *      Technisches Ergebnis
     * @return den Wert
     */
    public static double gwbTechnErgebnis(double gwbSatzTechnErgebnis, double technErgebnis) {
        return Math.max(0, gwbSatzTechnErgebnis * technErgebnis);
    }

    /**
     * Teilt die Gewinnbeteiligung auf das Technische Ergebnis nach dem zedierten Risikoergebnis zwischen Alt- und Neubestand auf.
     * @param gwbTechnErgebnis
     *      Gewinnbeteiligung auf das Technische Ergebnis
     * @param zedReBestand
     *      Zediertes Risikoergebnis des betrachteten Bestands
     * @param zedReAlt
     *      Zediertes Risikoergebnis Altbestand
     * @param zedReNeu
     *      Zediertes Risikoergebnis Neubestand
     * @return den Wert
     */
    public static double gwbTechnErgebnisBestand(double gwbTechnErgebnis, double zedReBestand, double zedReAlt,
            double zedReNeu) {

        double zedRe;
        zedRe = zedReAlt + zedReNeu;

        if (gwbTechnErgebnis > 0) {
            if (Math.signum(zedReAlt) + Math.signum(zedReNeu) == 0) { // Einer der Bestaende mit negativem Risikoergebnis
                if (zedReBestand < 0) {
                    return 0.0d;
                } else {
                    return gwbTechnErgebnis;
                }
            } else { // quotale Aufteilung
                return gwbTechnErgebnis * zedReBestand / zedRe;
            }
        } else {
            return 0.0d;
        }
    }

    /**
     * Berechnet den Rückversicherung-Saldo <br/>
     * Excel-Funktion: rvSaldo()
     * @param zedRe
     *      Zediertes Risikoergebnis
     * @param lcf
     *      Loss Carry Forward
     * @param lcfVj
     *      Loss Carry Forward, Vorjahr
     * @param biometrieCap
     *      Cap-Wert des Loss Carry Forward auf dem Risikoergebnis
     * @param zedUee
     *      Zediertes Kostenergebnis
     * @param gwbTechnErg
     *      Gewinnbeteiligung des EVU am Technischen Ergebnis der RV
     * @param gwbUeErg
     *      Gewinnbeteiligung des EVU am Übrigen Ergebnis
     * @param komKapErg
     *      Kommission RV -> EVU, verrechnet im Kapitalanlageergebnis
     * @param komUeErg
     *      Kommission RV -> EVU, verrechnet im Übrigen Ergebnis
     * @param zedRmz
     *      Zedierter rechnungsmäßiger Zins
     * @param depotzins
     *      Depotzins der RV
     * @param zedDeltaZzr
     *      Zedierte Delta ZZR (gekappt)
     * @param adjZedDeltaZzr
     *      Adjustierung der zedierten Delta ZZR
     * @param ausfallRv
     *      Kumulierte Ausfallwahrscheinlichkeit des Rückversicherers
     * @return der Wert
     */
    public static double rvSaldo(double zedRe, double lcf, double lcfVj, double biometrieCap, double zedUeErg,
            double gwbTechnErg, double gwbUeErg, double komKapErg, double komUeErg, double zedRmz, double depotzins,
            double zedDeltaZzr, double adjZedDeltaZzr, double ausfallRv) {

        // Alle Bestandteile gehen ein, unabhängig von Cash / Non-Cash -> benutze nicht die rvEffekt Größen

        double rvSaldo;

        // Berücksichtigung LCF mit Cap
        if (lcf == -biometrieCap) { // Im Cap
            rvSaldo = (Math.abs(lcfVj - lcf) + gwbTechnErg // Risikoergebnis
                    - zedUeErg + gwbUeErg + komUeErg // Übriges Ergebnis
                    + zedRmz - depotzins + zedDeltaZzr + adjZedDeltaZzr + komKapErg) // Zinsergebnis
                    * ausfallRv;
        } else { // nicht im Cap
            rvSaldo = (-zedRe + gwbTechnErg // Risikoergebnis
                    - zedUeErg + gwbUeErg + komUeErg // Übriges Ergebnis
                    + zedRmz - depotzins + zedDeltaZzr + adjZedDeltaZzr + komKapErg) // Zinsergebnis
                    * ausfallRv;
        }

        return (Math.abs(rvSaldo) < 0.001 ? 0.0 : rvSaldo);
    }

    /**
     * Berechnet den Cash Anteil des Saldos der Rückversicherung <br/>
     * Excel-Funktion: rvSaldoCashAnteil()
     * @param schalterRvEffRmzNonCash
     *      Schalter: Effekt der RV auf den rechnungsmäßigen Zins als Non-Cash: 0: Cash, 1: Non-Cash
     * @param schalterRvEffZzrNonCash
     *      Schalter: Effekt der ZZR RV als Non-Cash: 0: Cash, 1: Non-Cash
     * @param schalterRvEffReNonCash
     *      Schalter: Effekt der RV auf das Risikoergebnis als Non-Cash: 0: Cash, 1: Non-Cash
     * @param schalterRvEffUeeNonCash
     *      Schalter: Effekt der Rückversicherung auf das Übrige Ergebnis als Non-Cash: 0: Cash, 1: Non-Cash
     * @param rvEffRmz
     *      Effekt der Rückversicherung auf den rechnungsmäßigen Zins (vor Zusatzerträgen)
     * @param rvEffRe
     *      Effekt der Rückversicherung auf das Risikoergebnis
     * @param rvEffUee
     *      Effekt der Rückversicherung auf das Kostenergebnis
     * @return der Wert 
     */
    public static double rvSaldoCashAnteil(boolean schalterRvEffRmzNonCash, boolean schalterZzrEffNonCash,
            boolean schalterRvEffReNonCash, boolean schalterRvEffUeeNonCash, double rvEffRmz, double rvEffZzr,
            double rvEffRe, double rvEffUee) {

        double rvSaldoCashAnteil = 0.0;

        // rmZ Non-Cash & ZZR Non-Cash: 0
        if (schalterRvEffRmzNonCash && !schalterZzrEffNonCash) {
            rvSaldoCashAnteil += rvEffZzr; // rmZ Non-Cash & ZZR Cash: zedDeltaZzr
        } else if (!schalterRvEffRmzNonCash) {
            // Wenn rmZ Cash, dann ist -rvEffRmz:
            //   ZZR Cash:      (zedRmz - DepZ) + zedDeltaZzr
            //   ZZR Non-Cash:  (zedRmz - DepZ)
            rvSaldoCashAnteil -= rvEffRmz;
        }

        if (!schalterRvEffReNonCash) {
            rvSaldoCashAnteil += rvEffRe;
        }

        if (!schalterRvEffUeeNonCash) {
            rvSaldoCashAnteil += rvEffUee;
        }

        return (Math.abs(rvSaldoCashAnteil) < 0.001 ? 0.0 : rvSaldoCashAnteil);

    }

    /**
     * Berechnet den Cash Anteil des RV Saldos inklusive möglicher Zusatzerträge durch Nachrangereignis im Jahr t.
     * @param t
     *      Aktueller Zeitpunkt
     * @param rvQuoteLaufzeit
     *      Laufzeit der Quoten-RV
     * @param rvSaldoCashAnteil
     *      Cash-Anteil des RV-Saldos ohne Zusatzerträge durch Nachrangereignis
     * @param nachrangSaldoAlt
     *      Delta des kumulierten Zusatzertrags, der sich aus Nachrangereignissen ergibt, Altbestand
     * @param nachrangSaldoNeu
     *      Delta des kumulierten Zusatzertrags, der sich aus Nachrangereignissen ergibt, Neubestand
     * @param kumZe
     *      Kumulierte Zusatzerträge durch Nachrangereignis
     * @param schalterZusatzertragNonCash
     *      Schalter: Zusatzertrag durch Nachrangereignis: 0: Cash, 1: Non-Cash
     * @return
     */
    public static double rvSaldoCashAnteilInklZE(int t, int rvQuoteLaufzeit, double rvSaldoCashAnteil,
            double nachrangSaldoAlt, double nachrangSaldoNeu, double kumZe, boolean schalterZusatzertragNonCash) {

        double rvSaldoCashAnteilInklZE = 0.0;

        if (!schalterZusatzertragNonCash) { // Zusatzertrag in Cash
            rvSaldoCashAnteilInklZE = rvSaldoCashAnteil + nachrangSaldoAlt + nachrangSaldoNeu;

        } else if (schalterZusatzertragNonCash && t == rvQuoteLaufzeit && kumZe > 0.001) {
            // Non-Cash Variante: Falls ausgesetzte Zahlungen zu Vertragsende noch vorhanden --> Zahlung in Cash (Bilanzeffekt)
            rvSaldoCashAnteilInklZE = rvSaldoCashAnteil + kumZe;

        } else { // Zusatzertrag Non Cash
            rvSaldoCashAnteilInklZE = rvSaldoCashAnteil;
        }

        return rvSaldoCashAnteilInklZE;

    }

    /**
     * 
     * @param cashOutFlow
     * @param t
     * @param rvQuoteLaufzeit
     * @param nachrangSaldoAlt
     * @param nachrangSaldoNeu
     * @param kumZe
     * @param schalterQuotenRv
     * @param schalterRvArt
     * @param schalterZusatzertragNonCash
     * @return
     */
    public static double cashOutFlowInklZe(double cashOutFlow, int t, int rvQuoteLaufzeit, double nachrangSaldoAlt,
            double nachrangSaldoNeu, double kumZe, boolean schalterQuotenRv, boolean schalterRvArt,
            boolean schalterZusatzertragNonCash) {

        double cashOutFlowInklZe = cashOutFlow;

        if (schalterQuotenRv && schalterRvArt) { // Lila
            if (!schalterZusatzertragNonCash) {
                // Cash-Fall: Anpassung jährlich um Zusatzertrag
                cashOutFlowInklZe -= (nachrangSaldoNeu + nachrangSaldoAlt);
            }

            // Non-Cash-Fall: Nur in t_max Anpassung um ausstehende ausgesetzte Zahlungen
            if (schalterZusatzertragNonCash && t == rvQuoteLaufzeit && kumZe > 0.001) {
                cashOutFlowInklZe -= kumZe;
            }
        }

        return cashOutFlowInklZe;
    }

    /**
     * Berechnet den Effekt der Rückversicherung auf den rechnungsmäßigen Zins<br/>
     * Excel-Funktion: rvEffRmz
     * @param komKapErg
     *      Kommission, verrechnet im Kapitalanlageergebnis
     * @param zedRmz
     *      Zedierter Rechnungsmäßiger Zins
     * @param depotzins
     *      Depotzins
     * @param zedDeltaZzr
     *      Zedierte Delta ZZR (ZZR Aufwand / Ertrag)
     * @param adjZedDeltaZzr
     *      Adjustierung der zedierten Delta ZZR im letzten Jahr der Rückzahlung (Non-Cash) bei "Überrückzahlung"
     * @param gebuehrRvBestand
     *      RV-Gebühr, die auf den Bestand entfällt
     * @param schalterErgebnisTopfRvGebuehr
     *      Schalter: In welchem Ergebnistopf wird die RV-Gebühr verrechnet?
     * @param schalterErgebnisTopfRvGebuehr
     *      Schalter: Erfolgt die Finanzierung der ZZR in Cash oder Non-Cash?
     * @return der Wert
     */
    public static double rvEffektRmz(double komKapErg, double zedRmz, double depotzins, double zedDeltaZzr,
            double adjZedDeltaZzr, double gebuehrRvBestand, int schalterErgebnisTopfRvGebuehr,
            boolean schalterZzrEffektNonCash) {

        double rvEffRmz;

        rvEffRmz = -(zedRmz - depotzins + komKapErg);

        // ZZR Beteiligung in Cash
        if (!schalterZzrEffektNonCash) {
            rvEffRmz -= (zedDeltaZzr + adjZedDeltaZzr);
        }

        // Verrechnung der RV-Gebuehr im Kapitalanlageergebnis
        if (schalterErgebnisTopfRvGebuehr == 1) {
            rvEffRmz += gebuehrRvBestand;
        }

        return (Math.abs(rvEffRmz) < 0.001 ? 0 : rvEffRmz);
    }

    /**
     * Berechnet den Effekt der Rückversicherung auf das Risikoergebnis <br/>
     * Beruecksichtigung eines Loss Carry Forward mit Cap.<br/>
     * Option zur Verrechnung der Gwb auf das Technische Ergebnis im Übrigen Ergebnis
     * @param zedReBestand
     *      Zediertes Risikoergebnis, betrachteter Bestand
     * @param zedReAlt
     *      Zediertes Risikoergebnis, Altbestand
     * @param zedReNeu
     *      Zediertes Risikoergebnis, Neubestand
     * @param lcf
     *      Loss Carry Forward
     * @param lcfVorjahr
     *      Loss Carry Forward, Vorjahr
     * @param capBiometrie
     *      Cap-Wert Biometrie (max. Loss Carry Forward)
     * @param gwbTechnErgBestand
     *      Gewinnbeteiligung am Technischen Ergebnis, betrachteter Bestand
     * @param schalterGwbTechnErgInUee
     *      Schalter, ob Gewinnbeteiligung am Technischen Ergebnis im Übrigen Ergebnis verrechnet wird. <br/>0: nein, 1: ja
     * @param gebuehrRvBestand
     *      Rückversicherungsgebühr, betrachteter Bestand
     * @param schalterErgebnisTopfRvGebuehr
     *      Schalter: In welchem Ergebnistopf wird die RV-Gebühr verrechnet?
     * @return der Wert
     */
    public static double rvEffektRe(double zedReBestand, double zedReAlt, double zedReNeu, double lcf,
            double lcfVorjahr, double capBiometrie, double gwbTechnErgBestand, boolean schalterGwbTechnErgInUee,
            double gebuehrRvBestand, int schalterErgebnisTopfRvGebuehr) {

        double rvEffektRisikoErg = 0.0;
        double zedRe = zedReAlt + zedReNeu;

        if (lcf == -capBiometrie && zedRe != 0) { // Im Cap
            if (Math.signum(zedReAlt) + Math.signum(zedReNeu) == 0) { // Einer der Bestaende mit negativem Risikoergebnis
                if (zedReBestand > 0) {
                    rvEffektRisikoErg = -zedReBestand;
                } else {
                    rvEffektRisikoErg = Math.abs(lcfVorjahr - lcf) + (zedRe - zedReBestand);
                }
            } else { // quotal
                rvEffektRisikoErg = zedReBestand / zedRe * Math.abs(lcfVorjahr - lcf);
            }
        } else { // Nicht im Cap
            rvEffektRisikoErg = -zedReBestand;
        }

        // Verrechnung der Gewinnbeteiligung auf technisches Ergebnis im Risikoergebnis, wenn Schalter aus
        if (!schalterGwbTechnErgInUee) {
            rvEffektRisikoErg += gwbTechnErgBestand;
        }

        // Verrechnung der RV-Gebuehr im Risikoergebnis
        if (schalterErgebnisTopfRvGebuehr == 3) {
            rvEffektRisikoErg -= gebuehrRvBestand;
        }

        if (Math.abs(rvEffektRisikoErg) < 0.001) {
            return 0.0d;
        } else {
            return rvEffektRisikoErg;
        }
    }

    /**
     * Berechnet den Effekt der Rückversicherung auf das Übriges Ergebnis <br/>
     * Option zur Verrechnung der Gewinnbeteiligung auf das Technische Ergebnis im Übrigen Ergebnis
     * Excel-Funktion: rvEffektUee()
     * @param zedUee
     *      Zediertes Übriges Ergebnis
     * @param gwbZedUee
     *      Gewinnbeteiligung auf das Übrige Ergebnis
     * @param komUee
     *      Kommission, verrechnet im Übrigen Ergebnis 
     * @param schalterGwbTechnErgInUee
     *      Schalter, ob Gewinnbeteiligung am Technischen Ergebnis im Übrigen Ergebnis verrechnet wird. <br/>0: nein, 1: ja
     * @param gwbTechnErg
     *      Gewinnbeteiligung am Technischen Ergebnis
     * @param gebuehrRvBestand
     *      Rückversicherungsgebühr, betrachteter Bestand
     * @param schalterErgebnisTopfRvGebuehr
     *      Schalter: In welchem Ergebnistopf wird die RV-Gebühr verrechnet?
     * @return der Wert
     */
    public static double rvEffektUee(double zedUee, double gwbZedUee, double komUee, boolean schalterGwbTechnErgInUee,
            double gwbTechnErg, double gebuehrRvBestand, int schalterErgebnisTopfRvGebuehr) {

        double rvEffektUeErg = -zedUee + gwbZedUee + komUee;

        if (schalterGwbTechnErgInUee) { // Gewinnbeteiligung auf Technisches Ergebnis fliesst in Übriges Ergebnis
            rvEffektUeErg += gwbTechnErg;
        }
        if (schalterErgebnisTopfRvGebuehr == 2) { // RV-Gebühr wird im Übrigen Ergebnis verrechnet
            rvEffektUeErg -= gebuehrRvBestand;
        }

        if (Math.abs(rvEffektUeErg) < 0.001) {
            return 0.0d;
        } else {
            return rvEffektUeErg;
        }
    }

    /**
     * Berechnet die rechnungsmäßigen Zinsen rmZ_gesamt inklusive Rueckversicherung
     * <br>
     * Excel-Funktion: rmZ_Gesamt_RV
     * @param rmZtarif
     *      tariflicher Rechnungszins
     * @param deltaZZR
     *      Delta ZZR
     * @param rvEffektRmz
     *      Effekt der Rückversicherung auf den rechnungsmäßigen Zins
     * @param schalterQuotenRv
     *      Schalter fuer Quoten-Rueckversicherung
     * @return der Wert
     */
    public static double rmzGesamtMitRv(double rmZtarif, double deltaZZR, double rvEffektRmz,
            boolean schalterQuotenRv) {

        double rmzGesamtRv = rmZtarif + deltaZZR;

        if (schalterQuotenRv) {
            rmzGesamtRv += rvEffektRmz;
        }

        return rmzGesamtRv;
    }

    /**
     * Berechnet im Fall eines Nachrangereignisses einen Zusatzertrag für den betrachteten Bestand. <br/> 
     * Trigger: Negativer Rohüberschuss ausgelöst durch die Rückzahlung an den RV. Aufteilung nach zedDeltaZzr und kumuliertem Zusatzertrag. <br/> 
     * Excel-Funktion: Zusatzertrag
     * @param t 
     *      Zeit
     * @param lfzQuotenRv 
     *      Laufzeit der Quoten-RV
     * @param zedDeltaZzrBestand
     *      Zedierte Delta ZZR (gekappt) des betrachteten Bestands
     * @param zeVorjahrBestand
     *      Kumulierter Zusatzertrag im betrachteten Bestand, Vorjahr
     * @param zedDeltaZzrAlt
     *      Zedierte Delta ZZR (gekappt) Altbestand
     * @param zeVorjahrAlt
     *      Kumulierter Zusatzertrag im Altbestand, Vorjahr
     * @param zedDeltaZzrNeu
     *      Zedierte Delta ZZR (gekappt) Neubestand
     * @param zeVorjahrNeu
     *      Kumulierter Zusatzertrag im Neubestand, Vorjahr
     * @param rvSaldoVorl
     *      Saldo, vorläufig
     * @param rohuebMitRvVorl
     *      Rohüberschuss mit Rückversicherung, vorläufig
     * @return der Wert
     */
    public static double nachrangZusatzertrag(int t, int lfzQuotenRv, double zedDeltaZzrBestand,
            double zeVorjahrBestand, double zedDeltaZzrAlt, double zeVorjahrAlt, double zedDeltaZzrNeu,
            double zeVorjahrNeu, double rvSaldoVorl, double rohuebMitRvVorl, boolean schalterZzrNachrangEreignis) {

        double Zusatzertrag = 0.0;
        double DeltaZZR_ZE_gesamt;
        double DeltaZZR_ZE_Bestand;
        double DeltaZZR_ZE_alt;
        double DeltaZZR_ZE_neu;

        if (schalterZzrNachrangEreignis && rohuebMitRvVorl < 0 && rvSaldoVorl < 0 && t <= lfzQuotenRv) { // Rohüberschuss mit RV wäre negativ und in ZZR-Abbauphase

            // 1. Bestimmung der Höhe des gesamten Zusatzertrags
            if (rvSaldoVorl > rohuebMitRvVorl) { // Komplette Aussetzung in Höhe von rvSaldoVorl
                Zusatzertrag = -rvSaldoVorl;
            } else {// Teilaussetzung in Höhe von rohuebMitRvVorl
                Zusatzertrag = -rohuebMitRvVorl;
            }

            // 2. Aufteilung des Zusatzertrags
            DeltaZZR_ZE_Bestand = zedDeltaZzrBestand - zeVorjahrBestand;
            DeltaZZR_ZE_alt = zedDeltaZzrAlt - zeVorjahrAlt;
            DeltaZZR_ZE_neu = zedDeltaZzrNeu - zeVorjahrNeu;
            DeltaZZR_ZE_gesamt = DeltaZZR_ZE_alt + DeltaZZR_ZE_neu;

            // Aufteilung des Zusatzertrags auf Neu- und Altbestand
            if (DeltaZZR_ZE_alt < 0 && DeltaZZR_ZE_neu < 0) { // Zusatzertrag wird quotal nach zedDeltaZzr und Zusatzertrag aufgeteilt
                Zusatzertrag = DeltaZZR_ZE_Bestand / DeltaZZR_ZE_gesamt * Zusatzertrag;
            } else if (DeltaZZR_ZE_Bestand >= 0) { // Betrachter Bestand ist positiv -> kein Zusatzertrag für diesen
                Zusatzertrag = 0.0;
            }
        }
        return Zusatzertrag;
    }

    /**
     * Berechnet den Zusatzertrag als Delta des kumulierten Zusatzertrags, der sich aus Nachrangereignissen ergibt.</br>
     * @param t
     *      aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit Quoten-RV
     * @param kumZusatzertrag
     *      Kumulierter Zusatzertrag durch (teil)ausgesetzte Zahlungen
     * @param kumZusatzertragVorjahr
     *      Kumulierter Zusatzertrag durch (teil)ausgesetzte Zahlungen, Vorjahreswert
     * @return der Wert
     */
    public static double nachrangSaldoCash(int t, int lfzQuotenRv, double kumZusatzertrag,
            double kumZusatzertragVorjahr) {

        // Keine gesonderte Behandlung von t = lfzQuotenRv:
        // Falls zur max. Vertragslaufzeit noch ausgesetzte Zahlungen vorhanden sind, steht gegen diese Verbindlichkeit 
        // gegenüber dem RV die Nachrangigkeit, die einen Cash Effekt gleicher Höhe generiert -> In Summe Null

        if (t <= lfzQuotenRv) {
            return kumZusatzertrag - kumZusatzertragVorjahr;
        } else {
            return 0.0d;
        }
    }

}
