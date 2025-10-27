package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import dao.ConnessioneDB;
import dao.PrezziProdottoDAO;
import dao.ProdottoDAO;
import entities.PrezziProdotto;

public class PrezziProdottoService {

	private Scanner scanner;

	private PrezziProdottoDAO dao;
	private ProdottoDAO prodottoDAO;

	public PrezziProdottoService() throws SQLException {
		this.dao = new PrezziProdottoDAO();
		this.prodottoDAO = new ProdottoDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaPrezzi() {

		List<PrezziProdotto> listaPrezzi = dao.recuperaTutti();

		if (listaPrezzi == null) {
			System.out.println("ERRORE in fase di elaborazione");
		} else if (listaPrezzi.isEmpty()) {
			System.out.println("Non ci sono prezzi");
		} else {
			Formatter.stampaRigaFormattata("CODICE PRODOTTO", "DATA INIZIO", "DATA FINE", "PREZZO", "IVA");
			System.out.println(
					"----------------------------------------------------------------------------------------------------");
			for (PrezziProdotto prezzo : listaPrezzi) {
				Formatter.stampaRigaFormattata(String.valueOf(prezzo.getCodiceProdotto()),
						String.valueOf(prezzo.getDataInizio()), String.valueOf(prezzo.getDataFine()),
						String.valueOf(prezzo.getPrezzo()), String.valueOf(prezzo.getIva()));
			}
		}
	}

	public void visualizzaPrezzo() {
		try {
			System.out.println("Dimmi codice prodotto per visualizzare il prezzo del prodotto desiderato");
			int codiceProdotto = scanner.nextInt();
			scanner.nextLine();

			if (prodottoDAO.esisteProdotto(codiceProdotto)) {

				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				Date dataInizioDate = null;
				Date dataFineDate = null;

				System.out.println("Dimmi la data inizio del prezzo nel formato gg/mm/aaaa");
				String dataInizio = scanner.nextLine();
				dataInizioDate = dateFormat.parse(dataInizio);

				System.out.println("Dimmi la data fine del prezzo nel formato gg/mm/aaaa");
				String dataFine = scanner.nextLine();
				dataFineDate = dateFormat.parse(dataFine);

				PrezziProdotto p = dao.recuperaUno(codiceProdotto, dataInizioDate, dataFineDate); // Variabile locale

				if (p != null) {
					Formatter.stampaRigaFormattata("CODICE PRODOTTO", "DATA INIZIO", "DATA FINE", "PREZZO", "IVA");
					System.out.println("-------------------------------------------------");
					Formatter.stampaRigaFormattata(String.valueOf(p.getCodiceProdotto()), p.getDataInizio().toString(),
							String.valueOf(p.getDataFine().toString()), String.valueOf(p.getPrezzo()),
							String.valueOf(p.getIva()));
				} else
					System.out.println(
							"Non c'è nessun prezzo con codice " + codiceProdotto + " per il periodo selezionato");
			} else
				System.out.println("Non c'è nessun prezzo con questo codice prodotto.");

		} catch (Exception e) {
			System.out.println("Hai inserito un valore non valido.");
		}
	}

	public void inserisciPrezzo() {
		Connection conn = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataInizioDate = null;
		Date dataFineDate = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			ProdottoService ps = new ProdottoService();
			ps.visualizzaProdotti();
			System.out.println("\nDimmi il codice prodotto del prezzo:");
			int codiceProdotto = Integer.parseInt(scanner.nextLine());

			if (!prodottoDAO.esisteProdotto(codiceProdotto)) {
				System.err.println("❌ Errore: Il Codice Prodotto '" + codiceProdotto
						+ "' non è esistente nella tabella Prodotto.");
				return;
			}

			System.out.println("Dimmi la data inizio del nuovo prezzo nel formato gg/mm/aaaa:");
			String dataInizio = scanner.nextLine();
			dataInizioDate = dateFormat.parse(dataInizio);

			System.out.println("Dimmi la data fine del nuovo prezzo nel formato gg/mm/aaaa:");
			String dataFine = scanner.nextLine();
			dataFineDate = dateFormat.parse(dataFine);

			System.out.println("Dimmi il prezzo:");
			double qtaPrezzo = Double.parseDouble(scanner.nextLine());

			System.out.println("Dimmi l'IVA:");
			double iva = Double.parseDouble(scanner.nextLine());

			PrezziProdotto prezzoNew = new PrezziProdotto(codiceProdotto, dataInizioDate, dataFineDate, qtaPrezzo, iva);

			boolean aggiunto = dao.aggiungi(prezzoNew, conn);

			if (aggiunto) {
				conn.commit(); // Commit in caso di successo
				System.out.println("✅ Prezzo aggiunto con successo.");
			} else {
				conn.rollback(); // Rollback in caso di fallimento DAO
				System.err.println(
						"❌ Prezzo non aggiunto. Possibili cause: sovrapposizione di validità, o errore del database.");
			}

		} catch (NumberFormatException e) {
			System.err.println("⚠️ Hai inserito un valore non consentito (es. testo per un numero).");
		} catch (ParseException e) {
			System.err.println("⚠️ Formato data non valido. Inserisci gg/mm/aaaa.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione.");
			e.printStackTrace();
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

	public void modificaPrezzo() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataFineDate = null;
		Date dataInizioDate = null;
		Connection conn = null;
		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi il codice prodotto del prezzo da modificare:");
			String inputCodiceProdotto = scanner.nextLine().trim();
			int codiceProdotto = Integer.parseInt(inputCodiceProdotto);

			if (!prodottoDAO.esisteProdotto(codiceProdotto)) {
				System.err.println(
						"❌ Errore: Il Codice Prodotto " + codiceProdotto + " non è esistente nella tabella Prodotto.");
				return;
			}

			System.out.println("Dimmi data inizio prezzo da modificare (gg/mm/aaaa):");
			String dataInizioStr = scanner.nextLine().trim();
			dataInizioDate = dateFormat.parse(dataInizioStr);

			System.out.println("Dimmi data fine prezzo da modificare (gg/mm/aaaa) [Se N/D, lascia vuoto]:");
			String dataFineKeyStr = scanner.nextLine().trim();
			if (!dataFineKeyStr.isEmpty()) {
				dataFineDate = dateFormat.parse(dataFineKeyStr);
			} else {
				dataFineDate = null;
			}

			PrezziProdotto prezzoEsistente = dao.recuperaUno(codiceProdotto, dataInizioDate, dataFineDate);

			if (prezzoEsistente == null) {
				System.err.println("❌ Errore: Nessun prezzo trovato con i codici chiave forniti.");
				return;
			}

			double nuovoPrezzo = prezzoEsistente.getPrezzo();
			double nuovaIva = prezzoEsistente.getIva();

			System.out
					.println("Dimmi il NUOVO prezzo (attuale: " + nuovoPrezzo + ", lascia vuoto per non modificare):");
			String inputNuovoPrezzo = scanner.nextLine().trim();

			if (!inputNuovoPrezzo.isEmpty()) {
				try {
					nuovoPrezzo = Double.parseDouble(inputNuovoPrezzo);
				} catch (NumberFormatException e) {
					System.err.println("❌ Errore: Inserisci un numero valido per prezzo. Modifica annullata.");
					conn.rollback();
					return;
				}
			}

			System.out.println("Dimmi la NUOVA IVA (attuale: " + nuovaIva + ", lascia vuoto per non modificare):");
			String inputNuovaIva = scanner.nextLine().trim();

			if (!inputNuovaIva.isEmpty()) {
				try {
					nuovaIva = Double.parseDouble(inputNuovaIva);
				} catch (NumberFormatException e) {
					System.err.println("❌ Errore: Inserisci un numero valido per l'IVA. Modifica annullata.");
					conn.rollback();
					return;
				}
			}

			PrezziProdotto p = new PrezziProdotto(codiceProdotto, dataInizioDate, dataFineDate, nuovoPrezzo, nuovaIva);

			boolean modificato = dao.modifica(p, conn);

			if (modificato) {
				conn.commit();
				System.out.println("✅ Prezzo modificato con successo.");
			} else {
				conn.rollback();
				System.err.println(
						"❌ Prezzo non modificato. Possibili cause: la combinazione Codice/Date non è stata trovata o errore del database.");
			}
		} catch (NumberFormatException e) {
			System.err.println("❌ Errore: Inserisci un numero valido per codice, prezzo o IVA.");
		} catch (ParseException e) {
			System.err.println("❌ Errore: Formato data non valido. Inserisci gg/mm/aaaa.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato: " + e.getMessage());
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

	public void cancellaPrezzo() {
		Connection conn = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date utilDataInizio = null;
		Date utilDataFine = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			// 1. RACCOLTA INPUT (PK COMPLETA)
			System.out.println("Dimmi il CODICE PRODOTTO del prezzo da cancellare:");
			int codiceProdotto = Integer.parseInt(scanner.nextLine());

			if (!prodottoDAO.esisteProdotto(codiceProdotto)) {
				System.err.println("❌ Errore: Il Codice Prodotto '" + codiceProdotto
						+ "' non è esistente nella tabella Prodotto.");
				return;
			}

			System.out.println("Dimmi la DATA INIZIO (gg/mm/aaaa) del prezzo da cancellare:");
			String dataInizioStr = scanner.nextLine();
			utilDataInizio = dateFormat.parse(dataInizioStr);

			System.out.println("Dimmi la DATA FINE (gg/mm/aaaa) del prezzo da cancellare:");
			String dataFineStr = scanner.nextLine();
			utilDataFine = dateFormat.parse(dataFineStr);

			boolean cancellato = dao.cancella(codiceProdotto, utilDataInizio, utilDataFine, conn);

			if (cancellato) {
				conn.commit();
				System.out.println("✅ Prezzo cancellato con successo.");
			} else {
				conn.rollback();
				System.err.println(
						"❌ Cancellazione fallita. La combinazione Codice Prodotto / Data Inizio / Data Fine non corrisponde a nessun prezzo esistente.");
			}

		} catch (NumberFormatException e) {
			System.err.println("⚠️ Errore: Inserisci un numero valido per il codice prodotto.");
		} catch (ParseException e) {
			System.err.println("⚠️ Formato data non valido. Inserisci gg/mm/aaaa.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato.");
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