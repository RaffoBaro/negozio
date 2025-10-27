package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import dao.ConnessioneDB;
import dao.ProdottoDAO;
import dao.RepartoDAO;
import entities.Prodotto;
import entities.Reparto;

public class ProdottoService {

	private Scanner scanner;
	private ConfigurazioneService configurazioneService = new ConfigurazioneService();
	private ProdottoDAO dao;
	private RepartoDAO repartoDao;

	public ProdottoService() throws SQLException {
		this.dao = new ProdottoDAO();
		this.repartoDao = new RepartoDAO();
		this.scanner = new Scanner(System.in);
	}

	public void verificaScorta(String nomeProdotto, int nuovaGiacenza) {

		Map<String, String> emailParams = configurazioneService.recuperaParametriEmail();

		int sogliaMinima = configurazioneService.recuperaSogliaMinima();

		if (nuovaGiacenza <= sogliaMinima) {

			EmailService.inviaMessaggioAlert(emailParams.get("SMTP_HOST"), emailParams.get("SMTP_PORT"),
					emailParams.get("EMAIL_SENDER_USERNAME"), emailParams.get("EMAIL_SENDER_PASSWORD"),
					emailParams.get("EMAIL_ADMIN_RECIPIENT"), nomeProdotto, nuovaGiacenza, sogliaMinima);
			System.out.println("Alert inviato per " + nomeProdotto);
		} else {
			System.out.println("Stock ok per " + nomeProdotto);
		}
	}

	public void visualizzaProdotti() {

		List<Prodotto> listaProdotti = dao.recuperaTutti();

		if (listaProdotti == null) {
			System.out.println("ERRORE in fase di elaborazione");
		} else if (listaProdotti.isEmpty()) {
			System.out.println("Non ci sono prodotti");
		} else {
			Formatter.stampaRigaFormattata("CODICE PRODOTTO", "DESCRIZIONE", "CODICE REPARTO", "QUANTITA'");
			System.out.println("--------------------------------------------------------------------------------");

			for (Prodotto p : listaProdotti) {
				Formatter.stampaRigaFormattata(String.valueOf(p.getCodiceProdotto()), p.getDescrizione(),
						String.valueOf(p.getCodiceReparto()), String.valueOf(p.getQuantita()));
			}
		}
	}

	public void visualizzaProdotto() {

		try {
			System.out.println("Dimmi codice prodotto da visualizzare");
			int codiceProdotto = scanner.nextInt();
			scanner.nextLine();

			Prodotto p = dao.recuperaUno(codiceProdotto);

			if (p != null) {
				Formatter.stampaRigaFormattata("CODICE PRODOTTO", "DESCRIZIONE", "CODICE REPARTO", "QUANTITA");
				System.out.println("-----------------------------------------------------------------------");
				Formatter.stampaRigaFormattata(String.valueOf(p.getCodiceProdotto()), p.getDescrizione(),
						String.valueOf(p.getCodiceReparto()), String.valueOf(p.getQuantita()));

			} else
				System.out.println("Prodotto non trovato");

		} catch (Exception e) {
			System.out.println("Hai inserito un valore non valido per la scelta del prodotto");
		}
	}

	public void inserisciProdotto() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi descrizione nuovo prodotto");
			String descrizione = scanner.nextLine();

			RepartoService rs = new RepartoService();
			rs.visualizzaReparti();
			System.out.println("\nDimmi il codice del reparto a cui è associato il prodotto");
			int codiceReparto = scanner.nextInt();

			System.out.println("Dimmi la quantita di scorte");
			int quantita = scanner.nextInt();
			scanner.nextLine();

			Prodotto p = new Prodotto(descrizione, codiceReparto, quantita);

			Reparto r = repartoDao.recuperaUno(p.getCodiceReparto());

			if (r != null) {
				boolean aggiunto = dao.aggiungi(p, conn);

				if (aggiunto) {
					conn.commit();
					System.out.println("✅ Prodotto aggiunto con successo.");
				} else {
					conn.rollback();
					System.err.println("❌ Prodotto non aggiunto. Errore DB.");
				}

			} else {
				System.err.println(
						"Non puoi inserire un prodotto non referenziato alla tabella reparto, fornisci un codice reparto esistente.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione.");
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

	public void modificaProdotto() {
		Connection conn = null;
		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi il codice del prodotto da modificare:");
			int codiceProdotto = scanner.nextInt();
			scanner.nextLine();

			Prodotto p = dao.recuperaUno(codiceProdotto);

			if (p != null) {

				System.out.println("Dimmi nuova descrizione prodotto (attuale: " + p.getDescrizione()
						+ ", lascia vuoto per non modificare):");
				String nuovaDescrizione = scanner.nextLine().trim();
				if (!nuovaDescrizione.isEmpty()) {
					p.setDescrizione(nuovaDescrizione);
				}

				System.out.println("Dimmi nuovo codice reparto a cui è associato il prodotto (attuale: "
						+ p.getCodiceReparto() + ", lascia vuoto per non modificare):");
				String inputCodiceReparto = scanner.nextLine().trim();
				if (!inputCodiceReparto.isEmpty()) {
					try {
						int nuovoCodiceReparto = Integer.parseInt(inputCodiceReparto);
						p.setCodiceReparto(nuovoCodiceReparto);
					} catch (NumberFormatException e) {
						System.err.println("❌ Codice reparto non valido. Modifica annullata.");
						conn.rollback();
						return;
					}
				}

				System.out.println("Dimmi la nuova quantità di scorte (attuale: " + p.getQuantita()
						+ ", lascia vuoto per non modificare):");
				String inputQuantita = scanner.nextLine().trim();
				if (!inputQuantita.isEmpty()) {
					try {
						int nuovaQuantita = Integer.parseInt(inputQuantita);
						p.setQuantita(nuovaQuantita);
					} catch (NumberFormatException e) {
						System.err.println("❌ Quantità non valida. Modifica annullata.");
						conn.rollback();
						return;
					}
				}

				boolean modificato = dao.modifica(p, conn);

				if (modificato) {
					conn.commit();
					System.out.println("✅ Prodotto modificato con successo.");
				} else {
					conn.rollback();
					System.err.println("❌ Prodotto non modificato. Errore DB.");
				}
			} else {
				System.err.println("Il codice inserito non corrisponde a nessun prodotto");
			}
		} catch (Exception e) {
			// Cattura errori come InputMismatchException per il codice prodotto iniziale o
			// altri problemi generici
			System.err.println("❌ Errore durante l'inserimento dei dati: " + e.getMessage());
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

	public void cancellaProdotto() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);
			System.out.println("Dimmi codice prodotto da cancellare");
			int codiceProdotto = scanner.nextInt();
			scanner.nextLine();

			Prodotto p = dao.recuperaUno(codiceProdotto);

			if (p != null) {

				if (!dao.isProdottoReferenziato(codiceProdotto)) {

					boolean cancellato = dao.cancella(codiceProdotto, conn);

					if (cancellato) {
						conn.commit();
						System.out.println("✅ Prodotto trovato e cancellato");
					} else {
						conn.rollback();
						System.out.println("❌ Errore durante la cancellazione del prodotto.");
					}
				} else {
					System.err.println(
							"Il prodotto non può essere eliminato perché è ancora referenziato nella tabella 'prezzi_prodotto'.");
				}
			} else {
				System.err.println("Non c'è nessun prodotto con il codice prodotto fornito");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione.");
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