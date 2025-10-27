package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import entities.Configurazione;

public class ConfigurazioneDAO {

	public ConfigurazioneDAO() throws SQLException {
		super();

	}

	public Map<String, String> recuperaTutti() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "SELECT chiave, valore FROM configurazione";
		Map<String, String> configurazioni = new HashMap<>();

		try {
			conn = ConnessioneDB.getConnessione();
			st = conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				configurazioni.put(rs.getString("chiave"), rs.getString("valore"));
			}
			return configurazioni;

		} catch (SQLException e) {
			System.err.println("Errore SQL: Impossibile recuperare tutte le configurazioni in Map.");
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new HashMap<>(); // Restituisce una Map vuota in caso di errore, non null
	}

	public Configurazione recuperaUno(String chiave) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ConnessioneDB.getConnessione();
			String sql = "SELECT chiave, valore FROM configurazione WHERE chiave = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, chiave);
			rs = ps.executeQuery();

			if (rs.next()) {
				return new Configurazione(rs.getString("chiave"), rs.getString("valore"));
			}

		} catch (SQLException e) {
			System.err.println("Errore SQL durante recuperaUno: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return null;
	}

	

    public Map<String, String> recuperaConfigurazioniEmail(Connection conn) throws SQLException {
        
 
        String sql = "SELECT chiave, valore FROM configurazione WHERE chiave IN (?, ?, ?, ?)";
        
        Map<String, String> configMap = new HashMap<>();
        
        // Uso try-with-resources per chiudere PreparedStatement e ResultSet
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "SMTP_HOST");
            ps.setString(2, "SMTP_PORT");
            ps.setString(3, "EMAIL_SENDER_USERNAME");
            ps.setString(4, "EMAIL_SENDER_PASSWORD");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    configMap.put(rs.getString("chiave"), rs.getString("valore"));
                }
            }
        }
        
        // Verifica se mancano parametri essenziali
        if (configMap.size() < 4) {
             // Lancio l'eccezione per bloccare il batch se la configurazione è incompleta
            throw new SQLException("Configurazione SMTP incompleta nel DB. Trovati solo " + configMap.size() + " parametri necessari.");
        }
        
        return configMap;
    }

	
	// Recupera solo il valore String di una chiave
	public String recuperaValorePerChiave(String chiave) {
		Configurazione config = recuperaUno(chiave);
		return (config != null) ? config.getValore() : null;
	}

	public boolean inserisci(Configurazione config, Connection conn) throws SQLException {
		String sql = "INSERT INTO configurazione (chiave, valore) VALUES (?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, config.getChiave());
			ps.setString(2, config.getValore());
			return ps.executeUpdate() > 0;
		}
	}

	public boolean modifica(Configurazione config, Connection conn) throws SQLException {
		String sql = "UPDATE configurazione SET valore = ? WHERE chiave = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, config.getValore());
			ps.setString(2, config.getChiave());
			return ps.executeUpdate() > 0;
		}
	}

	public int cancella(String chiave, Connection conn) throws SQLException {
		String sqlDelete = "DELETE FROM configurazione WHERE chiave = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
			pstmt.setString(1, chiave);
			return pstmt.executeUpdate();
		}
	}
}