package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import dao.CartaFedeltaDAO;
import dao.ClienteDAO;
import dao.ConnessioneDB;
import entities.CartaFedelta;
import entities.Cliente;

public class CartaFedeltaService {

	private Scanner scanner;
	private CartaFedeltaDAO dao;
	private ClienteDAO clienteDao;

	public CartaFedeltaService() throws SQLException {
		this.dao = new CartaFedeltaDAO();
		this.clienteDao = new ClienteDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaCarte() {
		List<CartaFedelta> listaCarteFedelta = dao.recuperaTutti();

		if (listaCarteFedelta == null) {
			System.err.println("Errore in fase di elaborazione.");
		} else if (listaCarteFedelta.isEmpty()) {
			System.out.println("Non ci sono carte fedeltà.");
		} else {
			Formatter.stampaRigaFormattata("CODICE CARTA", "CODICE CLIENTE", "PUNTI");
			System.out.println("------------------------------------------------------------");

			for (CartaFedelta carta : listaCarteFedelta) {
				Formatter.stampaRigaFormattata(String.valueOf(carta.getCodiceCarta()),
						String.valueOf(carta.getCodiceCliente()), String.valueOf(carta.getPunti()));
			}
		}
	}

	public void visualizzaCarta() {

		try {
			System.out.println("Dimmi il codice della carta da visualizzare:");
			String codiceCarta = scanner.nextLine();

			CartaFedelta carta = dao.recuperaUno(codiceCarta);

			if (carta != null) {
				Formatter.stampaRigaFormattata("CODICE CARTA", "CODICE CLIENTE", "PUNTI");
				System.out.println(
						"---------------------------------------------------------------------------------------");
				Formatter.stampaRigaFormattata(String.valueOf(carta.getCodiceCarta()),
						String.valueOf(carta.getCodiceCliente()), String.valueOf(carta.getPunti()));
			} else
				System.out.println("La carta fedeltà con codice (" + codiceCarta + ") non esiste.");

		} catch (Exception e) {
			e.printStackTrace(); // verificare errori
		}
	}

	public void inserisciCarta() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio transazione

			System.out.println("Dimmi codice carta nuova carta");
			String codiceCarta = scanner.nextLine();

			// Controllo esistenza
			CartaFedelta cartaEsistente = dao.recuperaUno(codiceCarta);

			if (cartaEsistente == null) {
				ClienteService cs = new ClienteService();
				cs.visualizzaClienti();
				System.out.println("\n Dimmi codice cliente nuova carta");
				int codiceCliente = scanner.nextInt();
				scanner.nextLine();

				// Controllo referenza
				Cliente c = clienteDao.recuperaUno(codiceCliente);

				if (c != null) {
					System.out.println("Dimmi punti nuova carta");
					int punti = scanner.nextInt();
					scanner.nextLine();

					CartaFedelta cartaNew = new CartaFedelta(codiceCarta, codiceCliente, punti);

					// Passaggio della connessione al DAO
					boolean aggiunto = dao.aggiungi(cartaNew, conn);

					if (aggiunto) {
						conn.commit();
						System.out.println("✅ Carta aggiunta con successo.");
					} else {
						conn.rollback();
						System.err.println("❌ Carta non aggiunt, errore DB.");
					}
				} else {
					System.err.println(
							"Codice cliente fornito per la carta non esistente nella tabella Cliente, inserirne uno valido.");
				}
			} else {
				System.err.println("Stai cercando di inserire una carta con un codice già esistente in DB");
			}

		} catch (Exception e) {
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

	public void modificaCarta() {
		Connection conn = null;
		try {
			System.out.println("Dimmi il codice della carta da modificare:");
			String codiceCartaOriginale = scanner.nextLine();

			CartaFedelta cartaDaModificare = dao.recuperaUno(codiceCartaOriginale);

			if (cartaDaModificare != null) {

				System.out.println("\n--- Dati Carta Corrente ---");

				// --- GESTIONE NUOVO CODICE CARTA (Stringa) ---
				System.out.println(
						"Dimmi nuovo codice carta (lascia vuoto per mantenere: " + codiceCartaOriginale + "):");
				String codiceCartaNew = scanner.nextLine().trim();

				// Se la stringa è vuota, codiceCartaNew userà implicitamente il
				// codiceCartaOriginale per i controlli
				if (codiceCartaNew.isEmpty()) {
					codiceCartaNew = codiceCartaOriginale;
				}

				// Controllo se il nuovo codice è uguale al vecchio OPPURE se il nuovo codice
				// non esiste già
				CartaFedelta cartaFedeltaConNuovoCodice = dao.recuperaUno(codiceCartaNew);

				if (codiceCartaOriginale.equals(codiceCartaNew) || (cartaFedeltaConNuovoCodice == null)) {

					// RECUPERO VALORI ESISTENTI
					int codiceCliente = cartaDaModificare.getCodiceCliente();
					int punti = cartaDaModificare.getPunti();

					// --- GESTIONE CODICE CLIENTE (int) ---
					System.out.println("Dimmi nuovo codice cliente (attuale: " + codiceCliente
							+ ", lascia vuoto per non modificare):");
					String inputCodiceCliente = scanner.nextLine().trim();

					if (!inputCodiceCliente.isEmpty()) {
						try {
							// Aggiorna la variabile 'codiceCliente' solo se l'input non è vuoto e valido
							codiceCliente = Integer.parseInt(inputCodiceCliente);
						} catch (NumberFormatException e) {
							System.err.println(
									"❌ Codice cliente non valido (deve essere un numero). Modifica annullata.");
							return;
						}
					}

					// IMPLENTARE LOGICA PER CONTROLLO ESISTENZA CLIENTE (come nel codice originale)
					Cliente cliente = clienteDao.recuperaUno(codiceCliente);

					if (cliente != null) {

						// --- GESTIONE PUNTI (int) ---
						System.out.println(
								"Dimmi nuovi punti (attuali: " + punti + ", lascia vuoto per non modificare):");
						String inputPunti = scanner.nextLine().trim();

						if (!inputPunti.isEmpty()) {
							try {
								// Aggiorna la variabile 'punti' solo se l'input non è vuoto e valido
								punti = Integer.parseInt(inputPunti);
							} catch (NumberFormatException e) {
								System.err.println("❌ Punti non validi (devono essere un numero). Modifica annullata.");
								return;
							}
						}

						// Inizio della transazione
						conn = ConnessioneDB.getConnessione();
						conn.setAutoCommit(false);

						// Aggiornamento dell'oggetto con i valori finali (che sono stati aggiornati o
						// mantenuti)
						cartaDaModificare.setCodiceCarta(codiceCartaNew); // Usa il nuovo codice (o l'originale se input
																			// vuoto)
						cartaDaModificare.setCodiceCliente(codiceCliente); // Usa il nuovo codice (o l'originale)
						cartaDaModificare.setPunti(punti); // Usa i nuovi punti (o gli originali)

						boolean modificato = dao.modifica(cartaDaModificare, conn);

						if (modificato) {
							conn.commit();
							System.out.println("✅ Carta modificata con successo.");
						} else {
							conn.rollback();
							System.err.println("❌ Carta non modificata. Errore DB.");
						}
					} else {
						System.err.println(
								"Il codice cliente fornito non corrisponde a nessun cliente. Inserirne uno esistente.");
					}

				} else {
					System.err.println(
							"Impossibile modificare: il nuovo codice carta (" + codiceCartaNew + ") è già esistente.");
				}
			} else {
				System.err.println(
						"Il codice carta fornito non corrisponde a nessuna carta, scegliere codice di una carta esistente.");
			}
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante la modifica: " + e.getMessage());
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

	public void cancellaCarta() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio transazione

			System.out.println("Dimmi codice carta da cancellare");
			String codiceCarta = scanner.nextLine();

			CartaFedelta carta = dao.recuperaUno(codiceCarta);

			if (carta != null) {

				boolean cancellato = dao.cancella(codiceCarta, conn);
				if (cancellato) {
					conn.commit();
					System.out.println("✅ Carta cancellata con successo.");
				} else {
					conn.rollback();
					System.err.println("❌ Errore durante la cancellazione.");
				}

			} else {
				System.err.println("Non esiste nessuna carta con il codice fornito.");
			}

		} catch (Exception e) {
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