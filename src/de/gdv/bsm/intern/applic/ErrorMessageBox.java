package de.gdv.bsm.intern.applic;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.gdv.bsm.intern.rechnung.ResultNotFinite;

/**
 * Box mit einer Fehlermeldung und einem Stack-Trace.
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
public class ErrorMessageBox extends JDialog {

	/**
	 * Zeige den angegebenen Fehler an.
	 * 
	 * @param e
	 *            der Fehler
	 */
	public ErrorMessageBox(Throwable e) {
		setTitle("Fehler in der Anwendung");
		setModal(true);
		setLocationByPlatform(true);

		final JPanel north = new JPanel() {
			{
				setLayout(new GridLayout(2, 1));
				if (e instanceof ResultNotFinite) {
					final ResultNotFinite rnf = (ResultNotFinite) e;
					add(new JLabel(rnf.getHeader()));
					final JTextField err = new JTextField(rnf.getFelder());
					err.setEditable(false);
					add(err);
				} else {
					add(new JLabel("Es trat ein Fehler auf:"));
					final JTextField err = new JTextField(e.getLocalizedMessage());
					err.setEditable(false);
					add(err);
				}
			}
		};
		add(north, BorderLayout.NORTH);

		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		final JTextArea ta = new JTextArea(sw.toString());
		add(new JScrollPane(ta), BorderLayout.CENTER);

		setSize(800, 400);
		setVisible(true);
	}

}
