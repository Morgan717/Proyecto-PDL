package clasesAux;
import java.io.*;

public class GestorErrores {

    private File salidaErrores;
    private FileWriter escrituraErrores;
    private int linea;

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
    public void error(String tipo, String mensaje) {
        try {
            escrituraErrores.write("Error de tipo: " + tipo +"; " + mensaje + " linea de codigo: " + linea +"\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setLinea(int linea) {
        this.linea = linea;
    }

    public void finGestor(){
        try {
          escrituraErrores.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
