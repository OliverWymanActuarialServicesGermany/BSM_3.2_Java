package de.gdv.bsm.vu.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import de.gdv.bsm.intern.applic.LabelPanel;


@SuppressWarnings("serial")
class NumberDocument extends PlainDocument {

    @Override
    public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {
        if (s.matches("[0-9]") || s.matches(",")) {
            super.insertString(offset, s, attributeSet);
        } 
        if (s.matches("0,005"))
        {
        	super.insertString(offset, s, attributeSet);
        }
        else {
            
        }            
    }

    
    
}


/**
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
 */
@SuppressWarnings("serial")
public class DiffToolGUI extends JFrame {
	
	private final JTextField pfad1;
	private final JTextField pfad2;
	private final JTextField pfadAusgabe;
	private final JTextField precisionInput;
	private final JComboBox<String> choosePrecisionMeasure;
	
	/**
	 * Constructor der DiffTool GUI
	 */
	public DiffToolGUI(){

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("DiffTool zum Abgleich von Input / Output CSV Dateien für das BSM");
		pfad1 = new JTextField();
		pfad2 = new JTextField();
		pfadAusgabe = new JTextField();
		precisionInput = new JTextField(new NumberDocument(), "				", 8); 
		precisionInput.setText("xxxxxx");
		choosePrecisionMeasure = new JComboBox<String>();
		choosePrecisionMeasure.addItem("absolut");
		choosePrecisionMeasure.addItem("releativ");
		choosePrecisionMeasure.setSelectedItem("releativ");
		
		final JPanel center = new LabelPanel() {
			{
				int row = 0;
				final JPanel panPfad1 = new LabelPanel() {
					{
						final JButton button = new JButton("...");
						pfad1.setText("												");
						pfad1.setEnabled(false);

						button.addActionListener(e -> {
							final JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							// TODO: ggf. schon default Vorabwert setzen fc.setCurrentDirectory(new File());
							fc.setMultiSelectionEnabled(false);
							switch (fc.showOpenDialog(button)) {
							case JFileChooser.APPROVE_OPTION:
								pfad1.setText(fc.getSelectedFile().getAbsolutePath());
								break;
							}
						});

						addLabelComponent(0, button);
						addFirstComponent(0, pfad1);
					}
				};
				final JPanel panPfad2 = new LabelPanel() {
					{
						final JButton button = new JButton("...");
						pfad2.setText("												");
						pfad2.setEnabled(false);

						button.addActionListener(e -> {
							final JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							// TODO: ggf. schon default Vorabwert setzen fc.setCurrentDirectory(new File());
							fc.setMultiSelectionEnabled(false);
							switch (fc.showOpenDialog(button)) {
							case JFileChooser.APPROVE_OPTION:
								pfad2.setText(fc.getSelectedFile().getAbsolutePath());
								break;
							}
						});

						addLabelComponent(0, button);
						addFirstComponent(0, pfad2);
					}
				};
				final JPanel panPfadAusgabe = new LabelPanel() {
					{
						final JButton button = new JButton("...");
						pfadAusgabe.setText("												");
						pfadAusgabe.setEnabled(false);

						button.addActionListener(e -> {
							final JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							// TODO: ggf. schon default Vorabwert setzen fc.setCurrentDirectory(new File());
							fc.setMultiSelectionEnabled(false);
							switch (fc.showOpenDialog(button)) {
							case JFileChooser.APPROVE_OPTION:
								pfadAusgabe.setText(fc.getSelectedFile().getAbsolutePath());
								break;
							}
						});

						addLabelComponent(0, button);
						addFirstComponent(0, pfadAusgabe);
					}
				};
				
				addLine(row++, "Dateipfad 1: ", panPfad1);
				addLine(row++, "Dateipfad 2: ", panPfad2);
				addLine(row++, "Ausgabepfad: ", panPfadAusgabe);
				
				

				
				final JPanel panPrec = new LabelPanel();
				panPrec.setLayout(new FlowLayout(FlowLayout.LEFT));
				panPrec.add(new JLabel("Toleranz: "));
				precisionInput.setText("0,005");
				panPrec.add(precisionInput);
				panPrec.add(new JLabel(" Messung: "));
				panPrec.add(choosePrecisionMeasure);
				
				addLine(row++, "Abweichungen: " ,panPrec);

			}
		};
		add(center, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel() {
			{
				final JButton compareButton = new JButton("Vergleichen");
				 compareButton.addActionListener(e -> compare());
				add(compareButton);

				final JButton ende = new JButton("Programm schließen");
				ende.addActionListener(e -> System.exit(0));
				add(ende);
			}
		};
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
		
		
	}
	
	
	/**
	 * Main Methode zum starten der GUI
	 * @param args 
	 */
	public static void main(String[] args) {
		Locale.setDefault(new Locale("de", "DE"));
		// Setze für den UIManager das look-and-feel passend zum aktuellen Betriebssystem
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		new DiffToolGUI();
	
	}
	
	private void compare (){
		
		
		try {
		
		new DiffTool(new File(pfad1.getText()), new File(pfad2.getText()), new File(pfadAusgabe.getText()),   Double.parseDouble( precisionInput.getText().replace(",",".")) , choosePrecisionMeasure.getSelectedItem().equals("absolut") ? false : true);
		
		}
		catch(Exception e){
			System.out.println(e);
			System.out.println(e.getStackTrace());
		}
		
	}

}
