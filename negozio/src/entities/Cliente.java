package entities;

import java.util.Date;


public class Cliente {

	private int codice_cliente;
	private String cognome;
	private String nome;
	private Date data_nascita;
	private String email;

	public Cliente() {
		super();
	}

	public Cliente(int codice_cliente, String cognome, String nome, Date data_Nascita) {
		super();
		this.codice_cliente = codice_cliente;
		this.cognome = cognome;
		this.nome = nome;
		this.data_nascita = data_Nascita;
	}
	
	public Cliente(int codice_cliente, String cognome, String nome, Date data_Nascita, String email) {
		super();
		this.codice_cliente = codice_cliente;
		this.cognome = cognome;
		this.nome = nome;
		this.data_nascita = data_Nascita;
		this.email = email;
	}

	public Cliente(String cognome, String nome, Date dataNascitaDate, String email) {
		this.cognome = cognome;
		this.nome = nome;
		this.data_nascita = dataNascitaDate;
		this.email = email;
			}

	public int getCodiceCliente() {
		return codice_cliente;
	}

	public void setCodiceCliente(int codice_cliente) {
		this.codice_cliente = codice_cliente;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public Date getDataNascita() {
		return data_nascita;
	}

	public void setDataNascita(Date data_nascita) {
		this.data_nascita = data_nascita;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Cliente [codiceCliente=" + codice_cliente + ", nome=" + nome + ", cognome=" + cognome + ", dataNascita="
				+ data_nascita + "]";
	}

	
	

}
