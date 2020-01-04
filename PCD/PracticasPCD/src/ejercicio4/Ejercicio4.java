package ejercicio4;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import messagepassing.*;

class Msj implements Serializable {

	private static final long serialVersionUID = 1L;
	private int tiempo;
	private int id;
	private String impresora;
	
	public Msj(int _tiempo, int _id, String _impresora) {
		this.tiempo = _tiempo;
		this.id = _id;
		this.impresora = _impresora;
	}
	
	public int getId() {
		return id;
	}
	
	public int getTiempo() {
		return tiempo;
	}
	
	public String getImpresora() {
		return impresora;
	}
	
	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}
	
	public void setImpresora(String impresora) {
		this.impresora = impresora;
	}
	
}

class Hilo extends Thread {
	
	private int id;
	private MailBox buzonServidor;
	private MailBox buzonMio;
	private MailBox buzonFinal;
	private MailBox buzonMutex;
	private int tiempo;
	
	public Hilo(int _id, MailBox _buzonServidor, MailBox _buzonMio, MailBox _buzonFinal, MailBox _buzonMutex) {
		this.id = _id;
		this.buzonServidor = _buzonServidor;
		this.buzonMio = _buzonMio;
		this.buzonFinal = _buzonFinal;
		this.buzonMutex = _buzonMutex;
		tiempo = 0;
	}
	
	public void run() {
		
		for(int i=0; i<5; i++) {
			
			try {
				Thread.sleep((int)(Math.random()*1000));				//Generamos un trabajo
			} catch (InterruptedException e) {}

			Msj mensaje = new Msj(tiempo, id, null);
			
			buzonServidor.send(mensaje);								// Enviamos peticion al servidor
			
			Msj imp = (Msj) buzonMio.receive();							// Recibimos cual se nos ha otorgado
			
			tiempo = imp.getTiempo();
			
			try {
				Thread.sleep((int)(Math.random()*tiempo*100));			//Usamos la impresora
			} catch (InterruptedException e) {}
			
			buzonFinal.send(imp.getImpresora());						//Indicamos que hemos dejado de usarla
			
			Object token = buzonMutex.receive();						// Pedimos la exclusion mutua de la pantalla
			
			System.out.println("Hilo "+ id +" ha usado la impresora " + imp.getImpresora());
			System.out.println("Tiempo de uso = " + tiempo);
			try {
				Thread.sleep((int)(Math.random()*100));
			} catch (InterruptedException e) {}
			System.out.println("Hilo "+ id +" liberando impresora "+ imp.getImpresora() + "\n");
			
			buzonMutex.send(token);
			
		}
		
	}
	
	
}

class Servidor extends Thread {
	
	private boolean impresoraA;
	private boolean impresoraB;
	private MailBox buzonServidor;
	private MailBox buzonFinal;
	private MailBox[] buzonesHilos;
	private Selector s;
	private Queue<Msj> colaImpresoraA;
	private Queue<Msj> colaImpresoraB;
	
	
	public Servidor(MailBox[] _buzonesHilos, MailBox _buzonServidor, MailBox _buzonFinal) {
		this.buzonesHilos = _buzonesHilos;
		this.buzonServidor = _buzonServidor;
		this.buzonFinal = _buzonFinal;
		impresoraA = false;
		impresoraB = false;
		s = new Selector();
		s.addSelectable(buzonServidor, false);
		s.addSelectable(buzonFinal, false);
		colaImpresoraA = new LinkedList<>();
		colaImpresoraB = new LinkedList<>();
	}
	
	public void run() {
		
		while(true) {
			buzonServidor.setGuardValue(true);
			buzonFinal.setGuardValue(true);
			
			//System.out.println("Aqui esperando");
			switch(s.selectNonBlocking()) {
				case 1: Msj mensaje = (Msj) buzonServidor.receive();
					int tiempo = (int)(Math.random()*9+1);					//Calculamos el tiempo que tardará
					if(tiempo >= 5 ) {
						mensaje.setTiempo(tiempo);
						colaImpresoraA.add(mensaje);
					} else {
						mensaje.setTiempo(tiempo);						
						colaImpresoraB.add(mensaje);
					}
					break;
			
				case 2: String imp = (String) buzonFinal.receive();
					if(imp.equals("A")) {
						impresoraA = false;
					} else {
						impresoraB = false;
					}
					break;
			}
			
			if(!impresoraA && !colaImpresoraA.isEmpty()) {
				Msj msj = colaImpresoraA.remove();
				msj.setImpresora("A");
				impresoraA = true;
				buzonesHilos[msj.getId()].send(msj);
			}
			
			if(!impresoraB && !colaImpresoraB.isEmpty()) {
				Msj msj = colaImpresoraB.remove();
				msj.setImpresora("B");
				impresoraB = true;
				buzonesHilos[msj.getId()].send(msj);
			}
		}
	}
}

public class Ejercicio4 {
	
	public static final int NHILOS = 30;

	public static void main(String[] args) {
		
		MailBox buzonesHilos[] = new MailBox[NHILOS];
		MailBox buzonServidor = new MailBox();
		MailBox buzonFinal = new MailBox();
		MailBox buzonMutex = new MailBox();
		
		Hilo hilos[] = new Hilo[NHILOS];
		
		for(int i=0; i<NHILOS; i++) {
			buzonesHilos[i] = new MailBox();
			hilos[i] = new Hilo(i, buzonServidor, buzonesHilos[i], buzonFinal, buzonMutex);
		}
		
		Servidor serv = new Servidor(buzonesHilos, buzonServidor, buzonFinal);
		
		serv.start();
		
		buzonMutex.send("token");
		
		for (int i = 0; i<30; i++) {
			hilos[i].start();
		}
		for (int i = 0; i<30; i++) {
			try {
				hilos[i].join();
			} catch (Exception e) {}		
		}
		
	}
	
}
