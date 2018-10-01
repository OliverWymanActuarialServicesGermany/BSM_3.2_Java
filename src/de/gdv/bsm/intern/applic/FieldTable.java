package de.gdv.bsm.intern.applic;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * JTable zur Anzeige von Ergebnistabellen. Die anzuzeigenden Felder müssen mit der Annotation {@link TableField}
 * versehen sein.
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
public class FieldTable extends JTable {
	private final Font monospaced = new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize());
	private final int charWidth = getFontMetrics(monospaced).charWidth('M');

	private final SizedTableModel model;

	/**
	 * Erstelle eine Tabelle.
	 * 
	 * @param model
	 *            das zugrundeliegende Modell
	 */
	public FieldTable(final SizedTableModel model) {
		super(model);
		this.model = model;
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		final TableCellRenderer renderer = new DefaultTableCellRenderer() {
			final private JLabel self = (JLabel) this;

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
			 * java.lang.Object, boolean, boolean, int, int)
			 */
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (value instanceof String) {
					self.setHorizontalAlignment(SwingConstants.LEFT);
				} else {
					self.setHorizontalAlignment(SwingConstants.RIGHT);
					if (value instanceof Double) {
						final boolean isPercent = model.isPercent(column);
						final Double dv;
						if (isPercent) {
							dv = ((Double) value) * 100;
						} else {
							dv = (Double) value;
						}
						if (dv.isNaN()) {
							self.setText("");
						} else if (dv == 0.0) {
							self.setText("-    ");
						} else {
							int nachKomma = model.getNachkommaStellen(column);
							self.setText(String.format("%,8." + nachKomma + "f" + (isPercent ? "%%" : ""), dv));
						}
					}
				}
				self.setFont(monospaced);
				return this;
			}

		};

		setDefaultRenderer(String.class, renderer);
		setDefaultRenderer(int.class, renderer);
		setDefaultRenderer(double.class, renderer);
		setDefaultRenderer(Integer.class, renderer);
		setDefaultRenderer(Double.class, renderer);

		for (int i = 0; i < getColumnCount(); ++i) {
			getColumnModel().getColumn(i).setPreferredWidth((model.getWidth(i) + 1) * charWidth);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		final int row = rowAtPoint(event.getPoint());
		final int column = columnAtPoint(event.getPoint());

		return model.getToolTip(row, column);
	}

}
