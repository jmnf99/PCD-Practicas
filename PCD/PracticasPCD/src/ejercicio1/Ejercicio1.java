package ejercicio1;

import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio1 {
	public static ReentrantLock l = new ReentrantLock(); //Cerrojo compartido entre todos los hilos

	public static void main(String[] args) {
		Thread a = new Hilo("A");
		Thread b = new Hilo("B");
		Thread c = new Hilo("C");
		a.start();
		b.start();
		c.start();
		try {
			a.join();
			b.join();
			c.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class Hilo extends Thread{
	
	private String id;			// Id del hilo
	private int[][] array;		// Array con los numeros aleatorios
	private int[] nCifras;		// Array que almacena la cantidad de numeros generados
	
	public Hilo(String _id) {
		id=_id;
		array = new int[10][10];
		nCifras = new int[10];
	}
	
	public void run() {
		for (int i = 0; i < 10; i++) {
			int n;
			for(int j = 0; j<10; j++) {
				for(int k = 0; k<10; k++) {
					n = (int) ((Math.random() * 10) % 10);		//Generamos el numero aleatorio entre 0 y 9
					nCifras[n]++;								//Incrementamos la cantidad del numero correspondiente
					array[j][k] = n;							//Lo metemos en el array
				}
			}
			
			
			Ejercicio1.l.lock();		//Cerramos el cerrojo para que se cumpla la exclusion mutua de la pantalla
			System.out.println("Hilo " + id + "\nArray generado\n");
			for(int j = 0; j<10; j++) {
				for(int k = 0; k<10; k++) {
					System.out.print(array[j][k] + " ");		//Imprimimos todo el array
				}
				System.out.print("\n");
			}
			System.out.println();
			for(int l=0; l<10; l++) {
				System.out.println("Contador de " + l + " = "+ nCifras[l]);		//Imprimimos la cantidad de cifras generadas
			}
			System.out.println("\nTerminando Hilo " + id + "\n");
			Ejercicio1.l.unlock();		//Volvemos a abrir el cerrojo para que otro hilo pueda imprimir 
			
			for(int m=0; m<10; m++) {
				nCifras[m]=0;			//Reiniciamos el array que cuenta la cantidad de cifras generadas
			}
			
		}
	}
}
	