package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import entities.Fattura;

public class FatturaDAO {
    
    // Si basa sull'anno corrente del sistema
    private static final int ANNO_CORRENTE = java.time.Year.now().getValue(); 
    
    /** Recupera il prossimo progressivo per l'anno corrente (MAX + 1). */
    public int getNextProgressivo(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(progressivo), 0) + 1 FROM fattura WHERE anno = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ANNO_CORRENTE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    /** Inserisce la nuova fattura nel database, salvando il BLOB del PDF. */
    public boolean aggiungi(Fattura fattura, Connection conn) throws SQLException {
        String sql = "INSERT INTO fattura (anno, progressivo, codice_ordine, fattura) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fattura.getAnno());
            ps.setInt(2, fattura.getProgressivo());
            ps.setInt(3, fattura.getCodiceOrdine());
            ps.setBytes(4, fattura.getFileFattura()); // Imposta il contenuto PDF (BLOB)
            return ps.executeUpdate() > 0;
        }
    }
}