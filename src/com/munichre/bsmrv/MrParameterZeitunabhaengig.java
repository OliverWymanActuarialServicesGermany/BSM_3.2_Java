package com.munichre.bsmrv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;

public class MrParameterZeitunabhaengig {

    private static final int COLUMN_WITH_DATA = 1;
    private static final int NUMBER_OF_LINES_TO_READ = 20; // Anzahl Zeilen mit Parametern
    private static final Set<Integer> LINES_TO_IGNORE = new HashSet<>(Arrays.asList(1, 4, 13, 14)); // Zwischenüberschriften

    // Parameter zur Quotenrückversicherung
    public final boolean schalterQuotenRv; // Feld B2
    public final int rvQuoteLaufzeit; // Feld B3

    // Parameter zu ZZR
    public final boolean schalterRvZzr; // Feld B5
    public final int erstesVertragsjahrZzr; // Feld B6
    public final int letztesVertragsjahrZzr; // Feld B7
    public final boolean schalterZzrWdrAnstieg; // Feld B8
    public final double capZedZzr; // Feld B9
    public final double startwertZedZzrAlt; // Feld B10
    public final double startwertZedZzrNeu; // Feld B11
    public final boolean schalterZzrNachrangEreignis; // Feld B12

    // Sonstige Parameter
    public final boolean schalterRvEffektRmZinsNonCash; // Feld B15
    public final boolean schalterZzrEffektNonCash; // Feld B16
    public final boolean schalterZusatzertragNonCash; // Feld B17
    public final boolean schalterRvEffektRisikoergebnisNonCash; // Feld B18
    public final boolean schalterRvEffektUebrigesergebnisNonCash; // Feld B19
    public final boolean schalterErgebnisTopfGwbTechnErg; // Feld B20 (0 - Risikoergebnis, 1 - übriges Ergebnis)
    public final double gwbTechnErgSatz; // Feld B21
    public final double lfdGebuehrZedZzr; // Feld B22
    public final int schalterErgebnisTopfRvGebuehr; // Feld B23
    public final double capBiometrie; // Feld B24
    public final double ausfallRv; // Feld B25

    public MrParameterZeitunabhaengig(final File dataFile) throws IOException, LineFormatException {

        try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
            CsvZeile line;
            int lineIndex = 0;
            final List<String> parameters = new ArrayList<>();
            while ((line = csv.readLine()) != null) {
                lineIndex++;

                // ignore certain captions in the middle
                if (LINES_TO_IGNORE.contains(lineIndex)) {
                    continue;
                }

                parameters.add(line.getString(COLUMN_WITH_DATA));
            }

            if (parameters.size() < NUMBER_OF_LINES_TO_READ) {
                throw new IllegalArgumentException(String.format("expected %d lines with parameters, but found only %d",
                        NUMBER_OF_LINES_TO_READ, parameters.size()));
            }

            int idx = -1;

            //Auslesen der Parameter zu Quotenrückversicherung
            schalterQuotenRv = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            rvQuoteLaufzeit = MrParseUtils.parseInteger(parameters.get(++idx));

            // Auslesen der Parameter zu ZZR
            schalterRvZzr = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            erstesVertragsjahrZzr = MrParseUtils.parseInteger(parameters.get(++idx));
            letztesVertragsjahrZzr = MrParseUtils.parseInteger(parameters.get(++idx));
            schalterZzrWdrAnstieg = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            capZedZzr = MrParseUtils.parseDouble(parameters.get(++idx));
            startwertZedZzrAlt = MrParseUtils.parseDouble(parameters.get(++idx));
            startwertZedZzrNeu = MrParseUtils.parseDouble(parameters.get(++idx));
            schalterZzrNachrangEreignis = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));

            //Auslesen der sonstigen Parameter
            schalterRvEffektRmZinsNonCash = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            schalterZzrEffektNonCash = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            schalterZusatzertragNonCash = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            schalterRvEffektRisikoergebnisNonCash = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            schalterRvEffektUebrigesergebnisNonCash = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            schalterErgebnisTopfGwbTechnErg = MrParseUtils.parseBooleanFromInteger(parameters.get(++idx));
            gwbTechnErgSatz = MrParseUtils.parseDouble(parameters.get(++idx));
            lfdGebuehrZedZzr = MrParseUtils.parseDouble(parameters.get(++idx));
            schalterErgebnisTopfRvGebuehr = MrParseUtils.parseInteger(parameters.get(++idx));
            capBiometrie = MrParseUtils.parseDouble(parameters.get(++idx));
            ausfallRv = MrParseUtils.parseDouble(parameters.get(++idx));
        }
    }

    /**
     * Schalter Quotenrückversicherung (0: nein, 1: ja). Feld B2.
     * @return der Wert
     */
    public boolean getSchalterQuotenRv() {
        return schalterQuotenRv;
    }

    /**
     * Laufzeit der Quotenrückversicherung. Feld B3.
     * @return der Wert
     */
    public int getQuotenRvLaufzeit() {
        return rvQuoteLaufzeit;
    }

    /**
     * Schalter ZZR Rückversicherung (0: nein, 1: ja). Feld B5.
     * @return der Wert
     */
    public boolean getSchalterZzrRv() {
        return schalterRvZzr;
    }

    /**
     * Erstes Vertragsjahr ZZR Rückversicherung. Feld B6.
     * @return der Wert
     */
    public int getZzrRvErstesVertragsjahr() {
        return erstesVertragsjahrZzr;
    }

    /**
     * Letztes Vertragsjahr ZZR Rückversicherung. Feld B7.
     * @return der Wert
     */
    public int getZzrRvLetztesVertragsjahr() {
        return letztesVertragsjahrZzr;
    }

    /**
     * Schalter, ob Wiederanstieg der zedierten ZZR möglich ist. Feld B8.
     * @return der Wert
     */
    public boolean getSchalterZzrWdrAnstieg() {
        return schalterZzrWdrAnstieg;
    }

    /**
     * Cap-Wert der zedierten ZZR. Feld B9.
     * @return der Wert
     */
    public double getCapZedZzr() {
        return capZedZzr;
    }

    /**
     * Startwert der zedierten ZZR, Altbestand. Feld B10.
     * @return der Wert
     */
    public double getStartwertZedZzrAlt() {
        return startwertZedZzrAlt;
    }

    /**
     * Startwert der zedierten ZZR, Neubestand. Feld B11.
     * @return der Wert
     */
    public double getStartwertZedZzrNeu() {
        return startwertZedZzrNeu;
    }

    /**
     * Schalter, ob Nachrangereignisse auftreten können. Feld B12.
     * @return der Wert
     */
    public boolean getSchalterZzrNachrangEreignis() {
        return schalterZzrNachrangEreignis;
    }

    /**
     * RV rm Zins (0 - Cash, 1 - Non-Cash). Feld B15.
     * @return der Wert
     */
    public boolean getSchalterRvEffektRmZinsNonCash() {
        return schalterRvEffektRmZinsNonCash;
    }

    /**
     * RV Effekt Risikoergebnis (0: Cash, 1: Non-Cash). Feld B16.
     * @return der Wert
     */
    public boolean getSchalterRvEffektRisikoergebnisNonCash() {
        return schalterRvEffektRisikoergebnisNonCash;
    }

    /**
     * RV Effekt der ZZR Rückversicherung (0: Cash, 1: Non-Cash). Feld B17.
     * @return der Wert
     */
    public boolean getSchalterZzrEffektNonCash() {
        return schalterZzrEffektNonCash;
    }

    /**
     * RV Effekt der ZZR Rückversicherung (0: Cash, 1: Non-Cash). Feld B18.
     * @return der Wert
     */
    public boolean getSchalterZusatzertragNonCash() {
        return schalterZusatzertragNonCash;
    }

    /**
     * RV Effekt übriges Ergebnis (0: Cash, 1: Non-Cash). Feld B19.
     * @return der Wert
     */
    public boolean getSchalterRvEffektUebrigesergebnisNonCash() {
        return schalterRvEffektUebrigesergebnisNonCash;
    }

    /**
     * Ergebnistopf Gewinnbeteiligung am Technischen Ergebnis (0: Risikoergebnis, 1: übriges Ergebnis). Feld B20.
     * @return der Wert
     */
    public boolean getSchalterErgebnisTopfGwbTechnErg() {
        return schalterErgebnisTopfGwbTechnErg;
    }

    /**
     * Gewinnbeteiligung (%) auf das Technische Ergebnis. Feld B21.
     * @return der Wert
     */
    public double getGwbTechnErgSatz() {
        return gwbTechnErgSatz;
    }

    /**
     * Laufende RV-Gebühr auf die zedierte ZZR (bp). Feld B22.
     * @return der Wert
     */
    public double getLfdGebuehrZedZzr() {
        return lfdGebuehrZedZzr;
    }

    /**
     * Schalter Verrechnung der RV-Gebühr in 1: Kapitalanlageergebnis, 2: Übriges Ergebnis, 3: Risikoergebnis. Feld B23.
     * @return der Wert
     */
    public int getschalterErgebnisTopfRvGebuehr() {
        return schalterErgebnisTopfRvGebuehr;
    }

    /**
     * Cap des zedierten biometrischen Ergebnisses (max. LCF, positiver Wert). Feld B24.
     * @return der Wert
     */
    public double getCapBiometrie() {
        return capBiometrie;
    }

    /**
     * Einjährige Ausfallwahrscheinlichkeit des Rückversicherers. Feld B25.
     * @return der Wert
     */
    public double getAusfallRv() {
        return ausfallRv;
    }

}
