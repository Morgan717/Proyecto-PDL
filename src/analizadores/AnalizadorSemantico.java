package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PosicionActual;
import tablaS.TablaSimbolos;

public class AnalizadorSemantico {

    private GestorErrores gestorE;
    private TablaSimbolos tablaS;
    private String Lexema;
    private int cte;
    private String cadena;
    private PosicionActual pos;
    private int n_param;


    public AnalizadorSemantico(TablaSimbolos tablaS, GestorErrores gestor,PosicionActual p) {
        this.gestorE= gestor;
        this.tablaS = tablaS;
        this.pos = p; //comunicacion con el sintactico
        Lexema=""; // comunicacion con el lexico
        tablaS.setZona_declaracion(true);// la zona de declaracion empieza en true
    }
    private void error(String mensaje){gestorE.error("Semantico",mensaje);}
    public void setN_param(int i){n_param=i;}
    public int getN_param(){return n_param;}
    public void setCte(int i){cte = i;}
    public int getCte(){return cte;}
    public void setCadena(String s){cadena = s;}
    public String getCadena(){return cadena;}
    // llamaos a procesar cuando emepzamos una produccion
    public void procesar() {
        switch (pos.getProduccion()) {

            case "DECL":
                tablaS.setZona_declaracion(true);
                declaracion();
                tablaS.setZona_declaracion(false);
                break;
            case "FUNC":
                tablaS.setZona_declaracion(true);
                funcion();
                break;
            case"PARAMS": parametros();
                break;
            case"%=":
            case"=":
                asignacion();
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
                expresiones("");
                break;
            default:
                System.err.println("Se ha llamado al semantico con una produccion no reconocida");
        }
    }

    private void funcion(){
        tablaS.agregarAtributo(Lexema,"tipo","funcion");
        tablaS.agregarAtributo(Lexema,"tipo retorno",pos.getTokenActual());
        tablaS.agregarAtributo(Lexema,"desplazamiento",String.valueOf(tablaS.getDespLocal()));
        tablaS.crearTabla(Lexema);
    }
    public void fin_funcion(){
        tablaS.setZona_declaracion(false);
        tablaS.liberarTabla();
    }

    private void declaracion(){
            int tam = datos(pos.getTokenActual());
            // token actual = id
            // lexema = nombre id
            if(tam ==  -1){
                System.err.println("En el semantico hemos intentado encontrar el tamaño de un tipo de variable mal");
                return;
            }
            if(tablaS.declarado(Lexema)){
                error("Variable duplicada: "+ Lexema+" ya ha sido declarada previamente");
            }
            else{
                tablaS.agregarAtributo(Lexema,"tipo",pos.getTokenActual());
                tablaS.agregarAtributo(Lexema,"desplazamiento", String.valueOf(tablaS.getDespLocal()));
                tablaS.setDespLocal(tablaS.getDespLocal() + tam);
            }
    }

    private void asignacion() {
        //token actual = , %=
        //token sig tipo
        String sTipo = expresiones(pos.getTokenSig());
        if (sTipo.isEmpty()) {
            error("Error de asignacion");
        } else if (pos.getProduccion().equals("%=")) {
            if (!sTipo.equals("int")) {
                error("Error de asignacion %= no estas usando un entero ");
            }
        }
    }


    private void parametros(){
        int tam = datos(pos.getTokenActual());
        if(tam !=  -1) {
            tablaS.agregarParam(Lexema,pos.getTokenActual() ,n_param);
            tablaS.agregarAtributo(Lexema,"param",String.valueOf(n_param));
            tablaS.agregarAtributo(Lexema,"desplazamiento",String.valueOf(tablaS.getDespLocal()));
            tablaS.agregarAtributo(Lexema,"tipo",pos.getTokenActual());
            tablaS.setDespLocal(tablaS.getDespLocal() + tam);
        }
    }

    private int datos(String tipo){
        switch (tipo){
            case "int":
                return 2;
            case"boolean":
                return 1;
            case "void":
                return 0;
            case"string":
                return 4;
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
        String sTipo = expresiones(s);
        String eTipo = expresiones(e);
        switch (pos.getProduccion()) {
            case "=": case"%=":
                asignacion();
                break;
            case "OUTPUT":
                if(eTipo.isEmpty()){error("Error en output, no puedes hacer un output con" +eTipo);}
                break;
            case"INPUT":
                if(!e.equals("id")){error("Error en input, no puedes hacer un input con" + eTipo);}
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

    private String expresiones(String lexema){
        if(lexema.equals("saltar")){return "";}
        if(lexema.equals("id")){return "";}
        if(lexema.equals("=") || lexema.equals("%=") ){return "";}
        if(lexema.equals("true")||lexema.equals("false") || lexema.equals("boolean") ){return"boolean";}
        String res = "";
        if(!lexema.isEmpty()) {
            if (lexema.equals("cad")) {return "string"; }
            else if (lexema.equals("cte")) { return "int";}
            // tipo de un id
                if(tablaS.declarado(lexema)) {
                    String t = tablaS.getTipo(lexema);
                    if (!t.isEmpty()) { res = t;} }
                else { error("Uso de una variable no declarada");   return "";
                    }

            //se vuelve a comprobar
            if (res.equals("cad")) {return "string";}
            else if (res.equals("cte")) {return "int";}
            return res;
        }
        String s = pos.getTokenActual();
        String e = pos.getTokenSig();
        String sTipo = expresiones(s);
        String eTipo = expresiones(e);
        switch(pos.getProduccion()){
            case"+":
                if(!eTipo.equals(sTipo)){
                    error("error de expresion no puede sumar tipos de datos distintos");
                    return "";
                } else if (eTipo.equals("boolean")) {
                    error("erro de expresion no puedes sumar booleanos");
                    return "";
                }
                return "int";
            case"==":
                if(!eTipo.equals(sTipo)){
                    error("error de expresion no puede comparar tipos de datos distintos");
                    return "";
                }
                return "boolean";
            case"&&":
                if(!eTipo.equals(sTipo)){
                    error("error de expresion no puede comparar tipos de datos distintos");
                    return "";
                } else if (!eTipo.equals("boolean")) {
                    error("erro de expresion no puedes comparar datos que no sean booleanos");
                    return "";
                }
                return "boolean";
        }
        return res;
    }
    public void finSemantico(){ tablaS.imprimirTablaS(); }
    public String getLexema(){ return Lexema; }
    public void setLexema(String uVariable) { this.Lexema = uVariable; }
}
