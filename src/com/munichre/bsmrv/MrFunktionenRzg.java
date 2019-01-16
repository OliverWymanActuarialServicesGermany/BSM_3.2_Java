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

public class MrFunktionenRzg {

    private MrFunktionenRzg() {
        throw new IllegalStateException(String.format("%s is not meant to be instantiated", this.getClass().getName()));
    }

    /**
     * Berechnet die RV-Quote zum Zeitpunkt t.</br>
     * Automatische Vertragsbeendigung (Quote = 0), wenn keine Finanzierung eines Wiederanstiegs der ZZR nach vollständiger Rückzahlung möglich 
     * und keine ausgesetzen Zahlungen bestehen.
     * @param t
     *      Aktueller Zeitpunkt
     * @param rvQuoteTab
     *      Tabellierte RV-Quote
     * @param zedZzrVj
     *      Zedierte ZZR, Vorjahr
     * @param kumZeVj
     *      Kumulierter Zusatzertrag (ausgesetzte Zahlungen), Vorjahr
     * @param schalterZzrWdrAnstieg
     *      Schalter, ob Wiederanstieg der zedierten ZZR nach vollständigem Abbau möglich ist
     * @return
     */
    public static double rvQuote(int t, boolean schalterQuotenRv, int lfzQuotenRv, double rvQuoteTab, double zedZzrVj,
            double kumZeVj, boolean schalterZzrWdrAnstieg) {

        double rvQuote = 0.0d;

        if (schalterQuotenRv && t <= lfzQuotenRv) {
            if (t != 1 && schalterZzrWdrAnstieg == false && zedZzrVj < 0.01 && kumZeVj < 0.01) {
                rvQuote = 0.0d;
            } else {
                rvQuote = rvQuoteTab;
            }
        }
        return rvQuote;
    }

    /**
     * Berechnet das Risiko-Ergebnis der Rückversicherung für die Rechnungszinsgeneration rz zum Zeitpunkt t <br/>
     * Excel-Funktion: zedRisikoergebnis()
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-RV
     * @param Risiko_Ergebnis
     *      Zediertes Risikoergebnis
     * @param rvQuote
     *      RV-Quote
     * @param schalterQuotenRv
     *     Schalter für Quoten-RV 
     * @return der Wert
     */
    public static double zedRisikoergebnis(int t, int lfzQuotenRv, double Risiko_Ergebnis, double rvQuote,
            boolean schalterQuotenRv) {
        if (schalterQuotenRv && t <= lfzQuotenRv) {
            return Risiko_Ergebnis * rvQuote;
        }

        return 0.0d;
    }

    /**
     * Berechnet das Übrige Ergebnis der Rückversicherung für die Rechnungszinsgeneration rz zum Zeitpunkt t<br/>
     * Excel-Funktion: zedKostenueberschuesse<br/>
     * <b>Momentan keine Beteiligung an den Kosten, deswegen hart auf null gesetzt<b>
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit Quoten-RV
     * @param Kostenergebnis
     *       Kostenüberschüsse, stochastisch
     * @param rvQuote
     *      RV-Quote
     * @param schalterQuotenRv
     *      Schalter für Quoten-RV 
     * @return der Wert
     */
    public static double zedKostenueberschuesse(int t, int lfzQuotenRv, double Kostenergebnis, double rvQuote,
            boolean schalterQuotenRv) {

        if (schalterQuotenRv && t <= lfzQuotenRv) {

            // momentan keine Beteiligung an den Kosten, deswegen hart auf null gesetzt 
            return Kostenergebnis * 0.0d;
        }

        return 0.0d;

    }

    /**
     * Berechnet das Gewinnbeteiligung aus dem Uebrigen-Ergebnis der Rueckversicherung für die Rechnungszinsgeneration rz zum Zeitpunkt t <br/>
     * Excel-Funktion: gwbUebrigesErg()
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-RV
     * @param zedKostenueberschuesse
     *      Zedierte Kostenüberschüsse für die Rechnungszinsgeneration rz zum Zeitpunkt t
     * @param gwbSatzKostenueberschuesse
     *      Gewinnbeteilungssatz für Gwb am Übrigen Ergebnis der Rückversicherung (zedierte Kostenüberschüsse)
     * @param schalterQuotenRv
     *      Schalter für Quoten-RV
     * @return der Wert
     */
    public static double gwbUebrigesErg(int t, int lfzQuotenRv, double zedKostenueberschuesse,
            double gwbSatzKostenueberschuesse, boolean schalterQuotenRv) {

        if (schalterQuotenRv && t <= lfzQuotenRv) {
            return gwbSatzKostenueberschuesse * Math.max(zedKostenueberschuesse, 0);
        }
        return 0.0d;
    }

    /**
     * Berechnet die Kommission des Übrigen Ergebnisses für die Rechnungszinsgeneration rz zum Zeitpunkt t <br/>
     * Excel-Funktion: komUebrigesErg()
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-Rueckversicherung.
     * @param komUebrigesErgAbsolut
     *      Absoluter Teil der Kommission des Uebrigen Ergebnisses
     * @param schalterQuotenRv
     *       Schalter für Quoten-RV
     * @return der Wert
     */
    public static double komUebrigesErg(int t, int lfzQuotenRv, double komUebrigesErgAbsolut,
            boolean schalterQuotenRv) {

        if (schalterQuotenRv && t <= lfzQuotenRv) {
            return komUebrigesErgAbsolut;
        } else {
            return 0.0d;
        }
    }

    /**
     * Berechnet die Kommission des Kapitalanlageergebnisses für die Rechnungszinsgeneration rz zum Zeitpunkt t <br/>
     * Excel-Funktion: komKapErg()
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-Rueckversicherung.
     * @param komKapErgAbsolut
     *      Absoluter Teil der Kommission des Kapitalanlageergebnisses
     * @param schalterQuotenRv
     *      Schalter für Quoten-RV
     * @return der Wert
     */
    public static double komKapErg(int t, int lfzQuotenRv, double komKapErgAbsolut, boolean schalterQuotenRv) {

        if (schalterQuotenRv && t <= lfzQuotenRv) {
            return komKapErgAbsolut;
        }

        return 0.0d;
    }

    /**
     * Berechnet zum Zeitpunkt t den rechnungsmaessigen Zins für die Rechnungszinsgeneration rz zum Zeitpunkt t <br/>
     * Funktionsname in Excel: rmz_RV
     * @param t 
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-Rueckversicherung
     * @param rz
     *      Rechnungszinsgeneration (in bp)
     * @param beDrst
     *      HGB DRSt des Best Estimate Szenarios
     * @param schalterQuotenRv
     *      Schalter fuer Rueckversicherungs Customcode
     * @param rvQuote
     * @return Zedierter Rechnungsmaessiger Zins
     */
    public static double rmz_RV(int t, int lfzQuotenRv, double rz, double beDrst, boolean schalterQuotenRv,
            double rvQuote) {
        if (schalterQuotenRv && t <= lfzQuotenRv) {
            return rvQuote * beDrst * rz / 10000.0;
        }

        return 0.0d;
    }

    /**
     * Berechnet zum Zeitpunkt t den Depotzinssatz <br/>
     * Excel-Funktion: Depotzinssatz_RV
     * @param t
     *      Aktueller Zeitpunkt
     * @param RZG
     *      Rechnungszinsgeneration (in bp)
     * @param lfzQuotenRv
     *      Laufzeit der Quoten-RV
     * @param schalterQuotenRv
     *      Schalter für Quoten-RV
     * @return der Wert
     */
    public static double depotzinssatzRv(int t, double RZG, int lfzQuotenRv, boolean schalterQuotenRv) {

        // Dieser Sonderfall muss abgefangen werden. Er tritt auf, wenn das BSM die Formeln erweitert.
        if (t == 0) {
            return 0.0d;
        }

        if (!schalterQuotenRv || t > lfzQuotenRv) {
            return 0.0d;
        } else {
            return RZG / 10000.0;
        }

    }

    /**
     * Berechnet zum Zeitpunkt t den Depotzins <br/>
     * Excel-Funktion: Depotzins_RV
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Laufzeit Quoten-RV
     * @param Depotzinssatz_RV
     *      Depotzinssatz (in bp)
     * @param rvQuote
     *      RV-Quote
     * @param HGB_DRSt
     *      Aktuelle HGB Deckungsrueckstellung in t-1
     * @param schalterQuotenRv
     *      Schalter für Quoten-RV
     * @return
     */
    public static double depotzinsRv(int t, int lfzQuotenRv, double Depotzinssatz_RV, double rvQuote, double HGB_DRSt,
            boolean schalterQuotenRv) {
        if (!schalterQuotenRv || t > lfzQuotenRv) {
            //Wenn Schalter aus ist oder das Laufzeitende erreicht ist
            return 0.0d;
        } else {
            return Depotzinssatz_RV * HGB_DRSt * rvQuote;
        }

    }

    /**
     * Berechnet die zedierte ZZR Ende des Jahres zum Zeitpunkt t <br/>
     * @param zedZzrAdj
     *      Zedierte ZZR Anfang des Jahres
     * @param zedDeltaZzr
     *      Delta der zedierten ZZR
     * @return
     */
    public static double zedZzrEdj(double zedZzrAdj, double zedDeltaZzr) {

        double zedZzrEdj = zedZzrAdj + zedDeltaZzr;

        return (Math.abs(zedZzrEdj) < 0.001 ? 0.0d : zedZzrEdj);

    }

    /**
     * Berechnet das Delta der zedierten ZZR zum Zeitpunkt t (ohne Cap). <br/>
     * @param t
     *      Aktueller Zeitpunkt
     * @param schalterQuotenRv
     *      Schalter für Quoten-RV
     * @param schalterZzrRv
     *      Schalter für ZZR-RV
     * @param zzrRvErstesJahr
     *      RV der ZZR, erstes Jahr
     * @param zzrRvLetztesJahr
     *      RV der ZZR, letztes Jahr
     * @param schalterZzrWdrAnstieg
     *      Schalter, ob Wiederanstieg der zedierten ZZR nach vollständigem Abbau möglich ist
     * @param flagErsteZedZzrPhaseVj
     *      Indikator, ob sich die zedierte ZZR erstmalig auf- und abbaut, Vorjahr
     * @param deltaZzr
     *      Delta der gebuchten ZZR
     * @param zzrQuote
     *      Tabellierte Quote für RV der ZZR
     * @param zedZzrVj
     *      Zedierte ZZR (Outstanding / Schattenrechnung), Vorjahr
     * @return der Wert
     */
    public static double zedDeltaZzr(int t, boolean schalterQuotenRv, boolean schalterZzrRv, int zzrRvErstesJahr,
            int zzrRvLetztesJahr, boolean schalterZzrWdrAnstieg, boolean flagErsteZedZzrPhaseVj, double deltaZzr,
            double zzrQuote, double zedZzrVj) {

        double zedDeltaZzr;

        if (schalterQuotenRv && schalterZzrRv && t >= zzrRvErstesJahr && t <= zzrRvLetztesJahr) {
            zedDeltaZzr = deltaZzr * zzrQuote;

            if (schalterZzrWdrAnstieg) { // Wiederanstieg möglich
                // Keine Beteiligung, wenn Outstanding zurückgeführt und ZZR im Abbau
                if (t > zzrRvErstesJahr && zedZzrVj < 0.001 && deltaZzr < 0.001) {
                    zedDeltaZzr = 0.0;
                }
            } else { // Kein Wiederanstieg möglich
                // Keine Beteiligung, wenn Outstanding zurückgeführt und ZZR im Abbau oder der erste Auf/Abbau der zed. ZZR vorüber ist
                if ((t > zzrRvErstesJahr && zedZzrVj < 0.001 && deltaZzr < 0.001) || !flagErsteZedZzrPhaseVj) {
                    zedDeltaZzr = 0.0;
                }
            }
        } else {
            zedDeltaZzr = 0.0;
        }

        return (Math.abs(zedDeltaZzr) < 0.001 ? 0.0d : zedDeltaZzr);
    }

    /**
     * Gibt tabellierte absolute RV-Gebühr zurück, solange RV-Quote > 0.
     * @param t
     *      Aktueller Zeitpunkt
     * @param lfzQuotenRv
     *      Maximale Laufzeit der Quote-RV
     * @param rvQuote
     *      Berechnete RV-Quote im aktuellen Zeitpunkt
     * @param rvGebuehrAbsolut
     *      Tabellierte absolute RV-Gebühr
     * @return der Wert
     */
    public static double rvGebuehrAbsolut(int t, int lfzQuotenRv, double rvQuote, double rvGebuehrAbsolut) {

        if (rvQuote < 0.001 || t > lfzQuotenRv) { // tabellarische Gebühr auf Null, wenn Vertrag beendet
            return 0.0d;
        } else {
            return rvGebuehrAbsolut;
        }
    }

}
