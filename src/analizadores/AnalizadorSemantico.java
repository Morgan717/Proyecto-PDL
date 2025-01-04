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
        this.pos = p;
        Lexema="";
    }
    private void error(String mensaje){
        gestorE.error("Semantico",mensaje);
    }

    public void procesar() {
        switch (pos.getProduccion()) {
            case "DECL":
                tablaS.setZona_declaracion(true);
                declaracion();
                break;
            case "FUNC_DEF":
                tablaS.setZona_declaracion(true);
                funcion();
                break;
            case"PARAMS":
                parametros();
                break;
            case"ASIGN":
                sentencias();
                break;
            case "OUTPUT":
                sentencias();
                break;
            case"INPUT":
                sentencias();
                break;
            case "RETURN":
                sentencias();
                break;
            case"WHILE":
                sentencias();
                break;
            case"EXPX":
                expresiones();
                break;
            case"IF":
               sentencias();
            case"+":
                expresiones();
                break;
            case"==":
                expresiones();
                break;
            case"&&":
                expresiones();
                break;
            default:
                error("Produccion no econtrada");
        }
    }

    private void funcion(){
        // el lexico acaba de añadir el nombre de la funcion a TS
        tablaS.agregarAtributo(Lexema,"tipo","funcion");
        tablaS.agregarAtributo(Lexema,"tipo retorno",pos.getTokenActual());
        tablaS.setZona_declaracion(false);
        tablaS.crearTabla(Lexema);
    }
    public void fin_funcion(){
        tablaS.setZona_declaracion(true);
        tablaS.liberarTabla();
    }

    private void declaracion(){
            int tam = datos(pos.getTokenActual());
            // token actual = id
            // lexema = nombre id
            if(tam ==  -1){ return;}
            if(tablaS.declarado(Lexema)){
                error("Variable duplicada: "+ Lexema+" ya ha sido declarada previamente");
            }
            else if(tablaS.isZona_declaracion()){
                tablaS.agregarAtributo(Lexema,"tipo",pos.getTokenActual());
                tablaS.agregarAtributo(Lexema,"desplazamiento", Integer.toString(tablaS.getDespLocal()));
                tablaS.setDespLocal(tablaS.getDespLocal() + tam);
            }
            else{
                error("Se esta intentando declarar y no es posible en este punto del codigo");
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
        String s = pos.getTokenActual();
        String e = pos.getTokenSig();
        String sTipo = tipo(s);
        String eTipo = tipo(e);
        switch (pos.getProduccion()) {
            case"ASIGN":
                if(!eTipo.equals(sTipo)){
                    error("Error al asignar: se esta itentando asignar un "+ eTipo+" a una varible tipo: "+sTipo);
                }
                else if(eTipo.equals("error")){
                    error("tipo de variable no reconocida al asignar");
                }
                break;
            case "OUTPUT":
                if(eTipo.equals("error")){
                    error("Error en output, no puedes hacer un output con" +eTipo);
                }
                break;
            case"INPUT":
                if(eTipo.equals("error")){
                    error("Error en input, no puedes hacer un input con" +eTipo);
                }
                break;
            case "RETURN":
                String tipo =  tablaS.getTipoRetorno();

               if(tipo.isEmpty()){error("Error al hacer return, no hay funciones definidas");}
                else if(tipo.equals("void")){ error("Error, return dentro de una funcion void");}
                else if(!eTipo.equals(tipo)){
                    error("Error en return, el valor devuelto de return no coincide con el tipo de retorno de la funcion");}
                break;
            case"WHILE":
                if(!eTipo.equals(sTipo)){
                    error("Condicion del while mal hecha");
                }
                else if(!eTipo.equals("boolean")){
                    error("condicion del while no booleana");
                }
                break;
            case"IF":
                if(!eTipo.equals(sTipo)){
                    error("Condicion del if mal hecha");
                }
                else if(!eTipo.equals("boolean")){
                    error("condicion del if no booleana");
                }
                break;
            default:
                error("Sentencia incorrecta");
        }
    }



    private String tipo(String lexema){
        String res = "error";
        if(lexema.equals("cad")) {return "String";}
        else if(lexema.equals("cte")) {return "int";}
        else {
            String t =  tablaS.getTipo(lexema);
           if(!t.isEmpty()){
               res = t;
           }
        }
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
