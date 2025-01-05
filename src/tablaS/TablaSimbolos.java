package tablaS;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import clasesAux.Atributos;
import clasesAux.GestorErrores;

public class TablaSimbolos {

    private boolean zonaFuncion;
    private boolean zona_declaracion;
    private int despGlobal;
    private int despLocal;

    private int posTS;
    private GestorErrores gestorE;
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
        posTS = 0;
        tablaG= new LinkedHashMap<>();
        tablaActual = tablaG;
        this.pilaTFun = new LinkedHashMap<>();
    }

    public int getDespLocal() { return despLocal; }
    public void setDespLocal(int i ) { despLocal= i; }
    public boolean isZona_declaracion() { return zona_declaracion; }
    public void setZona_declaracion(boolean zona_declaracion) { this.zona_declaracion = zona_declaracion; }
    public boolean declarado(String lexema){return tablaActual.containsKey(lexema);}

    public void crearTabla(String nombre) {
        despGlobal= despLocal;
        despLocal =0;
        pilaTFun.put(nombre,new LinkedHashMap<>());
        tablaActual= pilaTFun.get(nombre);
        zonaFuncion=true;
    }
    public void liberarTabla() {
        despLocal = despGlobal;
        tablaActual = tablaG;
        zonaFuncion=false;
    }

    public int añadir(String lexema){
            //añadir por parte del lexico
        int res = posTS;
        boolean encontrado = false;
        for(String clave: tablaActual.keySet() ){
            if(lexema.equals(clave)){
                res = tablaActual.get(clave).getDesp();
                encontrado = true;
                break;
            }
        }

        if (!encontrado){
            if (!tablaActual.containsKey(lexema)) {
                tablaActual.put(lexema, new Atributos());
            }
            res = posTS;
        }// si no se ha encontrado o no tiene desplazamiento
        return res;
    }

    public void agregarAtributo(String lexema, String atributo, String valor) {
        // añadimos atributo a la tabla actual al lexema concreto
        Atributos a = tablaActual.get(lexema);
        if (a != null) {a.añadir(atributo, valor);}
        else {System.err.println("TablaSimbolos, linea: 83 error; Se esta intentando agregar atributos a una variable que no existe;");}
        if(atributo.equals("desplazamiento") && !zonaFuncion){posTS = Integer.parseInt(valor); }// si estamos en una funcion no avanzamos la pos

    }

    public void agregarParam(String nombre,String tipo, int n) {
        // añadimos parametros a la funcion actual
        if(!zonaFuncion){System.err.println("TablaSimbolos, linea: 90 error; Se esta intentadno añadir parametros cuando no estamos en una funcion"); return;}
        if(pilaTFun.isEmpty()){System.err.println("TablaSimbolos, linea: 91 error; Se esta intentadno añadir parametros sin niguna funcion definida");return;}
        String ultimaFuncion="";
        for (String key : pilaTFun.keySet()) {ultimaFuncion = key;} // La última iteración tendrá la última clave
        Atributos a = tablaG.get(ultimaFuncion);
        if (a != null) {
            a.añadir("numParam",String.valueOf(n));
            a.añadir("TipoParam"+n ,tipo);
        }
        else {System.err.println("TablaSimbolos, linea: 99 error; Se esta intentando agregar atributos a una variable que no existe");}
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
        if(!encontrado){System.err.println("TablaSimbolos, linea: 112 error; Se esta intentando buscar el tipo del id: "+ id + " que no existe");}
        return res;
    }
    public  String getTipoRetorno(){
        if(pilaTFun.isEmpty()){System.err.println("TablaSimbolos, linea: 116 error; Se esta intentando de hacer un return sin niguna funcion definida");return "";}
        String ultimaFuncion="";
        for (String key : pilaTFun.keySet()) {ultimaFuncion = key;}
        if(ultimaFuncion.isEmpty()){ System.err.println("TablaSimbolos, linea: 119 error; Error al acceder a la ultima funcion en tipoRetorno"); return "";}
        String res = tablaG.get(ultimaFuncion).getTipoRetorno();
        return res;
    }



    public void imprimirTablaS() {
        try {
            // Imprimir la tabla principal
            escritura.write("TABLA PRINCIPAL #1:\n");
            for (Map.Entry<String, Atributos> entrada : tablaActual.entrySet()) {
                escritura.write("* LEXEMA : '" + entrada.getKey() + "'\n");
                escritura.write(entrada.getValue().imprimirAtributos());
                escritura.write("\n");

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

    private void imprimirFunc(){

    }
}
