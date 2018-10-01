package de.gdv.bsm.intern.szenario;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Zeile eines Pfades. Dies entspricht einem Zeitschritt.
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
public class PfadZeile {
	final static NumberFormat numberFormat = new DecimalFormat();

	/** Nummer des Pfades. A. */
	public final int pfadNummer;
	/** Zeitschritt. B. */
	public final int zeit;
	/** Diskontfunktion. C. */
	public final double diskontFunktion;
	/** Aktien. D. */
	public final double aktien;
	/** Dividenden. E. */
	public final double dividenden;
	/** Immobilien. F. */
	public final double immobilien;
	/** Mieten. G. */
	public final double mieten;
	/** 10j Spotrate für ZZR. H. */
	public final double spotrate10jZZR;
	/** Shortrate. I. */
	public final double shortrate;

	private final int maxValues;
	private final double[] spotRlz;
	private final double[] kuponRlz;

	/**
	 * Erstelle eine Zeile.
	 * 
	 * @param pfadNummer
	 *            des Pfades
	 * @param dataIn
	 *            Eingabedaten
	 * @param projetkionsHorizont
	 *            maximale Zeit
	 * @param maximaleRestlaufzeit
	 *            definierte Restlaufzeit
	 */
	public PfadZeile(final int pfadNummer, final String dataIn, final int projetkionsHorizont,
			final int maximaleRestlaufzeit) {
		this.pfadNummer = pfadNummer;

		StringBuilder data = new StringBuilder(dataIn.replace(',', '.'));
		int semikolon, start;

		start = 0;
		semikolon = nextSemiColon(data, start);
		zeit = Integer.parseInt(data.substring(start, semikolon));
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		diskontFunktion = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		aktien = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		dividenden = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		immobilien = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		mieten = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		spotrate10jZZR = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		semikolon = nextSemiColon(data, start);
		shortrate = Parse.parseDouble2(data, start, semikolon);
		start = semikolon + 1;

		// die Werte sind nur in der oberen linken Hälfte der Matrix
		// deshalb können wir kompatker speichern
		maxValues = projetkionsHorizont - zeit;

		spotRlz = new double[maxValues];
		for (int i = 0; i < maximaleRestlaufzeit; ++i) {
			semikolon = nextSemiColon(data, start);

			if (i < maxValues) {
				// andere Werte werden ohme Prüfung abgeschnitten:
				final double v = Parse.parseDouble2(data, start, semikolon);
				spotRlz[i] = v;
			}
			start = semikolon + 1;
		}

		kuponRlz = new double[maxValues];
		for (int i = 0; i < maximaleRestlaufzeit; ++i) {
			semikolon = nextSemiColon(data, start);
			if (i < maxValues) {
				// andere Werte werden ohme Prüfung abgeschnitten:
				final double v = Parse.parseDouble2(data, start, semikolon);
				kuponRlz[i] = v;
			}
			start = semikolon + 1;
		}
	}

	private int nextSemiColon(final StringBuilder sb, int start) {
		while (start < sb.length() && sb.charAt(start) != ';')
			++start;
		return start;
	}

	/**
	 * Ermittle den Zeitpunkt
	 * 
	 * @return die Zeit
	 */
	public int getZeit() {
		return zeit;
	}
	
	/**
	 * Ermittle den Spot RLZ.
	 * 
	 * @param i
	 *            die Zeit
	 * @return der Wert
	 */
	public double getSpotRlz(final int i) {
		if (i == 0 || i > maxValues)
			return 0.0;
		return spotRlz[i - 1];
	}

	/**
	 * Ermittle den Spot RLZ.
	 * 
	 * @param i
	 *            die Zeit
	 * @return der Wert
	 */
	public double getKuponRlz(final int i) {
		if (i == 0 || i > maxValues)
			return 0.0;
		return kuponRlz[i - 1];
	}
}
