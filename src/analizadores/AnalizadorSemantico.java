package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PosicionActual;
import clasesAux.TokenBuffer;
import tablaS.TablaSimbolos;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {

    private GestorErrores gestorE;
    private TablaSimbolos tablaS;
    private String Lexema;
    private int cte;
    private String cadena;
    private PosicionActual pos;
    private int n_param;
    private String llamada;
    private List<String> argumentos;
    private TokenBuffer tokenBuffer; // Nuevo buffer para contexto completo

    public AnalizadorSemantico(TablaSimbolos tablaS, GestorErrores gestor,PosicionActual p) {
        this.gestorE= gestor;
        this.tablaS = tablaS;
        this.pos = p; //comunicacion con el sintactico
        Lexema=""; // comunicacion con el lexico
        tablaS.setZona_declaracion(true);// la zona de declaracion empieza en true
    }
    public void setTokenBuffer(TokenBuffer tokenBuffer) {
        this.tokenBuffer = tokenBuffer;
    }
    private void error(String mensaje){
        gestorE.error("Semantico",mensaje);
    }
    public void setN_param(int i){n_param=i;}
    public int getN_param(){return n_param;}
    public void setCte(int i){cte = i;}
    public int getCte(){return cte;}
    public void setCadena(String s){cadena = s;}
    public String getCadena(){return cadena;}
    public String getLexema(){ return Lexema; }
    public void setLexema(String s) { this.Lexema = s; }


    // llamamos a procesar cuando emepzamos una produccion
    public void procesar() {
        // Si tenemos buffer, procesar expresión completa
        if (tokenBuffer != null && pos.getProduccion().startsWith("EXP")) {
            procesarExpresionCompleta();
        }
        // Procesamiento normal
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
            case "PARAMS":
                parametros();
                break;
            case "%=":
            case "=":
                asignacion();
                break;
            case "WHILE":
            case "OUTPUT":
            case "INPUT":
            case "RETURN":
            case "IF":
                sentencias();
                break;
            case "EXPX":
            case "+":
            case "==":
            case "&&":
                expresiones();
                break;
            case "LLAMADA":
                llamada();
                break;
            case "ARGUMENTOS":
                argumentos();
                break;
            default:
                System.err.println("Se ha llamado al semantico con una produccion no reconocida");
        }
    }
    private void procesarExpresionCompleta() {
        if (tokenBuffer == null) return;

        // Obtener todos los tokens de la expresión actual
        List<TokenBuffer.TokenInfo> tokens = tokenBuffer.getAllTokens();
        int currentPos = tokenBuffer.getCurrentPosition();

        // Procesar tokens desde la posición actual
        for (int i = currentPos; i < tokens.size(); i++) {
            TokenBuffer.TokenInfo token = tokens.get(i);

            // Detectar fin de expresión
            if (token.token.equals(";") || token.token.equals(")") || token.token.equals(",")) {
                break;
            }

            // Validar operaciones
            if (token.token.equals("+")) {
                if (i + 1 < tokens.size()) {
                    TokenBuffer.TokenInfo nextToken = tokens.get(i + 1);
                    if (!"int".equals(nextToken.tipo)) {
                        error("No se puede sumar con tipo: " + nextToken.tipo);
                    }
                }
            } else if (token.token.equals("==")) {
                if (i + 1 < tokens.size()) {
                    TokenBuffer.TokenInfo nextToken = tokens.get(i + 1);
                    if (!token.tipo.equals(nextToken.tipo)) {
                        error("Comparación inválida entre " + token.tipo + " y " + nextToken.tipo);
                    }
                }
            }
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
            System.err.println("En el semantico hemos intentado encontrar el tamano de un tipo de variable mal");
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
        // token actual lexema int
        // token sig  lexema o cte o cad
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
                // si el retorno de funcion es booleano comprobamos que los dos valores comparados sean iguales
                else if(tipo.equals("boolean") && !eTipo.equals(sTipo)){
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
        if(lexema.equals("=") || lexema.equals("%=") ){return "";}
        if(lexema.equals("true")||lexema.equals("false") || lexema.equals("boolean") ){return"boolean";}
        if (lexema.equals("cad")||lexema.equals("string")) return "string";
        if (lexema.equals("cte") || lexema.equals("int")) return "int";
        if(lexema.equals("funcion")) return llamada();
        // si no es nada de lo anterior es un id con el nombre
        String t;
        if(!lexema.isEmpty()) {
            // tipo de un id
            if (!tablaS.declarado(lexema)) {
                // comprobamos, si no esta declarada error
                error("Uso de una variable no declarada");
                return "";
            }
            else{
                // pedimos tipo
                t = tablaS.getTipo(lexema);
                // si no lo esta devuelve una cadena vacia por lo que error
                if (t.isEmpty()) {
                    error("Uso de una variable no declarada");
                    return "";
                }
            }
            // ahora si estamos aqui tenemos el tipo en t
            return expresiones(t);
        }
        return lexema;
    }

    public String expresiones(){
        // pos token actual int
        // pos TokenSig cad cte o nombre id
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
                    error("error de expresion no puedes sumar booleanos");
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
        return "";
    }

    private String llamada(){
        String nombreFuncion = Lexema;
        if (!tablaS.declarado(nombreFuncion)) {
            error("variable no declarada: " + nombreFuncion);
        }
        if (!tablaS.getTipo(nombreFuncion).equals("funcion")) {
            error("El identificador '" + nombreFuncion + "' no es una función.");
        }
        argumentos = new ArrayList<>();// reinciamos lista de argumentos
        llamada = Lexema;
        return  tablaS.getTipoRetorno(Lexema);
    }
    private void argumentos(){
        //entramos aqui con cada nuevo argumento de la llamada
        // el token actual es el id cte cad
        if(pos.getTokenActual().equals("id")){
            if (!tablaS.declarado(Lexema)) {
                error("Uso de variable no declarada como argumento: " + Lexema);
            }
            // si es un id el argumento guardamos su tipo
            argumentos.add(tablaS.getTipo(Lexema));
        }
        else{
            // sustituimos el cad cte a int string
            argumentos.add(expresiones(pos.getTokenActual()));}
    }
    public void finLlamada(){
        // cuando acabemos la llamada comprobamos que los argumentos concuerdan en tipo
        List<String> parametros = tablaS.getParametros(llamada);
        if(parametros.size()!= argumentos.size())
            error("Numero de argumentos incorrecto en la llamada a la funcion: '" + llamada+"'");
        boolean coincide = true;
        int i = 0;
        while(i<argumentos.size()&&coincide){
            if (!argumentos.get(i).equals(parametros.get(i))){
                coincide =false;
            }
            i++;
        }
        if(!coincide)
            error("El tipo incorrecto en la llamada a la funcion'"+llamada+"'");
    }



}