package de.gdv.bsm.intern.applic;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.intern.params.Eingabe;
import de.gdv.bsm.intern.params.SzenarioMapping;
import de.gdv.bsm.intern.params.SzenarioMappingZeile;
import de.gdv.bsm.intern.params.VuParameter;
import de.gdv.bsm.intern.rechnung.KennzahlenMittelung;
import de.gdv.bsm.intern.rechnung.KennzahlenMittelungLoB;
import de.gdv.bsm.intern.rechnung.Mittelwerte;
import de.gdv.bsm.intern.rechnung.RechenThread;
import de.gdv.bsm.intern.szenario.Szenario;
import de.gdv.bsm.vu.berechnung.AggZeile;
import de.gdv.bsm.vu.berechnung.FiAusfallZeile;
import de.gdv.bsm.vu.berechnung.RzgZeile;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweise;
import de.gdv.bsm.vu.kennzahlen.KennzahlenPfadweiseLoB;

/**
 * Klasse zur Gesamtberechnung ausgewählten Szenarien und Pfaden. Der Fortschritt des Ladens und der Berechnung wird in
 * einem Fenster angezeigt.
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
@SuppressWarnings("serial")
public class RechenFortschritt extends JDialog implements RechenFortschrittInterface {
	/**
	 * Art der Beendigung einer Berechnung.
	 */
	public enum ExitCode {
		/** Die Berechnung wurde ordnungsgemäß beendet. */
		OK,

		/** Die Berechnung wurde durch den Benutzer abgebrochen. */
		ABBRUCH,

		/**
		 * Die Berechnung wurde mit Fehler abgebrochen. Die Fehlerursache findet sich mit
		 * {@link RechenFortschritt#getCrashReason()}.
		 */
		FEHLER
	}

	private final Map<Integer, JProgressBar> sznrProgress = new HashMap<>();
	private final Map<Integer, JProgressBar> berechnungProgress = new HashMap<>();
	private final Map<Class<?>, JProgressBar> ausgabeProgress = new HashMap<>();
	private BerechnungResultat berechnungResultat = null;
	private ExitCode exitCode = ExitCode.OK;
	private Throwable crashReason = null;

	/**
	 * Erstelle das Fenster.
	 * 
	 * @param vuParameterParam
	 * 
	 * @param eingabe
	 *            Vorgaben
	 * @param basisPfad
	 *            Grundverzeichnis
	 * @throws IOException
	 *             bei IO-Fehlern
	 * @throws LineFormatException
	 *             bei csv Lesefehlern
	 */
	public RechenFortschritt(final VuParameter vuParameterParam, final Eingabe eingabe)
			throws IOException, LineFormatException {
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// bei jedem Rechnen laden wir die VU-Parameter im Dialog neu,
		// da der Benutzer dann diverse Excel-Reports machen kann
		final VuParameter vuParameter = new VuParameter(vuParameterParam.getTransferDir());
		final Set<Integer> szenarienSatzIds = new TreeSet<>();

		final JComponent center = new LabelPanel() {
			{
				int row = 0;

				final SzenarioMapping szenarioMapping = vuParameter.getSzenarioMapping();
				if (eingabe.isAlleSzenarien()) {
					for (SzenarioMappingZeile zeile : szenarioMapping.getAktiveSzenarien()) {
						szenarienSatzIds.add(zeile.getZinskurve());
					}
				} else {
					SzenarioMappingZeile zeile = szenarioMapping.getSzenarionMapping(eingabe.getSzenario());
					szenarienSatzIds.add(zeile.getZinskurve());
				}

				addLine(row++, "Eingaben", new JSeparator());

				for (int id : szenarienSatzIds) {
					final JProgressBar pb = new JProgressBar();
					sznrProgress.put(id, pb);
					addLine(row++, Szenario.getName(id), pb);
				}

				addLine(row++, "Berechnungen", new JSeparator());

				if (eingabe.isAlleSzenarien()) {
					for (SzenarioMappingZeile zeile : szenarioMapping.getAktiveSzenarien()) {
						final JProgressBar p = new JProgressBar();
						berechnungProgress.put(zeile.getId(), p);
						addLine(row++, zeile.getName() + " (" + zeile.getId() + ")", p);
					}
				} else {
					SzenarioMappingZeile zeile = szenarioMapping.getSzenarionMapping(eingabe.getSzenario());
					final JProgressBar p = new JProgressBar();
					berechnungProgress.put(zeile.getId(), p);
					addLine(row++, zeile.getName() + " (" + zeile.getId() + ")", p);
				}

				addLine(row++, "Ausgaben", new JSeparator());

				final JProgressBar rzg = new JProgressBar();
				ausgabeProgress.put(RzgZeile.class, rzg);
				addLine(row++, "rzg", rzg);

				final JProgressBar agg = new JProgressBar();
				ausgabeProgress.put(AggZeile.class, agg);
				addLine(row++, "agg", agg);
				
				final JProgressBar fiAusfJPrg = new JProgressBar();
				ausgabeProgress.put(FiAusfallZeile.class, fiAusfJPrg);
				addLine(row++, "Fi Ausfall", fiAusfJPrg);

				final JProgressBar kennzahlenPfadweise = new JProgressBar();
				ausgabeProgress.put(KennzahlenPfadweise.class, kennzahlenPfadweise);
				addLine(row++, "Kennzahlen Pfadweise", kennzahlenPfadweise);

				final JProgressBar kennzahlenPfadweiseLob = new JProgressBar();
				ausgabeProgress.put(KennzahlenPfadweiseLoB.class, kennzahlenPfadweiseLob);
				addLine(row++, "Kennzahlen Pfadweise LoB", kennzahlenPfadweiseLob);

				final JProgressBar mittelwerteZeitschrittig = new JProgressBar();
				ausgabeProgress.put(Mittelwerte.class, mittelwerteZeitschrittig);
				addLine(row++, "Mittelwerte zeitschrittig", mittelwerteZeitschrittig);
				
				final JProgressBar mittelwerteJPBar = new JProgressBar();
				ausgabeProgress.put(KennzahlenMittelung.class, mittelwerteJPBar);
				addLine(row++, "Schätzer Mittelwerte", mittelwerteJPBar);
				
				final JProgressBar mittelwerteLoBJPBar = new JProgressBar();
				ausgabeProgress.put(KennzahlenMittelungLoB.class, mittelwerteLoBJPBar);
				addLine(row++, "Schätzer Mittelwerte LoB", mittelwerteLoBJPBar);

			}
		};
		add(center, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel() {
			{
				final JButton abbruch = new JButton("Abbruch");
				abbruch.addActionListener(e -> exitCode = ExitCode.ABBRUCH);
				add(abbruch);
			}
		};
		add(buttonPanel, BorderLayout.SOUTH);

		pack();

		final RechenThread rechenThread = new RechenThread(this, eingabe, vuParameter);
		new Thread(rechenThread).start();

		setVisible(true);
	}

	/**
	 * Die letzte durchgeführte Berechnung. Dies ist die Berechnung des letzten Pfades des letzten Szenarios.
	 * 
	 * @return die Berechnung
	 */
	public BerechnungResultat getLetzteBerechnung() {
		return berechnungResultat;
	}

	/**
	 * Art der Beendigung der Rechnung.
	 * 
	 * @return der Code
	 */
	public ExitCode getExitCode() {
		return exitCode;
	}

	/**
	 * Exception, die den Abbruch verursachte.
	 * 
	 * @return die Exception
	 */
	public Throwable getCrashReason() {
		return crashReason;
	}

	@Override
	public void berechnungBeendet(final BerechnungResultat berechnung) {
		this.berechnungResultat = berechnung;
		dispose();
	}

	@Override
	public void berechnungGechrashed(Throwable reason) {
		this.exitCode = ExitCode.FEHLER;
		this.crashReason = reason;
		dispose();
	}

	@Override
	public void setSznrPercent(int id, int i) {
		if (sznrProgress.containsKey(id)) {
			SwingUtilities.invokeLater(() -> sznrProgress.get(id).setValue(i));
		}
	}

	@Override
	public void setBerechnungPercent(int id, int lastPercent) {
		if (berechnungProgress.containsKey(id)) {
			SwingUtilities.invokeLater(() -> berechnungProgress.get(id).setValue(lastPercent));
		}
	}

	@Override
	public boolean isAbbruch() {
		return getExitCode() == ExitCode.ABBRUCH;
	}

	@Override
	public void setAusgabePercent(Class<?> klasse, int percent) {
		if (ausgabeProgress.containsKey(klasse)) {
			SwingUtilities.invokeLater(() -> ausgabeProgress.get(klasse).setValue(percent));
		}

	}
}
