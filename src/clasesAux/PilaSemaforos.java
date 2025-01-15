package clasesAux;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class PilaSemaforos {

    private  Deque<Thread> pilaEspera ;
    private  Semaphore semaforo ; // Semáforo para bloquear/desbloquear hilos
    private  Object lock; // Para sincronizar el acceso a la pila

    public PilaSemaforos() {
        this.pilaEspera = new LinkedList<>(); // Crear una pila vacía
        this.semaforo = new Semaphore(0); // Semáforo inicializado con 0 permisos
        this.lock = new Object(); // Crear un lock para sincronización
    }
    public void acquire() throws InterruptedException {
        synchronized (lock) {
            pilaEspera.push(Thread.currentThread()); // Agregar el hilo actual a la pila
        }
        semaforo.acquire(); // Bloquear el hilo hasta que sea liberado
    }

    public void release() {
        Thread hiloDesbloqueado = null;
        synchronized (lock) {
            if (!pilaEspera.isEmpty()) {
                hiloDesbloqueado = pilaEspera.pop(); // Obtener el hilo al tope de la pila
            }
        }
        if (hiloDesbloqueado != null) {
            semaforo.release(); // Liberar un permiso
        }
    }
}
