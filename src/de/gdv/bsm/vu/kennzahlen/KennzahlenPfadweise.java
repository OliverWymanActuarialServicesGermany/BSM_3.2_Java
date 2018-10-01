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
import java.util.List;
import java.util.Map;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.berechnung.AggZeile;

/**
 * Berechnung der Mittelwerte. Dies entspricht den Angaben im Blatt <code>StrgTab_Mittelwerte</code> in der Rubrik
 * <b>Pfadweise Ausgabe</b>.
 * <p/>
 * Die Namen wurden übernommen und nur nach Java-Konvention verändert. Die Spaltennamen wurden durch <code>get...</code>
 * -Funktionen im Blatt <code>agg</code> ersetzt. Sollen andere Werte aus <code>agg</code> verwendet werden, sind in der
 * Klasse {@link AggZeile} eventuell passende <code>get...</code>-Funktionen zu ergänzen. Die dortigen Angaben
 * <code>DF</code> bzw. <code>DFVU</code> wurden durch die Funktionsaufrufe <code>df</code> und <code>dfVu</code>
 * ersetzt. Addition bzw. Subtraktion wurden direkte in die Formeln übernommen.
 * <p/>
 * 
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
public class KennzahlenPfadweise {
	@TableField
	private final int szenarioId;
	@TableField
	private final int pfad;
	@TableField(nachKomma = 0)
	private final double zag;
	@TableField(nachKomma = 0)
	private final double be;
	@TableField(nachKomma = 2)
	private final double gcr;
	@TableField
	private final double grnd;
	@TableField
	private final double steuer;
	@TableField(nachKomma = 0)
	private final double mwPassiva;
	@TableField(nachKomma = 0)
	private final double ueberschussFond;
	@TableField(nachKomma = 0)
	private final double ewGar;
	@TableField(nachKomma = 2)
	private final double rr;
	@TableField(nachKomma = 2)
	private final double epifp;
	@TableField(nachKomma = 2)
	private final double kbm;
	@TableField(nachKomma = 2)
	private final double zueb;
	@TableField(nachKomma = 2)
	private final double optionen;

	/**
	 * Erstelle die Kennzahlen zu einem Pfad anhand der Agg-Zeilen.
	 * 
	 * @param szenarioId
	 *            zu dem die Berechnung gehört
	 * @param pfad
	 *            der Berechnung
	 * @param aggZeilen
	 *            über alle Zeiten
	 * @param monat
	 *            der Zahlungsmonat aus den zeitunabhängigen Managementregeln
	 */
	public KennzahlenPfadweise(final int szenarioId, final int pfad, final List<AggZeile> aggZeilen,
			final double monat) {
		this.szenarioId = szenarioId;
		this.pfad = pfad;
		zag = aggZeilen.stream().mapToDouble(z -> dfVu(z.getZagFaellig(), z, monat) + df(z.getZagEndzahlung(), z))
				.sum();

		be = aggZeilen.stream()
				.mapToDouble(z -> dfVu(z.getLGesAgg(), z, monat) + df(z.getEndZahlungAgg(), z)
						- dfVu(z.getBStochAgg(), z, monat) + dfVu(z.getKStochAgg(), z, monat)
						+ df(z.getAufwendungenKa(), z))
				.sum();

		gcr = aggZeilen.stream().mapToDouble(z -> dfVu(z.getGcrUeB(), z, monat)).sum();
		grnd = aggZeilen.stream().mapToDouble(z -> df(z.getRueckZahlung(), z) + df(z.getZinsen(), z)).sum();
		steuer = aggZeilen.stream().mapToDouble(z -> df(z.getErtragsSteuerLs(), z)).sum();
		mwPassiva = aggZeilen.stream()
				.mapToDouble(z -> dfVu(z.getZagFaellig(), z, monat) + df(z.getZagEndzahlung(), z)
						+ dfVu(z.getLGesAgg(), z, monat) + df(z.getEndZahlungAgg(), z)
						- dfVu(z.getBStochAgg(), z, monat) + dfVu(z.getKStochAgg(), z, monat)
						+ df(z.getAufwendungenKa(), z) + dfVu(z.getGcrUeB(), z, monat) + df(z.getRueckZahlung(), z)
						+ df(z.getZinsen(), z) + df(z.getErtragsSteuerLs(), z) + dfVu(z.getCfRvstochAgg(), z, monat))
				.sum();
		ueberschussFond = aggZeilen.stream().mapToDouble(z -> df(z.getCashflowSf(), z)).sum();
		ewGar = aggZeilen.stream().mapToDouble(z -> dfVu(z.getLGarAgg(), z, monat) + dfVu(z.getKAgg(), z, monat)
				- dfVu(z.getBAgg(), z, monat) + df(z.getAufwendungenKa(), z)).sum();
		rr = aggZeilen.stream().mapToDouble(z -> -dfVu(z.getCfRvstochAgg(), z, monat)).sum();
		epifp = aggZeilen.stream().mapToDouble(z -> df(z.getJueVnKP(), z)).sum();
		kbm = aggZeilen.stream().mapToDouble(z -> dfVu(z.getCashflowGesamt(), z, monat)).sum();
		zueb = aggZeilen.stream().mapToDouble(z -> dfVu(z.getZuebCashflowAgg(), z, monat) + df(z.getEndZahlungAgg(), z))
				.sum();
		optionen = aggZeilen.stream().mapToDouble(z -> dfVu(z.getOptionenCashflowAgg(), z, monat)).sum();

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
	 * @return the zagKlassik
	 */
	public double getZag() {
		return zag;
	}

	/**
	 * @return the be
	 */
	public double getBe() {
		return be;
	}

	/**
	 * @return the grcKlassik
	 */
	public double getGrcKlassik() {
		return gcr;
	}

	/**
	 * @return the grnd
	 */
	public double getGrnd() {
		return grnd;
	}

	/**
	 * @return the steuer
	 */
	public double getSteuer() {
		return steuer;
	}

	/**
	 * @return the mwPassiva
	 */
	public double getMwPassiva() {
		return mwPassiva;
	}

	/**
	 * @return the ueberschussFond
	 */
	public double getUeberschussFond() {
		return ueberschussFond;
	}

	/**
	 * @return the epIfp
	 */
	public double getEpIfp() {
		return epifp;
	}

	/**
	 * @return the GCR
	 */
	public double getGcr() {
		return gcr;
	}

	/**
	 * @return the EW_Gar
	 */
	public double getEwGar() {
		return ewGar;
	}

	/**
	 * @return the RR
	 */
	public double getRr() {
		return rr;
	}

	/**
	 * @return the epKBM
	 */
	public double getKbm() {
		return kbm;
	}

	/**
	 * @return the ZUEB
	 */
	public double getZueb() {
		return zueb;
	}

	/**
	 * @return the Optionen
	 */
	public double getOptionen() {
		return optionen;
	}

	/**
	 * Ausgabe der ersten vier Spalten des Blattes Schaetzer Mittelwerte.
	 * 
	 * @param ausgabe
	 *            Ausgabedatei (voller Pfad)
	 * @param kp
	 *            Kennzahlen pfadweise zur Ermittlung der Stressszenarien
	 * @throws FileNotFoundException
	 *             bei Ausgabefehlern
	 */
	public static void writeSchaeterMittelwerte(final File ausgabe, final List<KennzahlenPfadweise> kp)
			throws FileNotFoundException {
		final List<Integer> szenarien = new ArrayList<>();
		int lastValue = Integer.MIN_VALUE;
		for (KennzahlenPfadweise k : kp) {
			final int id = k.getSzenarioId();
			if (id != lastValue) {
				szenarien.add(id);
				lastValue = id;
			}
		}
		try (final PrintStream out = new PrintStream(new FileOutputStream(ausgabe))) {
			writeSchaeterMittelwerteById(out, szenarien);
		}
	}

	private final static List<Field> ausgabeFelder = new ArrayList<>();
	private final static Map<String, String> spaltenNamen = new HashMap<>();

	static {
		char spalte1 = 'A';
		char spalte2 = ' ';
		for (Field field : KennzahlenPfadweise.class.getDeclaredFields()) {
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
	 * Ausgabe der ersten vier Spalten des Blattes Schaetzer Mittelwerte.
	 * 
	 * @param ausgabe
	 *            Ausgabedatei (voller Pfad)
	 * @param szenarien
	 *            Liste der auszugebenden Szenarien
	 */
	public static void writeSchaeterMittelwerteById(final PrintStream ausgabe, final List<Integer> szenarien) {
		ausgabe.println("StressSzenario;Kennzahl;Spalte;Spalte CV");
		for (int szenario : szenarien) {
			for (Field field : ausgabeFelder) {
				final String cvKennzahlen = field.getAnnotation(TableField.class).cvKennzahlen();
				final String cvSpalte = getCvSpalte(cvKennzahlen);
				ausgabe.println(
						szenario + ";" + field.getName() + ";" + spaltenNamen.get(field.getName()) + ";" + cvSpalte);
			}
		}
	}

	/**
	 * Ermittle zu einem Feldnamen den Spaltennamen. Das Feld muss in dieser Klasse definiert sein, die Annotation
	 * {@link TableField} besitzen und vom Typ double sein.
	 * 
	 * @param cvName
	 *            der Feldname
	 * @return die Spalte in Excel (A, B, ..., AA, AB, ...)
	 */
	public static String getCvSpalte(final String cvName) {
		if (!spaltenNamen.containsKey(cvName)) {
			throw new IllegalArgumentException("Der Spaltenname " + cvName + " ist in KennzahlenPfadweise unbekannt.");
		}
		return spaltenNamen.get(cvName);
	}
}
