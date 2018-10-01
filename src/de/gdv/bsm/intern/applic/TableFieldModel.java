package de.gdv.bsm.intern.applic;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TableModelListener;

/**
 * TableModel für Daten mit Annotationen. Die dargestellten Daten müssen die Annotation {@link TableField} verwenden, um
 * anzuzeigende Spalten zu kennzeichnen.
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
 * 
 * @param <T>
 *            Typ der dargestellten Daten
 *
 */
public class TableFieldModel<T> implements SizedTableModel {
	private final List<T> data;
	private final List<Field> dataFields;
	private final List<String> excelColumns;

	/**
	 * Erstelle ein Modell aus den Daten.
	 * 
	 * @param data
	 *            die Daten
	 * @param dataClass
	 *            die zugehörige Klasse
	 */
	public TableFieldModel(final List<T> data, final Class<T> dataClass) {
		this.data = data;

		char excelFirstChar = ' ';
		char excelChar = 'A';

		final List<Field> dataFields = new ArrayList<>();
		final List<String> excelColumns = new ArrayList<>();
		for (Field f : dataClass.getDeclaredFields()) {
			final TableField tf = f.getAnnotation(TableField.class);
			if (tf != null) {
				if (!tf.suppress()) {
					dataFields.add(f);
					excelColumns.add((excelFirstChar == ' ' ? "" : String.valueOf(excelFirstChar)) + excelChar);
				}
				if (excelChar == 'Z') {
					if (excelFirstChar == ' ')
						excelFirstChar = 'A';
					else
						++excelFirstChar;
					excelChar = 'A';
				} else {
					++excelChar;
				}
			}
		}
		this.dataFields = Collections.unmodifiableList(dataFields);
		this.excelColumns = Collections.unmodifiableList(excelColumns);
	}

	/**
	 * Ermittle die maximale Zeichenzahl einer Spalte
	 * 
	 * @param column
	 *            die Spalte
	 * @return die Zeichenzahl
	 */
	public int getWidth(final int column) {
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
				int size = String.format("%,8.0f", (double) v).length()
						+ dataFields.get(column).getAnnotation(TableField.class).nachKomma() + 1;
				if (size > width) {
					width = size;
				}
			}
		}
		return width;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return dataFields.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return "<html>" + dataFields.get(columnIndex).getName() + "<br/>(" + excelColumns.get(columnIndex) + ")</html>";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return dataFields.get(columnIndex).getType();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Field f = dataFields.get(columnIndex);
		final boolean acc = f.isAccessible();
		f.setAccessible(true);
		final Object v = data.get(rowIndex);
		Object value = "?";
		try {
			value = f.get(v);
		} catch (IllegalArgumentException | IllegalAccessException e) {
		}
		f.setAccessible(acc);
		return value;
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

	/**
	 * Anzahl gewünschter Nachkommastellen.
	 * 
	 * @param column
	 *            die Spalte
	 * @return die Stellen
	 */
	public int getNachkommaStellen(final int column) {
		final TableField anno = dataFields.get(column).getAnnotation(TableField.class);
		if (anno != null) {
			return anno.nachKomma();
		}
		return 1;
	}

	/**
	 * Anzahl gewünschter Nachkommastellen.
	 * 
	 * @param column
	 *            die Spalte
	 * @return die Stellen
	 */
	public boolean isPercent(final int column) {
		final TableField anno = dataFields.get(column).getAnnotation(TableField.class);
		if (anno != null) {
			return anno.percent();
		}
		return false;
	}

	/**
	 * Erzeugt einen Tool-Tip.
	 * 
	 * @param row
	 *            Spalte des Tool-Tips
	 * @param column
	 *            Zeile des Tool-Tips
	 * @return der Tool-Tip
	 */
	public String getToolTip(int row, int column) {
		final Object v = getValueAt(row, column);
		final String value;
		if (v instanceof Double) {
			final NumberFormat nf = new DecimalFormat();
			nf.setMaximumFractionDigits(20);
			value = nf.format(v);
		} else {
			value = String.valueOf(v);
		}
		return value + " (" + excelColumns.get(column) + ")";
	}

}
