package services;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import dao.ClienteDAO;
import dao.ConnessioneDB;

import entities.Cliente;

public class ClienteService {

	private Scanner scanner;
	ClienteDAO dao;
	Cliente cliente;

	public ClienteService() throws SQLException {
		this.dao = new ClienteDAO();
		this.scanner = new Scanner(System.in);
	}

	public void visualizzaClienti() {

		List<Cliente> listaClienti = dao.recuperaTutti();

		if (listaClienti == null) {
			System.out.println("ERRORE in fase di elaborazione");
		} else if (listaClienti.isEmpty()) {
			System.out.println("Non ci sono prodotti");
		} else {
			Formatter.stampaRigaFormattata("CODICE CLIENTE", "COGNOME", "NOME", "DATA NASCITA", "EMAIL");
			System.out.println("--------------------------------------------------------------------------------");
			for (Cliente c : listaClienti) {
				Formatter.stampaRigaFormattata(String.valueOf(c.getCodiceCliente()), String.valueOf(c.getCognome()),
						String.valueOf(c.getNome()), String.valueOf(c.getDataNascita()), String.valueOf(c.getEmail()));

			}

		}

	}

	public void visualizzaCliente() {

		try {
			System.out.println("Dimmi codice cliente da visualizzare");
			int codiceCliente = scanner.nextInt();
			scanner.nextLine();

			Cliente cliente = dao.recuperaUno(codiceCliente);

			if (cliente != null) {
				Formatter.stampaRigaFormattata("CODICE CLIENTE", "COGNOME", "NOME", "DATA NASCITA", "EMAIL");
				System.out.println(
						"---------------------------------------------------------------------------------------");
				Formatter.stampaRigaFormattata(String.valueOf(cliente.getCodiceCliente()),
						String.valueOf(cliente.getCognome()), String.valueOf(cliente.getNome()),
						String.valueOf(cliente.getDataNascita()), String.valueOf(cliente.getEmail()));
			} else {
				System.err.println("Cliente non trovato, fornire un codice cliente esistente.");
			}
		} catch (InputMismatchException e) {
			System.err.println("Hai inserito un valore non valido per la scelta del cliente");
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void inserisciCliente() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataNascitaDate = null;

		try {
			System.out.println("Dimmi cognome nuovo cliente:");
			String cognome = scanner.nextLine();

			System.out.println("Dimmi nome nuovo cliente:");
			String nome = scanner.nextLine();

			System.out.println("Dimmi data nascita nuovo cliente:");
			String dataNascita = scanner.nextLine();

			try {
				dataNascitaDate = dateFormat.parse(dataNascita);
			} catch (ParseException e) {
				System.err.println("Cliente non aggiunto, data non valida. Fornire data nel formato gg/mm//aaaa.");
				return;
			}

			System.out.println("Dimmi email nuovo cliente:");
			String email = scanner.nextLine();

			Cliente clienteNew = new Cliente(cognome, nome, dataNascitaDate, email);

			try (Connection conn = ConnessioneDB.getConnessione()) {
				conn.setAutoCommit(false); // inizio transazione

				boolean aggiunto = dao.inserisci(clienteNew, conn);

				if (aggiunto) {
					conn.commit();
					System.out.println("✅ Cliente aggiunto con successo.");
				} else {
					conn.rollback();
					System.err.println("⚠ Cliente non aggiunto.");
				}

			} catch (SQLException e) {
				System.err.println("❌ Errore DB: " + e.getMessage());
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void modificaCliente() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date dataNascitaDate = null;

		try (Connection conn = ConnessioneDB.getConnessione()) {
			conn.setAutoCommit(false);

			System.out.println("Dimmi il codice cliente da modificare:");
			int codiceCliente = scanner.nextInt();
			scanner.nextLine();

			Cliente esistente = dao.recuperaUno(codiceCliente);
			if (esistente != null) {
				// === COGNOME ===
				System.out.println("Dimmi nuovo cognome cliente (lascia vuoto per non modificare):");
				String nuovoCognome = scanner.nextLine();
				// Se la stringa NON è vuota, aggiorna la variabile 'cognome', altrimenti usa
				// l'esistente
				String cognome = nuovoCognome.isEmpty() ? esistente.getCognome() : nuovoCognome;

				// === NOME ===
				System.out.println("Dimmi nuovo nome cliente (lascia vuoto per non modificare):");
				String nuovoNome = scanner.nextLine();
				// Se la stringa NON è vuota, aggiorna la variabile 'nome', altrimenti usa
				// l'esistente
				String nome = nuovoNome.isEmpty() ? esistente.getNome() : nuovoNome;

				// === DATA DI NASCITA ===
				System.out.println("Dimmi nuova data nascita cliente (dd/MM/yyyy) (lascia vuoto per non modificare):");
				String dataNascitaString = scanner.nextLine();

				dataNascitaDate = esistente.getDataNascita(); // Inizializza con la data esistente

				// Controlla se l'input non è vuoto, solo in tal caso prova a parsare
				if (!dataNascitaString.isEmpty()) {
					try {
						dataNascitaDate = dateFormat.parse(dataNascitaString);
					} catch (ParseException e) {
						System.err.println("Data non valida, inserimento annullato.");
						return; // Esci dalla funzione se il formato è errato
					}
				}

				System.out.println("Dimmi email nuovo cliente:");
				String email = scanner.nextLine();

				Cliente clienteModificato = new Cliente(codiceCliente, cognome, nome, dataNascitaDate, email);
				boolean modificato = dao.modifica(clienteModificato, conn);

				if (modificato) {
					conn.commit();
					System.out.println("✅ Cliente modificato con successo.");
				} else {
					conn.rollback();
					System.err.println("⚠ Cliente non modificato. Rollback generale.");
				}

			} else
				System.err
						.println("⚠ il cliente con il codice fornito non esiste, fornire un codice cliente esistente.");

		} catch (InputMismatchException e) {
			System.err.println("Hai inserito un valore non valido.");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void cancellaCliente() {

		System.out.println("Dimmi codice cliente da cancellare");

		int codiceCliente = scanner.nextInt();
		scanner.nextLine();

		try {

			if (dao.clienteHaRiferimenti(codiceCliente)) {
				System.err.println("Cancellazione annullata, il cliente ha riferimenti e non può essere eliminato.");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Errore di verifica DB: " + e.getMessage());

		}

		Connection conn = null;

		try {

			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizia la transazione

			int righeCancellate = dao.cancella(codiceCliente, conn);

			if (righeCancellate > 0) {
				conn.commit();
				System.out.println("Cliente cancellato con successo.");

			} else {
				conn.rollback();
				System.err.println("Cancellazione annullata, cliente non trovato.");
			}

		} catch (SQLException e) {
			System.err.println("Errore transazionale durante l'eliminazione: " + e.getMessage());

			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException ex) {
				System.err.println("Errore durante il rollback: " + ex.getMessage());
			}
		} finally {

			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException e) {
					System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
				}
			}
		}
	}

}
