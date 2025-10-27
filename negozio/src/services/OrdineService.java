package services;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;
import dao.ClienteDAO;
import dao.ConnessioneDB;
import dao.DettaglioOrdineDAO;
import dao.OrdineDAO;
import entities.Cliente;
import entities.Ordine;

public class OrdineService {

	private Scanner scanner;

	private OrdineDAO dao;
	private ClienteDAO clienteDao;

	public OrdineService() throws SQLException {
		this.dao = new OrdineDAO();
		this.clienteDao = new ClienteDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaOrdini() {

		try {
			List<Ordine> listaOrdini = dao.recuperaTutti();

			if (listaOrdini == null) {
				System.out.println("ERRORE in fase di elaborazione");
			} else if (listaOrdini.isEmpty()) {
				System.out.println("Non ci sono ordini");
			} else {
				Formatter.stampaRigaFormattata("CODICE ORDINE", "CODICE CLIENTE", "DATA ORDINE",
						"TOTALE ORDINE CALCOLATO", "FATTURATO");
				

				for (Ordine o : listaOrdini) {
					Formatter.stampaRigaFormattata(String.valueOf(o.getCodiceOrdine()),
							String.valueOf(o.getCodiceCliente()), String.valueOf(o.getDataOrdine()),
							String.valueOf(o.getTotaleOrdineCalcolato()), String.valueOf(o.getFatturato()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void visualizzaOrdine() {

		try {
			System.out.println("Dimmi codice ordine da visualizzare");
			int codiceOrdine = scanner.nextInt();
			scanner.nextLine();

			Ordine o = dao.recuperaUno(codiceOrdine);

			if (o != null) {
				Formatter.stampaRigaFormattata("CODICE ORDINE", "CODICE CLIENTE", "DATA ORDINE",
						"TOTALE ORDINE CALCOLATO", "FATTURATO");
				System.out.println(
						"---------------------------------------------------------------------------------------");

				Formatter.stampaRigaFormattata(String.valueOf(o.getCodiceOrdine()),
						String.valueOf(o.getCodiceCliente()), String.valueOf(o.getDataOrdine()),
						String.valueOf(o.getTotaleOrdineCalcolato()), String.valueOf(o.getFatturato()));

			} else
				System.out.println("Ordine non trovato");
		} catch (Exception e) {
			System.out.println("Hai inserito un valore non valido per la scelta dell'ordine");
		}
	}

	public void inserisciOrdine() {
		Connection conn = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataOrdine = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio transazione

			ClienteService cs = new ClienteService();
			cs.visualizzaClienti();
			System.out.println("\nDimmi codice cliente nuovo ordine");
			int codiceCliente = scanner.nextInt();
			scanner.nextLine();

			Cliente c = clienteDao.recuperaUno(codiceCliente);

			if (c != null) {
				System.out.println("Dimmi data nuovo ordine");
				String dataOrdineStr = scanner.nextLine();

				try {
					dataOrdine = dateFormat.parse(dataOrdineStr);
				} catch (java.text.ParseException e) {
					System.err.println("❌ Hai inserito un valore non valido per la data");
					System.err.println("❌ Ordine non aggiunto.");

					return;
				}

				Ordine ordineNew = new Ordine(codiceCliente, dataOrdine);

				boolean aggiunto = dao.aggiungi(ordineNew, conn);

				if (aggiunto) {
					conn.commit();
					System.out.println("✅ Ordine aggiunto con successo.");
				} else {
					conn.rollback();
					System.err.println("❌ Ordine non aggiunto. Errore durante l'inserimento.");
				}
			} else {
				System.out
						.println("❗Il cliente con il codice fornito non esiste, fornire un codice cliente esistente.");
			}

		} catch (Exception e) {
			System.err.println(
					"❌ Si è verificato un errore inaspettato durante l'operazione./Fornisci un codice cliente valido.");
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
					System.out.println("Connessione chiusa.");
				} catch (java.sql.SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

	public void modificaOrdine() {
		Connection conn = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataOrdineDate = null;

		try {

			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio della transazione

			System.out.println("Dimmi il codice dell'ordine da modificare");
			int codiceOrdine = scanner.nextInt();
			scanner.nextLine();

			Ordine o = dao.recuperaUno(codiceOrdine);

			if (o != null) {
				System.out.println("Dimmi nuovo codice cliente dell'ordine");
				int codiceCliente = scanner.nextInt();
				scanner.nextLine();

				System.out.println("Dimmi nuova data ordine");
				String dataOrdineStr = scanner.nextLine();

				o.setCodiceCliente(codiceCliente);
				dataOrdineDate = dateFormat.parse(dataOrdineStr);
				o.setDataOrdine(dataOrdineDate);

				boolean modificato = dao.modifica(o, conn);

				if (modificato) {
					conn.commit();
					System.out.println("Ordine modificato");
				} else {
					conn.rollback();
					System.out.println("Ordine non modificato");
				}
			} else {
				System.out.println("Il codice inserito non corrisponde a nessun ordine");
			}

		} catch (java.text.ParseException pe) {
			System.out.println("❌ Errore di formato data. Operazione annullata.");
		} catch (Exception e) {
			System.err.println("❌ Si è verificato un errore inaspettato durante l'operazione.");
			e.printStackTrace();

			System.out.println("Hai inserito un valore non valido per la scelta dell'ordine");

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
					System.out.println("Connessione chiusa.");
				} catch (java.sql.SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

	public void cancellaOrdine() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizio transazione

			System.out.println("Dimmi codice ordine da cancellare");
			int codiceOrdine = scanner.nextInt();
			scanner.nextLine();

			Ordine o = dao.recuperaUno(codiceOrdine);
			boolean dettOrdCancellato = false;
			if (o != null) {

				DettaglioOrdineDAO dettOrdDao = new DettaglioOrdineDAO();

				if (dao.isOrdineReferenziato(codiceOrdine)) {
					dettOrdCancellato = dettOrdDao.cancellaReferenziatiOrdine(codiceOrdine, conn);

				}

				boolean cancellato = dao.cancella(codiceOrdine, conn);

				if (cancellato && dettOrdCancellato) {
					conn.commit();
					System.out.println("✅ Ordine e dettagli cancellati con successo.");
				} else {
					conn.rollback();
					System.out.println(
							"❌ Errore durante la cancellazione dell'ordine o dei dettagli ordine associati. Rollback generale.");
				}
			} else {
				System.out.println("Non esiste nessun ordine con il codice fornito.");
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