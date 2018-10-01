package de.gdv.bsm.vu.kennzahlen;

import static de.gdv.bsm.vu.module.DiskontFunktion.df;
import static de.gdv.bsm.vu.module.DiskontFunktion.dfVu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;

/**
 * Berechnung der Kennzahlen pro LoB. Dies entspricht den Angaben im Blatt <code>StrgTab_Mittelwerte</code> in der
 * Rubrik <b>Pfadweise Ausgabe LoB</b>.
 * <p/>
 * Die Namen wurden übernommen und nur nach Java-Konvention verändert. Die Spaltennamen wurden durch <code>get...</code>
 * -Funktionen im Blatt <code>rzg</code> ersetzt. Sollen andere Werte aus <code>rzg</code> verwendet werden, sind in der
 * Klasse {@link RzgZeile} eventuell passende <code>get...</code>-Funktionen zu ergänzen. Die dortigen Angaben
 * <code>DF</code> bzw. <code>DFVU</code> wurden durch die Funktionsaufrufe <code>df</code> und <code>dfVu</code>
 * ersetzt. Addition bzw. Subtraktion wurden direkte in die Formeln übernommen.
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
public class KennzahlenPfadweiseLoB {
	@TableField
	private final int szenarioId;
	@TableField
	private final int pfad;
	@TableField
	private final String lob;
	@TableField
	private final double be;
	@TableField
	private final double ueberschussFond;
	@TableField
	private final double ewGar;
	@TableField
	private final double epifp;
	@TableField
	private final double zueb;
	@TableField
	private final double optionen;
	@TableField
	private final double rr;
	@TableField
	private final double gcr;
	@TableField
	private final double kbm;

	/**
	 * Erstelle die Kennzahlen zu einem Pfad anhand der Agg- und Rzg- Zeilen.
	 * 
	 * @param szenarioId
	 *            zu dem die Berechnung gehört
	 * @param pfad
	 *            der Berechnung
	 * @param lob
	 *            die zu berechnende LoB
	 * @param rzgZeilenMap
	 *            alle relevanten rzg-Zeilen
	 * @param aggZeilen
	 *            über alle Zeiten
	 * @param monat
	 *            der Zahlungsmonat aus den zeitunabhängigen Managementregeln
	 */
	public KennzahlenPfadweiseLoB(final int szenarioId, final int pfad, final String lob,
			final Map<Integer, Map<String, Map<String, List<RzgZeile>>>> rzgZeilenMap, final List<AggZeile> aggZeilen,
			final double monat) {
		this.szenarioId = szenarioId;
		this.pfad = pfad;
		this.lob = lob;

		final List<RzgZeile> rzgZeilen = new ArrayList<>();
		for (int zins : rzgZeilenMap.keySet()) {
			for (String altNeu : rzgZeilenMap.get(zins).keySet()) {
				for (List<RzgZeile> values : rzgZeilenMap.get(zins).get(altNeu).values()) {
					rzgZeilen.addAll(values);
				}
			}
		}

		be = rzgZeilen.stream()
				.mapToDouble(z -> df(z.getKostenKaRzg(), aggZeilen.get(z.getZeit()))
						+ dfVu(z.getLGesamt(), aggZeilen.get(z.getZeit()), monat)
						+ df(z.getEndZahlung(), aggZeilen.get(z.getZeit()))
						- dfVu(z.getBeitraegeStoch(), aggZeilen.get(z.getZeit()), monat)
						+ dfVu(z.getKostenStoch(), aggZeilen.get(z.getZeit()), monat))
				.sum();
		ueberschussFond = rzgZeilen.stream().mapToDouble(z -> df(z.getSurplusFondRzg(), aggZeilen.get(z.getZeit())))
				.sum();
		ewGar = rzgZeilen.stream()
				.mapToDouble(z -> dfVu(z.getLGarantiertDet(), aggZeilen.get(z.getZeit()), monat)
						+ dfVu(z.getKosten(), aggZeilen.get(z.getZeit()), monat)
						- dfVu(z.getPraemien(), aggZeilen.get(z.getZeit()), monat)
						+ df(z.getKostenKaRzg(), aggZeilen.get(z.getZeit())))
				.sum();
		epifp = rzgZeilen.stream().mapToDouble(z -> df(z.getJueVnKpRzg(), aggZeilen.get(z.getZeit()))).sum();
		zueb = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getCashflowZuebRzg(), aggZeilen.get(z.getZeit()), monat)
				+ df(z.getEndZahlung(), aggZeilen.get(z.getZeit()))).sum();
		optionen = rzgZeilen.stream()
				.mapToDouble(z -> dfVu(z.getCashflowOptionenRzg(), aggZeilen.get(z.getZeit()), monat)).sum();
		rr = rzgZeilen.stream().mapToDouble(z -> dfVu(-z.getCfRvStoch(), aggZeilen.get(z.getZeit()), monat)).sum();
		gcr = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getCfGcrRzg(), aggZeilen.get(z.getZeit()), monat)).sum();
		kbm = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getKbmRzg(), aggZeilen.get(z.getZeit()), monat)).sum();
	}

	/**
	 * @return the szenarioId
	 */
	public int getSzenarioId() {
		return szenarioId;
	}

	/**
	 * @return the pfad
	 */
	public int getPfad() {
		return pfad;
	}

	/**
	 * @return the lob
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * @return the be
	 */
	public double getBe() {
		return be;
	}

	/**
	 * @return the ueberschussFond
	 */
	public double getUeberschussFond() {
		return ueberschussFond;
	}

	/**
	 * @return the ewGar
	 */
	public double getEwGar() {
		return ewGar;
	}

	/**
	 * @return the epIfp
	 */
	public double getEpifp() {
		return epifp;
	}

	/**
	 * @return the zuep
	 */
	public double getZueb() {
		return zueb;
	}

	/**
	 * @return the optionen
	 */
	public double getOptionen() {
		return optionen;
	}

	/**
	 * @return the RR
	 */
	public double getRr() {
		return rr;
	}

	/**
	 * @return the gcr
	 */
	public double getGcr() {
		return gcr;
	}

	/**
	 * @return the kbm
	 */
	public double getKbm() {
		return kbm;
	}

	/**
	 * Ausgabe der ersten fünf Spalten des Blattes Schaetzer Mittelwerte LoB.
	 * 
	 * @param ausgabe
	 *            Ausgabedatei (voller Pfad)
	 * @param kp
	 *            Kennzahlen pfadweise zur Ermittlung der Stressszenarien
	 * @throws FileNotFoundException
	 *             bei Ausgabefehlern
	 */
	public static void writeSchaeterMittelwerteLob(final File ausgabe, final List<KennzahlenPfadweiseLoB> kp)
			throws FileNotFoundException {

		final List<Integer> szenarien = new ArrayList<>();
		final Map<Integer, List<String>> lobs = new HashMap<>();
		final Map<Integer, Set<String>> bekannt = new HashMap<>();
		for (KennzahlenPfadweiseLoB k : kp) {
			final int id = k.getSzenarioId();
			if (!bekannt.containsKey(id)) {
				szenarien.add(id);
				lobs.put(id, new ArrayList<>());
				bekannt.put(id, new HashSet<>());
			}
			final String lob = k.getLob();
			if (!bekannt.get(id).contains(lob)) {
				lobs.get(id).add(lob);
				bekannt.get(id).add(lob);
			}
		}
		try (final PrintStream out = new PrintStream(new FileOutputStream(ausgabe))) {
			writeSchaeterMittelwerteLobById(out, szenarien, lobs);
		}
	}

	private final static List<Field> ausgabeFelder = new ArrayList<>();
	private final static Map<String, String> spaltenNamen = new HashMap<>();

	static {
		char spalte1 = 'A';
		char spalte2 = ' ';
		for (Field field : KennzahlenPfadweiseLoB.class.getDeclaredFields()) {
			final TableField tableField = field.getAnnotation(TableField.class);
			if (tableField != null) {
				final String spaltenName = (spalte2 == ' ' ? "" : String.valueOf(spalte2)) + String.valueOf(spalte1);
				if (field.getType().equals(double.class)) {
					ausgabeFelder.add(field);
					spaltenNamen.put(field.getName(), spaltenName);
				}
				if (spalte1 == 'Z') {
					spalte1 = 'A';
					spalte2 = (char) (spalte2 + 1);
				} else {
					spalte1 = (char) (spalte1 + 1);
				}
			}

		}

	}

	/**
	 * Ausgabe der ersten fünf Spalten des Blattes Schaetzer Mittelwerte LoB.
	 * 
	 * @param ausgabe
	 *            Ausgabedatei (voller Pfad)
	 * @param szenarien
	 *            alle Szenarien
	 * @param szenarioLobs
	 *            Liste der Lob
	 */
	public static void writeSchaeterMittelwerteLobById(final PrintStream ausgabe, final List<Integer> szenarien,
			final Map<Integer, List<String>> szenarioLobs) {
		ausgabe.println("StressSzenario;LoB;Kennzahl;Spalte;Spalte CV");
		for (int szenario : szenarien) {
			final List<String> lobs = szenarioLobs.get(szenario);
			for (Field field : ausgabeFelder) {
				for (String lob : lobs) {
					final String cvKennzahlen = field.getAnnotation(TableField.class).cvKennzahlen();
					final String cvSpalte = KennzahlenPfadweise.getCvSpalte(cvKennzahlen);
					ausgabe.println(szenario + ";" + lob + ";" + field.getName() + ";"
							+ spaltenNamen.get(field.getName()) + ";" + cvSpalte);
				}
			}
		}
	}

}
