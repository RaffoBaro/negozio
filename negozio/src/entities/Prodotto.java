package entities;

public class Prodotto {

	private int codice_prodotto;
	private String descrizione;
	private int codice_reparto;
	private int quantita;
	

	public Prodotto() {
		super();
	}

	public Prodotto(int codiceProdotto, String descrizione, int codiceReparto, int quantita) {
		super();
		this.codice_prodotto = codiceProdotto;
		this.descrizione = descrizione;
		this.codice_reparto = codiceReparto;
		this.quantita = quantita;
	}

	public Prodotto(String descrizione, int codiceReparto, int quantita) {
		this.descrizione = descrizione;
		this.codice_reparto = codiceReparto;
		this.quantita = quantita;	}

	public int getCodiceProdotto() {
		return codice_prodotto;
	}

	public void setCodiceProdotto(int codiceProdotto) {
		this.codice_prodotto = codiceProdotto;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public int getCodiceReparto() {
		return codice_reparto;
	}

	public void setCodiceReparto(int codice_reparto) {
		this.codice_reparto = codice_reparto;
	}

	public int getQuantita() {
		return quantita;
	}

	public void setQuantita(int quantita) {
		this.quantita = quantita;
	}

	@Override
	public String toString() {
		return "Prodotto [codiceProdotto=" + codice_prodotto + ", descrizione=" + descrizione + ", codiceReparto="
				+ codice_reparto + ", quantita=" + quantita + "]";
	}

}
