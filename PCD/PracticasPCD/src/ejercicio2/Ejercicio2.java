package ejercicio2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Ejercicio2 {
	
	public static ArrayList<String> palabras = new ArrayList<String>();		//Lista de todas las palabras de los hilos
	public static int contador = 0;											//Contador de cuantas palabras llevamos generadas
	public static Semaphore mutexPantalla = new Semaphore(1);				//Semaforo binario para la exclusion mutua de la pantalla
	public static Semaphore mutexArray = new Semaphore(1);					//Semaforo binario para la exclusion mutua del array de todas las palabras
	public static Semaphore mutexContador = new Semaphore(1);				//Semaforo binario para la exclusion mutua del contador
	public static Semaphore terminados = new Semaphore(0);					//Semaforo general para la condicion de sincronizacion
	private static LinkedList<Thread> hilos = new LinkedList<Thread>();		//Lista de hilos para agilizar su ejecución

	public static void main(String[] args) {
		Thread a = new Hilo("A");
		Thread b = new Hilo("B");
		Thread c = new Hilo("C");
		Thread d = new Hilo("D");
		Thread e = new Hilo("E");
		Thread f = new Hilo("F");
		Thread g = new Hilo("G");
		Thread h = new Hilo("H");
		Thread i = new Hilo("I");
		Thread j = new Hilo("J");
		Thread k = new Hilo("K");
		Thread l = new Hilo("L");
		Thread m = new Hilo("M");
		Thread n = new Hilo("N");
		Thread o = new Hilo("O");
		Thread p = new Hilo("P");
		Thread q = new Hilo("Q");
		Thread r = new Hilo("R");
		Thread s = new Hilo("S");
		Thread t = new Hilo("T");
		Thread u = new Hilo("U");
		Thread v = new Hilo("V");
		Thread w = new Hilo("W");
		Thread x = new Hilo("X");
		Thread y = new Hilo("Y");
		Thread z = new Hilo("Z");
		Thread aa = new Hilo("AA");
		Thread bb = new Hilo("BB");
		Thread cc = new Hilo("CC");
		Thread dd = new Hilo("DD");
		
		Collections.addAll(hilos, a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,aa,bb,cc,dd);
		
		for (Thread hilo : hilos) {
			hilo.start();
		}
		
		for (Thread hilo : hilos) {
			try {
				hilo.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}

class Hilo extends Thread {
	
	private String id;						//ID del hilo
	private String palabra;					//Palabra generada por el hilo
	private ArrayList<String> igualLetra;	//Lista con todas las palabras que empiecen por la misma letra que la nuestra
	
	public Hilo(String _id) {
		id = _id;
		igualLetra = new ArrayList<String>();
	}
	
	private String generarPalabra() {		//Generador de palabras aleatorias entre 1 y 10 caracteres
		String palabra = "";
		int n = (int) ((Math.random()*10)+1);
		for(int i = 0; i<n; i++) {
			char c = (char) ((Math.random()*26)+97);
			palabra += c;
		}
		return palabra;
	}
	
	public void run() {
		
		palabra = generarPalabra();			//Generamos la palabra
		
		try {
			Thread.sleep((long) (Math.random()*1000));	//Ralentizamos la generacion de la palabra
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			Ejercicio2.mutexArray.acquire();	//Adquirimos el semaforo del array para añadir la palabra
		} catch (InterruptedException e) {}
		Ejercicio2.palabras.add(palabra);		//Añadimos la palabra al array de palabras
		Ejercicio2.mutexArray.release();		//Devolvemos el semaforo
		
		try {
			Ejercicio2.mutexContador.acquire();	//Adquirimos el semaforo del contador
		} catch (InterruptedException e1) {}
		Ejercicio2.contador++;					//Aumentamos el contador
		Ejercicio2.mutexContador.release();
		
		if(Ejercicio2.contador!=30) {				//Comprobamos que ya estan las 30 palabras
			try {
				Ejercicio2.terminados.acquire();	//Si no estan generadas nos dormimos
			} catch (InterruptedException e) {}
		}
		
		
		for (String pal : Ejercicio2.palabras) { 	//Buscamos las palabras que empiecen por la misma letra que la nuestra
			if(pal.charAt(0)==palabra.charAt(0)) {
				igualLetra.add(pal);
			}
		}
		
		Ejercicio2.terminados.release();			// Despertamos otro hilo
		
		try {
			Ejercicio2.mutexPantalla.acquire();		//Adquirimos el semaforo de la pantalla para imprimir
		} catch (InterruptedException e) {}
		
		//Imprimimos todo
		System.out.println("Hilo " + id);
		System.out.println("Todas las palabras = "+Ejercicio2.palabras.toString());
		System.out.println("Palabra hilo "+ id + " " + palabra);
		System.out.println("Palabras que empiezan con mi letra = " + igualLetra.toString());
		System.out.println("Terminando hilo " + id + "\n");
		
		Ejercicio2.mutexPantalla.release();			//Devolvemos el semaforo de pantalla
	}
	
}