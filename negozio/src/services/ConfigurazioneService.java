package services;

import dao.ConfigurazioneDAO;
import dao.ConnessioneDB;
import entities.Configurazione;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigurazioneService {

	private final ConfigurazioneDAO configDAO;
	private final Scanner scanner;
	private Map<String, String> cacheConfigurazioni;

	public ConfigurazioneService() {
		this.scanner = new Scanner(System.in);
		try {
			this.configDAO = new ConfigurazioneDAO();
			// Carica tutte le configurazioni dal DB in memoria all'avvio
			this.caricaCacheConfigurazioni();
		} catch (SQLException e) {
			// Errore critico se il DAO non si inizializza (es. DB non connesso)
			throw new RuntimeException("Errore critico durante l'inizializzazione del ConfigurazioneService.", e);
		}
	}

	/**
	 * Carico tutte le configurazioni dal DB in una Map per accesso rapido.
	 */
	private void caricaCacheConfigurazioni() {
		this.cacheConfigurazioni = configDAO.recuperaTutti();
		if (this.cacheConfigurazioni == null || this.cacheConfigurazioni.isEmpty()) {
			System.out.println("AVVISO: Nessuna configurazione trovata nel database.");
			if (this.cacheConfigurazioni == null) {
				this.cacheConfigurazioni = new HashMap<>();
			}
		}
	}

	public void visualizzaConfigurazioni() {
		// Utilizzo la Map invece di chiamare configDAO.recuperaTutti()
		Map<String, String> listaConfig = this.cacheConfigurazioni;

		if (listaConfig == null || listaConfig.isEmpty()) {
			System.out.println("Non ci sono configurazioni");
		} else {
			System.out.println("\n--- LISTA CONFIGURAZIONI ---");

			Formatter.stampaRigaFormattata("CHIAVE", "VALORE");
			System.out.println("--------------------------------------------------");

			for (Map.Entry<String, String> entry : listaConfig.entrySet()) {
				Formatter.stampaRigaFormattata(entry.getKey(), entry.getValue());
			}
			System.out.println("--------------------------------------------------");
		}
	}

	public void visualizzaConfigurazione() {
		System.out.println("Dimmi la CHIAVE della configurazione da visualizzare:");
		String chiave = scanner.nextLine().trim();

		Configurazione configurazione = configDAO.recuperaUno(chiave);

		if (configurazione != null) {
			System.out.println("\n--- DETTAGLIO CONFIGURAZIONE ---");

			Formatter.stampaRigaFormattata("CHIAVE", "VALORE");
			System.out.println("--------------------------------------------------");
			Formatter.stampaRigaFormattata(configurazione.getChiave(), configurazione.getValore());
			System.out.println("--------------------------------------------------");

		} else {
			System.err.println("⚠ Configurazione non trovata. Fornire una chiave esistente.");
		}
	}

	public void inserisciConfigurazione() {
		System.out.println("Dimmi la CHIAVE della nuova configurazione (es. SMTP_HOST):");
		String chiave = scanner.nextLine().trim();

		System.out.println("Dimmi il VALORE della nuova configurazione:");
		String valore = scanner.nextLine().trim();

		Configurazione nuovaConfig = new Configurazione(chiave, valore);

		try (Connection conn = ConnessioneDB.getConnessione()) {
			conn.setAutoCommit(false);

			if (configDAO.recuperaUno(chiave) != null) {
				System.err.println("⚠ La chiave '" + chiave + "' esiste già. Usa la funzione 'modifica'.");
				return;
			}

			boolean aggiunto = configDAO.inserisci(nuovaConfig, conn);

			if (aggiunto) {
				conn.commit();

				this.cacheConfigurazioni.put(chiave, valore);
				System.out.println("✅ Configurazione aggiunta con successo.");
			} else {
				conn.rollback();
				System.err.println("⚠ Configurazione non aggiunta.");
			}
		} catch (SQLException e) {
			System.err.println("❌ Errore DB durante l'inserimento: " + e.getMessage());

		}
	}

	public void modificaConfigurazione() {
		System.out.println("Dimmi la CHIAVE della configurazione da modificare:");
		String chiave = scanner.nextLine().trim();

		try (Connection conn = ConnessioneDB.getConnessione()) {
			conn.setAutoCommit(false);

			Configurazione esistente = configDAO.recuperaUno(chiave);

			if (esistente != null) {
				System.out.println("Valore attuale: " + esistente.getValore());
				System.out.println("Dimmi il NUOVO VALORE:");
				String nuovoValore = scanner.nextLine().trim();

				Configurazione configModificata = new Configurazione(chiave, nuovoValore);
				boolean modificato = configDAO.modifica(configModificata, conn);

				if (modificato) {
					conn.commit();
					// Aggiorna la cache
					this.cacheConfigurazioni.put(chiave, nuovoValore);
					System.out.println("✅ Configurazione modificata con successo.");
				} else {
					conn.rollback();
					System.err.println("⚠ Configurazione non modificata.");
				}

			} else {
				System.err.println("⚠ La chiave '" + chiave + "' non esiste.");
			}
		} catch (SQLException e) {
			System.err.println("❌ Errore DB durante la modifica: " + e.getMessage());

		}
	}

	public void cancellaConfigurazione() {
		System.out.println("Dimmi la CHIAVE della configurazione da cancellare:");
		String chiave = scanner.nextLine().trim();

		Connection conn = null;

		try {
			conn = ConnessioneDB.getConnessione();
			conn.setAutoCommit(false); // Inizia la transazione

			int righeCancellate = configDAO.cancella(chiave, conn);

			if (righeCancellate > 0) {
				conn.commit();
				// Rimuovi dalla cache
				this.cacheConfigurazioni.remove(chiave);
				System.out.println("✅ Configurazione cancellata con successo.");

			} else {
				conn.rollback();
				System.err.println("⚠ Cancellazione annullata, chiave non trovata.");
			}

		} catch (SQLException e) {
			System.err.println("❌ Errore transazionale durante l'eliminazione: " + e.getMessage());

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

	// ===================================
	// METODI DI UTILIZZO PER LOGICA EMAIL
	// ===================================

	private String getValore(String chiave) {
		
		String valore = this.cacheConfigurazioni.get(chiave);

		if (valore == null) {
			Configurazione config = configDAO.recuperaUno(chiave);
			if (config != null) {
				valore = config.getValore();
				this.cacheConfigurazioni.put(chiave, valore); // Aggiunge alla cache
			}
		}
		return valore;
	}

	
	public Map<String, String> recuperaParametriEmail() {

		Map<String, String> params = new HashMap<>();

		// Recupera i parametri dalla cache
		params.put("SMTP_HOST", getValore("SMTP_HOST"));
		params.put("SMTP_PORT", getValore("SMTP_PORT"));
		params.put("EMAIL_SENDER_USERNAME", getValore("EMAIL_SENDER_USERNAME"));
		params.put("EMAIL_SENDER_PASSWORD", getValore("EMAIL_SENDER_PASSWORD"));
		params.put("EMAIL_ADMIN_RECIPIENT", getValore("EMAIL_ADMIN_RECIPIENT"));

		return params;
	}

	
	public int recuperaSogliaMinima() {
		String sogliaStr = getValore("STOCK_MIN");
		int sogliaMinima = 15; // Valore di default

		if (sogliaStr != null) {
			try {
				sogliaMinima = Integer.parseInt(sogliaStr);
			} catch (NumberFormatException e) {
				System.err.println("ERRORE: STOCK_MIN nel DB non è un numero valido. Uso il default 10.");
			}
		}
		return sogliaMinima;
	}
}