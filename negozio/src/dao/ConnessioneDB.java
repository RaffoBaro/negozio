package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface ConnessioneDB {

	public static Connection getConnessione() {
		
			String server = "localhost";
			int porta = 5432;
			
			String utente = "negozio";
			String password = "Maradona#10#";
			
			String nomeDB = "negozio";
			
			String url = "jdbc:postgresql://" + server + ":" + porta + "/" + nomeDB;
			
			Connection c = null;
			
			try {
				c = DriverManager.getConnection(url, utente, password);
			} catch (SQLException e) {
				System.out.println("Errore di connessione");
			}
			
			return c;
		
	}
}
