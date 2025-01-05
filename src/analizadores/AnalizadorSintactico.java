package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PosicionActual;

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

    private File fichero;
    private FileWriter salida;

    public AnalizadorSintactico(AnalizadorLexico lexico, File f, GestorErrores gestorE,AnalizadorSemantico semantico,PosicionActual p) {
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

    private void avanzar() {
        tokenActual = tokenSig;

        if(!tokenActual.equals("eof")){
            tokenSig = lexico.obtenerToken();
        }
    }

    private void equipara(String token) {
        if (tokenSig.equals(token)) {
            avanzar();
        }else if(tokenActual == "id"){
                error("Error sintatico, valor no esperado: "+ tokenSig + " detras  de variable ");
        }
        else if(tokenSig.equals("eof")){
            error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
            finSintactico();
            System.exit(1);
        }else {
            error("Error al equiparar se esperaba " + token + ", encontrado " + tokenSig);
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
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DECL() {
        try {
            // token actual = var
            salida.write("11 ");
            pos.setTokenActual(TIPO());// token actual = int;...
            pos.setProduccion("DECL");
            semantico.procesar();
            equipara("id");//en semantico lexema ya esta actualizado
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
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
            }else {
                salida.write("13 "); // sin asignación
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ASIGN() {
        try {
            pos.setProduccion("ASIGN");
            if (tokenSig.equals("%=")) {
                salida.write("14 ");
                // token actual = id
                pos.setTokenActual("%=");// nombre del lexema
                avanzar();
                EXP();
            } else if (tokenSig.equals("=")) {
                salida.write("15 ");
                // token actual = id
                pos.setTokenActual("=");// nombre del lexema
                avanzar();
                EXP();
            }else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            } else {
                error("Error al asignar se ha encontrado un operador no esperado: " + tokenSig);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void EXP() {
        try {
            salida.write("16 ");
            // viniendo del while token actual = ?
            // produccion = "WHILE"
            VAL();
            if(!((pos.getProduccion().equals("IF") || pos.getProduccion().equals("WHILE")) && pos.getTokenActual().isEmpty())){
                // esto quiere decir que estamos en la primera iteracion de el while o if a si que saltamos evitamos el procesar
                semantico.procesar();
            }
            // token actual = + , &&, o ==
            pos.setTokenActual(tokenActual);
            EXPX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void EXPX() {
        try {
            if (tokenSig.equals("+")) {
                salida.write("17 ");
                if(!(pos.getProduccion().equals("WHILE")||pos.getProduccion().equals("IF"))){pos.setProduccion("+");}
                else{pos.setTokenActual(tokenSig);}// cambiamos el token para que se vea que ya hemos pasado primera iteracion del while
                avanzar();
                EXP();
            } else if (tokenSig.equals("&&")) {
                salida.write("18 ");
                // si no venimos de while cambiamos la produccion si no no
                if(!(pos.getProduccion().equals("WHILE")||pos.getProduccion().equals("IF"))){pos.setProduccion("&&");}
                else{pos.setTokenActual(tokenSig);}// cambiamos el token para que se vea que ya hemos pasado primera iteracion del while
                avanzar();
                EXP();
            } else if (tokenSig.equals("==")) {
                salida.write("19 ");
                if(!(pos.getProduccion().equals("WHILE")||pos.getProduccion().equals("IF"))){pos.setProduccion("==");}
                else{pos.setTokenActual(tokenSig);}
                avanzar();
                EXP();
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            }else salida.write("20 "); // solo VAL
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void VAL() {
        try {
            if (tokenSig.equals("id")) {
                salida.write("21 ");
                pos.setTokenSig(semantico.getLexema());
                equipara("id");
            } else if (tokenSig.equals("cte")) {
                salida.write("22 ");
                equipara("cte");
                pos.setTokenSig("cte");
            } else if (tokenSig.equals("cad")) {
                salida.write("23 ");
                equipara("cad");
                pos.setTokenSig("cad");
            }
            else if(tokenSig.equals("eof")){
                error("Sentencia incompleta se ha acabado el fichero antes de lo esperado");
                finSintactico();
                System.exit(1);
            }else error("Error de variable, valor no esperado: " + tokenSig );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void WHILE_LOOP() {
        try {
            salida.write("24 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pos.setTokenActual("");
        pos.setProduccion("WHILE");
        equipara("(");
        EXP();
        equipara(")");
        equipara("{");
        BODY();
        equipara("}");
    }

    private void IF_STMT() {
        try {
            salida.write("25 ");
            pos.setTokenActual("");
            pos.setProduccion("IF");
            equipara("(");
            EXP();
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
        try {
            salida.write("30 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // token actual = output
        pos.setTokenActual("saltar");
        pos.setProduccion("OUTPUT");
        EXP();

    }

    private void INPUT_STMT() {
        try {
            salida.write("31 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pos.setTokenActual("saltar");
        pos.setProduccion("INPUT");
        pos.setTokenSig(tokenActual);
        semantico.procesar();
        equipara ("id");
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void FUNC_DEF() {
        try {
            salida.write("35 ");
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
                salida.write("36 ");
                semantico.setN_param(1);
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
                salida.write("37 "); // lambda
            }
        } catch (IOException e) {
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
