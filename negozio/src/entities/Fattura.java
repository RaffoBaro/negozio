package entities;

// Nota: il PDF è gestito come array di byte (byte[])
public class Fattura {
    private int anno;
    private int progressivo;
    private int codiceOrdine;
    private byte[] fileFattura; 

    // Costruttore completo
    public Fattura(int anno, int progressivo, int codiceOrdine, byte[] fileFattura) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.codiceOrdine = codiceOrdine;
        this.fileFattura = fileFattura;
    }

    // Getter e Setter
    public int getAnno() { return anno; }
    public void setAnno(int anno) { this.anno = anno; }
    
    public int getProgressivo() { return progressivo; }
    public void setProgressivo(int progressivo) { this.progressivo = progressivo; }
    
    public int getCodiceOrdine() { return codiceOrdine; }
    public void setCodiceOrdine(int codiceOrdine) { this.codiceOrdine = codiceOrdine; }
    
    public byte[] getFileFattura() { return fileFattura; }
    public void setFileFattura(byte[] fileFattura) { this.fileFattura = fileFattura; }
}
