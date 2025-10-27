package entities;

public class CartaFedelta {

	private String codice_carta;
	private int codice_cliente;
	private int punti;

	public CartaFedelta() {
		super();
	}

	public CartaFedelta(String codiceCarta, int codiceCliente, int punti) {
		super();
		this.codice_carta = codiceCarta;
		this.codice_cliente = codiceCliente;
		this.punti = punti;
	}

	public String getCodiceCarta() {
		return codice_carta;
	}

	public void setCodiceCarta(String codiceCarta) {
		this.codice_carta = codiceCarta;
	}

	public int getCodiceCliente() {
		return codice_cliente;
	}

	public void setCodiceCliente(int codiceCliente) {
		this.codice_cliente = codiceCliente;
	}

	public int getPunti() {
		return punti;
	}

	public void setPunti(int punti) {
		this.punti = punti;
	}

	@Override
	public String toString() {
		return "CartaFedelta [codiceCarta=" + codice_carta + ", codiceCliente=" + codice_cliente + ", punti=" + punti
				+ "]";
	}

}
