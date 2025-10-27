package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.DettaglioOrdine;

public class DettaglioOrdineDAO {

	public List<DettaglioOrdine> recuperaTutti() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		List<DettaglioOrdine> lista = new ArrayList<>();
		String sql = "SELECT * FROM dettaglio_ordine ORDER BY codice_ordine, progressivo ASC";

		try {
			conn = ConnessioneDB.getConnessione();
			st = conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next())
				lista.add(new DettaglioOrdine(rs.getInt("codice_ordine"), rs.getInt("progressivo"),
						rs.getInt("codice_prodotto"), rs.getInt("quantita_ordinata"),
						rs.getDouble("totale_riga_calcolato")));
			return lista;
		} catch (SQLException e) {
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
		return null;
	}
	
	// --- METODO AGGIUNTO PER IL BATCH DI FATTURAZIONE ---
    /**
     * Recupera tutti i DettagliOrdine associati a un specifico CodiceOrdine.
     * Non chiude la Connection.
     * * @param codiceOrdine Il codice dell'Ordine.
     * @param conn La connessione al database.
     * @return Una List di DettaglioOrdine.
     */
    public List<DettaglioOrdine> recuperaDettagliPerOrdine(int codiceOrdine, Connection conn) throws SQLException {
        List<DettaglioOrdine> dettagli = new ArrayList<>();
        
        // Selezioniamo tutti i campi necessari per ricostruire l'entità DettaglioOrdine
        String sql = "SELECT codice_ordine, progressivo, codice_prodotto, quantita_ordinata, totale_riga_calcolato " + 
                     "FROM dettaglio_ordine WHERE codice_ordine = ? ORDER BY progressivo ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codiceOrdine);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dettagli.add(new DettaglioOrdine(
                        rs.getInt("codice_ordine"), 
                        rs.getInt("progressivo"),
                        rs.getInt("codice_prodotto"), 
                        rs.getInt("quantita_ordinata"),
                        rs.getDouble("totale_riga_calcolato")
                    ));
                }
            }
        }
        return dettagli;
    }

	public DettaglioOrdine recuperaUno(int codiceOrdine, int progressivo) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT * FROM dettaglio_ordine WHERE codice_ordine = ? AND progressivo = ?";

		try {
			conn = ConnessioneDB.getConnessione();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, codiceOrdine);
			ps.setInt(2, progressivo);

			rs = ps.executeQuery();

			if (rs.next()) {
				return new DettaglioOrdine(rs.getInt("codice_ordine"), rs.getInt("progressivo"),
						rs.getInt("codice_prodotto"), rs.getInt("quantita_ordinata"),
						rs.getDouble("totale_riga_calcolato"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean aggiungi(DettaglioOrdine dettOrdine, Connection conn) throws SQLException {
		PreparedStatement ps = null;

		String sql = "INSERT INTO dettaglio_ordine (codice_ordine, progressivo, codice_prodotto, quantita_ordinata, totale_riga_calcolato) VALUES(?, ?, ?, ?, ?)";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, dettOrdine.getCodiceOrdine());
			ps.setInt(2, dettOrdine.getProgressivo());
			ps.setInt(3, dettOrdine.getCodiceProdotto());
			ps.setInt(4, dettOrdine.getQuantitaOrdinata());
			ps.setDouble(5, dettOrdine.getTotaleRigaCalcolato());

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public boolean modifica(DettaglioOrdine dettOrd, Connection conn) throws SQLException {
		PreparedStatement ps = null;

		String sql = "UPDATE dettaglio_ordine SET codice_prodotto = ?, quantita_ordinata = ?, totale_riga_calcolato = ? WHERE codice_ordine = ? AND progressivo = ?";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, dettOrd.getCodiceProdotto());
			ps.setInt(2, dettOrd.getQuantitaOrdinata());
			ps.setDouble(3, dettOrd.getTotaleRigaCalcolato());
			ps.setInt(4, dettOrd.getCodiceOrdine());
			ps.setInt(5, dettOrd.getProgressivo());

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public boolean cancella(int codiceOrdine, int progressivo, Connection conn) throws SQLException {
		PreparedStatement ps = null;
		String sql = "DELETE FROM dettaglio_ordine WHERE codice_ordine = ? AND progressivo = ?";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, codiceOrdine);
			ps.setInt(2, progressivo);

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public boolean cancellaReferenziatiOrdine(int codiceOrdine, Connection conn) throws SQLException {
		PreparedStatement ps = null;
		String sql = "DELETE FROM dettaglio_ordine WHERE codice_ordine = ?";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, codiceOrdine);

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public boolean calcolaTotaleRiga(int codiceOrdine, int progressivo, int codiceProdotto, java.util.Date dataOrdine,
			Connection conn, double moltiplicatoreSconto) throws SQLException {

		// ... (La query SQL deve essere quella corretta sopra)

		String sql = "UPDATE DETTAGLIO_ORDINE d "
				+ "SET totale_riga_calcolato = d.quantita_ordinata * pp.prezzo * (1 + pp.iva / 100.0) * ? "
				+ "FROM PREZZI_PRODOTTO pp " + "WHERE pp.codice_prodotto = ?" + "  AND pp.data_inizio <= ? "
				+ "  AND COALESCE(pp.data_fine, '9999-12-31') >= ? " + "  AND d.codice_ordine = ? "
				+ "  AND d.progressivo = ?";

		PreparedStatement ps = null;
		java.sql.Date sqlDataOrdine = new java.sql.Date(dataOrdine.getTime());

		try {
			ps = conn.prepareStatement(sql);

			ps.setDouble(1, moltiplicatoreSconto);
			ps.setInt(2, codiceProdotto);
			ps.setDate(3, sqlDataOrdine);
			ps.setDate(4, sqlDataOrdine);
			ps.setInt(5, codiceOrdine);
			ps.setInt(6, progressivo);

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		}
	}

	public boolean calcolaTotaleRiga(int codiceOrdine, int progressivo, int codiceProdotto, java.util.Date dataOrdine,
			Connection conn) throws SQLException {

		String sql = "UPDATE DETTAGLIO_ORDINE d "
				+ "SET totale_riga_calcolato = d.quantita_ordinata * pp.prezzo * (1 + pp.iva / 100.0) "
				+ "FROM PREZZI_PRODOTTO pp " + "WHERE pp.codice_prodotto = d.codice_prodotto "
				+ "  AND pp.codice_prodotto = ? " + "  AND pp.data_inizio <= ? "
				+ "  AND COALESCE(pp.data_fine, '9999-12-31') >= ? "
				+ "  AND d.codice_ordine = ? AND d.progressivo = ?";

		PreparedStatement ps = null;

		// Converto java.util.Date in java.sql.Date
		java.sql.Date sqlDataOrdine = new java.sql.Date(dataOrdine.getTime());

		try {
			ps = conn.prepareStatement(sql);

			ps.setInt(1, codiceProdotto);

			ps.setDate(2, sqlDataOrdine);
			ps.setDate(3, sqlDataOrdine);

			ps.setInt(4, codiceOrdine);
			ps.setInt(5, progressivo);

			return ps.executeUpdate() > 0;

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException se) {

				}
			}
		}
	}

	public int getNextProgressivo(int codiceOrdine, Connection conn) throws SQLException {

		String sql = "SELECT COALESCE(MAX(progressivo), 0) + 1 AS next_progressivo FROM dettaglio_ordine WHERE codice_ordine = ?";

		int nextProgressivo = 1; // Valore di default in caso di errore (se la query dovesse fallire)

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, codiceOrdine);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {

					nextProgressivo = rs.getInt("next_progressivo");
				}
			}

		}

		return nextProgressivo;
	}

}