package clasesAux;

import java.util.LinkedHashMap;
import java.util.Map;


public class Atributos {

    private LinkedHashMap<String, String> atributos; // Atributos del identificador

    public Atributos() {
        this.atributos = new LinkedHashMap<>();
    }

    public void añadir(String nombreAtributo, String valor) {
        atributos.put(nombreAtributo, valor);
    }
    public String getTipo(){
        return atributos.get("tipo");
    }
    public String getTipoRetorno(){
        return atributos.get("tipo retorno");

    }
    public boolean declarado(){
         return atributos.containsKey("tipo");
    }
    public String imprimirAtributos(){
        StringBuilder sb = new StringBuilder();
        sb.append("\tATRIBUTOS:\n");
        for (Map.Entry<String, String> entry : atributos.entrySet()) {
            sb.append("\t\t+ ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" \n");
        }
        return sb.toString();
    }
}



