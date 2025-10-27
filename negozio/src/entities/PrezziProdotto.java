package entities;

import java.util.Date;

public class PrezziProdotto {

	private int codice_prodotto;
	private Date data_inizio;
	private Date data_fine;
	private double prezzo;
	private double iva;

	public PrezziProdotto() {
		super();
	}

	public PrezziProdotto(int codice_prodotto, Date dataInizioDate, Date dataFineDate, double prezzo, double iva) {
		super();
		this.codice_prodotto = codice_prodotto;
		this.data_inizio = dataInizioDate;
		this.data_fine = dataFineDate;
		this.prezzo = prezzo;
		this.iva = iva;
	}

	public int getCodiceProdotto() {
		return codice_prodotto;
	}

	public void setCodiceProdotto(int codice_prodotto) {
		this.codice_prodotto = codice_prodotto;
	}

	public Date getDataInizio() {
		return data_inizio;
	}

	public void setDataInizio(Date data_inizio) {
		this.data_inizio = data_inizio;
	}

	public Date getDataFine() {
		return data_fine;
	}

	public void setDataFine(Date data_fine) {
		this.data_fine = data_fine;
	}

	public double getPrezzo() {
		return prezzo;
	}

	public void setPrezzo(double prezzo) {
		this.prezzo = prezzo;
	}

	public double getIva() {
		return iva;
	}

	public void setIva(double iva) {
		this.iva = iva;
	}

	@Override
	public String toString() {
		return "PrezziProdotto [codiceProdotto=" + codice_prodotto + ", dataInizio=" + data_inizio + ", dataFine="
				+ data_fine + ", prezzo=" + prezzo + ", iva=" + iva + "]";
	}

}
