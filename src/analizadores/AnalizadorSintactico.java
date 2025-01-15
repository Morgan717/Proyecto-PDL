package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PilaSemaforos;
import clasesAux.PosicionActual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class AnalizadorSintactico {

    private PilaSemaforos semaforo;
    private String tokenActual;
    private String tokenSig;

    private PosicionActual pos;
    private GestorErrores gestorE;
    private AnalizadorLexico lexico;
    private AnalizadorSemantico semantico;

    private File fichero;
    private FileWriter salida;

    public AnalizadorSintactico(AnalizadorLexico lexico, File f, GestorErrores gestorE,AnalizadorSemantico semantico,PosicionActual p,PilaSemaforos s) {
        this.semaforo = s;
        this.lexico = lexico;
        this.gestorE=gestorE;
        this.semantico= semantico;
        this.pos = p;
        try {
            this.fichero = f;
            this.salida = new FileWriter(fichero);
            salida.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void liberarSemaforo(){semaforo.release();}

    private void avanzar() {
        tokenActual = tokenSig;
        pos.setTokenActual(tokenActual);
        if(!tokenActual.equals("eof")){
            tokenSig = lexico.obtenerToken();
            pos.setTokenSig(tokenSig);
        }
    }

    private void equipara(String token) {
        if (tokenSig.equals(token)) {
            avanzar();
        }else if(tokenActual == "id"){
                 // este caso en concreto para dar una mas exacta salida de error
                error("Valor no esperado: "+ tokenSig + " detras  de variable ");
                avanzar();
                tokenActual  = token;// marcamos como error pero corregimos para poder seguir analizando de forma correcta
        }
        else if(tokenSig.equals("eof")){
            error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            finSintactico();
            System.exit(1);
        }else {
            error("Se esperaba " + token + ", encontrado " + tokenSig);
            avanzar();
            tokenActual  = token;// marcamos como error pero corregimos para poder seguir analizando de forma correcta
        }

    }

    private void error(String mensaje) {
        gestorE.error("Sintactico",mensaje);
        finSintactico();
        System.exit(1);  // Detener ejecución en caso de error
    }

    public void analizar(){
        tokenActual = lexico.obtenerToken();
        tokenSig = lexico.obtenerToken();
        pos.setTokenActual(tokenActual);
        pos.setTokenSig(tokenSig);
        try {
            salida.write("descendente ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        START();
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

            } else if(tokenActual.equals("id") ||tokenActual.equals("while") || tokenActual.equals("if")
                    || tokenActual.equals("output") || (tokenActual.equals("var"))
                    || tokenActual.equals("input") || tokenActual.equals("return")){
                salida.write("1 ");
                SEN();
                avanzar();
                START();
            }
            else{
                error("Error al principio de linea se ha encontrado un valor no esperado: "+ tokenActual);
                avanzar();
                START();
                // seguimos analizando
            }
        }catch(IOException e){
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
                    pos.setProduccion("ASIGN");
                    ASIGN();
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
                case"eof":
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    finSintactico();
                    System.exit(1);
                    break;
                default:
                    error("Error en el segundo elemento valor no esperado: "+ tokenActual);
                    avanzar();
                    START();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void DECL() {
        // token actual   var
        // token sig   string boolean int
        try {
            salida.write("11 ");
            pos.setProduccion("DECL");
            TIPO();
            // token actual  string boolean int
            // tokenSig   id
            semantico.procesar();
            equipara("id");//en semantico lexema ya esta
            // token actual  id
            // tokenSig   =  %=  ;
            semaforo.release();
            DECLX();
            // token actual  string boolean int
            // tokenSig   =  %= ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void DECLX() {
        // token actual  string boolean int
        // tokenSig   =  %=  ;
        try {
            if (tokenSig.equals("=") || tokenSig.equals("%=")) {
                salida.write("12 ");
                ASIGN();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();

            }else if (tokenSig.equals(";")){
                salida.write("13 "); // sin asignación
            }
            else{
                error("Declaracion mal hecha");
                avanzar();
                START();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void ASIGN() throws InterruptedException {
        // token actual  id
        // token sig %= =
        if(pos.getProduccion().equals("ASIGN")){
            // si venimos del SEN y no de DECL
            semantico.procesar();// procesamos con sentencias
        }
        try {
            if (tokenSig.equals("%=")) {
                salida.write("14 ");
                avanzar();
                // token actual %=
                // token sig    cte cad id
                semaforo.release();
                EXP();
            } else if (tokenSig.equals("=")) {
                salida.write("15 ");
                avanzar();
                // token actual %=
                // token sig    cte cad id
                semaforo.release();
                EXP();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void EXP() {
        // tokenactual  %= = (  output  + && ==
        // tokenSig  cte cad id
        try {
            salida.write("16 ");
            VAL();
            // tokenActual cte cad id
            // tokensig + && == ;
            if(pos.getProduccion().equals("WHILE") || pos.getProduccion().equals("IF") || pos.getProduccion().equals("OUTPUT")){
                semaforo.release();
            }
            EXPX();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void EXPX() {
        pos.setProduccion("EXP");
        // token actual cte cad id
        // token sig + && == ;
        try {
            semantico.procesar();
            if (tokenSig.equals("+")) {
                salida.write("17 ");
                avanzar();
                // token actual +
                // token sig cte cad id
                semaforo.release();
                EXP();
            } else if (tokenSig.equals("&&")) {
                salida.write("18 ");
                avanzar();
                // token actual &&
                // token sig cte cad id
                semaforo.release();
                EXP();
            } else if (tokenSig.equals("==")) {
                salida.write("19 ");
                avanzar();
                // token actual ==
                // token sig cte cad id
                semaforo.release();
                EXP();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);

            }else{
                salida.write("20 ");} // solo VAL
                // token actual id cte cad
                // token sig ;
                semaforo.release();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void VAL() {
        // tokenactual  %= = ( output + && ==
        // tokenSig  cte cad id
        try {
            if (tokenSig.equals("id")) {
                salida.write("21 ");
                equipara("id");

            } else if (tokenSig.equals("cte")) {
                salida.write("22 ");
                equipara("cte");

            } else if (tokenSig.equals("cad")) {
                salida.write("23 ");
                equipara("cad");
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            }else error("Error de variable, valor no esperado: " + tokenSig + " detras de: " + tokenActual );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void WHILE_LOOP() {
        try {
            salida.write("24 ");
            // token actual while
            // token sig (
            pos.setProduccion("WHILE");
            semantico.procesar();
            equipara("(");
            // token actual (
            // token sig cad cte id
            EXP();
            equipara(")");
            equipara("{");
            BODY();
            equipara("}");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void IF_STMT() {
        try {
            salida.write("25 ");
            // token actual if
            // token sig (
            pos.setProduccion("IF");
            semantico.procesar();
            equipara("(");
            // token actual (
            // token sig cad cte id
            EXP();
            equipara(")");
            equipara("{");
            BODY();
            equipara("}");
            // token actual }
            // token sig else o.c
            IF_STMTX();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void IF_STMTX() {
        try {
            if (tokenSig.equals("else")) {
                salida.write("26 ");
                equipara("else");
                equipara("{");
                BODY();
                equipara("}");
            }
            else salida.write("27 ");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void BODY() {
        try {
            if (tokenSig.equals("id") ||tokenSig.equals("while") || tokenSig.equals("if")
                    || tokenSig.equals("output") || (tokenSig.equals("var"))
                    || tokenSig.equals("input") || tokenSig.equals("return")) {
                salida.write("28 ");
                avanzar();
                SEN();
                BODY();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            }else {
                salida.write("29 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void OUTPUT_STMT() {
        // token actual output
        // token sig id cte cad
        try {
            salida.write("30 ");
            pos.setProduccion("OUTPUT");
            semantico.procesar();
            EXP();
        } catch (IOException | InterruptedException e ) {
            throw new RuntimeException(e);
        }
    }

    private void INPUT_STMT() throws InterruptedException {
        // token actual input
        // token sig id
        try {
            salida.write("31 ");
            equipara ("id");
            // token actual id
            // token sig ;
            semaforo.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void RETURN_STMT() {
        try {
           salida.write("32 ");
            RETURN_STMTX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void RETURN_STMTX(){
        try{
            pos.setProduccion("RETURN");
            // token actual return
            // token sig id cte cad ;
                semantico.procesar();
                if (tokenSig.equals("id") || tokenSig.equals("cte") || tokenSig.equals("cad")) {
                    salida.write("33 ");
                    pos.setProduccion("RETURN");
                    EXP();
                }
                else if(tokenSig.equals("eof")){
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    finSintactico();
                    System.exit(1);
                }else {
                    salida.write("34 "); // solo return
                }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void FUNC_DEF() {
        try {
            salida.write("35 ");
            if(tokenSig.equals("int")||tokenSig.equals("string")||
                    tokenSig.equals("boolean")||tokenSig.equals("void")) {
                TIPOX(); // metemos el tipo de funcion
                pos.setProduccion("FUNC");
                // token actual int boolean string void
                // token sig id
                semantico.procesar();// procesamos la funcion
                equipara("id"); // lexema ya actualizado en semantico
                equipara("(");
                PARAMS();
                equipara(")");
                equipara("{");
                BODY();
                equipara("}");
                semaforo.release();
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void PARAMS() {
        try {
            if (tokenSig.equals("int") || tokenSig.equals("string") || tokenSig.equals("boolean") || tokenSig.equals("void")) {
                salida.write("36 ");
                semantico.setN_param(1);
                TIPO();
                // token actual  int string boolean
                // token sig id
                pos.setProduccion("PARAMS");
                semantico.procesar();
                equipara("id");//en semantico lexema ya esta actualizado
                PARAMSX();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            }else {
                salida.write("37 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void PARAMSX() {
        try {
            if(tokenSig.equals(",")) {
                salida.write("38 ");
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
                finSintactico();
                System.exit(1);
            }else {
                salida.write("39 ");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private String TIPO() {
        try {
            switch (tokenSig) {
                case "int":
                    salida.write("40 ");
                    equipara("int");
                    break;
                case "string":
                    salida.write("41 ");
                    equipara("string");
                    break;
                case "boolean":
                    salida.write("42 ");
                    equipara("boolean");
                    break;
                case"eof":
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    finSintactico();
                    System.exit(1);
                    break;
                default:
                    error("error de tipo: tipo no reconocido");
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
                    salida.write("43 ");
                    equipara("int");
                    break;
                case "string":
                    salida.write("44 ");
                    equipara("string");
                    break;
                case "boolean":
                    salida.write("45 ");
                    equipara("boolean");
                    break;
                case "void":
                    salida.write("46 ");
                    break;
                case"eof":
                    error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                    finSintactico();
                    System.exit(1);
                    break;
                default:
                    error("error de tipo: tipo no reconocido");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokenActual;
    }

    public void finSintactico(){
        try {
            salida.flush();
            salida.close();
            gestorE.finGestor();
            semantico.finSemantico();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
