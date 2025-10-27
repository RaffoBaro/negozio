package entities;

public class Configurazione {
	// String
	private String chiave;
	private String valore;

	public Configurazione(String chiave, String valore) {
		super();
		this.chiave = chiave;
		this.valore = valore;
	}

	public String getChiave() {
		return chiave;
	}

	public void setChiave(String chiave) {
		this.chiave = chiave;
	}

	public String getValore() {
		return valore;
	}

	public void setValore(String valore) {
		this.valore = valore;
	}

	@Override
	public String toString() {
		return "Configurazione [chiave=" + chiave + ", valore=" + valore + "]";
	}

}
