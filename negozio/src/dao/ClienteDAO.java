package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.Cliente;

public class ClienteDAO {

	private Statement st;

	public ClienteDAO() throws SQLException {

		super();

	}

	public List<Cliente> recuperaTutti() {
		Connection conn = ConnessioneDB.getConnessione();

		String sql = "SELECT * FROM cliente ORDER BY codice_cliente ASC";

		try {

			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			List<Cliente> lista = new ArrayList<Cliente>();

			while (rs.next())
				lista.add(new Cliente(rs.getInt("codice_cliente"), rs.getString("cognome"), rs.getString("nome"),
						rs.getDate("data_nascita"), rs.getString("email")));

			return lista;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
					System.out.println("Chiusa.");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return null;

	}

	public Cliente recuperaUno(int codice_cliente, Connection currentConn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			String sql = "SELECT codice_cliente, cognome, nome, data_nascita, email FROM cliente WHERE codice_cliente = ?";

			ps = currentConn.prepareStatement(sql);

			ps.setInt(1, codice_cliente);

			rs = ps.executeQuery();

			if (rs.next()) {
				return new Cliente(rs.getInt("codice_cliente"), rs.getString("cognome"), rs.getString("nome"),
						rs.getDate("data_nascita"), rs.getString("email"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return null;
	}

	public Cliente recuperaUno(int codice_cliente) {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			conn = ConnessioneDB.getConnessione();

			String sql = "SELECT codice_cliente, cognome, nome, data_nascita, email FROM cliente WHERE codice_cliente = ?";

			ps = conn.prepareStatement(sql);

			ps.setInt(1, codice_cliente);

			rs = ps.executeQuery();

			if (rs.next()) {
				return new Cliente(rs.getInt("codice_cliente"), rs.getString("cognome"), rs.getString("nome"),
						rs.getDate("data_nascita"), rs.getString("email"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return null;
	}

	public boolean inserisci(Cliente cliente, Connection conn) throws SQLException {
		String sql = "INSERT INTO cliente (cognome, nome, data_nascita, email) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, cliente.getCognome());
			ps.setString(2, cliente.getNome());
			ps.setDate(3, new java.sql.Date(cliente.getDataNascita().getTime()));
			ps.setString(4, cliente.getEmail());
			return ps.executeUpdate() > 0;
		}
	}

	public boolean modifica(Cliente cliente, Connection conn) throws SQLException {
		String sql = "UPDATE cliente SET cognome = ?, nome = ?, data_nascita = ?, email = ? WHERE codice_cliente = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, cliente.getCognome());
			ps.setString(2, cliente.getNome());
			ps.setDate(3, new java.sql.Date(cliente.getDataNascita().getTime()));
			ps.setString(4, cliente.getEmail());
			ps.setInt(5, cliente.getCodiceCliente());

			return ps.executeUpdate() > 0; // true se almeno una riga è stata modificata
		}
	}

	public int cancella(int codiceCliente, Connection conn) throws SQLException {

		String sqlDelete = "DELETE FROM cliente WHERE codice_cliente = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
			pstmt.setInt(1, codiceCliente);
			return pstmt.executeUpdate();
		}
	}

	public boolean clienteHaRiferimenti(int codiceCliente) throws SQLException {

		String sql = "SELECT ((SELECT COUNT(*) FROM ordine WHERE codice_cliente = ?) + (SELECT COUNT(*) FROM carta_fedelta WHERE codice_cliente = ?)) AS total_references";

		try (Connection conn = ConnessioneDB.getConnessione(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, codiceCliente);
			pstmt.setInt(2, codiceCliente);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("total_references") > 0;
			}
			return false;

		}
	}

}
