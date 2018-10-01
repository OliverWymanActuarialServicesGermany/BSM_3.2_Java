package de.gdv.bsm.intern.applic;

import java.util.List;
import java.util.TreeMap;

import de.gdv.bsm.intern.rechnung.KennzahlenMittelung;
import de.gdv.bsm.intern.rechnung.KennzahlenMittelungLoB;
import de.gdv.bsm.intern.rechnung.Mittelwerte;
import de.gdv.bsm.vu.berechnung.Berechnung;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;

/**
 * Das Resultat einer Berechnung.
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
public class BerechnungResultat {
	private final Berechnung letzteBerechnung;
	private final TreeMap<Integer, TreeMap<Integer, KennzahlenPfadweise>> kennzahlenPfadweise;
	// Deprecated: private final TreeMap<Integer, TreeMap<Integer, List<KennzahlenPfadweiseLoB>>> kennzahlenPfadweiseLoB;
	private final TreeMap<Integer, TreeMap<String, TreeMap<Integer,KennzahlenPfadweiseLoB>>> kennzahlenPfadweiseLoB;
	private final List<Mittelwerte> mittelwerte;
	private final TreeMap<Integer, TreeMap<String,KennzahlenMittelung>> schaetzerMittelwerte;
	private final TreeMap<Integer, TreeMap<String,TreeMap<String,KennzahlenMittelungLoB>>> schaetzerMittelwerteLoB;
	/**
	 * Erstelle das Resultat einer Berechnung zwecks Anzeige.
	 * 
	 * @param letzteBerechnung
	 *            Daten der letzten Berechnung
	 * @param kennzahlenPfadweise
	 *            die Pfadweise ermittelten Kennzeichen
	 * @param kennzahlenPfadweiseLoB
	 *            die ermittelten Kennzahlen pro Line of Business
	 * @param mittelwerte
	 *            die Ermittelten Mittelwerte pro Zeit (alle Pfade)
	 * @param schaetzerMittelwerte die Ermittelten Mittelwerte
	 * @param schaetzerMittelwerteLoB die Ermittelten Mittelwerte pro LoB 
	 */
	public BerechnungResultat(final Berechnung letzteBerechnung,
			final TreeMap<Integer, TreeMap<Integer, KennzahlenPfadweise>> kennzahlenPfadweise,
			final TreeMap<Integer, TreeMap<String, TreeMap<Integer,KennzahlenPfadweiseLoB>>> kennzahlenPfadweiseLoB,
			final List<Mittelwerte> mittelwerte,
			final TreeMap<Integer, TreeMap<String,KennzahlenMittelung>> schaetzerMittelwerte,
			final TreeMap<Integer, TreeMap<String,TreeMap<String,KennzahlenMittelungLoB>>> schaetzerMittelwerteLoB
			) {
		super();
		this.letzteBerechnung = letzteBerechnung;
		this.kennzahlenPfadweise = kennzahlenPfadweise;
		this.kennzahlenPfadweiseLoB = kennzahlenPfadweiseLoB;
		this.mittelwerte = mittelwerte;
		this.schaetzerMittelwerte= schaetzerMittelwerte;
		this.schaetzerMittelwerteLoB=schaetzerMittelwerteLoB;
	}

	/**
	 * Die letzte durchgeführte Berechnung.
	 * 
	 * @return the letzteBerechnung
	 */
	public Berechnung getLetzteBerechnung() {
		return letzteBerechnung;
	}

	/**
	 * Die Pfadweisen Kennzahlen
	 * 
	 * @return the kennzahlenPfadweise
	 */
	public TreeMap<Integer, TreeMap<Integer, KennzahlenPfadweise>> getKennzahlenPfadweise() {
		return kennzahlenPfadweise;
	}

	/**
	 * Pfadweise Kennzahlen auf LoB Basis.
	 * 
	 * @return the kennzahlenPfadweiseLoB
	 */
	public TreeMap<Integer, TreeMap<String, TreeMap<Integer,KennzahlenPfadweiseLoB>>> getKennzahlenPfadweiseLoB() {
		return kennzahlenPfadweiseLoB;
	}

	/**
	 * Berechnete Mittelwerte.
	 * 
	 * @return the mittelwerte
	 */
	public List<Mittelwerte> getMittelwerte() {
		return mittelwerte;
	}
	
	/**
	 * Berechnete SchätzerMittelwerte.
	 * 
	 * @return the schätzer mittelwerte
	 */
	public TreeMap<Integer, TreeMap<String,KennzahlenMittelung>> getSchaetzerMittelwerte() {
		return schaetzerMittelwerte;
	}
	
	
	/**
	 * Berechnete SchätzerMittelwerte LoB
	 * 
	 * @return the schätzer mittelwerte LoB
	 */
	public  TreeMap<Integer, TreeMap<String,TreeMap<String,KennzahlenMittelungLoB>>>  getSchaetzerMittelwerteLoB() {
		return schaetzerMittelwerteLoB;
	}
	

}
