package de.gdv.bsm.intern.applic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * GridBagPanel zur Anzeige von Label-Werte-Paaren.
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
@SuppressWarnings("serial")
public class LabelPanel extends JPanel {
	private final GridBagConstraints labelConstraints = new GridBagConstraints();
	final GridBagConstraints valueConstraints = new GridBagConstraints();

	{
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.anchor = GridBagConstraints.WEST;
		labelConstraints.weightx = 0.1;

		valueConstraints.gridx = 1;
		valueConstraints.gridy = 0;
		valueConstraints.anchor = GridBagConstraints.WEST;
		valueConstraints.weightx = 0.9;
		valueConstraints.fill = GridBagConstraints.HORIZONTAL;
	}

	/**
	 * Erstelle ein Panel.
	 */
	public LabelPanel() {
		setLayout(new GridBagLayout());
	}

	/**
	 * Füge ein Label hinzu.
	 * 
	 * @param row
	 *            Zeile des Labels
	 * @param label
	 *            der Text des Labels
	 */
	public void addLabel(final int row, final String label) {
		labelConstraints.gridy = row;
		add(new JLabel(label), labelConstraints);
	}

	/**
	 * Füge ein Label hinzu.
	 * 
	 * @param row
	 *            Zeile des Labels
	 * @param label
	 *            der Text des Labels
	 */
	public void addLabelComponent(final int row, final JComponent label) {
		labelConstraints.gridy = row;
		add(label, labelConstraints);
	}

	/**
	 * Füge eine Komponente hinzu.
	 * 
	 * @param row
	 *            Zeile der Komponente
	 * @param component
	 *            die Komponente
	 */
	public void addFirstComponent(final int row, final JComponent component) {
		valueConstraints.gridy = row;
		add(component, valueConstraints);
	}

	/**
	 * Füge ein Paar Label-Komponente hinzu.
	 * 
	 * @param row
	 *            Zeile des Eintrags
	 * @param label
	 *            Text des Labels
	 * @param component
	 *            die Komponente
	 */
	public void addLine(final int row, final String label, final JComponent component) {
		addLabel(row, label);
		addFirstComponent(row, component);
	}
}
