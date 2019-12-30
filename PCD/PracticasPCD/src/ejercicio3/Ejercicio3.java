package ejercicio3;

import java.util.concurrent.locks.*;

public class Ejercicio3 {
	public static void main(String[] args) {
		Buffer buffer = new Buffer(10);				//Monitor con el buffer
		
		Productor1 p11 = new Productor1(buffer);
		Productor1 p12 = new Productor1(buffer);
		Productor1 p13 = new Productor1(buffer);
		Productor2 p21 = new Productor2(buffer);
		Productor2 p22 = new Productor2(buffer);
		Productor2 p23 = new Productor2(buffer);
		Consumidor1 c11 = new Consumidor1(buffer);
		Consumidor1 c12 = new Consumidor1(buffer);
		Consumidor1 c13 = new Consumidor1(buffer);
		Consumidor2 c21 = new Consumidor2(buffer);
		Consumidor2 c22 = new Consumidor2(buffer);
		Consumidor2 c23 = new Consumidor2(buffer);
		
		p11.start();
		p12.start();
		p13.start();
		p21.start();
		p22.start();
		p23.start();
		c11.start();
		c12.start();
		c13.start();
		c21.start();
		c22.start();
		c23.start();
		
	}
}

class Buffer{
	
	private ReentrantLock l = new ReentrantLock();		//Cerrojo para la exclusion mutua
	private Condition productor = l.newCondition(); 	//Condition para el control de los productores
	private Condition consumidor = l.newCondition();	//Condition para el control de los consumidores
	//private Condition consumidor2 = l.newCondition();	
	private ElementoB buffer[];							//Buffer
	private int tam;									//Variables para controlar por donde hay que devolver objetos y por donde hay que meterlos en el buffer
	private int tipo1,tipo2;							//Variables que controlan la cantidad de objetos de cada tipo
	
	public Buffer(int _tam) {					//Inicializacion del monitor
		buffer = new ElementoB[_tam];
		for(int i=0;i<buffer.length;i++) buffer[i]=null;
		tipo1=tipo2=tam=0;
	}
	
	synchronized private int buscarHueco(ElementoB[] array) {
		for(int i=0; i<array.length; i++) {
			if(array[i]==null) return i;
		}
		return -1;
	}
	
	synchronized private int buscarEleTipo(ElementoB[] array, int tipo) {
		for(int i=0; i<array.length; i++) {
			if(array[i]!=null && array[i].tipo==tipo) return i;
		}
		return -1;
	}
	
	public void depositar(ElementoB ele) throws InterruptedException {
		l.lock();			//Cogemos el cerrojo para cumplir la exclusion mutua es igual en todas las funciones
		try {
			while(tam>=buffer.length) {	//Si el array esta lleno no podemos insertar
				productor.await();
			}
			
			int pos = buscarHueco(buffer);
			buffer[pos]=ele;				//insertamos
			tam++;						//Aumentamos la variable que controla cuantos elementos llevamos
			if(ele.tipo==1) {
				tipo1++;				//Si el elemento insertado es de tipo 1 se aumenta su variable
			} else {
				tipo2++;				//si no se aumenta la otra
//				consumidor2.signalAll();
			}
			consumidor.signal();		//despertamos un consumidor cualquiera
			System.out.println("Insertamos\t Elemento "+ ele.item +" de tipo " + ele.tipo + "\t Quedan " + tipo1 + " Elementos del tipo 1 y \t" + tipo2 + " Elementos del tipo 2");
		}finally {
			l.unlock();					//Devolvemos el cerrojo, igual en todas las funciones
		}
	}
	
	
	public ElementoB extraer(int tipo) throws InterruptedException{
		l.lock();
		try{
			if(tipo == Productor1.TIPO) {
				while(tipo1==0) {		//Si no hay ningun elemento de mi tipo
					consumidor.await();	
				}
			} else {
				while(tipo2==0)			//Si no hay ningun elemento de mi tipo
					consumidor.await();
			}
			
			int pos = buscarEleTipo(buffer, tipo);
			ElementoB ele=buffer[pos];			//Extraemos el elemento
			buffer[pos]=null;					
			tam--;								//Reducimos la cantidad de elementos en el buffer
			//Reducimos la cantidad de elementos de este tipo
			if(tipo == Productor1.TIPO) tipo1--;
			else tipo2--;
			System.out.println("Extraemos\t Elemento "+ ele.item +" de tipo " + tipo + "\t Quedan " + tipo1 + " Elementos del tipo 1 y \t" + tipo2 + " Elementos del tipo 2");
			productor.signal();					//Llamamos a un productor cualquiera
			return ele;							//Devolvemos el elemento
		}finally{
			l.unlock();
		}
	}

	public int getElementos() {
		return tam;
	}
}

class Productor1 extends Thread{
	private Buffer buffer;
	public static final int TIPO = 1;		//Tipo de productor
	
	public Productor1(Buffer b){
		buffer=b;
	}
	
	public void run(){
		for(int i=1;i<20;i++){				//Repetimos 20 veces
			// producir elemento			
			int item = (int)(Math.random()*10);
			try{
				Thread.sleep((int)Math.random()*100);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			// depositar elemento
			try{
				//Insertamos el elemento en el buffer indicando el tipo
				buffer.depositar(new ElementoB(item, TIPO));
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Productor2 extends Thread{
	private Buffer buffer;
	public static final int TIPO = 2;
	
	public Productor2(Buffer b){
		buffer=b;
	}
	
	public void run(){
		//Repetimos 20 veces
		for(int i=1;i<20;i++){
			// producir elemento
			int item = (int)(Math.random()*100);
			try{
				Thread.sleep((int)Math.random()*10);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			// depositar elemento
			try{
				//Insertamos el elemento en el buffer indicando el tipo
				buffer.depositar(new ElementoB(item, TIPO));
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Consumidor1 extends Thread{
	private Buffer buffer;
	
	public Consumidor1(Buffer b){
		buffer=b;
	}
	
	public void run(){
		for(int i=1;i<20;i++){
			// extraer elemento 
			try{
				//Extraemos elemento de nuestro tipo
				ElementoB item = buffer.extraer(Productor1.TIPO);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			// consumir elemento
			try{
				Thread.sleep((int)Math.random()*10);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Consumidor2 extends Thread{
	private Buffer buffer;
	
	public Consumidor2(Buffer b){
		buffer=b;
	}
	
	public void run(){
		for(int i=1;i<20;i++){
			// extraer elemento 
			try{
				//Extraemos elemento de nuestro tipo
				ElementoB item = buffer.extraer(Productor2.TIPO);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			// consumir elemento
			try{
				Thread.sleep((int)Math.random()*10);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

//Definicion de un elemento de buffer
class ElementoB{
	int item;
	int tipo;
	
	public ElementoB(int _item, int _tipo) {
		item=_item;
		tipo=_tipo;
	}
}