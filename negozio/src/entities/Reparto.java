package entities;

public class Reparto {

	private int codice_reparto;
	private String descrizione;

	public Reparto() {
		super();
	}

	public Reparto(String descrizione) {
		super();
		this.descrizione = descrizione;
	}

	public Reparto(String descrizione, int codice_reparto) {
		super();
		this.descrizione = descrizione;
		this.codice_reparto = codice_reparto;
	}

	public Reparto(int codice_reparto, String descrizione) {
		super();
		this.codice_reparto = codice_reparto;
		this.descrizione = descrizione;
	}

	public int getCodiceReparto() {
		return codice_reparto;
	}

	public void setCodiceReparto(int codice_reparto) {
		this.codice_reparto = codice_reparto;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	@Override
	public String toString() {
		return "Reparto [codiceReparto=" + codice_reparto + ", descrizione=" + descrizione + "]";
	}


}








