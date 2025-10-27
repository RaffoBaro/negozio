package entities;

public class DettaglioOrdine {

	private int codice_ordine;
	private int progressivo;
	private int codice_prodotto;
	private int quantita_ordinata;
	private double totale_riga_calcolato;
	
	public DettaglioOrdine() {
		super();
	}

	public DettaglioOrdine(int codiceOrdine, int progressivo, int codiceProdotto, int quantitaOrdinata,
			double totaleRigaCalcolato) {
		super();
		this.codice_ordine = codiceOrdine;
		this.progressivo = progressivo;
		this.codice_prodotto = codiceProdotto;
		this.quantita_ordinata = quantitaOrdinata;
		this.totale_riga_calcolato = totaleRigaCalcolato;
	}

	public DettaglioOrdine(int codiceOrdine, int progressivo, int codiceProdotto, int quantitaOrdinata) {
		this.codice_ordine = codiceOrdine;
		this.progressivo = progressivo;
		this.codice_prodotto = codiceProdotto;
		this.quantita_ordinata = quantitaOrdinata;	}

	public DettaglioOrdine(int codiceOrdine, int codiceProdotto, int quantitaOrdinata) {
		this.codice_ordine = codiceOrdine;
		this.codice_prodotto = codiceProdotto;
		this.quantita_ordinata = quantitaOrdinata;
	}

	public int getCodiceOrdine() {
		return codice_ordine;
	}

	public void setCodiceOrdine(int codiceOrdine) {
		this.codice_ordine = codiceOrdine;
	}

	public int getProgressivo() {
		return progressivo;
	}

	public void setProgressivo(int progressivo) {
		this.progressivo = progressivo;
	}

	public int getCodiceProdotto() {
		return codice_prodotto;
	}

	public void setCodiceProdotto(int codiceProdotto) {
		this.codice_prodotto = codiceProdotto;
	}

	public int getQuantitaOrdinata() {
		return quantita_ordinata;
	}

	public void setQuantitaOrdinata(int quantitaOrdinata) {
		this.quantita_ordinata = quantitaOrdinata;
	}

	public double getTotaleRigaCalcolato() {
		return totale_riga_calcolato;
	}

	public void setTotaleRigaCalcolato(double totaleRigaCalcolato) {
		this.totale_riga_calcolato = totaleRigaCalcolato;
	}

	@Override
	public String toString() {
		return "DettaglioOrdine [codiceOrdine=" + codice_ordine + ", progressivo=" + progressivo + ", codiceProdotto="
				+ codice_prodotto + ", quantitaOrdinata=" + quantita_ordinata + ", totaleRigaCalcolato="
				+ totale_riga_calcolato + "]";
	}

}
