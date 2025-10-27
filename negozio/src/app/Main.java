package app;

import java.util.Scanner;

import services.CartaFedeltaService;
import services.ClienteService;
import services.ConfigurazioneService;
import services.DettaglioOrdineService;
import services.OrdineService;
import services.PrezziProdottoService;
import services.ProdottoService;
import services.RepartoService;
import services.ScontiService;

public class Main {

	public static void main(String[] args) {

		try {
			Scanner scanner = new Scanner(System.in);
			String sceltaUtente = "0";

			System.out.println("BENVENUTO NEL NEGOZIO");
			riga();

			while (!sceltaUtente.equalsIgnoreCase("e")) {

				System.out.println("\nSeleziona la tabella su cui vuoi effettuare operazioni\n"
						+ "1. Gestione tabella REPARTO\r\n" + "2. Gestione tabella PRODOTTO\r\n"
						+ "3. Gestione tabella PREZZI_PRODOTTO\r\n" + "4. Gestione tabella CLIENTE\r\n"
						+ "5. Gestione tabella CARTA_FEDELTA\r\n" + "6. Gestione tabella SCONTI\r\n"
						+ "7. Gestione tabella ORDINE\r\n" + "8. Gestione tabella DETTAGLIO_ORDINE\r\n"
						+ "9. Gestione tabella CONFIGURAZIONE\r\n" + "E. Esci");

				sceltaUtente = scanner.nextLine();

				switch (sceltaUtente.toLowerCase()) {
				case "1":
					RepartoService serviziReparto = new RepartoService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente.toLowerCase()) {
					case "1":
						serviziReparto.visualizzaReparti();
						break;
					case "2":
						serviziReparto.visualizzaReparto();
						break;
					case "3":
						serviziReparto.inserisciReparto();
						break;
					case "4":
						serviziReparto.modificaReparto();
						break;
					case "5":
						serviziReparto.cancellaReparto();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
						break;
					}
					break;
				case "2":
					ProdottoService serviziProdotto = new ProdottoService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente.toLowerCase()) {
					case "1":
						serviziProdotto.visualizzaProdotti();
						break;
					case "2":
						serviziProdotto.visualizzaProdotto();
						break;
					case "3":
						serviziProdotto.inserisciProdotto();
						break;
					case "4":
						serviziProdotto.modificaProdotto();
						break;
					case "5":
						serviziProdotto.cancellaProdotto();
						break;
					case "9":
						break;
					default:

						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
						break;

					}
					break;
				case "3":
					PrezziProdottoService serviziPrezziProdotto = new PrezziProdottoService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente.toLowerCase()) {
					case "1":
						serviziPrezziProdotto.visualizzaPrezzi();
						break;
					case "2":
						serviziPrezziProdotto.visualizzaPrezzo();
						break;
					case "3":
						serviziPrezziProdotto.inserisciPrezzo();
						break;
					case "4":
						serviziPrezziProdotto.modificaPrezzo();
						break;
					case "5":
						serviziPrezziProdotto.cancellaPrezzo();
						break;
					case "9":
						break;
					default:

						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
						break;

					}
					break;
				case "4":
					ClienteService serviziCliente = new ClienteService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente) {
					case "1":
						serviziCliente.visualizzaClienti();
						break;
					case "2":
						serviziCliente.visualizzaCliente();
						break;
					case "3":
						serviziCliente.inserisciCliente();
						break;
					case "4":
						serviziCliente.modificaCliente();
						break;
					case "5":
						serviziCliente.cancellaCliente();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
						break;
					}
					break;
				case "5":
					CartaFedeltaService serviziCarta = new CartaFedeltaService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente) {
					case "1":
						serviziCarta.visualizzaCarte();
						break;
					case "2":
						serviziCarta.visualizzaCarta();
						break;
					case "3":
						serviziCarta.inserisciCarta();
						break;
					case "4":
						serviziCarta.modificaCarta();
						break;
					case "5":
						serviziCarta.cancellaCarta();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
						break;
					}
					break;
				case "6":
					ScontiService serviziSconti = new ScontiService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente) {
					case "1":
						serviziSconti.visualizzaSconti();
						break;
					case "2":
						serviziSconti.visualizzaSconto();
						break;
					case "3":
						serviziSconti.inserisciSconto();
						break;
					case "4":
						serviziSconti.modificaSconto();
						break;
					case "5":
						serviziSconti.cancellaSconto();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
					}
					;
					break;
				case "7":
					OrdineService serviziOrdine = new OrdineService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente) {
					case "1":
						serviziOrdine.visualizzaOrdini();
						break;
					case "2":
						serviziOrdine.visualizzaOrdine();
						break;
					case "3":
						serviziOrdine.inserisciOrdine();
						break;
					case "4":
						serviziOrdine.modificaOrdine();
						break;
					case "5":
						serviziOrdine.cancellaOrdine();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
					}
					;
					break;
				case "8":
					DettaglioOrdineService serviziDettaglioOrdine = new DettaglioOrdineService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente) {
					case "1":
						serviziDettaglioOrdine.visualizzaDettagliOrdine();
						break;
					case "2":
						serviziDettaglioOrdine.visualizzaSingoloDettaglioOrdine();
						break;
					case "3":
						serviziDettaglioOrdine.inserisciDettaglioOrdine();
						break;
					case "4":
						serviziDettaglioOrdine.modificaDettaglioOrdine();
						break;
					case "5":
						serviziDettaglioOrdine.cancellaDettaglioOrdine();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
					};
					break;
				case "9":
					ConfigurazioneService serviziConfigurazione = new ConfigurazioneService();
					mostraMenu();
					sceltaUtente = scanner.nextLine();
					switch (sceltaUtente) {
					case "1":
						serviziConfigurazione.visualizzaConfigurazioni();
						break;
					case "2":
						serviziConfigurazione.visualizzaConfigurazione();
						break;
					case "3":
						serviziConfigurazione.inserisciConfigurazione();
						break;
					case "4":
						serviziConfigurazione.modificaConfigurazione();
						break;
					case "5":
						serviziConfigurazione.cancellaConfigurazione();
						break;
					case "9":
						break;
					default:
						System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
					};
					break;
				case "e":
					break;
				default:
					System.out.println("Hai inserito un numero non presente nel menù, scegli un'opzione valida.");
					break;

				}
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Sei uscito dal programma.");
		System.exit(0); // Termina il programma con successo
		System.out.println("Questa riga non verrà mai eseguita.");

	}

	private static void mostraMenu() {
		System.out.println("Quale delle seguenti operazioni vuoi effettuare?\n" + "1. Estrai tutte le occorrenze\r\n"
				+ "2. Estrai elemento per chiave\r\n" + "3. Inserisci un nuovo elemento\r\n"
				+ "4. Aggiorna un elemento\r\n" + "5. Cancella un elemento\r\n" + "9. Indietro");
	};

	private static void riga() {
		System.out.println("...........................................................");
	}

}
