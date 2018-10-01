package de.gdv.bsm.vu.kennzahlen;

import static de.gdv.bsm.vu.module.DiskontFunktion.dfVu;

import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;
import de.gdv.bsm.vu.module.DiskontFunktion;

/**
 * Berechnung der Mittelwerte. Dies entspricht den Angaben im Blatt <code>StrgTab_Mittelwerte</code> in der Rubrik
 * <b>Nur CE</b>.
 * <p/>
 * Die Namen der Kenngrößen wurden übernommen und nur nach Java-Konvention verändert. Die Spaltennamen der einfließenden
 * Größen wurden durch <code>get...</code> -Funktionen im Blatt <code>rzg</code> ersetzt. Die Java-Doc gibt Auskunft
 * über die Spalten. Sollen andere Werte aus <code>agg</code> verwendet werden, sind in der Klasse {@link RzgZeile}
 * eventuell passende <code>get...</code> -Funktionen zu ergänzen. Die dortigen Angaben <code>DF</code> bzw.
 * <code>DFVU</code> wurden durch die Funktionsaufrufe {@link DiskontFunktion#df(double, AggZeile)} und
 * {@link DiskontFunktion#dfVu(double, AggZeile, int)} ersetzt. Bei Bedarf sind diese Anzupassen. Addition bzw.
 * Subtraktion wurden direkte in die Formeln übernommen.
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
public class MittelwerteNurCe {
	// Schlüsselfelder dürfen hier nicht Annotiert werden, da diese
	// später zur Zusammenfassung genutzt werden
	private final int pfad;
	private final String szenario;
	private final int szenarioId;
	private final String lob;
	private final int zeit;
	@TableField(columnName = "sämtliche garantierte Leistungen, deterministisch, diskontiert") //neue Spalte AR
	private final double aq; // sämtliche garantierte Leistungen, deterministisch; diskontiert
	@TableField(columnName = "Kosten")
	private final double l; // Kosten
	@TableField(columnName = "Prämien")
	private final double m;	// prämien
	@TableField(columnName = "sämtlich garantierte Leistungen, deterministisch, nicht diskontiert") // neue Spalte AR
	private final double aqOrig; // sämtlich garantierte Leistungen, deterministisch; nicht diskontiert

	/**
	 * Erstelle die Kennzahlen zu einem Pfad anhand der Agg-Zeilen.
	 * 
	 * @param pfad
	 *            der berechnete Pfad
	 * @param szenario
	 *            Name des berechneten Szenarios
	 * @param szenarioId
	 *            zu dem die Berechnung gehört
	 * @param lob
	 *            berechnete Line of Business
	 * @param zeit
	 *            Zeitpunkt (Jahr)
	 * @param rzgZeilen
	 *            zugehörige RZG-Zeilen
	 * @param aggZeile
	 *            zum passenden Zeitpunkt
	 * @param monat
	 *            der Zahlungsmonat aus den zeitunabhängigen Managementregeln
	 */
	public MittelwerteNurCe(final int pfad, final String szenario, final int szenarioId, final String lob,
			final int zeit, final List<RzgZeile> rzgZeilen, final AggZeile aggZeile, final double monat) {
		this.pfad = pfad;
		this.szenario = szenario;
		this.szenarioId = szenarioId;
		this.lob = lob;
		this.zeit = zeit;

		this.aq = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getLGarantiertDet(), aggZeile, monat)).sum();
		this.l = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getKosten(), aggZeile, monat)).sum();
		this.m = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getPraemien(), aggZeile, monat)).sum();
		this.aqOrig = rzgZeilen.stream().mapToDouble(z -> z.getLGarantiertDet()).sum();
	}

	/**
	 * @return the pfad
	 */
	public int getPfad() {
		return pfad;
	}

	/**
	 * @return the szenario
	 */
	public String getSzenario() {
		return szenario;
	}

	/**
	 * @return the szenarioId
	 */
	public int getSzenarioId() {
		return szenarioId;
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
	 * @return the aq
	 */
	public double getAq() {
		return aq;
	}

	/**
	 * @return the l
	 */
	public double getL() {
		return l;
	}

	/**
	 * @return the m
	 */
	public double getM() {
		return m;
	}

	/**
	 * @return the aqOrig
	 */
	public double getAqOrig() {
		return aqOrig;
	}

}
