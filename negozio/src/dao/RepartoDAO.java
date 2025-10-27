package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.Reparto;

public class RepartoDAO {

	public RepartoDAO() {
		super();
	}

	public List<Reparto> recuperaTutti() {

		Connection c = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			st = c.createStatement();
			String sql = "SELECT * FROM reparto ORDER BY codice_reparto ASC";

			rs = st.executeQuery(sql);

			List<Reparto> lista = new ArrayList<>();

			while (rs.next())
				lista.add(new Reparto(rs.getInt("codice_reparto"), rs.getString("descrizione")));

			return lista;
		} catch (SQLException e) {
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

	public Reparto recuperaUno(int codice_reparto) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = ConnessioneDB.getConnessione();
			String sql = "SELECT * FROM reparto WHERE codice_reparto = ?";
			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_reparto);
			rs = ps.executeQuery();

			if (rs.next())
				return new Reparto(rs.getInt("codice_reparto"), rs.getString("descrizione"));

		} catch (SQLException e) {
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

	public boolean aggiungi(Reparto r, Connection c) {
		PreparedStatement ps = null;

		try {

			String sql = "INSERT INTO reparto (descrizione) VALUES(?)";

			ps = c.prepareStatement(sql);
			ps.setString(1, r.getDescrizione());

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

	public boolean modifica(Reparto r, Connection c) {
		PreparedStatement ps = null;

		try {

			String sql = "UPDATE reparto SET descrizione = ? WHERE codice_reparto = ?";

			ps = c.prepareStatement(sql);

			ps.setString(1, r.getDescrizione());
			ps.setInt(2, r.getCodiceReparto());

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

	public boolean cancella(int codice_reparto, Connection c) {
		PreparedStatement ps = null;

		try {

			String sql = "DELETE from reparto WHERE codice_reparto = ?";

			ps = c.prepareStatement(sql);
			ps.setInt(1, codice_reparto);

			return ps.executeUpdate() != 0;

		} catch (SQLException e) {
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

	public boolean isRepartoReferenziato(int codiceReparto) {

		String sql = "SELECT COUNT(*) FROM prodotto WHERE codice_reparto = ?";

		try (Connection conn = ConnessioneDB.getConnessione(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, codiceReparto);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("Errore durante la verifica referenze reparto: " + e.getMessage());
		}
		return false;
	}
}