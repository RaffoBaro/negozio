package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import dao.CartaFedeltaDAO;
import dao.ConnessioneDB;
import dao.ScontiDAO;
import entities.CartaFedelta;
import entities.Sconti;

public class ScontiService {

	private Scanner scanner;
	private ScontiDAO dao;
	private CartaFedeltaDAO cartaDao;

	public ScontiService() throws SQLException {
		this.dao = new ScontiDAO();
		this.cartaDao = new CartaFedeltaDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaSconti() {
		List<Sconti> listaSconti = dao.recuperaTutti();

		if (listaSconti == null) {
			System.out.println("ERRORE in fase di elaborazione");
		} else if (listaSconti.isEmpty()) {
			System.out.println("Non ci sono sconti");
		} else {
			Formatter.stampaRigaFormattata("CODICE SCONTO", "CODICE CARTA", "DATA INIZIO", "DATA FINE", "SCONTO");
			System.out.println(
					"----------------------------------------------------------------------------------------------------");
			for (Sconti s : listaSconti) {
				Formatter.stampaRigaFormattata(String.valueOf(s.getCodiceSconto()), String.valueOf(s.getCodiceCarta()),
						String.valueOf(s.getDataInizio()), String.valueOf(s.getDataFine()),
						String.valueOf(s.getSconto()));
			}
		}
	}

	public void visualizzaSconto() {
		try {
			System.out.println("Dimmi codice sconto per visualizzare lo sconto desiderato");
			int codiceSconto = scanner.nextInt();
			scanner.nextLine();

			Sconti s = dao.recuperaUno(codiceSconto);

			if (s != null) {
				Formatter.stampaRigaFormattata("CODICE SCONTO", "CODICE CARTA", "DATA INIZIO", "DATA FINE", "SCONTO");
				System.out.println("-------------------------------------------------");
				Formatter.stampaRigaFormattata(String.valueOf(s.getCodiceSconto()), String.valueOf(s.getCodiceCarta()),
						String.valueOf(s.getDataInizio()), String.valueOf(s.getDataFine()),
						String.valueOf(s.getSconto()));
			} else {
				System.out.println("Sconto non trovato");
			}

		} catch (InputMismatchException e) {
			System.err.println("Hai inserito un valore non valido.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double calcolaMoltiplicatoreSconto(String codiceCarta, Date dataOrdine) {

		try {

			Sconti sconto = dao.recuperaUnoCodiceCarta(codiceCarta, dataOrdine);

			if (sconto != null) {
				double moltiplicatore = 1.0 - (sconto.getSconto() / 100.0);
				System.out.println("✅ Sconto trovato: " + sconto.getSconto() + "%. Moltiplicatore: " + moltiplicatore);
				return moltiplicatore;
			} else
				System.out.println("Nessuno sconto applicabile in questa data ordine.");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return 1.0;
	}

	public void inserisciSconto() {
		Connection conn = null;

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataInizioDate = null;
		Date dataFineDate = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio transazione

			CartaFedeltaService cfs = new CartaFedeltaService();
			cfs.visualizzaCarte();

			System.out.println("Dimmi codice carta nuovo sconto");
			String codiceCarta = scanner.nextLine();

			CartaFedelta c = cartaDao.recuperaUno(codiceCarta);

			if (c != null) {
				System.out.println("Dimmi la data inizio dello sconto nel formato gg/mm/aaaa");
				String dataInizio = scanner.nextLine();
				dataInizioDate = dateFormat.parse(dataInizio);

				System.out.println("Dimmi la data fine dello sconto nel formato gg/mm/aaaa");
				String dataFine = scanner.nextLine();
				dataFineDate = dateFormat.parse(dataFine);

				System.out.println("Dimmi valore nuovo sconto");
				int valoreSconto = scanner.nextInt();
				scanner.nextLine();

				Sconti s = new Sconti(codiceCarta, dataInizioDate, dataFineDate, valoreSconto);

				// Passaggio della connessione al DAO
				boolean aggiunto = dao.aggiungi(s, conn);

				if (aggiunto) {
					conn.commit();
					System.out.println("✅ Sconto aggiunto con successo.");
				} else {
					conn.rollback();
					System.out.println("❌ Sconto non aggiunto. Errore DB.");
				}
			} else {
				System.out.println(
						"Non puoi inserire uno sconto non referenziato alla tabella carta_fedelta, fornisci un codice carta esistente.");
			}

		} catch (InputMismatchException e) {
			System.out.println("Hai inserito un valore non consentito (es. testo dove è richiesto un numero).");
		} catch (ParseException e) {
			System.out.println("Hai inserito un formato non valido per la data (usa gg/mm/aaaa).");
		} catch (Exception e) { // Cattura generica per SQLException e altri
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione.");
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

	public void modificaSconto() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataInizioDate = null;
		Date dataFineDate = null;
		Connection conn = null; // Dichiarazione all'inizio del blocco try
		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio transazione

			System.out.println("Dimmi codice sconto da modificare:");
			int codiceSconto = scanner.nextInt();
			scanner.nextLine(); // Consuma il newline

			// Recupero l'oggetto da modificare
			Sconti s = dao.recuperaUno(codiceSconto);

			if (s != null) {

				// --- GESTIONE CODICE CARTA (String) ---
				String codiceCartaEsistente = s.getCodiceCarta();
				System.out.println("Dimmi nuovo codice carta (attuale: " + codiceCartaEsistente
						+ ", lascia vuoto per non modificare):");
				String codiceCartaNew = scanner.nextLine().trim();
				if (!codiceCartaNew.isEmpty()) {
					s.setCodiceCarta(codiceCartaNew);
				}
				// NOTA: Se input vuoto, s.getCodiceCarta() mantiene l'originale.

				// --- GESTIONE DATA INIZIO (Date) ---
				dataInizioDate = s.getDataInizio(); // Inizializza con la data esistente
				String dataInizioStringa = dateFormat.format(s.getDataInizio());
				System.out.println("Dimmi nuova data inizio nel formato gg/mm/aaaa (attuale: " + dataInizioStringa
						+ ", lascia vuoto per non modificare):");
				String dataInizio = scanner.nextLine().trim();

				if (!dataInizio.isEmpty()) {
					try {
						dataInizioDate = dateFormat.parse(dataInizio);
						s.setDataInizio(dataInizioDate);
					} catch (ParseException e) {
						System.err.println("❌ Data inizio non valida, Modifica annullata.");
						conn.rollback();
						return;
					}
				}

				// --- GESTIONE DATA FINE (Date) ---
				dataFineDate = s.getDataFine(); // Inizializza con la data esistente
				String dataFineStringa = s.getDataFine() != null ? dateFormat.format(s.getDataFine()) : "N/D";
				System.out.println("Dimmi nuova data fine nel formato gg/mm/aaaa (attuale: " + dataFineStringa
						+ ", lascia vuoto per non modificare):");
				String dataFine = scanner.nextLine().trim();

				if (!dataFine.isEmpty()) {
					try {
						dataFineDate = dateFormat.parse(dataFine);
						s.setDataFine(dataFineDate);
					} catch (ParseException e) {
						System.err.println("❌ Data fine non valida, Modifica annullata.");
						conn.rollback();
						return;
					}
				}

				// --- GESTIONE VALORE SCONTO (int) ---
				int valoreScontoEsistente = s.getSconto();
				System.out.println("Dimmi nuovo valore sconto (attuale: " + valoreScontoEsistente
						+ "%, lascia vuoto per non modificare):");
				String inputValoreSconto = scanner.nextLine().trim();

				if (!inputValoreSconto.isEmpty()) {
					try {
						int valoreSconto = Integer.parseInt(inputValoreSconto);
						s.setSconto(valoreSconto);
					} catch (NumberFormatException e) {
						System.err.println(
								"❌ Valore sconto non valido (deve essere un numero intero). Modifica annullata.");
						conn.rollback();
						return;
					}
				}

				// Passaggio della connessione al DAO
				boolean modificato = dao.modifica(s, conn);

				if (modificato) {
					conn.commit();
					System.out.println("✅ Sconto modificato con successo.");
				} else {
					conn.rollback();
					System.err.println("❌ Sconto non modificato. Errore DB.");
				}

			} else {
				System.out.println("Non esiste nessuno sconto con il codice fornito.");
			}
		} catch (java.util.InputMismatchException e) {
			System.err.println("❌ Hai inserito un valore non consentito (es. testo dove è richiesto un numero).");
			// Non è necessario un blocco try-catch per ParseException qui, dato che l'ho
			// spostato inline
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione: " + e.getMessage());
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback();
				} catch (java.sql.SQLException se) {
					se.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (java.sql.SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

	public void cancellaSconto() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi codice sconto da cancellare");
			int codiceSconto = scanner.nextInt();
			scanner.nextLine();

			Sconti s = dao.recuperaUno(codiceSconto);

			if (s != null) {

				boolean cancellato = dao.cancella(codiceSconto, conn);

				if (cancellato) {
					conn.commit();
					System.out.println("✅ Sconto cancellato con successo.");
				} else {
					conn.rollback();
					System.out.println("❌ Errore durante la cancellazione dello sconto.");
				}
			} else {
				System.out.println("Non esiste nessuno sconto con il codice fornito.");
			}

		} catch (InputMismatchException e) {
			System.out.println("Hai inserito un valore non consentito.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione.");
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

}