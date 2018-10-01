package de.gdv.bsm.intern.szenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.gdv.bsm.intern.applic.DummyFortschritt;
import de.gdv.bsm.intern.applic.RechenFortschrittInterface;

/**
 * Ein Szenariensatz mit den Zinskurven für einen Pfad.
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
public class Szenario {
	private static String dateiName = "Szenariensatz_Sznr%.csv";

	// Id dieses Szenarios
	@SuppressWarnings("unused")
	private final int id;
	// Header-Zeilen des Szenarios
	private final List<String> header = new ArrayList<>();
	// alle bisher gelesenen Pfade
	final Map<Integer, Pfad> pfade = new HashMap<Integer, Pfad>();
	// indiziert mit 0 statt 1!
	private final double[] zskSzenario;
	/** Maximale Restlaufzeit. $B$5. */
	public final int maximaleRestlaufzeit;
	/** Stressszenario ID. $B$6. */
	public final int stressSzenario;
	/** Projektionshorizont ID. $D$6. */
	public final int projektionsHorizont;
	
	/**
	 * Handelt es isch um einen Szenariensatz mit antithetischen Variablen? wahr / falsch
	 */
	public final boolean antitethischeVariablen;
	
	// geöffneter FileStream
	private final BufferedReader bufferedReader;

	/**
	 * Erstelle den Dateinamen für die Datei zu einer Szenario-Id.
	 * 
	 * @param id
	 *            die Szenario-Id
	 * @return der Name
	 */
	public static String getName(final int id) {
		return dateiName.replace("%", String.valueOf(id));
	}

	/**
	 * Erstelle und lade einen Szenariensatz. Es wird keine Fortschrittsanzeige bestückt. Ist der Konstruktor beendet,
	 * sind alle gewünschten Daten gefüllt.
	 * 
	 * @param baseDir
	 *            in dem die Datei steht
	 * @param id
	 *            des zu lesenden Szenarios
	 * @param bisPfad
	 *            maximaler Pfad, der gelesen werden soll
	 * @throws IOException
	 *             bei IO-Fehlern
	 */
	public Szenario(final File baseDir, final int id, final int bisPfad) throws IOException {
		this(baseDir, id, bisPfad, new DummyFortschritt());
	}

	/**
	 * Lade einen Szenariensatz. Ist der Konstruktor beendet, sind alle gewünschten Daten gefüllt.
	 * 
	 * @param baseDir
	 *            in dem die Datei steht
	 * @param id
	 *            des zu lesenden Szenarios
	 * @param bisPfad
	 *            maximaler Pfad, der gelesen werden soll
	 * @param projektionsHorizont
	 *            maximale Jahreszahl (zur Zeit immer 100)
	 * @param fortschritt
	 *            Fortschrittsanzeige für das Laden
	 * @throws IOException
	 *             bei IO-Fehlern
	 */
	public Szenario(File baseDir, final int id, final int bisPfad, final RechenFortschrittInterface fortschritt)
			throws IOException {
		
		this.id = id;

		fortschritt.setSznrPercent(id, 0);
		
		//Trunkiere den Beispieldateinamen um an das wirkliche baseDir und den wirklichen Scenariodateinamen zu kommen
		
		Pattern pattern = Pattern.compile("^(.*\\\\)(.*Szenariensatz_Sznr).*\\.csv$");
		Matcher matcher = pattern.matcher(baseDir.getAbsolutePath());

		try {
			matcher.find();
		    baseDir=new File(matcher.group(1));
		    dateiName=matcher.group(2)+"%.csv";
		}
		catch(java.lang.IllegalStateException e) {
			throw new FileNotFoundException(baseDir.getAbsolutePath()+"(Das System kann keine entsprechend benannten Scenariosätze finden! Bitte wählen Sie als Referenzscenario eine der Dateien mit ESG Datensätzen und stellen Sie sicher, dass diese jeweils mit dem Suffix Szenariensatz_Sznr<ID>.csv benannt sind.)");
		}
		
		
		final File dataFile = new File(baseDir, getName(id));

		bufferedReader = new BufferedReader(new FileReader(dataFile), 32 * 1024);
		String line;
		int semikolon = 0;

		{
			// ZSK-Szenario (Zeile 1)
			line = bufferedReader.readLine();
			header.add(line);
			line = line.replace(',', '.');
			line = line.substring(line.indexOf(';') + 1);
			final List<Double> data = new ArrayList<>();
			semikolon = 0;
			while (semikolon >= 0) {
				semikolon = line.indexOf(';');
				data.add(Parse.parseDouble(line, semikolon));
				line = line.substring(semikolon + 1);
			}
			zskSzenario = new double[data.size()];
			for (int i = 0; i < data.size(); ++i) {
				zskSzenario[i] = data.get(i);
			}
		}
		{
			// Bsis-ZSK (Zeile 2)
			line = bufferedReader.readLine();
			header.add(line);
		}
		{
			// Aktienvola (Zeile 3)
			line = bufferedReader.readLine();
			header.add(line);
		}
		{
			// Immobilienvola (Zeile 4)
			line = bufferedReader.readLine();
			header.add(line);
		}
		{
			// diverse Kennziffern (Zeile 5)
			line = bufferedReader.readLine();
			header.add(line);
			line = skipSemikolon(line);

			semikolon = line.indexOf(';');
			maximaleRestlaufzeit = Integer.parseInt(line.substring(0, semikolon).trim());
			line = line.substring(semikolon + 1);
		}
		{
			// diverse Kennziffern (Zeile 6)
			line = bufferedReader.readLine();
			header.add(line);

			line = skipSemikolon(line);
			semikolon = line.indexOf(';');
			stressSzenario = Integer.parseInt(line.substring(0, semikolon).trim());
			if (stressSzenario != id) {
				throw new IllegalArgumentException(
						"SzenarioId in Datei (" + this.stressSzenario + ") weicht ab vom Parameter: " + id);
			}

			line = line.substring(semikolon + 1);

			line = skipSemikolon(line);
			semikolon = line.indexOf(';');
			this.projektionsHorizont = Integer.parseInt(line.substring(0, semikolon).trim());
			// if (this.projektionsHorizont != projektionsHorizont) {
			// throw new IllegalArgumentException("Projektionshorizont in Datei (" + this.projektionsHorizont
			// + ") weicht ab vom Parameter: " + projektionsHorizont);
			// }
			
			
			
			line = line.substring(semikolon + 1);
			
			
			// Lese und initialisiere Wert, ob antithetische Varaiblen verwendet wurden
			semikolon = line.indexOf(';');
			line = line.substring(semikolon + 1);
			semikolon = line.indexOf(';');
			String antitethischeVariablenString=line.substring(0, semikolon).trim();
			int antitethischeVariablenInt=0;
			try{
				antitethischeVariablenInt=Integer.parseInt(antitethischeVariablenString);
			} catch(NumberFormatException e){}
			if (antitethischeVariablenString.equals("WAHR") || antitethischeVariablenString.equals("TRUE") || antitethischeVariablenInt==1) {
				this.antitethischeVariablen=true;
			}
			else {
				this.antitethischeVariablen=false;
			}
			line = line.substring(semikolon + 1);
		}
		{
			// diverse Kennziffern und Header, die (noch) nicht benötigt werden:
			line = bufferedReader.readLine();
		}

		fuelle(bisPfad, fortschritt);
	}

	private void fuelle(final int bisPfad, final RechenFortschrittInterface fortschritt) throws IOException {
		// aktuell gelesene Pfad Nummer
		int currentPfadNummer = -1;

		// eine Zeile in den Puffer vorlesen:
		String bufferedLine = bufferedReader.readLine();

		int lastPercent = 0;
		final Optional<Integer> maxPfad = pfade.keySet().stream().max((x, y) -> x - y);
		final int pfadeZuLesen;
		if (maxPfad.isPresent()) {
			pfadeZuLesen = bisPfad - maxPfad.get();
		} else {
			pfadeZuLesen = bisPfad + 1;
		}

		if (pfadeZuLesen < 1) {
			fortschritt.setSznrPercent(stressSzenario, 100);
			return;
		}

		int pfadeGelesen = 0;

		List<PfadZeile> pfadZeilen = new ArrayList<>(200);
		int pfadNummer = Integer.MAX_VALUE;

		boolean stop = false;

		while (bufferedLine != null) {
			final int posPfad = bufferedLine.indexOf(';');
			pfadNummer = Integer.parseInt(bufferedLine.substring(0, posPfad));
			if (pfadNummer != currentPfadNummer && currentPfadNummer >= 0) {
				if (fortschritt.isAbbruch()) {
					// nur nach komplettem Pfad wird abgebrochen:
					stop = true;
				}
				// Pfade wurden bereits gelesen:
				if (!pfade.containsKey(currentPfadNummer)) {
					final Pfad pfad = new Pfad(currentPfadNummer, pfadZeilen);
					pfade.put(currentPfadNummer, pfad);
					pfadZeilen = new ArrayList<PfadZeile>(200);

					++pfadeGelesen;

					final int percent = pfadeGelesen * 100 / pfadeZuLesen;
					if (lastPercent != percent) {
						fortschritt.setSznrPercent(stressSzenario, percent);
						lastPercent = percent;
					}
				}
			}
			if (pfadNummer <= bisPfad && !stop) {
				currentPfadNummer = pfadNummer;
				if (pfade.containsKey(currentPfadNummer))
					throw new IllegalArgumentException("Datei enthält dopplete Pfadnummer " + currentPfadNummer);
				pfadZeilen.add(new PfadZeile(pfadNummer, bufferedLine.substring(posPfad + 1), this.projektionsHorizont,
						this.maximaleRestlaufzeit));

			} else {
				break;
			}
			bufferedLine = bufferedReader.readLine();
		}
		// Dateiende benoetigt Sonderbehandlung:
		if (bufferedLine == null) {
			if (pfade.containsKey(currentPfadNummer))
				throw new IllegalArgumentException("Datei enthält dopplete Pfadnummer " + currentPfadNummer);
			final Pfad pfad = new Pfad(currentPfadNummer, pfadZeilen);
			pfade.put(currentPfadNummer, pfad);

			bufferedReader.close();
		}

		fortschritt.setSznrPercent(stressSzenario, 100);
	}

	private final String skipSemikolon(final String line) {
		return line.substring(line.indexOf(';') + 1);
	}

	/**
	 * Gebe die Daten eines Pfades zurück.
	 * 
	 * @param pfadNummer
	 *            des gewünschten Pfades
	 * @return die Daten
	 */
	public Pfad getPfad(final int pfadNummer) {
		return pfade.get(pfadNummer);
	}
	
	/**
	 * Gebe die Pfade HAsMap zurück
	 * 
	 * @return die Daten
	 */
	public Map<Integer, Pfad> getPfade() {
		return pfade;
	}
	
	
	/**
	 * Gebe die maximale Restlaufzeit eines Scenarios zurück
	 * 
	 * @param pfadNummer
	 *            des gewünschten Pfades
	 * @return die Daten
	 */
	public int getMaximaleRestlaufzeit() {
		return maximaleRestlaufzeit;
	}
	
	/**
	 * Ermittle die (pfadunabhängigen) Zsk-Daten. Szenariensatz, Zeile 1.
	 * 
	 * @param zeit
	 *            die gewünschte Zeit zwischen 1 und projektionsHorizong (inklusive)
	 * @return der Wert
	 */
	public double getZskSzenario(final int zeit) {
		if (zeit - 1 < zskSzenario.length) {
			double value = zskSzenario[zeit - 1];
			if (Double.isFinite(value))
				return value;
		}
		return 0.0;
	}

	/**
	 * Ermittle die Header-Zeilen.
	 * 
	 * @return die Zeilen
	 */
	public List<String> getHeader() {
		return Collections.unmodifiableList(header);
	}
}
