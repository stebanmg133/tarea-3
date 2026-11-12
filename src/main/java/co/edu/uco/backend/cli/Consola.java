package co.edu.uco.backend.cli;

import java.util.ArrayList;
import java.util.List;

/** Utilidades de impresión de la consola interna (tablas y colores). */
final class Consola {

    private static final boolean COLOR = System.getenv("NO_COLOR") == null && System.console() != null;

    private static final String RESET = "\u001B[0m";
    private static final String NEGRITA = "\u001B[1m";
    private static final String TENUE = "\u001B[2m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARILLO = "\u001B[33m";
    private static final String ROJO = "\u001B[31m";
    private static final String CIAN = "\u001B[36m";

    private static final int ANCHO_MAXIMO = 34;
    private static final int FILAS_VISIBLES = 40;

    private Consola() {
    }

    static String negrita(String texto) {
        return conColor(NEGRITA, texto);
    }

    static String tenue(String texto) {
        return conColor(TENUE, texto);
    }

    static String cian(String texto) {
        return conColor(CIAN, texto);
    }

    static String verde(String texto) {
        return conColor(VERDE, texto);
    }

    static String amarillo(String texto) {
        return conColor(AMARILLO, texto);
    }

    static String rojo(String texto) {
        return conColor(ROJO, texto);
    }

    static void titulo(String texto) {
        System.out.println();
        System.out.println(negrita("── " + texto + " " + "─".repeat(Math.max(0, 46 - texto.length()))));
    }

    /** Imprime una tabla simple alineada. */
    static void tabla(List<String> cabeceras, List<List<String>> filas) {
        if (filas.isEmpty()) {
            System.out.println(tenue("  (sin resultados)"));
            return;
        }

        List<Integer> anchos = new ArrayList<>();
        for (int i = 0; i < cabeceras.size(); i++) {
            int ancho = cabeceras.get(i).length();
            for (List<String> fila : filas) {
                ancho = Math.max(ancho, texto(fila, i).length());
            }
            anchos.add(Math.min(ancho, ANCHO_MAXIMO));
        }

        System.out.println(tenue(fila(cabeceras, anchos)));
        System.out.println(tenue("  " + "─".repeat(anchos.stream().mapToInt(Integer::intValue).sum()
                + 2 * (anchos.size() - 1))));

        int limite = Math.min(filas.size(), FILAS_VISIBLES);
        for (int i = 0; i < limite; i++) {
            System.out.println(fila(filas.get(i), anchos));
        }
        if (filas.size() > limite) {
            System.out.println(tenue("  … " + (filas.size() - limite) + " fila(s) más"));
        }
    }

    static void ok(String mensaje) {
        System.out.println(verde("✓ " + mensaje));
    }

    static void aviso(String mensaje) {
        System.out.println(amarillo("! " + mensaje));
    }

    static void error(String mensaje) {
        System.out.println(rojo("✗ " + mensaje));
    }

    private static String fila(List<String> celdas, List<Integer> anchos) {
        StringBuilder linea = new StringBuilder("  ");
        for (int i = 0; i < anchos.size(); i++) {
            String celda = recortar(texto(celdas, i), anchos.get(i));
            linea.append(celda).append(" ".repeat(anchos.get(i) - celda.length()));
            if (i < anchos.size() - 1) {
                linea.append("  ");
            }
        }
        return linea.toString();
    }

    private static String texto(List<String> celdas, int indice) {
        if (indice >= celdas.size() || celdas.get(indice) == null) {
            return "—";
        }
        return celdas.get(indice).replace('\n', ' ').replace('\r', ' ');
    }

    private static String recortar(String texto, int ancho) {
        return texto.length() <= ancho ? texto : texto.substring(0, Math.max(0, ancho - 1)) + "…";
    }

    private static String conColor(String color, String texto) {
        return COLOR ? color + texto + RESET : texto;
    }
}
