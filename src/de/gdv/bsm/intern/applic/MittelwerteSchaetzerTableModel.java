package de.gdv.bsm.intern.applic;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.event.TableModelListener;

import de.gdv.bsm.intern.rechnung.KennzahlenMittelung;
import de.gdv.bsm.intern.rechnung.Mittelwerte;

/**
 * Spezielles TableModel zur Anzeige von {@link Mittelwerte}.
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
public class MittelwerteSchaetzerTableModel implements SizedTableModel {
	private TreeMap<Integer, TreeMap<String,KennzahlenMittelung>> mittelwerte;
	private List<String> alleTitle;
	
	/**
	 * Erstelle das Modell aus den Daten.
	 * 
	 * @param mittelwerte
	 *            die Daten
	 */ 
	public MittelwerteSchaetzerTableModel(final TreeMap<Integer, TreeMap<String,KennzahlenMittelung>> mittelwerte) {
		this.mittelwerte = mittelwerte;

		alleTitle =	KennzahlenMittelung.getTitle();
				
		
	}

	@Override
	public int getRowCount() {
		return mittelwerte.values().stream().map(e -> e.values()).flatMap(l -> l.stream()).collect(Collectors.toList()).size();
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
		final KennzahlenMittelung mw = mittelwerte.values().stream().map(e -> e.values()).flatMap(l -> l.stream()).collect(Collectors.toList()).get(rowIndex);
		// ;
		return mw.getSchaetzerMittelwerte().get(columnIndex);
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
