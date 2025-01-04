package main;

import ClasesAuxiliares.TablaSimbolos;
import analizador.AnalizadorLexico;
import analizador.AnalizadorSintactico;

import java.io.*;

public class Main {


        public static void main(String[] args) {

            int i = 0;
            TablaSimbolos tabla;
            File entrada, salidaToken,salidaTablaS,salidaErrores, salidaParse, entradaSintactico, erroresSintactico;


        try {
                // aqui hay que poner las rutas exactas de donde estan los ficherosç
                entrada = new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\entrada_lexico\\entradaLexico" + i + ".txt");
                salidaToken = new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\salida_tokens\\salidaToken" + i + ".txt");
                salidaTablaS = new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\salida_TS\\salidaTablaS" + i + ".txt");
                salidaErrores = new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\salida_errores_lexico\\salidaErrores" + i + ".txt");
                salidaParse = new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\salida_parse\\salidaParse" + i + ".txt");
                entradaSintactico =  new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\entrada_sintactico\\entradaSintactico" + i + ".txt");
                erroresSintactico = new File("C:\\y todo\\UPM\\Tercero\\5o cuatri\\PDL\\practica\\pruebas\\salida_errores_sintactico\\erroresSintactico" + i + ".txt");
        } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }

            tabla = new TablaSimbolos();
            AnalizadorLexico aL = new AnalizadorLexico(entradaSintactico, salidaToken, salidaTablaS, salidaErrores,tabla);
            AnalizadorSintactico aS = new AnalizadorSintactico(aL,salidaParse,erroresSintactico);
               aS.analizar();
        }

}
