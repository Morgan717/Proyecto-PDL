package analizadores;

import clasesAux.GestorErrores;
import tablaS.TablaSimbolos;

import java.io.*;

public class AnalizadorLexico {


    private GestorErrores gestorE;
    private AnalizadorSemantico semantico;
    private TablaSimbolos tabla;
    private int numeroLineaEntrada;// vamos aumentando segun el numero de lines del fichero entrada
    private int asciicActual;
    private char cActual;// caracter actual manejado

    private File entrada;
    private BufferedReader lectura;// scanner para ir leyendo

    private File salidaToken;
    private FileWriter escrituraToken;// objeto escritura para cada archivo

    public AnalizadorLexico(File entCodigo, File salidaTok,GestorErrores gestorE,AnalizadorSemantico semantico, TablaSimbolos t) {
        numeroLineaEntrada = 1;
        this.gestorE= gestorE;
        this.semantico= semantico;
        this.tabla = t;
        gestorE.setLinea(numeroLineaEntrada);
        try {
            // aqui hay que poner las rutas exactas de donde estan los ficheros
            this.entrada = entCodigo;
            this.salidaToken = salidaTok;
            this.lectura = new BufferedReader(new FileReader(entrada));
            this.escrituraToken = new FileWriter(salidaToken);
            escrituraToken.write("");
            // vaciamos los archivos
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
        // iniciamos la tabla de simbolos global


    }
    private void error(String mensaje){
        // mandamos error y el gestor se encarga de cerrar todo correctamente
        gestorE.error("Lexico",mensaje);
    }

    private boolean avanzar() {
        boolean res = true;
        if (cActual == '\n') gestorE.setLinea(++numeroLineaEntrada);
        try {
            asciicActual = lectura.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (asciicActual == -1) return false;
        else cActual = (char) asciicActual;

        return res;
    }


    private void gestionC() {
        String lexema = "";
        try {
            if (avanzar() && cActual == '*') {
                lectura.mark(1000);// marcamos la posicion actual para poder volver en caso de que no cierre el comentario
                int auxLinea = numeroLineaEntrada;// guardamos tambien el numero de lineas por si volvemos
                boolean correcto = false;
                while (!correcto && avanzar()) {
                    lexema += cActual;
                    // importante comprobar primero correcto
                    if (cActual == '*' && avanzar() && cActual == '/') {
                        // lo metemos en un solo if importante el orden
                        correcto = true;
                    }

                }
                if (!correcto) {
                    lectura.reset();// volvemos hasta el ultimo * leido mandamos error
                    numeroLineaEntrada = auxLinea;
                    gestorE.setLinea(numeroLineaEntrada);
                    error("No se ha cerrado bien el comentario");
                    gestorE.setLinea(++numeroLineaEntrada);
                }
            } else
                error("Caracter no reconocido: "+lexema);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String palabraReservada(String s) {
        try {
            switch (s) {
                case "var":
                    escrituraToken.write("<VAR,>\n");
                    return "var";
                case "int":
                    escrituraToken.write("<INT,>\n");
                    return "int";
                case "boolean":
                    escrituraToken.write("<BOOL,>\n");
                    return "boolean";
                case "function":
                    escrituraToken.write("<FUNCT,>\n");
                    return "function";
                case "if":
                    escrituraToken.write("<IF,>\n");
                    return "if";
                case "input":
                    escrituraToken.write("<INPUT,>\n");
                    return "input";
                case "output":
                    escrituraToken.write("<OUTPUT,>\n");
                    return "output";
                case "return":
                    escrituraToken.write("<RETURN,>\n");
                    return "return";
                case "string":
                    escrituraToken.write("<STRING,>\n");
                    return "string";
                case "void":
                    escrituraToken.write("<VOID,>\n");
                    return "void";
                case "while":
                    escrituraToken.write("<WHILE,>\n");
                    return "while";
                case"else":
                    escrituraToken.write("<ELSE,>\n");
                    return "else";
                case"true":
                        escrituraToken.write("<BOOL,true>\n");
                    return "true";
                case"false":
                    escrituraToken.write("<BOOL,false>\n");
                    return "false";
                default:
                    return "";
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String obtenerToken() {

        if (!avanzar()) {
            try {
                escrituraToken.write("<eof,>\n");
                    finLexico();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "eof";
        }
        switch (cActual) {
            case ' ':  return obtenerToken();
            case '\n': return obtenerToken();
            case '\r': return obtenerToken();
            case '\t': return obtenerToken();

            case '(':
                try {escrituraToken.write("<ParIzq,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return "(";

            case ')':
                try {escrituraToken.write("<ParDrch,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return ")";
            case '{':
                try {escrituraToken.write("<LlavIzq,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return "{";
            case '}':
                try {escrituraToken.write("<LlavDrch,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return "}";
            case ',':
                try {escrituraToken.write("<Coma,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return ",";
            case ';':
                try {escrituraToken.write("<PuntoComa,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return ";";
            case '+':
                try {escrituraToken.write("<OpArSuma,>\n");}
                catch (IOException e) {throw new RuntimeException(e);}
                return "+";
            case '&':
                try {
                    // al hacer avanzar hacemos dos cosas comprobamos si es el final de fichero y avanzamos el char en caso contrario
                    lectura.mark(1);
                    if (avanzar() && cActual == '&') {
                        escrituraToken.write("<OpLogAnd,>\n");
                        return "&&";
                    }
                    else {
                        error("Los '&&' siempre van dos seguidos falta uno");
                        lectura.reset();
                        return "&";
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case '=':
                try {
                    lectura.mark(1);
                    if (avanzar() && cActual == '='){
                        escrituraToken.write("<OpRelIgual,>\n");
                        return "==";
                    }
                    else {
                        escrituraToken.write("<Asig,>\n");
                        lectura.reset();
                        return "=";
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            case '/':
                gestionC();
                return obtenerToken();
            case '%':
                try {
                    lectura.mark(1);
                    if (avanzar() && cActual == '='){
                        escrituraToken.write("<AsigRest,>\n");
                        return "%=";
                    }
                    else {
                        error("Caracter incorrecto a continuación del %");
                        lectura.reset();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case '\'':// caso para las comilla '
                String res1 = "";
                try {
                    lectura.mark(1000);
                    int aux2Linea = numeroLineaEntrada;
                    boolean error = false;
                    while (avanzar()) {

                        if (cActual == '\'')
                            break;//hacemos break para que no se ejecute avanzar() de mas
                        else if (cActual == '\n') {
                            // si se acaba la linea y no hemos terminado es un error
                            error = true;
                            break;
                        } else {
                            res1 += String.valueOf(cActual);
                            if (res1.length() > 64) {
                                error("Longitud de cadena excede máximo de 64 caracteres");
                                break;
                            }
                        }
                    }

                    if (error) {
                        lectura.reset();
                        numeroLineaEntrada = aux2Linea;
                        gestorE.setLinea(numeroLineaEntrada);
                        error("No se ha cerrado la cadena de caracteres");
                        gestorE.setLinea(--numeroLineaEntrada);
                    } else {
                        escrituraToken.write("<Cad,'" + res1 + "'>\n");
                        semantico.setCadena(res1);
                        return "cad";
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            default:
                boolean eof = false;
                if ((cActual >= 'A' && cActual <= 'Z') || (cActual >= 'a' && cActual <= 'z')) {
                    String lexema = String.valueOf(cActual);// pasamos el c a un string
                    try {
                        lectura.mark(100);// marcamos primer caracter por si da error
                        avanzar();
                        while ((cActual >= 'A' && cActual <= 'Z') || (cActual >= 'a' && cActual <= 'z') || Character.isDigit(cActual) || cActual == '_') {// cortamos cuando el lexema acabe
                            lectura.mark(100);// marcamos todos los caracteres para evitar saltar de mas
                            lexema += cActual;
                            // si ya se ha acabado el archivo cerramos
                            if (!avanzar()) {
                                eof = true;
                                break;
                            }

                        }// cuando acabamos mandamos token
                        if (!eof) {
                            // si hemos salido del bucle de forma natural
                            // retrocedemos una posicion
                            lectura.reset();
                        }
                        // reconocemos si es plabra reservada
                        if (lexema.length() <= 64) {
                            String pr = palabraReservada(lexema);

                            if (pr.isEmpty()) {
                                int pos = tabla.add(lexema);
                                semantico.setLexema(lexema);
                                escrituraToken.write("<id," + pos + ">\n");
                                return "id";
                            }
                            else return pr;
                        } else
                            error("Longitud de cadena alfanumerica por encima del maximo de 64");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else if (Character.isDigit(cActual)) {

                    int entero = cActual - '0';// le restamos el valor de 0 en ascii
                    try {
                        lectura.mark(1);// marcamos primer caracter por si da error
                        avanzar();
                        while (Character.isDigit(cActual)) {// cortamos cuando el lexema acabe
                            lectura.mark(1);// marcamos todos los caracteres para evitar saltar de mas
                            entero = entero * 10 + (cActual - '0');
                            // si ya se ha acabado el archivo cerramos
                            if (!avanzar()) {
                                eof = true;
                                break;
                            }

                        }// cuando acabamos mandamos token
                        if (!eof) {
                            // si hemos salido del bucle de forma natural
                            // retrocedemos una posicion
                            lectura.reset();
                        }

                        if (entero <= 32767) {
                            escrituraToken.write("<Cte," + entero + ">\n");// falta la pos en la tabla de simbolos
                            semantico.setCte(entero);
                            return "cte";
                        }
                        else
                            error("Entero superior al limite de 32767");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                        error("Caracter no reconocido: "+cActual);
                }
        }
        // si llegamos aqui es por que no hemos reconocido el token por lo que avanzamos y mandamos el siguiente token
        return obtenerToken();
    }

    public void finLexico(){
        try {
            escrituraToken.flush();
            escrituraToken.close();
             lectura.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}