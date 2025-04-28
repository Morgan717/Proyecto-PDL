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
    public boolean declarado(String lexema){
        if(!tablaActual.containsKey(lexema) && !tablaG.containsKey(lexema)) return false;
        else if(tablaG.containsKey(lexema)) return tablaG.get(lexema) != null;
        else return tablaActual.get(lexema) != null;
    }

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

    public int add(String lexema){
            //anadir por parte del lexico
        int res = posTS;
        boolean encontrado = false;
            for (String clave : tablaActual.keySet()) {
                if (lexema.equals(clave)) {
                    res = tablaActual.get(clave).getDesp();
                    encontrado = true;
                    break;
                }
            }

        if (!encontrado){

            for (String clave : tablaG.keySet()) {
                if (lexema.equals(clave)) {
                    res = tablaG.get(clave).getDesp();
                    encontrado = true;
                    break;
                }
            }
            if(!encontrado && !tablaActual.containsKey(lexema)) {
                    tablaActual.put(lexema, null);
                    res = ++posTS;
            }

        }// si no se ha encontrado o no tiene desplazamiento
        return res;
    }
    public void agregarAtributo(String lexema, String atributo, String valor) {
        // anadimos atributo a la tabla actual al lexema concreto
        if(!declarado(lexema)) {tablaActual.put(lexema, new Atributos());}
            Atributos a = tablaActual.get(lexema);
            a.add(atributo, valor);
            if(atributo.equals("desplazamiento") && !zonaFuncion){posTS = Integer.parseInt(valor); }// si estamos en una funcion no avanzamos la pos
    }
    public void agregarParam(String nombre,String tipo, int n) {
        // añadimos parametros a la funcion actual
        if(!zonaFuncion){System.err.println("TablaSimbolos,  Se esta intentadno añadir parametros cuando no estamos en una funcion"); return;}
        if(pilaTFun.isEmpty()){System.err.println("TablaSimbolos,  Se esta intentadno añadir parametros sin niguna funcion definida");return;}
        String ultimaFuncion="";
        for (String key : pilaTFun.keySet()) {ultimaFuncion = key;} // La última iteración tendrá la última clave
        Atributos a = tablaG.get(ultimaFuncion);
        if (a != null) {
            a.add("numParam",String.valueOf(n));
            a.add("TipoParam"+n ,tipo);
        }
        else {System.err.println("TablaSimbolos,Se esta intentando agregar atributos a una variable que no existe");}

        tablaActual.put(nombre,new Atributos());
        tablaActual.get(nombre).add("tipo",tipo);
        posTS++;
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
        if(!encontrado) {
            for (String clave2 : tablaG.keySet()) {
                if (clave2.equals(id)) {
                    res = tablaG.get(clave2).getTipo();
                    encontrado = true;
                }
            }
        }
        if(!encontrado) System.err.println("TablaSimbolos, Se esta intentando buscar el tipo del id: "+ id + " que no existe");
        return res;
    }
    public  String getTipoRetorno(){
        if(pilaTFun.isEmpty()){System.err.println("TablaSimbolos,Se esta intentando de hacer un return sin niguna funcion definida");return "";}
        String ultimaFuncion="";
        for (String key : pilaTFun.keySet()) {ultimaFuncion = key;}
        if(ultimaFuncion.isEmpty()){ System.err.println("TablaSimbolos, Error al acceder a la ultima funcion en tipoRetorno"); return "";}
        String res = tablaG.get(ultimaFuncion).getTipoRetorno();
        return res;
    }


    public void imprimirTablaS() {
        try {
            // Contador para las tablas
            int contadorFuncion = 2;
            // Imprimir la tabla principal
            escritura.write("TABLA PRINCIPAL #1:\n");
            for (Map.Entry<String, Atributos> entrada : tablaActual.entrySet()) {
                escritura.write("* LEXEMA : '" + entrada.getKey() + "'\n");
                if(entrada.getValue()== null){
                    escritura.write(("\tATRIBUTOS :\n"));
                }
                else {
                    escritura.write(entrada.getValue().imprimirAtributos());
                }
                escritura.write("\n");

                // Si el lexema es una función, imprimir su tabla de símbolos
                if (pilaTFun.containsKey(entrada.getKey())) {
                    imprimirFunc(entrada.getKey(), contadorFuncion);
                    contadorFuncion++; // Incrementar el contador después de imprimir
                }
            }
            escritura.flush();
            escritura.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void imprimirFunc(String funcion, int contador) {
        try {
            LinkedHashMap<String, Atributos> tablaFuncion = pilaTFun.get(funcion);
            if (tablaFuncion != null) {
                escritura.write("---------------------------------------------------\n");
                escritura.write("TABLA de la FUNCION " + funcion + " #" + contador + ":\n");
                for (Map.Entry<String, Atributos> entrada : tablaFuncion.entrySet()) {
                    escritura.write("* LEXEMA: '" + entrada.getKey() + "' \n");
                    if(entrada.getValue()== null){
                        escritura.write(("\tATRIBUTOS :\n"));
                    }
                    else {
                        escritura.write(entrada.getValue().imprimirAtributos());
                    }
                }
                escritura.write("---------------------------------------------------\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
