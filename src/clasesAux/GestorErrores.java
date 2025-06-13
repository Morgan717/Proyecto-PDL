package clasesAux;
import java.io.*;

import analizadores.AnalizadorLexico;
import analizadores.AnalizadorSemantico;
import analizadores.AnalizadorSintactico;

public class GestorErrores {

    private File salidaErrores;
    private FileWriter escrituraErrores;
    private int linea;
    private AnalizadorLexico lexico;
    private AnalizadorSintactico sintactico;
    private AnalizadorSemantico semantico;

    public GestorErrores(File salidaErr) {
        try {
            this.salidaErrores = salidaErr;
            this.escrituraErrores = new FileWriter(salidaErrores);
            escrituraErrores.write("");
        } catch (NullPointerException t) {
            throw new RuntimeException(t);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setAnalizadores(AnalizadorLexico l, AnalizadorSintactico s, AnalizadorSemantico sem) {
        this.lexico = l;
        this.sintactico = s;
        this.semantico = sem;
    }
    public void setLinea(int linea) {
        this.linea = linea;
    }

    public void error(String tipo, String mensaje) {
        try {
            escrituraErrores.write("Error de tipo: " + tipo +"; " + mensaje + " linea de codigo: " + linea +"\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // para que sea mas sencilla la implementacion cortamos todo en cuanto detectamos error
        if (sintactico != null) sintactico.finSintactico();
        if (semantico != null) semantico.finSemantico();
        if (lexico != null) lexico.finLexico();
        finGestor();
        System.exit(1);
    }



    public void finGestor(){
        try {
            escrituraErrores.flush();
            escrituraErrores.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
