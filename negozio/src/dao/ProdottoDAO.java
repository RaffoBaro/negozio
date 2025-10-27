package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.Prodotto;


public class ProdottoDAO {

	public ProdottoDAO() {
		super();
	}

	public List<Prodotto> recuperaTutti() {

		Connection c = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			st = c.createStatement();
			String sql = "SELECT * FROM prodotto ORDER BY codice_prodotto ASC";

			rs = st.executeQuery(sql);
			List<Prodotto> lista = new ArrayList<>();

			while (rs.next())
				lista.add(new Prodotto(rs.getInt("codice_prodotto"), rs.getString("descrizione"),
						rs.getInt("codice_reparto"), rs.getInt("quantita")));

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

	public Prodotto recuperaUno(int codice_prodotto) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			String sql = "SELECT * FROM prodotto WHERE codice_prodotto = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_prodotto);
			rs = ps.executeQuery();

			if (rs.next())
				return new Prodotto(rs.getInt("codice_prodotto"), rs.getString("descrizione"),
						rs.getInt("codice_reparto"), rs.getInt("quantita"));
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

	public boolean esisteProdotto(int codiceProdotto) {

		String sql = "SELECT COUNT(*) FROM prodotto WHERE codice_prodotto = ?";

		try (Connection c = ConnessioneDB.getConnessione(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setInt(1, codiceProdotto);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Prodotto recuperaUnoInReparto(int codice_reparto) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			String sql = "SELECT * FROM prodotto WHERE codice_reparto = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_reparto);
			rs = ps.executeQuery();

			if (rs.next())
			
				return new Prodotto(rs.getInt("codice_prodotto"), rs.getString("descrizione"),
						rs.getInt("codice_reparto"), rs.getInt("quantita"));

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

	public boolean aggiungi(Prodotto p, Connection c) {

		PreparedStatement ps = null;

		try {

			String sql = "INSERT INTO prodotto (codice_prodotto, descrizione, codice_reparto, quantita) VALUES(nextval('prodotto_codice_prodotto_seq'), ?, ?, ?)";

			ps = c.prepareStatement(sql);

			ps.setString(1, p.getDescrizione());
			ps.setInt(2, p.getCodiceReparto());
			ps.setInt(3, p.getQuantita());

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

	public boolean modifica(Prodotto p, Connection c) {

		PreparedStatement ps = null;

		try {

			

			String sql = "UPDATE prodotto SET descrizione = ?, codice_reparto = ?, quantita = ? WHERE codice_prodotto = ?";

			ps = c.prepareStatement(sql);

			ps.setString(1, p.getDescrizione());
			ps.setInt(2, p.getCodiceReparto());
			ps.setInt(3, p.getQuantita());
			ps.setInt(4, p.getCodiceProdotto());

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

	public boolean cancella(int codice_prodotto, Connection c) {

		PreparedStatement ps = null;

		try {

			String sql = "DELETE from prodotto WHERE codice_prodotto = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_prodotto);

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

	public boolean isProdottoReferenziato(int codiceProdotto) {
		String sql = "SELECT COUNT(*) FROM prezzi_prodotto WHERE codice_prodotto = ?";

		try (Connection conn = ConnessioneDB.getConnessione(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, codiceProdotto);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("Errore durante la verifica referenze: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Questo metodo Aggiorna la quantità disponibile del prodotto.
	 */
	public boolean sottraiQuantita(int codiceProdotto, int quantitaDaSottrarre, Connection conn) throws SQLException {
		PreparedStatement ps = null;

		String sql = "UPDATE prodotto SET quantita = quantita - ? WHERE codice_prodotto = ?";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, quantitaDaSottrarre);
			ps.setInt(2, codiceProdotto);

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public boolean ripristinaQuantita(int codiceProdotto, int quantitaDaRipristinare, Connection conn)
			throws SQLException {
		PreparedStatement ps = null;

		String sql = "UPDATE prodotto SET quantita = quantita + ? WHERE codice_prodotto = ?";

		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, quantitaDaRipristinare);
			ps.setInt(2, codiceProdotto);

			return ps.executeUpdate() > 0;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

}