package dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.PrezziProdotto;


public class PrezziProdottoDAO {

	public PrezziProdottoDAO() throws SQLException {
		super();
	}

	public List<PrezziProdotto> recuperaTutti() {

		Connection c = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			c = ConnessioneDB.getConnessione();
			st = c.createStatement();

			String sql = "SELECT * FROM prezzi_prodotto ORDER BY codice_prodotto ASC";
			rs = st.executeQuery(sql);

			List<PrezziProdotto> lista = new ArrayList<>();

			while (rs.next())
				lista.add(new PrezziProdotto(rs.getInt("codice_prodotto"), rs.getDate("data_inizio"),
						rs.getDate("data_fine"), rs.getDouble("prezzo"), rs.getDouble("iva")));

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


	

	public double recuperaPrezzoValidoAllaData(int codiceProdotto, Date dataRiferimento) throws SQLException {
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    
	    // Cerca il prezzo dove la data di riferimento è compresa tra data_inizio e data_fine (inclusi).
	    String sql = "SELECT prezzo FROM prezzi_prodotto WHERE codice_prodotto = ? AND ? BETWEEN data_inizio AND data_fine";

	    try {
	        conn = ConnessioneDB.getConnessione();
	        ps = conn.prepareStatement(sql);
	        
	        // Conversione da java.util.Date a java.sql.Date
	        ps.setInt(1, codiceProdotto);
	        java.sql.Date sqlDataRiferimento = new java.sql.Date(dataRiferimento.getTime());
	        ps.setDate(2,sqlDataRiferimento); 
	        
	        rs = ps.executeQuery();

	        if (rs.next()) {
	            return rs.getDouble("prezzo");
	        } else {
	            // Prezzo non trovato (nessun prezzo valido in quel periodo)
	            return -1.0; 
	        }
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (ps != null) ps.close();
	            if (conn != null) conn.close(); 
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	public PrezziProdotto recuperaUno(int codiceProdotto, Date dataInizio, Date dataFine) {

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			String sql = "SELECT * FROM prezzi_prodotto WHERE codice_prodotto = ? AND data_inizio = ? AND data_fine = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codiceProdotto);
			java.sql.Date sqlDataInizio = new java.sql.Date(dataInizio.getTime());
			ps.setDate(2, sqlDataInizio);
			java.sql.Date sqlDataFine = new java.sql.Date(dataFine.getTime());
			ps.setDate(3, sqlDataFine);

			rs = ps.executeQuery();
			if (rs.next()) {
				return new PrezziProdotto(rs.getInt("codice_prodotto"), rs.getDate("data_inizio"),
						rs.getDate("data_fine"), rs.getDouble("prezzo"), rs.getDouble("iva"));
			}

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

	public boolean aggiungi(PrezziProdotto p, Connection c) {

		PreparedStatement ps = null;
		ResultSet rs = null;

		java.sql.Date sqlDataInizio = new java.sql.Date(p.getDataInizio().getTime());
		java.sql.Date sqlDataFine = new java.sql.Date(p.getDataFine().getTime());

		try {

			String sqlCheck = "SELECT COUNT(*) FROM prezzi_prodotto WHERE " + "codice_prodotto = ? AND "
					+ "(? > data_inizio AND ? < data_fine)";

			ps = c.prepareStatement(sqlCheck);
			int i = 1;
			ps.setInt(i++, p.getCodiceProdotto());
			ps.setDate(i++, sqlDataFine);
			ps.setDate(i++, sqlDataInizio);
			

			rs = ps.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				return false;
			}

			ps.close();
			if (rs != null)
				rs.close();

			String sqlInsert = "INSERT INTO prezzi_prodotto (codice_prodotto, data_inizio, data_fine, prezzo, iva) VALUES (?, ?, ?, ?, ?)";
			ps = c.prepareStatement(sqlInsert);

			int j = 1;
			ps.setInt(j++, p.getCodiceProdotto());
			ps.setDate(j++, sqlDataInizio);
			ps.setDate(j++, sqlDataFine);
			ps.setDouble(j++, p.getPrezzo());
			ps.setDouble(j++, p.getIva());

			int righeAggiornate = ps.executeUpdate();
			return righeAggiornate != 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	public boolean modifica(PrezziProdotto p, Connection c) {

		PreparedStatement ps = null;

		try {

			String sql = "UPDATE prezzi_prodotto SET prezzo = ?, iva = ? "
					+ "WHERE codice_prodotto = ? AND data_inizio = ? AND data_fine = ?";

			ps = c.prepareStatement(sql);

			ps.setDouble(1, p.getPrezzo());
			ps.setDouble(2, p.getIva());

			java.sql.Date sqlDataInizio = new java.sql.Date(p.getDataInizio().getTime());
			java.sql.Date sqlDataFine = new java.sql.Date(p.getDataFine().getTime());

			ps.setInt(3, p.getCodiceProdotto());
			ps.setDate(4, sqlDataInizio);
			ps.setDate(5, sqlDataFine);

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

	public boolean cancella(int codiceProdotto, Date dataInizio, Date dataFine, Connection c) { 
        
        PreparedStatement ps = null;
        int righeCancellate = 0;

        java.sql.Date sqlDataInizio = new java.sql.Date(dataInizio.getTime());
        java.sql.Date sqlDataFine = new java.sql.Date(dataFine.getTime());

        try {
           
            String sql = "DELETE FROM prezzi_prodotto WHERE codice_prodotto = ? AND data_inizio = ? AND data_fine = ?";

            ps = c.prepareStatement(sql); 

            ps.setInt(1, codiceProdotto);
            ps.setDate(2, sqlDataInizio);
            ps.setDate(3, sqlDataFine);

            righeCancellate = ps.executeUpdate();

            return righeCancellate != 0;

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
	
	public double calcolaPrezzoFinale(Date dataRiferimento, int codiceProdotto, Connection c) {
		
		
		return 0.0;
		
	}
	
	
	
	
	
	
	
}