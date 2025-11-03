package services;

import entities.Ordine;
import entities.Cliente;
import entities.DettaglioOrdine;
import entities.Prodotto;
import entities.Sconti;

import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.awt.Color;

public class FatturaService {

	// Costanti per le posizioni e formattazione
	private static final float MARGIN = 40;
	private static final float FONT_SIZE_NORMAL = 10;
	private static final float FONT_SIZE_SMALL = 8;
	private static final float LEADING = 1.5f * FONT_SIZE_NORMAL;

	private float tableYPosition = 0;

	// 1. FIRMA AGGIORNATA: Ora accetta Sconti
	public byte[] creaFatturaPDFContent(Ordine ordine, Cliente cliente,
			Map<DettaglioOrdine, Prodotto> dettagliConProdotti, int anno, int progressivo, Sconti scontoApplicato)
			throws Exception {

		System.out.println("✅ Generazione contenuto PDF per Ordine " + ordine.getCodiceOrdine() + " (usando PDFBox)\n");

		// Calcolo la percentuale di sconto (0 se scontoApplicato è null)
		final int percentualeSconto = (scontoApplicato != null) ? scontoApplicato.getSconto() : 0;

		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);

			try (PDPageContentStream contents = new PDPageContentStream(document, page)) {

				float width = page.getMediaBox().getWidth();
				float yPosition = page.getMediaBox().getHeight() - MARGIN;
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);

				// --- 1. SEZIONE INTESTAZIONE PRINCIPALE ---
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA_BOLD, 16);
				contents.newLineAtOffset(MARGIN, yPosition);
				contents.showText("EMITTENTE");
				contents.endText();
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA_BOLD, 24);
				contents.newLineAtOffset(width - MARGIN - 100, yPosition);
				contents.showText("INVOICE");
				contents.endText();
				yPosition -= LEADING * 2;

				// --- 2. SEZIONE DATI FATTURA E CLIENTE ---
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_NORMAL);
				contents.newLineAtOffset(MARGIN, yPosition);
				contents.showText("CLIENTE:");
				contents.endText();
				yPosition -= LEADING;
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
				contents.newLineAtOffset(MARGIN, yPosition);
				contents.showText(cliente.getNome() + " " + cliente.getCognome());
				contents.endText();
				float rightCol = width - MARGIN - 200;
				yPosition += LEADING;
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_NORMAL);
				contents.newLineAtOffset(rightCol, yPosition);
				contents.showText("Fattura n°: " + progressivo + " / " + anno);
				contents.endText();
				yPosition -= LEADING;
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
				contents.newLineAtOffset(rightCol, yPosition);
				contents.showText("Data: " + sdf.format(ordine.getDataOrdine()));
				contents.endText();

				yPosition -= LEADING * 2;
				tableYPosition = yPosition;

				// --- 3. TABELLA DEI DETTAGLI DELL'ORDINE ---
				// Larghezze AGGIORNATE: Desc(200), P.Unitario(70), Q.tà(40), Sconto%(40), IVA%(40), Totale(80)
				float[] colWidths = { 200, 70, 40, 40, 40, 80 };
				float tableX = MARGIN;

				tableYPosition = drawTableHeader(contents, tableX, tableYPosition, colWidths);

				double subtotalNetto = 0.0; // Totale imponibile netto
				double ivaTotaleMonetaria = 0.0; // Totale IVA monetaria

				// Ciclo per disegnare le righe di dettaglio
				for (Map.Entry<DettaglioOrdine, Prodotto> entry : dettagliConProdotti.entrySet()) {
					DettaglioOrdine dettaglio = entry.getKey();
					Prodotto prodotto = entry.getValue();
					
					// Recupera l'aliquota IVA dal DettaglioOrdine (come impostato nel Batch)
					double ivaAliquota = dettaglio.getIvaStoricaApplicata();

					// Gestione cambio pagina
					if (tableYPosition < MARGIN + 50) {
						contents.close();
						page = new PDPage();
						document.addPage(page);
						try (PDPageContentStream newContents = new PDPageContentStream(document, page)) {
							tableYPosition = page.getMediaBox().getHeight() - MARGIN;
							tableYPosition = drawTableHeader(newContents, tableX, tableYPosition, colWidths);
							
							double[] results = drawDetailRow(newContents, tableX, tableYPosition, colWidths, dettaglio, prodotto, percentualeSconto, ivaAliquota);
							subtotalNetto += results[0];
							ivaTotaleMonetaria += results[1];
							tableYPosition = (float) results[2];
							
						}
					} else {
						// 5. CHIAMATA A DRAW DETAIL ROW AGGIORNATA
						double[] results = drawDetailRow(contents, tableX, tableYPosition, colWidths, dettaglio,
								prodotto, percentualeSconto, ivaAliquota);
						
						subtotalNetto += results[0];
						ivaTotaleMonetaria += results[1];
						tableYPosition = (float) results[2];
					}
				}

				yPosition = tableYPosition - LEADING * 3;

				// --- 4. SEZIONE RIEPILOGO FINALE ---
				float summaryX = width - MARGIN - 150;

				// Ora usiamo subtotalNetto e ivaTotaleMonetaria calcolati riga per riga
				yPosition = drawSummaryLine(contents, "SUBTOTALE", String.format(Locale.ITALY, "€ %.2f", subtotalNetto),
						summaryX, yPosition, PDType1Font.HELVETICA_BOLD);
				
				yPosition = drawSummaryLine(contents, "IVA/Tax", String.format(Locale.ITALY, "€ %.2f", ivaTotaleMonetaria),
						summaryX, yPosition, PDType1Font.HELVETICA);
				
				// TOTALE DOVUTO: somma dei due (deve coincidere con ordine.getTotaleOrdineCalcolato())
				yPosition = drawSummaryLine(contents, "TOTAL DUE",
						String.format(Locale.ITALY, "€ %.2f", subtotalNetto + ivaTotaleMonetaria), summaryX, yPosition,
						PDType1Font.HELVETICA_BOLD, 12);

				contents.close();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			document.save(baos);
			return baos.toByteArray();
		}
	}

	// METODI AUSILIARI
	// =================
	private float drawTableHeader(PDPageContentStream contents, float x, float y, float[] colWidths)
			throws IOException {
		y -= LEADING;
		contents.setNonStrokingColor(Color.BLACK);
		contents.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL);

		float currentX = x;

		// 1. DESCRIZIONE
		contents.beginText();
		contents.newLineAtOffset(currentX, y);
		contents.showText("DESCRIZIONE");
		contents.endText();
		currentX += colWidths[0];
		
		// 2. PREZZO UNITARIO
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[1]
				- (getScaledStringWidth("P. UNITARIO", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)), y);
		contents.showText("PREZZO UN.");
		contents.endText();
		currentX += colWidths[1];
		
		// 3. Q.TA'
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[2] - (getScaledStringWidth("Q.TA'", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)),
				y);
		contents.showText("Q.TA'");
		contents.endText();
		currentX += colWidths[2];
		
		// 4. SCONTO % (NUOVA COLONNA)
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[3] - (getScaledStringWidth("SCONTO", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)),
				y);
		contents.showText("SCONTO");
		contents.endText();
		currentX += colWidths[3];
		
		// 5. IVA % (NUOVA COLONNA)
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[4] - (getScaledStringWidth("IVA", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)),
				y);
		contents.showText("IVA");
		contents.endText();
		currentX += colWidths[4];
		
		// 6. TOTALE
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[5] - (getScaledStringWidth("TOTALE", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)),
				y);
		contents.showText("TOTALE");
		contents.endText();
		// currentX += colWidths[5]; // Non serve spostarlo oltre

		y -= 2;
		contents.setLineWidth(0.5f);
		// La linea deve coprire tutte e 6 le colonne
		float tableWidth = colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + colWidths[5];
		contents.moveTo(MARGIN, y);
		contents.lineTo(MARGIN + tableWidth, y);
		contents.stroke();

		return y - LEADING;
	}

	// 3. FIRMA AGGIORNATA E TIPO DI RITORNO AGGIORNATO: Ritorna Imponibile, IVA Monetaria e Y
	private double[] drawDetailRow(PDPageContentStream contents, float x, float y, float[] colWidths,
			DettaglioOrdine dettaglio, Prodotto prodotto, int percentualeSconto, double ivaAliquota) throws IOException {

		contents.setNonStrokingColor(Color.BLACK);
		contents.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);

		float currentX = x;
		
		// --- PREPARAZIONE DATI E CALCOLI ---
		double moltiplicatoreIva = 1 + (ivaAliquota / 100.0);
		double moltiplicatoreSconto = 1.0 - (percentualeSconto / 100.0);
		
		// Totale Lordo/IVATO finale della riga
		double totaleRigaLordoScontato = dettaglio.getTotaleRigaCalcolato(); 
		
		// Calcolo Imponibile (Netto Scontato) e IVA Monetaria
		double totaleRigaNettoScontato = totaleRigaLordoScontato / moltiplicatoreIva;
		double ivaRigaMonetaria = totaleRigaLordoScontato - totaleRigaNettoScontato;
		
		// Calcolo Prezzo Unitario Netto NON scontato (per la visualizzazione di listino)
		double prezzoUnitarioNettoScontato = (dettaglio.getQuantitaOrdinata() > 0)
			? totaleRigaNettoScontato / dettaglio.getQuantitaOrdinata()
			: 0.00;
			
		double prezzoUnitarioNettoNonScontato = (moltiplicatoreSconto > 0)
			? prezzoUnitarioNettoScontato / moltiplicatoreSconto
			: prezzoUnitarioNettoScontato;


		// --- DISEGNO DELLE 6 COLONNE ---

		// 1. DESCRIZIONE
		contents.beginText();
		contents.newLineAtOffset(currentX, y);
		contents.showText(prodotto.getDescrizione());
		contents.endText();
		currentX += colWidths[0];

		// 2. PREZZO UNITARIO NETTO NON SCONTATO (Listino)
		String sPrezzoUnitarioNetto = String.format(Locale.ITALY, "€ %.2f", prezzoUnitarioNettoNonScontato);
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[1]
				- (getScaledStringWidth(sPrezzoUnitarioNetto, PDType1Font.HELVETICA, FONT_SIZE_SMALL)), y);
		contents.showText(sPrezzoUnitarioNetto);
		contents.endText();
		currentX += colWidths[1];

		// 3. QUANTITA'
		String sQuantita = String.valueOf(dettaglio.getQuantitaOrdinata());
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[2] - (getScaledStringWidth(sQuantita, PDType1Font.HELVETICA, FONT_SIZE_SMALL)), y);
		contents.showText(sQuantita);
		contents.endText();
		currentX += colWidths[2];

		// 4. SCONTO %
		String sSconto = percentualeSconto > 0 ? (percentualeSconto + "%") : "0%";
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[3] - getScaledStringWidth(sSconto, PDType1Font.HELVETICA, FONT_SIZE_SMALL), y);
		if (percentualeSconto > 0) contents.setNonStrokingColor(Color.RED);
		contents.showText(sSconto);
		contents.setNonStrokingColor(Color.BLACK); 
		contents.endText();
		currentX += colWidths[3];

		// 5. IVA %
		String sIva = String.format(Locale.ITALY, "%.0f%%", ivaAliquota); 
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[4] - getScaledStringWidth(sIva, PDType1Font.HELVETICA, FONT_SIZE_SMALL), y);
		contents.showText(sIva);
		contents.endText();
		currentX += colWidths[4];

		// 6. TOTALE RIGA (Finale Lordo/IVATO)
		String sTotaleRiga = String.format(Locale.ITALY, "€ %.2f", totaleRigaLordoScontato);
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[5] - getScaledStringWidth(sTotaleRiga, PDType1Font.HELVETICA, FONT_SIZE_SMALL),y);
		contents.showText(sTotaleRiga);
		contents.endText();
		
		float newY = y - LEADING;
		
		// 4. RITORNO VALORI PER IL CUMULO
		return new double[]{totaleRigaNettoScontato, ivaRigaMonetaria, newY};
	}

	private float drawSummaryLine(PDPageContentStream contents, String label, String value, float x, float y,
			PDType1Font font) throws IOException {
		return drawSummaryLine(contents, label, value, x, y, font, FONT_SIZE_NORMAL);
	}

	private float drawSummaryLine(PDPageContentStream contents, String label, String value, float x, float y,
			PDType1Font font, float fontSize) throws IOException {

		contents.beginText();
		contents.setFont(font, fontSize);
		contents.newLineAtOffset(x, y);
		contents.showText(label);
		contents.endText();

		float valueColWidth = 50;
		float valueX = x + 100;

		contents.beginText();
		contents.setFont(font, fontSize);
		float offset = valueColWidth - getScaledStringWidth(value, font, fontSize);
		contents.newLineAtOffset(valueX + offset, y);
		contents.showText(value);
		contents.endText();

		return y - LEADING;
	}

	/**
	 * Calcola la larghezza di una stringa con il font e la dimensione specificati.
	 */
	private float getScaledStringWidth(String text, PDType1Font font, float fontSize) throws IOException {
		return font.getStringWidth(text) / 1000 * fontSize;
	}
}