package de.gdv.bsm.intern.applic;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;

import de.gdv.bsm.intern.rechnung.Mittelwerte;

/**
 * Spezielles TableModel zur Anzeige von {@link Mittelwerte}.
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
public class MittelwerteTableModel implements SizedTableModel {
	private List<Mittelwerte> mittelwerte;
	private List<String> alleTitle;

	/**
	 * Erstelle das Modell aus den Daten.
	 * 
	 * @param mittelwerte
	 *            die Daten
	 */
	public MittelwerteTableModel(final List<Mittelwerte> mittelwerte) {
		this.mittelwerte = mittelwerte;

		alleTitle = new ArrayList<>();

		if (mittelwerte.size() > 0) {
			final List<String> title = mittelwerte.get(0).getTitle();
			alleTitle.addAll(title);
		}
	}

	@Override
	public int getRowCount() {
		return mittelwerte.size();
	}

	@Override
	public int getColumnCount() {
		return alleTitle.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return alleTitle.get(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
		case 2:
			return String.class;
		case 1:
		case 3:
			return Integer.class;
		default:
			return Double.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Mittelwerte mw = mittelwerte.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return mw.getSzenario();
		case 1:
			return mw.getSzenarioId();
		case 2:
			return mw.getLob();
		case 3:
			return mw.getZeit();
		default:
			return mw.getValue(columnIndex - 4);
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

	@Override
	public boolean isPercent(int column) {
		return false;
	}

	@Override
	public int getNachkommaStellen(int column) {
		return 1;
	}

	@Override
	public int getWidth(int column) {
		int width = 10;
		for (int i = 0; i < getRowCount(); ++i) {
			final Object v = getValueAt(i, column);
			if (v instanceof String) {
				if (((String) v).length() > width)
					width = ((String) v).length();
			} else if (v instanceof Integer) {
				int size = String.valueOf((int) v).length();
				if (size > width) {
					width = size;
				}
			} else if (v instanceof Double) {
				int size = String.format("%,8.0f", (double) v).length() + getNachkommaStellen(column) + 1;
				if (size > width) {
					width = size;
				}
			}
		}
		return width;
	}

	@Override
	public String getToolTip(int row, int column) {
		return "" + getValueAt(row, column).toString();
	}
}
