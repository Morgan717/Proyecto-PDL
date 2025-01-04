package ClasesAuxiliares;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class TablaSimbolos {

    private String funcion;
    private Map<String,List<String>> tabla;
    private int desplazamientoActual = 0;


    public TablaSimbolos(){
       tabla = new LinkedHashMap<>();
    }

    // esto genera una entrada en la tabla de simbolos solo añadiendo lexema
    public int añadir (String id){
        int res = 0;
        if(!tabla.containsKey(id)){// si no contiene el lexema
            tabla.put(id,new ArrayList<String>());// lo añadimos
            res = tabla.size();// devolvemos la posicione en la tabla de simbolos
        }
        else{
            for(String clave : tabla.keySet()){//bucle para encontrar la posicion del lexema que ya existe
                res++;
                if(clave == id) break;
            }
        }
        return res;
    }
    public boolean declarado(String id){
        return tabla.containsKey(id);
    }

    public Map.Entry<String, List<String>> getRow( int position) {
        if (position < 0 || position >= tabla.size()) {
            throw new IndexOutOfBoundsException("La posición está fuera de los límites del mapa.");
        }

        int currentIndex = 0;
        for (Map.Entry<String, List<String>> entry : tabla.entrySet()) {
            if (currentIndex == position) {
                return entry;
            }
            currentIndex++;
        }
        return null;
    }

    public Set<String> keySet(){
        return tabla.keySet();
    }
    public List<String> get (String clave){
        return tabla.get(clave);
    }
    public void setName(String id){
        funcion = id;
    }
    // falta implementar
    // añadir atributos as un elemento de la tabla
    
    private int calcularTamano(String tipo) {
        switch (tipo.toLowerCase()) {
            case "entero":
                return 4;
            case "logico":
                return 1;
            case "cadena":
                return 8;
            default:
                System.out.println("Tipo desconocido: " + tipo);
                return 0;
        }
    }
   
    public void añadirAtributos(String lexema, String tipo) {
        if (!tabla.containsKey(lexema)) {
            System.out.println("El lexema '" + lexema + "' no existe en la tabla.");
            return;
        }

        List<String> atributos = tabla.get(lexema);

        if (atributos.size() > 0) {
            atributos.set(0, tipo);
        } else {
            atributos.add(tipo);
        }

        if (atributos.size() > 1) {
            atributos.set(1, String.valueOf(desplazamientoActual));
        } else {
            atributos.add(String.valueOf(desplazamientoActual));
        }

        desplazamientoActual += calcularTamano(tipo); 
    }

    // modificar para imprimir tmb atributos
    public void escribirTabla(FileWriter escrituraTablaS) {
        try {
            escrituraTablaS.write("TABLA PRINCIPAL " + funcion + ":\n");

            for (String lexema : tabla.keySet()) {
                escrituraTablaS.write("* LEXEMA : '" + lexema + "'\n");
                escrituraTablaS.write("  ATRIBUTOS:\n");

                List<String> atributos = tabla.get(lexema);
                if (atributos != null && !atributos.isEmpty()) {
                    escrituraTablaS.write("  + tipo : '" + atributos.get(0) + "'\n");
                    escrituraTablaS.write("  + despl : '" + atributos.get(1) + "'\n");
                }
                escrituraTablaS.write("--------- ----------\n");
            }

            escrituraTablaS.close();
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo la tabla de símbolos: " + e.getMessage(), e);
        }
    }



}
