package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PosicionActual;
import clasesAux.TokenBuffer;
import tablaS.TablaSimbolos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

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
    private Stack<String> pilaTipos = new Stack<>();

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
    public void iniciarExpresion() {pilaTipos.clear();}  // Limpiar pila al empezar nueva expresión

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
    // procesamos las ctes o cad
    public void procesarOperando(String token, String lexema) {
        switch (token) {
            case "cte":
                pilaTipos.push("int");
                break;
            case "cad":
                pilaTipos.push("string");
                break;
            case "true":
            case "false":
                pilaTipos.push("boolean");
                break;
            case "id":
                String tipo = tablaS.getTipo(lexema);

                // Manejar funciones como su tipo de retorno
                if ("funcion".equals(tipo)) {
                    tipo = tablaS.getTipoRetorno(lexema);
                }

                if (tipo.isEmpty()) {
                    error("Variable no declarada: " + lexema);
                    pilaTipos.push("error");
                } else {
                    pilaTipos.push(tipo);
                }
                break;
            default:
                pilaTipos.push("error");
        }
    }
    // procesamos los + == and &&
    public void procesarOperador(String operador) {
        if (pilaTipos.size() < 2) {
            error("Faltan operandos para " + operador);
            return;
        }

        String der = pilaTipos.pop();
        String izq = pilaTipos.pop();

        switch (operador) {
            case "+":
                if ("int".equals(izq) && "int".equals(der)) {
                    pilaTipos.push("int");
                } else {
                    error("Suma no válida entre " + izq + " y " + der);
                    pilaTipos.push("error");
                }
                break;

            case "&&":
                if ("boolean".equals(izq) && "boolean".equals(der)) {
                    pilaTipos.push("boolean");
                } else {
                    error("Operación lógica no válida entre " + izq + " y " + der);
                    pilaTipos.push("error");
                }
                break;

            case "==":
                if (izq.equals(der)) {
                    pilaTipos.push("boolean");
                } else {
                    error("Comparación inválida entre " + izq + " y " + der);
                    pilaTipos.push("error");
                }
                break;
        }
    }
    public void procesarAsignacionCompleta(TokenBuffer buffer) {
        List<TokenBuffer.TokenInfo> tokens = buffer.getAllTokens();

        // Buscar la posición del operador de asignación
        int posAsignacion = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).token.equals("=") || tokens.get(i).token.equals("%=")) {
                posAsignacion = i;
                break;
            }
        }

        if (posAsignacion == -1) {
            error("Asignación mal formada: no se encontró operador de asignación");
            return;
        }

        // Verificar que haya tokens antes y después del operador
        if (posAsignacion < 1 || posAsignacion >= tokens.size() - 1) {
            error("Asignación incompleta");
            return;
        }

        // Obtener información del lado izquierdo (variable)
        TokenBuffer.TokenInfo varToken = tokens.get(posAsignacion - 1);
        String tipoVar = varToken.tipo;
        String nombreVar = varToken.lexema;

        // Obtener información del lado derecho (expresión)
        TokenBuffer.TokenInfo exprToken = tokens.get(posAsignacion + 1);
        String tipoExpr = exprToken.tipo;

        // Si es una llamada a función, obtener el tipo de retorno real
        if ("funcion".equals(tipoExpr)) {
            tipoExpr = tablaS.getTipoRetorno(exprToken.lexema);
        }

        // Manejar tipos especiales
        if (tipoExpr == null) tipoExpr = "";
        if (tipoVar == null) tipoVar = "";

        // Verificar declaración de variable
        if (tipoVar.isEmpty()) {
            error("Variable no declarada: " + nombreVar);
            return;
        }

        // Obtener operador
        String operador = tokens.get(posAsignacion).token;

        // Verificar compatibilidad de tipos según el operador
        if ("%=".equals(operador)) {
            if (!"int".equals(tipoVar) || !"int".equals(tipoExpr)) {
                error("Asignación %= requiere que ambos lados sean enteros");
            }
        } else {
            if (!tipoVar.equals(tipoExpr)) {
                error("Tipos incompatibles en asignación: " + tipoVar + " vs " + tipoExpr);
            }
        }

        // Limpiar el buffer después de procesar
        buffer.clear();
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
        if (pilaTipos.isEmpty()) {
            error("Asignación sin expresión");
            return;
        }

        String tipoExp = pilaTipos.pop();
        String tipoVar = tablaS.getTipo(Lexema);

        if (tipoVar.isEmpty()) {
            error("Variable no declarada: " + Lexema);
            return;
        }

        if ("%=".equals(pos.getProduccion())) {
            if (!"int".equals(tipoVar) || !"int".equals(tipoExp)) {
                error("Asignación %= requiere enteros");
            }
        } else if (!tipoVar.equals(tipoExp)) {
            error("Tipos incompatibles: " + tipoVar + " vs " + tipoExp);
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
    private void sentencias() {
        switch (pos.getProduccion()) {
            case "=":
            case "%=":
                asignacion();
                break;

            case "OUTPUT":
                if (pilaTipos.isEmpty()) {
                    error("Output sin expresión");
                } else {
                    String tipo = pilaTipos.pop();
                    if ("error".equals(tipo)) {
                        error("Expresión inválida en output");
                    }
                }
                break;

            case "INPUT":
                if (!tablaS.declarado(Lexema)) {
                    error("Variable no declarada: " + Lexema);
                } else {
                    String tipo = tablaS.getTipo(Lexema);
                    if (!tipo.equals("int") && !tipo.equals("string")) {
                        error("Input solo acepta variables enteras o de cadena");
                    }
                }
                break;

            case "RETURN":
                String tipoRetorno = tablaS.getTipoRetorno();

                // Manejar funciones void
                if ("void".equals(tipoRetorno)) {
                    if (!pilaTipos.isEmpty()) {
                        // Extraer y descartar cualquier valor de la pila
                        String tipoExp = pilaTipos.pop();
                        error("Función void no puede retornar un valor (tipo: " + tipoExp + ")");
                    }
                }
                // Manejar funciones no void
                else {
                    if (pilaTipos.isEmpty()) {
                        error("Return sin expresión en función no void");
                    } else {
                        String tipoExp = pilaTipos.pop();
                        if (!tipoRetorno.equals(tipoExp)) {
                            error("Tipo de retorno incorrecto: esperaba " + tipoRetorno + ", recibió " + tipoExp);
                        }
                    }
                }
                break;

            case "WHILE":
            case "IF":
                if (pilaTipos.isEmpty()) {
                    error("Condición faltante en " + pos.getProduccion());
                } else {
                    String tipoCond = pilaTipos.pop();
                    if (!"boolean".equals(tipoCond)) {
                        error("La condición debe ser booleana");
                    }
                }
                break;

            default:
                error("Sentencia incorrecta");
        }
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
    private void argumentos() {
        if (pilaTipos.isEmpty()) {
            error("Argumento inválido");
            return;
        }

        // Obtenemos el tipo del tope de la pila
        String tipoArgumento = pilaTipos.pop();

        // Añadimos el tipo a la lista de argumentos
        argumentos.add(tipoArgumento);
    }
    public void finLlamada() {
        List<String> parametros = tablaS.getParametros(llamada);
        Collections.reverse(argumentos);

        if (parametros.size() != argumentos.size()) {
            error("Número incorrecto de argumentos");
            return;
        }

        for (int i = 0; i < parametros.size(); i++) {
            if (!parametros.get(i).equals(argumentos.get(i))) {
                error("Tipo incorrecto en argumento " + (i+1) + "...");
            }
        }

        // APILAR EL TIPO DE RETORNO DE LA FUNCIÓN
        String tipoRetorno = tablaS.getTipoRetorno(llamada);
        if (!"void".equals(tipoRetorno)) {
            pilaTipos.push(tipoRetorno);
        }

        argumentos.clear();
    }


}