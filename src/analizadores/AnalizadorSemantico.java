package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PosicionActual;
import tablaS.TablaSimbolos;

public class AnalizadorSemantico {

    private GestorErrores gestorE;
    private TablaSimbolos tablaS;
    private String Lexema;
    private PosicionActual pos;


    public AnalizadorSemantico(TablaSimbolos tablaS, GestorErrores gestor,PosicionActual p) {
        this.gestorE= gestor;
        this.tablaS = tablaS;
        this.pos = p; //comunicacion con el sintactico
        Lexema=""; // comunicacion con el lexico
        tablaS.setZona_declaracion(true);// la zona de declaracion empieza en true
    }
    private void error(String mensaje){gestorE.error("Semantico",mensaje);}

    // llamaos a procesar cuando emepzamos una produccion
    public void procesar() {
        switch (pos.getProduccion()) {
            case "DECL": declaracion();
                break;
            case "FUNC": funcion();
                break;
            case"PARAMS": parametros();
                break;
            case"ASIGN":
            case"WHILE":
            case "OUTPUT":
            case"INPUT":
            case "RETURN":
            case"IF":
                sentencias();
                break;
            case"EXPX":
            case"+":
            case"==":
            case"&&":
                expresiones();
                break;
            default:
                System.err.println("Se ha llamado al semantico con una produccion no reconocida");
        }
    }

    private void funcion(){
        tablaS.setZona_declaracion(true);
        tablaS.agregarAtributo(Lexema,"tipo","funcion");
        tablaS.agregarAtributo(Lexema,"tipo retorno",pos.getTokenActual());
        tablaS.agregarAtributo(Lexema,"desplazamiento","0");
        tablaS.crearTabla(Lexema);
    }
    public void fin_funcion(){
        tablaS.setZona_declaracion(false);
        tablaS.liberarTabla();
    }

    private void declaracion(){
            tablaS.setZona_declaracion(true);
            int tam = datos(pos.getTokenActual());
            // token actual = id
            // lexema = nombre id
            if(tam ==  -1){
                System.err.println("En el semantico hemos intentado encontrar el tamño de un tipo de variable mal");
                return;
            }
            if(tablaS.declarado(Lexema)){
                error("Variable duplicada: "+ Lexema+" ya ha sido declarada previamente");
            }
            else{
                tablaS.agregarAtributo(Lexema,"tipo",pos.getTokenActual());
                tablaS.agregarAtributo(Lexema,"desplazamiento", Integer.toString(tablaS.getDespLocal()));
                tablaS.setDespLocal(tablaS.getDespLocal() + tam);
            }
    }

    private void parametros(){
        int tam = datos(pos.getTokenActual());
        // token actual = id
        // lexema = nombre id
        if(tam !=  -1) {
            tablaS.agregarAtributo(Lexema, "tipo", pos.getTokenActual());
            tablaS.agregarAtributo(Lexema, "desplazamiento", Integer.toString(tablaS.getDespLocal()));
            tablaS.setDespLocal(tablaS.getDespLocal() + tam);
        }
    }
    private int datos(String tipo){
        switch (tipo){
            case "int":
                return 4;
            case"bool":
                return 1;
            case "void":
                return 0;
            case"string":
                return 64;
            default:
                error("tipo no reconocido");
                return -1;
        }
    }
    private void sentencias(){
        // token actual = lexema;
        // token sig = lexema o cte o cad
        String s = pos.getTokenActual();// nombre var 1
        String e = pos.getTokenSig();// nombre var 2
        String sTipo = tipo(s);
        String eTipo = tipo(e);
        switch (pos.getProduccion()) {
            case"ASIGN":
                if(!eTipo.equals(sTipo)){error("Error al asignar: se esta itentando asignar un "+ eTipo+" a una varible tipo: "+sTipo);}
                else if(eTipo.isEmpty()){error("tipo de variable no reconocida al asignar");}
                break;
            case "OUTPUT":
                if(eTipo.isEmpty()){error("Error en output, no puedes hacer un output con" +eTipo);}
                break;
            case"INPUT":
                if(!e.equals("id")){error("Error en input, no puedes hacer un input con" +eTipo);}
                break;
            case "RETURN":
                String tipo =  tablaS.getTipoRetorno();
               if(tipo.isEmpty()){error("Error al hacer return, no hay funciones definidas");}
                else if(tipo.equals("void")){ error("Error, return dentro de una funcion void");}
                else if(!eTipo.equals(tipo)){
                    error("Error en return, el valor devuelto de return no coincide con el tipo de retorno de la funcion");}
                break;
            case"WHILE":
                if(sTipo.equals("+")){error("Condicion del while no booleana");}
                break;
            case"IF":
                if(sTipo.equals("+")){error("Condicion del if no booleana");}
                break;
            default:
                error("Sentencia incorrecta");
        }
    }



    private String tipo(String lexema){
        String res = "";
        if(lexema.equals("cad")) {return "String";}
        else if(lexema.equals("cte")) {return "int";}
        else {
            String t =  tablaS.getTipo(lexema);
           if(!t.isEmpty()){
               res = t;
           }
           else{
               error("Uso de una variable no declarada");
               return "";
           }
        }
        // para funciones se vuelve a comprobar
        if(res.equals("cad")){ res = "string";}
        else if(res.equals("cte")){res = "int";}
        else if(res.equals("bool")){ res ="boolean";}
        return res;
    }
    private void expresiones(){
        String s = pos.getTokenActual();
        String e = pos.getTokenSig();
        String sTipo = tipo(s);
        String eTipo = tipo(e);
        switch(pos.getProduccion()){
            case"+":
                if(!eTipo.equals(sTipo)){
                    error("error de expresion no puede sumar tipos de datos distintos");
                } else if (eTipo.equals("boolean")) {
                    error("erro de expresion no puedes sumar booleanos");
                }
                break;
            case"==":
                if(!eTipo.equals(sTipo)){
                    error("error de expresion no puede comparar tipos de datos distintos");
                }
                break;
            case"&&":
                if(!eTipo.equals(sTipo)){
                    error("error de expresion no puede comparar tipos de datos distintos");
                } else if (!eTipo.equals("boolean")) {
                    error("erro de expresion no puedes comparar datos que no sean booleanos");
                }
                break;
        }
    }
    public void finSemantico(){ tablaS.imprimirTablaS(); }
    public String getLexema(){ return Lexema; }
    public void setLexema(String uVariable) { this.Lexema = uVariable; }
}
