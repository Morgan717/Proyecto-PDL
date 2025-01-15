package analizadores;

import clasesAux.GestorErrores;
import clasesAux.PilaSemaforos;
import clasesAux.PosicionActual;
import tablaS.TablaSimbolos;

import java.util.concurrent.Semaphore;

public class AnalizadorSemantico {

    private boolean returnHecho;
    private PilaSemaforos semaforo;
    private GestorErrores gestorE;
    private TablaSimbolos tablaS;
    private String Lexema;
    private PosicionActual pos;
    private int n_param;


    public AnalizadorSemantico(TablaSimbolos tablaS, GestorErrores gestor,PosicionActual p,PilaSemaforos s) {
        returnHecho = false;
        this.semaforo = s;
        this.gestorE= gestor;
        this.tablaS = tablaS;
        this.pos = p; //comunicacion con el sintactico
        Lexema=""; // comunicacion con el lexico
        tablaS.setZona_declaracion(true);// la zona de declaracion empieza en true
    }
    private void error(String mensaje){gestorE.error("Semantico",mensaje);}
    public void setN_param(int i){n_param=i;}
    public int getN_param(){return n_param;}

    // llamaos a procesar cuando emepzamos una produccion
    public void procesar() throws InterruptedException {
        switch (pos.getProduccion()) {

            case "DECL":
                tablaS.setZona_declaracion(true);
                declaracion();
                tablaS.setZona_declaracion(false);
                break;
            case "FUNC":
                tablaS.setZona_declaracion(true);
                funcion();
                tablaS.setZona_declaracion(false);
                break;
            case"PARAMS": parametros();break;
            case "ASIGN":
            case"WHILE":
            case "OUTPUT":
            case"INPUT":
            case "RETURN":
            case"IF":
                sentencias();
                break;
            case"EXP":
                expresiones();
                break;
            default:
                System.err.println("Se ha llamado al semantico con una produccion no reconocida");
        }
    }

    private void funcion() throws InterruptedException {
        returnHecho= false;
        String nombre = Lexema;
        tablaS.agregarAtributo(Lexema,"tipo","funcion");
        tablaS.agregarAtributo(Lexema,"tipo retorno",pos.getTokenActual());
        tablaS.agregarAtributo(Lexema,"desplazamiento",String.valueOf(tablaS.getDespLocal()));
        tablaS.crearTabla(Lexema);
        semaforo.acquire();// necesitamos esperar a que termine para terminar
        if(!returnHecho && !tablaS.getTipo(Lexema).equals("void")){error("No se ha hehco return en la funcion "+nombre);}
        tablaS.liberarTabla();
        returnHecho = false;
    }

    private void declaracion() throws InterruptedException {
        // token actual  string boolean int
        // tokenSig   =  %=  ;
            String Ttipo = pos.getTokenActual();
            int tam = datos(pos.getTokenActual());
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
            semaforo.acquire();
           if(pos.getTokenSig().equals("=")||pos.getTokenSig().equals("%=")){
               // token actual  id
               // tokenSig   =  %=  ;
               if(!Ttipo.equals("int")&& pos.getTokenSig().equals("%=")){
                   error("error de declaracion no puedes hacer asignacion con resto a una variable no entera");
                   return;
               }
                String Atipo =  asignacion();
               if(Atipo.equals("error")){return;}
               else if(!Ttipo.equals(Atipo) && !Atipo.equals("lambda")){
                    error("Error en la declaracion mal puesto el tipo de variable con el contenido");}
                    }
    }

    private String asignacion() throws InterruptedException {
        // token actual  id
        // tokenSig  =  %=  ;
        String siguiente;
        if(pos.getTokenSig().equals("=")){
            semaforo.acquire();
            // token actual =
            // token sig    cte cad id
            siguiente = expresiones();
            return siguiente;
        }
        else if(pos.getTokenSig().equals("%=")){
            semaforo.acquire();
            // token actual %=
            // token sig    cte cad id
            siguiente = expresiones();
            if(!siguiente.equals("int")){
                error("Asignacion con resto %= se ha usado sin enteros");
                return "error";
            }
        }
        return "lambda";
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
    private String sentencias() throws InterruptedException {
        // token actual id while if output input return
        // token sig %= = (
        String Stipo = expresiones(pos.getTokenActual()); // si es ( no hace nah
        semaforo.acquire();
        // token actual %= =
        // token sig  cte cad id
        String Etipo;
        switch(pos.getTokenActual()){
            case "=": case "%=":
                Etipo = expresiones(pos.getTokenSig());
                if(!Stipo.equals(Etipo)){error("Error de asignacion diferentes tipos"); return "error";}
                else return "ok";
            case"while":
                if(Stipo.isEmpty()){
                    semaforo.acquire();
                    // tokenActual cte cad id
                    // tokensig + && ==
                    Etipo = expresiones();
                    if(Etipo.equals("error")){return "error";};
                    if(!Etipo.equals("boolean")){error("Condicion del while no booleana"); return "error";};
                    return "ok";
                }
                return "ok";
            case"if":
                if(Stipo.isEmpty()){
                    semaforo.acquire();
                    // tokenActual cte cad id
                    // tokensig + && ==
                    Etipo = expresiones();
                    if(Etipo.equals("error")){ return "error";};
                    if(!Etipo.equals("boolean")){error("Condicion del if no booleana"); return "error";};
                    return "ok";
                }
            case"ouput":
                // token actual output
                // token sig id cte cad
                semaforo.acquire();
                // token actual + && ==
                // token sig id cte cad
                Etipo = expresiones();
                if (Etipo.equals("error")){ return "error";}
                return "ok";
            case"input":
                semaforo.acquire();
                Etipo = expresiones("id");
                return Etipo;

            case"return":
                // token actual return
                // token sig id cte cad
                Etipo = expresiones(pos.getTokenSig());
                if(!Etipo.equals(tablaS.getTipoRetorno())){error("Tipo de retorno mal"); return "error";}
                returnHecho = true;
                return Etipo;
            default: return "error";
        }

    }
private String expresiones(String lexema){
        // lexema cte cad id
    switch (lexema) {
        case "cad": return "string";
        case "cte": return "int";
        case "id":
            String s = tablaS.getTipo(Lexema);
            if (s.equals("funcion")) {
                error("No se puede hacer una llamada a funcion");
                return "error";
            }
            return s;
        case "(": return "";
        default: return "error";
    }
}
    private String expresiones() throws InterruptedException {
        // token actual cte cad id
        // token sig + && == ;
        String Stipo = expresiones(pos.getTokenActual());
        if(Stipo .equals("error")){return "error";}
        semaforo.acquire();
        // token actual +
        // token sig cte cad id
        String Etipo = expresiones(pos.getTokenSig());
        switch(pos.getTokenActual()){
            case "=": case "%=":
                if (pos.getTokenSig().equals("cte")){ return "int";}
                else if(pos.getTokenSig().equals("cad")){ return "string";}
                else if(pos.getTokenSig().equals("id")){return tablaS.getTipo(Lexema);}
                else {
                    error ("error de expresiones intentas hacer un igual con valores que no puedes");
                    return "error";
                }
            case"+":
                if(Etipo.equals("error")){
                    error("Error de en la expresion suma");
                    return "error";
                } else if (Etipo.equals("boolean")) {
                    error("error de expresion no puedes sumar booleanos");
                    return "error";
                }else if(!Etipo.equals(Stipo)){
                   error("error de expresion no puede sumar tipos de datos distintos");
                    return "error";
                }
                return "int";
            case"==":
                if(Etipo.equals("error")){
                    error("Error de en la expresion comparador");
                    return "error";
                }else if(!Etipo.equals(Stipo)){
                    error("error de expresion no puede comparar tipos de datos distintos");
                    return "error";
                }
                return "boolean";
            case"&&":
                if(Etipo.equals("error")){
                    error("Error de en la expresion AND");
                    return "";
                } else if (!Etipo.equals("boolean")) {
                    error("erro de expresion no puedes hacer && con datos que no sean booleanos");
                    return "";
                }else if(!Etipo.equals(Stipo)){
                    error("error de expresion no puede comparar tipos de datos distintos");
                        return "error";
                }
                return "boolean";
                case ";": return Stipo;
            default:
                return "error";
        }
    }

    public void finSemantico(){ tablaS.imprimirTablaS(); }
    public void setLexema(String uVariable) { this.Lexema = uVariable; }
}
