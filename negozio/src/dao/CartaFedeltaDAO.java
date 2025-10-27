package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.CartaFedelta;


public class CartaFedeltaDAO {

	public CartaFedeltaDAO() throws SQLException {
		super();
	}

	public List<CartaFedelta> recuperaTutti() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		String sql = "SELECT * FROM carta_fedelta ORDER BY codice_carta ASC";

		try {
			conn = ConnessioneDB.getConnessione();
			st = conn.createStatement();
			rs = st.executeQuery(sql);

			List<CartaFedelta> lista = new ArrayList<>();

			while (rs.next())
				lista.add(new CartaFedelta(rs.getString("codice_carta"), rs.getInt("codice_cliente"),
						rs.getInt("punti")));

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
	
	 /**
     * Recupera la Carta Fedeltà attiva per un dato cliente.
     */
    public CartaFedelta recuperaCartaFedelta(int codiceCliente, Connection conn) throws SQLException {
        
        String sql = "SELECT codice_carta FROM carta_fedelta WHERE codice_cliente = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, codiceCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Crea e restituisce l'oggetto CartaFedelta solo con i dati necessari
                    String codiceCarta = rs.getString("codice_carta");
                    return new CartaFedelta(codiceCarta, codiceCliente, 0); // Punti a zero per semplicità
                }
                return null; // Nessuna carta fedeltà trovata
            }
        }
    }

	public CartaFedelta recuperaUno(String codiceCarta) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT codice_carta, codice_cliente, punti FROM carta_fedelta WHERE codice_carta = ?";

		try {
			conn = ConnessioneDB.getConnessione();
			ps = conn.prepareStatement(sql);
			ps.setString(1, codiceCarta);

			rs = ps.executeQuery();

			if (rs.next()) {
				return new CartaFedelta(rs.getString("codice_carta"), rs.getInt("codice_cliente"), rs.getInt("punti"));
			}

		} catch (SQLException e) {
			System.err.println("Errore SQL durante la ricerca della carta: " + e.getMessage());
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

	public boolean aggiungi(CartaFedelta carta, Connection conn) {
		PreparedStatement ps = null;

		try {
			String sql = "INSERT INTO carta_fedelta (codice_carta, codice_cliente, punti) VALUES(?, ?, ?)";

			ps = conn.prepareStatement(sql);

			ps.setString(1, carta.getCodiceCarta());
			ps.setInt(2, carta.getCodiceCliente());
			ps.setInt(3, carta.getPunti());

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

	public boolean modifica(CartaFedelta carta, Connection conn) {
		PreparedStatement ps = null;

		try {
			String sql = "UPDATE carta_fedelta SET codice_carta = ?, codice_cliente = ?, punti = ? WHERE codice_carta = ?";

			ps = conn.prepareStatement(sql);

			ps.setString(1, carta.getCodiceCarta()); // Nuovo codice carta
			ps.setInt(2, carta.getCodiceCliente());
			ps.setInt(3, carta.getPunti());
			ps.setString(4, carta.getCodiceCarta());

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

	public boolean cancella(String codice_carta, Connection conn) {
		PreparedStatement ps = null;

		try {
			String sql = "DELETE FROM carta_fedelta WHERE codice_carta = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, codice_carta);

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
}