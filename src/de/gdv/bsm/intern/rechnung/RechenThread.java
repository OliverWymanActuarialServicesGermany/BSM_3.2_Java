package de.gdv.bsm.intern.rechnung;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import com.munichre.bsmrv.MrVuParameter;
import de.gdv.bsm.intern.applic.AusgabThreadMittelwerte;
import de.gdv.bsm.intern.applic.AusgabeThread;
import de.gdv.bsm.intern.applic.AusgabeThreadTableField;
import de.gdv.bsm.intern.applic.BerechnungResultat;
import de.gdv.bsm.intern.applic.RechenFortschrittInterface;
import de.gdv.bsm.intern.params.DynManReg;
import de.gdv.bsm.intern.params.Eingabe;
import de.gdv.bsm.intern.params.SzenarioMapping;
import de.gdv.bsm.intern.params.SzenarioMappingZeile;
import de.gdv.bsm.intern.params.VuParameter;
import de.gdv.bsm.intern.szenario.Szenario;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.Berechnung;
import de.gdv.bsm.vu.berechnung.FiAusfallZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;
//TODO: Auskommentieren / löschen: Datenbanknutzung
/*
import de.gdv.bsm.vu.db.BSMDB;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.io.BufferedOutputStream;
*/
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;
import de.gdv.bsm.vu.kennzahlen.MittelwerteNurCe;
import de.gdv.bsm.vu.kennzahlen.MittelwerteUndCe;
import de.gdv.bsm.vu.util.AuxiliaryFunctions;
import de.gdv.bsm.vu.util.BSMLog;

/**
 * Rechenkern für die komplette Ausführung von Berechnungen.
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
 */
public class RechenThread implements Runnable {

    private final RechenFortschrittInterface fortschritt;
	private final DynManReg dynManReg;
    private final Eingabe eingabe;
    // BSM MR IN <
    private final MrVuParameter vuParameter;
    // BSM MR IN >
    // alle zu berechnenden Pfade:
    private List<Integer> pfade = new ArrayList<>();
    // nächster zu berechnender Pfad:
    private int nextPfad = 0;
    // Alle asymchronen Berechnung-Threads
    private final Set<BerechnungThread> berechnungThreads = new HashSet<>();
    // Queue mit den Resultaten
    private final BlockingQueue<BerechnungReady> resultat = new LinkedBlockingQueue<>();
    // Queue mit den Ausgaben
    private final BlockingQueue<AusgabeThread> ausgaben = new LinkedBlockingQueue<>();
    // bereits berechnete Pfade:
    private final Set<Integer> berechnetePfade = new HashSet<>();
    // die gesammelten berechneten Kennzahlen
    private final TreeMap<Integer, TreeMap<Integer, KennzahlenPfadweise>> kennzahlenPfadweise = new TreeMap<>();
    // Deprecated private final TreeMap<Integer, TreeMap<Integer, List<KennzahlenPfadweiseLoB>>> kennzahlenPfadweiseLoB = new TreeMap<>();
    private final TreeMap<Integer, TreeMap<String, TreeMap<Integer, KennzahlenPfadweiseLoB>>> kennzahlenPfadweiseLoB = new TreeMap<>();
    private final List<Mittelwerte> mittelwerteList = new ArrayList<>();

    /**
     * Erstelle den Rechenkern.
     * 
     * @param fortschritt
     *            zur Fortschrittsanzeige
     * @param eingabe
     *            Vorgaben
     * @param vuParameter
     *            Parameter des VU
     * @param sznrCache
     *            Cache für die Zinskurven
     * @param baseDir
     *            Basispfad des Verzeichnisses
     */
    public RechenThread(final RechenFortschrittInterface fortschritt, final Eingabe eingabe,
            final MrVuParameter vuParameter, final DynManReg dynManReg) {
        this.fortschritt = fortschritt;
        this.eingabe = eingabe;
        this.vuParameter = vuParameter;
		this.dynManReg = dynManReg;
    }

    /**
     * Signalisiere eine durchgeführte Berechnung.
     * 
     * @param berechnungReady
     *            die Signalisierung
     */
    public void done(final BerechnungReady berechnungReady) {
        while (true) {
            try {
                resultat.put(berechnungReady);
                return;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void run() {
        // TODO: Auskommentieren / löschen: Datenbankinitialisierung
        // new BSMDB(true);

        final File ausgabeFile = new File(vuParameter.getTransferDir(), VuParameter.AUSGABE);
        try (final PrintStream ausgabe = new PrintStream(new FileOutputStream(ausgabeFile))) {
            // Startzeit der Simulation
            final Calendar start = new GregorianCalendar();
            final Map<Integer, List<String>> sznrHeader = new TreeMap<>();

            // erster Teil: Laden der benötigten Zinskurven

            final SzenarioMapping szenarioMapping = vuParameter.getSzenarioMapping();
            final Set<Integer> sznrAlleIdSet = new TreeSet<>();
            if (eingabe.isAlleSzenarien()) {
                for (SzenarioMappingZeile smz : szenarioMapping.getAktiveSzenarien()) {
                    sznrAlleIdSet.add(smz.getZinskurve());
                }
            } else {
                int sznrId = szenarioMapping.getSzenarionMapping(eingabe.getSzenario()).getZinskurve();
                sznrAlleIdSet.add(sznrId);
            }

            if (eingabe.getPfadVon() > eingabe.getPfadBis() && eingabe.getPfadBis() != 0) {
                throw new IllegalArgumentException("Pfad von größer als Pfad bis.");
            }
            // Pfad bis kann null sein, dann nur Pfad von rechnen:
            final int pfadBis = Math.max(eingabe.getPfadVon(), eingabe.getPfadBis());

            if (fortschritt.isAbbruch()) {
                // Benutzerabbruch:
                fortschritt.berechnungBeendet(null);
                return;
            }

            final List<SzenarioMappingZeile> szenarien = new ArrayList<>();
            if (eingabe.isAlleSzenarien()) {
                szenarien.addAll(szenarioMapping.getAktiveSzenarien());
            } else {
                szenarien.add(szenarioMapping.getSzenarionMapping(eingabe.getSzenario()));
            }

            // nacheinander alle Szenarien berechnen:
            int lastPercent = 0;
            final boolean addierePfad0;
            if (eingabe.getPfadVon() > 0) {
                // Pfad 0 muss immer berechnet werden (für CE-Werte)
                pfade.add(0);
                addierePfad0 = false;
            } else {
                addierePfad0 = true;
            }
            for (int pfad = eingabe.getPfadVon(); pfad <= pfadBis; ++pfad) {
                pfade.add(pfad);
            }

            Berechnung letzteBerechnung = null;

            ausgabe.println(
                    "Stressszenario ID;Stressszenarion;Modifizierte Duration Zinstitel-Portfolio;FI-Ausfall-Wahrscheinlichkeit");
            final DecimalFormat df = new DecimalFormat("#.##############################");

            Szenario szenario = null;
            int szenarioId = 0;

            for (SzenarioMappingZeile sz : szenarien) {
                boolean ausgabeGeschrieben = false;

                // Mittelwerte gesammelt zunächst für das aktuelle Szenario:
                final Map<String, Map<Integer, List<MittelwerteUndCe>>> mittelwerteUndCe = new HashMap<>();
                final Map<String, Map<Integer, List<MittelwerteNurCe>>> mittelwerteNurCe = new HashMap<>();

                fortschritt.setBerechnungPercent(sz.getId(), 0);
                nextPfad = 0;
                berechnungThreads.clear();
                berechnetePfade.clear();

                // setze parallele Threads auf, maximal einer weniger als Prozessoren
                // und zu berechnende Pfade
                final int threadCount = Math.max(1,
                        Math.min(Runtime.getRuntime().availableProcessors() - 1, pfade.size()));
                for (int i = 0; i < threadCount; ++i) {
                    if (szenarioId != sz.getZinskurve()) {
                        szenario = new Szenario(new File(eingabe.getPfadSzenariensatz()), sz.getZinskurve(), pfadBis,
                                fortschritt);

                        try {
                            //TODO: Es ist zu überlegen, ob die Konsistenzchecks im Vergleich zum Performanceverlust durch die Überprüfung als sinnvoll zu erachten sind. Ggf. Konsitenzchecks als separates Tool vor und unabhängig von der Berechnung wählbar machen
                            //Da zumeist auf max abgefragt wird, kann z.B. auch ein einzelner Zeitpunkt oder nur ein Zeitpunkt in einem Pfad fehlen.
                            // Korrektheit der Anzahl der Jahre und der Restlaufzeiten im Szenariensatz überprüfen
                            int maxRlz = szenario.getMaximaleRestlaufzeit();
                            //OW_F.Wellens
							int rlzNeuanlage = 0;
							final int sznr = szenarioId;
							if (eingabe.isOWRechnen() && dynManReg.FI_Neuanl_RLZ == true){
								rlzNeuanlage = vuParameter.getZeitabhManReg().getList().stream()
	                                    .mapToInt(e -> e.getRlzNeuAnl()[sznr]).max().getAsInt();
							} else {
								rlzNeuanlage = vuParameter.getZeitabhManReg().getList().stream()
	                                    .mapToInt(e -> e.getRlzNeuAnl()[0]).max().getAsInt();
							}
                            int k = vuParameter.getBwAktivaFi().getList().size() - 1;
                            for (k = vuParameter.getBwAktivaFi().getList().size() - 1; k >= 0; k--) {
                                if (vuParameter.getBwAktivaFi().getList().get(k).getCashflowFi() > 0) {
                                    break;
                                }
                            }
                            int rlzAnfangsbestand = k;
                            //long rlzAnfangsbestand=vuParameter.getBwAktivaFi().getList().stream().count();
                            int zeitpunkte_pro_pfad = szenario.getPfade().entrySet().stream()
                                    .flatMap(e -> e.getValue().getList().stream()).mapToInt(h -> h.getZeit()).max()
                                    .getAsInt();
                            int zeitpunkte_im_cf = vuParameter.getVtKlassik().getDatenListe()
                                    .get(sz.getProjektionVtKlassik()).stream().mapToInt(e -> e.getZeit()).max()
                                    .getAsInt();
                            if (zeitpunkte_pro_pfad < zeitpunkte_im_cf) {
                                BSMLog.writeLog("WARNUNG: Fehler in der Eingabe: Szenariensatz " + sz.getZinskurve()
                                        + " enthält nur " + zeitpunkte_pro_pfad + " Zeitpunkte pro Pfad, während "
                                        + zeitpunkte_im_cf + "benötigt werden.", "0: Warning/Error");
                            }
                            if (vuParameter.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation() < 2
                                    && maxRlz < Math.max(rlzAnfangsbestand, rlzNeuanlage)) {
                                BSMLog.writeLog("WARNUNG: Fehler in der Eingabe: Restlaufzeit " + maxRlz
                                        + " im Szenariensatz " + sz.getZinskurve() + " ist zu klein verglichen mit "
                                        + Math.max(rlzAnfangsbestand, rlzNeuanlage) + ".", "0: Warning/Error");
                            }
                            if (vuParameter.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation() == 2
                                    && maxRlz < Math.max(Math.max(rlzAnfangsbestand, rlzNeuanlage), zeitpunkte_im_cf)) {
                                BSMLog.writeLog("WARNUNG: Fehler in der Eingabe: Restlaufzeit " + maxRlz
                                        + " im Szenariensatz " + sz.getZinskurve() + " ist zu klein verglichen mit "
                                        + Math.max(Math.max(rlzAnfangsbestand, rlzNeuanlage), zeitpunkte_im_cf) + ".",
                                        "0: Warning/Error");
                            }
                        } catch (Exception e) {
                            BSMLog.writeLog("WARNUNG: Konsistenzchecks konnten für Szenario " + sz.getId()
                                    + " Szenariensatz " + sz.getZinskurve()
                                    + " nicht durchgeführt werden. Prüfen Sie die Eingabedaten. Grund: " + e
                                    + ". In Klasse: " + e.getStackTrace()[0].getClassName() + " In Zeile: "
                                    + e.getStackTrace()[0].getLineNumber());
                        }
                        szenarioId = sz.getZinskurve();
                        sznrHeader.put(sz.getZinskurve(), szenario.getHeader());
                    }
                    // Ergänze die Zusatzinfo: Verwendung antithetischer Variablen in der ScenarioMappingZeile sz
                    sz.setAntitethischeVariablen(szenario.antitethischeVariablen);

                    final Berechnung berechnung = new Berechnung(sz.getId(), eingabe.isFlvRechnen(),
                            eingabe.isNegAusfallwk(), eingabe.isAusgabe(), vuParameter, szenario, eingabe.isOWRechnen());

                    if (!ausgabeGeschrieben) {
                        ausgabe.println(
                                sz.getId() + ";" + sz.getName() + ";" + df.format(berechnung.getDurationKaBestand())
                                        + ";" + df.format(berechnung.getAusfallWahrscheinlichkeitQ()));
                        ausgabeGeschrieben = true;
                    }

                    final BerechnungThread berechnungThread = new BerechnungThread(berechnung, this);
                    berechnungThreads.add(berechnungThread);
                    new Thread(berechnungThread).start();
                }

                for (BerechnungThread bt : berechnungThreads) {
                    bt.berechne(pfade.get(nextPfad));
                    if (pfade.get(nextPfad) == pfadBis) {
                        letzteBerechnung = bt.getBerechnung();
                    }
                    ++nextPfad;
                }

                while (true) {
                    try {
                        final BerechnungReady br = resultat.take();
                        if (br.berechneterPfad.isPresent()) {
                            int pfad = br.berechneterPfad.get();
                            if (berechnetePfade.contains(pfad)) {
                                throw new IllegalStateException("Pfad doppelt berechnet: " + pfad);
                            }
                            berechnetePfade.add(pfad);

                            if (!kennzahlenPfadweise.containsKey(sz.getId())) {
                                kennzahlenPfadweise.put(sz.getId(), new TreeMap<>());
                            }
                            kennzahlenPfadweise.get(sz.getId()).put(pfad, br.kennzahlenPfadweise);

                            /* Deprecated
                            if (!kennzahlenPfadweiseLoB.containsKey(sz.getId())) {
                            	kennzahlenPfadweiseLoB.put(sz.getId(), new TreeMap<>());
                            }
                            */

                            if (!kennzahlenPfadweiseLoB.containsKey(sz.getId())) {
                                kennzahlenPfadweiseLoB.put(sz.getId(), new TreeMap<>());
                            }

                            // Deprecated: kennzahlenPfadweiseLoB.get(sz.getId()).put(pfad, br.kennzahlenPfadweiseLoB);

                            // Get Set of relevant LoBs
                            Map<String, Long> relevantLoBs = br.kennzahlenPfadweiseLoB.stream()
                                    .collect(Collectors.groupingBy(h -> h.getLob(), Collectors.counting()));

                            for (String loBVal : relevantLoBs.keySet()) {
                                if (!kennzahlenPfadweiseLoB.get(sz.getId()).containsKey(loBVal)) {
                                    kennzahlenPfadweiseLoB.get(sz.getId()).put(loBVal, new TreeMap<>());
                                }
                                kennzahlenPfadweiseLoB.get(sz.getId()).get(loBVal).put(pfad,
                                        br.kennzahlenPfadweiseLoB.stream().filter(e -> e.getLob().equals(loBVal))
                                                .collect(Collectors.toList()).get(0));
                            }

                            for (String lob : br.mittelwerteUndCe.keySet()) {
                                if (!mittelwerteUndCe.containsKey(lob)) {
                                    mittelwerteUndCe.put(lob, new HashMap<>());
                                }
                                final Map<Integer, MittelwerteUndCe> map = br.mittelwerteUndCe.get(lob);
                                for (int zeit : map.keySet()) {
                                    if (!mittelwerteUndCe.get(lob).containsKey(zeit)) {
                                        mittelwerteUndCe.get(lob).put(zeit, new ArrayList<>());
                                    }
                                    mittelwerteUndCe.get(lob).get(zeit).add(map.get(zeit));
                                }
                            }

                            for (String lob : br.mittelwerteNurCe.keySet()) {
                                if (!mittelwerteNurCe.containsKey(lob)) {
                                    mittelwerteNurCe.put(lob, new HashMap<>());
                                }
                                final Map<Integer, MittelwerteNurCe> map = br.mittelwerteNurCe.get(lob);
                                for (int zeit : map.keySet()) {
                                    if (!mittelwerteNurCe.get(lob).containsKey(zeit)) {
                                        mittelwerteNurCe.get(lob).put(zeit, new ArrayList<>());
                                    }
                                    mittelwerteNurCe.get(lob).get(zeit).add(map.get(zeit));
                                }
                            }

                            final int percent = berechnetePfade.size() * 100 / pfade.size();
                            if (percent != lastPercent) {
                                fortschritt.setBerechnungPercent(sz.getId(), percent);
                                lastPercent = percent;
                            }

                            if (nextPfad < pfade.size() && !fortschritt.isAbbruch()) {
                                br.doer.berechne(pfade.get(nextPfad));
                                if (pfade.get(nextPfad) == pfadBis) {
                                    letzteBerechnung = br.doer.getBerechnung();
                                }
                                ++nextPfad;
                            } else {
                                br.doer.stop();
                            }

                        } else {
                            // an error occured:
                            if (br.error.isPresent()) {
                                throw br.error.get();
                            }
                            // doer has stopped:
                            berechnungThreads.remove(br.doer);
                            if (berechnungThreads.isEmpty()) {
                                // all threads ready: stop
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }

                if (berechnetePfade.size() != pfade.size() && !fortschritt.isAbbruch()) {
                    throw new IllegalStateException("es wurden einige Pfade nicht berechnet!");
                }

                fortschritt.setBerechnungPercent(sz.getId(), 100);

                // alle Lobs, die in den Daten gefunden wurden
                final Set<String> lobs = new HashSet<>();
                lobs.addAll(mittelwerteUndCe.keySet());
                lobs.addAll(mittelwerteNurCe.keySet());

                // TODO: Auskommentieren / löschen: Datenbankklasseninstanz
                //BSMDB db=new BSMDB(false);

                for (String lob : vuParameter.getLobs()) {
                    lobs.remove(lob);

                    if (!mittelwerteUndCe.containsKey(lob)) {
                        continue;
                    }

                    // Alle Zeiten in der korrekten Sortierung
                    final TreeSet<Integer> zeiten = new TreeSet<>();
                    zeiten.addAll(mittelwerteUndCe.get(lob).keySet());

                    for (int zeit : zeiten) {
                        Mittelwerte mittelwerte = null;
                        MittelwerteUndCe undCeNurCe = null;
                        MittelwerteNurCe nurCe = null;
                        for (MittelwerteUndCe mwCe : mittelwerteUndCe.get(lob).get(zeit)) {
                            if (mittelwerte == null) {
                                mittelwerte = new Mittelwerte(mwCe, addierePfad0);
                            } else {
                                mittelwerte.addValues(mwCe, addierePfad0);
                            }
                            if (mwCe.getPfad() == 0) {
                                undCeNurCe = mwCe;
                            }
                        }
                        for (MittelwerteNurCe nce : mittelwerteNurCe.get(lob).get(zeit)) {
                            if (nce.getPfad() == 0) {
                                nurCe = nce;
                                break;
                            }
                        }
                        mittelwerte.setValues(undCeNurCe);
                        mittelwerte.setValues(nurCe);
                        mittelwerteList.add(mittelwerte);

                        //TODO: Auskommentieren / löschen: Datenbankaufruf
                        /*MittelwerteUndCEZeitschrittig mundCEZeitschrittig=db.getMittelwerteUndCEZeitschrittig(sz.getId(),lob,zeit,addierePfad0);
                        //mittelwerteUndCeZeitschrittigList.add(mundCEZeitschrittig);
                        try {
                        	final PrintStream out = new PrintStream(
                        			new BufferedOutputStream(new FileOutputStream("AusgabeTest.csv", true), 8 * 1024) );
                        	mundCEZeitschrittig.writeZeile(out);
                        	out.close();
                        }
                        catch(Exception er){er.printStackTrace();}
                        */
                    }
                }
            }

            // DG: Berechnung der normalen Mittelwerte pro LOB		
            //Beachte Schlüsselung bei kennzahlenPfadweiseLoB: ScenarioID, Pfad ID, LOB-Name
            // TODO: ggf. weitere Teile in die Klasse KennzahlenMittelung (LoB) auslagern und Lesbarkeit des Codes verbessern
            // TODO: CV Schätzer Spalte H aus!!! Kenzzahlen Pfadweise !!! (ohne LOB !!!) mwPassiva konfiguerierbar machen
            // Rechnung für Antithethische Variablen wird nicht bei Einzelpfadanalyse genutzt d.h. nutze die Logik nur, wenn pfade.size() > 2 
            // TODO: The serializable class does not declare a static final serialVersionUID field of type long prüfen
            @SuppressWarnings("serial")
            TreeMap<Integer, TreeMap<String, TreeMap<String, KennzahlenMittelungLoB>>> schaetzerMittelwerteLoB = kennzahlenPfadweiseLoB
                    .entrySet().stream()
                    .collect(Collectors.toMap(l -> l.getValue().get(l.getValue().firstKey()).get(0).getSzenarioId(),
                            l -> l.getValue().entrySet().stream()
                                    .collect(Collectors.toMap(e -> e.getValue().get(0).getLob(),

                                            e -> new TreeMap<String, KennzahlenMittelungLoB>() {

                                                {
                                                    // Best Estimate be Spalte D
                                                    put("be", new KennzahlenMittelungLoB("be",
                                                            "Best Estimate versicherungstechnische Rückstellungen",
                                                            "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(), e.getValue().get(0).getBe(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getBe())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getBe())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getBe())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getBe())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getBe()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getBe()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getBe(), 2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getBe())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getBe())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getBe()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // Überschussfonds Spalte E
                                                    put("ueberschussFond", new KennzahlenMittelungLoB("ueberschussFond",
                                                            "Überschussfonds", "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(),
                                                            e.getValue().get(0).getUeberschussFond(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getUeberschussFond())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getUeberschussFond())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getUeberschussFond())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getUeberschussFond())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getUeberschussFond())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getUeberschussFond())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(k
                                                                                            .getValue()
                                                                                            .getUeberschussFond(), 2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getUeberschussFond())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getUeberschussFond())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getUeberschussFond()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // ewGar Spalte F
                                                    put("ewGar", new KennzahlenMittelungLoB("ewGar",
                                                            "Erwartungswertrückstellungen für garantierte Leistungen",
                                                            "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(),
                                                            e.getValue().get(0).getEwGar(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEwGar())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEwGar())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEwGar())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEwGar())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getEwGar()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getEwGar()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getEwGar(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getEwGar())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getEwGar())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEwGar()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // epifp Spalte G
                                                    put("epifp", new KennzahlenMittelungLoB("epifp",
                                                            "Bei künftigen Prämien einkalkulierter erwarteter Gewinn",
                                                            "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(),
                                                            e.getValue().get(0).getEpifp(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEpifp())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEpifp())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEpifp())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEpifp())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getEpifp()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getEpifp()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getEpifp(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getEpifp())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getEpifp())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getEpifp()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // zueb Spalte H
                                                    put("zueb", new KennzahlenMittelungLoB("zueb",
                                                            "Zukünftige Überschussbeteiligung", "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(), e.getValue().get(0).getZueb(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getZueb())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getZueb())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getZueb())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getZueb())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getZueb()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getZueb()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getZueb(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getZueb())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getZueb())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getZueb()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // optionen Spalte I
                                                    put("optionen", new KennzahlenMittelungLoB("optionen",
                                                            "Zeitwert der Optionen", "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(),
                                                            e.getValue().get(0).getOptionen(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getOptionen())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getOptionen())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getOptionen())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getOptionen())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getOptionen())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getOptionen())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getOptionen(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getOptionen())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getOptionen())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getOptionen()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // Reinsurance Recoverables RR Spalte J
                                                    put("rr", new KennzahlenMittelungLoB("rr", "Einforderbare Beträge",
                                                            "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(), e.getValue().get(0).getRr(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getRr())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getRr())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getRr())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getRr())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getRr()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getRr()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getRr(), 2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getRr())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getRr())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getRr()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // GCR Spalte K
                                                    put("gcr", new KennzahlenMittelungLoB("gcr",
                                                            "Going Concern Reserve", "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(), e.getValue().get(0).getGcr(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getGcr())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getGcr())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getGcr())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getGcr())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getGcr()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getGcr()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getGcr(), 2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getGcr())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getGcr())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getGcr()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));
                                                    // KBM Spalte L
                                                    put("kbm", new KennzahlenMittelungLoB("kbm",
                                                            "Kosten Biometrie Marge", "mwPassiva",
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen(),
                                                            e.getValue().get(0).getSzenarioId(),
                                                            e.getValue().get(0).getLob(), e.getValue().get(0).getKbm(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getKbm())
                                                                                    .sum() / 2.0
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getKbm())
                                                                                    .sum(),
                                                            // Unterscheide Anthitethisch / Nicht Antithetisch
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getKbm())
                                                                                    .count() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> k.getValue().getKbm())
                                                                                    .count(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                                                    AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getKbm()).boxed()
                                                                    .collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getKbm()).boxed()
                                                                    .collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            k.getValue().getKbm(), 2.0))
                                                                                    .sum(),

                                                            kennzahlenPfadweise.get(e.getValue().get(0).getSzenarioId())
                                                                    .get(0).getMwPassiva(),
                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum() / 2
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions.addListsWithDoubles(e
                                                                                    .getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis()
                                                                                            && (j.getValue().getPfad()
                                                                                                    & 1) == 0)
                                                                                    .mapToDouble(
                                                                                            k -> kennzahlenPfadweise
                                                                                                    .get(e.getValue()
                                                                                                            .get(0)
                                                                                                            .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .boxed()
                                                                                    .collect(Collectors.toList()),
                                                                                    e.getValue().entrySet().stream()
                                                                                            .filter(j -> j.getValue()
                                                                                                    .getPfad() >= eingabe
                                                                                                            .getPfadVon()
                                                                                                    && j.getValue()
                                                                                                            .getPfad() <= eingabe
                                                                                                                    .getPfadBis()
                                                                                                    && (j.getValue()
                                                                                                            .getPfad()
                                                                                                            & 1) == 1)
                                                                                            .mapToDouble(
                                                                                                    k -> kennzahlenPfadweise
                                                                                                            .get(e.getValue()
                                                                                                                    .get(0)
                                                                                                                    .getSzenarioId())
                                                                                                            .get(k.getKey())
                                                                                                            .getMwPassiva())
                                                                                            .boxed()
                                                                                            .collect(Collectors
                                                                                                    .toList()))
                                                                                    .stream()
                                                                                    .mapToDouble(
                                                                                            k -> Math.pow(0.5 * k, 2.0))
                                                                                    .sum()
                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> Math.pow(
                                                                                            kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva(),
                                                                                            2.0))
                                                                                    .sum(),

                                                            szenarien.stream()
                                                                    .filter(j -> j.getId() == e.getValue().get(0)
                                                                            .getSzenarioId())
                                                                    .collect(Collectors.toList()).get(0)
                                                                    .isAntitethischeVariablen() && pfade.size() > 2
                                                                            ? AuxiliaryFunctions
                                                                                    .multiplyListsWithDoubles(
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> kennzahlenPfadweise
                                                                                                                                    .get(e.getValue()
                                                                                                                                            .get(0)
                                                                                                                                            .getSzenarioId())
                                                                                                                                    .get(k.getKey())
                                                                                                                                    .getMwPassiva())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())),
                                                                                            AuxiliaryFunctions
                                                                                                    .addListsWithDoubles(
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 0)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getKbm())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList()),
                                                                                                            e.getValue()
                                                                                                                    .entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(j -> j
                                                                                                                            .getValue()
                                                                                                                            .getPfad() >= eingabe
                                                                                                                                    .getPfadVon()
                                                                                                                            && j.getValue()
                                                                                                                                    .getPfad() <= eingabe
                                                                                                                                            .getPfadBis()
                                                                                                                            && (j.getValue()
                                                                                                                                    .getPfad()
                                                                                                                                    & 1) == 1)
                                                                                                                    .mapToDouble(
                                                                                                                            k -> k.getValue()
                                                                                                                                    .getKbm())
                                                                                                                    .boxed()
                                                                                                                    .collect(
                                                                                                                            Collectors
                                                                                                                                    .toList())))
                                                                                    .stream().mapToDouble(k -> 0.25 * k)
                                                                                    .sum()

                                                                            : e.getValue().entrySet().stream()
                                                                                    .filter(j -> j.getValue()
                                                                                            .getPfad() >= eingabe
                                                                                                    .getPfadVon()
                                                                                            && j.getValue()
                                                                                                    .getPfad() <= eingabe
                                                                                                            .getPfadBis())
                                                                                    .mapToDouble(k -> k.getValue()
                                                                                            .getKbm()
                                                                                            * kennzahlenPfadweise.get(e
                                                                                                    .getValue().get(0)
                                                                                                    .getSzenarioId())
                                                                                                    .get(k.getKey())
                                                                                                    .getMwPassiva())
                                                                                    .sum()));

                                                    // ggf. weitere Kennzahlen hier einfügen
                                                }
                                            }, (a, b) -> a, () -> {
                                                return new TreeMap<>();
                                            })),
                            (p, q) -> p, () -> {
                                return new TreeMap<Integer, TreeMap<String, TreeMap<String, KennzahlenMittelungLoB>>>();
                            }));

            // DG: Berechnung der normalen Mittelwerte		
            // TODO: CV Schätzer MWPassiva ggf. konfigurierbar machen
            // TODO: ggf. weitere Teile in die Klasse KennzahlenMittelung (LoB) auslagern und Lesbarkeit des Codes verbessern
            // TODO: ggf. Excel und Java Logik angleichen bei ungerader Pfadanzahl und antithetischen Variablen! Fachlich sollte dies aber eigentlich nicht erlaubt sein.

            if ((pfade.size() & 1) == 0 && pfade.size() > 2
                    && szenarien.stream().mapToInt(e -> e.isAntitethischeVariablen() == true ? 1 : 0).sum() > 0) {
                BSMLog.writeLog(
                        "WARNUNG: Die Berechnung verwendet Szenariensätze mit antithetischen Pfaden und es wird eine ungerade Anzahl an Pfaden berechnet. Die Ergebnisse der Mittelung sind in diesem Fall nicht aussagekräftig! ");
            }

            @SuppressWarnings("serial")
            // TODO: The serializable class does not declare a static final serialVersionUID field of type long prüfen
            TreeMap<Integer, TreeMap<String, KennzahlenMittelung>> schaetzerMittelwerte = kennzahlenPfadweise.entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> new TreeMap<String, KennzahlenMittelung>() {

                        {
                            // ZAG: Spalte C
                            put("zag", new KennzahlenMittelung("zag", "Zukünftige Aktionärsgewinne", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getZag(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZag()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZag()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZag()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZag()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getZag()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getZag()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getZag(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getZag()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getZag())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZag()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));

                            // Best Estimate Spalte D
                            put("be", new KennzahlenMittelung("be",
                                    "Best Estimate versicherungstechnische Rückstellungen", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getBe(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getBe()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getBe()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getBe()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getBe()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getBe()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getBe()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getBe(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getBe()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getBe())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getBe()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // GCR: Spalte E
                            put("gcr", new KennzahlenMittelung("gcr", "Going Concern Reserve", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getGrcKlassik(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrcKlassik()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrcKlassik()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrcKlassik()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrcKlassik()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getGrcKlassik()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getGrcKlassik()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getGrcKlassik(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getGrcKlassik())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getGrcKlassik())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrcKlassik()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // GRND: Spalte F
                            put("grnd", new KennzahlenMittelung("grnd", "Genussrechte und Nachrangdarlehen",
                                    "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getGrnd(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrnd()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrnd()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrnd()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrnd()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getGrnd()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getGrnd()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getGrnd(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getGrnd()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getGrnd())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getGrnd()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // GRND: Steuer G
                            put("steuer", new KennzahlenMittelung("steuer", "Steuer", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getSteuer(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getSteuer()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getSteuer()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getSteuer()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getSteuer()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getSteuer()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getSteuer()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getSteuer(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getSteuer()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getSteuer())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getSteuer()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));

                            // mwPassiva Spalte H
                            put("mwPassiva", new KennzahlenMittelung("mwPassiva", "Marktwert der Passiva", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getMwPassiva(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getMwPassiva()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getMwPassiva()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // Überschussfonds Spalte I
                            put("ueberschussfond", new KennzahlenMittelung("ueberschussfond", "Überschussfonds",
                                    "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getUeberschussFond(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getUeberschussFond()).sum()
                                                            / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getUeberschussFond()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getUeberschussFond()).count()
                                                            / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getUeberschussFond())
                                                            .count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getUeberschussFond()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getUeberschussFond()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math
                                                                    .pow(k.getValue().getUeberschussFond(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getUeberschussFond())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue()
                                                                                    .getUeberschussFond())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getUeberschussFond()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // ewGar Spalte J
                            put("ewGar", new KennzahlenMittelung("ewGar",
                                    "Erwartungswertrückstellungen für garantierte Leistungen", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getEwGar(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEwGar()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEwGar()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEwGar()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEwGar()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getEwGar()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getEwGar()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getEwGar(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getEwGar()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getEwGar())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEwGar()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // RR Spalte K
                            put("rr", new KennzahlenMittelung("rr", "Einforderbare Beträge", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getRr(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getRr()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getRr()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getRr()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getRr()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getRr()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getRr()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getRr(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getRr()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getRr())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getRr()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // epifp Spalte L
                            put("epifp", new KennzahlenMittelung("epifp",
                                    "Bei künftigen Prämien einkalkulierter erwarteter Gewinn", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getEpIfp(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEpIfp()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEpIfp()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEpIfp()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEpIfp()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getEpIfp()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getEpIfp()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getEpIfp(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getEpIfp()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getEpIfp())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getEpIfp()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // kbm Spalte M
                            put("kbm", new KennzahlenMittelung("kbm", "Kosten Biometrie Marge", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getKbm(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getKbm()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getKbm()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getKbm()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getKbm()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getKbm()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getKbm()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getKbm(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getKbm()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getKbm())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getKbm()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // ZÜB Spalte N
                            put("zueb", new KennzahlenMittelung("zueb", "Zukünftige Überschussbeteiligung", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getZueb(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZueb()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZueb()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZueb()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZueb()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getZueb()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getZueb()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getZueb(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getZueb()).boxed()
                                                                    .collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue().getZueb())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getZueb()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // Optionen Spalte O
                            put("optionen", new KennzahlenMittelung("optionen", "Zeitwert der Optionen", "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getOptionen(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getOptionen()).sum() / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getOptionen()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getOptionen()).count() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getOptionen()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getOptionen()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getOptionen()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> Math.pow(k.getValue().getOptionen(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getOptionen())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getOptionen())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getOptionen()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // BSM MR IN <
                            // Reinsurance Recoverable Spalte S
                            put("reRecoverable", new KennzahlenMittelung("reRecoverable", "Reinsurance Recoverable",
                                    "mwPassiva",
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen(),
                                    e.getValue().get(0).getSzenarioId(), e.getValue().get(0).getReRecoverable(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getReRecoverable()).sum()
                                                            / 2.0
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getReRecoverable()).sum(),
                                    // Unterscheide Anthitethisch / Nicht Antithetisch
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getReRecoverable()).count()
                                                            / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getReRecoverable()).count(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2 ? // Lösung ist leider sehr umständlich, da Java Streams keine Zip Funktionalität bieten
                            AuxiliaryFunctions
                                    .addListsWithDoubles(
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 0)
                                                    .mapToDouble(k -> k.getValue().getReRecoverable()).boxed()
                                                    .collect(Collectors.toList()),
                                            e.getValue().entrySet().stream()
                                                    .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                            && j.getValue().getPfad() <= eingabe.getPfadBis()
                                                            && (j.getValue().getPfad() & 1) == 1)
                                                    .mapToDouble(k -> k.getValue().getReRecoverable()).boxed()
                                                    .collect(Collectors.toList()))
                                    .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getReRecoverable(), 2.0))
                                                            .sum(),

                                    e.getValue().get(0).getMwPassiva(),
                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum() / 2
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getMwPassiva()).sum(),

                                    szenarien.stream().filter(j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.addListsWithDoubles(
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue()
                                                                                    .getPfad() <= eingabe.getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                            e.getValue().entrySet().stream().filter(
                                                                    j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 1)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()))
                                                            .stream().mapToDouble(k -> Math.pow(0.5 * k, 2.0)).sum()
                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(
                                                                    k -> Math.pow(k.getValue().getMwPassiva(), 2.0))
                                                            .sum(),

                                    szenarien
                                            .stream().filter(
                                                    j -> j.getId() == e.getValue().get(0).getSzenarioId())
                                            .collect(Collectors.toList()).get(0).isAntitethischeVariablen()
                                            && pfade.size() > 2
                                                    ? AuxiliaryFunctions.multiplyListsWithDoubles(
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getMwPassiva())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(
                                                                                    k -> k.getValue().getMwPassiva())
                                                                            .boxed().collect(Collectors.toList())),
                                                            AuxiliaryFunctions.addListsWithDoubles(e.getValue()
                                                                    .entrySet().stream()
                                                                    .filter(j -> j.getValue().getPfad() >= eingabe
                                                                            .getPfadVon()
                                                                            && j.getValue().getPfad() <= eingabe
                                                                                    .getPfadBis()
                                                                            && (j.getValue().getPfad() & 1) == 0)
                                                                    .mapToDouble(k -> k.getValue().getReRecoverable())
                                                                    .boxed().collect(Collectors.toList()),
                                                                    e.getValue().entrySet().stream()
                                                                            .filter(j -> j.getValue()
                                                                                    .getPfad() >= eingabe.getPfadVon()
                                                                                    && j.getValue().getPfad() <= eingabe
                                                                                            .getPfadBis()
                                                                                    && (j.getValue().getPfad()
                                                                                            & 1) == 1)
                                                                            .mapToDouble(k -> k.getValue()
                                                                                    .getReRecoverable())
                                                                            .boxed().collect(Collectors.toList())))
                                                            .stream().mapToDouble(k -> 0.25 * k).sum()

                                                    : e.getValue().entrySet().stream()
                                                            .filter(j -> j.getValue().getPfad() >= eingabe.getPfadVon()
                                                                    && j.getValue().getPfad() <= eingabe.getPfadBis())
                                                            .mapToDouble(k -> k.getValue().getReRecoverable()
                                                                    * k.getValue().getMwPassiva())
                                                            .sum()

                    ));
                            // BSM MR IN >
                            // Hier lassen sich weitere Kennzahlen ergänzen
                        }
                    }, (a, b) -> a, () -> {
                        return new TreeMap<>();
                    }));

            // nach Test wieder entfernen

            final Set<AusgabeThread> ausgabeThreads = new HashSet<>();

            final File rzgFileName = new File(vuParameter.getTransferDir(), VuParameter.RZG);
            final AusgabeThreadTableField rzgT = new AusgabeThreadTableField(this, fortschritt, RzgZeile.class,
                    letzteBerechnung.getRzgZeilen(), rzgFileName);
            ausgabeThreads.add(rzgT);
            new Thread(rzgT).start();

            final File aggFileName = new File(vuParameter.getTransferDir(), VuParameter.AGG);
            final AusgabeThreadTableField aggT = new AusgabeThreadTableField(this, fortschritt, AggZeile.class,
                    letzteBerechnung.getAggZeilen(), aggFileName);
            ausgabeThreads.add(aggT);
            new Thread(aggT).start();

            // Ausgabe von  FI Ausfall.csv
            // Beachte: Es wird nur das Ergebnis der letzten Berechnung ausgegeben. Eigentlich müsste man die Werte für jeden ESG Szenariensatz z.B. 1, 10,11 exportieren
            final File fiAusfallFileName = new File(vuParameter.getTransferDir(), VuParameter.FI_AUSFALL);
            final AusgabeThreadTableField fiAusf = new AusgabeThreadTableField(this, fortschritt, FiAusfallZeile.class,
                    letzteBerechnung.getFiAusfallZeilen(), fiAusfallFileName);
            ausgabeThreads.add(fiAusf);
            new Thread(fiAusf).start();

            // Ausgabe der Dateien
            final List<KennzahlenPfadweise> kp = new ArrayList<>();
            for (int i : kennzahlenPfadweise.keySet()) {
                Map<Integer, KennzahlenPfadweise> map = kennzahlenPfadweise.get(i);
                for (int j : map.keySet()) {
                    final KennzahlenPfadweise kptmp = map.get(j);
                    kp.add(map.get(j));
                    if (kptmp.getPfad() == 0 && eingabe.getPfadVon() == 0) {
                        kp.add(map.get(j));
                    }
                }
            }

            final File kennzPfadFileName = new File(vuParameter.getTransferDir(), VuParameter.KENNZAHLEN_PFADWEISE);
            final AusgabeThreadTableField kpt = new AusgabeThreadTableField(this, fortschritt,
                    KennzahlenPfadweise.class, kp, kennzPfadFileName);
            ausgabeThreads.add(kpt);
            new Thread(kpt).start();

            // Hier schon mal die Header für alte CSV Datei Kennzahlen Pfadweise ausgeben:
            final File schaetzerMittelwerteFileName = new File(vuParameter.getTransferDir(),
                    VuParameter.SCHAETZER_MITTELWERTE);
            KennzahlenPfadweise.writeSchaeterMittelwerte(schaetzerMittelwerteFileName, kp);

            // und auch die Schätzer Mittelwerte-Datei mit neu berechneten Werten ausgeben 
            final File schaetzerMittelwerteWithValuesFileName = new File(vuParameter.getTransferDir(),
                    VuParameter.SCHAETZER_MITTELWERTE_JAVA);

            // Ausgabe direkt ist deprecated: KennzahlenMittelung.writeSchaeterMittelwerteWithValues(schaetzerMittelwerteWithValuesFileName, schaetzerMittelwerte);
            // Überführen der Schätzer mittelwerte Struktur in eine Array-Liste (Flattening of Collection in den Typ: List<KennzahlenMittelung>)	
            final AusgabeThreadTableField schaetzerMittelwerteAusgabe = new AusgabeThreadTableField(this, fortschritt,
                    KennzahlenMittelung.class, schaetzerMittelwerte.values().stream().map(e -> e.values())
                            .flatMap(l -> l.stream()).collect(Collectors.toList()),
                    schaetzerMittelwerteWithValuesFileName);
            ausgabeThreads.add(schaetzerMittelwerteAusgabe);
            new Thread(schaetzerMittelwerteAusgabe).start();

            // und auch die Schätzer Mittelwerte LOB-Datei mit berechneten Werten 
            final File schaetzerMittelwerteLoBWithValuesFileName = new File(vuParameter.getTransferDir(),
                    VuParameter.SCHAETZER_MITTELWERTE_LOB_JAVA);
            // Ausgabe direkt ist deprecated:  KennzahlenMittelung.writeSchaeterMittelwerteLoBWithValues(schaetzerMittelwerteLoBWithValuesFileName, schaetzerMittelwerteLoB);
            final AusgabeThreadTableField schaetzerMittelwerteLoBAusgabe = new AusgabeThreadTableField(this,
                    fortschritt, KennzahlenMittelungLoB.class,
                    schaetzerMittelwerteLoB.values().stream().map(e -> e.values()).flatMap(l -> l.stream())
                            .map(j -> j.values()).flatMap(h -> h.stream()).collect(Collectors.toList()),
                    schaetzerMittelwerteLoBWithValuesFileName);
            ausgabeThreads.add(schaetzerMittelwerteLoBAusgabe);
            new Thread(schaetzerMittelwerteLoBAusgabe).start();

            // und auch den Header für die Stochastischen Kennzahlen:
            {
                final File stochastischeKennzahlen = new File(vuParameter.getTransferDir(),
                        VuParameter.STOCHASTISCHE_KENNZAHLEN);
                try (final PrintStream ps = new PrintStream(new FileOutputStream(stochastischeKennzahlen))) {
                    ps.println("Stressszenario;Stressszenario ID");
                    for (SzenarioMappingZeile z : szenarien) {
                        ps.println(z.getName() + ";" + z.getId());
                    }
                }
            }

            final List<KennzahlenPfadweiseLoB> kpl = new ArrayList<>();
            //Deprecated: for (int i : kennzahlenPfadweiseLoB.keySet()) {
            // Deprecated:	Map<Integer, List<KennzahlenPfadweiseLoB>> map = kennzahlenPfadweiseLoB.get(i);
            for (int i : kennzahlenPfadweiseLoB.keySet()) {
                TreeMap<String, TreeMap<Integer, KennzahlenPfadweiseLoB>> map = kennzahlenPfadweiseLoB.get(i);
                for (String j : map.keySet()) {
                    TreeMap<Integer, KennzahlenPfadweiseLoB> map2 = map.get(j);
                    for (int k : map2.keySet()) {
                        kpl.add(map2.get(k));
                        if (k == 0 && eingabe.getPfadVon() == 0) {
                            // pfad 0 doppelt ausgeben:
                            kpl.add(map2.get(k));
                        }
                    }
                }
            }

            final File kennzPfadLobFileName = new File(vuParameter.getTransferDir(),
                    VuParameter.KENNZAHLEN_PFADWEISE_LOB);
            final AusgabeThreadTableField kplt = new AusgabeThreadTableField(this, fortschritt,
                    KennzahlenPfadweiseLoB.class, kpl, kennzPfadLobFileName);
            ausgabeThreads.add(kplt);
            new Thread(kplt).start();

            // Hier schon mmal die Header für Kennzahlen Pfadweise LoB ausgeben:
            final File schaetzerMittelwerteLobFileName = new File(vuParameter.getTransferDir(),
                    VuParameter.SCHAETZER_MITTELWERTE_LOB);
            KennzahlenPfadweiseLoB.writeSchaeterMittelwerteLob(schaetzerMittelwerteLobFileName, kpl);

            {
                // und den Header für die alte CSV Datei Stochastischen Kennzahlen pro LoB
                final Map<Integer, Set<String>> szenarioNachLob = new HashMap<>();
                final File stochastischeKennzahlen = new File(vuParameter.getTransferDir(),
                        VuParameter.STOCHASTISCHE_KENNZAHLEN_LOB);
                try (final PrintStream ps = new PrintStream(new FileOutputStream(stochastischeKennzahlen))) {
                    ps.println("");
                    for (KennzahlenPfadweiseLoB k : kpl) {
                        if (!szenarioNachLob.containsKey(k.getSzenarioId())) {
                            szenarioNachLob.put(k.getSzenarioId(), new HashSet<>());
                        }
                        if (!szenarioNachLob.get(k.getSzenarioId()).contains(k.getLob())) {
                            szenarioNachLob.get(k.getSzenarioId()).add(k.getLob());
                            final String name = vuParameter.getSzenarioMapping().getSzenarionMapping(k.getSzenarioId())
                                    .getName();
                            ps.println(name + ";" + k.getSzenarioId() + ";" + k.getLob());
                        }
                    }
                }
            }

            final AusgabThreadMittelwerte awMw = new AusgabThreadMittelwerte(this, fortschritt, mittelwerteList,
                    new File(vuParameter.getTransferDir(), VuParameter.KENNZAHLEN_MITTELWERTE_ZEITSCHRITTIG));
            ausgabeThreads.add(awMw);
            new Thread(awMw).start();

            Optional<Throwable> error = Optional.empty();

            while (true) {
                final AusgabeThread at = ausgaben.take();
                if (at.getError().isPresent()) {
                    error = at.getError();
                }
                ausgabeThreads.remove(at);
                if (ausgabeThreads.size() == 0) {
                    break;
                }
            }

            // ein Fehler ist aufgetreten, einmal signalisieren!
            if (error.isPresent()) {
                throw error.get();
            }

            // Schreiben des Protokolls:
            final File ausfuehrungsLog = new File(vuParameter.getTransferDir(), VuParameter.AUSFUEHRUNGS_LOG);
            try (final PrintStream out = new PrintStream(new FileOutputStream(ausfuehrungsLog))) {
                // Endezeit der Simulation:
                final GregorianCalendar ende = new GregorianCalendar();

                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                out.println("Start der Simulation;" + sdf.format(start.getTime()));
                out.println("Ende der Simulation;" + sdf.format(ende.getTime()));

                final long dauer = ende.getTimeInMillis() - start.getTimeInMillis();
                final long millis = dauer % 1000;
                final long sec = (dauer / 1000) % 60;
                final long min = (dauer / (1000 * 60)) % 60;
                final long hour = dauer / (1000 * 60 * 60);
                out.println("Benötigte Rechenzeit;" + String.format("%d:%02d:%02d:%03d", hour, min, sec, millis)
                        + " Std:Min:Sek:Msec");
                out.println("Pfad von:;" + eingabe.getPfadVon());
                out.println("Pfad bis:;" + eingabe.getPfadBis());
                out.println("Rechenkern:;Java");
                out.println();
                for (int sznrId : sznrAlleIdSet) {
                    for (String headerLine : sznrHeader.get(sznrId)) {
                        out.println(headerLine);
                    }
                }
            }

            // Resultat der Berechnung:
            final BerechnungResultat resultat = new BerechnungResultat(letzteBerechnung, kennzahlenPfadweise,
                    kennzahlenPfadweiseLoB, mittelwerteList, schaetzerMittelwerte, schaetzerMittelwerteLoB);
            fortschritt.berechnungBeendet(resultat);

        } catch (Throwable e) {
            fortschritt.berechnungGechrashed(e);
            return;
        }
    }

    /**
     * Signaliesiere das Ende einer Ausgabe.
     * 
     * @param thread
     *            der beendete Thread
     */
    public synchronized void ausgabeReady(final AusgabeThread thread) {
        while (true) {
            try {
                ausgaben.put(thread);
                return;
            } catch (InterruptedException e) {
            }
        }
    }
}
