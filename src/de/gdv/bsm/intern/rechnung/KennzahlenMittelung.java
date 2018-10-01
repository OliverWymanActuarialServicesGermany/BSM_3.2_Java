package de.gdv.bsm.intern.rechnung;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.math.Gaussian;

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
public class KennzahlenMittelung {

	// to be provided in constructor
	@TableField(columnName = "Stress-Szenario", exportPosition = 0.1f)
	protected final int 		ScenarioID;
	protected final boolean 	isAntithetic;
	@TableField(columnName = "Kennzahl", exportPosition = 0.8f)
	protected final String 	kennzahl;
	@TableField(columnName = "Kennzahlendetails" , nachKomma = 2)
	protected final String 	bezeichnung;
	@TableField(columnName = "Referenzwert für CV aus der Klasse Kennzahlen pfadweise" , nachKomma = 2)
	protected final String 	referenzCV;
	@TableField(columnName = "Wert unter dem CE" , nachKomma = 2)
	protected final double 	valueCE;
	protected final double 	sumValues;
	protected final long	countValues;
	protected final double 	sumSquaredValues;
	protected final double 	valueCE_CV_EW;
	protected final double 	sumValuesCV;
	protected final double 	sumSquaredValuesCV;
	protected final double 	sumValueTimesValueCV;
	
	// to be calculated
	@TableField(columnName = "empirisches Mittel" ,nachKomma = 2)
	protected double arithmeticMeanValue;
	protected double correctedVariance;
	@TableField(columnName = "Konfidenzintervall empirisches Mittel" ,nachKomma = 2)
	protected double confidenceLevel;
	protected double arithmeticMeanCV;
	protected double correctedVarianceCV;
	protected double correctedCovarianceCV;
	@TableField(columnName = "Mittelwert Control Variates (CV)" ,nachKomma = 2)
	protected double meanValueWithCV;
	protected double varianceValueWithCV;
	@TableField(columnName = "Konfidenzintervall Mittelwert CV" ,nachKomma = 2)
	protected double confidenceLevelCV;
	@TableField(columnName = "empirisches Mittel (Korrektur nach Mittelwerten)" ,nachKomma = 2)
	protected double meanCorrectedByMean;
	

	
	// Berechnet aus statistischen Summen die entsprechenden Mittelwerte
	/**
	 * @param kennzahl
	 * @param isAntithetic
	 * @param ScenarioID
	 * @param bezeichnung 
	 * @param referenzCV 
	 * @param valueCE
	 * @param sumValues
	 * @param countValues
	 * @param sumSquaredValues
	 * @param valueCE_CV_EW
	 * @param sumValuesCV
	 * @param sumSquaredValuesCV
	 * @param sumValueTimesValueCV
	 */
	public KennzahlenMittelung(String kennzahl, String bezeichnung, String referenzCV, boolean isAntithetic, int ScenarioID, double valueCE, double sumValues, long countValues, double sumSquaredValues, double valueCE_CV_EW, double sumValuesCV, double sumSquaredValuesCV, double sumValueTimesValueCV) {
		this.kennzahl                 =kennzahl; 
		this.isAntithetic             =isAntithetic;
		this.ScenarioID               =ScenarioID;
		this.bezeichnung			  =bezeichnung;
		this.referenzCV				  =referenzCV;
		this.valueCE                  =valueCE;
		this.sumValues                =sumValues;
		this.countValues              =countValues;
		this.sumSquaredValues         =sumSquaredValues;
		this.valueCE_CV_EW            =valueCE_CV_EW;
		this.sumValuesCV              =sumValuesCV;
		this.sumSquaredValuesCV       =sumSquaredValuesCV;
		this.sumValueTimesValueCV     =sumValueTimesValueCV;
		
		arithmeticMeanValue=calcArithmeticMean();
		correctedVariance=calcCorrectedVariance();
		confidenceLevel=calcConfidenceLevel();
		arithmeticMeanCV=calcArithmeticMeanCV();
		correctedVarianceCV=calcCorrectedVarianceCV();
		correctedCovarianceCV = calcCorrectedCovarianceCV();
		meanValueWithCV=calcMeanValueWithCV();
		varianceValueWithCV=calcVarianceValueWithCV();
		confidenceLevelCV=calcConfidenceLevelCV();
		meanCorrectedByMean=calcMeanCorrectedByMean();
	}
	
	protected double calcArithmeticMean(){
		return sumValues/countValues;
	}
	
	// Korrigierte Stichprobenvarianz: 1/(n-1)* (sum x_i^2 - (sum x_i)^2/n ) 
	protected double calcCorrectedVariance() {
		return 1.0d/(countValues-1.0d)*(sumSquaredValues- Math.pow(sumValues,2.0)/countValues);
	}
	
	// 95% Konfidenzintervall Gaussian.inverseCDF(0.025)= -1.9599639810621738*sigma/Wurzel(n) um den Mittelwert  bzw. 90% Konfidenzintervall Gaussian.inverseCDF(0.05)=-1.644853625446558*sigma/Wurzel(n) um Mittelwert
	protected double calcConfidenceLevel() {
		return -Gaussian.inverseCDF(0.025)*Math.pow(correctedVariance/countValues,0.5);
	}
	
	protected double calcArithmeticMeanCV() {
		return sumValuesCV/countValues;
	}
	
	protected double calcCorrectedVarianceCV() {
		return 1.0d/(countValues-1.0d)*(sumSquaredValuesCV- Math.pow(sumValuesCV,2.0)/countValues);
	}
	
	
	// Berechne korrigierte Stichproben Kovarianz zwischen Wert und CV Wert:  1/(n-1)*( sum(x_i *y_i) - 1/n( sum x_i * sum y_i))=1/(n-1)*( sum(x_i *y_i) - n*( Mittelwert * Mittelwert CV))
	protected double calcCorrectedCovarianceCV() {
		return 1.0d/(countValues-1.0d)*(sumValueTimesValueCV- arithmeticMeanValue*arithmeticMeanCV*countValues);
	}
	
	
	//Berechne Mittelwert_CV_Sznr = Mittelwert + (Kovarianz / Varianz CV) * (EW - Mittelwert CV)
	protected double calcMeanValueWithCV() {
		return arithmeticMeanValue+correctedCovarianceCV/correctedVarianceCV*(valueCE_CV_EW-arithmeticMeanCV);
	}		
	
	
	//Berechne Varianz mit CV = Abs(Varianz - (Kovarianz / Varianz CV) * Kovarianz)
	protected double calcVarianceValueWithCV() {
		return Math.abs(correctedVariance-Math.pow(correctedCovarianceCV,2.0)/correctedVarianceCV);
	}		
	
	// Beachte: Die 14te Stelle in Excel ist ungenau daher signifikante Abweichungen beim Quadrieren der Werte ggf. sollte überall die numerische Präzision noch einmal geprüft werden (Insbedonere relevant bei Konfidenzintervallberechnung)
	// Berechne Konfidenzinterval mit CV Wurzel(Varianz mit CV)*Gaussian.inverseCDF(0.025)/Wurzel(n)
	protected double calcConfidenceLevelCV() {
		return -Gaussian.inverseCDF(0.025)*Math.pow(varianceValueWithCV/countValues,0.5);
	}
	
	// Berechne  Schätzer (Korrektur nach Mittelwerten = Mittelwert Wert / Mittelwert (Korrketurwert) * EW // Annahme: Es wird der selbe Wert wie für CV herangezogen 
	protected double calcMeanCorrectedByMean() {
		return arithmeticMeanValue/arithmeticMeanCV*valueCE_CV_EW;
	}		
	
	

	
	
	
	/**
	 * @return ArrayList der relevanten SchaetzerMittelwerte
	 */
	// TODO: Warnungen The serializable class does not declare a static final serialVersionUID field of type long prüfen
	@SuppressWarnings("serial")
	public ArrayList<Object> getSchaetzerMittelwerte() {
		return new ArrayList<Object>(){{
			add(ScenarioID);
			add(kennzahl);
			add(valueCE);
			add(arithmeticMeanValue);
			add(confidenceLevel);
			add(meanValueWithCV);
			add(confidenceLevelCV);
			add(meanCorrectedByMean);
			}};	
	}
	
	
	// @Deprecated
	/**
	 * @return ArrayList der relevanten SchaetzerMittelwerte 8Alte Fassung)
	 */
	// TODO: Warnungen The serializable class does not declare a static final serialVersionUID field of type long prüfen
	@SuppressWarnings("serial")
	public ArrayList<Object> getSchaetzerMittelwerteOld() {
		return new ArrayList<Object>(){{
			add(ScenarioID);
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
	

	/**
	 * @return Titel für die Anzeige der Mittelung in der GUI
	 */
	public static List<String> getTitle() {
		final List<String> alleTitel = new ArrayList<>();
		alleTitel.add("<html><p>Stress-Szenario</p><br/><p></p></html>");
		alleTitel.add("<html><p>Kennzahl</p><br/><p></p></html>");
		alleTitel.add("<html><p>Wert unter dem CE</p><br/><p></p></html>");
		alleTitel.add("<html><p>empirisches Mittel</p><br/><p></p></html>");	
		alleTitel.add("<html><p>Konfidenzintervall empirisches Mittel</p><br/><p></p></html>");
		alleTitel.add("<html><p>Mittelwert Control Variates (CV)</p><br/><p></p></html>");
		alleTitel.add("<html><p>Konfidenzintervall Mittelwert CV</p><br/><p></p></html>");
		alleTitel.add("<html><p>empirisches Mittel (Korrektur nach Mittelwerten)</p><br/><p></p></html>");
		return alleTitel;
	}

	
	
	// @Deprecated
	/**
	 * @param ausgabe
	 * @param schaetzerMittelwerte
	 * @throws FileNotFoundException
	 */
	public static void writeSchaeterMittelwerteWithValues(final File ausgabe, final TreeMap<Integer, TreeMap<String,KennzahlenMittelung>> schaetzerMittelwerte)
			throws FileNotFoundException {
			
		try (final PrintStream out = new PrintStream(new FileOutputStream(ausgabe))) {
			out.println("Stress-szenario;Kennzahl;Antithetische Variablen;Wert unter dem CE;empirisches Mittel;Konfidenzintervall empirisches Mittel;Mittelwert Control Variates (CV);Konfidenzintervall Mittelwert CV;empirisches Mittel (Korrektur nach Mittelwerten)");
			for (Map.Entry<Integer, TreeMap<String,KennzahlenMittelung>> szenario : schaetzerMittelwerte.entrySet()) {
				for(Map.Entry<String,KennzahlenMittelung> kennzahl : szenario.getValue().entrySet()) {
					for (Object element : kennzahl.getValue().getSchaetzerMittelwerteOld()) {
						try{
							out.print(element.toString()+";");
						}
						catch(NullPointerException e){}
					}
					out.println("");
				}
			}
		}
	}
	// @Deprecated
	/**
	 * @param ausgabe
	 * @param schaetzerMittelwerteLoB
	 * @throws FileNotFoundException
	 */
	public static void writeSchaeterMittelwerteLoBWithValues(File ausgabe, TreeMap<Integer,TreeMap<String,TreeMap<String,KennzahlenMittelung>>> schaetzerMittelwerteLoB) throws FileNotFoundException{	
		try (final PrintStream out = new PrintStream(new FileOutputStream(ausgabe))) {
			out.println("Stress-szenario;LOB;Kennzahl;Antithetische Variablen;Wert unter dem CE;empirisches Mittel;Konfidenzintervall empirisches Mittel;Mittelwert Control Variates (CV);Konfidenzintervall Mittelwert CV;empirisches Mittel (Korrektur nach Mittelwerten)");
			for (Map.Entry<Integer,TreeMap<String,TreeMap<String,KennzahlenMittelung>>> szenario : schaetzerMittelwerteLoB.entrySet()) {
				for(Map.Entry<String,TreeMap<String,KennzahlenMittelung>> lob : szenario.getValue().entrySet()) {
					for(Map.Entry<String,KennzahlenMittelung> kennzahl : lob.getValue().entrySet()) {
						for (Object element : kennzahl.getValue().getSchaetzerMittelwerteOld()) {
							try{
								out.print(element.toString()+";");
							}
							catch(NullPointerException e){}
						}
						out.println("");
					}
				}
			}
		}
		
	}
	
}
