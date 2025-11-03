package app;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dao.ConnessioneDB;
import dao.OrdineDAO;
import dao.ProdottoDAO;
import dao.ClienteDAO;
import dao.FatturaDAO;
import dao.DettaglioOrdineDAO;
import dao.ConfigurazioneDAO;
import services.FatturaService;
import services.EmailService;
import entities.Ordine;
import entities.Prodotto;
import entities.Cliente;
import entities.Fattura;
import entities.DettaglioOrdine;

public class FatturazioneBatch {

	public static void main(String[] args) {
		System.out.println("🚀 AVVIO PROGRAMMA BATCH DI FATTURAZIONE E INVIO MAIL");

		try {
			OrdineDAO ordineDao = new OrdineDAO();
			ClienteDAO clienteDao = new ClienteDAO();
			FatturaDAO fatturaDao = new FatturaDAO();
			FatturaService fatturaService = new FatturaService();
			DettaglioOrdineDAO dettaglioOrdineDao = new DettaglioOrdineDAO();
			ConfigurazioneDAO configurazioneDao = new ConfigurazioneDAO();

			// 1. RECUPERO I PARAMETRI SMTP DAL DB
			Connection configConn = ConnessioneDB.getConnessione();
			Map<String, String> smtpConfig = configurazioneDao.recuperaConfigurazioniEmail(configConn);
			configConn.close();

			// Definisco i parametri
			final String SMTP_HOST = smtpConfig.get("SMTP_HOST");
			final String SMTP_PORT = smtpConfig.get("SMTP_PORT");
			final String SMTP_USER = smtpConfig.get("EMAIL_SENDER_USERNAME");
			final String SMTP_PASS = smtpConfig.get("EMAIL_SENDER_PASSWORD");

			System.out.println(smtpConfig);
			
			System.out.println("✅ Configurazione SMTP caricata: " + SMTP_HOST + ":" + SMTP_PORT);

			
			processaFatturazione(SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, ordineDao, clienteDao, fatturaDao,
					fatturaService, dettaglioOrdineDao);

		} catch (Exception e) {
			System.err.println("❌ ERRORE CRITICO DI INIZIALIZZAZIONE O CONFIGURAZIONE: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Cicla sugli ordini da fatturare gestendo la transazione per ciascuno
	 */
	private static void processaFatturazione(String smtpHost, String smtpPort, String smtpUser, String smtpPass,
			OrdineDAO ordineDao, ClienteDAO clienteDao, FatturaDAO fatturaDao, FatturaService fatturaService,
			DettaglioOrdineDAO dettaglioOrdineDao) throws SQLException {

		Connection mainConn = ConnessioneDB.getConnessione();
		List<Ordine> ordiniDaFatturare = ordineDao.recuperaOrdiniDaFatturare(mainConn);
		mainConn.close();

		System.out.println("Trovati " + ordiniDaFatturare.size() + " ordini da fatturare.");

		for (Ordine ordine : ordiniDaFatturare) {
			int codiceOrdine = ordine.getCodiceOrdine();

			Connection currentConn = null;
			String tempPdfPath = null;

			try {
				// Inizio Transazione (Ogni ordine è atomico)
				currentConn = ConnessioneDB.getConnessione();
				currentConn.setAutoCommit(false);

				
				int codiceCliente = ordine.getCodiceCliente();
				Cliente cliente = clienteDao.recuperaUno(codiceCliente, currentConn);
				List<DettaglioOrdine> dettagliOriginali = dettaglioOrdineDao
						.recuperaDettagliPerOrdine(ordine.getCodiceOrdine(), currentConn);

				

				if (cliente == null || cliente.getEmail() == null) {
					System.err.println(
							"  ❌ Fallimento per Ordine " + codiceOrdine + ": Cliente o email mancanti. Rollback.");
					currentConn.rollback();
					continue; // Passa al prossimo ordine
				}
				
				// 2. Creazione della Mappa Dettaglio -> Prodotto
				Map<DettaglioOrdine, Prodotto> dettagliConProdotti = new LinkedHashMap<>();
				
				

				for (DettaglioOrdine dettaglio : dettagliOriginali) {
		            // Recupera il prodotto utilizzando il DAO
					ProdottoDAO prodottoDao = new ProdottoDAO();
		            Prodotto prodotto = prodottoDao.recuperaUno(dettaglio.getCodiceProdotto()); 
		            
		            if (prodotto != null) {
		                // Associa il DettaglioOrdine (chiave) all'oggetto Prodotto (valore)
		                dettagliConProdotti.put(dettaglio, prodotto);
		            } else {
		                // Gestione caso : Prodotto non trovato
		                System.err.println("ATTENZIONE: Prodotto ID " + dettaglio.getCodiceProdotto() + " non trovato nel DB. ROLLBACK.");
		                currentConn.rollback();
		               
		            }
		        }
		        
		     
		        byte[] pdfContent = fatturaService.creaFatturaPDFContent(ordine, cliente, dettagliConProdotti);

				// 3. Salva la Fattura nel DB
				int anno = java.time.Year.now().getValue();
				int progressivo = fatturaDao.getNextProgressivo(currentConn);

				Fattura nuovaFattura = new Fattura(anno, progressivo, codiceOrdine, pdfContent);

				if (!fatturaDao.aggiungi(nuovaFattura, currentConn)) {
					throw new Exception("Errore nell'aggiungere la fattura al DB.");
				}

				// 4. Aggiorna lo stato dell'Ordine
				if (!ordineDao.segnaOrdineComeFatturato(codiceOrdine, currentConn)) {
					throw new Exception("Errore nell'aggiornare il campo 'fatturato'.");
				}

				// 5. Commit sul DB (se tutto è riuscito)
				currentConn.commit();

				
				tempPdfPath = salvaInFileTemporaneo(pdfContent, codiceOrdine);

				
				EmailService.inviaFatturazione(smtpHost, smtpPort, smtpUser, smtpPass, cliente.getEmail(),
						"La tua fattura n. " + anno + "/" + progressivo,
						"In allegato trovi la tua fattura per l'ordine " + codiceOrdine + ".", tempPdfPath);

				System.out.println("  ✅ Ordine " + codiceOrdine + " fatturato (ANNO:" + anno + ", PROGR.:" + progressivo
						+ ") e email inviata.");

			} catch (Exception e) {
				System.err.println(
						"  ❌ Transazione fallita per Ordine " + codiceOrdine + ". Rollback: " + e.getMessage());
				if (currentConn != null) {
					try {
						currentConn.rollback();
					} catch (SQLException se) {
						System.err.println("Errore durante il rollback: " + se.getMessage());
					}
				}
			} finally {
				// Pulizia e chiusura
				if (tempPdfPath != null) {
					// Assicura che il file temporaneo venga eliminato
					boolean deleted = new File(tempPdfPath).delete();
					if (!deleted) {
						System.err.println("Avviso: Impossibile eliminare il file temporaneo: " + tempPdfPath);
					}
				}
				if (currentConn != null) {
					try {
						currentConn.setAutoCommit(true);
						currentConn.close();
					} catch (SQLException se) {
						se.printStackTrace();
					}
				}
			}
		}
		System.out.println("✅ BATCH COMPLETATO.");
	}

	/**
	 * Metodo di supporto per salvare l'array di byte in un file per l'allegato
	 * email.
	 */
	private static String salvaInFileTemporaneo(byte[] content, int codiceOrdine) throws IOException {
		String tempDir = System.getProperty("java.io.tmpdir");
		String filePath = tempDir + File.separator + "Fattura_Temp_" + codiceOrdine + ".pdf";
		java.nio.file.Files.write(java.nio.file.Paths.get(filePath), content);
		return filePath;
	}
}