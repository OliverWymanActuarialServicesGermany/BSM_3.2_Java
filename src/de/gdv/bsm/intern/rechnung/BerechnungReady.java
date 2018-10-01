package de.gdv.bsm.intern.rechnung;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;
import de.gdv.bsm.vu.kennzahlen.MittelwerteNurCe;
import de.gdv.bsm.vu.kennzahlen.MittelwerteUndCe;

/**
 * Meldung über die Beendigung deer Berechnung eines Pfades.
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
public class BerechnungReady {
	/** Der ausführende Thread. */
	public final BerechnungThread doer;
	/** Der berechnete Pfad. */
	public final Optional<Integer> berechneterPfad;
	/** Die berechneten Pfadweisen Kennzahlen. */
	public final KennzahlenPfadweise kennzahlenPfadweise;
	/** Die berechneten Kennzahlen pro LOB. */
	public final List<KennzahlenPfadweiseLoB> kennzahlenPfadweiseLoB;
	/** Mittelwerte und CE pro Lob und Zeit. */
	public final Map<String, Map<Integer, MittelwerteUndCe>> mittelwerteUndCe;
	/** Mittelwerte ohne CE pro Lob und Zeit. */
	public final Map<String, Map<Integer, MittelwerteNurCe>> mittelwerteNurCe;
	/** Eine potentielle Fehlermeldung. */
	public final Optional<Throwable> error;

	/**
	 * Konstruiere eine Meldung.
	 * 
	 * @param doer
	 *            Der ausführende Thread.
	 * @param berechneterPfad
	 *            Der berechnete Pfad.
	 * @param kennzahlenPfadweise
	 *            die Kennzahlen für die Pfade
	 * @param kennzahlenPfadweiseLoB
	 *            die Kennzahlen für die Pfade und LoB
	 * @param mittelwerteUndCe
	 *            Berechnete Kennzahlen
	 * @param mittelwerteNurCe
	 *            Berechnete Kennzahlen
	 * 
	 */
	public BerechnungReady(final BerechnungThread doer, final Optional<Integer> berechneterPfad,
			final KennzahlenPfadweise kennzahlenPfadweise, List<KennzahlenPfadweiseLoB> kennzahlenPfadweiseLoB,
			final Map<String, Map<Integer, MittelwerteUndCe>> mittelwerteUndCe,
			final Map<String, Map<Integer, MittelwerteNurCe>> mittelwerteNurCe) {
		this(doer, berechneterPfad, kennzahlenPfadweise, kennzahlenPfadweiseLoB, mittelwerteUndCe, mittelwerteNurCe,
				Optional.empty());
	}

	/**
	 * Konstruiere eine Meldung.
	 * 
	 * @param doer
	 *            Der ausführende Thread.
	 * @param berechneterPfad
	 *            Der berechnete Pfad.
	 * @param kennzahlenPfadweise
	 *            die Kennzahlen für die Pfade
	 * @param kennzahlenPfadweiseLoB
	 *            die Kennzahlen für die Pfade und LoB
	 * @param mittelwerteUndCe
	 *            Berechnete Kennzahlen
	 * @param mittelwerteNurCe
	 *            Berechnete Kennzahlen
	 * @param error
	 *            eine Fehlermeldung, oder empty
	 * 
	 */
	public BerechnungReady(final BerechnungThread doer, final Optional<Integer> berechneterPfad,
			final KennzahlenPfadweise kennzahlenPfadweise, List<KennzahlenPfadweiseLoB> kennzahlenPfadweiseLoB,
			final Map<String, Map<Integer, MittelwerteUndCe>> mittelwerteUndCe,
			final Map<String, Map<Integer, MittelwerteNurCe>> mittelwerteNurCe, final Optional<Throwable> error) {
		this.doer = doer;
		this.berechneterPfad = berechneterPfad;
		this.kennzahlenPfadweise = kennzahlenPfadweise;
		this.kennzahlenPfadweiseLoB = kennzahlenPfadweiseLoB;
		this.mittelwerteUndCe = mittelwerteUndCe;
		this.mittelwerteNurCe = mittelwerteNurCe;
		this.error = error;
	}
}
