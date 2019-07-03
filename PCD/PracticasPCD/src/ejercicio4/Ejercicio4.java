package ejercicio4;

import java.io.Serializable;

import messagepassing.*;

class Msj implements Serializable {

	private static final long serialVersionUID = 1L;
	private int tiempo;
	private String impresora;
	
	public Msj(int _tiempo, String _impresora) {
		this.tiempo = _tiempo;
		this.impresora = _impresora;
	}
	
	public String getImpresora() {
		return impresora;
	}
	
	public int getTiempo() {
		return tiempo;
	}
	
}

class Hilo extends Thread {
	
	private int id;
	private MailBox buzonServidor;
	private MailBox buzon;
	private MailBox buzonFinal;
	private MailBox buzonMutex;
	private int tiempo;
	
	public Hilo(int _id, MailBox _buzonServidor, MailBox _buzon, MailBox _buzonFinal, MailBox _buzonMutex) {
		this.id = _id;
		this.buzonServidor = _buzonServidor;
		this.buzon = _buzon;
		this.buzonFinal = _buzonFinal;
		this.buzonMutex = _buzonMutex;
		tiempo = 0;
	}
	
	public void run() {
		
		for(int i=0; i<5; i++) {
			
			try {
				Thread.sleep((int)(Math.random()*1000));			//Generamos un trabajo
			} catch (InterruptedException e) {}
			
			buzonServidor.send(id);						// Enviamos peticion al servidor
			
			Msj imp = (Msj) buzon.receive();						// Recibimos cual se nos ha otorgado
			
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
	
	
	public Servidor(MailBox[] _buzonesHilos, MailBox _buzonServidor, MailBox _buzonFinal) {
		this.buzonesHilos = _buzonesHilos;
		this.buzonServidor = _buzonServidor;
		this.buzonFinal = _buzonFinal;
		impresoraA = false;
		impresoraB = false;
		s = new Selector();
		s.addSelectable(buzonServidor, false);
		s.addSelectable(buzonFinal, false);
	}
	
	public void run() {
		
		while(true) {
			buzonServidor.setGuardValue(true);
			buzonFinal.setGuardValue(true);
			System.out.println("Aqui esperando");
			switch(s.selectOrBlock()) {
				case 1: int id = (int) buzonServidor.receive();
					int tiempo = (int)(Math.random()*9+1);
					if(tiempo >= 5 ) {
						Msj mens = new Msj(tiempo, "A");
						buzonesHilos[id].send(mens);
						impresoraA = true;
					} else {
						Msj mens = new Msj(tiempo, "B");
						buzonesHilos[id].send(mens);
						impresoraB = true;
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
		}
	}
}

public class Ejercicio4 {

	public static void main(String[] args) {
		
		MailBox buzonesHilos[] = new MailBox[30];
		MailBox buzonServidor = new MailBox();
		MailBox buzonFinal = new MailBox();
		MailBox buzonMutex = new MailBox();
		
		Hilo hilos[] = new Hilo[30];
		
		for(int i=0; i<30; i++) {
			buzonesHilos[i] = new MailBox();
			hilos[i] = new Hilo(i, buzonesHilos[i], buzonServidor, buzonFinal, buzonMutex);
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
