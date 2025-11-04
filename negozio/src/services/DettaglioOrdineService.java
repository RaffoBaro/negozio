package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import dao.CartaFedeltaDAO;
import dao.ConnessioneDB;
import dao.DettaglioOrdineDAO;
import dao.OrdineDAO;
import dao.ProdottoDAO;
import entities.CartaFedelta;
import entities.DettaglioOrdine;
import entities.Ordine;
import entities.Prodotto;

public class DettaglioOrdineService {

	private Scanner scanner;
	private DettaglioOrdineDAO dao;

	public DettaglioOrdineService() {
		this.dao = new DettaglioOrdineDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaDettagliOrdine() {
		List<DettaglioOrdine> lista = dao.recuperaTutti();

		if (lista == null) {
			System.out.println("❌ ERRORE in fase di elaborazione.");
		} else if (lista.isEmpty()) {
			System.out.println("Non ci sono dettagli d'ordine registrati.");
		} else {

			Formatter.stampaRigaFormattata("CODICE ORDINE", "PROGRESSIVO", "CODICE PRODOTTO", "QTA'", "TOTALE RIGA");
			System.out.println(
					"----------------------------------------------------------------------------------------------------");
			for (DettaglioOrdine dettOrdine : lista) {
				Formatter.stampaRigaFormattata(String.valueOf(dettOrdine.getCodiceOrdine()),
						String.valueOf(dettOrdine.getProgressivo()), String.valueOf(dettOrdine.getCodiceProdotto()),
						String.valueOf(dettOrdine.getQuantitaOrdinata()),
						String.format("%.2f", dettOrdine.getTotaleRigaCalcolato()));
			}
		}
	}

	public void visualizzaSingoloDettaglioOrdine() {

		try {
			System.out.println("➡️ VISUALIZZAZIONE SINGOLO DETTAGLIO ORDINE");
			System.out.println("Dimmi codice ordine da visualizzare:");
			int codiceOrdine = scanner.nextInt();
			System.out.println("Dimmi il progressivo di riga da visualizzare:");
			int progressivo = scanner.nextInt();
			scanner.nextLine();

			DettaglioOrdine dettOrdine = dao.recuperaUno(codiceOrdine, progressivo);

			if (dettOrdine != null) {
				System.out.println("✅ Dettaglio Ordine Trovato:");
				Formatter.stampaRigaFormattata("COD. ORDINE", "PROGR.", "COD. PRODOTTO", "QTA'", "TOTALE RIGA");
				System.out.println("---------------------------------------------------------------------------------");
				Formatter.stampaRigaFormattata(String.valueOf(dettOrdine.getCodiceOrdine()),
						String.valueOf(dettOrdine.getProgressivo()), String.valueOf(dettOrdine.getCodiceProdotto()),
						String.valueOf(dettOrdine.getQuantitaOrdinata()),
						String.format("%.2f", dettOrdine.getTotaleRigaCalcolato()));
			} else {
				System.out.println("Nessun dettaglio ordine trovato con Codice Ordine: " + codiceOrdine
						+ " e Progressivo: " + progressivo);
			}

		} catch (InputMismatchException e) {
			System.out.println("❌ Errore: Inserimento non valido. I codici devono essere numeri interi.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante la visualizzazione: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void inserisciDettaglioOrdine() {
		Connection conn = null;

		OrdineDAO ordineDao;
		Ordine o = null;
		ProdottoDAO prodottoDao;
		Prodotto prodotto = null;
		ProdottoService ps = null;
		PrezziProdottoService pps = null;

		try {
			ordineDao = new OrdineDAO();
			prodottoDao = new ProdottoDAO();
			int codiceProdotto = 0;
			int quantita = 0;

			System.out.println("➡️ INSERIMENTO NUOVO DETTAGLIO ORDINE");

			OrdineService os = new OrdineService();
			os.visualizzaOrdini();

			System.out.println("\nDimmi codice ordine associato:");
			int codiceOrdine = scanner.nextInt();

			o = ordineDao.recuperaUno(codiceOrdine);

			if (o != null) {

				ps = new ProdottoService();
				ps.visualizzaProdotti();

				pps = new PrezziProdottoService();
				pps.visualizzaPrezzi();

				System.out.println("\nDimmi codice prodotto con prezzo valido alla data " + o.getDataOrdine() + ":");
				codiceProdotto = scanner.nextInt();
				scanner.nextLine();

				prodotto = prodottoDao.recuperaUno(codiceProdotto);

				if (prodotto == null) {
					System.err.println(
							"❌ Prodotto non esistente, fornire un codice prodotto esistente. Inserimento annullato.");
					return;
				}

				System.out.println("Dimmi la quantità ordinata:");
				quantita = scanner.nextInt();

				if (quantita <= 0 || quantita > prodotto.getQuantita()) {
					System.err.println("❌ Quantità ordinata non valida. La disponibilità è: " + prodotto.getQuantita()
							+ ". Inserimento annullato.");
					return;
				}

				conn = ConnessioneDB.getConnessione();
				conn.setAutoCommit(false); // inizio transazione

				int progressivo = dao.getNextProgressivo(codiceOrdine, conn);

				DettaglioOrdine newDettaglio = new DettaglioOrdine(codiceOrdine, progressivo, codiceProdotto, quantita);

				boolean aggiunto = dao.aggiungi(newDettaglio, conn);

				if (aggiunto) {
					ScontiService scontiService = new ScontiService();
					int codiceCliente = ordineDao.recuperaCodiceCliente(codiceOrdine, conn);
					CartaFedeltaDAO cartaDao = new CartaFedeltaDAO();
					CartaFedelta carta = cartaDao.recuperaCartaFedelta(codiceCliente, conn);
					double moltiplicatoreSconto = 1;

					if (carta != null) {
						moltiplicatoreSconto = scontiService.calcolaMoltiplicatoreSconto(carta.getCodiceCarta(),
								o.getDataOrdine());
					}

					Date dataOrdine = o.getDataOrdine();

					boolean totaleRigaAggiornato = dao.calcolaTotaleRiga(codiceOrdine, newDettaglio.getProgressivo(),
							codiceProdotto, dataOrdine, conn, moltiplicatoreSconto);

					boolean totaleOrdineAggiornato = ordineDao.ricalcolaTotaleOrdine(codiceOrdine, conn);

					boolean qtaProdottoAggiornato = prodottoDao.sottraiQuantita(codiceProdotto, quantita, conn);

					if (totaleOrdineAggiornato && qtaProdottoAggiornato && totaleRigaAggiornato) {

						conn.commit(); // Commit se tutte le operazioni hanno successo

						// verifico scorta su prodotto aggiornato per valutare invio alert
						Prodotto prodottoNew = prodottoDao.recuperaUno(codiceProdotto);
						ps = new ProdottoService();
						ps.verificaScorta(prodottoNew.getDescrizione(), prodottoNew.getQuantita());

						System.out.println(
								"✅ Ordine completato: Dettaglio, Totale Ordine e quantita prodotti aggiornati.");

					} else {
						conn.rollback(); // Rollback se una delle operazioni fallisce
						System.out.println("❌ Errore critico: Operazione incompleta. Rollback generale.");
					}
				} else {
					conn.rollback(); // Rollback se l'INSERT del dettaglio fallisce
					System.out.println("❌ Dettaglio ordine non aggiunto. Rollback generale.");
				}

			} else
				System.err.println("❌ Ordine non esistente. Inserimento annullato.");

		} catch (InputMismatchException e) {
			System.out.println("❌ Errore: Inserimento non valido. I valori devono essere numerici.");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					// Ripristino e chiusura della connessione
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

	public void modificaDettaglioOrdine() {
		Connection conn = null;
		try {

			System.out.println("➡️ MODIFICA DETTAGLIO ORDINE");
			System.out.println("Dimmi codice ordine del dettaglio da modificare:");
			int codiceOrdine = scanner.nextInt();

			System.out.println("Dimmi il progressivo di riga da modificare:");
			int progressivo = scanner.nextInt();
			scanner.nextLine();

			DettaglioOrdine dettaglioDaModificare = dao.recuperaUno(codiceOrdine, progressivo);

			if (dettaglioDaModificare == null) {
				System.out.println("⚠️ Nessun dettaglio ordine trovato con i codici forniti. Modifica annullata.");
				return;
			}

			Ordine o = new Ordine();
			OrdineDAO oDao = new OrdineDAO();

			o = oDao.recuperaUno(dettaglioDaModificare.getCodiceOrdine());

			if (o.getFatturato() == true) {
				System.err.println("Ordine associato al dettaglio già evaso, impossibile modificare!");
				return;
			}

			System.out.println(
					"Dimmi nuovo Codice Prodotto (attuale: " + dettaglioDaModificare.getCodiceProdotto() + "):");
			int nuovoCodiceProdotto = scanner.nextInt();

			System.out.println(
					"Dimmi nuova Quantità Ordinata (attuale: " + dettaglioDaModificare.getQuantitaOrdinata() + "):");
			int nuovaQuantita = scanner.nextInt();

			dettaglioDaModificare.setCodiceProdotto(nuovoCodiceProdotto);
			dettaglioDaModificare.setQuantitaOrdinata(nuovaQuantita);

			// Inizio transazione
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);
			boolean modificato = dao.modifica(dettaglioDaModificare, conn);
			if (modificato) {
				OrdineDAO ordineDao;
				ProdottoDAO prodottoDao;
				ordineDao = new OrdineDAO();
				prodottoDao = new ProdottoDAO();
				// Ricalcolo del totale sull'Ordine
				boolean totaleAggiornato = ordineDao.ricalcolaTotaleOrdine(codiceOrdine, conn);
				// Aggiorna la quantità disponibile sul Prodotto
				boolean qtaProdottoAggiornato = prodottoDao.sottraiQuantita(nuovoCodiceProdotto, nuovaQuantita, conn);
				if (totaleAggiornato && qtaProdottoAggiornato) {
					conn.commit();
					System.out.println("✅ Dettaglio ordine modificato e Totale Ordine aggiornato con successo.");
				} else {
					conn.rollback();
					System.out.println(
							"❌ Errore critico: Dettaglio modificato, ma Totale Ordine non aggiornato. Rollback generale.");
				}
			} else {
				conn.rollback();
				System.out.println("❌ Dettaglio ordine non modificato (errore DAO/DB).");
			}
		} catch (InputMismatchException e) {
			System.out.println("❌ Errore: Inserimento non valido. I valori devono essere numerici.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante la modifica: " + e.getMessage());
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

	public void cancellaDettaglioOrdine() {
		Connection conn = null;

		try {

			System.out.println("➡️ CANCELLAZIONE DETTAGLIO ORDINE");
			System.out.println("Dimmi codice ordine del dettaglio da cancellare:");
			int codiceOrdine = scanner.nextInt();
			System.out.println("Dimmi il progressivo di riga del dettaglio da cancellare:");
			int progressivo = scanner.nextInt();
			scanner.nextLine();

			DettaglioOrdine dettOrd = dao.recuperaUno(codiceOrdine, progressivo);

			if (dettOrd != null) {

				Ordine o = new Ordine();
				OrdineDAO oDao = new OrdineDAO();

				o = oDao.recuperaUno(dettOrd.getCodiceOrdine());

				if (o.getFatturato() == true) {
					System.err.println("Ordine associato al dettaglio già evaso, impossibile modificare!");
					return;
				}

				conn = ConnessioneDB.getConnessione();
				conn.setAutoCommit(false);

				boolean cancellato = dao.cancella(codiceOrdine, progressivo, conn);

				if (cancellato) {
					ProdottoDAO pDao = new ProdottoDAO();
					OrdineDAO ordineDao = new OrdineDAO();

					// Ripristino Quantità Prodotto
					boolean qtaProdottoAggiornata = pDao.ripristinaQuantita(dettOrd.getCodiceProdotto(),
							dettOrd.getQuantitaOrdinata(), conn);

					// Ricalcolo il totale sull'ordine
					boolean totaleOrdineAggiornato = ordineDao.ricalcolaTotaleOrdine(codiceOrdine, conn);

					if (qtaProdottoAggiornata && totaleOrdineAggiornato) {
						conn.commit();
						System.out.println("✅ Dettaglio ordine riga " + progressivo
								+ " cancellato, quantità prodotto e totale ordine aggiornati con successo.");
					} else {
						conn.rollback();
						System.out.println(
								"Errore: Dettaglio cancellato, ma totale ordine o quantità prodotto non aggiornati. Rollback generale.");
					}
				} else {
					conn.rollback();
					System.out.println(" Dettaglio ordine non cancellato (errore DAO/DB).");
				}
			} else {
				System.out.println("Nessun dettaglio ordine trovato con i codici forniti.");
			}

		} catch (Exception e) {
			System.err.println("Si è verificato un errore inaspettato durante la cancellazione.");
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