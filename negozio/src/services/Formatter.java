package services;



public class Formatter {

    
    private static final int LARGHEZZA_COLONNA_PREDEFINITA = 25; 

    public static void stampaRigaFormattata(String... colonne) {
        StringBuilder stringaFormato = new StringBuilder();
        
        // Costruisce la stringa di formato dinamicamente in base al numero di colonne
        for (int i = 0; i < colonne.length; i++) {
            stringaFormato.append("%-").append(LARGHEZZA_COLONNA_PREDEFINITA).append("s");
        }
        stringaFormato.append("%n"); // Aggiunge un a capo alla fine

        System.out.printf(stringaFormato.toString(), (Object[]) colonne);
    }

	


}