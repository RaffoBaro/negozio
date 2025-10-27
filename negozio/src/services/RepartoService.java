package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import dao.ConnessioneDB;
import dao.RepartoDAO;
import entities.Reparto;

public class RepartoService {

	private Scanner scanner;
	private RepartoDAO dao;

	public RepartoService() throws SQLException {
		this.dao = new RepartoDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaReparti() {

		List<Reparto> listaReparti = dao.recuperaTutti();

		if (listaReparti == null) {
			System.out.println("ERRORE in fase di elaborazione");
		} else if (listaReparti.isEmpty()) {
			System.out.println("Non ci sono reparti");
		} else {
			Formatter.stampaRigaFormattata("CODICE REPARTO", "DESCRIZIONE");
			System.out.println("----------------------------------------");

			for (Reparto r : listaReparti) {
				Formatter.stampaRigaFormattata(String.valueOf(r.getCodiceReparto()),
						String.valueOf(r.getDescrizione()));
			}
		}
	}

	public void visualizzaReparto() {
		try {
			System.out.println("Dimmi codice reparto da visualizzare");
			int codiceReparto = scanner.nextInt();
			scanner.nextLine();

			Reparto r = dao.recuperaUno(codiceReparto);

			if (r != null) {
				Formatter.stampaRigaFormattata("CODICE REPARTO", "DESCRIZIONE");
				System.out.println("-------------------------------------------------");
				Formatter.stampaRigaFormattata(String.valueOf(r.getCodiceReparto()), r.getDescrizione());
			} else
				System.out.println("Reparto non trovato");

		} catch (Exception e) {
			System.err.println("Hai inserito un valore non valido per la scelta del reparto");
		}

	}

	public void inserisciReparto() {
		Connection conn = null;
		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi descrizione nuovo reparto");
			String descrizione = scanner.nextLine();

			Reparto r = new Reparto(descrizione);

			boolean aggiunto = dao.aggiungi(r, conn);

			if (aggiunto) {
				conn.commit();
				System.out.println("✅ Reparto aggiunto con successo.");
			} else {
				conn.rollback();
				System.err.println("❌ Reparto non aggiunto. Errore DB.");
			}

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

	public void modificaReparto() {
		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi codice reparto da modificare");
			int codiceReparto = scanner.nextInt();
			scanner.nextLine();

			Reparto r = dao.recuperaUno(codiceReparto);

			if (r != null) {
				System.out.println("Dimmi nuova descrizione reparto");
				String descrizione = scanner.nextLine();

				r.setDescrizione(descrizione);

				boolean modificato = dao.modifica(r, conn);

				if (modificato) {
					conn.commit();
					System.out.println("✅ Reparto modificato con successo.");
				} else {
					conn.rollback();
					System.err.println("❌ Reparto non modificato. Errore DB.");
				}
			} else {
				System.out.println("Il codice inserito non corrisponde a nessun reparto");
			}

		} catch (Exception e) {
			System.err.println("Hai inserito un valore non valido per la scelta del reparto");
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

	public void cancellaReparto() {
		Connection conn = null;
		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false);

			System.out.println("Dimmi codice reparto da cancellare");
			int codiceReparto = scanner.nextInt();
			scanner.nextLine();

			Reparto r = dao.recuperaUno(codiceReparto);

			if (r != null) {

				if (!dao.isRepartoReferenziato(codiceReparto)) {

					boolean cancellato = dao.cancella(codiceReparto, conn);

					if (cancellato) {
						conn.commit();
						System.out.println("✅ Reparto cancellato con successo.");
					} else {
						conn.rollback();
						System.err.println("❌ Errore durante la cancellazione del reparto.");
					}
				} else {
					System.err.println(
							"Il reparto non può essere eliminato perché è referenziato da uno o più prodotti.");
				}
			} else {
				System.out.println("Il reparto non esiste");
			}

		} catch (java.util.InputMismatchException e) {
			System.err.println("Hai inserito un valore non consentito");
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