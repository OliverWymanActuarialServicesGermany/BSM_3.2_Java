package com.munichre.bsmrv;

import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

public class MrParameterZeitabhaengigZeile {

    private final String lob;
    private final int zeit;
    private final int zinsGeneration;
    private final String altNeubestand;

    public final double rvQuote; // Spalte E
    public final double zzrQuote; // Spalte F
    public final double gwbsatzUebrigesergebnis; // Spalte G
    public final double kommissionRvUebrigesrgebnisAbsolut; // Spalte H
    public final double kommissionRvKapitalanlageergebnisAbsolut; // Spalte I
    public final double gebuehrRvAbsolut; // Spalte J

    /**
     * Erstelle die Daten aus einer aufbereiteten Zeile der csv-Datei.
     * 
     * @param zeile die Zeile der csv-Datei
     * @throws LineFormatException bei Formatfehlern
     */
    public MrParameterZeitabhaengigZeile(final CsvZeile zeile) throws LineFormatException, EmptyLineException {
        lob = zeile.getString(0);
        zeit = zeile.getInt(1);
        zinsGeneration = zeile.getInt(2);
        altNeubestand = zeile.getString(3);

        int idx = 3;
        this.rvQuote = MrParseUtils.parseDouble(zeile.getString(++idx));
        this.zzrQuote = MrParseUtils.parseDouble(zeile.getString(++idx));
        this.gwbsatzUebrigesergebnis = MrParseUtils.parseDouble(zeile.getString(++idx));
        this.kommissionRvUebrigesrgebnisAbsolut = MrParseUtils.parseDouble(zeile.getString(++idx));
        this.kommissionRvKapitalanlageergebnisAbsolut = MrParseUtils.parseDouble(zeile.getString(++idx));
        this.gebuehrRvAbsolut = MrParseUtils.parseDouble(zeile.getString(++idx));
    }

    /**
     * @return the lob
     */
    public String getLob() {
        return lob;
    }

    /**
     * @return the zeit
     */
    public int getZeit() {
        return zeit;
    }

    /**
     * @return the zinsGeneration
     */
    public int getZinsGeneration() {
        return zinsGeneration;
    }

    /**
     * @return the altNeubestand
     */
    public String getAltNeuBestand() {
        return altNeubestand;
    }

    /**
     * @return the rvQuote
     */
    public double getRvQuote() {
        return rvQuote;
    }

    /**
     * @return the zzrQuote
     */
    public double getZzrQuote() {
        return zzrQuote;
    }

    /**
     * @return the gwbsatzRisikoergebnis
     */
    public double getGwbsatzUebrigesergebnis() {
        return gwbsatzUebrigesergebnis;
    }

    /**
     * @return the kommissionRvUebrigesrgebnisAbsolut
     */
    public double getKommissionRvUebrigesrgebnisAbsolut() {
        return kommissionRvUebrigesrgebnisAbsolut;
    }

    /**
     * @return the kommissionRvKapitalanlageergebnisAbsolut
     */
    public double getKommissionRvKapitalanlageergebnisAbsolut() {
        return kommissionRvKapitalanlageergebnisAbsolut;
    }

    /**
     * @return the gebuehrRvAbsolut
     */
    public double getgebuehrRvAbsolut() {
        return gebuehrRvAbsolut;
    }
}
