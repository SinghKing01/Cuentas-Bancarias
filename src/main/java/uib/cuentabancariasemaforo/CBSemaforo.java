package uib.cuentabancariasemaforo;

import java.util.Random;
import java.util.concurrent.Semaphore;
import static uib.cuentabancariasemaforo.CBSemaforo.OPERATIONS;
import static uib.cuentabancariasemaforo.CBSemaforo.saldo;

public class CBSemaforo {

    static final int OPERATIONS = 2;
    static final int CLIENTS = 5;
    static volatile int saldo = 0;

    public static void main(String[] args) throws InterruptedException {
        Semaphore mutex = new Semaphore(1);
        Thread [] clients = new Thread[CLIENTS];
        for (int i = 0; i < CLIENTS; i++) {
            clients[i] = new Thread(new Cliente(i,mutex));
            clients[i].start();
        }
        for (int i = 0; i < CLIENTS; i++) {
            clients[i].join();
        }
        System.out.println("Acaba la simulació");
    }
}

class Cliente implements Runnable {
    int id;
    Semaphore mutex;

    public Cliente(int id, Semaphore mutex) {
        this.id = id;
        this.mutex = mutex;
    }
    
    @Override
    public void run() {
        Random ran = new Random();
        for (int i = 0; i < OPERATIONS; i++) {
            int operacio = ran.nextInt(2); //0: lectura, 1: escriptura
            if (operacio == 0) {
                try {
                    //lectura
                    mutex.acquire();
                    System.out.printf("El client %d fa una colsulta. Disponible: %d\n", id, saldo);
                    mutex.release();
                } catch (InterruptedException ex) {
                }
            } else { //escriptura
                int value = ran.nextInt(100);
                int sign = ran.nextInt(2); //0: positivo, 1: negativo
                if (sign == 1) {
                    value = value * (-1);
                }
                if (saldo + value < 0) {
                    try {
                        mutex.acquire();
                        System.out.printf("\t\t\t\tEl client %d vol retirar %d. Disponible: %d\n", id, -value, saldo);
                        System.out.println("\t\t\t\tSaldo insuficient!!!");
                        mutex.release();
                    } catch (InterruptedException ex) {
                    }
                } else {
                    try {
                        mutex.acquire();
                        saldo += value;
                        if (value > 0) {
                            System.out.printf("\t\tEl client %d ha ingresat %d. Disponible: %d\n", id, value, saldo);
                        } else if (value < 0) {
                            System.out.printf("\t\tEl client %d ha retirat %d. Disponible: %d\n", id, -value, saldo);
                        }
                        mutex.release();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

}
