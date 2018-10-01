package de.gdv.bsm.vu.berechnung;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.applic.TableField.TestOption;
import de.gdv.bsm.intern.params.BwAktivaFiZeile;
import de.gdv.bsm.vu.module.KaModellierung;

/**
 * Implementierung einer Zeile des Blattes <code>FI Ausfall</code>.
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
public class FiAusfallZeile {
	final BwAktivaFiZeile bwAktivaFiZeile;

	/** Stressszenario. A, L 0. */
	@TableField(testOption = TestOption.NO)
	final String stressSzenario;
	/** StressszenarionId. B, L 0. */
	@TableField(testOption = TestOption.NO)
	final int stressSzenarionId;
	/** Risikokategorie. C, L 0. */
	@TableField
	final String kategorie;
	/** Zeit. D, L 0. */
	@TableField
	public final int zeit;
	/** FI Buchwert, akt. Bestand, Klassik, korrigiert um SP-SA-Verrechnung. E, L 0. */
	@TableField
	final double cashflowFi;
	/** FI Buchwert, akt. Bestand, Klassik, korrigiert um SP-SA-Verrechnung. F, L 0. */
	@TableField
	final double ertrag;
	/** FI Buchwert, akt. Bestand, Klassik, korrigiert um SP-SA-Verrechnung. G, L 0. */
	@TableField
	final double bwFiAktoAusfall;
	/** Cashflow FI, korrigiert um SP-SA-Verrechnung. H, L 0. */
	@TableField
	final double cfFioAusfall;
	/** Handelsrechtlicher Ertrag, ohne RAP, korrigiert um SP-SA-Verrechnung. I, L 0. */
	@TableField
	final double kfFioAusfall;
	/** Spot. J, L 0. */
	@TableField(percent = true, nachKomma = 6)
	final double spot;
	/** Diskontfaktor. K, L 0. */
	@TableField(percent = true, nachKomma = 2)
	final double df;
	/** CF mit Berücksichtigung des Ausfalls. L, L 1. */
	@TableField
	double cfFimitAusfall = Double.NaN;
	/** Handelrechtlicher Kapitalertrag mit Abschreibung wg. Ausfall. M, L 1. */
	@TableField
	double kemitAusfallAbschr = Double.NaN;
	/** Aufzinsung zum Endes des Jahres. N, L 0. */
	@TableField(percent = true, nachKomma = 2)
	final double dfHalbesjahr;
	/** CF mit Berücksichtigung des Ausfalls, mit Aufzinsung. O, L 1. */
	@TableField
	double cfFimitAusfallJahresende = Double.NaN;
	/** KE vor Berücksichtigung der Rechnungsabgrenzungsposten, mit Abschreibung. P, L 1. */
	@TableField
	double keAktBestandJe = Double.NaN;
	/** Rechnunsabgrenzungsposten: Stückzinsen aus dem Vorjahr. Q, L 1. */
	@TableField
	double rapZinsen = Double.NaN;
	/** KE mit Abschreibung wg. Ausfall, mit Aufzinsung. R, L 1. */
	@TableField
	double keaktBestandJeR = Double.NaN;

	/**
	 * Erstellen einer Zeile und Berechnung deer Grundgrößen.
	 * 
	 * @param berechnung
	 *            zugrundeliegende Berechnung
	 * @param vg
	 *            chronologischer Vorgänger
	 * @param zeit
	 *            Zeitschritt dieser Instanz
	 * @param bwAktivaFiZeile
	 *            bereits ausreichend berechnete BW-Aktiva
	 */
	public FiAusfallZeile(final Berechnung berechnung, final FiAusfallZeile vg, final int zeit,
			final BwAktivaFiZeile bwAktivaFiZeile) {
		this.stressSzenario = berechnung.szenarioName;
		this.stressSzenarionId = berechnung.szenarioId;
		this.kategorie = bwAktivaFiZeile.getRisikoKategorie();
		this.zeit = zeit;
		this.cashflowFi = bwAktivaFiZeile.getCashflowFi();
		this.ertrag = bwAktivaFiZeile.getErtrag();

		this.bwAktivaFiZeile = bwAktivaFiZeile;

		if (zeit == 1) {
			bwFiAktoAusfall = KaModellierung.bwFiAktoAusfall(zeit, berechnung.fiBuchwertBestand, berechnung.cfFiAkt, 0,
					0, 0);
		} else {
			bwFiAktoAusfall = KaModellierung.bwFiAktoAusfall(zeit, berechnung.fiBuchwertBestand, berechnung.cfFiAkt,
					vg.bwFiAktoAusfall, vg.cfFioAusfall, vg.kfFioAusfall);
		}

		cfFioAusfall = KaModellierung.cfFioAusfall(cashflowFi, berechnung.cfFiAkt);
		kfFioAusfall = KaModellierung.kfFioAusfall(ertrag, berechnung.cfFiAkt);

		spot = berechnung.szenario.getZskSzenario(zeit);
		final double vuZeitpunkt = berechnung.getZeitunabhManReg().getFaelligkeitZinstitel();
		if (zeit == 1) {
			df = KaModellierung.df(0.0, spot, zeit, vuZeitpunkt);
			dfHalbesjahr = KaModellierung.dfHalbesjahr(0, spot, zeit, vuZeitpunkt);
		} else {
			df = KaModellierung.df(vg.spot, spot, zeit, vuZeitpunkt);
			dfHalbesjahr = KaModellierung.dfHalbesjahr(vg.spot, spot, zeit, vuZeitpunkt);
		}
	}

	/**
	 * Berechnung chronologisch rückwärts. Achtung: diese Berechnung muss cronologisch absteigend aufgerufen werden!
	 * 
	 * @param berechnung
	 *            Master für die Berechnung
	 * @param nf
	 *            chronologischer Nachfolger
	 */
	public void berechnungLevel01(final Berechnung berechnung, final FiAusfallZeile nf) {
		final double vuZeitpunkt = berechnung.getZeitunabhManReg().getFaelligkeitZinstitel();
		final double q = berechnung.q;

		cfFimitAusfall = KaModellierung.cfFiMitAusfall(cfFioAusfall, q, zeit, vuZeitpunkt);

		kemitAusfallAbschr = KaModellierung.kemitAusfallAbschr(kfFioAusfall, bwFiAktoAusfall, q, zeit, vuZeitpunkt);

		cfFimitAusfallJahresende = KaModellierung.cfFimitAusfallJahresende(cfFimitAusfall, dfHalbesjahr);

		keAktBestandJe = KaModellierung.keaktBestandJe(kemitAusfallAbschr, cfFimitAusfall, cfFimitAusfallJahresende,
				0.0, 0.0);

		rapZinsen = KaModellierung.rapZinsen(zeit, vuZeitpunkt, berechnung.hgbBilanzdaten.getRapZinsenBuchwert(),
				kfFioAusfall, q, berechnung.getZeitunabhManReg().getAbgrenzungImBsmRechnen());

		final double rapNf = nf == null ? 0.0 : nf.rapZinsen;
		keaktBestandJeR = KaModellierung.keaktBestandJe(kemitAusfallAbschr, cfFimitAusfall, cfFimitAusfallJahresende,
				rapZinsen, rapNf);

	}

	/**
	 * Cashflow FI. E, L 0.
	 * 
	 * @return die Zahl
	 */
	public double getCashflowFi() {
		return bwAktivaFiZeile.getCashflowFi();
	}

	/**
	 * Handelsrechtlicher Ertrag. F, L 0.
	 * 
	 * @return die Zahl
	 */
	public double getErtrag() {
		return bwAktivaFiZeile.getErtrag();
	}
}
