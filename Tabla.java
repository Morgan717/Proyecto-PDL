package ClasesAuxiliares;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Tabla {

    private File ficheroTablaS;
    private FileWriter escrituraTablaS;
    private Map<String,TablaSimbolos> mapaTotal;

    public Tabla (File tablaS){
        TablaSimbolos principal = new TablaSimbolos();
        mapaTotal.put("Principal",principal);
        this.ficheroTablaS = tablaS;
        try {
            this.escrituraTablaS = new FileWriter(ficheroTablaS);
            escrituraTablaS.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // añadir variables nuevas
    public int añadir (String id, String lexema){
        int res = 0;
        if(id.isEmpty()){
            TablaSimbolos t = mapaTotal.get("Principal");
            res = t.añadir(lexema);
        }
        else{
            TablaSimbolos funcion= mapaTotal.get(id);
            res = funcion.añadir(lexema);
        }
        return res;
    }

    public void tablaLocal(String nombreFuncion){
        TablaSimbolos funcion = new TablaSimbolos();
        funcion.setName(nombreFuncion);
        mapaTotal.put(nombreFuncion,funcion);
    }

    public void añadirAtributos(String id, String lexema, String tipo) {
        if (id.isEmpty()) { // Si no hay identificador, usa la tabla principal
            TablaSimbolos t = mapaTotal.get("Principal");
            if (t != null) {
                t.añadirAtributos(lexema, tipo);
            } else {
                System.out.println("No se encontró la tabla de símbolos principal.");
            }
        } else { // Si hay identificador, usa la tabla de la función
            TablaSimbolos funcion = mapaTotal.get(id);
            if (funcion != null) {
                funcion.añadirAtributos(lexema, tipo);
            } else {
                System.out.println("No se encontró la tabla de símbolos para la función: " + id);
            }
        }
    }


    // para comprobar variables ya declaradas
    public boolean declarado (String id, String lexema){
        if(id.isEmpty()){
            TablaSimbolos t = mapaTotal.get("Principal");
            return t.declarado(lexema);
        }
        else {
            TablaSimbolos funcion = mapaTotal.get(id);
            return funcion.declarado(lexema);
        }
    }

    
 // falta añadir atributos
    
    public void escribirTabla() {
        try {
            // Escribe la tabla principal
            escrituraTablaS.write("TABLA PRINCIPAL #1:\n\n");
            TablaSimbolos t = mapaTotal.get("Principal");

            if (t != null) {
                for (String clave : t.keySet()) {
                    escrituraTablaS.write("* LEXEMA : '" + clave + "'\n");
                    escrituraTablaS.write("  ATRIBUTOS:\n");

                    List<String> atributos = t.get(clave); // Obtiene la lista de atributos
                    if (atributos != null && !atributos.isEmpty()) {
                        escrituraTablaS.write("  + tipo : '" + atributos.get(0) + "'\n");
                        escrituraTablaS.write("  + despl : '" + atributos.get(1) + "'\n");
                    }
                    escrituraTablaS.write("--------- ----------\n");

                    // Si es una función, escribe su tabla local
                    if ("funcion".equalsIgnoreCase(atributos.get(0))) {
                        escrituraTablaS.write("\nTABLA DE LA FUNCION '" + clave + "':\n");
                        escrituraTablaS.write("---------------------------------------------------\n");
                        TablaSimbolos tablaFuncion = mapaTotal.get(clave);
                        if (tablaFuncion != null) {
                            for (String lexemaFuncion : tablaFuncion.keySet()) {
                                List<String> atributosFuncion = tablaFuncion.get(lexemaFuncion);
                                escrituraTablaS.write("* LEXEMA : '" + lexemaFuncion + "'\n");
                                escrituraTablaS.write("  ATRIBUTOS:\n");
                                escrituraTablaS.write("  + tipo : '" + atributosFuncion.get(0) + "'\n");
                                escrituraTablaS.write("  + despl : '" + atributosFuncion.get(1) + "'\n");
                                escrituraTablaS.write("--------- ----------\n");
                            }
                        } else {
                            escrituraTablaS.write("No hay tabla de símbolos local para esta función.\n");
                        }
                    }
                }
            } else {
                escrituraTablaS.write("No se encontró la tabla de símbolos principal.\n");
            }

            escrituraTablaS.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}