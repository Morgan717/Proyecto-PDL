package main;

import analizadores.AnalizadorSemantico;
import clasesAux.GestorErrores;
import clasesAux.PosicionActual;
import tablaS.TablaSimbolos;
import analizadores.AnalizadorLexico;
import analizadores.AnalizadorSintactico;

import java.io.*;

public class Main {


        public static void main(String[] args) {
            File salidaToken, salidaTablaS, salidaErrores, salidaParse, entrada;
            int i = 10;

            String path ="C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\";

                try {
                    //aqui hay que poner las rutas exactas de donde estan los ficherosç
                    salidaToken = new File(path+"salida_tokens\\salidaToken" + i + ".txt");
                    salidaTablaS = new File(path+"salida_TS\\salidaTablaS" + i + ".txt");
                    salidaParse = new File(path+"salida_parse\\salidaParse" + i + ".txt");
                    entrada = new File(path+"entrada_codigo\\entrada" + i + ".txt");
                    salidaErrores = new File(path+"salida_errores\\errores" + i + ".txt");
                } catch (NullPointerException e) {
                    throw new RuntimeException(e);
                }

                PosicionActual p = new PosicionActual();
                GestorErrores gestor = new GestorErrores(salidaErrores);
                TablaSimbolos tabla = new TablaSimbolos(salidaTablaS,gestor);
                AnalizadorSemantico semantico = new AnalizadorSemantico(tabla, gestor,p);
                AnalizadorLexico aL = new AnalizadorLexico(entrada, salidaToken, gestor,semantico,tabla);
                AnalizadorSintactico aS = new AnalizadorSintactico(aL, salidaParse, gestor,semantico,p);
                aS.analizar();

        }
}
