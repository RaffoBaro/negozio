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
import dao.PrezziProdottoDAO;
import dao.ProdottoDAO;
import dao.ScontiDAO;
import dao.CartaFedeltaDAO;
import dao.ClienteDAO;
import dao.FatturaDAO;
import dao.DettaglioOrdineDAO;
import dao.ConfigurazioneDAO;
import services.FatturaService;
import services.EmailService;
import entities.Ordine;
import entities.PrezziProdotto;
import entities.Prodotto;
import entities.Sconti;
import entities.CartaFedelta;
import entities.Cliente;
import entities.Fattura;
import entities.DettaglioOrdine;

public class FatturazioneBatch {

	public static void main(String[] args) {
		System.out.println("🚀 AVVIO PROGRAMMA BATCH DI FATTURAZIONE E INVIO MAIL\n");

		try {
			OrdineDAO ordineDao = new OrdineDAO();
			ClienteDAO clienteDao = new ClienteDAO();
			FatturaDAO fatturaDao = new FatturaDAO();
			PrezziProdottoDAO pProdDao = new PrezziProdottoDAO();
			CartaFedeltaDAO cartaDao = new CartaFedeltaDAO();
			ScontiDAO scontiDao = new ScontiDAO();
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

			processaFatturazione(SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, ordineDao, clienteDao, fatturaDao,
					fatturaService, dettaglioOrdineDao, pProdDao, cartaDao, scontiDao);

		} catch (Exception e) {
			System.err.println("❌ ERRORE CRITICO DI INIZIALIZZAZIONE O CONFIGURAZIONE: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void processaFatturazione(String smtpHost, String smtpPort, String smtpUser, String smtpPass,
			OrdineDAO ordineDao, ClienteDAO clienteDao, FatturaDAO fatturaDao, FatturaService fatturaService,
			DettaglioOrdineDAO dettaglioOrdineDao, PrezziProdottoDAO pProdDao, CartaFedeltaDAO cartaDao,
			ScontiDAO scontiDao) throws SQLException {

		Connection mainConn = ConnessioneDB.getConnessione();
		List<Ordine> ordiniDaFatturare = ordineDao.recuperaOrdiniDaFatturare(mainConn);
		mainConn.close();

		System.out.println("Trovati " + ordiniDaFatturare.size() + " ordini da fatturare.\n");

		for (Ordine ordine : ordiniDaFatturare) {
			int codiceOrdine = ordine.getCodiceOrdine();

			Connection currentConn = null;
			String tempPdfPath = null;

			try {
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
					continue;
				}
				
				// 🎯 1. LOGICA DI RECUPERO SCONTO
				Sconti scontoApplicato = null;
				try {
				    CartaFedelta carta = cartaDao.recuperaCartaFedelta(codiceCliente, currentConn);
				    if (carta != null) {
				        // Recupera l'oggetto Sconti valido alla data dell'ordine
				        // Assumiamo che scontiDao.recuperaUnoCodiceCarta() accetti la Connection se necessario,
				        // altrimenti dovrà gestire la connessione separatamente. Per sicurezza, usiamo currentConn.
				        scontoApplicato = scontiDao.recuperaUnoCodiceCarta(carta.getCodiceCarta(), ordine.getDataOrdine());
				        if (scontoApplicato != null) {
				            System.out.println("  Sconto Fedeltà " + scontoApplicato.getSconto() + "% trovato per il Cliente.");
				        }
				    }
				} catch (Exception e) {
				    System.err.println("ATTENZIONE: Errore durante il recupero dello Sconto Fedeltà. Proseguo senza sconto.");
				    // Lo sconto rimane null e il batch prosegue
				}
				
				
				

				Map<DettaglioOrdine, Prodotto> dettagliConProdotti = new LinkedHashMap<>();

				for (DettaglioOrdine dettaglio : dettagliOriginali) {
					ProdottoDAO prodottoDao = new ProdottoDAO();
					Prodotto prodotto = prodottoDao.recuperaUno(dettaglio.getCodiceProdotto());

					if (prodotto != null) {

						// 🎯 LOGICA PER RECUPERARE E IMPOSTARE L'ALIQUOTA IVA STORICA

						// 1. Recupera l'oggetto PrezziProdotto valido alla data dell'ordine
						pProdDao = new PrezziProdottoDAO(); 
						PrezziProdotto prezziStorici = pProdDao.recuperaPrezziValidiAllaData(
								dettaglio.getCodiceProdotto(), ordine.getDataOrdine(), currentConn);

						if (prezziStorici == null) {
							System.err.println("ATTENZIONE: Aliquota IVA storica mancante per Prodotto ID "
									+ dettaglio.getCodiceProdotto() + ". ROLLBACK.");
							currentConn.rollback();
							// Lancia un'eccezione per uscire e assicurare il rollback
							throw new Exception(
									"Dati IVA storica mancanti per prodotto " + dettaglio.getCodiceProdotto());
						}

						// 2. Imposta l'IVA storica nel DettaglioOrdine
						dettaglio.setIvaStoricaApplicata(prezziStorici.getIva());

						// Associo il DettaglioOrdine (ora con IVA) all'oggetto Prodotto (per
						// descrizione)
						dettagliConProdotti.put(dettaglio, prodotto);

					} else {
						// Gestione caso : Prodotto non trovato
						System.err.println("ATTENZIONE: Prodotto ID " + dettaglio.getCodiceProdotto()
								+ " non trovato nel DB. ROLLBACK.");
						currentConn.rollback();
						// Lancia un'eccezione per uscire e assicurare il rollback
						throw new Exception("Prodotto non trovato con ID " + dettaglio.getCodiceProdotto());
					}
				}

				int anno = java.time.Year.now().getValue();
				int progressivo = fatturaDao.getNextProgressivo(currentConn);

				byte[] pdfContent = fatturaService.creaFatturaPDFContent(ordine, cliente, dettagliConProdotti, anno,
						progressivo, scontoApplicato);

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

				System.out.println("✅ Ordine " + codiceOrdine + " fatturato (ANNO:" + anno + ", PROGR.:" + progressivo
						+ ") e email inviata.\n");

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