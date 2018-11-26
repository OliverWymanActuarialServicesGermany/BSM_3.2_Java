package de.gdv.bsm.vu.kennzahlen;

import static de.gdv.bsm.vu.module.DiskontFunktion.df;
import static de.gdv.bsm.vu.module.DiskontFunktion.dfVu;

import java.util.List;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;
import de.gdv.bsm.vu.module.DiskontFunktion;

/**
 * Berechnung der Mittelwerte. Dies entspricht den Angaben im Blatt <code>StrgTab_Mittelwerte</code> in der Rubrik
 * <b>Mittelwerte und CE</b>.
 * <p/>
 * Die Namen der Kenngr��en wurden �bernommen und nur nach Java-Konvention ver�ndert. Die Spaltennamen der einflie�enden
 * Gr��en wurden durch <code>get...</code> -Funktionen im Blatt <code>rzg</code> ersetzt. Die Java-Doc gibt Auskunft
 * �ber die Spalten. Sollen andere Werte aus <code>agg</code> verwendet werden, sind in der Klasse {@link RzgZeile}
 * eventuell passende <code>get...</code> -Funktionen zu erg�nzen. Die dortigen Angaben <code>DF</code> bzw.
 * <code>DFVU</code> wurden durch die Funktionsaufrufe {@link DiskontFunktion#df(double, AggZeile)} und
 * {@link DiskontFunktion#dfVu(double, AggZeile, int)} ersetzt. Bei Bedarf sind diese Anzupassen. Addition bzw.
 * Subtraktion wurden direkte in die Formeln �bernommen.
 * <p/>
 * <h4>Rechtliche Hinweise</h4>
 * 
 * Das Simulationsmodell ist ein kostenfreies Produkt des GDV, das nach bestem Wissen und Gewissen von den zust�ndigen
 * Mitarbeitern entwickelt wurde. Trotzdem ist nicht auszuschlie�en, dass sich Fehler eingeschlichen haben oder dass die
 * Berechnungen unter speziellen Datenbedingungen fehlerbehaftet sind. Entsprechende R�ckmeldungen w�rde der GDV
 * begr��en. Der GDV �bernimmt aber keine Haftung f�r die fehlerfreie Funktionalit�t des Modells oder den korrekten
 * Einsatz im Unternehmen.
 * <p/>
 * Alle Inhalte des Simulationsmodells einschlie�lich aller Tabellen, Grafiken und Erl�uterungen sind urheberrechtlich
 * gesch�tzt. Die ausschlie�lichen Nutzungsrechte liegen beim Gesamtverband der Deutschen Versicherungswirtschaft e.V.
 * (GDV).
 * <p/>
 * <b>Simulationsmodell � GDV 2016</b>
 */
public class MittelwerteUndCe {
	// Schl�sselfelder d�rfen hier nicht Annotiert werden, da diese
	// sp�ter zur Zusammenfassung genutzt werden
	private final int pfad;
	private final String szenario;
	private final int szenarioId;
	private final String lob;
	private final int zeit;
	@TableField(columnName="Kosten")	
	private final double l; //Kosten
	@TableField(columnName="Leistungen beim Tod")	
	private final double n; //Leistungen beim Tod
	@TableField(columnName="Kapitalabfindungen, nur Rentenversicherung")	
	private final double o;//Kapitalabfindungen, nur Rentenversicherung
	@TableField(columnName="Sonstige Erlebensfallleistungen")	
	private final double p;//Sonstige Erlebensfallleistungen
	@TableField(columnName="R�ckkauf")	
	private final double q;//R�ckkauf
	@TableField(columnName="HGB DRst inkl. Ansammlungsguthaben ohne ZZR")	
	private final double w;//HGB DRst inkl. Ansammlungsguthaben ohne ZZR
	@TableField(columnName="Aufwendungen f�r KA, anteilsm��ig")
	private final double bv; // Aufwendungen f�r KA, anteilsm��ig, neu BW
	@TableField(columnName="Leistungen Gesamt")
	private final double bj; // Leistungen Gesamt, neu BK
	@TableField(columnName="Leistungen durch Endzahlung")
	private final double bk; // Leistungen durch Endzahlung, neu BL
	@TableField(columnName="Beitr�ge stochastisch")
	private final double af; // Beitr�ge stochastisch, neu AG
	@TableField(columnName="Kosten stochastisch")
	private final double ag; // Kosten stochastisch, neu AH
	@TableField(columnName="CF EVU�-> RVU (nicht LE-abh�ngig), stochastisch, diskontiert")
	private final double ak; // CE EVU�-> RVU (nicht LE-abh�ngig), stochastisch; diskontiert, neu AL
	@TableField(columnName="FI Buchwert gesamt")	
	private final double br;//FI Buchwert gesamt
	/**
	 * Erstelle die Kennzahlen zu einem Pfad anhand der Agg-Zeilen.
	 * 
	 * @param pfad
	 *            der berechnete Pfad
	 * @param szenario
	 *            Name des berechneten Szenarios
	 * @param szenarioId
	 *            zu dem die Berechnung geh�rt
	 * @param lob
	 *            berechnete LoB
	 * @param zeit
	 *            in Jahren
	 * @param rzgZeilen
	 *            die zu dieser Zeit geh�ren
	 * @param aggZeile
	 *            passen zu den rzgZeilen
	 * @param monat
	 *            der Zahlungsmonat aus den zeitunabh�ngigen Managementregeln
	 */
	public MittelwerteUndCe(final int pfad, final String szenario, final int szenarioId, final String lob,
			final int zeit, final List<RzgZeile> rzgZeilen, final AggZeile aggZeile, final double monat) {
		this.pfad = pfad;
		this.szenario = szenario;
		this.szenarioId = szenarioId;
		this.lob = lob;
		this.zeit = zeit;

		this.l = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getKosten(), aggZeile, monat)).sum();
		this.n = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getlTod(), aggZeile, monat)).sum();
		this.o = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getlKa(), aggZeile, monat)).sum();		
		this.p = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getsonstigeErlebensfallLeistungen(), aggZeile, monat)).sum();		
		this.q = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getlRkw(), aggZeile, monat)).sum();	
		this.w = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getDrDet(), aggZeile, monat)).sum();	
		this.bv = rzgZeilen.stream().mapToDouble(z -> df(z.getKostenKaRzg(), aggZeile)).sum();
		this.bj = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getLGesamt(), aggZeile, monat)).sum();
		this.bk = rzgZeilen.stream().mapToDouble(z -> df(z.getEndZahlung(), aggZeile)).sum();
		this.af = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getBeitraegeStoch(), aggZeile, monat)).sum();
		this.ag = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getKostenStoch(), aggZeile, monat)).sum();
		this.ak = rzgZeilen.stream().mapToDouble(z -> dfVu(z.getCfRvStoch(), aggZeile, monat)).sum();
		this.br = aggZeile.getfiMw();
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
	 * @return the l
	 */
	public double getL() {
		return l;
	}
	
	/**
	 * @return the n
	 */
	public double getN() {
		return n;
	}

	/**
	 * @return the o
	 */
	public double getO() {
		return o;
	}
	
	/**
	 * @return the p
	 */
	public double getP() {
		return p;
	}
	
	/**
	 * @return the q
	 */
	public double getQ() {
		return q;
	}
	
	/**
	 * @return the w
	 */
	public double getW() {
		return w;
	}
	
	/**
	 * @return the bv
	 */
	public double getBv() {
		return bv;
	}

	/**
	 * @return the bj
	 */
	public double getBj() {
		return bj;
	}

	/**
	 * @return the bk
	 */
	public double getBk() {
		return bk;
	}

	/**
	 * @return the af
	 */
	public double getAf() {
		return af;
	}

	/**
	 * @return the ag
	 */
	public double getAg() {
		return ag;
	}

	/**
	 * @return the ak
	 */
	public double getAk() {
		return ak;
	}
	
	/**
	 * @return the br
	 */
	public double getBr() {
		return br;
	}

}
