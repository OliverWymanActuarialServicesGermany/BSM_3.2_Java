package de.gdv.bsm.intern.params;

import java.io.File;
import java.io.IOException;

import de.gdv.bsm.intern.csv.CsvReader;
import de.gdv.bsm.intern.csv.CsvZeile;
import de.gdv.bsm.intern.csv.LineFormatException;
import de.gdv.bsm.vu.module.Functions;

public class DynManReg {
	/**
	 * OW_F.Wellens lesen vom Tabellenblatt "Dyn.ManReg"
	 */
	private boolean p_RohUebTriggerRechnen = false;
	private Boolean Anteil_NG_am_uebrigen_Ergebnis;
	public Boolean FI_Neuanl_RLZ;
	private Boolean FI_BWR;

	public DynManReg(final File dataFile) throws IOException, LineFormatException {
		try (final CsvReader csv = new CsvReader(dataFile, ';', '"')) {
			// skip header
			csv.readLine();

			CsvZeile zeilezwei = csv.readLine();
			final String p_RohUebString = zeilezwei.getString(1).toUpperCase();
			p_RohUebTriggerRechnen = Functions.booleanValueOfString(p_RohUebString);

			// Zeile 3
			CsvZeile drei = csv.readLine();
			final String Anteil_NG_am_uebrigen_ErgebnisString = drei.getString(1).toUpperCase();
			if (Anteil_NG_am_uebrigen_ErgebnisString.equals("WAHR")) {
				Anteil_NG_am_uebrigen_Ergebnis = true;
			} else {
				Anteil_NG_am_uebrigen_Ergebnis = false;
			}
			
			// Zeile 4
			CsvZeile vier = csv.readLine();
			final String FI_Neuanl_RLZString = vier.getString(1).toUpperCase();
			if (FI_Neuanl_RLZString.equals("WAHR")) {
				FI_Neuanl_RLZ = true;
			} else {
				FI_Neuanl_RLZ = false;
			}
			
			// Zeile 5
			CsvZeile fuenf = csv.readLine();
			final String FI_BWRString = fuenf.getString(1).toUpperCase();
			if (FI_BWRString.equals("WAHR")) {
				FI_BWR = true;
			} else {
				FI_BWR = false;
			}
		}
	}

	/**
	 * Soll mit p_RohUeb stressszenario abhaengig gerechnet werden?
	 * 
	 * @return ja oder nein
	 */
	public boolean isP_RohUebTriggerRechnen() {
		return p_RohUebTriggerRechnen;
	}

	/**
	 * Soll der Trigger zum stressabhängigen Input der zeitabhängigen Managementregel „Anteil des NG am übrigen Ergebnis“ ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getAnteil_NG_am_uebrigen_Ergebnis() {
		return Anteil_NG_am_uebrigen_Ergebnis;
	}
	
	/**
	 * Soll der Trigger zum stressabhängigen Input der zeitabhängigen Managementregel „FI Neuanlage Restlaufzeit RLZ_neuAnl“ ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getFI_Neuanl_RLZ() {
		return FI_Neuanl_RLZ;
	}
	
	/**
	 * Soll der Trigger zum stressabhängigen Input der zeitabhängigen Managementregel „Verrechnungszeitraum für FI-BWR ?_Verrechn“ ziehen?
	 * 
	 * @return der Wert
	 */
	public Boolean getFI_BWR() {
		return FI_BWR;
	}
}