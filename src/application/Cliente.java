package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Cliente extends Application{

	private Socket         socket;
	private BufferedReader recebe;
	private PrintStream    ENVIA;	
	private int            controle = 0;
	private boolean        votou = false;
	private int            port = 40006;
	private Stage          primaryStage;

	ArrayList<Candidato> listaCandidato = null ;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		showMenu();
	}

	public void showMenu(){
		try{
			FXMLLoader loader = new FXMLLoader
					(Cliente.class.getResource("MenuView.fxml"));
			AnchorPane mainView = (AnchorPane) loader.load();
			Scene scene = new Scene(mainView);
			primaryStage.setScene(scene);
			primaryStage.show();

			// Give the controller access to the main app
			MenuViewController controller = loader.getController();
			controller.setCliente(this);

		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void showCandidate(ArrayList<Candidato> candidateList){
		try{
			FXMLLoader loader = new FXMLLoader
					(Cliente.class.getResource("CandidateView.fxml"));
			AnchorPane mainView = (AnchorPane) loader.load();
			Scene scene = new Scene(mainView);
			primaryStage.setScene(scene);
			primaryStage.show();

			// Give the controller access to the main app
			CandidateViewController controller = loader.getController();
			controller.setCliente(this);
			controller.setCandidateList(candidateList);

		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void showConfirmation(ArrayList<Candidato>candidateList, Candidato candidate){
		try{
			FXMLLoader loader = new FXMLLoader
					(Cliente.class.getResource("ConfirmationView.fxml"));
			AnchorPane mainView = (AnchorPane) loader.load();
			Scene scene = new Scene(mainView);
			primaryStage.setScene(scene);
			primaryStage.show();

			// Give the controller access to the main app
			ConfirmationViewController controller = loader.getController();
			controller.setCliente(this);
			controller.setCandidate(candidate);
			controller.setCandidateList(candidateList);

		}catch(IOException e){
			e.printStackTrace();
		}
	}

	//Estabelece conexao com o servidor
	public void abreConexao() throws IOException{
		try {
			socket = new Socket("localhost", port);
			recebe = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ENVIA  = new PrintStream(socket.getOutputStream());

		} catch (Exception e) {
			fecha();
			System.out.println(e);
		}
	}

	//Fecha a conex�o com o servidor
	public void fecha() throws IOException{
		if(recebe != null)
			recebe.close();

		if(ENVIA != null)
			ENVIA.close();

		if(socket != null)
			socket.close();

	}
	
	//Carrega a lista de candidatos que est� no servidor
	public ArrayList<Candidato> carregaCandidatos(String opCod) throws JsonSyntaxException, IOException{		

		if(controle == 0){
			abreConexao();	

			Gson gson =  new Gson();
			listaCandidato = new ArrayList<Candidato>();

			ENVIA.println(opCod);
			ENVIA.flush();

			Type type = new TypeToken<ArrayList<Candidato>>(){}.getType();
			listaCandidato = gson.fromJson(recebe.readLine(), type);

			System.out.println("\nLista de Candidatos carregada.\n");
			fecha();
			controle++;

			//Mostra as informa��es sobre todos os candidatos
			System.out.println("Lista de candidatos");
			for(int i=0; i < listaCandidato.size() ; i++){
				System.out.println("Codigo: "           + listaCandidato.get(i).getCodigo_votacao() + 
						", Nome: "           + listaCandidato.get(i).getNome_candidato() +  
						", Pardido: "        + listaCandidato.get(i).getPartido() +
						", Numero de Votos:" + listaCandidato.get(i).getNum_votos()  );
			}
			System.out.println("\n");
			return listaCandidato;
		}
		else{
			System.out.println("\nLista de Candidatos ja foi carreda. Ja pode votar.\n");
			return listaCandidato;
		}
	}
	
	public void confirmVote(Candidato candidate){

		for(Candidato c : listaCandidato){
			if(c.getCodigo_votacao() == candidate.getCodigo_votacao()){
				int votes = c.getNum_votos();
				votes++;
				c.setNum_votos(votes);
				votou = true;
				return;
			}
		}
	}
	
	//Contabiliza os votos Nulos
	public void votoNulo() throws IOException{
		if(controle == 0){
			System.out.println("Por favor carregue a lista de cadidatos.\n");
		}
		else{
			int posi = 0 ;
			
			System.out.println("Deseja votar Nulo");
			
			System.out.println("Aperte s para confirmar seu voto");
			System.out.println("Aperte n para corrigir seu voto");
			char c = (char)System.in.read();

			if(c == 's'){
				int votos = listaCandidato.get(posi).getNum_votos();
				votos++;
				listaCandidato.get(posi).setNum_votos(votos);
				System.out.println("Seu voto foi registrado com sucesso\n");
				votou = true;

				for(int i=0; i < listaCandidato.size() ; i++){
					System.out.println("Codigo: "           + listaCandidato.get(i).getCodigo_votacao() + 
							", Nome: "           + listaCandidato.get(i).getNome_candidato() +  
							", Pardido: "        + listaCandidato.get(i).getPartido() +
							", Numero de Votos:" + listaCandidato.get(i).getNum_votos()  );
				}
			}
	    }
    }
	
	//Contabiliza os votos Brancos
	public void votoBranco() throws IOException{
		if(controle == 0){
			System.out.println("Por favor carregue a lista de cadidatos.\n");
		}
		else{
			int posi = 1 ;
			
			System.out.println("Deseja votar em Branco");
			
			System.out.println("Aperte s para confirmar seu voto");
			System.out.println("Aperte n para corrigir seu voto");
			char c = (char)System.in.read();

			if(c == 's'){
				int votos = listaCandidato.get(posi).getNum_votos();
				votos++;
				listaCandidato.get(posi).setNum_votos(votos);
				System.out.println("Seu voto foi registrado com sucesso\n");
				votou = true;

				for(int i=0; i < listaCandidato.size() ; i++){
					System.out.println("Codigo: "           + listaCandidato.get(i).getCodigo_votacao() + 
							", Nome: "           + listaCandidato.get(i).getNome_candidato() +  
							", Pardido: "        + listaCandidato.get(i).getPartido() +
							", Numero de Votos:" + listaCandidato.get(i).getNum_votos()  );
				}
			}
	    }
    }
	
	
    public void executaVoto() {
		
		if(controle == 0){ //Checa se a lista de candidato foi carregada 
			System.out.println("Por favor carregue a lista de cadidatos.\n");
		}
		else{
			boolean codigoEncontrado = false;
		    int posi = 0 ;
		    Scanner scanner = new Scanner(System.in);
		    //Fica no Loop at� o usu�rio entrar com um valor correto 
			do{
				System.out.println("Digite o codigo do candidato que deseja votar.\n");
				int codigo = scanner.nextInt();
				
				//Procura o cadidato na lista
				if(codigo != 100 && codigo != 200){ //impede que os votos nulos e brancos sejam contabilizados aqui
					for(int j=0; j<listaCandidato.size(); j++){
						if(listaCandidato.get(j).getCodigo_votacao() == codigo){
							posi = j;
							codigoEncontrado = true;
						}
					}
				}
				
				if(codigoEncontrado == true){
					
					System.out.println("Esse � o seu candidato?");
					System.out.println("Codigo: "  + listaCandidato.get(posi).getCodigo_votacao() + "\n" +
			                           "Nome: "    + listaCandidato.get(posi).getNome_candidato() + "\n" +
			                           "Pardido: " + listaCandidato.get(posi).getPartido() +"\n");
					
					System.out.println("Aperte s para confirmar seu voto");
					System.out.println("Aperte n para corrigir seu voto");
					char c = 0;
					try {
						c = (char)System.in.read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(c == 's'){
						int votos = listaCandidato.get(posi).getNum_votos();
						votos++;
						listaCandidato.get(posi).setNum_votos(votos);
						System.out.println("Seu voto registrado com sucesso\n");
						votou = true;
						
						
						for(int i=0; i < listaCandidato.size() ; i++){
							System.out.println("Codigo: "           + listaCandidato.get(i).getCodigo_votacao() + 
							                   ", Nome: "           + listaCandidato.get(i).getNome_candidato() +  
							                   ", Pardido: "        + listaCandidato.get(i).getPartido() +
							                   ", Numero de Votos:" + listaCandidato.get(i).getNum_votos()  );
						}
					}
				}
				else{
					System.out.println("Por favor, entre com um codigo de candidato valido.\n");
			    }
				
			}while(codigoEncontrado == false);
			
			scanner.close();
	   }
	}
	
	//Encerra a vota�ao da urna, e envia todos os votos para o servidor 
	public void enviaVotosServidor(String opCod){

		if(votou == true){

			Gson gson =  new Gson();

			try {
				abreConexao();	
				String jsonCandidato = gson.toJson(listaCandidato);
				
				ENVIA.println(jsonCandidato);

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else
			System.out.println("Nenhum voto foi registrado");
	}

	public static void main(String[] args) throws UnknownHostException, IOException {

		launch(args);
	}
}
