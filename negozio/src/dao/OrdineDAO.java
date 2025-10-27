package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.Ordine;

public class OrdineDAO {

	public OrdineDAO() throws SQLException {
		super();
	}

	public List<Ordine> recuperaTutti() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		String sql = "SELECT * FROM ordine ORDER BY codice_ordine ASC";

		try {
			conn = ConnessioneDB.getConnessione();
			st = conn.createStatement();
			rs = st.executeQuery(sql);

			List<Ordine> lista = new ArrayList<Ordine>();

			while (rs.next())
				lista.add(new Ordine(rs.getInt("codice_ordine"), rs.getInt("codice_cliente"), rs.getDate("data_ordine"),
						rs.getDouble("totale_ordine_calcolato"),rs.getBoolean("fatturato") ));

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
	
	/** Recupera tutti gli ordini con fatturato = false e popola l'oggetto Ordine. */
    public List<Ordine> recuperaOrdiniDaFatturare(Connection conn) throws SQLException {
        List<Ordine> ordini = new ArrayList<>();
        // In una vera applicazione, si farebbe SELECT di tutti i campi
        String sql = "SELECT codice_ordine, data_ordine, totale_ordine_calcolato, codice_cliente, fatturato FROM ordine WHERE fatturato = false"; 
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Ordine o = new Ordine(
                    rs.getInt("codice_ordine"),
                    rs.getDate("data_ordine"),
                    rs.getDouble("totale_ordine_calcolato"),
                    rs.getInt("codice_cliente"),
                    rs.getBoolean("fatturato")
                );
                ordini.add(o);
            }
        }
        return ordini;
    }

    /** Aggiorna lo stato di fatturazione di un ordine a TRUE. */
    public boolean segnaOrdineComeFatturato(int codiceOrdine, Connection conn) throws SQLException {
        String sql = "UPDATE ordine SET fatturato = true WHERE codice_ordine = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codiceOrdine);
            return ps.executeUpdate() > 0;
        }
    }
	
	 
     // Recupera il codice del cliente associato a un ordine.
    public int recuperaCodiceCliente(int codiceOrdine, Connection conn) throws SQLException {
        String sql = "SELECT codice_cliente FROM ordine WHERE codice_ordine = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codiceOrdine);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("codice_cliente");
                }
                throw new SQLException("Codice cliente non trovato per l'ordine: " + codiceOrdine);
            }
        }
    }

	public Ordine recuperaUno(int codice_ordine) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ConnessioneDB.getConnessione();

			String sql = "SELECT * FROM ordine WHERE codice_ordine = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, codice_ordine);

			rs = ps.executeQuery();

			if (rs.next())
				return new Ordine(rs.getInt("codice_ordine"), rs.getInt("codice_cliente"), rs.getDate("data_ordine"),
						rs.getDouble("totale_ordine_calcolato"), rs.getBoolean("fatturato"));

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

	public boolean aggiungi(Ordine o, Connection conn) {
		java.sql.PreparedStatement ps = null;

		try {
			String sql = "INSERT INTO ordine (codice_ordine, codice_cliente, data_ordine, totale_ordine_calcolato) VALUES (nextval('ordine_codice_ordine_seq'), ?, ?, ?)";

			ps = conn.prepareStatement(sql);

			ps.setInt(1, o.getCodiceCliente());
			java.sql.Date sqlDataOrdine = new java.sql.Date(o.getDataOrdine().getTime());
			ps.setDate(2, sqlDataOrdine);
			ps.setDouble(3, o.getTotaleOrdineCalcolato());

			return ps.executeUpdate() != 0;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (java.sql.SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public boolean modifica(Ordine o, Connection conn) {
		java.sql.PreparedStatement ps = null;

		try {
			System.out.println(o);

			String sql = "UPDATE ordine SET codice_cliente = ?, data_ordine = ?, totale_ordine_calcolato = ? WHERE codice_ordine = ?";

			ps = conn.prepareStatement(sql);

			ps.setInt(1, o.getCodiceCliente());
			java.sql.Date sqlDataOrdine = new java.sql.Date(o.getDataOrdine().getTime());
			ps.setDate(2, sqlDataOrdine);
			ps.setDouble(3, o.getTotaleOrdineCalcolato());
			ps.setInt(4, o.getCodiceOrdine());

			return ps.executeUpdate() != 0;

		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (java.sql.SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public boolean cancella(int codice_ordine, Connection c) {

		PreparedStatement ps = null;

		try {

			String sql = "DELETE from ordine WHERE codice_ordine = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_ordine);

			return ps.executeUpdate() != 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isOrdineReferenziato(int codiceOrdine) {

		String sql = "SELECT COUNT(*) FROM dettaglio_ordine WHERE codice_ordine = ?";

		try (Connection conn = ConnessioneDB.getConnessione(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, codiceOrdine);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("Errore durante la verifica referenze ordine: " + e.getMessage());
		}
		return false;
	}

	public boolean ricalcolaTotaleOrdine(int codiceOrdine, Connection conn) throws SQLException {
		PreparedStatement ps = null;

		// Query SQL per aggiornare il totale dell'ordine sommando i totali riga dei
		// dettagli
		String sql = " UPDATE ordine SET totale_ordine_calcolato = ( "
				+ "    SELECT COALESCE(SUM(totale_riga_calcolato), 0) " + "    FROM dettaglio_ordine "
				+ " WHERE codice_ordine = ? " + " ) WHERE codice_ordine = ? ";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, codiceOrdine);
			ps.setInt(2, codiceOrdine);

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}
}