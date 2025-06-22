package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PosicionActual;
import clasesAux.TokenBuffer;
import tablaS.TablaSimbolos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AnalizadorSintactico {

    private String tokenActual;
    private String tokenSig;

    private PosicionActual pos;
    private GestorErrores gestorE;
    private AnalizadorLexico lexico;
    private AnalizadorSemantico semantico;
    private TablaSimbolos tablaS;

    private File fichero;
    private FileWriter salida;
    private final TokenBuffer tokenBuffer = new TokenBuffer();

    public AnalizadorSintactico(AnalizadorLexico lexico, File f, GestorErrores gestorE,
                                AnalizadorSemantico semantico, PosicionActual p, TablaSimbolos tablaS) {
        this.lexico = lexico;
        this.gestorE=gestorE;
        this.semantico= semantico;
        this.pos = p;
        this.tablaS=tablaS;

        try {
            this.fichero = f;
            this.salida = new FileWriter(fichero);
            salida.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void avanzar() {

        tokenActual = tokenSig;
        if(!tokenActual.equals("eof")) {
            tokenSig = lexico.obtenerToken();
        }

        pos.setTokenActual(tokenActual);
        pos.setTokenSig(tokenSig);
    }

    private void equipara(String token) {
        if (tokenSig.equals(token)) {
            avanzar();
        }else if(tokenActual == "id"){
            // este caso en concreto para dar una mas exacta salida de error
            error("Valor no esperado: '"+ tokenSig + "' despues de: '" + semantico.getLexema()+"'");
        }
        else if(tokenSig.equals("eof")){
            error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");

        }else if (tokenSig == "id") {
            error("Se esperaba '" + token + "', encontrado '" + semantico.getLexema()+"'");
        }
        else{
            error("Se esperaba '" + token + "', encontrado '" + tokenSig +"'");
        }
    }

    private void error(String mensaje) {
        gestorE.error("Sintactico",mensaje);
    }

    public void analizar(){
        tokenActual = lexico.obtenerToken();
        tokenSig = lexico.obtenerToken();
        pos.setTokenActual(tokenActual);
        pos.setTokenSig(tokenSig);
        // Llenar buffer inicial
        tokenBuffer.addToken(tokenActual, semantico.getLexema(), determinarTipo(tokenActual, semantico.getLexema()));
        tokenBuffer.addToken(tokenSig, semantico.getLexema(), determinarTipo(tokenSig, semantico.getLexema()));

        try {
            salida.write("descendente ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        START();
    }
    private String determinarTipo(String token, String lexema) {
        if ("id".equals(token)) {
            String tipo = tablaS.getTipo(lexema);


            // Si es función, devolver su tipo de retorno
            if ("funcion".equals(tipo)) {
                return tablaS.getTipoRetorno(lexema);
            }
            return tipo != null ? tipo : "error";
        }
        if ("cte".equals(token)) return "int";
        if ("cad".equals(token)) return "string";
        if ("true".equals(token) || "false".equals(token)) return "boolean";
        return token;
    }
    private void START() {
        try {
            if (tokenActual.equals("eof")) {
                salida.write("3");
                finSintactico();
            } else if (tokenActual.equals("function")) {
                salida.write("2 ");
                FUNC_DEF();
                avanzar();
                START();
            } else if (tokenActual.equals("id") || tokenActual.equals("while") || tokenActual.equals("if")
                    || tokenActual.equals("output") || (tokenActual.equals("var"))
                    || tokenActual.equals("input") || tokenActual.equals("return")) {
                salida.write("1 ");
                SEN();
                avanzar();
                START();
            } else {
                error("Error al principio de linea se ha encontrado un valor no esperado: " + tokenActual);
                avanzar();
                START();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void SEN() {
        try {
            switch (tokenActual) {
                case "var":
                    salida.write("4 ");
                    DECL();
                    equipara(";");
                    break;
                case "id":
                    salida.write("5 ");
                    GESTOR();
                    equipara(";");
                    break;
                case "while":
                    salida.write("6 ");
                    WHILE_LOOP();
                    break;
                case "if":
                    salida.write("7 ");
                    IF_STMT();
                    break;
                case "output":
                    salida.write("8 ");
                    OUTPUT_STMT();
                    equipara(";");
                    break;
                case "input":
                    salida.write("9 ");
                    INPUT_STMT();
                    equipara(";");
                    break;
                case "return":
                    salida.write("10 ");
                    RETURN_STMT();
                    equipara(";");
                    break;
                case "eof":
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    break;
                default:
                    error("Error en el segundo elemento valor no esperado: " + tokenActual);
                    avanzar();
                    START();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DECL() {
        try {
            // token actual = var
            salida.write("11 ");
            pos.setTokenActual(TIPO()); // token actual = int;...
            pos.setProduccion("DECL");
            semantico.procesar();
            equipara("id"); // en semantico lexema ya esta actualizado
            DECLX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DECLX() {
        try {
            if (tokenSig.equals("=") || tokenSig.equals("%=")) {
                salida.write("12 ");
                ASIGN();
            } else if (tokenSig.equals("eof")) {
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
            } else if (tokenSig.equals(";")) {
                salida.write("13 "); // sin asignación
            } else {
                error("Declaracion mal hecha");
                avanzar();
                START();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ASIGN() {
        try {
            if (tokenSig.equals("%=")) {
                salida.write("14 ");
                pos.setProduccion("%=");

                // Guardar información del lado izquierdo
                String nombreVar = semantico.getLexema();
                String tipoVar = tablaS.getTipo(nombreVar);
                tokenBuffer.addToken("id", nombreVar, tipoVar);


                // Agregar el operador al buffer
                tokenBuffer.addToken("%=", "%=", "operador");

                avanzar(); // Consumir el %=
                semantico.iniciarExpresion();  // Iniciar nueva expresión para la asignación
                EXP();

                // Procesar semántica de la asignación
                semantico.procesarAsignacionCompleta(tokenBuffer);
                tokenBuffer.clear();  // Limpiar después de procesar

            } else if (tokenSig.equals("=")) {
                salida.write("15 ");
                pos.setProduccion("=");

                // Guardar información del lado izquierdo
                String nombreVar = semantico.getLexema();
                String tipoVar = tablaS.getTipo(nombreVar);
                tokenBuffer.addToken("id", nombreVar, tipoVar);

                // Agregar el operador al buffer
                tokenBuffer.addToken("=", "=", "operador");

                avanzar(); // Consumir el =
                semantico.iniciarExpresion();  // Iniciar nueva expresión para la asignación
                EXP();     // Procesar el lado derecho

                // Procesar semántica de la asignación
                semantico.procesarAsignacionCompleta(tokenBuffer);
                tokenBuffer.clear();  // Limpiar después de procesar

            } else if(tokenSig.equals("eof")) {
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            } else {
                error("Valor no esperado: '" + tokenSig+"' detras de " + tokenActual);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void EXP() {
        try {
            salida.write("16 ");
            semantico.setTokenBuffer(tokenBuffer);

            VAL();
            EXPX();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void EXPX() {
        try {
            String operador = tokenSig;
            if (tokenSig.equals("+")) {
                salida.write("17 ");

                // Agregar operador al buffer
                tokenBuffer.addToken("+", "+", "operador");

                avanzar();
                VAL();
                semantico.procesarOperador(operador);
                EXPX();

            } else if (tokenSig.equals("&&")) {
                salida.write("18 ");

                // Agregar operador al buffer
                tokenBuffer.addToken("&&", "&&", "operador");

                avanzar();
                VAL();
                semantico.procesarOperador(operador);
                EXPX();

            } else if (tokenSig.equals("==")) {
                salida.write("19 ");

                // Agregar operador al buffer
                tokenBuffer.addToken("==", "==", "operador");

                avanzar();
                VAL();
                semantico.procesarOperador(operador);
                EXPX();

            } else if (tokenSig.equals("eof")) {
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            } else {
                salida.write("20 ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void VAL() {
        try {
            if (tokenSig.equals("id")) {
                salida.write("21 ");
                String lexema = semantico.getLexema();
                String tipo = determinarTipo("id", lexema);

                equipara("id");
                semantico.procesarOperando("id", lexema);

                // Agregar identificador al buffer
                tokenBuffer.addToken("id", lexema, tipo);

                if(tokenSig.equals("(")) {
                    salida.write("24 ");
                    FUNC_CALL();
                } else {
                    salida.write("25 ");
                    tokenBuffer.addToken("id", lexema, tablaS.getTipo(lexema));
                }

            } else if (tokenSig.equals("cte")) {
                salida.write("22 ");
                int valor = semantico.getCte();
                equipara("cte");
                semantico.procesarOperando("cte", String.valueOf(valor));

                // Agregar constante al buffer
                tokenBuffer.addToken("cte", String.valueOf(valor), "int");

            } else if (tokenSig.equals("cad")) {
                salida.write("23 ");
                String valor = semantico.getCadena();
                equipara("cad");
                semantico.procesarOperando("cad", valor);

                // Agregar cadena al buffer
                tokenBuffer.addToken("cad", valor, "string");

            } else if(tokenSig.equals("eof")) {
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            } else {
                error("Error de variable, valor no esperado: " + tokenSig);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void WHILE_LOOP() {
        try {
            salida.write("26 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        semantico.iniciarExpresion();  // Iniciar nueva expresión
        equipara("(");
        EXP();  // Procesar condición
        equipara(")");
        equipara("{");
        BODY();
        equipara("}");
    }

    private void IF_STMT() {
        try {
            salida.write("27 ");
            semantico.iniciarExpresion();  // Iniciar nueva expresión
            equipara("(");
            EXP();  // Procesar condición
            equipara(")");
            equipara("{");
            BODY();
            equipara("}");
            IF_STMTX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void IF_STMTX() {
        try {
            if (tokenSig.equals("else")) {
                salida.write("28 ");
                equipara("else");
                equipara("{");
                BODY();
                equipara("}");
            }
            else salida.write("29 ");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void BODY() {
        try {
            if (tokenSig.equals("id") ||tokenSig.equals("while") || tokenSig.equals("if")
                    || tokenSig.equals("output") || (tokenSig.equals("var"))
                    || tokenSig.equals("input") || tokenSig.equals("return")) {
                salida.write("30 ");
                avanzar();
                SEN();
                BODY();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }else {
                salida.write("31 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void OUTPUT_STMT() {
        try {
            salida.write("32 ");
            semantico.iniciarExpresion();  // Iniciar nueva expresión
            EXP();  // Procesar expresión
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void INPUT_STMT() {
        try {
            salida.write("33 ");
            pos.setProduccion("INPUT");

            // Verificar que el siguiente token es un ID
            if (!"id".equals(tokenSig)) {
                error("Input debe seguirse de una variable");
                return;
            }

            semantico.procesar();
            equipara("id");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void RETURN_STMT() {
        try {
            salida.write("34 ");
            RETURN_STMTX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void RETURN_STMTX(){
        try{
            if (tokenSig.equals("id") || tokenSig.equals("cte") || tokenSig.equals("cad")) {
                if(tokenSig.equals("id"))  pos.setTokenSig(semantico.getLexema());
                salida.write("35 ");
                semantico.iniciarExpresion();  // Iniciar nueva expresión para el return
                EXP();
                pos.setProduccion("RETURN");
                semantico.procesar();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }else {
                salida.write("36 "); // solo return
                // Verificación para return sin valor
                pos.setProduccion("RETURN");
                semantico.procesar();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void FUNC_DEF() {
        try {
            salida.write("37 ");
            if(tokenSig.equals("int")||tokenSig.equals("string")||
                    tokenSig.equals("boolean")||tokenSig.equals("void")) {
                pos.setTokenActual(TIPOX()); // metemos el tipo de funcion
                pos.setProduccion("FUNC");
                semantico.procesar();// procesamos la funcion
                equipara("id"); // lexema ya actualizado en semantico
                equipara("(");
                PARAMS();
                equipara(")");
                equipara("{");
                BODY();
                equipara("}");
                semantico.fin_funcion();
            } else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            }
            else{
                error("Error al definir funcion: valor ineseperado despues de function: " + tokenSig);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void PARAMS() {
        try {

            if (tokenSig.equals("int") || tokenSig.equals("string") || tokenSig.equals("boolean") || tokenSig.equals("void")) {
                salida.write("38 ");//importante despues de comprobar puedes entrar y no hacer nada
                semantico.setN_param(1);
                pos.setTokenActual(TIPO());// token actual = int;...
                pos.setProduccion("PARAMS");
                semantico.procesar();
                equipara("id");//en semantico lexema ya esta actualizado
                PARAMSX();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }else {
                salida.write("39 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void PARAMSX() {
        try {
            if(tokenSig.equals(",")) {
                salida.write("40 ");//importante despues de comprobar puedes entrar y no hacer nada
                semantico.setN_param(semantico.getN_param()+1);
                equipara(",");
                pos.setTokenActual(TIPO());// token actual = int;...
                pos.setProduccion("PARAMS");
                semantico.procesar();
                equipara("id");//en semantico lexema ya esta actualizado
                PARAMSX();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }else {
                salida.write("41 ");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String TIPO() {
        try {
            switch (tokenSig) {
                case "int":
                    salida.write("42 ");
                    equipara("int");
                    break;
                case "string":
                    salida.write("43 ");
                    equipara("string");
                    break;
                case "boolean":
                    salida.write("44 ");
                    equipara("boolean");
                    break;
                case"eof":
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    break;
                default:
                    error("error de tipo se esperaba encontrar un tipo y se ha encontrado: " + tokenSig);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokenActual;
    }
    private String TIPOX() {
        try {
            switch (tokenSig) {
                case "int":
                    salida.write("45 ");
                    equipara("int");
                    break;
                case "string":
                    salida.write("46 ");
                    equipara("string");
                    break;
                case "boolean":
                    salida.write("47 ");
                    equipara("boolean");
                    break;
                case "void":
                    salida.write("48 ");
                    equipara("void");
                    break;
                case"eof":
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    break;
                default:
                    error("error de tipo: tipo no reconocido");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokenActual;
    }
    private void GESTOR(){
        try {
            salida.write("49 ");
            GESTORX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void GESTORX(){
        try {
            if (tokenSig.equals("%=")||tokenSig.equals("=")) {
                salida.write("50 ");
                // token actual = id
                ASIGN();
            } else if (tokenSig.equals("(")) {
                salida.write("51 ");
                // token actual es id
                FUNC_CALL();
            }else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            } else {
                error("Valor no esperado: '" + tokenSig+"' detras de " + tokenActual);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void FUNC_CALL(){
        //token actual sabemos que es id
        try {
            salida.write("52 ");
            if(tokenSig.equals("(")) {
                pos.setProduccion("LLAMADA");
                String nombreFuncion = semantico.getLexema();
                String tipoRetorno = tablaS.getTipoRetorno(nombreFuncion);

                tokenBuffer.addToken("id", nombreFuncion, tipoRetorno);
                semantico.procesar();
                equipara("(");//ahora token actual (
                //lexema en el semantico es el id de funcion  a si que llamamos
                ARGUMENTOS();
                equipara(")");
                semantico.finLlamada();
            } else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }
            else{
                error("Error al llamar a una funcion valor no esperado: '"+ tokenSig + "' despues de: '" + semantico.getLexema()+"'");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void ARGUMENTOS(){
        try{
            //token actual (
            if (tokenSig.equals("id") || tokenSig.equals("cte") || tokenSig.equals("cad")) {
                salida.write("53 ");//importante despues de comprobar puedes entrar y no hacer nada
                EXP();// ahora el token actual es id cte cad
                pos.setProduccion("ARGUMENTOS");
                semantico.procesar();
                ARGUMENTOSX();  // posibles más argumentos
            }   else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }else {
                salida.write("54 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void ARGUMENTOSX(){
        try{
            //token actual id cte cad
            if (tokenSig.equals(",")) {
                salida.write("55 "); //importante despues de comprobar puedes entrar y no hacer nada
                equipara(",");
               EXP();// ahora el token actual es id cte cad
                pos.setProduccion("ARGUMENTOS");
                semantico.procesar();
                ARGUMENTOSX();  // posibles más argumentos
            }   else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            }else {
                salida.write("56 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void finSintactico(){
        try {
            salida.flush();
            salida.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}