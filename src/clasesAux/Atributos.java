package clasesAux;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Atributos {

    private LinkedHashMap<String, String> atributos; // Atributos del identificador

    public Atributos() {this.atributos = new LinkedHashMap<>(); }
    public void add(String nombreAtributo, String valor) {atributos.put(nombreAtributo, valor);}
    public boolean declarado(){return !atributos.isEmpty();}
    public String getTipo(){
        String res = "";
        boolean encontrado= false;
        for(String clave: atributos.keySet()){
            if(clave.equals("tipo")){
                res = atributos.get(clave);
                encontrado= true;
            }
        }
        return res;}

    public String getTipoRetorno(){
        String res = "";
        boolean encontrado= false;
        for(String clave: atributos.keySet()){
            if(clave.equals("tipo retorno")){
                res = atributos.get(clave);
                encontrado= true;
            }
        }
        return res;}

    public int getDesp(){
        // si tiene desplazamiento se lo da
        int res = -1;
        boolean encontrado= false;
        for(String clave : atributos.keySet()){
            if(clave.equals("desplazamiento")){
                res = Integer.parseInt(atributos.get(clave));
                encontrado = true;
            }
        }
        return res;
    }
    public List<String> getParametros() {
        List<String> parametros = new ArrayList<>();
        int i = 1;
        while (true) {
            String tipo = atributos.get("TipoParam" + i);
            if (tipo == null) break;
            parametros.add(tipo);
            i++;
        }
        return parametros;
    }



    public String imprimirAtributos(){
        StringBuilder sb = new StringBuilder();
        sb.append("\tATRIBUTOS :\n");
        for (Map.Entry<String, String> entry : atributos.entrySet()) {
            sb.append("\t\t+ ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" \n");
        }
        return sb.toString();
    }
}



