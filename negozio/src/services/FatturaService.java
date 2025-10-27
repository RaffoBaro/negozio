package services;

import entities.Ordine;
import entities.Cliente;
import entities.DettaglioOrdine;
import entities.Prodotto; // Necessario per la mappa

import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Import di APACHE PDFBOX
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.awt.Color; // Per risolvere l'avviso di deprecazione

public class FatturaService {

	// Costanti per le posizioni e formattazione
	private static final float MARGIN = 40;
	private static final float FONT_SIZE_NORMAL = 10;
	private static final float FONT_SIZE_SMALL = 8;
	private static final float LEADING = 1.5f * FONT_SIZE_NORMAL;

	private float tableYPosition = 0;

	/**
	 * Crea il contenuto del PDF della fattura in memoria (byte array) usando
	 * PDFBox.
	 */
	public byte[] creaFatturaPDFContent(Ordine ordine, Cliente cliente,
			// 🛑 NUOVA FIRMA: ACCETTA LA MAPPA 🛑
			Map<DettaglioOrdine, Prodotto> dettagliConProdotti) throws Exception {

		System.out.println("✅ Generazione contenuto PDF con layout grafico per Ordine " + ordine.getCodiceOrdine()
				+ " (usando PDFBox)");

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
				contents.showText(cliente.getNome() + " (" + cliente.getEmail() + ")");
				contents.endText();
				float rightCol = width - MARGIN - 200;
				yPosition += LEADING;
				contents.beginText();
				contents.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_NORMAL);
				contents.newLineAtOffset(rightCol, yPosition);
				contents.showText("Fattura n°: " + ordine.getCodiceOrdine());
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
				float[] colWidths = { 250, 80, 50, 80 };
				float tableX = MARGIN;

				tableYPosition = drawTableHeader(contents, tableX, tableYPosition, colWidths);

				double subtotal = 0.0;

				// Ciclo per disegnare le righe di dettaglio (Itera sulla mappa)
				for (Map.Entry<DettaglioOrdine, Prodotto> entry : dettagliConProdotti.entrySet()) {
					DettaglioOrdine dettaglio = entry.getKey();
					Prodotto prodotto = entry.getValue();

					// Gestione cambio pagina
					if (tableYPosition < MARGIN + 50) {
						contents.close();
						page = new PDPage();
						document.addPage(page);
						try (PDPageContentStream newContents = new PDPageContentStream(document, page)) {
							tableYPosition = page.getMediaBox().getHeight() - MARGIN;
							tableYPosition = drawTableHeader(newContents, tableX, tableYPosition, colWidths);
							drawDetailRow(newContents, tableX, tableYPosition, colWidths, dettaglio, prodotto);
							tableYPosition -= LEADING;
						}
					} else {
						// 🛑 CHIAMATA AGGIORNATA 🛑
						tableYPosition = drawDetailRow(contents, tableX, tableYPosition, colWidths, dettaglio,
								prodotto);
					}

					subtotal += dettaglio.getTotaleRigaCalcolato(); // Assumo questo metodo esista nel DettaglioOrdine
				}

				yPosition = tableYPosition - LEADING * 3;

				// --- 4. SEZIONE RIEPILOGO FINALE ---
				float summaryX = width - MARGIN - 150;

				yPosition = drawSummaryLine(contents, "SUBTOTALE", String.format(Locale.ITALY, "€ %.2f", subtotal),
						summaryX, yPosition, PDType1Font.HELVETICA_BOLD);
				double ivaTotale = ordine.getTotaleOrdineCalcolato() - subtotal;
				yPosition = drawSummaryLine(contents, "IVA/Tax", String.format(Locale.ITALY, "€ %.2f", ivaTotale),
						summaryX, yPosition, PDType1Font.HELVETICA);
				yPosition = drawSummaryLine(contents, "TOTAL DUE",
						String.format(Locale.ITALY, "€ %.2f", ordine.getTotaleOrdineCalcolato()), summaryX, yPosition,
						PDType1Font.HELVETICA_BOLD, 12);

				contents.close();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			document.save(baos);
			return baos.toByteArray();
		}
	}

	// =========================================================================
	// METODI AUSILIARI PER PDFBOX (Disegno Tabella e Righe)
	// =========================================================================

	/** Disegna le intestazioni della tabella dei dettagli. */
	private float drawTableHeader(PDPageContentStream contents, float x, float y, float[] colWidths)
			throws IOException {
		y -= LEADING;
		contents.setNonStrokingColor(Color.BLACK); // Correzione deprecazione
		contents.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL);

		float currentX = x;

		contents.beginText();
		contents.newLineAtOffset(currentX, y);
		contents.showText("DESCRIZIONE");
		contents.endText();
		currentX += colWidths[0];
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[1]
				- (getScaledStringWidth("PREZZO UN.", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)), y);
		contents.showText("PREZZO UN.");
		contents.endText();
		currentX += colWidths[1];
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[2] - (getScaledStringWidth("Q.TA'", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)),
				y);
		contents.showText("Q.TA'");
		contents.endText();
		currentX += colWidths[2];
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[3] - (getScaledStringWidth("TOTALE", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SMALL)),
				y);
		contents.showText("TOTALE");
		contents.endText();

		y -= 2;
		contents.setLineWidth(0.5f);
		contents.moveTo(MARGIN, y);
		contents.lineTo(MARGIN + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3], y);
		contents.stroke();

		return y - LEADING;
	}

	/**
	 * Disegna una riga di dettaglio. 🛑 FIRMA AGGIORNATA: ACCETTA IL PRODOTTO PER
	 * LA DESCRIZIONE 🛑
	 */
	private float drawDetailRow(PDPageContentStream contents, float x, float y, float[] colWidths,
			DettaglioOrdine dettaglio, Prodotto prodotto) throws IOException {

		contents.setNonStrokingColor(Color.BLACK);
		contents.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);

		float currentX = x;

		// Uso i metodi corretti di DettaglioOrdine (quantita e totale riga)
		double prezzoUnitario = (dettaglio.getQuantitaOrdinata() > 0)
				? dettaglio.getTotaleRigaCalcolato() / dettaglio.getQuantitaOrdinata()
				: 0.00;

		// 1. Descrizione (Sinistra)
		contents.beginText();
		contents.newLineAtOffset(currentX, y);
		// 🛑 USA ORA IL GETTER DELL'ENTITÀ PRODOTTO 🛑
		contents.showText(prodotto.getDescrizione());
		contents.endText();
		currentX += colWidths[0];

		// 2. Prezzo Un. (Destra)
		String sPrezzoUnitario = String.format(Locale.ITALY, "€ %.2f", prezzoUnitario);
		contents.beginText();
		contents.newLineAtOffset(currentX + colWidths[1]
				- (getScaledStringWidth(sPrezzoUnitario, PDType1Font.HELVETICA, FONT_SIZE_SMALL)), y);
		contents.showText(sPrezzoUnitario);
		contents.endText();
		currentX += colWidths[1];

		// 3. Q.TA' (Destra)
		String sQuantita = String.valueOf(dettaglio.getQuantitaOrdinata());
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[2] - (getScaledStringWidth(sQuantita, PDType1Font.HELVETICA, FONT_SIZE_SMALL)), y);
		contents.showText(sQuantita);
		contents.endText();
		currentX += colWidths[2];

		// 4. Totale (Destra)
		String sTotaleRiga = String.format(Locale.ITALY, "€ %.2f", dettaglio.getTotaleRigaCalcolato());
		contents.beginText();
		contents.newLineAtOffset(
				currentX + colWidths[3] - (getScaledStringWidth(sTotaleRiga, PDType1Font.HELVETICA, FONT_SIZE_SMALL)),
				y);
		contents.showText(sTotaleRiga);
		contents.endText();

		return y - LEADING;
	}

	/** Disegna una riga di riepilogo (Subtotale/Totale). */
	private float drawSummaryLine(PDPageContentStream contents, String label, String value, float x, float y,
			PDType1Font font) throws IOException {
		return drawSummaryLine(contents, label, value, x, y, font, FONT_SIZE_NORMAL);
	}

	private float drawSummaryLine(PDPageContentStream contents, String label, String value, float x, float y,
			PDType1Font font, float fontSize) throws IOException {

		// Etichetta (Sinistra della colonna riepilogo)
		contents.beginText();
		contents.setFont(font, fontSize);
		contents.newLineAtOffset(x, y);
		contents.showText(label);
		contents.endText();

		// Valore (Destra della colonna riepilogo, allineato a destra)
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