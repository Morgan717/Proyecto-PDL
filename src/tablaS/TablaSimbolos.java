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

    private int posTS;
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
        } catch (IOException e) { throw new RuntimeException(e); }
        this.gestorE = g;
        despLocal = 0;
        despGlobal= 0;
        pos = 0;
        posTS = 0;
        tablaG= new LinkedHashMap<>();
        tablaActual = tablaG;
        this.pilaTFun = new LinkedHashMap<>();

    }
    private void error(String mensaje) { gestorE.error("Sintactico",mensaje); }
    public int getDespLocal() { return despLocal; }
    public void setDespLocal(int i ) { despLocal= i; }
    public boolean isZona_declaracion() { return zona_declaracion; }
    public void setZona_declaracion(boolean zona_declaracion) { this.zona_declaracion = zona_declaracion; }
    public boolean declarado(String lexema){ return tablaActual.get(lexema).declarado(); }


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
        int res = posTS;
        boolean encontrado = false;
        for(String clave: tablaG.keySet()){
            if(lexema.equals(clave)){
                res = tablaG.get(clave).getDesp();
                encontrado = true;
            }
        }
        if (!encontrado){res = posTS;}// si no se ha encontrado o no tiene desplazamiento
        return res;
    }

    public void agregarAtributo(String lexema, String atributo, String valor) {
        // añadimos atributo a la tabla actual al lexema concreto
        Atributos a = tablaActual.get(lexema);
        if (a != null) {a.añadir(atributo, valor);}
        else {System.err.println("Se esta intentando agregar atributos a una variable que no existe");}

        if(atributo.equals("desplazamiento") && zona_declaracion){ posTS = Integer.parseInt(valor); }
    }

    public String getTipo(String id) {
        String res = "";
        boolean encontrado= false;
        for(String clave: tablaActual.keySet()){
            if(clave.equals(id)){
                res = tablaActual.get(clave).getTipo();
                encontrado= true;
            }
        }
        if(!encontrado){System.err.println("Se esta intentando buscar el tipo de un id que no existe");}
        return res;
    }
    public  String getTipoRetorno(){
        String res ="";
        if(pilaTFun.isEmpty()){
            System.err.println("Se esta intentando de hacer un return sin niguna funcion definida");
            return "";
        }
        String ultimaFuncion="";
        for (String key : pilaTFun.keySet()) { ultimaFuncion = key;} // La última iteración tendrá la última clave
        res = tablaG.get(ultimaFuncion).getTipoRetorno();
        return res;
    }



    public void imprimirTablaS() {
        try {
            // Imprimir la tabla principal
            escritura.write("TABLA PRINCIPAL #1:\n");
            for (Map.Entry<String, Atributos> entrada : tablaActual.entrySet()) {
                escritura.write("* LEXEMA : '" + entrada.getKey() + "'\n");
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
            escritura.flush();
            escritura.close();
        } catch (IOException e) { e.printStackTrace();}
    }

}
