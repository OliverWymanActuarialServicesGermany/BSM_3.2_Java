package de.gdv.bsm.intern.rechnung;

import java.util.ArrayList;
import java.util.List;
import de.gdv.bsm.intern.applic.TableField;

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
public class KennzahlenMittelungLoB extends KennzahlenMittelung{

	// to be provided in constructor
	@TableField(columnName = "LoB Name", exportPosition = 0.3f)
	protected final String LobName;
	
	// Berechnet aus statistischen Summen die entsprechenden Mittelwerte
	/**
	 * @param kennzahl
	 * @param isAntithetic
	 * @param ScenarioID
	 * @param bezeichnung 
	 * @param referenzCV 
	 * @param LobName
	 * @param valueCE
	 * @param sumValues
	 * @param countValues
	 * @param sumSquaredValues
	 * @param valueCE_CV_EW
	 * @param sumValuesCV
	 * @param sumSquaredValuesCV
	 * @param sumValueTimesValueCV
	 */
	public KennzahlenMittelungLoB(String kennzahl, String bezeichnung, String referenzCV, boolean isAntithetic, int ScenarioID, String LobName, double valueCE, double sumValues, long countValues, double sumSquaredValues, double valueCE_CV_EW, double sumValuesCV, double sumSquaredValuesCV, double sumValueTimesValueCV) {
		super(kennzahl, bezeichnung, referenzCV, isAntithetic, ScenarioID, valueCE, sumValues, countValues, sumSquaredValues, valueCE_CV_EW, sumValuesCV, sumSquaredValuesCV, sumValueTimesValueCV);
		this.LobName                  =LobName;
	}

	
	

	public static List<String> getTitle() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("<html><p>Stress-Szenario</p><br/><p></p></html>");
		alleTitel.add("<html><p>LoB</p><br/><p></p></html>");
		alleTitel.add("<html><p>Kennzahl</p><br/><p></p></html>");
		alleTitel.add("<html><p>Wert unter dem CE</p><br/><p></p></html>");
		alleTitel.add("<html><p>empirisches Mittel</p><br/><p></p></html>");	
		alleTitel.add("<html><p>Konfidenzintervall empirisches Mittel</p><br/><p></p></html>");
		alleTitel.add("<html><p>Mittelwert Control Variates (CV)</p><br/><p></p></html>");
		alleTitel.add("<html><p>Konfidenzintervall Mittelwert CV</p><br/><p></p></html>");
		alleTitel.add("<html><p>empirisches Mittel (Korrektur nach Mittelwerten)</p><br/><p></p></html>");
		return alleTitel;
	}
	
	// TODO: The serializable class does not declare a static final serialVersionUID field of type long prüfen
	@SuppressWarnings("serial")
	@Override
	public ArrayList<Object> getSchaetzerMittelwerte() {
		return new ArrayList<Object>(){{
			add(ScenarioID);
			add(LobName);
			add(kennzahl);
			add(valueCE);
			add(arithmeticMeanValue);
			add(confidenceLevel);
			add(meanValueWithCV);
			add(confidenceLevelCV);
			add(meanCorrectedByMean);
			}};	
	}
	
	// TODO: The serializable class does not declare a static final serialVersionUID field of type long prüfen
	@SuppressWarnings("serial")
	@Override
	@Deprecated
	public ArrayList<Object> getSchaetzerMittelwerteOld() {
		return new ArrayList<Object>(){{
			add(ScenarioID);
			add(LobName);
			add(kennzahl);
			add(isAntithetic);
			add(valueCE);
			add(arithmeticMeanValue);
			add(confidenceLevel);
			add(meanValueWithCV);
			add(confidenceLevelCV);
			add(meanCorrectedByMean);
			}};	
	}
	
}
