package com.munichre.bsmrv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.EmptyLineException;
import de.gdv.bsm.intern.csv.LineFormatException;

public class MrParameterZeitabhaengig {

    private static final boolean HAVE_SECOND_CAPTION_LINE = true;
    private final List<MrParameterZeitabhaengigZeile> zeilen = new ArrayList<>();

    // maps LoB -> Zins -> Alt-/Neubestand -> Zeit-Array
    private final Map<String, Map<Integer, Map<String, List<MrParameterZeitabhaengigZeile>>>> datenMrParZeitAbh = new HashMap<>();

    /**
     * Erzeuge die Datenstruktur aus einer csv-Datei.
     * 
     * @param dataFile Name der csv-Datei
     * @throws IOException bei Ein-/Ausgabefehlern
     * @throws LineFormatException bei Formatfehlern in der Datei
     */
    public MrParameterZeitabhaengig(final File dataFile) throws IOException, LineFormatException {

        // temporäre Map:
        final Map<String, Map<Integer, Map<String, List<MrParameterZeitabhaengigZeile>>>> map = new HashMap<>();

        try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
            // Titelzeile überlesen
            csv.readLine();
            if (HAVE_SECOND_CAPTION_LINE) {
                csv.readLine();
            }

            CsvZeile zeile;
            while ((zeile = csv.readLine()) != null) {
                MrParameterZeitabhaengigZeile z;
                try {
                    z = new MrParameterZeitabhaengigZeile(zeile);

                    if (!map.containsKey(z.getLob())) {
                        map.put(z.getLob(), new HashMap<Integer, Map<String, List<MrParameterZeitabhaengigZeile>>>());
                    }

                    final Map<Integer, Map<String, List<MrParameterZeitabhaengigZeile>>> zinsMap = map.get(z.getLob());

                    if (!zinsMap.containsKey(z.getZinsGeneration())) {
                        zinsMap.put(z.getZinsGeneration(), new HashMap<String, List<MrParameterZeitabhaengigZeile>>());
                    }
                    final Map<String, List<MrParameterZeitabhaengigZeile>> altNeuMap = zinsMap.get(z
                            .getZinsGeneration());
                    if (!altNeuMap.containsKey(z.getAltNeuBestand())) {
                        altNeuMap.put(z.getAltNeuBestand(), new ArrayList<MrParameterZeitabhaengigZeile>());
                    }

                    final List<MrParameterZeitabhaengigZeile> list = altNeuMap.get(z.getAltNeuBestand());
                    if (z.getZeit() != list.size())
                        throw new IllegalStateException("Zeit nicht fortlaufend!");
                    list.add(z);
                    zeilen.add(z);

                } catch (EmptyLineException e) {
                    // leerzeilen in der csv-Datei werden ignoriert.
                }
            }
        }
        // mache die Map nicht modifizierbar:
        for (String lob : map.keySet()) {
            final Map<Integer, Map<String, List<MrParameterZeitabhaengigZeile>>> zinsMap = map.get(lob);
            for (int zins : zinsMap.keySet()) {
                final Map<String, List<MrParameterZeitabhaengigZeile>> altNeuMap = zinsMap.get(zins);
                for (String altNeu : altNeuMap.keySet()) {
                    altNeuMap.put(altNeu, Collections.unmodifiableList(altNeuMap.get(altNeu)));
                }
                zinsMap.put(zins, Collections.unmodifiableMap(altNeuMap));
            }
            datenMrParZeitAbh.put(lob, Collections.unmodifiableMap(map.get(lob)));
        }

    }

    /**
     * Ermittle die zeitabhängigen RV-Parameter für die angegebene Zeit.
     * 
     * @param zeit
     *            die Zeit
     * @return die RV-Parameter
     */
    public MrParameterZeitabhaengigZeile get(final int zeit) {
        return zeilen.get(zeit - 1);
    }

    /**
     * Liefert die Map mit den Daten. Schlüssel sind die Zinsgeneration und Alt/Neubestand.
     * 
     * @param lob der gewünschten Parameter.
     * @return die Map mit den Daten
     */
    public Map<Integer, Map<String, List<MrParameterZeitabhaengigZeile>>> getMrParZeitAbh(final String lob) {
        return datenMrParZeitAbh.get(lob);
    }
}
