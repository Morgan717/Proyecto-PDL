package tablaS;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import clasesAux.Atributos;
import clasesAux.GestorErrores;

public class TablaSimbolos {


    private int pos;
    private boolean zona_declaracion;
    private int despGlobal;
    private int despLocal;

    private Map<String,Integer> posTS;
    GestorErrores gestorE;
    private LinkedHashMap<String,Atributos> tablaG;
    private LinkedHashMap<String,Atributos> tablaActual;
    private LinkedHashMap<String,LinkedHashMap<String,Atributos>> pilaTFun;

    private File salida;
    private FileWriter escritura;
    public TablaSimbolos(File salida, GestorErrores g) {
        try{
            this.salida = salida;
            this.escritura= new FileWriter(this.salida);
            escritura.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.gestorE = g;
        despLocal = 0;
        despGlobal= 0;
        pos = 0;
        posTS = new HashMap<>();
        tablaG= new LinkedHashMap<>();
        tablaActual = tablaG;
        this.pilaTFun = new LinkedHashMap<>();

    }
    private void error(String mensaje) {
        gestorE.error("Sintactico",mensaje);
    }


    public void crearTabla(String nombre) {
        despGlobal= despLocal;
        despLocal =0;
        pilaTFun.put(nombre,new LinkedHashMap<>());
        tablaActual= pilaTFun.get(nombre);
    }

    public void liberarTabla() {
        despLocal = despGlobal;
        tablaActual = tablaG;
    }

    public int añadir(String lexema){
            //añadir por parte del lexico
        if(posTS.containsKey(lexema)){
            return posTS.get(lexema);
        }
        else{
            posTS.put(lexema,++pos);
            return posTS.get(lexema);
        }
    }

    public void agregarAtributo(String lexema, String clave, String valor) {
        // añadimos atributo a la tabla actual al lexema concreto
        Atributos a = tablaActual.get(lexema);
        if (a != null) {
            a.añadir(clave, valor);
        } else {
            error("Se esta intentando agregar atributos a un id que no existe");
        }
    }
    public String getTipo(String id) {
        Atributos atributos = tablaActual.get(id);
        if (atributos == null) {
           gestorE.error("Sintatico","se esta intentando acceder a un tipo");
            return null; // O
        }
        return atributos.getTipo();
    }

    public boolean declarado(String lexema){
        // vale para que funcion bien el lexico añade lexemas
        // pero el semantico mete el tipo
        return tablaActual.get(lexema).declarado();
    }
    public  String getTipoRetorno(){
        String res ="";
        if(pilaTFun.isEmpty()){
            gestorE.error("Sintactico","se trata de hacer un return sin niguna funcion definida");
            return null;
        }
        String lastKey = "";
        for (String key : pilaTFun.keySet()) {
            lastKey = key; // La última iteración tendrá la última clave
        }
        res = tablaG.get(lastKey).getTipoRetorno();
        return res;
    }

    public int getDespLocal() {
        return despLocal;
    }
    public void setDespLocal(int i ) {
        despLocal= i;
    }

    public boolean isZona_declaracion() {
        return zona_declaracion;
    }
    public void setZona_declaracion(boolean zona_declaracion) {
        this.zona_declaracion = zona_declaracion;
    }
    public void imprimirTablaS() {
        try {
            // Imprimir la tabla principal
            escritura.write("TABLA PRINCIPAL #1:\n");
            for (Map.Entry<String, Atributos> entrada : tablaActual.entrySet()) {
                escritura.write("LEXEMA : '" + entrada.getKey() + "'\n");
                escritura.write(entrada.getValue().imprimirAtributos());
                escritura.write("---------------------------------------------------\n");
            }

            // Imprimir las tablas de funciones
            int contadorFuncion = 2;
            for (Map.Entry<String, LinkedHashMap<String, Atributos>> funcion : pilaTFun.entrySet()) {
                escritura.write("TABLA de la FUNCION " + funcion.getKey() + " #" + contadorFuncion + ":\n");
                for (Map.Entry<String, Atributos> entrada : funcion.getValue().entrySet()) {
                    escritura.write("* LEXEMA: '" + entrada.getKey() + "' \n");
                    escritura.write(entrada.getValue().imprimirAtributos());
                }
                escritura.write("---------------------------------------------------\n");
                contadorFuncion++;
            }
        } catch (IOException e) {
            e.printStackTrace();}
            finTabla();
    }
    public void finTabla() {
        try {
            escritura.flush();
            escritura.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
