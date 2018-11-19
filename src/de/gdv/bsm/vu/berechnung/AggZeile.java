package de.gdv.bsm.vu.berechnung;

import static de.gdv.bsm.vu.module.Functions.nanZero;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.gdv.bsm.intern.applic.TableField;
import de.gdv.bsm.intern.applic.TableField.TestOption;
import de.gdv.bsm.intern.params.MW;
import de.gdv.bsm.intern.params.MWZeile;
import de.gdv.bsm.intern.params.ReferenzZinssatz;
import de.gdv.bsm.intern.params.ZeitunabhManReg;
import de.gdv.bsm.intern.rechnung.CheckData;
import de.gdv.bsm.intern.rechnung.ResultNotFinite;
import de.gdv.bsm.intern.szenario.PfadZeile;
import de.gdv.bsm.intern.szenario.Szenario;
import de.gdv.bsm.vu.module.Bilanzpositionen;
import de.gdv.bsm.vu.module.Deklaration;
import de.gdv.bsm.vu.module.EsgFormeln;
import de.gdv.bsm.vu.module.Functions;
import de.gdv.bsm.vu.module.KaModellierung;
import de.gdv.bsm.vu.module.MIL_Funktionen;
import de.gdv.bsm.vu.module.Rohueberschuss;

//OW_D.Hohmann
import de.gdv.bsm.vu.module.OW_Funktionen;


/**
 * Simuliert eine Zeile des Blattes agg. Weiter werden hier auch die Daten der Blätter <b>VT Klassik MW</b> und <b>FI
 * CFs</b> vorgehalten.
 * <p>
 * Die Daten sind in der Regel Package-Lokal. Insbesondere für die Berechnung der Kennzahlen, die im Package
 * <code>de.gdv.bsm.vu</code> liegt, werden get-Funktionen benötigt. Zur Zeit sind nur die für die
 * Standard-Implementierung benögigten get-Funktionen implementiert. Dies kann aber bei Bedarf leicht erweitert werden.
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
public class AggZeile {
	/**
	 * Initialisierung von Double-Werten. Sollte produktiv Double.NaN sein, kann zu Testzwecken aber geändert werden
	 */
	public static final double DOUBLE_INIT = Double.NaN;

	/** Übergeordnete Berechnung für allgemeine Parameter. */
	private final Berechnung berechnung;
	/** rzg-Zeilen, die von diesem Agg Aggregiert werden */
	private final List<RzgZeile> rzgZeilen;
	/** flv-Zeilen zur selben Zeit. */
	private final List<FlvZeile> flvZeilen;

	/** Zeilicher Vorgänger zu dieser Zeile. */
	public final AggZeile vg;
	/** Zeilicher Nachfolger zu dieser Zeile. */
	AggZeile nf = null;

	// Blatt VT Klassik MW ================================================
	// Die Spalte B ist hier nicht vorhanden, da diese MW_VT in agg (Spalte FX)
	// entsprechen.
	/** Anpassung der KA-Aufwendungen. VT Klassik MW!E, Rekursiv2. */
	public double aKaAufwendungen = DOUBLE_INIT;
	/** Anpassung durch VN-Verhalten. VT Klassik MW!D, Rekursiv2. */
	public double aVN = DOUBLE_INIT;
	/** Lock-In-Faktor. VT Klassik MW!C, Rekursiv2. */
	public double lockInFaktor = DOUBLE_INIT;

	// Blatt FI CFs =======================================================
	/**
	 * CF FIs zur selben Zeit. Indiziert über die Restlaufzeit. Blatt FI CFs, Spalten C bis CY.
	 */
	private double[] cfFis;
	// Blatt FI MW =======================================================
	/**
	 * CF FI Zeitschrittig zur selben Zeit. Indiziert über die Restlaufzeit. Blatt FI MW, Spalten C bis CY.
	 */
	private double[] cfFiZeitschrittig;
	/** MW FI (Endes des Jahres). Blatt FI MW, Spalte B. */
	public double mwFiJahresende = DOUBLE_INIT;

	// Spalte A - Z =======================================================
	/** Stressszenario. */
	@TableField(testColumn = "A")
	final String szenario;
	/** Stressszenario ID */
	@TableField(testColumn = "B")
	final int szenarioId;
	/** Zeit */
	@TableField(testColumn = "C")
	public final int zeit;
	/** Kosten. */
	@TableField(testColumn = "D", nachKomma = 2)
	final double kAgg;
	/** Prämien. */
	@TableField(testColumn = "E", nachKomma = 2)
	final double bAgg;
	/** Leistungen beim Tod. */
	@TableField(testColumn = "F", nachKomma = 2)
	final double lTodAgg;
	/** Kapitalabfindungen, nur Rentenversicherung. */
	@TableField(testColumn = "G", nachKomma = 2)
	final double kaAgg;
	/** Sonstige Erlebensfallleistungen. */
	@TableField(testColumn = "H", nachKomma = 2)
	final double lSonstErlAgg;
	/** Rückkauf. */
	@TableField(testColumn = "I", nachKomma = 2)
	final double rkAgg;
	/** Risikoüberschüsse. */
	@TableField(testColumn = "J", nachKomma = 2)
	final double rueAgg;
	/** Kostenüberschüsse. */
	@TableField(testColumn = "K", nachKomma = 2)
	final double kueAgg;
	/** CF EVU -> RVU. */
	@TableField(testColumn = "L", nachKomma = 0)
	final double cfEvuRvu;
	/** Zinsratenzuschlag. M, */
	@TableField(testColumn = "M", nachKomma = 2)
	final double zinsratenzuschlaegeAgg;
	/** Rechnungsmässiger Zinsaufwand. */
	@TableField(testColumn = "N", nachKomma = 2)
	final double rmzEingabeAgg;
	/** HGB DRSt inkl. Ansammlungsguthaben ohne ZZR. */
	@TableField(testColumn = "O", nachKomma = 0)
	final double hgbDrAgg;
	
	/**  Übertrag festgelegte RfB (Im BSM als Teil der HGB DRSt repräsentiert) in freie RfB  aggregiert. */
	@TableField(testColumn = "P", nachKomma = 0)
	final double uebertragFestgelegteInFreieRfBAgg;
	
	/** ZZR UEB, alt. */
	@TableField(testColumn = "Q", testOption = TestOption.START, nachKomma = 0)
	double zzrAlt;
	/** ZZR UEB, neu. */
	@TableField(testColumn = "R", testOption = TestOption.START, nachKomma = 0)
	double zzrNeu;
	/** ZZR - NÜB. */
	@TableField(testColumn = "S", testOption = TestOption.START, nachKomma = 0)
	double zzrNueb;
	/** ZZR. */
	@TableField(testColumn = "T", testOption = TestOption.START, nachKomma = 0)
	double zzrGesamt = DOUBLE_INIT;
	/** SÜAF, alt. */
	@TableField(testColumn = "U", testOption = TestOption.START, nachKomma = 0)
	double sueAfAlt = DOUBLE_INIT;
	/** SÜAF, neu. */
	@TableField(testColumn = "V", testOption = TestOption.START, nachKomma = 0)
	double sueAfNeu = DOUBLE_INIT;
	/** SÜAF. */
	@TableField(testColumn = "W", testOption = TestOption.START, nachKomma = 0)
	double sueAf = DOUBLE_INIT;
	/** freie RfB. */
	@TableField(testColumn = "X", testOption = TestOption.START, nachKomma = 0)
	double fRfBFrei = DOUBLE_INIT;
	/** nicht festgelegte RfB. */
	@TableField(testColumn = "Y", testOption = TestOption.START, nachKomma = 0)
	double nfRfB = DOUBLE_INIT;
	/** Eigenkapital. */
	@TableField(testColumn = "Z", testOption = TestOption.START, nachKomma = 0)
	double eigenkapitalFortschreibung = DOUBLE_INIT;
	// =================================================================================
	// Spalten A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A A
	/** Nachrangige Verbindlichkeiten. */
	@TableField(testColumn = "AA", testOption = TestOption.START, nachKomma = 0)
	double grNrd = DOUBLE_INIT;
	// Spalten AB - AG stehen in Berechnung
	// deshalb hier die Dummies:
	@TableField(testColumn = "AB", suppress = true)
	private final char dummyAB = '?';
	@TableField(testColumn = "AC", suppress = true)
	private final char dummyAC = '?';
	@TableField(testColumn = "AD", suppress = true)
	private final char dummyAD = '?';
	@TableField(testColumn = "AE", suppress = true)
	private final char dummyAE = '?';
	@TableField(testColumn = "AF", suppress = true)
	private final char dummyAF = '?';
	@TableField(testColumn = "AG", suppress = true)
	private final char dummyAG = '?';
	/** HGB Latente Steuer, saldiert, Aktiva - Passiva. */
	@TableField(testColumn = "AH", testOption = TestOption.START)
	private double lsHgb = DOUBLE_INIT;
	// Spalten AH - AL stehen in Berechnung
	// deshalb hier die Dummies:
	@TableField(testColumn = "AI", suppress = true)
	private final char dummyAI = '?';
	@TableField(testColumn = "AJ", suppress = true)
	private final char dummyAJ = '?';
	@TableField(testColumn = "AK", suppress = true)
	private final char dummyAK = '?';
	@TableField(testColumn = "AL", suppress = true)
	private final char dummyAL = '?';
	@TableField(testColumn = "AM", suppress = true)
	private final char dummyAM = '?';
	/** Zinsen, Nachrangliche Verbindlichkeiten. */
	@TableField(testColumn = "AN", nachKomma = 0)
	double zinsen = DOUBLE_INIT;
	/** Rückzahlung, Nachrangliche Verbindlichkeiten. */
	@TableField(testColumn = "AO", nachKomma = 0)
	double rueckZahlung = DOUBLE_INIT;
	/** Rechnungsabgrenzungsposten. */
	@TableField(testColumn = "AP", nachKomma = 0)
	double rapVT = DOUBLE_INIT;
	/** Korrigierter FI Buchwert, aktueller Bestand Klassik. */
	@TableField(testColumn = "AQ", testOption = TestOption.START, nachKomma = 0)
	double bwFiAkt = DOUBLE_INIT;
	/** FI Buchwert Neuanlage im Zeitpunkt t. */
	@TableField(testColumn = "AR", testOption = TestOption.START, nachKomma = 0)
	double bwFiNeuAn = DOUBLE_INIT;
	private double[] bwFiNeuAnArr;
	/** FI Restlaufzeit der Neuanlage. */
	@TableField(testColumn = "AS")
	int rlz = Integer.MAX_VALUE;
	/** Zeitpunkt der Fälligkeit der Neuanlage. */
	@TableField(testColumn = "AT")
	int zpFaelligkeit = Integer.MAX_VALUE;
	private int[] zpFaelligkeitArr;
	/** FI Buchwert gesamt, Verrechnung. */
	@TableField(testColumn = "AU", testOption = TestOption.START, nachKomma = 0)
	double bwFiVerrechnung = DOUBLE_INIT;
	/** FI Kupon der Neuanlage in t. */
	@TableField(testColumn = "AV", nachKomma = 3, percent = true)
	double kuponEsg = DOUBLE_INIT;
	private double[] kuponEsgArr;
	/** Korrigierter EQ Buchwert, Klassik. */
	@TableField(testColumn = "AW", testOption = TestOption.START, nachKomma = 0)
	double bwEq = DOUBLE_INIT;
	/** Neuanlage RE, Buchwert, Klassik. */
	@TableField(testColumn = "AX", testOption = TestOption.START, nachKomma = 0)
	double bwReNeuAnl = DOUBLE_INIT;
	/** Korrigierter RE Buchwert, Klassik. */
	@TableField(testColumn = "AY", testOption = TestOption.START, nachKomma = 0)
	double bwRe = DOUBLE_INIT;
	/** Korrigierter EQ Anschaffungswert. */
	@TableField(testColumn = "AZ", testOption = TestOption.START, nachKomma = 0)
	double awEq = DOUBLE_INIT;
	
	// =================================================================================
	// Spalten B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B B
	
	/** Korrigierter RE Anschaffungswert. */
	@TableField(testColumn = "BA", testOption = TestOption.START, nachKomma = 0)
	double awRe = DOUBLE_INIT;
	/** Korrigierter EQ Marktwert, Klassik. */
	@TableField(testColumn = "BB", testOption = TestOption.START, nachKomma = 0)
	double mwEq = DOUBLE_INIT;
	/** Korrigierter RE Marktwert, Klassik. */
	@TableField(testColumn = "BC", testOption = TestOption.START, nachKomma = 0)
	double mwRe = DOUBLE_INIT;
	/** Korrigierter FI Marktwert, Klassik. */
	@TableField(testColumn = "BD", testOption = TestOption.START, nachKomma = 0)
	double mwFianfangJ = DOUBLE_INIT;
	// Spalten BD - BE stehen in Berechnung
	// deshalb hier die Dummies:
	@TableField(testColumn = "BE", suppress = true)
	private final char dummyBE = '?';
	@TableField(testColumn = "BF", suppress = true)
	private final char dummyBF = '?';
	/** Zielanteil FI. */
	@TableField(testColumn = "BG", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aFiZielDaa = DOUBLE_INIT;
	/** Mindestanteil FI. */
	@TableField(testColumn = "BH", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aFiMinDaa = DOUBLE_INIT;
	/** RE Zielanteil. BI, L 1. */
	@TableField(testColumn = "BI", nachKomma = 0, percent = true)
	double aReZielDaa = DOUBLE_INIT;
	/** EQ Preis. */
	@TableField(testColumn = "BJ", nachKomma = 0, percent = true)
	double preisAktieEsg = DOUBLE_INIT;
	/** RE Preis. */
	@TableField(testColumn = "BK", nachKomma = 0, percent = true)
	double preisImmoEsg = DOUBLE_INIT;
	/** FI Cash Flow aktueller Bestand. */
	@TableField(testColumn = "BL", nachKomma = 0)
	double cfFiAkt = DOUBLE_INIT;
	/** FI Kapitalertrag aktueller Bestand. */
	@TableField(testColumn = "BM", nachKomma = 0)
	double keFiAkt = DOUBLE_INIT;
	/** FI Cash Flow aus dem Neubestand. */
	@TableField(testColumn = "BN", testOption = TestOption.START, nachKomma = 0)
	double cfFiNeuAnl = DOUBLE_INIT;
	/** FI Kapitalertrag des Neubestands. */
	@TableField(testColumn = "BO", testOption = TestOption.START, nachKomma = 0)
	double keFiNeuAnl = DOUBLE_INIT;
	/** FI Cash Flow gesamt. */
	@TableField(testColumn = "BP", testOption = TestOption.START, nachKomma = 0)
	double cfFi = DOUBLE_INIT;
	/** FI Kapitalertrag gesamt. */
	@TableField(testColumn = "BQ", testOption = TestOption.START, nachKomma = 0)
	double keFi = DOUBLE_INIT;
	/** FI Buchwert gesamt. */
	@TableField(testColumn = "BR", testOption = TestOption.START, nachKomma = 0)
	double bwFiGesamtJe = DOUBLE_INIT;
	/** FI Marktwert gesamt. */
	@TableField(testColumn = "BS", testOption = TestOption.START, nachKomma = 0)
	double fiMw = DOUBLE_INIT;
	/** EQ Marktwert vor Realisierung. */
	@TableField(testColumn = "BT", testOption = TestOption.START, nachKomma = 0)
	double mwEqVorRls = DOUBLE_INIT;
	/** RE Marktwert vor Realisierung. */
	@TableField(testColumn = "BU", testOption = TestOption.START, nachKomma = 0)
	double mwReVorRls = DOUBLE_INIT;
	/** Kapitalertrag aus Dividenden, */
	@TableField(testColumn = "BV", testOption = TestOption.START, nachKomma = 0)
	double keDiv = DOUBLE_INIT;
	/** Kapitalertrag aus Mieten, */
	@TableField(testColumn = "BW", testOption = TestOption.START, nachKomma = 0)
	double keMieten = DOUBLE_INIT;
	/** EQ Kapitalertrag aus Ab- und Zuschreibungen. */
	@TableField(testColumn = "BX", testOption = TestOption.START, nachKomma = 0)
	double keEqAbUndZuschreibung = DOUBLE_INIT;
	/** RE Kapitalertrag aus Ab- und Zuschreibungen. */
	@TableField(testColumn = "BY", testOption = TestOption.START, nachKomma = 0)
	double keReAbUndZuschreibung = DOUBLE_INIT;
	/** RE laufender Kapitalertrag. */
	@TableField(testColumn = "BZ", testOption = TestOption.START, nachKomma = 0)
	double keEqLaufend = DOUBLE_INIT;
	
	// =================================================================================
	// Spalten C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C C
	
	/** EQ laufender Kapitalertrag. */
	@TableField(testColumn = "CA", testOption = TestOption.START, nachKomma = 0)
	double keReLaufend = DOUBLE_INIT;
	/** RE laufender Kapitalertrag. */
	@TableField(testColumn = "CB", testOption = TestOption.START, nachKomma = 0)
	double bwEqNachAbUndZuschreibung = DOUBLE_INIT;
	/** EQ laufender Kapitalertrag. */
	@TableField(testColumn = "CC", testOption = TestOption.START, nachKomma = 0)
	double bwReNachAbUndZuschreibung = DOUBLE_INIT;
	/** EQ BWR vor Realisierung in %. */
	@TableField(testColumn = "CD", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrEqVorRls = DOUBLE_INIT;
	/** RE BWR vor Realisierung in % */
	@TableField(testColumn = "CE", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrReVorRls = DOUBLE_INIT;
	/** EQ Ziel-BWR in %. */
	@TableField(testColumn = "CF", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrEqZiel = DOUBLE_INIT;
	/** RE Ziel-BWR in % */
	@TableField(testColumn = "CG", testOption = TestOption.START, nachKomma = 0, percent = true)
	double bwrReZiel = DOUBLE_INIT;
	/** EQ Anteil zur planmäßigen Realisierung in %. */
	@TableField(testColumn = "CH", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aEqRlsI = DOUBLE_INIT;
	/** RE Anteil zur planmäßigen Realisierung in % */
	@TableField(testColumn = "CI", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aReRlsPlan = DOUBLE_INIT;
	/** EQ Buchwert nach planmäßiger Realisierung (Senkung der EQ BWR) */
	@TableField(testColumn = "CJ", testOption = TestOption.START, nachKomma = 0)
	double bwEqRlsI = DOUBLE_INIT;
	/** RE Buchwert nach planmäßiger Realisierung (Senkung der RE BWR) */
	@TableField(testColumn = "CK", testOption = TestOption.START, nachKomma = 0)
	double bwReRlsPlan = DOUBLE_INIT;
	/** Kapitalertrag aus der Realisierung der EQ-BWR. */
	@TableField(testColumn = "CL", testOption = TestOption.START, nachKomma = 0)
	double keEqRlsI = DOUBLE_INIT;
	/** Kapitalertrag aus der Realisierung der RE-BWR */
	@TableField(testColumn = "CM", testOption = TestOption.START, nachKomma = 0)
	double keReRlsPlan = DOUBLE_INIT;
	/** Kapitalertrag/-aufwand aus der Aufzinsung der Leistungen, Kosten, Beiträgen, ZAG und Ertragsteuer. */
	@TableField(testColumn = "CN", testOption = TestOption.START, nachKomma = 0)
	double keCfAufzinsung = DOUBLE_INIT;
	/** Kapitalertrag, gesamt, nach planmäßiger Realisierung. */
	@TableField(testColumn = "CO", testOption = TestOption.START, nachKomma = 0)
	double keRlsI = DOUBLE_INIT;
	/** Buchwert, gesamt nach planmäßiger Realisierung. */
	@TableField(testColumn = "CP", testOption = TestOption.START, nachKomma = 0)
	double bwVorRls = DOUBLE_INIT;
	/** Marktwert, gesamt, nach planmäßiger Realisierung. */
	@TableField(testColumn = "CQ", testOption = TestOption.START, nachKomma = 0)
	double mwVorRls = DOUBLE_INIT;
	/** zu realisierender Anteil der Immobilien. */
	@TableField(testColumn = "CR", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aReRls = DOUBLE_INIT;
	/** RE Cashflow aus der 2. Realisierung. */
	@TableField(testColumn = "CS", testOption = TestOption.START, nachKomma = 0)
	double cfReRls = DOUBLE_INIT;
	/** RE Kapitalertrag aus der 2. Realisierung. */
	@TableField(testColumn = "CT", testOption = TestOption.START, nachKomma = 0)
	double keReRls = DOUBLE_INIT;
	/** RE Buchwert nach der 2. Realisierung. */
	@TableField(testColumn = "CU", testOption = TestOption.START, nachKomma = 0)
	double bwReNachRls = DOUBLE_INIT;
	/** RE Marktwert nach der 2. Realisierung. */
	@TableField(testColumn = "CV", testOption = TestOption.START, nachKomma = 0)
	double mwRenachRls = DOUBLE_INIT;
	/** EQ zu realisierender Anteil der 2. Realisierung. */
	@TableField(testColumn = "CW", testOption = TestOption.START, nachKomma = 0, percent = true)
	double aEqRlsII = DOUBLE_INIT;
	/** EQ Cashflow aus der 2. Realisierung. */
	@TableField(testColumn = "CX", testOption = TestOption.START, nachKomma = 0)
	double cfEqRlsII = DOUBLE_INIT;
	/** EQ Kapitalertrag aus der 2. Realisierung. */
	@TableField(testColumn = "CY", testOption = TestOption.START, nachKomma = 0)
	double keEqRlsII = DOUBLE_INIT;
	/** EQ Buchwert nach der 2. Realisierung */
	@TableField(testColumn = "CZ", testOption = TestOption.START)
	double bwEqRlsII = DOUBLE_INIT;
	/** EQ Marktwert nach der 2. Realisierung */
	
	// =================================================================================
	// Spalten D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D D

	@TableField(testColumn = "DA", testOption = TestOption.START)
	double mwEqRlsII = DOUBLE_INIT;
	/** Kapitalertrag, gesamt, nach 2. Realisierung. */
	@TableField(testColumn = "DB", testOption = TestOption.START, nachKomma = 0)
	double keRlsII = DOUBLE_INIT;
	/** Buchwert, gesamt nach 2. Realisierung. */
	@TableField(testColumn = "DC", testOption = TestOption.START, nachKomma = 0)
	double bwRlsII = DOUBLE_INIT;
	/** Marktwert, gesamt nach 2. Realisierung */
	@TableField(testColumn = "DD", testOption = TestOption.START, nachKomma = 0)
	double mwRlsII = DOUBLE_INIT;
	/** Kapitalertragsdefizite aus den Vorjahren zum Verrechnen. */
	@TableField(testColumn = "DE", testOption = TestOption.START, nachKomma = 0)
	double kedVjVerrechnen = DOUBLE_INIT;
	double[] kedVjVerrechnenArr;
	/** Kapitalertragdefizit zum Verrechnen. */
	@TableField(testColumn = "DF", testOption = TestOption.START, nachKomma = 0)
	double kedVerrechnung = DOUBLE_INIT;
	double[] kedVerrechnungArr;
	/** Kapitalertrag mit Verrechnung. */
	@TableField(testColumn = "DG", testOption = TestOption.START, nachKomma = 0)
	double keVerrechnung = DOUBLE_INIT;
	/** Buchwert, gesamt, nach der aktuellen Verrechnung. */
	@TableField(testColumn = "DH", testOption = TestOption.START, nachKomma = 0)
	double bwVerechnungJe = DOUBLE_INIT;
	/** FI Buchwert gesamt nach der aktuellen Verrechnung. */
	@TableField(testColumn = "DI", testOption = TestOption.START, nachKomma = 0)
	double bwFiVerechnungJe = DOUBLE_INIT;
	/** Cash Flow vor Kreditaufnahme. */
	@TableField(testColumn = "DJ", testOption = TestOption.START, nachKomma = 0)
	double cfVorKredit = DOUBLE_INIT;
	/** Zins für Kredit. */
	@TableField(testColumn = "DK", nachKomma = 2, percent = true)
	double kuponEsgII = DOUBLE_INIT;
	/** Kredit zur Liquiditätssicherstellung. */
	@TableField(testColumn = "DL", testOption = TestOption.START, nachKomma = 0)
	double kredit = DOUBLE_INIT;
	/** Kapitalaufwand für Kredit. */
	@TableField(testColumn = "DM", testOption = TestOption.START, nachKomma = 0)
	double ak = DOUBLE_INIT;
	/** Rückzahlung Kredit. */
	@TableField(testColumn = "DN", testOption = TestOption.START, nachKomma = 0)
	double cfKredit = DOUBLE_INIT;
	/** Cash Flow zur Neuanlage. */
	@TableField(testColumn = "DO", testOption = TestOption.START, nachKomma = 0)
	double cfNeuAnlage = DOUBLE_INIT;
	/** Aufwendungen für KA. */
	@TableField(testColumn = "DP", testOption = TestOption.START, nachKomma = 0)
	double aufwendungenKa = DOUBLE_INIT;
	/** Nettoverzinsung ohne Zinsratenzuschläge. */
	@TableField(testColumn = "DQ", testOption = TestOption.START, nachKomma = 2, percent = true)
	double nvz = DOUBLE_INIT;
	/** Zins einer 10 -jährigen Nullkupon-Anleihe - für ZZR Berechnung. DL, L 1. */
	@TableField(testColumn = "DR", nachKomma = 2, percent = true)
	double zzrSpotEsg = DOUBLE_INIT;
	/** Referenzzinssatz. */
	@TableField(testColumn = "DS", nachKomma = 2, percent = true)
	double referenzZinssatz = DOUBLE_INIT;
	/** Referenzzinssatz 2M. */
	@TableField(testColumn = "DT", nachKomma = 2, percent = true)
	double refZins2M = DOUBLE_INIT;
	/** Mittlere Rechnungszins. */
	@TableField(testColumn = "DU", testOption = TestOption.START, nachKomma = 2, percent = true)
	double rzMittel = DOUBLE_INIT;
	/** delta-ZZR UEB, Altbestand. */
	@TableField(testColumn = "DV", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrUebAlt = DOUBLE_INIT;
	/** delta-ZZR UEB, Neubestand.. */
	@TableField(testColumn = "DW", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrUebNeu = DOUBLE_INIT;
	/** Delta ZZR - NÜB. */
	@TableField(testColumn = "DX", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrNueb = DOUBLE_INIT;
	/** rmZ UEB Altbestand. */
	@TableField(testColumn = "DY", testOption = TestOption.START, nachKomma = 0)
	double rmzUebAlt = DOUBLE_INIT;
	/** rmZ UEB Neubestand. */
	@TableField(testColumn = "DZ", testOption = TestOption.START, nachKomma = 0)
	double rmzUebNeu = DOUBLE_INIT;
	/** rmZ NÜB. */
	
	// =================================================================================
	// Spalten E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E E
	
	@TableField(testColumn = "EA", testOption = TestOption.START, nachKomma = 0)
	double rmzNueb = DOUBLE_INIT;
	/** rmZ UEB Gesamt Altbestand. */
	@TableField(testColumn = "EB", testOption = TestOption.START, nachKomma = 0)
	double rmzUebGesamtAlt = DOUBLE_INIT;
	/** rmZ UEB Gesamt Neubestand. */
	@TableField(testColumn = "EC", testOption = TestOption.START, nachKomma = 0)
	double rmzUebGesamtNeu = DOUBLE_INIT;
	/** rmZ Gesamt NÜB. */
	@TableField(testColumn = "ED", testOption = TestOption.START, nachKomma = 0)
	double rmzNuebGesamt = DOUBLE_INIT;
	/** Übriges Ergebnis ÜB Altbestand stochastisch. */
	@TableField(testColumn = "EE", testOption = TestOption.START, nachKomma = 0)
	double ueEalt = DOUBLE_INIT;
	/** Übriges Ergebnis ÜB Neubestand stochastisch. */
	@TableField(testColumn = "EF", testOption = TestOption.START, nachKomma = 0)
	double ueEneu = DOUBLE_INIT;
	/** Übriges Ergebnis NÜB stochastisch. */
	@TableField(testColumn = "EG", testOption = TestOption.START, nachKomma = 0)
	double ueEnueb = DOUBLE_INIT;
	/** Übriges Ergebnis UEB, Bestand (ohne GCR) Altbestand stochastisch. */
	@TableField(testColumn = "EH", testOption = TestOption.START, nachKomma = 0)
	double ueEaltNoGcr = DOUBLE_INIT;
	/** Übriges Ergebnis UEB, Bestand (ohne GCR) Neubestand stochastisch. */
	@TableField(testColumn = "EI", testOption = TestOption.START, nachKomma = 0)
	double ueEneuNoGcr = DOUBLE_INIT;
	/** GCR übriges Ergebnis an Neugeschäft - ÜEB. */
	@TableField(testColumn = "EJ", testOption = TestOption.START, nachKomma = 0)
	double gcrUeB = DOUBLE_INIT;
	/** Risikoergebnis UEB, alt, stochastisch. */
	@TableField(testColumn = "EK", testOption = TestOption.START, nachKomma = 0)
	double reAlt = DOUBLE_INIT;
	/** Risikoergebnis UEB, neu, stochastisch. */
	@TableField(testColumn = "EL", testOption = TestOption.START, nachKomma = 0)
	double reNeu = DOUBLE_INIT;
	/** Risikoergebnis - NÜB, stochastisch. */
	@TableField(testColumn = "EM", testOption = TestOption.START, nachKomma = 0)
	double risikoUebStochAgg = DOUBLE_INIT;
	/** JÜ_Zielerhöhung. */
	@TableField(testColumn = "EN", testOption = TestOption.START, nachKomma = 0)
	double jUeZielerhoehung = DOUBLE_INIT;
	/** JÜZiel. */
	@TableField(testColumn = "EO", testOption = TestOption.START, nachKomma = 0)
	double jueZiel = DOUBLE_INIT;
	/** Verlustvortrag. */
	@TableField(testColumn = "EP", testOption = TestOption.START, nachKomma = 0)
	double vv = DOUBLE_INIT;
	/** Mindeskapitalertrag. */
	@TableField(testColumn = "EQ", testOption = TestOption.START, nachKomma = 0)
	double mindestkapitalertragLvrgAltNeu = DOUBLE_INIT;
	/** Rohüberschuss. */
	@TableField(testColumn = "ER", testOption = TestOption.START, nachKomma = 0)
	double rohueb = DOUBLE_INIT;
	double[] rohuebArr;
	/** Mindestzuführung. */
	@TableField(testColumn = "ES", testOption = TestOption.START, nachKomma = 0)
	double mindZf = DOUBLE_INIT;
	/** Deckungsrückstellung vor Deklaration. */
	@TableField(testColumn = "ET", testOption = TestOption.START, nachKomma = 0)
	double drVorDeklAgg = DOUBLE_INIT;
	/** anrechenbare Kapitalerträge. */
	@TableField(testColumn = "EU", testOption = TestOption.START, nachKomma = 0)
	double kapitalertragAnrechenbar = DOUBLE_INIT;
	/** Mindestzuführung Kürzungskonto. */
	@TableField(testColumn = "EV", testOption = TestOption.START, nachKomma = 0)
	double mindZfKk = DOUBLE_INIT;
	/** Mindestzuführung gesamt. */
	@TableField(testColumn = "EW", testOption = TestOption.START, nachKomma = 0)
	double mindZfGes = DOUBLE_INIT;
	/** JÜ. */
	@TableField(testColumn = "EX", testOption = TestOption.START, nachKomma = 0)
	double jue = DOUBLE_INIT;
	/** RfB_Zuf. */
	@TableField(testColumn = "EY", testOption = TestOption.START, nachKomma = 0)
	double rfBZuf = DOUBLE_INIT;
	private double[] rfBZufArr;
	/** nfRfB_56b-Entnahmen. */
	@TableField(testColumn = "EZ", testOption = TestOption.START, nachKomma = 0)
	double nfRfB56b = DOUBLE_INIT;
	private double[] nfRfB56bArr;
	/** ZAG, festgelegt. */
	
	// =================================================================================
	// Spalten F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F F
	
	@TableField(testColumn = "FA", testOption = TestOption.START, nachKomma = 0)
	double zag = DOUBLE_INIT;
	/** ZAG, fällig. */
	@TableField(testColumn = "FB", testOption = TestOption.START, nachKomma = 0)
	double zagFaellig = DOUBLE_INIT;
	/** ZAG, Endzahlung. */
	@TableField(testColumn = "FC", testOption = TestOption.START, nachKomma = 0)
	double zagEndzahlung = DOUBLE_INIT;
	/** Steuer, festgelegt. */
	@TableField(testColumn = "FD", testOption = TestOption.START, nachKomma = 0)
	double ertragssteuer = DOUBLE_INIT;
	/** Steuer, festgelegt, nach der LS-Korrektur. */
	@TableField(testColumn = "FE", testOption = TestOption.START)
	double ertragsSteuerLs = DOUBLE_INIT;
	/** Mittlere Zuführung zur RfB (gemittelt über die letzten M Jahre). */
	@TableField(testColumn = "FF", testOption = TestOption.START, nachKomma = 2, percent = true)
	double mittlRfBZufuehrung = DOUBLE_INIT;
	/** fRfB_min. */
	@TableField(testColumn = "FG", testOption = TestOption.START, nachKomma = 0)
	double fRfBMin = DOUBLE_INIT;
	/** fRfB_max. */
	@TableField(testColumn = "FH", testOption = TestOption.START, nachKomma = 0)
	double fRfBMax = DOUBLE_INIT;
	/** Zieldeklaration. */
	@TableField(testColumn = "FI", testOption = TestOption.START, nachKomma = 0)
	double zielDeklaration = DOUBLE_INIT;
	/** SÜAF 56b Entnahme */
	@TableField(testColumn = "FJ", testOption = TestOption.START, nachKomma = 0)
	double sUeAf56bEntnahme = DOUBLE_INIT;
	private double sUeAf56bEntnahmeArr[];
	/** fRfB 56b Entnahme. */
	@TableField(testColumn = "FK", testOption = TestOption.START, nachKomma = 0)
	double fRfB56bEntnahme = DOUBLE_INIT;
	private double[] fRfB56bEntnahmeArr;
	/** fRfB Überlauf. */
	@TableField(testColumn = "FL", testOption = TestOption.START, nachKomma = 0)
	double fRfBUeberlauf = DOUBLE_INIT;
	/** fRfB vor Endzahlung. */
	@TableField(testColumn = "FM", testOption = TestOption.START, nachKomma = 0)
	double fRfBVorEndzahlung = DOUBLE_INIT;
	/** Deklaration. */
	@TableField(testColumn = "FN", testOption = TestOption.START, nachKomma = 0)
	double dekl = DOUBLE_INIT;
	/** Deklaration. */
	@TableField(testColumn = "FO", testOption = TestOption.START, nachKomma = 0)
	double deklZins = DOUBLE_INIT;
	/** Deklaration, rest */
	@TableField(testColumn = "FP", testOption = TestOption.START, nachKomma = 0)
	double deklRest = DOUBLE_INIT;
	/** Gesamtzins. */
	@TableField(testColumn = "FQ", testOption = TestOption.START, nachKomma = 2, percent = true)
	double vzGes = DOUBLE_INIT;
	/** Deckungsrückstellung vor Deklaration - ÜB. */
	@TableField(testColumn = "FR", testOption = TestOption.START, nachKomma = 0)
	double drVorDeklUebAgg = DOUBLE_INIT;
	/** Deckungsrückstellung ÜB, Lockin, Altbestand */
	@TableField(testColumn = "FS", testOption = TestOption.START, nachKomma = 0)
	double drLockInAlt = DOUBLE_INIT;
	/** Deckungsrückstellung ÜB, Lockin, Neubestand */
	@TableField(testColumn = "FT", testOption = TestOption.START, nachKomma = 0)
	double drLockInNeu = DOUBLE_INIT;
	/** Deckungsrückstellung Lock-In - ÜB. */
	@TableField(testColumn = "FU", testOption = TestOption.START, nachKomma = 0)
	double drLockInAggWennLoB = DOUBLE_INIT;
	private double[] drLockInAggWennLoBArr;
	/** Deckungsrückstellung, Gesamtbestand Lock-In. */
	@TableField(testColumn = "FV", testOption = TestOption.START, nachKomma = 0)
	double drLockInAgg = DOUBLE_INIT;
	/** Deckungsrückstellung Gesamt - NÜB. FQ, */
	@TableField(testColumn = "FW", testOption = TestOption.START, nachKomma = 0)
	double drVorDeklNuebAgg = DOUBLE_INIT;
	/** Deckungsrückstellung gesamt. */
	@TableField(testColumn = "FX", testOption = TestOption.START, nachKomma = 0)
	double drGesAgg = DOUBLE_INIT;
	/** LBW gar. */
	@TableField(testColumn = "FY", testOption = TestOption.START, nachKomma = 0)
	double lbwGarAgg = DOUBLE_INIT;
	/** SÜAF_Zuf durch RfB Überlauf. */
	@TableField(testColumn = "FZ", testOption = TestOption.START, nachKomma = 0)
	double sueAFZufFRfBUeberlaufAgg = DOUBLE_INIT;
	
	// =================================================================================
	// Spalten G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G G
	
	/** SÜAF_Zuf. */
	@TableField(testColumn = "GA", testOption = TestOption.START, nachKomma = 0)
	double sueAFZufAgg = DOUBLE_INIT;
	/** SÜAF Entnahme. */
	@TableField(testColumn = "GB", testOption = TestOption.START, nachKomma = 0)
	double sueAFEntnahmeAgg = DOUBLE_INIT;
	/** Barauszahlung */
	@TableField(testColumn = "GC", testOption = TestOption.START, nachKomma = 0)
	double barAgg = DOUBLE_INIT;
	/** Lock-In (Garantierte Leistung. */
	@TableField(testColumn = "GD", testOption = TestOption.START, nachKomma = 0)
	double lockInAgg = DOUBLE_INIT;
	/** sämtliche garantierte Leistungen. FY. */
	@TableField(testColumn = "GE", nachKomma = 0)
	double lGarAgg = DOUBLE_INIT;
	/** Garantierten Leistungen inkl. Lock-In. */
	@TableField(testColumn = "GF", testOption = TestOption.START, nachKomma = 0)
	double lGarStochAgg = DOUBLE_INIT;
	/** Leistungen Gesamt. */
	@TableField(testColumn = "GG", testOption = TestOption.START, nachKomma = 0)
	double lGesAgg = DOUBLE_INIT;
	/** Beiträge, stochastisch. */
	@TableField(testColumn = "GH", testOption = TestOption.START, nachKomma = 0)
	double bStochAgg = DOUBLE_INIT;
	/** Kosten, stochastisch. */
	@TableField(testColumn = "GI", testOption = TestOption.START, nachKomma = 0)
	double kStochAgg = DOUBLE_INIT;
	/** Cashflow EVU -> RVU, stochastisch. */
	@TableField(testColumn = "GJ", testOption = TestOption.START, nachKomma = 0)
	double cfRvstochAgg = DOUBLE_INIT;
	/** Summe über Zinsratenzuschlag, stochastisch. */
	@TableField(testColumn = "GK", testOption = TestOption.START, nachKomma = 0)
	double ziRaZuStochAgg = DOUBLE_INIT;
	/** Cashflow, Risikoergebnis + Kostenergebnis FLV Aufschub, aufgezinst. */
	@TableField(testColumn = "GL", nachKomma = 0)
	double cashflowGesamt = DOUBLE_INIT;
	/** Diskontfunktion. */
	@TableField(testColumn = "GM", nachKomma = 2, percent = true)
	double diskontEsg = DOUBLE_INIT;
	/** Mittlerer jährlicher Zins (für Aufzinsung). */
	@TableField(testColumn = "GN", nachKomma = 3, percent = true)
	double jaehrlZinsEsg = DOUBLE_INIT;
	/** Leistungen Gesamt, , aufgezinst. */
	@TableField(testColumn = "GO", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungGesamt = DOUBLE_INIT;
	/** Beiträge, aufgezinst. */
	@TableField(testColumn = "GP", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungBeitraege = DOUBLE_INIT;
	/** Kosten, aufgezinst. */
	@TableField(testColumn = "GQ", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungKosten = DOUBLE_INIT;
	/** Leistungen durch Endzahlung. */
	@TableField(testColumn = "GR", testOption = TestOption.START, nachKomma = 0)
	double endZahlungAgg = DOUBLE_INIT;
	/** ZAG, aufgezinst. */
	@TableField(testColumn = "GS", testOption = TestOption.START, nachKomma = 0)
	double zagAufgezinst = DOUBLE_INIT;
	/** Steuer aus dem Vorjahr ausgezahlt, inklusive latente HGB-Steuer, aufgezinst. */
	@TableField(testColumn = "GT", testOption = TestOption.START, nachKomma = 0)
	double steuerVjAufgezinst = DOUBLE_INIT;
	/** CF EVU -> RVU aufgezinst. */
	@TableField(testColumn = "GU", testOption = TestOption.START, nachKomma = 0)
	double aufzinsungcfEvuRvu = DOUBLE_INIT;
	/** Cashflow, Risikoergebnis + Kostenergebnis FLV Aufschub, aufgezinst. */
	@TableField(testColumn = "GV", testOption = TestOption.START, nachKomma = 0)
	double cashflowAufgezinst = DOUBLE_INIT;
	/** Cash-out-flows, gesamt, außer KA-Cashflows. */
	@TableField(testColumn = "GW", testOption = TestOption.START, nachKomma = 0)
	double cfOhneKa = DOUBLE_INIT;
	/** MW VT. Entspricht auch VT Klassik MW!B. */
	@TableField(testColumn = "GX", testOption = TestOption.START, nachKomma = 0)
	double mwVt = DOUBLE_INIT;
	/** Passive Reserven. */
	@TableField(testColumn = "GY", testOption = TestOption.START, nachKomma = 0)
	double bwrPas = DOUBLE_INIT;
	/** Überschussfonds: Deklaration (fRfB) */
	@TableField(testColumn = "GZ", testOption = TestOption.START, nachKomma = 0)
	double deklsurplusfRfB = DOUBLE_INIT;
	private double[] deklsurplusfRfBarr;
	
	// =================================================================================
	// Spalten H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H H

	/** Überschussfonds: SÜA-Zuführungen */
	@TableField(testColumn = "HA", testOption = TestOption.START, nachKomma = 0)
	double sUeAfZufSf = DOUBLE_INIT;
	private double[] sUeAfZufSfArr;
	/** Überschussfonds: SÜA-Entnahmen */
	@TableField(testColumn = "HB", testOption = TestOption.START, nachKomma = 0)
	double sUeAfEntSf = DOUBLE_INIT;
	private double[] sUeAfEntSfArr;
	/** Überschussfonds: Barauszahlung */
	@TableField(testColumn = "HC", testOption = TestOption.START, nachKomma = 0)
	double barSf = DOUBLE_INIT;
	/** Überschussfonds: Lock-in */
	@TableField(testColumn = "HD", testOption = TestOption.START, nachKomma = 0)
	double lockInSf = DOUBLE_INIT;
	/** LE durch SF, aggr. GG, Rekursiv3. */
	/** Überschussfonds: Leistungserhöhung, aggr */
	@TableField(testColumn = "HE", testOption = TestOption.START, nachKomma = 5, percent = true)
	double leAggrSf = DOUBLE_INIT;
	/** Überschussfonds: Cashflow gesamt */
	@TableField(testColumn = "HF", testOption = TestOption.START, nachKomma = 0)
	double cashflowSf = DOUBLE_INIT;
	/** Überschussfonds: Delta Leistung, gar vs. Leistung, gesamt. */
	@TableField(testColumn = "HG", testOption = TestOption.START, nachKomma = 0)
	double deltaLAgg = DOUBLE_INIT;
	/** EPIFP: Deckungsrückstellung aus den künftigen Prämien. */
	@TableField(testColumn = "HH", nachKomma = 0)
	double drstKPAgg = DOUBLE_INIT;
	/** EPIFP: Rohüberschuss aus den künftigen Prämien, nur positive Beiträge. */
	@TableField(testColumn = "HI", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpK = DOUBLE_INIT;
	/** EPIFP: Rohüberschuss aus den künftigen Prämien, nur negative Beiträge. */
	@TableField(testColumn = "HJ", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpN = DOUBLE_INIT;
	/** EPIFP: Rohüberschuss , nur positive Beiträge. */
	@TableField(testColumn = "HK", testOption = TestOption.START, nachKomma = 0)
	double rohuebKpP = DOUBLE_INIT;
	/** EPIFP: Jahresüberschuss geschlüsselt auf künftige Prämien. */
	@TableField(testColumn = "HL", testOption = TestOption.START, nachKomma = 0)
	double jueVnKp = DOUBLE_INIT;
	/** ZÜB: Cashflow gesamt, Überschussbeteiligung, ohne Endzahlung. */
	@TableField(testColumn = "HM", testOption = TestOption.START, nachKomma = 0)
	double zuebCashflowAgg = DOUBLE_INIT;
	/** ZÜB: Cashflow gesamt, Überschussbeteiligung, ohne Endzahlung. */
	@TableField(testColumn = "HN", testOption = TestOption.START, nachKomma = 0)
	double optionenCashflowAgg = DOUBLE_INIT;
	/** Summe der positiven Beiträge zum Rohüberschuss, nur RE und üE. */
	@TableField(testColumn = "HO", testOption = TestOption.START, nachKomma = 0)
	double beitragRohUebAgg = DOUBLE_INIT;
	/** Anteil LoBs, deren KA-Kosten gestresst sind. */
	@TableField(testColumn = "HP", testOption = TestOption.START, nachKomma = 0)
	double anteilLobsKaStress = DOUBLE_INIT;
	/** Zins einer 10 -jährigen Nullkupon-Anleihe - für Kundenverhalten. */
	@TableField(testColumn = "HQ", nachKomma = 2, percent = true)
	double spotVnVerhaltenEsg = DOUBLE_INIT;

	// MIL_W.Schalesi
	/** MMR-Trigger FI Kupon der Neuanlage in t. L 1. */
	@TableField(testColumn = "HR", nachKomma = 0)
	double mmrCouponTrigger = 0;
	double[] mmrCouponTriggerArr;

	// MIL_W.Schalesi
	/** MMR-Trigger FI Kupon der Neuanlage in t Boolean. L 1. */
	@TableField(testColumn = "HS", nachKomma = 0)
	boolean mmrCouponTriggerBoolean = false;

	// MIL_W.Schalesi
	/** MMR-Trigger JÜ. Rekursiv1. */
	@TableField(testColumn = "HT", nachKomma = 0)
	double mmrJueTrigger = 0;
	double[] mmrJueTriggerArr;

	// MIL_W.Schalesi
	/** MMR-Trigger JÜ Boolean. Rekursiv1. */
	@TableField(testColumn = "HU", nachKomma = 0)
	boolean mmrJueTriggerBoolean = false;
	
	// OW_M.Bartnicki
	/** Zielbeteiligung am Rohüberschuss in % vom Rohüberschuss p_Rohüb. L2*/
	@TableField(testColumn = "HV", nachKomma = 2)
	double zielBeteiligungssatzRohueb_OW = DOUBLE_INIT;
	/** Flex. Überschussbet. Trigger Marktwertveränderung - Auswahl Marktschock L2*/
	@TableField(testColumn = "HW", nachKomma = 0)
	int zusatzoption_UEB_SzenarioID;
	/** Flex. Überschussbet. Trigger Marktwertveränderung - Ziel Projektionsjahr . L2*/
	@TableField(testColumn = "HX", nachKomma = 0)
	int zusatzoption_UEB_ZielPJ;
		
	// MIL_W.Schalesi
	/** Mindestanteil FI stetig. Rekursiv2. */
	@TableField(testColumn = "HY", nachKomma = 0, percent = true)
	double aFiMinDaaStetig = DOUBLE_INIT;


	// OW_LS
	/** FI Buchwert Neuanlage im Zeitpunkt t - 1 */
	@TableField(testColumn = "HZ", testOption = TestOption.START, nachKomma = 0)
	double bwFiNeuAn_1 = DOUBLE_INIT;
	/** FI Restlaufzeit der Neuanlage - 1 */
	@TableField(testColumn = "IA")
	int rlz_1 = Integer.MAX_VALUE;
	/** Zeitpunkt der Fälligkeit der Neuanlage - 1 */
	@TableField(testColumn = "IB")
	int zpFaelligkeit_1 = Integer.MAX_VALUE;
	/** FI Kupon der Neuanlage in t - 1 */
	@TableField(testColumn = "IC", nachKomma = 3, percent = true)
	double kuponEsg_1 = DOUBLE_INIT;
	/** FI Cash Flow aus dem Neubestand - 1  */
	@TableField(testColumn = "ID", testOption = TestOption.START, nachKomma = 0)
	double cfFiNeuAnl_1 = DOUBLE_INIT;
	/** FI Kapitalertrag des Neubestands - 1 */
	@TableField(testColumn = "IE", testOption = TestOption.START, nachKomma = 0)
	double keFiNeuAnl_1 = DOUBLE_INIT;
	/** FI Buchwert Neuanlage im Zeitpunkt t - 2 */
	@TableField(testColumn = "IF", testOption = TestOption.START, nachKomma = 0)
	double bwFiNeuAn_2 = DOUBLE_INIT;
	/** FI Restlaufzeit der Neuanlage - 2 */
	@TableField(testColumn = "IG")
	int rlz_2 = Integer.MAX_VALUE;
	/** Zeitpunkt der Fälligkeit der Neuanlage - 2 */
	@TableField(testColumn = "IH")
	int zpFaelligkeit_2 = Integer.MAX_VALUE;
	/** FI Kupon der Neuanlage in t - 2 */
	@TableField(testColumn = "II", nachKomma = 3, percent = true)
	double kuponEsg_2 = DOUBLE_INIT;
	/** FI Cash Flow aus dem Neubestand - 2  */
	@TableField(testColumn = "IJ", testOption = TestOption.START, nachKomma = 0)
	double cfFiNeuAnl_2 = DOUBLE_INIT;
	/** FI Kapitalertrag des Neubestands - 2 */
	@TableField(testColumn = "IK", testOption = TestOption.START, nachKomma = 0)
	double keFiNeuAnl_2 = DOUBLE_INIT;
	/** FI Buchwert Neuanlage im Zeitpunkt t - 3 */
	@TableField(testColumn = "IL", testOption = TestOption.START, nachKomma = 0)
	double bwFiNeuAn_3 = DOUBLE_INIT;
	/** FI Restlaufzeit der Neuanlage - 3 */
	@TableField(testColumn = "IM")
	int rlz_3 = Integer.MAX_VALUE;
	/** Zeitpunkt der Fälligkeit der Neuanlage -3 */
	@TableField(testColumn = "IN")
	int zpFaelligkeit_3 = Integer.MAX_VALUE;
	/** FI Kupon der Neuanlage in t - 3 */
	@TableField(testColumn = "IO", nachKomma = 3, percent = true)
	double kuponEsg_3 = DOUBLE_INIT;
	/** FI Cash Flow aus dem Neubestand - 3  */
	@TableField(testColumn = "IP", testOption = TestOption.START, nachKomma = 0)
	double cfFiNeuAnl_3 = DOUBLE_INIT;
	/** FI Kapitalertrag des Neubestands - 3 */
	@TableField(testColumn = "IQ", testOption = TestOption.START, nachKomma = 0)
	double keFiNeuAnl_3 = DOUBLE_INIT;
	/** FI Buchwert Neuanlage im Zeitpunkt t - 4 */
	@TableField(testColumn = "IR", testOption = TestOption.START, nachKomma = 0)
	double bwFiNeuAn_4 = DOUBLE_INIT;
	/** FI Restlaufzeit der Neuanlage - 4 */
	@TableField(testColumn = "IS")
	int rlz_4 = Integer.MAX_VALUE;
	/** Zeitpunkt der Fälligkeit der Neuanlage -4 */
	@TableField(testColumn = "IT")
	int zpFaelligkeit_4 = Integer.MAX_VALUE;
	/** FI Kupon der Neuanlage in t - 4 */
	@TableField(testColumn = "IU", nachKomma = 3, percent = true)
	double kuponEsg_4 = DOUBLE_INIT;
	/** FI Cash Flow aus dem Neubestand - 4  */
	@TableField(testColumn = "IV", testOption = TestOption.START, nachKomma = 0)
	double cfFiNeuAnl_4 = DOUBLE_INIT;
	/** FI Kapitalertrag des Neubestands - 4 */
	@TableField(testColumn = "IW", testOption = TestOption.START, nachKomma = 0)
	double keFiNeuAnl_4 = DOUBLE_INIT;
	
	//OW_D.Hohmann
    /** ZZR UEB, alt_sl. */
	@TableField(testColumn = "IX", testOption = TestOption.START, nachKomma = 0)
	double zzrAlt_sl;
	/** ZZR UEB, neu_sl. */
	@TableField(testColumn = "IY", testOption = TestOption.START, nachKomma = 0)
	double zzrNeu_sl;
   /** delta-ZZR UEB, Altbestand_sl. */
	@TableField(testColumn = "IZ", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrUebAlt_sl = DOUBLE_INIT;
	/** delta-ZZR UEB, Neubestand_sl. */
	@TableField(testColumn = "JA", testOption = TestOption.START, nachKomma = 0)
	double deltaZzrUebNeu_sl = DOUBLE_INIT;
	/** rmZ UEB Altbestand_sl. */
	@TableField(testColumn = "JB", testOption = TestOption.START, nachKomma = 0)
	double rmzUebAlt_sl = DOUBLE_INIT;
	/** rmZ UEB Neubestand_sl. */
	@TableField(testColumn = "JC", testOption = TestOption.START, nachKomma = 0)
	double rmzUebNeu_sl = DOUBLE_INIT;
	/** rmZ UEB Gesamt Altbestand_sl. */
	@TableField(testColumn = "JD", testOption = TestOption.START, nachKomma = 0)
	double rmzUebGesamtAlt_sl = DOUBLE_INIT;
	/** rmZ UEB Gesamt Neubestand_sl. */
	@TableField(testColumn = "JE", testOption = TestOption.START, nachKomma = 0)
	double rmzUebGesamtNeu_sl = DOUBLE_INIT;
	/** SÜAF, alt_sl. */
	@TableField(testColumn = "JF", testOption = TestOption.START, nachKomma = 0)
	double sueAfAlt_sl = DOUBLE_INIT;
	/** SÜAF, neu_sl. */
	@TableField(testColumn = "JG", testOption = TestOption.START, nachKomma = 0)
	double sueAfNeu_sl = DOUBLE_INIT;
	/** Übriges Ergebnis ÜB Altbestand_sl stochastisch. */
	@TableField(testColumn = "JH", testOption = TestOption.START, nachKomma = 0)
	double ueEalt_sl = DOUBLE_INIT;
	/** Übriges Ergebnis ÜB Neubestand_sl stochastisch. */
	@TableField(testColumn = "JI", testOption = TestOption.START, nachKomma = 0)
	double ueEneu_sl = DOUBLE_INIT;
	/** Übriges Ergebnis UEB, Bestand (ohne GCR) Altbestand_sl stochastisch. */
	@TableField(testColumn = "JJ", testOption = TestOption.START, nachKomma = 0)
	double ueEaltNoGcr_sl = DOUBLE_INIT;
	/** Übriges Ergebnis UEB, Bestand (ohne GCR) Neubestand_sl stochastisch. */
	@TableField(testColumn = "JK", testOption = TestOption.START, nachKomma = 0)
	double ueEneuNoGcr_sl = DOUBLE_INIT;
	/** Risikoergebnis UEB, alt_sl, stochastisch. */
	@TableField(testColumn = "JL", testOption = TestOption.START, nachKomma = 0)
	double reAlt_sl = DOUBLE_INIT;
	/** Risikoergebnis UEB, neu_sl, stochastisch. */
	@TableField(testColumn = "JM", testOption = TestOption.START, nachKomma = 0)
	double reNeu_sl = DOUBLE_INIT;
	/** Deckungsrückstellung ÜB, Lockin, Altbestand_sl */
	@TableField(testColumn = "JN", testOption = TestOption.START, nachKomma = 0)
	double drLockInAlt_sl = DOUBLE_INIT;
	/** Deckungsrückstellung ÜB, Lockin, Neubestand_sl */
	@TableField(testColumn = "JO", testOption = TestOption.START, nachKomma = 0)
	double drLockInNeu_sl = DOUBLE_INIT;

	
	private double[] bwFiNeuAnArr_1;
	private double[] bwFiNeuAnArr_2;
	private double[] bwFiNeuAnArr_3;
	private double[] bwFiNeuAnArr_4;
	private int[] zpFaelligkeitArr_1;
	private int[] zpFaelligkeitArr_2;
	private int[] zpFaelligkeitArr_3;
	private int[] zpFaelligkeitArr_4;
	private double[] kuponEsgArr_1;
	private double[] kuponEsgArr_2;
	private double[] kuponEsgArr_3;
	private double[] kuponEsgArr_4;
	
	/**
	 * Konstruktion einer neuen Agg-Zeile.
	 * 
	 * @param berechnung
	 *            zugrunde liegende Berechnung
	 * @param rv
	 *            soll RV gerechnet werden?
	 * @param rzgZeilen
	 *            zugehörige rzg
	 * @param flvZeilen
	 *            zu dieser Zeit gehörende Flv-Zeilen
	 * @param vg
	 *            die chronologische Vorgängerzeile
	 */
	public AggZeile(final Berechnung berechnung, final List<RzgZeile> rzgZeilen, final List<FlvZeile> flvZeilen,
			final AggZeile vg) {
		this.berechnung = berechnung;
		this.rzgZeilen = rzgZeilen;
		this.flvZeilen = flvZeilen;
		this.vg = vg;

		// Zeit muss konstant sein für alle Aggregationen:
		szenario = berechnung.szenarioName;
		szenarioId = berechnung.szenarioId;
		this.zeit = rzgZeilen.get(0).zeit;

		// Summation der rohen Aggregationen aus den rzg-LoB's
		double kAgg = 0.0;
		double bAgg = 0.0;
		double lTodAgg = 0.0;
		double kaAgg = 0.0;
		double lSonstErlAgg = 0.0;
		double rkAgg = 0.0;
		double rUeAgg = 0.0;
		double kUeAgg = 0.0;
		double cfEvuRvu = 0.0;
		double zinsratenzuschlaegeAgg = 0.0;
		double rmzEingabeAgg = 0.0;
		double hgbDrAgg = 0.0;
		double uebertragFestgelegteInFreieRfBAgg = 0.0d;

		for (RzgZeile rzgZeile : rzgZeilen) {
			kAgg += rzgZeile.kosten;
			bAgg += rzgZeile.praemien;
			lTodAgg += rzgZeile.lTod;
			kaAgg += rzgZeile.lKa;
			lSonstErlAgg += rzgZeile.sonstigeErlebensfallLeistungen;
			rkAgg += rzgZeile.lRkw;
			rUeAgg += rzgZeile.risikoErgebnis;
			kUeAgg += rzgZeile.uebrigesErgebnis;
			cfEvuRvu += rzgZeile.cfEvuRvu;
			zinsratenzuschlaegeAgg += rzgZeile.zinsratenZuschlag;
			rmzEingabeAgg += rzgZeile.zinsaufwand;
			hgbDrAgg += rzgZeile.drDet;
			uebertragFestgelegteInFreieRfBAgg +=rzgZeile.uebertragFestgelegteInFreieRfB;
		}

		this.kAgg = kAgg;
		this.bAgg = bAgg;
		this.lTodAgg = lTodAgg;
		this.kaAgg = kaAgg;
		this.lSonstErlAgg = lSonstErlAgg;
		this.rkAgg = rkAgg;
		this.rueAgg = rUeAgg;
		this.kueAgg = kUeAgg;
		this.cfEvuRvu = cfEvuRvu;
		this.zinsratenzuschlaegeAgg = zinsratenzuschlaegeAgg;
		this.rmzEingabeAgg = rmzEingabeAgg;
		this.hgbDrAgg = hgbDrAgg;
		this.uebertragFestgelegteInFreieRfBAgg=uebertragFestgelegteInFreieRfBAgg;

		if (zeit == 0) {
			fRfBFrei = berechnung.hgbBilanzdaten.getFreieRfbBuchwert();
			eigenkapitalFortschreibung = berechnung.hgbBilanzdaten.getEkBuchwert();
			grNrd = berechnung.hgbBilanzdaten.getGrNrdBuchwert();
			lsHgb = berechnung.hgbBilanzdaten.getLatSteuerAktiva() - berechnung.hgbBilanzdaten.getLatSteuerPassiva();
		}

		if (zeit > 0) {
			this.zinsen = berechnung.genussNachrang.get(zeit).getZinsen();
			this.rueckZahlung = berechnung.genussNachrang.get(zeit).getRueckzahlung();
		}

		// für die Berechnung der Ausfallwahrscheinlichkeit wird FI Ausfall!$U$2
		// benötigt, und dafür werden die beiden folgenden Werte für Zeit = 1
		// schon vorab berechnet.
		if (zeit == 1) {
			// Temporäre vorab-Berechnung für die Bestimmung der Ausfallwahrscheinlichkeit
			final double aq5 = KaModellierung.bwFiNeuAn(vg.bwVerechnungJe, vg.bwFiVerechnungJe, vg.cfNeuAnlage,
					vg.aFiZielDaa, rlz, zeit);
			double aw5 = KaModellierung.bwReNeuAnl(vg.aReZielDaa, vg.bwVerechnungJe, vg.bwReNachRls, vg.cfNeuAnlage,
					bwFiNeuAn, zeit, berechnung.laengeProjektionDr);
			bwEq = KaModellierung.bwEq(0.0, 0.0, aq5, aw5, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0,
					berechnung.bwSaSp, berechnung.mwSaSp);
			bwRe = KaModellierung.bwRe(0.0, aw5, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0,
					berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp, berechnung.mwSaSp);

			// Temporäre vorab-Berechnung für die Bestimmung der Ausfallwahrscheinlichkeit
			mwEq = KaModellierung.mwEq(vg.mwEqRlsII, vg.cfNeuAnlage, bwFiNeuAn, bwReNeuAnl, zeit,
					berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp, berechnung.mwSaSp);
			mwRe = KaModellierung.mwRe(vg.mwRenachRls, bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0,
					berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp, berechnung.mwSaSp);
			mwFianfangJ = KaModellierung.mwFiAnfangJ(vg.fiMw, bwFiNeuAn, zeit, berechnung.mwFi0, berechnung.mwEq0,
					berechnung.mwRe0, mwEq, mwRe, berechnung.mwSaSp);
		}
	}

	/**
	 * Setze den chronologischen Nachfolger dieser Zeile.
	 * 
	 * @param nf
	 *            der Nachfolger
	 */
	public void setNachfolger(final AggZeile nf) {
		this.nf = nf;
	}

	/**
	 * Durchführung der Berechnung auf der ersten Ebene.
	 * 
	 * @param pfad
	 *            Nummer des Pfades, der gerechnet werden soll
	 */
	public void berechnungLevel01(final int pfad) {
		kuponEsgArr = fillArr(agg -> agg.kuponEsg, kuponEsgArr);
		zpFaelligkeitArr = fillArrInt(agg -> agg.zpFaelligkeit, zpFaelligkeitArr);

		// OW_LS
		kuponEsgArr_1 = fillArr(agg -> agg.kuponEsg_1, kuponEsgArr_1);
		kuponEsgArr_2 = fillArr(agg -> agg.kuponEsg_2, kuponEsgArr_2);
		kuponEsgArr_3 = fillArr(agg -> agg.kuponEsg_3, kuponEsgArr_3);
		kuponEsgArr_4 = fillArr(agg -> agg.kuponEsg_4, kuponEsgArr_4);
		zpFaelligkeitArr_1 = fillArrInt(agg -> agg.zpFaelligkeit_1, zpFaelligkeitArr_1);
		zpFaelligkeitArr_2 = fillArrInt(agg -> agg.zpFaelligkeit_2, zpFaelligkeitArr_2);
		zpFaelligkeitArr_3 = fillArrInt(agg -> agg.zpFaelligkeit_3, zpFaelligkeitArr_3);
		zpFaelligkeitArr_4 = fillArrInt(agg -> agg.zpFaelligkeit_4, zpFaelligkeitArr_4);
		
		// MIL_W.Schalesi
		mmrCouponTriggerArr = fillArr(agg -> agg.mmrCouponTrigger, mmrCouponTriggerArr);
		if (zeit > 0) {
			//if (Functions.sum(mmrCouponTriggerArr) >= 0)
			// OW_LS 12.09.18: Es werden maximal die letzten 10 Jahre betrachtet
			if (Functions.sum10(mmrCouponTriggerArr) >= 0)
				mmrCouponTriggerBoolean = false;
			else
				mmrCouponTriggerBoolean = true;
			mmrCouponTriggerArr[zeit] = mmrCouponTrigger;
		}

		spotVnVerhaltenEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).getSpotRlz(10);
		//sdl: Wozu wird hier diese Variable verwendet?
		aReZielDaa = berechnung.getZeitunabhManReg().getZielAnteilARe();
		if (zeit != 0) {
			cfFiAkt = berechnung.getFiAusfall(zeit).cfFimitAusfallJahresende;
			keFiAkt = berechnung.getFiAusfall(zeit).keaktBestandJeR;
			rlz = KaModellierung.rlz(berechnung.getZeitabhManReg().get(zeit).getRlzNeuAnl(),
					berechnung.laengeProjektionDr, zeit);
			zpFaelligkeit = KaModellierung.zpFaelligkeit(zeit, rlz);
			
			//*** OW_LS Beginn Flexible FI Neuanlage***
			/*if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isZeitabhFiNaRlz()) {
				rlz_liqui = KaModellierung.rlz(berechnung.getZeitabhManReg().get(zeit).getFiNaRlz(), berechnung.laengeProjektionDr, zeit);
				if (berechnung.getDynManReg().isFiNAmehrereRLZ()) {
					rlz_sonstiges = 0;
					zpFaelligkeit_sonstiges = 0;
				} else {
					if (mmrCouponTriggerBoolean) {
						rlz_sonstiges = KaModellierung.rlz(berechnung.getZeitunabhManReg().getFiNeuanlageRestlaufzeitAlternative(), berechnung.laengeProjektionDr, zeit);
					} else {
						rlz_sonstiges = KaModellierung.rlz(berechnung.getZeitunabhManReg().getFiNeuanlageRestlaufzeit(), berechnung.laengeProjektionDr, zeit);
					}
					zpFaelligkeit_sonstiges = KaModellierung.zpFaelligkeit(zeit, rlz_sonstiges);
				}
				zpFaelligkeit_liqui = KaModellierung.zpFaelligkeit(zeit, rlz_liqui);
			} else {
				rlz_liqui = 0;
				rlz_sonstiges = 0;
				zpFaelligkeit_liqui = 0;
				zpFaelligkeit_sonstiges = 0;
			}*/
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isFiNAmehrereRLZ()) {
				if (mmrCouponTriggerBoolean) {
					rlz_1 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzAlternativ().get(1), berechnung.laengeProjektionDr, zeit);
					rlz_2 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzAlternativ().get(2), berechnung.laengeProjektionDr, zeit);
					rlz_3 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzAlternativ().get(3), berechnung.laengeProjektionDr, zeit);
					rlz_4 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzAlternativ().get(4), berechnung.laengeProjektionDr, zeit);
				} else {
					rlz_1 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzStandard().get(1),berechnung.laengeProjektionDr, zeit);
					rlz_2 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzStandard().get(2),berechnung.laengeProjektionDr, zeit);
					rlz_3 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzStandard().get(3),berechnung.laengeProjektionDr, zeit);
					rlz_4 = KaModellierung.rlz(berechnung.getDynManReg().getfiNaRlzStandard().get(4),berechnung.laengeProjektionDr, zeit);
				}
				zpFaelligkeit_1 = KaModellierung.zpFaelligkeit(zeit, rlz_1);
				zpFaelligkeit_2 = KaModellierung.zpFaelligkeit(zeit, rlz_2);
				zpFaelligkeit_3 = KaModellierung.zpFaelligkeit(zeit, rlz_3);
				zpFaelligkeit_4 = KaModellierung.zpFaelligkeit(zeit, rlz_4);
			} else {
				rlz_1 = 0;
				rlz_2 = 0;
				rlz_3 = 0;
				rlz_4 = 0;
				zpFaelligkeit_1 = 0;
				zpFaelligkeit_2 = 0;
				zpFaelligkeit_3 = 0;
				zpFaelligkeit_4 = 0;
			}			
			//*** OW_LS Ende ***
			
		} else {
			rlz = 0;
			zpFaelligkeit = 0;
			
			//*** OW_LS Beginn Flexible FI Neuanlage***
			rlz_1 = 0;
			rlz_2 = 0;
			rlz_3 = 0;
			rlz_4 = 0;
			zpFaelligkeit_1 = 0;
			zpFaelligkeit_2 = 0;
			zpFaelligkeit_3 = 0;
			zpFaelligkeit_4 = 0;
			//*** OW_LS Ende ***
		}
		zpFaelligkeitArr[zeit] = zpFaelligkeit;

		//*** OW_LS Beginn Flexible FI Neuanlage***
		zpFaelligkeitArr_1[zeit] = zpFaelligkeit_1;
		zpFaelligkeitArr_2[zeit] = zpFaelligkeit_2;
		zpFaelligkeitArr_3[zeit] = zpFaelligkeit_3;
		zpFaelligkeitArr_4[zeit] = zpFaelligkeit_4;
		//*** OW_LS Ende ***
		
		if (zeit != berechnung.zeitHorizont) {
			rapVT = berechnung.getFiAusfall(zeit + 1).rapZinsen;
		} else {
			rapVT = 0.0;
		}

		if (zeit == 0) {
			kuponEsg = 0.0;
			
			//*** OW_LS Beginn Flexible FI Neuanlage***
			kuponEsg_1 = 0.0;
			kuponEsg_2 = 0.0;
			kuponEsg_3 = 0.0;
			kuponEsg_4 = 0.0;
			//*** OW_LS Ende ***
			
		} else {
			kuponEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit - 1).getKuponRlz(rlz);
			
			//*** OW_LS Beginn Flexible FI Neuanlage***
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isFiNAmehrereRLZ()) {
				kuponEsg_1 = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit - 1).getKuponRlz(rlz_1);
				kuponEsg_2 = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit - 1).getKuponRlz(rlz_2);
				kuponEsg_3 = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit - 1).getKuponRlz(rlz_3);
				kuponEsg_4 = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit - 1).getKuponRlz(rlz_4);
			} else {
				kuponEsg_1 = 0;
				kuponEsg_2 = 0;
				kuponEsg_3 = 0;
				kuponEsg_4 = 0;
			}
			//*** OW_LS Ende ***
			
		}
		kuponEsgArr[zeit] = kuponEsg;

		//*** OW_LS Beginn Flexible FI Neuanlage***
		kuponEsgArr_1[zeit] = kuponEsg_1;
		kuponEsgArr_2[zeit] = kuponEsg_2;
		kuponEsgArr_3[zeit] = kuponEsg_3;
		kuponEsgArr_4[zeit] = kuponEsg_4;
		//*** OW_LS Ende ***
		

		kuponEsgII = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).getKuponRlz(1);
		diskontEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).diskontFunktion;

		// MIL_W.Schalesi
		// OW_LS
		if (zeit > 0) {
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isFiNAmehrereRLZ()) {
				if ( MIL_Funktionen.OW_mittlererKupon(mmrCouponTriggerBoolean, kuponEsg_1, kuponEsg_2, kuponEsg_3, kuponEsg_4, berechnung.getDynManReg().getfiNaAnteilStandard(), 
						berechnung.getDynManReg().getfiNaAnteilAlternativ()) < berechnung.getZeitunabhManReg().getCouponTrigger()) {
					mmrCouponTrigger = -1;
				} else {
					mmrCouponTrigger = 1;
				}
			} else {
				if (kuponEsg < berechnung.getZeitunabhManReg().getCouponTrigger()) {
					mmrCouponTrigger = -1;
				} else {
					mmrCouponTrigger = 1;
				}
			}
		}

		if (zeit > 0) {
			jaehrlZinsEsg = EsgFormeln.jaehrlZinsEsg(vg.diskontEsg, diskontEsg);
		}

		zzrSpotEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).spotrate10jZZR;

		{
			final ReferenzZinssatz rz = berechnung.referenzZinssatz;
			AggZeile az = this;
			double[] zinsArr = new double[10];
			double[] zzrSpotEsgArr = new double[10];
			for (int i = 0; i <= 9; ++i) {
				if (zeit - i > 0) {
					zzrSpotEsgArr[i] = az.zzrSpotEsg;
				} else {
					zinsArr[i] = rz.getZins(zeit - i);
				}
				if (az != null)
					az = az.vg;
			}
			referenzZinssatz = Rohueberschuss.referenzZinssatz(zeit, zzrSpotEsgArr, zinsArr);
		}
		
		// MIL_W.Schalesi
		if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isMaxRefZinsRechnen()) {
			referenzZinssatz = Math.max(referenzZinssatz, 0);
		}
				
		if (zeit > 0) {
			refZins2M = Rohueberschuss.refZins2M(vg.referenzZinssatz, referenzZinssatz, vg.refZins2M, zzrSpotEsg, zeit,
					berechnung.getZeitunabhManReg().getParameter2M(), berechnung.getZeitunabhManReg().getStartRefZins());
		}
		preisAktieEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).aktien;
		preisImmoEsg = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit).immobilien;
	}

	/**
	 * Chronologisch rekursive Berechnung auf Ebene 1.
	 * 
	 * @param pfad
	 *            der gerechnet werden soll
	 */
	public void zeitRekursionL01(final int pfad) {
		
		// MIL_W.Schalesi
		mmrJueTriggerArr = fillArr(agg -> agg.mmrJueTrigger, mmrJueTriggerArr);

		deklsurplusfRfBarr = fillArr(agg -> agg.deklsurplusfRfB, deklsurplusfRfBarr);
		sUeAf56bEntnahmeArr = fillArr(agg -> agg.sUeAf56bEntnahme, sUeAf56bEntnahmeArr);
		fRfB56bEntnahmeArr = fillArr(agg -> agg.fRfB56bEntnahme, fRfB56bEntnahmeArr);
		bwFiNeuAnArr = fillArr(agg -> agg.bwFiNeuAn, bwFiNeuAnArr);
		kuponEsgArr[zeit] = kuponEsg;
		kedVerrechnungArr = fillArr(agg -> agg.kedVerrechnung, kedVerrechnungArr);
		kedVjVerrechnenArr = fillArr(agg -> agg.kedVjVerrechnen, kedVjVerrechnenArr);
		rohuebArr = fillArr(agg -> agg.rohueb, rohuebArr);
		rfBZufArr = fillArr(agg -> agg.rfBZuf, rfBZufArr);
		nfRfB56bArr = fillArr(agg -> agg.nfRfB56b, nfRfB56bArr);
		sUeAfZufSfArr = fillArr(agg -> agg.sUeAfZufSf, sUeAfZufSfArr);
		sUeAfEntSfArr = fillArr(agg -> agg.sUeAfEntSf, sUeAfEntSfArr);
		drLockInAggWennLoBArr = fillArr(agg -> agg.drLockInAggWennLoB, drLockInAggWennLoBArr);

		cfFis = new double[berechnung.zeitHorizont + 1];
		cfFiZeitschrittig = new double[berechnung.zeitHorizont + 1];

		// OW_LS
		bwFiNeuAnArr_1 = fillArr(agg -> agg.bwFiNeuAn_1, bwFiNeuAnArr_1);
		bwFiNeuAnArr_2 = fillArr(agg -> agg.bwFiNeuAn_2, bwFiNeuAnArr_2);
		bwFiNeuAnArr_3 = fillArr(agg -> agg.bwFiNeuAn_3, bwFiNeuAnArr_3);
		bwFiNeuAnArr_4 = fillArr(agg -> agg.bwFiNeuAn_4, bwFiNeuAnArr_4);
		kuponEsgArr_1[zeit] = kuponEsg_1;
		kuponEsgArr_2[zeit] = kuponEsg_2;
		kuponEsgArr_3[zeit] = kuponEsg_3;
		kuponEsgArr_4[zeit] = kuponEsg_4;
		
		if (zeit == 0) {
			// hier kann FI-CFs und FI-MW gerechnet werden (werte aus FiAusfall)!
			cfFis[0] = 0.0;
			cfFiZeitschrittig[0] = 0.0;
			for (int rlz = 1; rlz <= berechnung.zeitHorizont; ++rlz) {
				// Achtung: in Excel wird mit agg!$AR4 als zweitem Parameter gerechnet
				// dies ist aber leer, deshalb nehmen wir hier berechnung.zeitHorizont
				cfFis[rlz] = KaModellierung.cfFis(rlz, berechnung.zeitHorizont, bwFiNeuAn, kuponEsg, zeit,
						berechnung.getFiAusfall(rlz).cfFimitAusfallJahresende);

				cfFiZeitschrittig[rlz] = cfFis[rlz];
			}

			PfadZeile pfadZeile = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit);
			mwFiJahresende = KaModellierung.mwFiJahresende(rlz, cfFiZeitschrittig, pfadZeile, pfad, zeit,
					berechnung.bwAktivaFi.getMaxZeitCashflowFi(), berechnung.maxRlzNeuAnl);

		}

		if (zeit > 0) {
			lGarAgg = 0.0;
			drstKPAgg = 0.0;
			for (RzgZeile z : rzgZeilen) {
				lGarAgg += Functions.nanZero(z.lGarantiertDet);
				drstKPAgg += Functions.nanZero(z.drstKp);
			}
			
			// MIL_W.Schalesi
			// OW_LS 12.09.18: Es werden maximal die letzten 10 Jahre betrachtet
			//if (Functions.sum(mmrJueTriggerArr) >= 0){
			if (Functions.sum10(mmrJueTriggerArr) >= 0){
				mmrJueTriggerBoolean = false;
			}else{
				mmrJueTriggerBoolean = true;
			}
			mmrJueTriggerArr[zeit] = mmrJueTrigger;


			cashflowGesamt = 0.0;
			if (flvZeilen != null) {
				for (FlvZeile flvZeile : flvZeilen) {
					if (flvZeile.uebNueb.equals("UEB")) {
						cashflowGesamt += flvZeile.uebrigesErgebnis;
						cashflowGesamt += flvZeile.risikoErgebnis;
					} else if (flvZeile.uebNueb.equals("NUEB")) {
						cashflowGesamt += flvZeile.uebrigesErgebnis;
						cashflowGesamt += flvZeile.risikoErgebnis;
					}
				}
			}

			bwFiNeuAn = KaModellierung.bwFiNeuAn(vg.bwVerechnungJe, vg.bwFiVerechnungJe, vg.cfNeuAnlage, vg.aFiZielDaa,
					rlz, zeit);
			bwFiNeuAnArr[zeit] = bwFiNeuAn;


			//*** OW_LS Beginn Flexible FI Neuanlage***
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isFiNAmehrereRLZ()) {
				if (mmrCouponTriggerBoolean) {
					bwFiNeuAn_1 = berechnung.getDynManReg().getfiNaAnteilAlternativ().get(1) * bwFiNeuAn;
					bwFiNeuAn_2 = berechnung.getDynManReg().getfiNaAnteilAlternativ().get(2) * bwFiNeuAn;
					bwFiNeuAn_3 = berechnung.getDynManReg().getfiNaAnteilAlternativ().get(3) * bwFiNeuAn;
					bwFiNeuAn_4 = berechnung.getDynManReg().getfiNaAnteilAlternativ().get(4) * bwFiNeuAn;
				} else {
						bwFiNeuAn_1 = berechnung.getDynManReg().getfiNaAnteilStandard().get(1) * bwFiNeuAn;
						bwFiNeuAn_2 = berechnung.getDynManReg().getfiNaAnteilStandard().get(2) * bwFiNeuAn;
						bwFiNeuAn_3 = berechnung.getDynManReg().getfiNaAnteilStandard().get(3) * bwFiNeuAn;
						bwFiNeuAn_4 = berechnung.getDynManReg().getfiNaAnteilStandard().get(4) * bwFiNeuAn;
				}
			} else {
				bwFiNeuAn_1 = 0;
				bwFiNeuAn_2 = 0;
				bwFiNeuAn_3 = 0;
				bwFiNeuAn_4 = 0;
			}
			bwFiNeuAnArr_1[zeit] = bwFiNeuAn_1;
			bwFiNeuAnArr_2[zeit] = bwFiNeuAn_2;
			bwFiNeuAnArr_3[zeit] = bwFiNeuAn_3;
			bwFiNeuAnArr_4[zeit] = bwFiNeuAn_4;
			//*** OW_LS Ende***
			
			
			// jetzt kann CF_FI_s berechnet werten:
			//OW_LS Flexible FI Neuanlage
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isFiNAmehrereRLZ()) {
				for (int i = 0; i <= berechnung.zeitHorizont; ++i) {
					cfFis[i] = 	KaModellierung.cfFis(i, rlz_1, bwFiNeuAn_1, kuponEsg_1, zeit, 0.0) + KaModellierung.cfFis(i, rlz_2, bwFiNeuAn_2, kuponEsg_2, zeit, 0.0) +
								KaModellierung.cfFis(i, rlz_3, bwFiNeuAn_3, kuponEsg_3, zeit, 0.0) + KaModellierung.cfFis(i, rlz_4, bwFiNeuAn_4, kuponEsg_4, zeit, 0.0);
				}
			} else {
				for (int i = 0; i <= berechnung.zeitHorizont; ++i) {
					cfFis[i] = KaModellierung.cfFis(i, rlz, bwFiNeuAn, kuponEsg, zeit, 0.0);
				}
			}

			for (int i = 0; i < berechnung.zeitHorizont; ++i) {
				cfFiZeitschrittig[i] = cfFis[i] + vg.cfFiZeitschrittig[i + 1];
			}
			cfFiZeitschrittig[berechnung.zeitHorizont] = cfFis[berechnung.zeitHorizont];

			final PfadZeile pfadZeile = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit);

			mwFiJahresende = KaModellierung.mwFiJahresende(rlz, cfFiZeitschrittig, pfadZeile, pfad, zeit,
					berechnung.bwAktivaFi.getMaxZeitCashflowFi(),
					berechnung.getZeitabhManReg().get(zeit).getRlzNeuAnl());

			fiMw = mwFiJahresende;

			// ab hier neu in 3.0:
			bwReNeuAnl = KaModellierung.bwReNeuAnl(vg.aReZielDaa, vg.bwVerechnungJe, vg.bwReNachRls, vg.cfNeuAnlage,
					bwFiNeuAn, zeit, berechnung.laengeProjektionDr);
			awEq = KaModellierung.awEq(vg.aEqRlsI, vg.awEq, vg.mwEqVorRls, vg.cfEqRlsII, vg.cfNeuAnlage, bwFiNeuAn,
					bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp,
					berechnung.mwSaSp);
			awRe = KaModellierung.awRe(bwReNeuAnl, vg.aReRls, vg.awRe, zeit, berechnung.eqBuchwertKlassic,
					berechnung.mwEq0, berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp,
					berechnung.mwSaSp, vg.aReRlsPlan, vg.mwReVorRls);
			if (zeit != 1) {
				// Zeit == 1 wurde bereits im Konstruktor gerechnet!
				bwEq = KaModellierung.bwEq(vg.bwEqRlsII, vg.cfNeuAnlage, bwFiNeuAn, bwReNeuAnl, zeit,
						berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp, berechnung.mwSaSp);
				bwRe = KaModellierung.bwRe(vg.bwReNachRls, bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic,
						berechnung.mwEq0, berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp,
						berechnung.mwSaSp);
				mwEq = KaModellierung.mwEq(vg.mwEqRlsII, vg.cfNeuAnlage, bwFiNeuAn, bwReNeuAnl, zeit,
						berechnung.eqBuchwertKlassic, berechnung.mwEq0, berechnung.bwSaSp, berechnung.mwSaSp);
				mwRe = KaModellierung.mwRe(vg.mwRenachRls, bwReNeuAnl, zeit, berechnung.eqBuchwertKlassic,
						berechnung.mwEq0, berechnung.reBuchwertKlassic, berechnung.mwRe0, berechnung.bwSaSp,
						berechnung.mwSaSp);
				// Achtung: hier werden zwei Parameter immer aus Zeit 1 genommen!
				mwFianfangJ = KaModellierung.mwFiAnfangJ(vg.fiMw, bwFiNeuAn, zeit, berechnung.mwFi0, berechnung.mwEq0,
						berechnung.mwRe0, berechnung.getAggZeile(1).mwEq, berechnung.getAggZeile(1).mwRe,
						berechnung.mwSaSp);
			}

			mwEqVorRls = KaModellierung.mwEqVorRls(vg.preisAktieEsg, preisAktieEsg, mwEq);
			mwReVorRls = KaModellierung.mwReVorRls(vg.preisImmoEsg, preisImmoEsg, mwRe);
			keDiv = KaModellierung.keDiv(pfadZeile.dividenden, vg.preisAktieEsg, mwEq);
			keMieten = KaModellierung.keMieten(zeit, pfadZeile.mieten, vg.preisImmoEsg, mwRe, berechnung.arapMieten);
			keEqAbUndZuschreibung = KaModellierung.keEqAbUndZuschreibung(mwEqVorRls, bwEq, awEq,
					berechnung.getZeitunabhManReg().getAbschreibungsGrenzeEQ());
			keReAbUndZuschreibung = KaModellierung.keReAbUndZuschreibung(mwReVorRls, bwRe, awRe,
					berechnung.getZeitunabhManReg().getAbschreibungsGrenzeRE());
			keEqLaufend = KaModellierung.keEqLaufend(keDiv, keEqAbUndZuschreibung);
			keReLaufend = KaModellierung.keReLaufend(keMieten, keReAbUndZuschreibung);
			bwEqNachAbUndZuschreibung = KaModellierung.bwEqNachAbUndZuschreibung(bwEq, keEqAbUndZuschreibung);
			bwReNachAbUndZuschreibung = KaModellierung.bwReNachAbUndZuschreibung(bwRe, keReAbUndZuschreibung);
			bwrEqVorRls = KaModellierung.bwrEqVorRls(mwEqVorRls, bwEqNachAbUndZuschreibung);
			bwrReVorRls = KaModellierung.bwrReVorRls(mwReVorRls, bwReNachAbUndZuschreibung);

			bwrEqZiel = KaModellierung.bwrEqZiel(bwrEqVorRls, berechnung.getZeitunabhManReg().getBwrGrenzeEq(),
					berechnung.getZeitunabhManReg().getAnteilEqY());
			bwrReZiel = KaModellierung.bwrReZiel(bwrReVorRls, berechnung.getZeitunabhManReg().getBwrGrenzeRe(),
					berechnung.getZeitunabhManReg().getAnteilReY());
			aEqRlsI = KaModellierung.aEqRlsI(bwrEqVorRls, bwrEqZiel, berechnung.getZeitunabhManReg().getBwrGrenzeEq());
			aReRlsPlan = KaModellierung.aReRlsPlan(bwrReVorRls, bwrReZiel,
					berechnung.getZeitunabhManReg().getBwrGrenzeRe());
			bwEqRlsI = KaModellierung.bwEqRlsI(mwEqVorRls, bwEqNachAbUndZuschreibung, aEqRlsI);
			bwReRlsPlan = KaModellierung.bwReRlsPlan(mwReVorRls, bwReNachAbUndZuschreibung, aReRlsPlan);

			keEqRlsI = KaModellierung.keEqRlsI(mwEqVorRls, bwEqNachAbUndZuschreibung, aEqRlsI);
			keReRlsPlan = KaModellierung.keReRlsPlan(mwReVorRls, bwReNachAbUndZuschreibung, aReRlsPlan);

			bwFiAkt = KaModellierung.bwFiAkt(vg.zeit, vg.bwFiAkt, vg.cfFiAkt, vg.keFiAkt, berechnung.fiBuchwertBestand,
					berechnung.eqBuchwertKlassic, berechnung.reBuchwertKlassic, berechnung.getAggZeile(1).bwEq,
					berechnung.getAggZeile(1).bwRe, berechnung.bwSaSp, vg.rapVT, (vg.vg == null ? 0.0 : vg.vg.rapVT));
			kedVjVerrechnen = KaModellierung.kedVjVerrechnen(zeit, berechnung.getZeitabhManReg(), kedVerrechnungArr,
					berechnung.laengeProjektionDr, berechnung.isMillimanRechnen(), berechnung.getDynManReg().isDynTauTriggerRechnen());
			kedVjVerrechnenArr[zeit] = kedVjVerrechnen;
			bwFiVerrechnung = KaModellierung.bwFiVerrechnung(zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr,
					bwFiAkt, bwFiNeuAnArr, kedVerrechnungArr,
					kedVjVerrechnenArr);
			
			//*** OW_LS Beginn
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isFiNAmehrereRLZ()) {
				keFiNeuAnl_1 = KaModellierung.keFiNeuAnl(bwFiNeuAnArr_1, kuponEsgArr_1, zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr_1);
				keFiNeuAnl_2 = KaModellierung.keFiNeuAnl(bwFiNeuAnArr_2, kuponEsgArr_2, zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr_2);
				keFiNeuAnl_3 = KaModellierung.keFiNeuAnl(bwFiNeuAnArr_3, kuponEsgArr_3, zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr_3);
				keFiNeuAnl_4 = KaModellierung.keFiNeuAnl(bwFiNeuAnArr_4, kuponEsgArr_4, zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr_4);
				cfFiNeuAnl_1 = KaModellierung.cfFiNeuAnl(keFiNeuAnl_1, zeit, zpFaelligkeitArr_1, bwFiNeuAnArr_1);
				cfFiNeuAnl_2 = KaModellierung.cfFiNeuAnl(keFiNeuAnl_2, zeit, zpFaelligkeitArr_2, bwFiNeuAnArr_2);
				cfFiNeuAnl_3 = KaModellierung.cfFiNeuAnl(keFiNeuAnl_3, zeit, zpFaelligkeitArr_3, bwFiNeuAnArr_3);
				cfFiNeuAnl_4 = KaModellierung.cfFiNeuAnl(keFiNeuAnl_4, zeit, zpFaelligkeitArr_4, bwFiNeuAnArr_4);
			} else {
				keFiNeuAnl_1 = 0;
				keFiNeuAnl_2 = 0;
				keFiNeuAnl_3 = 0;
				keFiNeuAnl_4 = 0;
				cfFiNeuAnl_1 = 0;
				cfFiNeuAnl_2 = 0;
				cfFiNeuAnl_3 = 0;
				cfFiNeuAnl_4 = 0;
			}
			//*** OW_LS Ende ***
			
			// OW_M.Bartnicki
			if (berechnung.isMillimanRechnen()){
				if(berechnung.getDynManReg().isFiNAmehrereRLZ()) {
					keFiNeuAnl = keFiNeuAnl_1 + keFiNeuAnl_2 + keFiNeuAnl_3 + keFiNeuAnl_4;
					cfFiNeuAnl = cfFiNeuAnl_1 + cfFiNeuAnl_2 + cfFiNeuAnl_3 + cfFiNeuAnl_4;
				// }else if(mmrCouponTriggerBoolean){ <-- Dieser Fall muss nicht betrachtet werden, da die Funktion: KaModellierung.keFiNeuAnl() die rlz gar nicht verwendet!
				} else {	
					//Standard Fall
					keFiNeuAnl = KaModellierung.keFiNeuAnl(bwFiNeuAnArr, kuponEsgArr, zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr);
					cfFiNeuAnl = KaModellierung.cfFiNeuAnl(keFiNeuAnl, zeit, zpFaelligkeitArr, bwFiNeuAnArr);
				}
		    }else{
				//Standard Fall
				keFiNeuAnl = KaModellierung.keFiNeuAnl(bwFiNeuAnArr, kuponEsgArr, zeit, berechnung.laengeProjektionDr, zpFaelligkeitArr);
				cfFiNeuAnl = KaModellierung.cfFiNeuAnl(keFiNeuAnl, zeit, zpFaelligkeitArr, bwFiNeuAnArr);
			}
		
			
			cfFi = KaModellierung.cfFi(cfFiNeuAnl, cfFiAkt);
			keFi = KaModellierung.keFi(keFiAkt, keFiNeuAnl, kedVjVerrechnen);
			bwFiGesamtJe = KaModellierung.bwFiGesamtJe(bwFiVerrechnung, cfFi, keFi, rapVT, vg.rapVT);

			steuerVjAufgezinst = KaModellierung.aufzinsung(vg.ertragsSteuerLs, jaehrlZinsEsg, 0.0, zeit,
					berechnung.laengeProjektionDr);
			jueZiel = Rohueberschuss.jueZiel(berechnung.getZeitunabhManReg().getStrategie(), zeit,
					vg.eigenkapitalFortschreibung, berechnung.getZeitabhManReg().get(zeit).getREk(),
					berechnung.getZeitunabhManReg().getSteuersatz(), berechnung.getZeitunabhManReg().isiJuez(),
					vg.jUeZielerhoehung);
			zagFaellig = Rohueberschuss.zagFaellig(zeit, vg.zag);

			grNrd = Rohueberschuss.grNrd(zeit, vg.grNrd, rueckZahlung);
			
			//MIL_W.Schalesi
			if (berechnung.isMillimanRechnen() && mmrJueTriggerBoolean){
				fRfBMin = Deklaration.fRfBMin(vg.drLockInAggWennLoB, berechnung.getZeitunabhManReg().getpFrfbMinAlternative(), zeit);
			} else {
				fRfBMin = Deklaration.fRfBMin(vg.drLockInAggWennLoB, berechnung.getZeitunabhManReg().getpFrfbMin(), zeit);
			}
			
			//MIL_W.Schalesi
			if (berechnung.isMillimanRechnen() && mmrJueTriggerBoolean){
				fRfBMax = Deklaration.fRfBMax(vg.drLockInAggWennLoB, berechnung.getZeitunabhManReg().getpFrfbMaxAlternative(), zeit);	
			} else {
				fRfBMax = Deklaration.fRfBMax(vg.drLockInAggWennLoB, berechnung.getZeitunabhManReg().getpFrfbMax(), zeit);
			}	

			zagAufgezinst = KaModellierung.aufzinsung(zagFaellig, jaehrlZinsEsg,
					berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
			cashflowAufgezinst = KaModellierung.aufzinsung(cashflowGesamt, jaehrlZinsEsg,
					berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);

			cfKredit = KaModellierung.cfKredit(vg.kredit, vg.kuponEsgII, zeit);

		}

		for (RzgZeile rzg : rzgZeilen) {
			rzg.zeitRekursionL01(this);
		}
	}

	/**
	 * Chronologisch rekursive Berechnung auf Ebene 2.
	 * 
	 * @param pfad
	 *            der gerechnet werden soll
	 */
	public void zeitRekursionL02(final int pfad) {
		final ZeitunabhManReg zeitunabhManReg = berechnung.getZeitunabhManReg();

		// pauschale Initialisierungen für Summationen (auch Zeit 0)
		zzrAlt = 0.0;
		zzrNeu = 0.0;

		//OW_D.Hohmann
		zzrAlt_sl = 0.0;
		zzrNeu_sl = 0.0;

		zzrNueb = 0.0;
		drVorDeklAgg = 0.0;
		drVorDeklUebAgg = 0.0;
		lbwGarAgg = 0.0;
		fRfBVorEndzahlung = fRfBFrei;
		drVorDeklNuebAgg = 0.0;
		
		// pauschale Initialisierungen für Summationen (Zeit > 0)
		if (zeit > 0) {
			lGesAgg = 0.0;
			bStochAgg = 0.0;
			kStochAgg = 0.0;
			cfRvstochAgg = 0.0;
			ueEalt = 0.0;
			ueEneu = 0.0;

			//OW_D.Hohmann
			ueEalt_sl = 0.0;
			ueEneu_sl = 0.0;
			
			ueEnueb = 0.0;
			zuebCashflowAgg = 0.0;
			optionenCashflowAgg = 0.0;
			beitragRohUebAgg = 0.0;
			rmzUebAlt = 0.0;
			rmzUebNeu = 0.0;

			//OW_D.Hohmann
			rmzUebAlt_sl = 0.0;
			rmzUebNeu_sl = 0.0;

			rmzNueb = 0.0;
			ziRaZuStochAgg = 0.0;
			reAlt = 0.0;
			reNeu = 0.0;

			//OW_D.Hohmann
			reAlt_sl = 0.0;
			reNeu_sl = 0.0;

			risikoUebStochAgg = 0.0;
			lGarStochAgg = 0.0;
		}
		for (RzgZeile z : rzgZeilen) {
			// pauschale Additionen (immer)
			drVorDeklAgg += z.drVorDekl;
			lbwGarAgg += z.lbwGar;

			// pauschale Additionen (Zeit > 0)
			if (zeit > 0) {
				lGesAgg += z.lGesamt;
				bStochAgg += z.beitraegeStoch;
				kStochAgg += z.kostenStoch;
				cfRvstochAgg += z.cfRvStoch;
				zuebCashflowAgg += z.cashflowZuebRzg;
				optionenCashflowAgg += z.cashflowOptionenRzg;
				beitragRohUebAgg += z.beitragRueRzg;
				ziRaZuStochAgg += z.ziRaZuStoch;
				lGarStochAgg += z.lGarStoch;

			}

			if (z.uebNueb.equals("UEB")) {
				// Additionen nur UEB
				if (z.altNeuBestand.equals("a")) {
					// Speuialfall "alt", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					zzrAlt += z.zzrJ;
					if (zeit > 0) {
						ueEalt += z.kostenUebStoch;
						rmzUebAlt += z.rmZTarif;
						reAlt += z.risikoUebStoch;
					}
				}
				if (z.altNeuBestand.equals("n")) {
					// Speuialfall "neu", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					zzrNeu += z.zzrJ;
					if (zeit > 0) {
						ueEneu += z.kostenUebStoch;
						rmzUebNeu += z.rmZTarif;
						reNeu += z.risikoUebStoch;
					}
				}
				drVorDeklUebAgg += z.drVorDekl;

				//OW_D.Hohmann
				if (z.altNeuBestand.equals("a_sl")) {
					// Speuialfall "alt_sl", siehe
					// SummeUeberRZGzumGleichenZPWennAltNeu
					zzrAlt_sl += z.zzrJ;
					if (zeit > 0) {
						ueEalt_sl += z.kostenUebStoch;
						rmzUebAlt_sl += z.rmZTarif;
						reAlt_sl += z.risikoUebStoch;
					}
				}
				if (z.altNeuBestand.equals("n_sl")) {
					// Speuialfall "neu", siehe
					// SummeUeberRZGzumGleichenZPWennAltNeu
					zzrNeu_sl += z.zzrJ;
					if (zeit > 0) {
						ueEneu_sl += z.kostenUebStoch;
						rmzUebNeu_sl += z.rmZTarif;
						reNeu_sl += z.risikoUebStoch;
					}
				}	
			
			}
			if (z.uebNueb.equals("NUEB")) {
				// Additionen nur UEB
				zzrNueb += z.zzrJ;
				ueEnueb += z.kostenUebStoch;
				risikoUebStochAgg += z.risikoUebStoch;
				drVorDeklNuebAgg += z.drVorDekl;

				if (zeit > 0) {
					rmzNueb += z.rmZTarif;
				}
			}

		}
		aufzinsungcfEvuRvu = KaModellierung.aufzinsung(cfRvstochAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		aufzinsungBeitraege = KaModellierung.aufzinsung(bStochAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		aufzinsungKosten = KaModellierung.aufzinsung(kStochAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		aufzinsungGesamt = KaModellierung.aufzinsung(lGesAgg, jaehrlZinsEsg,
				berechnung.getZeitunabhManReg().getMonatZahlung(), zeit, berechnung.laengeProjektionDr);
		
		//OW_D.Hohmann
		//zzrGesamt = Rohueberschuss.zzrGesamt(zzrAlt, zzrNeu, zzrNueb);
		zzrGesamt = OW_Funktionen.zzrGesamt(zzrAlt, zzrNeu, zzrAlt_sl, zzrNeu_sl, zzrNueb);
		

		if (zeit > 0) {
			// OW_F.Wellens
			if(berechnung.isMillimanRechnen() && berechnung.getDynManReg().getAnteil_NG_am_uebrigen_Ergebnis()){
				ueEaltNoGcr = Rohueberschuss.kostenueberschussBestand(ueEalt, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[szenarioId-1]);
				ueEneuNoGcr = Rohueberschuss.kostenueberschussBestand(ueEneu, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[szenarioId-1]);
			} else {
				ueEaltNoGcr = Rohueberschuss.kostenueberschussBestand(ueEalt, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[0]);
				ueEneuNoGcr = Rohueberschuss.kostenueberschussBestand(ueEneu, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[0]);
			
			}

			//OW_D.Hohmann
			// OW_F.Wellens
			if(berechnung.isMillimanRechnen() && berechnung.getDynManReg().getAnteil_NG_am_uebrigen_Ergebnis()){
				ueEaltNoGcr_sl = Rohueberschuss.kostenueberschussBestand(ueEalt_sl, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[szenarioId-1]);
				ueEneuNoGcr_sl = Rohueberschuss.kostenueberschussBestand(ueEneu_sl, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[szenarioId-1]);			
			} else {
				ueEaltNoGcr_sl = Rohueberschuss.kostenueberschussBestand(ueEalt_sl, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[0]);
				ueEneuNoGcr_sl = Rohueberschuss.kostenueberschussBestand(ueEneu_sl, berechnung.getZeitabhManReg().get(zeit).getAnteilUebrigenErgebnisseNeugeschaeft()[0]);
			}
				
			deltaZzrUebAlt = Rohueberschuss.deltaZzr(zzrAlt, vg.zzrAlt);
			deltaZzrUebNeu = Rohueberschuss.deltaZzr(zzrNeu, vg.zzrNeu);

			//OW_D.Hohmann
			deltaZzrUebAlt_sl = Rohueberschuss.deltaZzr(zzrAlt_sl, vg.zzrAlt_sl);
			deltaZzrUebNeu_sl = Rohueberschuss.deltaZzr(zzrNeu_sl, vg.zzrNeu_sl);
			
			deltaZzrNueb = Rohueberschuss.deltaZzr(zzrNueb, vg.zzrNueb);

			//OW_D.Hohmann		
			//gcrUeB = KaModellierung.cfUebrEng(ueEalt, ueEneu, ueEaltNoGcr, ueEneuNoGcr);
			gcrUeB = OW_Funktionen.cfUebrEng(ueEalt, ueEneu, ueEaltNoGcr, ueEneuNoGcr, ueEalt_sl, ueEneu_sl, ueEaltNoGcr_sl, ueEneuNoGcr_sl);
			

			rmzUebGesamtAlt = Rohueberschuss.rmZGesamt(rmzUebAlt, deltaZzrUebAlt);
			rmzUebGesamtNeu = Rohueberschuss.rmZGesamt(rmzUebNeu, deltaZzrUebNeu);
			
			//OW_D.Hohmann		
			rmzUebGesamtAlt_sl = Rohueberschuss.rmZGesamt(rmzUebAlt_sl, deltaZzrUebAlt_sl);
			rmzUebGesamtNeu_sl = Rohueberschuss.rmZGesamt(rmzUebNeu_sl, deltaZzrUebNeu_sl);
						
			rmzNuebGesamt = Rohueberschuss.rmZGesamt(rmzNueb, deltaZzrNueb);
			
			anteilLobsKaStress = KaModellierung.anteilLobsKaStress(rzgZeilen, vg.szenarioId, vg.drLockInAgg, vg.sueAf,
					vg.zzrGesamt);

			aufwendungenKa = KaModellierung.kaAufwendungen(bwFiVerrechnung, bwRe, bwEq, vg.kredit,
					berechnung.getZeitunabhManReg().getFaktorKapitalanlagen(),
					berechnung.getZeitunabhManReg().getFaktorKapitalanlagenKostenStress(), anteilLobsKaStress);
			ak = KaModellierung.ak(vg.kredit, vg.kuponEsgII, zeit);

			keCfAufzinsung = KaModellierung.keCfAufzinsung(lGesAgg, bStochAgg, kStochAgg, cfRvstochAgg, zagFaellig,
					vg.ertragsSteuerLs, aufzinsungGesamt, aufzinsungBeitraege, aufzinsungKosten, aufzinsungcfEvuRvu,
					zagAufgezinst, steuerVjAufgezinst, zeit, berechnung.laengeProjektionDr, cashflowGesamt,
					cashflowAufgezinst);
			keRlsI = KaModellierung.keRlsI(keFi, keReLaufend, keEqLaufend, keEqRlsI, keReRlsPlan, ak, keCfAufzinsung);

			cfOhneKa = KaModellierung.cfOhneKa(aufzinsungGesamt, aufzinsungBeitraege, aufzinsungKosten, zagAufgezinst,
					steuerVjAufgezinst, aufzinsungcfEvuRvu, cashflowAufgezinst, zinsen, rueckZahlung, cfKredit, gcrUeB,
					aufwendungenKa);

			//OW_D.Hohmann
			//mindestkapitalertragLvrgAltNeu = Rohueberschuss.mindestkapitalertragLvrgAltNeu(berechnung.getZeitunabhManReg().getSchalterVerrechnungLebensversicherungsreformgesetz(), keRlsI, rmzUebGesamtAlt, rmzUebGesamtNeu, rmzNuebGesamt, reAlt, reNeu, risikoUebStochAgg, ueEaltNoGcr, ueEneuNoGcr, ueEnueb, jueZiel, ziRaZuStochAgg, zinsen);
			mindestkapitalertragLvrgAltNeu = OW_Funktionen.mindestkapitalertragLvrgAltNeu(berechnung.getZeitunabhManReg().getSchalterVerrechnungLebensversicherungsreformgesetz(), keRlsI, rmzUebGesamtAlt, rmzUebGesamtNeu, rmzUebGesamtAlt_sl, rmzUebGesamtNeu_sl,rmzNuebGesamt,
					reAlt, reNeu, reAlt_sl, reNeu_sl, risikoUebStochAgg, ueEaltNoGcr, ueEneuNoGcr, ueEaltNoGcr_sl, ueEneuNoGcr_sl, ueEnueb, jueZiel, ziRaZuStochAgg, zinsen);
			

			final double arapMieten = zeit == 1 ? berechnung.arapMieten : 0.0;
			bwVorRls = KaModellierung.bwVorRls(bwFiGesamtJe, bwReRlsPlan, bwEqRlsI, cfFi, keMieten, arapMieten, keDiv,
					cfOhneKa);
			mwVorRls = KaModellierung.mwVorRls(fiMw, mwReVorRls, mwEqVorRls, cfFi, keMieten, arapMieten, keDiv,
					cfOhneKa);
			//OW_D.Hohmann
			rzMittel = KaModellierung.rzMittel(zeit, berechnung.laengeProjektionDr, rmzUebAlt + rmzUebAlt_sl, rmzUebNeu + rmzUebNeu_sl, rmzNueb, vg.hgbDrAgg, vg.drLockInAgg, drVorDeklAgg);
		}
		aKaAufwendungen = KaModellierung.aKaAufwendungen(zeit, hgbDrAgg, drVorDeklAgg);
		aVN = KaModellierung.aVn(zeit, kAgg, bAgg, kStochAgg, bStochAgg);
		lockInFaktor = KaModellierung.lockInFaktor(zeit, lTodAgg, kaAgg, lSonstErlAgg, rkAgg, lGarStochAgg);

		final PfadZeile pfadZeile = berechnung.szenario.getPfad(pfad).getPfadZeile(zeit);
		mwVt = KaModellierung.mwVt(rlz, berechnung.leistGar, berechnung.restGar, berechnung.aufwendungenKa0,
				lockInFaktor, aVN, aKaAufwendungen, pfadZeile, pfad, zeit, berechnung.szenario.projektionsHorizont, // szenario.projektionsHorizont,
				berechnung.getZeitunabhManReg().getMonatZahlung());

		bwrPas = KaModellierung.bwrPas(drVorDeklAgg, zzrGesamt, mwVt);
		
		if (zeit > 0) {
			//MIL_W.Schalesi
			if (berechnung.isMillimanRechnen() && mmrCouponTriggerBoolean){
				aReZielDaa = KaModellierung.aReZielDaa(zeit, berechnung.laengeProjektionDr,
						berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
						berechnung.getZeitunabhManReg().getDaaFaktorRwAlternative(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwrAlternative(),
						berechnung.getZeitunabhManReg().getDaaFaktorRwVerlusteAlternative(),
						berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReservenAlternative(),
						berechnung.getZeitunabhManReg().getZielAnteilAReAlternative(),
						berechnung.getZeitunabhManReg().getZielAnteilReDaa(), vg.aReZielDaa,
						berechnung.getZeitunabhManReg().getaReZielInitialAlternative(),
						keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw,
						bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg, berechnung.getZeitunabhManReg().gethReZielAlternative(), berechnung.getZeitunabhManReg().gethReMaxAlternative());
			} else {
				aReZielDaa = KaModellierung.aReZielDaa(zeit, berechnung.laengeProjektionDr,
						berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
						berechnung.getZeitunabhManReg().getDaaFaktorRw(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwr(),
						berechnung.getZeitunabhManReg().getDaaFaktorRwVerluste(),
						berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReserven(),
						berechnung.getZeitunabhManReg().getZielAnteilARe(),
						berechnung.getZeitunabhManReg().getZielAnteilReDaa(), vg.aReZielDaa,
						berechnung.getZeitunabhManReg().getaReZielInitial(),
						keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw,
						bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg, berechnung.getZeitunabhManReg().gethReZiel(), berechnung.getZeitunabhManReg().gethReMax());
			}
			//MIL_W.Schalesi
			if (berechnung.isMillimanRechnen() && mmrCouponTriggerBoolean){
				aFiZielDaa = KaModellierung.aFiZielDaa(zeit, berechnung.laengeProjektionDr,
						zeitunabhManReg.getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
						zeitunabhManReg.getDaaFaktorRwAlternative(), zeitunabhManReg.getDaaFaktorFiBwrAlternative(),
						zeitunabhManReg.getDaaFaktorRwVerlusteAlternative(),
						zeitunabhManReg.getDaaFaktorUntergrenzePassiveAktiveReservenAlternative(), zeitunabhManReg.getaFiZielAlternative(),
						zeitunabhManReg.getZielMindestDaaFi(), vg.aFiZielDaa,
						zeitunabhManReg.getaFiZielInitialAlternative(),
						keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI,
						mwEqVorRls, bwrPas, drVorDeklAgg, zeitunabhManReg.gethFiZielAlternative(), zeitunabhManReg.gethFiMaxAlternative());
			} else {
				aFiZielDaa = KaModellierung.aFiZielDaa(zeit, berechnung.laengeProjektionDr,
						zeitunabhManReg.getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
						zeitunabhManReg.getDaaFaktorRw(), zeitunabhManReg.getDaaFaktorFiBwr(),
						zeitunabhManReg.getDaaFaktorRwVerluste(),
						zeitunabhManReg.getDaaFaktorUntergrenzePassiveAktiveReserven(), zeitunabhManReg.getaFiZiel(),
						zeitunabhManReg.getZielMindestDaaFi(), vg.aFiZielDaa,
						zeitunabhManReg.getaFiZielInitial(),
						keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI,
						mwEqVorRls, bwrPas, drVorDeklAgg, zeitunabhManReg.gethFiZiel(), zeitunabhManReg.gethFiMax());
			}
			
			//MIL_W.Schalesi
			if (mmrCouponTriggerBoolean) {
				aFiMinDaaStetig = MIL_Funktionen.mindestanteilFI_Stetig(zzrSpotEsg,
						berechnung.getDynManReg().getUntereSchranke(), berechnung.getDynManReg().getObereSchranke(),
						zeitunabhManReg.getaMinFiAlternative(), zeitunabhManReg.getaFiZielAlternative());
			} else {
				aFiMinDaaStetig = MIL_Funktionen.mindestanteilFI_Stetig(zzrSpotEsg,
						berechnung.getDynManReg().getUntereSchranke(), berechnung.getDynManReg().getObereSchranke(),
						zeitunabhManReg.getaMinFi(), zeitunabhManReg.getaFiZiel());
			}
			
			//MIL_W.Schalesi
			if (berechnung.isMillimanRechnen() && berechnung.getDynManReg().isRWTriggerRechnen()) {
				aFiMinDaa = KaModellierung.aFiMinDaa(zeit, berechnung.laengeProjektionDr,
						berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
						berechnung.getZeitunabhManReg().getDaaFaktorRwAlternative(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwrAlternative(),
						berechnung.getZeitunabhManReg().getDaaFaktorRwVerlusteAlternative(),
						berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReservenAlternative(),
						aFiMinDaaStetig, berechnung.getZeitunabhManReg().getZielMindestDaaFi(), vg.aFiMinDaa,
						berechnung.getZeitunabhManReg().getaFiMinInitialAlternative(),
						keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg, berechnung.getZeitunabhManReg().gethFiZielAlternative(), berechnung.getZeitunabhManReg().gethFiMaxAlternative());
			} else {
				if (berechnung.isMillimanRechnen() && mmrCouponTriggerBoolean) {	
					aFiMinDaa = KaModellierung.aFiMinDaa(zeit, berechnung.laengeProjektionDr,
							berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
							berechnung.getZeitunabhManReg().getDaaFaktorRwAlternative(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwrAlternative(),
							berechnung.getZeitunabhManReg().getDaaFaktorRwVerlusteAlternative(),
							berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReservenAlternative(),
							berechnung.getZeitunabhManReg().getaMinFiAlternative(), berechnung.getZeitunabhManReg().getZielMindestDaaFi(), vg.aFiMinDaa,
							berechnung.getZeitunabhManReg().getaFiMinInitialAlternative(),
							keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg, berechnung.getZeitunabhManReg().gethFiZielAlternative(), berechnung.getZeitunabhManReg().gethFiMaxAlternative());
				} else {
					aFiMinDaa = KaModellierung.aFiMinDaa(zeit, berechnung.laengeProjektionDr,
							berechnung.getZeitunabhManReg().getSteuerungsMethodeAssetAllokation(), nanZero(rzMittel),
							berechnung.getZeitunabhManReg().getDaaFaktorRw(), berechnung.getZeitunabhManReg().getDaaFaktorFiBwr(),
							berechnung.getZeitunabhManReg().getDaaFaktorRwVerluste(),
							berechnung.getZeitunabhManReg().getDaaFaktorUntergrenzePassiveAktiveReserven(),
							berechnung.getZeitunabhManReg().getaMinFi(), berechnung.getZeitunabhManReg().getZielMindestDaaFi(), vg.aFiMinDaa,
							berechnung.getZeitunabhManReg().getaFiMinInitial(),
							keRlsI, bwVorRls, mwVorRls, bwFiGesamtJe, fiMw, bwEqRlsI, mwEqVorRls, bwrPas, drVorDeklAgg, berechnung.getZeitunabhManReg().gethFiZiel(), berechnung.getZeitunabhManReg().gethFiMax());
				}
			}
		} else {
			// sdl: Initialisierung der Anteile der FI- bzw. RE-Titel an den gesamten Kapitalanlagen
			if (berechnung.isMillimanRechnen() && mmrCouponTriggerBoolean){
				aFiZielDaa = berechnung.getZeitunabhManReg().getaFiZielInitialAlternative();
				aFiMinDaa = berechnung.getZeitunabhManReg().getaFiMinInitialAlternative();
				aReZielDaa = berechnung.getZeitunabhManReg().getaReZielInitialAlternative();
			} else {
				aFiZielDaa = berechnung.getZeitunabhManReg().getaFiZielInitial();
				aFiMinDaa = berechnung.getZeitunabhManReg().getaFiMinInitial();
				aReZielDaa = berechnung.getZeitunabhManReg().getaReZielInitial();
			}
		}
		if (zeit > 0) {
			aReRls = KaModellierung.aReRls(bwReRlsPlan, mwReVorRls, aReZielDaa, bwEqRlsI, mwEqVorRls, bwFiGesamtJe,
					fiMw, bwVorRls, mwVorRls, keRlsI, mindestkapitalertragLvrgAltNeu, aufwendungenKa, zeit, aFiMinDaa,
					berechnung.laengeProjektionDr);
			cfReRls = KaModellierung.cfReRls(aReRls, mwReVorRls);
			keReRls = KaModellierung.keReRls(aReRls, mwReVorRls, bwReRlsPlan);
			bwReNachRls = KaModellierung.bwReNachRls(aReRls, bwReRlsPlan);
			mwRenachRls = KaModellierung.mwReNachRls(aReRls, mwReVorRls);
		}

		if (zeit > 0) {
			aEqRlsII = KaModellierung.aEqRlsII(bwEqRlsI, mwEqVorRls, bwFiGesamtJe, fiMw, bwReNachRls, bwVorRls,
					aReZielDaa, aFiZielDaa, aFiMinDaa, keReRls, keRlsI, mindestkapitalertragLvrgAltNeu, aufwendungenKa,
					keDiv, keMieten, zeit == 1 ? berechnung.arapMieten : 0.0, cfFi, cfReRls, cfOhneKa,
					berechnung.laengeProjektionDr, zeit);
			keEqRlsII = KaModellierung.keEqRlsII(aEqRlsII, mwEqVorRls, bwEqRlsI);
			keRlsII = KaModellierung.keRlsII(keRlsI, keReRls, keEqRlsII);
			kedVerrechnung = KaModellierung.kedVerrechnung(mindestkapitalertragLvrgAltNeu, keRlsII, fiMw, bwFiGesamtJe,
					aufwendungenKa);
			kedVerrechnungArr[zeit] = kedVerrechnung;

			keVerrechnung = KaModellierung.keVerrechnung(keRlsII, kedVerrechnung, ziRaZuStochAgg);
			bwFiVerechnungJe = KaModellierung.bwFiVerechnungJe(bwFiGesamtJe, kedVerrechnung);

			nvz = KaModellierung.nvz(keVerrechnung, aufwendungenKa, ak, ziRaZuStochAgg, drVorDeklAgg, zzrGesamt, grNrd,
					vg.drLockInAgg, vg.eigenkapitalFortschreibung, vg.zzrGesamt, vg.grNrd, vg.zag, vg.nfRfB,
					vg.ertragsSteuerLs, vg.kredit, berechnung.getZeitunabhManReg().getMonatZahlung());
			mwEqRlsII = KaModellierung.mwEqRlsII(aEqRlsII, mwEqVorRls);
			cfEqRlsII = KaModellierung.cfEqRlsII(aEqRlsII, mwEqVorRls);

			//OW.D_Hohmann
			//kapitalertragAnrechenbar = Rohueberschuss.kapitalertragAnrechenbar(zeit, nvz, drVorDeklUebAgg, vg.drLockInAggWennLoB, zzrAlt + zzrNeu, vg.zzrAlt + vg.zzrNeu, vg.nfRfB);
			kapitalertragAnrechenbar = Rohueberschuss.kapitalertragAnrechenbar(zeit, nvz, drVorDeklUebAgg, vg.drLockInAggWennLoB, zzrAlt + zzrNeu + zzrAlt_sl + zzrNeu_sl, vg.zzrAlt + vg.zzrNeu + vg.zzrAlt_sl + vg.zzrNeu_sl, vg.nfRfB);
			
			bwEqRlsII = KaModellierung.bwEqRlsII(aEqRlsII, bwEqRlsI);
			
			//OW.D_Hohmann
			//rohueb = Rohueberschuss.rohueb(keVerrechnung, rmzUebGesamtAlt + rmzUebGesamtNeu, rmzNuebGesamt, reAlt + reNeu, risikoUebStochAgg, ueEaltNoGcr + ueEneuNoGcr, ueEnueb, aufwendungenKa, zinsen);
			rohueb = Rohueberschuss.rohueb(keVerrechnung, rmzUebGesamtAlt + rmzUebGesamtNeu + rmzUebGesamtAlt_sl + rmzUebGesamtNeu_sl, rmzNuebGesamt, reAlt + reNeu + reAlt_sl + reNeu_sl, risikoUebStochAgg, ueEaltNoGcr + ueEneuNoGcr + ueEaltNoGcr_sl + ueEneuNoGcr_sl, ueEnueb, aufwendungenKa, zinsen);
			rohuebArr[zeit] = rohueb;

			//OW_D.Hohmann
			//mindZf = Rohueberschuss.mindZf(kapitalertragAnrechenbar, deltaZzrUebAlt + rmzUebAlt, reAlt, ueEaltNoGcr, vg.drLockInAlt, vg.zzrAlt, vg.sueAfAlt, deltaZzrUebNeu + rmzUebNeu, reNeu, ueEneuNoGcr, vg.drLockInNeu, vg.zzrNeu,vg.sueAfNeu);
			mindZf = OW_Funktionen.mindZf(kapitalertragAnrechenbar, deltaZzrUebAlt + rmzUebAlt, reAlt, ueEaltNoGcr, vg.drLockInAlt, vg.zzrAlt, vg.sueAfAlt, deltaZzrUebAlt_sl + rmzUebAlt_sl, reAlt_sl, ueEaltNoGcr_sl, vg.drLockInAlt_sl, vg.zzrAlt_sl, vg.sueAfAlt_sl, deltaZzrUebNeu + rmzUebNeu, reNeu, ueEneuNoGcr, vg.drLockInNeu, vg.zzrNeu, vg.sueAfNeu, deltaZzrUebNeu_sl + rmzUebNeu_sl, reNeu_sl, ueEneuNoGcr_sl, vg.drLockInNeu_sl, vg.zzrNeu_sl, vg.sueAfNeu_sl);
									

			mindZfGes = Rohueberschuss.mindZfGes(mindZf, vg.mindZfKk);

			// OW_M.Bartnicki
			// OW_L.Schlenke
			zusatzoption_UEB_ZielPJ = MIL_Funktionen.ow_Zielbeteiligung_ZielPJ(berechnung.szenario, szenarioId, vg.zusatzoption_UEB_SzenarioID ,vg.zusatzoption_UEB_ZielPJ, pfad, zeit, berechnung.laengeProjektionDr, 
					berechnung.getDynManReg().getUEB_Trigger_Crisis_Property(), berechnung.getDynManReg().getUEB_Trigger_Crisis_Equity(), berechnung.getDynManReg().getUEB_Trigger_Crisis_Interest(), berechnung.getDynManReg().getUEB_Laufzeit_Crisis_Interest(), 
					berechnung.getDynManReg().getUEB_Dauer_Crisis_Property(), berechnung.getDynManReg().getUEB_Dauer_Crisis_Equity(), berechnung.getDynManReg().getUEB_Dauer_Crisis_Interest(),
					berechnung.getDynManReg().getCrisis_Property_Stress_PJStart(), berechnung.getDynManReg().getCrisis_Equity_Stress_PJStart(), berechnung.getDynManReg().getCrisis_Interest_Stress_PJStart(), berechnung.getDynManReg().getCrisis_Spread_Stress_PJStart());
			
			zusatzoption_UEB_SzenarioID = MIL_Funktionen.ow_Zielbeteiligung_Shock_SzenarioID(berechnung.szenario, szenarioId, vg.zusatzoption_UEB_SzenarioID, vg.zusatzoption_UEB_ZielPJ, pfad, zeit, berechnung.laengeProjektionDr,
					berechnung.getDynManReg().getUEB_Trigger_Crisis_Property(), berechnung.getDynManReg().getUEB_Trigger_Crisis_Equity(), berechnung.getDynManReg().getUEB_Trigger_Crisis_Interest(), berechnung.getDynManReg().getUEB_Laufzeit_Crisis_Interest(),
					berechnung.getDynManReg().getUEB_Dauer_Crisis_Property(), berechnung.getDynManReg().getUEB_Dauer_Crisis_Equity(), berechnung.getDynManReg().getUEB_Dauer_Crisis_Interest(),
					berechnung.getDynManReg().getCrisis_Property_Stress_PJStart(), berechnung.getDynManReg().getCrisis_Equity_Stress_PJStart(), berechnung.getDynManReg().getCrisis_Interest_Stress_PJStart(), berechnung.getDynManReg().getCrisis_Spread_Stress_PJStart());
					

			// OW_M.Bartnicki
			// OW_L.Schlenke
			if(berechnung.isMillimanRechnen() && (berechnung.getDynManReg().isP_RohUebTriggerRechnen() || berechnung.getDynManReg().isflexUEBRechnen() || berechnung.getDynManReg().isP_RohUebTrigger_Op_An_Trigger())) {
				if(berechnung.getDynManReg().isP_RohUebTriggerRechnen()) {
					zielBeteiligungssatzRohueb_OW = berechnung.getDynManReg().getp_RohUebWerte().get(szenarioId);
				}else{
					if(berechnung.getDynManReg().isflexUEBRechnen()) {
						zielBeteiligungssatzRohueb_OW = MIL_Funktionen.ow_zielbeteiligung_rohUEB(rueAgg, kueAgg, drVorDeklAgg, berechnung.getDynManReg().getflexUEBGrenzen().get(1), berechnung.getDynManReg().getflexUEBGrenzen().get(2), berechnung.getDynManReg().getflexUEBGrenzen().get(3), berechnung.getDynManReg().getflexUEBGrenzen().get(4), berechnung.getDynManReg().getflexUEBAnteile().get(1), berechnung.getDynManReg().getflexUEBAnteile().get(2), berechnung.getDynManReg().getflexUEBAnteile().get(3), berechnung.getDynManReg().getflexUEBAnteile().get(4));
					}else {
						zielBeteiligungssatzRohueb_OW = MIL_Funktionen.ow_zielbeteiligung_Shock_UEB(zusatzoption_UEB_SzenarioID, berechnung.getZeitabhManReg().get(zeit).getRohUeb(), berechnung.getDynManReg().getUEB_Satz_Crisis_Property(),berechnung.getDynManReg().getUEB_Satz_Crisis_Equity(),berechnung.getDynManReg().getUEB_Satz_Crisis_Interest());
					}
				}			
			}else {
				zielBeteiligungssatzRohueb_OW = berechnung.getZeitabhManReg().get(zeit).getRohUeb();
			}

			
			// OW_M.Bartnicki: rfBZuf
			//OW.D_Hohmann
			if(berechnung.isMillimanRechnen() && berechnung.getDynManReg().isUebTrigger_aRechnen()) {
				rfBZuf = MIL_Funktionen.mil_jUERfBZuf(2, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr +ueEaltNoGcr_sl + ueEneuNoGcr_sl, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie,
						zielBeteiligungssatzRohueb_OW,
						berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(), berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
						berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB, keVerrechnung, kapitalertragAnrechenbar,
						berechnung.getZeitabhManReg().get(zeit).getdeltaNvz(), vg.eigenkapitalFortschreibung,
						berechnung.getZeitunabhManReg().getSteuersatz(),
						berechnung.getZeitunabhManReg().isiJuez(), vg.jUeZielerhoehung, nvz);
			}else {
				rfBZuf = Rohueberschuss.jUERfBZuf(2, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr + ueEaltNoGcr_sl + ueEneuNoGcr_sl, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie,
						zielBeteiligungssatzRohueb_OW,
						berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(),
						berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
						berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB,
						keVerrechnung, kapitalertragAnrechenbar);
			}	
			rfBZufArr[zeit] = rfBZuf;

			// OW_M.Bartnicki: nfRfB56b
			//OW.D_Hohmann
			if(berechnung.isMillimanRechnen() && berechnung.getDynManReg().isUebTrigger_aRechnen()) {
				nfRfB56b = MIL_Funktionen.mil_jUERfBZuf(3, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr + ueEaltNoGcr_sl + ueEneuNoGcr_sl, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie,
						zielBeteiligungssatzRohueb_OW,
			berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(),
			berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
			berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB, keVerrechnung, kapitalertragAnrechenbar,
			berechnung.getZeitabhManReg().get(zeit).getdeltaNvz(), vg.eigenkapitalFortschreibung,
			berechnung.getZeitunabhManReg().getSteuersatz(),
			berechnung.getZeitunabhManReg().isiJuez(), vg.jUeZielerhoehung, nvz);
			}else {
				nfRfB56b = Rohueberschuss.jUERfBZuf(3, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr + ueEaltNoGcr_sl + ueEneuNoGcr_sl, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie,
						zielBeteiligungssatzRohueb_OW, 
						berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(), 
						berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf, 
						berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg,
						vg.drLockInAggWennLoB, keVerrechnung, kapitalertragAnrechenbar);
			}
			nfRfB56bArr[zeit] = nfRfB56b;

			
			// OW_M.Bartnicki: jue
			//OW.D_Hohmann
			if(berechnung.isMillimanRechnen() && berechnung.getDynManReg().isUebTrigger_aRechnen()) {
				jue = MIL_Funktionen.mil_jUERfBZuf(1, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr + ueEaltNoGcr_sl + ueEneuNoGcr_sl, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie,
						zielBeteiligungssatzRohueb_OW,
						berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(), berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf,
						berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg, vg.drLockInAggWennLoB, keVerrechnung, kapitalertragAnrechenbar,
						berechnung.getZeitabhManReg().get(zeit).getdeltaNvz(), vg.eigenkapitalFortschreibung,
						berechnung.getZeitunabhManReg().getSteuersatz(),
						berechnung.getZeitunabhManReg().isiJuez(), vg.jUeZielerhoehung, nvz);
			}else {
				jue = Rohueberschuss.jUERfBZuf(1, zeit, rohuebArr, mindZfGes, mindZf, jueZiel, ueEaltNoGcr + ueEneuNoGcr + ueEaltNoGcr_sl + ueEneuNoGcr_sl, vg.fRfBFrei, rfBZufArr, berechnung.vuHistorie,
						zielBeteiligungssatzRohueb_OW, 
						berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme(), berechnung.getZeitabhManReg().get(zeit).getSueafEntnahme(), vg.sueAf, berechnung.getZeitunabhManReg().getStrategie(), nfRfB56bArr, drVorDeklUebAgg,
						vg.drLockInAggWennLoB, keVerrechnung, kapitalertragAnrechenbar);
			}
				
			
			// MIL_W.Schalesi
			if (jue < Math.abs(0.01)) {
				mmrJueTrigger = -1;
			} else {
				mmrJueTrigger = 1;
			}
							
			jUeZielerhoehung = Rohueberschuss.jUeZielerhoehung(zeit, berechnung.getZeitunabhManReg().isiJuez(), jueZiel, jue, berechnung.getZeitunabhManReg().getStrategie());
			
			mindZfKk = Rohueberschuss.mindZfKk(zeit, vg.mindZfKk, mindZf, rfBZuf);
			ertragssteuer = Rohueberschuss.ertragssteuer(jue, berechnung.getZeitunabhManReg().getSteuersatz(), vg.vv);
			vv = Rohueberschuss.vv(vg.vv, jue, ertragssteuer, berechnung.getZeitunabhManReg().getSteuersatz());
			ertragsSteuerLs = Rohueberschuss.ertragssteuerLs(ertragssteuer, vg.lsHgb);
			lsHgb = Rohueberschuss.lsHgb(vg.lsHgb, ertragssteuer, ertragsSteuerLs);

			mittlRfBZufuehrung = Deklaration.mittlRfBZufuehrung(
					berechnung.getZeitunabhManReg().getAnzahlJahreDurchschnittlRfbZufuehrung(), zeit, rfBZufArr,
					drLockInAggWennLoBArr, berechnung.vuHistorie);

			sUeAf56bEntnahme = Deklaration.sUeAf56bEntnahme(vg.fRfBFrei, nfRfB56b, zeit,
					berechnung.getZeitabhManReg().get(zeit).getRfbEntnahme());
			sUeAf56bEntnahmeArr[zeit] = sUeAf56bEntnahme;
			fRfB56bEntnahme = nfRfB56b - sUeAf56bEntnahme;
			fRfB56bEntnahmeArr[zeit] = fRfB56bEntnahme;
			zielDeklaration = Deklaration.zielDeklaration(mittlRfBZufuehrung, vg.drLockInAggWennLoB);
			fRfBUeberlauf = Deklaration.fRfBUeberlauf(vg.fRfBFrei, nfRfB56b, sUeAf56bEntnahme, rfBZuf, zielDeklaration, fRfBMax, zeit,
					berechnung.laengeProjektionDr);
			fRfBVorEndzahlung = Deklaration.fRfBVorEndzahlung(vg.fRfBFrei, rfBZuf, nfRfB56b, fRfBMin, fRfBMax,
					zielDeklaration, sUeAf56bEntnahme)+vg.uebertragFestgelegteInFreieRfBAgg;
			
			//MIL_W.Schalesi
			if (berechnung.isMillimanRechnen() && mmrJueTriggerBoolean){
				dekl = Deklaration.dekl(vg.fRfBVorEndzahlung, fRfBVorEndzahlung, rfBZuf, nfRfB56b, sUeAf56bEntnahme, 
						fRfBMin, zielDeklaration, berechnung.getZeitabhManReg().get(zeit).getFrfbUeberlaufAlternative(), 
						fRfBUeberlauf, zeit, berechnung.laengeProjektionDr);	
			} else {
					
				dekl = Deklaration.dekl(vg.fRfBVorEndzahlung, fRfBVorEndzahlung, rfBZuf, nfRfB56b, sUeAf56bEntnahme,
						fRfBMin, zielDeklaration, berechnung.getZeitabhManReg().get(zeit).getFrfbUeberlauf(), fRfBUeberlauf,
						zeit, berechnung.laengeProjektionDr);
			}
			
			if (lbwGarAgg > 0.001) {
				fRfBFrei = fRfBVorEndzahlung;
			} else {
				fRfBFrei = 0.0;
			}
			
			//OW_D.Hohmann
			deklRest = Deklaration.deklRest(berechnung.getZeitunabhManReg().getDeklarationsMethode(), dekl, 
					berechnung.getZeitabhManReg().get(zeit).getGrundUeberschuss(), reAlt + reAlt_sl, reNeu + reNeu_sl, ueEaltNoGcr + ueEaltNoGcr_sl, ueEneuNoGcr + ueEneuNoGcr_sl);

			deklZins = Deklaration.deklZins(berechnung.getZeitunabhManReg().getDeklarationsMethode(), dekl, deklRest);
			vzGes = Deklaration.vzGes(deklZins, rzgZeilen);
		}

		for (RzgZeile rzg : rzgZeilen) {
			rzg.zeitRekursionL02(this);
		}
	}

	/**
	 * Chronologisch rekursive Berechnung auf Ebene 3.
	 */
	public void zeitRekursionL03() {
		endZahlungAgg = 0.0;
		drLockInAgg = 0.0;
		drLockInAggWennLoB = 0.0;
		drLockInAlt = 0.0;
		drLockInNeu = 0.0;

		//OW_D.Hohmann
		drLockInAlt_sl = 0.0;
		drLockInNeu_sl = 0.0;

		barAgg = 0.0;
		drGesAgg = 0.0;
		sueAFZufFRfBUeberlaufAgg = 0.0;
		sueAFZufAgg = 0.0;
		sueAFEntnahmeAgg = 0.0;
		sueAfAlt = 0.0;
		sueAfNeu = 0.0;

		//OW_D.Hohmann
		sueAfAlt_sl = 0.0;
		sueAfNeu_sl = 0.0;

		lockInAgg = 0.0;
		if (zeit > 0) {
			rohuebKpK = 0.0;
			rohuebKpN = 0.0;
			rohuebKpP = 0.0;
			deltaLAgg = 0.0;
		}
		for (RzgZeile rzg : rzgZeilen) {
			endZahlungAgg += rzg.endZahlung;
			drLockInAgg += rzg.drLockInRzg;
			barAgg += nanZero(rzg.bar);
			sueAFZufFRfBUeberlaufAgg += nanZero(rzg.sueafZufFrfbUeberlauf);
			sueAFZufAgg += nanZero(rzg.sUeAfzuf);
			sueAFEntnahmeAgg += nanZero(rzg.sUeAfEntnahme);
			drGesAgg += nanZero(rzg.drGesamtRzg);
			lockInAgg += rzg.lockIn;
			if (zeit > 0) {
				rohuebKpK += rzg.rohuebKpRzgBY;
				rohuebKpN += rzg.rohuebKpRzgNeg;
				rohuebKpP += rzg.rohuebKpRzg;
				deltaLAgg += rzg.deltaLRzg;
			}
			if (rzg.uebNueb.equals("UEB")) {
				// Additionen nur UEB
				if (rzg.altNeuBestand.equals("a")) {
					// Speuialfall "alt", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					drLockInAlt += rzg.drLockInRzg;
					sueAfAlt += rzg.sUeAfRzg;
				}
				if (rzg.altNeuBestand.equals("n")) {
					// Speuialfall "neu", siehe SummeUeberRZGzumGleichenZPWennAltNeu
					drLockInNeu += rzg.drLockInRzg;
					sueAfNeu += rzg.sUeAfRzg;
				}
				
				//OW_D.Hohmann
				if (rzg.altNeuBestand.equals("a_sl")) {
					// Speuialfall "alt_sl", siehe
					// SummeUeberRZGzumGleichenZPWennAltNeu
					drLockInAlt_sl += rzg.drLockInRzg;
					sueAfAlt_sl += rzg.sUeAfRzg;
				}
				if (rzg.altNeuBestand.equals("n_sl")) {
					// Speuialfall "neu_sl", siehe
					// SummeUeberRZGzumGleichenZPWennAltNeu
					drLockInNeu_sl += rzg.drLockInRzg;
					sueAfNeu_sl += rzg.sUeAfRzg;
				}
						
				drLockInAggWennLoB += rzg.drLockInRzg;
			}
		}
		drLockInAggWennLoBArr[zeit] = drLockInAggWennLoB;

		if (zeit > 0) {
			jueVnKp = Bilanzpositionen.jueVnKp(kapitalertragAnrechenbar, keVerrechnung, aufwendungenKa, rohuebKpP,
					rohuebKpK, jue, vg.hgbDrAgg, vg.drstKPAgg);
			deklsurplusfRfB = Bilanzpositionen.deklSurplusFRfB(vg.berechnung.hgbBilanzdaten.getFreieRfbBuchwert(),
					dekl + sueAFZufFRfBUeberlaufAgg, vg.deklsurplusfRfBarr, vg.fRfB56bEntnahmeArr);
			deklsurplusfRfBarr[zeit] = deklsurplusfRfB;

			barSf = Bilanzpositionen.barSf(dekl, sueAFZufFRfBUeberlaufAgg, barAgg, deklsurplusfRfB);
			sUeAfZufSf = Bilanzpositionen.sUeAfZufSf(dekl, sueAFZufFRfBUeberlaufAgg, sueAFZufAgg, deklsurplusfRfB);
			sUeAfZufSfArr[zeit] = sUeAfZufSf;

			eigenkapitalFortschreibung = Rohueberschuss.eigenkapitalFortschreibung(zeit,
					berechnung.getZeitabhManReg().get(zeit).getEkZiel(), drGesAgg);
			zag = Rohueberschuss.zag(zeit, jue, ertragsSteuerLs, eigenkapitalFortschreibung,
					vg.eigenkapitalFortschreibung, berechnung.laengeProjektionDr);

			lockInSf = Bilanzpositionen.lockInSf(deklsurplusfRfB, barSf, sUeAfZufSf);
			leAggrSf = Bilanzpositionen.leAggrSf(vg.leAggrSf, lockInAgg, lockInSf, lbwGarAgg, vg.nfRfB,
					vg.drLockInAggWennLoB);

			{
				final double arapMieten = zeit == 1 ? berechnung.arapMieten : 0.0;
				cfVorKredit = KaModellierung.cfVorKredit(cfFi, cfReRls, cfEqRlsII, cfOhneKa, endZahlungAgg, keDiv,
						keMieten, arapMieten);
				bwRlsII = KaModellierung.bwRlsII(bwFiGesamtJe, bwReNachRls, bwEqRlsII, cfFi, keMieten, keDiv, cfReRls,
						cfEqRlsII, cfOhneKa, endZahlungAgg, arapMieten);
			}
			bwVerechnungJe = KaModellierung.bwVerrechnungJe(bwRlsII, kedVerrechnung);
			kredit = KaModellierung.k(cfVorKredit, zeit, berechnung.laengeProjektionDr);
			cfNeuAnlage = KaModellierung.cfNeuanlage(cfVorKredit, kredit, zeit, berechnung.laengeProjektionDr);

			zagEndzahlung = Rohueberschuss.zagEndzahlung(zeit, berechnung.laengeProjektionDr, cfVorKredit,
					ertragssteuer);
			mwRlsII = KaModellierung.mwRlsII(fiMw, mwRenachRls, mwEqRlsII, cfFi, keMieten, keDiv, cfReRls, cfEqRlsII,
					cfOhneKa, zeit == 1 ? berechnung.arapMieten : 0.0, endZahlungAgg);
			sUeAfEntSf = Bilanzpositionen.sueafEntSf(berechnung.getAggZeile(0).sueAf, sueAFEntnahmeAgg, sUeAfZufSfArr,
					vg.sUeAfEntSfArr, vg.sUeAf56bEntnahmeArr);
			sUeAfEntSfArr[zeit] = sUeAfEntSf;
			cashflowSf = Bilanzpositionen.cashflowSf(barSf, sUeAfEntSf, leAggrSf, lTodAgg, kaAgg, rkAgg, lSonstErlAgg,
					jaehrlZinsEsg, berechnung.getZeitunabhManReg().getMonatZahlung(), zeit,
					berechnung.laengeProjektionDr);
		}

		//OW_D.Hohmann
		//sueAf = Rohueberschuss.sueAf(sueAfAlt, sueAfNeu);
		sueAf = OW_Funktionen.sueAf(sueAfAlt, sueAfNeu, sueAfAlt_sl, sueAfNeu_sl);
		nfRfB = Rohueberschuss.nfRfB(sueAf, fRfBFrei);

		for (RzgZeile rzg : rzgZeilen) {
			rzg.zeitRekursionL03(this);
		}

	}

	/**
	 * Prüfe, ob die Daten hier und in RzgZeile finit sind. Falls nicht, wird eine {@link ResultNotFinite} Exception
	 * geworfen.
	 */
	public void checkFinite() {
		// in Zeit == 0 gibt es viele NaN, die lassen wir weg
		if (zeit > 0) {
			final List<String> errors = new ArrayList<>();
			errors.addAll(CheckData.checkFinite(this));
			for (RzgZeile rzg : rzgZeilen) {
				errors.addAll(CheckData.checkFinite(rzg));
			}
			if (errors.size() > 0) {
				final String header = "In Szenario " + szenarioId + " (" + szenario + "), pfad = "
						+ berechnung.getAktuellerPfad() + ", zeit = " + zeit + " traten Überläufe auf in den Feldern";
				final String felder = errors.stream().reduce("", (x, y) -> (x.isEmpty() ? x : x + ", ") + y);
				throw new ResultNotFinite(header + ": " + felder, header, felder);
			}
		}
	}

	/**
	 * Fülle ein Array von double rekursiv rückwärts über die Zeit. Aus technischen Gründen wird als aktueller Wert NaN
	 * eingetragen.
	 * 
	 * @param f
	 *            die Funktion, die auf Zeilen angewendet den gewünschten Wert erbigt
	 * @param data
	 *            das zu füllende Array, es wird erzeugt, wenn es null ist
	 * @return das Array
	 */
	public double[] fillArr(final Function<AggZeile, Double> f, double[] data) {
		if (data == null) {
			data = new double[zeit + 1];
		}
		final int index;
		if (vg != null) {
			index = vg.fillArrRecursive(f, data);
		} else {
			index = 0;
		}
		if (index != zeit)
			throw new IllegalStateException("Vorgänger und Zeiten divergieren: index = " + index + ", zeit = " + zeit);
		data[index] = Double.NaN;
		return data;
	}

	/**
	 * Fülle ein Array von ints rekursiv rückwärts über die Zeit. Aus technischen Gründen wird als aktueller Wert NaN
	 * eingetragen.
	 * 
	 * @param f
	 *            die Funktion, die auf Zeilen angewendet den gewünschten Wert erbigt
	 * @param data
	 *            das zu füllende Array, es wird erzeugt, wenn es null ist
	 * @return das Array
	 */
	public int[] fillArrInt(final Function<AggZeile, Integer> f, int[] data) {
		if (data == null) {
			data = new int[zeit + 1];
		}
		final int index;
		if (vg != null) {
			index = vg.fillArrRecursiveInt(f, data);
		} else {
			index = 0;
		}
		if (index != zeit)
			throw new IllegalStateException("Vorgänger und Zeiten divergieren: index = " + index + ", zeit = " + zeit);
		data[index] = Integer.MAX_VALUE;
		return data;
	}

	private final int fillArrRecursive(final Function<AggZeile, Double> f, final double[] data) {
		if (vg == null) {
			data[0] = f.apply(this);
			return 1;
		} else {
			final int index = vg.fillArrRecursive(f, data);
			data[index] = f.apply(this);
			return index + 1;
		}
	}

	private final int fillArrRecursiveInt(final Function<AggZeile, Integer> f, final int[] data) {
		if (vg == null) {
			data[0] = f.apply(this);
			return 1;
		} else {
			final int index = vg.fillArrRecursiveInt(f, data);
			data[index] = f.apply(this);
			return index + 1;
		}
	}

	// ========================================================================
	// Get-Funktionen des Blattes FI CFs.

	/**
	 * Wert zur Restlaufzeit der CF FIs zur selben Zeit. Blatt FI CFs.
	 * 
	 * @param rlz
	 *            die Restlaufzeit
	 * @return der Wert
	 */
	public double getCfFis(final int rlz) {
		return cfFis[rlz];
	}

	/**
	 * Wert zur Restlaufzeit der CF FI Zeitschrittig zur selben Zeit. Blatt FI MW.
	 * 
	 * @param rlz
	 *            die Restlaufzeit
	 * @return der Wert
	 */
	public double getCfFiZeitschrittig(final int rlz) {
		return cfFiZeitschrittig[rlz];
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten A bis Z.

	/**
	 * Zeitpunkt. C.
	 * 
	 * @return der Wert
	 */
	public int getZeit() {
		return zeit;
	}
	
	/**
	 * Kosten. D.
	 * 
	 * @return der Wert
	 */
	public double getKAgg() {
		return kAgg;
	}

	/**
	 * Prämien. E
	 * 
	 * @return der Wert
	 */
	public double getBAgg() {
		return bAgg;
	}
	
	
	/**
	 * Übertrag freie in festgelegte RfB
	 * 
	 * @return der Wert
	 */
	public double getUebertragFestgelegteInFreieRfBAgg() {
		return uebertragFestgelegteInFreieRfBAgg;
	}
	
	

	// ========================================================================
	// Get-Funktionen der agg-Spalten AA bis AZ.

	/**
	 * Zinsen, Nachrangliche Verbindlichkeiten. AM.
	 * 
	 * @return der Wert
	 */
	public double getZinsen() {
		return zinsen;
	}

	/**
	 * Rückzahlung, Nachrangliche Verbindlichkeiten. AN.
	 * 
	 * @return der Wert
	 */
	public double getRueckZahlung() {
		return rueckZahlung;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten BA bis BZ.
	
	/**
	 * FI_MW. BS.
	 * 
	 * @return der Wert
	 */
	public double getfiMw() {
		return fiMw;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten CA bis CZ.

	// ========================================================================
	// Get-Funktionen der agg-Spalten DA bis DZ.

	/**
	 * Aufwendungen für KA. DO.
	 * 
	 * @return der Wert
	 */
	public double getAufwendungenKa() {
		return aufwendungenKa;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten EA bis EZ.

	/**
	 * GCR übriges Ergebnis an Neugeschäft - ÜEB. EI.
	 * 
	 * @return der Wert
	 */
	public double getGcrUeB() {
		return gcrUeB;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten FA bis FZ.

	/**
	 * ZAG, fällig. FA.
	 * 
	 * @return der Wert
	 */
	public double getZagFaellig() {
		return zagFaellig;
	}

	/**
	 * ZAG, Endzahlung. FB.
	 * 
	 * @return der Wert
	 */
	public double getZagEndzahlung() {
		return zagEndzahlung;
	}

	/**
	 * Steuer, festgelegt. FC.
	 * 
	 * @return der Wert
	 */
	public double getErtragssteuer() {
		return ertragssteuer;
	}

	/**
	 * Steuer, festgelegt, nach der LS-Korrektur. FD.
	 * 
	 * @return der Wert
	 */
	public double getErtragsSteuerLs() {
		return ertragsSteuerLs;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten GA bis GZ.

	/**
	 * Sämtliche garantierte Leistungen. GD.
	 * 
	 * @return der Wert
	 */
	public double getLGarAgg() {
		return lGarAgg;
	}

	/**
	 * Leistungen Gesamt. GF.
	 * 
	 * @return der Wert
	 */
	public double getLGesAgg() {
		return lGesAgg;
	}

	/**
	 * Beiträge, stochastisch. GG.
	 * 
	 * @return der Wert
	 */
	public double getBStochAgg() {
		return bStochAgg;
	}

	/**
	 * Kosten, stochastisch. GH.
	 * 
	 * @return der Wert
	 */
	public double getKStochAgg() {
		return kStochAgg;
	}

	/**
	 * Cashflow EVU -> RVU, stochastisch. GI.
	 * 
	 * @return der Wert
	 */
	public double getCfRvstochAgg() {
		return cfRvstochAgg;
	}

	/**
	 * Cashflow, Risikoergebnis + Kostenergebnis FLV Aufschub, aufgezinst. GK.
	 * 
	 * @return der Wert
	 */
	public double getCashflowGesamt() {
		return cashflowGesamt;
	}

	/**
	 * Diskontfunktion. GL.
	 * 
	 * @return der Wert
	 */
	public double getDiskontEsg() {
		return diskontEsg;
	}

	/**
	 * Mittlerer jährlicher Zins (für Aufzinsung). GM.
	 * 
	 * @return der Wert
	 */
	public double getJaehrlZinsEsg() {
		return jaehrlZinsEsg;
	}

	/**
	 * Leistungen durch Endzahlung. GQ.
	 * 
	 * @return der Wert
	 */
	public double getEndZahlungAgg() {
		return endZahlungAgg;
	}

	/**
	 * Passive Reserve. GX.
	 * 
	 * @return der Wert
	 */
	public double getbwrPas() {
		return bwrPas;
	}

	// ========================================================================
	// Get-Funktionen der agg-Spalten HA bis HZ.

	/**
	 * Cashflow gesamt, Überschussfonds. HE.
	 * 
	 * @return der Wert
	 */
	public double getCashflowSf() {
		return cashflowSf;
	}

	/**
	 * Jahresüberschuss geschlüsselt auf künftige Prämien. HK.
	 * 
	 * @return der Wert
	 */
	public double getJueVnKP() {
		return jueVnKp;
	}

	/**
	 * ZÜB: Cashflow gesamt, Überschussbeteiligung, ohne Endzahlung.HL.
	 * 
	 * @return der Wert
	 */
	public double getZuebCashflowAgg() {
		return zuebCashflowAgg;
	}

	/**
	 * ZÜB: Cashflow gesamt, Überschussbeteiligung, ohne Endzahlung. HM.
	 * 
	 * @return der Wert
	 */
	public double getOptionenCashflowAgg() {
		return optionenCashflowAgg;
	}

	@Override
	public String toString() {
		return "[agg " + zeit + "]";
	}
}
