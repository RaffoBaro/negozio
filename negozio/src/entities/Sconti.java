package entities;

import java.util.Date;

public class Sconti {

	private int codiceSconto;
	private String codiceCarta;
	private Date dataInizio;
	private Date dataFine;
	private int sconto;

	public Sconti() {
		super();
	}

	public Sconti(int codiceSconto, String codiceCarta, Date dataInizio, Date dataFine, int sconto) {
		super();
		this.codiceSconto = codiceSconto;
		this.codiceCarta = codiceCarta;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
		this.sconto = sconto;
	}

	public Sconti(String codiceCarta, Date dataInizioDate, Date dataFineDate, int valoreSconto) {
		this.codiceCarta = codiceCarta;
		this.dataInizio = dataInizioDate;
		this.dataFine = dataFineDate;
		this.sconto = valoreSconto;	}

	public int getCodiceSconto() {
		return codiceSconto;
	}

	public void setCodiceSconto(int codiceSconto) {
		this.codiceSconto = codiceSconto;
	}

	public String getCodiceCarta() {
		return codiceCarta;
	}

	public void setCodiceCarta(String codiceCarta) {
		this.codiceCarta = codiceCarta;
	}

	public Date getDataInizio() {
		return dataInizio;
	}

	public void setDataInizio(Date dataInizio) {
		this.dataInizio = dataInizio;
	}

	public Date getDataFine() {
		return dataFine;
	}

	public void setDataFine(Date dataFine) {
		this.dataFine = dataFine;
	}

	public int getSconto() {
		return sconto;
	}

	public void setSconto(int sconto) {
		this.sconto = sconto;
	}

	@Override
	public String toString() {
		return "Sconti [codiceSconto=" + codiceSconto + ", codiceCarta=" + codiceCarta + ", dataInizio=" + dataInizio
				+ ", dataFine=" + dataFine + ", sconto=" + sconto + "]";
	}

	
	
	
}
