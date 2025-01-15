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

            // la carpeta desde donde se ejecuta el .exe
            String basePath = System.getProperty("user.dir");
            // Crear carpeta "pruebas" si no existe
            File pruebasDir = new File(basePath + "/pruebas");
            if (!pruebasDir.exists()) { pruebasDir.mkdirs();}
            int i = 0;// diferentes ficheros

            try {
                // Crear archivos directamente en la carpeta actual
                entrada = new File(pruebasDir, "entrada" + i + ".txt");
                salidaToken = new File(pruebasDir, "salidaToken" + i + ".txt");
                salidaTablaS = new File(pruebasDir, "salidaTablaS" + i + ".txt");
                salidaParse = new File(pruebasDir, "salidaParse" + i + ".txt");
                salidaErrores = new File(pruebasDir, "errores" + i + ".txt");
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
