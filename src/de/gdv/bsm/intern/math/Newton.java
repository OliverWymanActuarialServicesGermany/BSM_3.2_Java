package de.gdv.bsm.intern.math;

import static java.lang.Double.NaN;
import static java.lang.Double.isFinite;
import static java.lang.Math.abs;

import java.util.function.Function;

/**
 * Nullstellenbestimmung mit dem Newton-Verfahren.
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
public class Newton {
	/**
	 * Kombinatinonsverfahren mit Intervallhalbierung und dem Newton-Verfahren zur Nullstellenbestimmung. Zuerst wird
	 * für den Startwertd <code>start</code> versucht, eine Nullstelle mit
	 * {@link #newton(Function, Function, double, double, int, boolean)} zu ermitteln.
	 * <p/>
	 * Gelingt dies nicht, werden in Schritten <code>increment</code> Intervalle um <code>start</code> anhand des
	 * Vorzeichens von <code>f</code> untersucht, ob dort eine Nullstelle vorliegt. Wenn ja, so wird mit
	 * {@link #newtonIntervall(Function, Function, double, int, double, double, boolean)} versucht, eine Nullstelle zu
	 * bestimmen.
	 * <p/>
	 * Gelingt auch dies nicht innerhalb von Intervallen der Länge <code>maxIncrement</code>, wird
	 * <code>Double.NaN</code> zurückgeliefert. Ansonsten erhält man die angegebene Nullstelle. *
	 * <p/>
	 * Es werden keine Debug-Informationen ausgegeben.
	 * 
	 * @param f
	 *            Funktion, zu der eine Nullstelle gesucht wird
	 * @param df
	 *            Ableitung von f
	 * @param start
	 * @param epsilon
	 *            gewünschte Genauigkeit für das Newton-Verfahren
	 * @param maxIter
	 *            maximale Anzahl Iterationen für das Newton-Verfahren
	 * @param increment
	 *            für die Intervallgröße
	 * @param maxIncrement
	 *            maximal Intervalllänge
	 * @return die Nullstelle, oder <code>Double.NaN</code>, wenn keine Nullstelle gefunden wurde
	 */

	public static double solve(final Function<Double, Double> f, final Function<Double, Double> df, final double start,
			final double epsilon, final int maxIter, final double increment, final double maxIncrement) {
		return solve(f, df, start, epsilon, maxIter, increment, maxIncrement, false);
	}

	/**
	 * Kombinatinonsverfahren mit Intervallhalbierung und dem Newton-Verfahren zur Nullstellenbestimmung. Zuerst wird
	 * für den Startwertd <code>start</code> versucht, eine Nullstelle mit
	 * {@link #newton(Function, Function, double, double, int, boolean)} zu ermitteln.
	 * <p/>
	 * Gelingt dies nicht, werden in Schritten <code>increment</code> Intervalle um <code>start</code> anhand des
	 * Vorzeichens von <code>f</code> untersucht, ob dort eine Nullstelle vorliegt. Wenn ja, so wird mit
	 * {@link #newtonIntervall(Function, Function, double, int, double, double, boolean)} versucht, eine Nullstelle zu
	 * bestimmen.
	 * <p/>
	 * Gelingt auch dies nicht innerhalb von Intervallen der Länge <code>maxIncrement</code>, wird
	 * <code>Double.NaN</code> zurückgeliefert. Ansonsten erhält man die angegebene Nullstelle.
	 * 
	 * @param f
	 *            Funktion, zu der eine Nullstelle gesucht wird
	 * @param df
	 *            Ableitung von f
	 * @param start
	 * @param epsilon
	 *            gewünschte Genauigkeit für das Newton-Verfahren
	 * @param maxIter
	 *            maximale Anzahl Iterationen für das Newton-Verfahren
	 * @param increment
	 *            für die Intervallgröße
	 * @param maxIncrement
	 *            maximal Intervalllänge
	 * @param debug
	 *            sollen Debug-Informationen ausgegeben werden?
	 * @return die Nullstelle, oder <code>Double.NaN</code>, wenn keine Nullstelle gefunden wurde
	 */
	public static double solve(final Function<Double, Double> f, final Function<Double, Double> df, final double start,
			final double epsilon, final int maxIter, final double increment, final double maxIncrement,
			final boolean debug) {
		final double result = newton(f, df, start, epsilon, maxIter, debug);
		if (isFinite(result))
			return result;
		double fAkt = f.apply(start);
		if (!isFinite(fAkt)) {
			throw new IllegalArgumentException("Funktionswert am Startpunkt nicht endlich.");
		}
		double dist = increment;
		while (dist <= maxIncrement) {
			if (debug)
				System.out.println("checking dist " + dist);
			if (f.apply(start - dist) * fAkt < 0) {
				return newtonIntervall(f, df, epsilon, maxIter, start - dist, start, debug);
			} else if (f.apply(start + dist) * fAkt < 0) {
				return newtonIntervall(f, df, epsilon, maxIter, start, start + dist, debug);
			}
			dist += increment;
		}
		return NaN;
	}

	/**
	 * Kombinationsverfahren mit Intervallhalbierung und Newton. Es muss zwischen <code>min</code> und <code>max</code>
	 * eine Nullstelle liegen. Es wird auf der Intervallhälfte eine Newton-Iteration versucht. Konvergiert das Verfahren
	 * nicht oder nicht schnell genug, wird das Intervall halbiert und das Verfahren dort wiederholt.
	 * <p/>
	 * Es werden keine Debug-Informationen ausgegeben.
	 * 
	 * @param f
	 *            Funktion, zu der eine Nullstelle gesucht wird
	 * @param df
	 *            Ableitung von f
	 * @param epsilon
	 *            gewünschte Genauigkeit für das Newton-Verfahren
	 * @param maxIter
	 *            maximale Anzahl Iterationen für das Newton-Verfahren
	 * @param min
	 *            untere Grenze des Intervalls, in dem die Nullstelle gesucht wird
	 * @param max
	 *            obere Grenze des Intervalls, in dem die Nullstelle gesucht wird
	 * 
	 * @return die Nullstelle, oder <code>Double.NaN</code>, wenn keine Nullstelle gefunden wurde
	 */
	public static double newtonIntervall(final Function<Double, Double> f, final Function<Double, Double> df,
			final double epsilon, final int maxIter, final double min, final double max) {
		return newtonIntervall(f, df, epsilon, maxIter, min, max, false);
	}

	/**
	 * Kombinationsverfahren mit Intervallhalbierung und Newton. Es muss zwischen <code>min</code> und <code>max</code>
	 * eine Nullstelle liegen. Es wird auf der Intervallhälfte eine Newton-Iteration versucht. Konvergiert das Verfahren
	 * nicht oder nicht schnell genug, wird das Intervall halbiert und das Verfahren dort wiederholt.
	 * 
	 * @param f
	 *            Funktion, zu der eine Nullstelle gesucht wird
	 * @param df
	 *            Ableitung von f
	 * @param epsilon
	 *            gewünschte Genauigkeit für das Newton-Verfahren
	 * @param maxIter
	 *            maximale Anzahl Iterationen für das Newton-Verfahren
	 * @param min
	 *            untere Grenze des Intervalls, in dem die Nullstelle gesucht wird
	 * @param max
	 *            obere Grenze des Intervalls, in dem die Nullstelle gesucht wird
	 * @param debug
	 *            sollen Debug-Informationen ausgegeben werden?
	 * @return die Nullstelle, oder <code>Double.NaN</code>, wenn keine Nullstelle gefunden wurde
	 */
	public static double newtonIntervall(final Function<Double, Double> f, final Function<Double, Double> df,
			final double epsilon, final int maxIter, final double min, final double max, final boolean debug) {
		if (debug)
			System.out.println("solve in [" + min + ", " + max + "]");
		final double current = (max + min) / 2.0;
		final double result = newton(f, df, current, epsilon, maxIter, debug);
		if (isFinite(result)) {
			return result;
		}
		if (f.apply(current) * f.apply(max) > 0.0) {
			return newtonIntervall(f, df, epsilon, maxIter, min, current, debug);
		}
		return newtonIntervall(f, df, epsilon, maxIter, current, max, debug);
	}

	/**
	 * Newton-Verfahren zur Nullstellenbestimmung. Das Verfahren endet positiv, denn gilt
	 * <code>abs(f.apply(x)) <= epsilon</code>, wobei <code>x</code> ein Schätzwert für die Nullstelle ist. Treten
	 * während der Iteraton nicht-endliche Werte auf, oder konvergiert das Verfahren nicht nach der Maximalzahl
	 * gewünschter Schritte, so wird <code>Doble.NaN</code> zurückgegeben. Andernfalls wird die Lösung zurückgeliefert.
	 * <p/>
	 * Es werden keine Debug-Informationen ausgegeben.
	 * 
	 * @param f
	 *            die Funktion, zu der eine Nullstelle gesucht wird
	 * @param df
	 *            die Ableitung der Funktion f
	 * @param start
	 *            Startwert für die Newton-Iteration (Näherung der Nullstelle)
	 * @param epsilon
	 *            gewünschte Genauigkeit
	 * @param maxIter
	 *            maximale Anzahl Iterationen
	 * @return die Nullstelle, oder <code>Double.NaN</code>, wenn keine Nullstelle gefunden wurde
	 */
	public static double newton(final Function<Double, Double> f, final Function<Double, Double> df, final double start,
			final double epsilon, final int maxIter) {
		return newton(f, df, start, epsilon, maxIter, false);
	}

	/**
	 * Newton-Verfahren zur Nullstellenbestimmung. Das Verfahren endet positiv, denn gilt
	 * <code>abs(f.apply(x)) <= epsilon</code>, wobei <code>x</code> ein Schätzwert für die Nullstelle ist. Treten
	 * während der Iteraton nicht-endliche Werte auf, oder konvergiert das Verfahren nicht nach der Maximalzahl
	 * gewünschter Schritte, so wird <code>Doble.NaN</code> zurückgegeben. Andernfalls wird die Lösung zurückgeliefert.
	 * 
	 * @param f
	 *            die Funktion, zu der eine Nullstelle gesucht wird
	 * @param df
	 *            die Ableitung der Funktion f
	 * @param start
	 *            Startwert für die Newton-Iteration (Näherung der Nullstelle)
	 * @param epsilon
	 *            gewünschte Genauigkeit
	 * @param maxIter
	 *            maximale Anzahl Iterationen
	 * @param debug
	 *            sollen Debug-Informationen ausgegeben werden?
	 * @return die Nullstelle, oder <code>Double.NaN</code>, wenn keine Nullstelle gefunden wurde
	 */
	public static double newton(final Function<Double, Double> f, final Function<Double, Double> df, final double start,
			final double epsilon, final int maxIter, final boolean debug) {

		int iter = maxIter;
		double run = start;
		double fAkt = f.apply(run);
		while (iter > 0 && isFinite(run) && isFinite(fAkt) && abs(fAkt) > epsilon) {
			if (debug)
				System.out.println(String.format("%25.20f => %40.20f", run, fAkt));
			run -= fAkt / df.apply(run);
			fAkt = f.apply(run);
			--iter;
		}
		if (debug)
			System.out.println(String.format("%25.20f => %40.20f", run, f.apply(run)));

		if (abs(fAkt) <= epsilon)
			return run;
		return NaN;
	}
}
