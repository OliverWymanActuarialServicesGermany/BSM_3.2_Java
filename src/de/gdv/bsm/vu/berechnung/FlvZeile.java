package de.gdv.bsm.vu.berechnung;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.params.VtFlvZeile;
import de.gdv.bsm.vu.module.Flv;

/**
 * Eine Zeile des Blattes FLV.
 * 
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
public class FlvZeile {
	/** Zugrundeliegende Berechnung. */
	final Berechnung berechnung;
	/** Vorgänger (chronologisch) dieser Zeile */
	final FlvZeile vg;

	/** Stressszenario. A, L 0. */
	@TableField
	final String szenario;
	/** Stressszenario ID. B, L 0. */
	@TableField
	final int szenarioId;
	/** LoB. C, L 0. */
	@TableField
	final String lob;
	/** Zeit. D, L 0. */
	@TableField
	public final int zeit;
	/** Rechnungszinsgeneration. E, L 0. */
	@TableField
	final int zinsGeneration;
	/** Alt-/Neubestand (a / n). F, L 0. */
	@TableField
	final String altNeu;
	/** ÜB/NÜB. G, L 0. */
	@TableField
	final String uebNueb;
	/** Kosten VU, Aufschubzeit, Euro. H, L 0. */
	@TableField
	final double kosten;
	/** Prämien (Brutto, in den Fonds), Aufschubzeit, Euro. I, L 0. */
	@TableField
	final double praemien;
	/** Leistungen bei Tod (aus Fonds), Aufschubzeit, Euro. J, L 0. */
	@TableField
	final double tod;
	/** Sonstige Erlebensfallleistungen, Aufschubzeit, Euro. K, L 0. */
	@TableField
	final double sonstigeLeistungen;
	/** Kapitalabfindungen, nur Rentenversicherung, Aufschubzeit, Euro. L, L 0. */
	@TableField
	final double kapitalAbfindungen;
	/** Rückkauf, Aufschubzeit, Euro. M, L 0. */
	@TableField
	final double rueckKauf;
	/** Riskoergebnis FLV, Aufschubzeit, Euro. N, L 0. */
	@TableField
	final double risikoErgebnis;
	/** Übriges Ergebnis - FLV, Aufschubzeit, Euro. O, L 0. */
	@TableField
	final double uebrigesErgebnis;
	/** Kosten der Fondsverwaltung (bei 2 % Wertentwicklung vor Kosten), Euro. P, L 0. */
	@TableField
	final double kostenFondsverwaltung;
	/** Aktienanteil im Fond, %. Q, L 0. */
	@TableField
	final double aktienAnteil;
	/** Immobilienanteil im Fond, %. R, L 0. */
	@TableField
	final double immobilienAnteil;
	/** FI-anteil im Fond, %. S, L 0. */
	@TableField
	final double fiAnteil;
	/** Fondsentwicklung, Euro (bei 2 % Wertentwicklung vor Kosten für Fondsverwaltung). T, L 0. */
	@TableField
	final double fondsEntwicklung;
	/** Prämien = Verrentendes Kapital (aus Ende Aufschub des Fonds), Euro. U, L 0. */
	@TableField
	final double praemienVerrentendesKapital;
	/** Einjährige Wertentwicklung des Fonds. V, L 1. */
	@TableField(nachKomma = 6)
	double wertEntwicklungStoch = AggZeile.DOUBLE_INIT;
	/** Fondguthaben, stochastisch, vor Beitragt (Rente). W, L 1. */
	@TableField
	double fondguthabenStochZp = AggZeile.DOUBLE_INIT;
	/** Fondguthaben, stochastisch. X, L 1. */
	@TableField
	double fondguthaben = AggZeile.DOUBLE_INIT;
	/** Relative Fondentwicklung, stochastisch. Y, L 1. */
	@TableField(nachKomma = 2)
	double fondentwicklungRelStochVuZp = AggZeile.DOUBLE_INIT;
	/** Leistungen bei Tod (aus Fonds), stochastisch. Z, L 1. */
	@TableField
	double lTodAufschubStoch = AggZeile.DOUBLE_INIT;
	/** Sonstige Erlebens-fallleistungen, stochastisch. AA, L 1. */
	@TableField
	double lSonstErlAufschubStoch = AggZeile.DOUBLE_INIT;
	/** Kapitalabfindungen, nur Rentenversicherung, stochastisch. AB, L 1. */
	@TableField
	double lKaAufschubStoch = AggZeile.DOUBLE_INIT;
	/** Rückkauf, stochastisch. AC, L 1. */
	@TableField
	double lRkwAufschubStoch = AggZeile.DOUBLE_INIT;
	/** Kosten der Fondsverwaltung, stochastisch. AD, L 1. */
	@TableField
	double kFvAufschubStoch = AggZeile.DOUBLE_INIT;
	/** Beitrag Rente, stochastisch. AE, L 1. */
	@TableField
	double beitragRenteStoch = AggZeile.DOUBLE_INIT;

	/**
	 * Erstelle eine Zeile aus den Vorgaben.
	 * 
	 * @param berechnung
	 *            zugrundeliegende Berechnung
	 * @param zeile
	 *            die Vorgaben
	 * @param vg
	 *            der chronologische Vorgänger
	 */
	public FlvZeile(final Berechnung berechnung, final VtFlvZeile zeile, final FlvZeile vg) {
		this.berechnung = berechnung;
		this.vg = vg;
		szenario = berechnung.szenarioName;
		szenarioId = berechnung.szenarioId;
		lob = zeile.getLob();
		zeit = zeile.getZeit();
		zinsGeneration = zeile.getRechnungsZinsGeneration();
		altNeu = zeile.getAltNeuBestand();
		uebNueb = berechnung.lobMapping.getLobMapping(lob).getUebNueb();
		kosten = zeile.getKostenVuAufschubzeit();
		praemien = zeile.getPraemienAufschubzeit();
		tod = zeile.getLeistungenBeiTodAufschubzeit();
		sonstigeLeistungen = zeile.getSonstigeErlebensFallLeistungenAufschubzeit();
		kapitalAbfindungen = zeile.getKapitalabfindungenAufschubzeit();
		rueckKauf = zeile.getRueckkaufAufschubzeit();
		risikoErgebnis = zeile.getRiskoergebnisFlvAufschubzeit();
		uebrigesErgebnis = zeile.getUebrigesErgebnisFlvAufschubzeit();
		kostenFondsverwaltung = zeile.getKostenderFondsverwaltung();
		aktienAnteil = zeile.getAktienanteilImFond();
		immobilienAnteil = zeile.getImmobilienanteilImFond();
		fiAnteil = zeile.getFiAnteilImFond();
		fondsEntwicklung = zeile.getFondsentwicklung();
		praemienVerrentendesKapital = zeile.getPraemienVerrentendesKapital();
	}

	/**
	 * Berechnung dieser Zeile.
	 * 
	 * @param vgAgg
	 *            Vorgängerzeile aus {@link AggZeile}.
	 */
	public void rechnen(AggZeile vgAgg) {
		final double monat = berechnung.getZeitunabhManReg().getMonatZahlung();

		{
			final double kuponEsgII = vgAgg == null ? Double.NaN : vgAgg.kuponEsgII;
			wertEntwicklungStoch = Flv.wertEntwicklungStoch(kuponEsgII);
		}

		if (zeit == 0) {
			fondguthaben = Flv.fondguthabenDet(fondsEntwicklung);
		} else {
			fondentwicklungRelStochVuZp = Flv.fondentwicklungRelStochVuZp(vg.fondsEntwicklung, vg.fondguthaben,
					berechnung.getZeitabhManReg().get(zeit).getDetProjektionFlv(), wertEntwicklungStoch, monat);
			lTodAufschubStoch = Flv.lTodAufschubStoch(tod, fondentwicklungRelStochVuZp);
			lSonstErlAufschubStoch = Flv.lSonstErlAufschubStoch(sonstigeLeistungen, fondentwicklungRelStochVuZp);
			lKaAufschubStoch = Flv.lKaAufschubStoch(kapitalAbfindungen, fondentwicklungRelStochVuZp);
			lRkwAufschubStoch = Flv.lRkwAufschubStoch(rueckKauf, fondentwicklungRelStochVuZp);
			kFvAufschubStoch = Flv.kFvAufschubStoch(kostenFondsverwaltung, vg.fondsEntwicklung, vg.fondguthaben,
					berechnung.getZeitabhManReg().get(zeit).getDetProjektionFlv(), wertEntwicklungStoch);

			fondguthabenStochZp = Flv.fondguthabenStochZp(vg.fondguthaben, wertEntwicklungStoch, praemien, kosten,
					kFvAufschubStoch, lTodAufschubStoch, lSonstErlAufschubStoch, lKaAufschubStoch, lRkwAufschubStoch,
					risikoErgebnis, uebrigesErgebnis, monat);
			beitragRenteStoch = Flv.beitragRenteStoch(praemienVerrentendesKapital, fondguthabenStochZp,
					fondsEntwicklung, berechnung.getZeitabhManReg().get(zeit).getDetProjektionFlv(), zeit, monat);

			fondguthaben = Flv.fondguthabenStoch(fondguthabenStochZp, wertEntwicklungStoch, beitragRenteStoch, monat);
		}
	}

	/**
	 * Text mit den Schlüsseln dieser Zeile.
	 * 
	 * @return der Text
	 */
	public String keyText() {
		final String text = "Szenario " + szenarioId + ", LoB " + lob + ", zeit " + zeit + ", ZinsGen "
				+ zinsGeneration;
		return text;
	}

	/**
	 * Zeitlicher Vorgänger dieser Zeile.
	 * 
	 * @return der Vorgänger
	 */
	public FlvZeile getVg() {
		return vg;
	}

	/**
	 * LoB. C.
	 * 
	 * @return der Wert
	 */
	public String getLob() {
		return lob;
	}

	/**
	 * Rechnungszinsgeneration. E.
	 * 
	 * @return der Wert
	 */
	public int getZinsGeneration() {
		return zinsGeneration;
	}

	/**
	 * Alt-/Neubestand (a / n). F.
	 * 
	 * @return der Wert
	 */
	public String getAltNeu() {
		return altNeu;
	}

	/**
	 * Fondguthaben, stochastisch, vor Beitragt (Rente). W.
	 * 
	 * @return der Wert
	 */
	public double getFondguthabenStochZp() {
		return fondguthabenStochZp;
	}

	/**
	 * Kapitalabfindungen, nur Rentenversicherung, stochastisch. AB.
	 * 
	 * @return der Wert
	 */
	public double getlKaAufschubStoch() {
		return lKaAufschubStoch;
	}

	/**
	 * Beitrag Rente, stochastisch. AE
	 * 
	 * @return beitragRenteStoch
	 */
	public double getBeitragRenteStoch() {
		return beitragRenteStoch;
	}

	@Override
	public String toString() {
		return "Flv[" + szenarioId + "," + lob + "," + zeit + "," + zinsGeneration + "]";
	}
}
