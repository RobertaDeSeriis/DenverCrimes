package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

//i vertici del grafo sono i tipi di reato 
//--> varchar --> grafo<String, DefaultWeightedEdge>
//--> non serve un'idMap

public class Model {
	private Graph<String, DefaultWeightedEdge> grafo; 
	private EventsDao dao; 
	
	private List<String> best; 
	//cammino migliore --> che da sorgente arriva a destinazione 
	//toccando il maggior numero di vertici (percorso di vertici)
	
	public Model() {
		dao= new EventsDao(); 
	}
	
	//passo categoria e mese
	public void creaGrafo(String categoria, int mese) {
		grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class); 
		
		//AGGIUNTA VERTICI
		//i reati di una certa categoria, avvenuti in un dato mese
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
		//AGGIUNTA ARCHI
		for( Adiacenza a: dao.getArchi(categoria, mese)) {
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(), a.getPeso());
		}
		//TODO riempire la tendina degli archi
		
		System.out.println("Grafo creato!"); 
		System.out.println("# VERTICI: "+ this.grafo.vertexSet().size()); 
		System.out.println("# ARCHI: "+ this.grafo.edgeSet().size()); 
	}

	public List<Adiacenza> getArchiMaggioriPesoMedio() {
		//scorro gli archi del grafo e calcolo il peso medio
		double pesoTot=0;
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoTot+= this.grafo.getEdgeWeight(e);
		}
		double avg= pesoTot / this.grafo.edgeSet().size();
		System.out.println("PESO MEDIO :"+avg);
		//ri-scorro tutti gli archi, prendendo quelli maggiori di avg
		
		
		List<Adiacenza> result= new ArrayList<>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) > avg)
			result.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)this.grafo.getEdgeWeight(e))); 
			//restituisce il nodo sorgente dato l'arco
		}
		return result;
	}

	//RICORSIONE: 2 METODI, 1 PUBBLICO CHE PREPARA LE COSE E 1 CHE RICHIAMA QUELLO RICORSIVO
	
	public List <String> calcolaPercorso(String sorgente, String destinazione) {
		best= new ArrayList<>(); 
		List <String> parziale= new LinkedList<>(); 
		parziale.add(sorgente); //sappiamo che sarà il primo passo
		cerca(parziale, destinazione);
		return best; 
	}
	
	private void cerca(List<String> parziale, String destinazione) {
		
		//CONDIZIONE DI TERMINAZIONE
		if (parziale.get(parziale.size()-1).equals(destinazione)) {
			//è la soluzione migliore?
			if(parziale.size() > best.size()) {
				//sovrascrivo facendo una NEW (importante!)
				best= new ArrayList<>(parziale); 
				}
			return;
		}
		
		//PASSO RICORSIVO:  
		//scorro i vicini dell'ultimo inserito e provo le varia strade
		//COME RECUPERARE I VICINI Dato UN NODO
		
		for (String v : Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			//ogni vicino, aggiungo in parziale, lancio ricorsione, faccio backtrakking
			if(!parziale.contains(v)) { //non deve contenere cicli
			parziale.add(v);
			cerca(parziale, destinazione); 
			parziale.remove(parziale.size()-1); //backtrakking rimuovo l'ultimo inserito
			}
		}
	}
	
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<String> getCategorie() {
		return this.dao.getCategorie();
	}
	
	public List<Adiacenza> getArchi() {
		List<Adiacenza> archi= new ArrayList<Adiacenza>(); 
		for (DefaultWeightedEdge e: this.grafo.edgeSet()) {
			archi.add(new Adiacenza(this.grafo.getEdgeSource(e),
									this.grafo.getEdgeTarget(e),
									(int)this.grafo.getEdgeWeight(e)));
		}
		return archi;
	}
}
