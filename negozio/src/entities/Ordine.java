package entities;

import java.util.Date;

public class Ordine {

	private int codiceOrdine;
	private int codiceCliente;
	private Date dataOrdine;
	private double totaleOrdineCalcolato;
	private boolean fatturato;

	public Ordine() {
		super();
	}

	public Ordine(int codiceOrdine, int codiceCliente, Date dataOrdine, double totaleOrdineCalcolato,
			boolean fatturato) {
		super();
		this.codiceOrdine = codiceOrdine;
		this.codiceCliente = codiceCliente;
		this.dataOrdine = dataOrdine;
		this.totaleOrdineCalcolato = totaleOrdineCalcolato;
		this.fatturato = fatturato;
	}

	public Ordine(int codiceCliente, Date dataOrdine) {
		this.codiceCliente = codiceCliente;
		this.dataOrdine = dataOrdine;
	}

	public Ordine(int codiceOrdine, Date dataOrdine, double totaleOrdineCalcolato, int codiceCliente,
			boolean fatturato) {
		this.codiceOrdine = codiceOrdine;
		this.dataOrdine = dataOrdine;
		this.totaleOrdineCalcolato = totaleOrdineCalcolato;
		this.codiceCliente = codiceCliente;
		this.fatturato = fatturato;
	}

	public int getCodiceOrdine() {
		return codiceOrdine;
	}

	public void setCodiceOrdine(int codiceOrdine) {
		this.codiceOrdine = codiceOrdine;
	}

	public int getCodiceCliente() {
		return codiceCliente;
	}

	public void setCodiceCliente(int codiceCliente) {
		this.codiceCliente = codiceCliente;
	}

	public Date getDataOrdine() {
		return dataOrdine;
	}

	public void setDataOrdine(Date dataOrdine) {
		this.dataOrdine = dataOrdine;
	}

	public double getTotaleOrdineCalcolato() {
		return totaleOrdineCalcolato;
	}

	public void setTotaleOrdineCalcolato(double totaleOrdineCalcolato) {
		this.totaleOrdineCalcolato = totaleOrdineCalcolato;
	}

	public boolean getFatturato() {
		return fatturato;
	}

	public void setFatturato(boolean fatturato) {
		this.fatturato = fatturato;
	}

	@Override
	public String toString() {
		return "Ordine [codiceOrdine=" + codiceOrdine + ", codiceCliente=" + codiceCliente + ", dataOrdine="
				+ dataOrdine + ", totaleOrdineCalcolato=" + totaleOrdineCalcolato + "]";
	}

}
