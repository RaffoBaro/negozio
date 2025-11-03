package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import entities.Sconti;

public class ScontiDAO {

	public ScontiDAO() {
		super();
	}

	public List<Sconti> recuperaTutti() {

		Connection c = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			st = c.createStatement();

			String sql = "SELECT * FROM sconti ORDER BY codice_sconto ASC";

			rs = st.executeQuery(sql);

			List<Sconti> lista = new ArrayList<>();

			while (rs.next())
				lista.add(new Sconti(rs.getInt("codice_sconto"), rs.getString("codice_carta"),
						rs.getDate("data_inizio"), rs.getDate("data_fine"), rs.getInt("sconto")));

			return lista;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (c != null)
					c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public Sconti recuperaUnoCodiceCarta(String codice_carta, Date dataRiferimento) {
	    Connection c = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;

	    try {
	        c = ConnessioneDB.getConnessione();
	        
	        // **QUERY MODIFICATA** per includere il filtro data
	        String sql = "SELECT * FROM sconti WHERE codice_carta = ? "
	                   + "AND data_inizio <= ? "
	                   + "AND COALESCE(data_fine, '9999-12-31') >= ?";
	        
	        ps = c.prepareStatement(sql);
	        
	        // Conversione della data java.util.Date in java.sql.Date
	        java.sql.Date sqlDataRiferimento = new java.sql.Date(dataRiferimento.getTime());

	        // 1. codice_carta
	        ps.setString(1, codice_carta); 
	        
	        // 2. data_inizio (la data di riferimento deve essere MAGGIORE o UGUALE alla data di inizio)
	        ps.setDate(2, sqlDataRiferimento); 
	        
	        // 3. data_fine (la data di riferimento deve essere MINORE o UGUALE alla data di fine)
	        //    COALESCE gestisce i casi in cui data_fine è NULL (sconto valido indefinitamente)
	        ps.setDate(3, sqlDataRiferimento); 

	        rs = ps.executeQuery();

	        if (rs.next()) {
	            // ... (Restituisce l'oggetto Sconti trovato)
	            return new Sconti(
	                rs.getInt("codice_sconto"), 
	                rs.getString("codice_carta"), 
	                rs.getDate("data_inizio"), 
	                rs.getDate("data_fine"), 
	                rs.getInt("sconto")
	            );
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        // ... (Blocco finally per chiudere rs, ps, c)
	        if (rs != null) try { rs.close(); } catch (SQLException se) { se.printStackTrace(); }
	        if (ps != null) try { ps.close(); } catch (SQLException se) { se.printStackTrace(); }
	        if (c != null) try { c.close(); } catch (SQLException se) { se.printStackTrace(); }
	    }
	    return null;
	}
	

	public Sconti recuperaUno(int codice_sconto) {

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();

			String sql = "SELECT * FROM sconti WHERE codice_sconto = ?";
			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_sconto);

			rs = ps.executeQuery();

			if (rs.next())
				return new Sconti(rs.getInt("codice_sconto"), rs.getString("codice_carta"), rs.getDate("data_inizio"),
						rs.getDate("data_fine"), rs.getInt("sconto"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public boolean aggiungi(Sconti s, Connection c) {

		PreparedStatement ps = null;

		// Conversione delle date java.util.Date in java.sql.Date
		java.sql.Date sqlDataInizio = new java.sql.Date(s.getDataInizio().getTime());
		java.sql.Date sqlDataFine = new java.sql.Date(s.getDataFine().getTime());

		try {

			String sqlInsert = "INSERT INTO sconti (codice_sconto, codice_carta, data_inizio, data_fine, sconto) VALUES (nextval('sconti_codice_sconto_seq'), ?, ?, ?, ?)";
			ps = c.prepareStatement(sqlInsert);

			int j = 1;
			ps.setString(j++, s.getCodiceCarta());
			ps.setDate(j++, sqlDataInizio);
			ps.setDate(j++, sqlDataFine);
			ps.setDouble(j++, s.getSconto());

			int righeAggiornate = ps.executeUpdate();
			return righeAggiornate != 0;

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

	public boolean modifica(Sconti s, Connection c) {

		PreparedStatement ps = null;

		try {

			String sql = "UPDATE sconti SET codice_carta = ?, data_inizio = ?, data_fine = ?, sconto = ? WHERE codice_sconto = ?";

			ps = c.prepareStatement(sql);

			// Conversione delle date java.util.Date in java.sql.Date
			java.sql.Date sqlDataInizio = new java.sql.Date(s.getDataInizio().getTime());
			java.sql.Date sqlDataFine = new java.sql.Date(s.getDataFine().getTime());

			ps.setString(1, s.getCodiceCarta());
			ps.setDate(2, sqlDataInizio);
			ps.setDate(3, sqlDataFine);
			ps.setInt(4, s.getSconto());
			ps.setInt(5, s.getCodiceSconto());

			int righeAggiornate = ps.executeUpdate();
			return righeAggiornate != 0;
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

	public boolean cancella(int codice_sconto, Connection c) {

		PreparedStatement ps = null;

		try {

			String sql = "DELETE from sconti WHERE codice_sconto = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_sconto);

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


}