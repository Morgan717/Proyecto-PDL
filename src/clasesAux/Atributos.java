package clasesAux;

import java.util.LinkedHashMap;
import java.util.Map;


public class Atributos {

    private LinkedHashMap<String, String> atributos; // Atributos del identificador

    public Atributos() {this.atributos = new LinkedHashMap<>(); }
    public void añadir(String nombreAtributo, String valor) {atributos.put(nombreAtributo, valor);}
    public boolean declarado(){
        for(String clave: atributos.keySet()){
            if(clave.equals("tipo")){
                return true;
            }
        }
        return false;}

    public String getTipo(){
        String res = "";
        boolean encontrado= false;
        for(String clave: atributos.keySet()){
            if(clave.equals("tipo")){
                res = atributos.get(clave);
                encontrado= true;
            }
        }
        if(!encontrado){System.err.println("Se esta intentando buscar el atributo tipo sin que haya sido añadido");}
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
        if(!encontrado){System.err.println("Se esta intentando buscar el atributo tipo retorno sin que haya sido añadido");}
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
        if(!encontrado){System.err.println("Se esta intentando buscar el atributo desp sin que haya sido añadido");}
        return res;
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



