package analizador;

import ClasesAuxiliares.TablaSimbolos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AnalizadorSintactico {

    private String tokenActual;
    private String tokenSig;
    private AnalizadorLexico lexico;
    private TablaSimbolos tabla;
    private File fichero;
    private FileWriter salida;
    private File sError;
    private FileWriter escrituraError;

    public AnalizadorSintactico(AnalizadorLexico lexico, File f, File error) {
        this.lexico = lexico;
        try {
            this.fichero = f;
            sError = error;
            this.salida = new FileWriter(fichero);
            escrituraError= new FileWriter(sError);
            salida.write(""); escrituraError.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.tabla = new TablaSimbolos();
    }

    private void avanzar() {
        tokenActual = tokenSig;
        if(!tokenActual.equals("eof")) tokenSig = lexico.obtenerToken();
    }

    private void equipara(String token) {
        if (tokenSig.equals(token)) {
            avanzar();
        } else {
            error("Error: al equiparar se esperaba " + token + ", encontrado " + tokenSig);

        }
    }

    private void error(String mensaje) {
        try {
            int linea = lexico.lineaActual();
            escrituraError.write(mensaje + " linea de codigo:" +linea +"\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finSintactico();
        System.exit(1);  // Detener ejecución en caso de error
    }
    public void analizar(){
        tokenActual = lexico.obtenerToken();
        tokenSig = lexico.obtenerToken();
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
                error("Error en START token no esperado:"+tokenActual);
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
                default:
                    error("Error en SEN: token inesperado: " + tokenActual);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DECL() {
        try {
            salida.write("11 ");
            TIPO();
            equipara("id");
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
            } else {
                salida.write("13 "); // sin asignación
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ASIGN() {
        try {
            salida.write("14 ");
            ASIGNX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void ASIGNX() {
        try {
            if (tokenSig.equals("%=")) {
                salida.write("15 ");
                equipara("%=");
                EXP();
            } else if (tokenSig.equals("=")) {
                salida.write("16 ");
                equipara("=");
                EXP();
            } else {
                error("Error en ASIGN: operador inesperado " + tokenSig);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void EXP() {
        try {
            salida.write("17 ");
            VAL();
            EXPX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void EXPX() {
        try {
            if (tokenSig.equals("+")) {
                salida.write("18 ");
                OP_ARIT();
            } else if (tokenSig.equals("&&")) {
                salida.write("19 ");
                OP_LOG();
            } else if (tokenSig.equals("==")) {
               salida.write("20 ");
                OP_REL();
            } else salida.write("21 "); // solo VAL
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void VAL() {
        try {
            if (tokenSig.equals("id")) {
                salida.write("22 ");
                equipara("id");
            } else if (tokenSig.equals("cte")) {
                salida.write("23 ");
                equipara("cte");
            } else if (tokenSig.equals("cad")) {
                salida.write("24 ");
                equipara("cad");
            } else error("Error en VAL: valor inesperado " + tokenSig);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void OP_ARIT() {
        try {
            salida.write("25 ");
            equipara("+");
            EXP();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void OP_LOG() {
        try {
            salida.write("26 ");
            equipara("&&");
            EXP();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void OP_REL() {
        try {
            salida.write("27 ");
            equipara("==");
            EXP();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void WHILE_LOOP() {
        try {
            salida.write("28 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        equipara("(");
        EXP();
        equipara(")");
        BLOQUE();
    }

    private void IF_STMT() {
        try {
            salida.write("29 ");
            equipara("(");
            EXP();
            equipara(")");
            BLOQUE();
            IF_STMTX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void IF_STMTX() {
        try {
            if (tokenSig.equals("else")) {
                salida.write("30 ");
                equipara("else");
                BLOQUE();
            } else salida.write("31 ");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void BLOQUE() {
        try {
            if (tokenSig.equals("{")) {
                salida.write("32 ");
                equipara("{");
                BODY();
                equipara("}");
            }
            else{
                error("Error en BLOQUE, token recibido " +tokenSig);
            }
        }catch(IOException e){
            throw new RuntimeException(e);
        }

    }

    private void BODY() {
        try {
            if (tokenSig.equals("id") ||tokenSig.equals("while") || tokenSig.equals("if")
                    || tokenSig.equals("output") || (tokenSig.equals("var"))
                    || tokenSig.equals("input") || tokenSig.equals("return")) {
                salida.write("33 ");
                avanzar();
                SEN();
                BODY();
            } else {
                salida.write("34 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void OUTPUT_STMT() {
        try {
            salida.write("35 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        equipara("(");
        EXP();
        equipara(")");
    }

    private void INPUT_STMT() {
        try {
            salida.write("36 ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        equipara("(");
        equipara ("id");
        equipara(")");
    }

    private void RETURN_STMT() {
        try {
           salida.write("37 ");
            RETURN_STMTX();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void RETURN_STMTX(){
        try{
            if (tokenSig.equals("id") || tokenSig.equals("cte") || tokenSig.equals("cad")) {
                salida.write("38 ");
                EXP();
            } else {
                salida.write("39 "); // solo return
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void FUNC_DEF() {
        try {
            salida.write("40 ");

            if(tokenSig.equals("int")||tokenSig.equals("string")||
                    tokenSig.equals("boolean")||tokenSig.equals("void")) {
                TIPO();
                equipara("id");
                equipara("(");
                PARAMS();
                equipara(")");
                BLOQUE();
            }
            else{
                error("Error en FUNC_DEF: token ineseperado despues de function:" + tokenSig);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void PARAMS() {
        try {

            if (tokenSig.equals("int") || tokenSig.equals("string") || tokenSig.equals("boolean") || tokenSig.equals("void")) {
                salida.write("41 ");
                TIPO();
                equipara("id");
                PARAMSX();
            } else {
                salida.write("42 "); // lambda
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void PARAMSX() {
        try {
            if(tokenSig.equals(",")) {
                salida.write("43 ");
                equipara(",");
                TIPO();
                equipara("id");
                PARAMSX();
            }else {
                salida.write("44 ");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String TIPO() {
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
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokenActual;
    }

    private void finSintactico(){
        try {
            escrituraError.flush();
            salida.flush();
            escrituraError.close();
            salida.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
