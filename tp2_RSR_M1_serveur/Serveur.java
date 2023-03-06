package tp2_RSR_M1_serveur;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import utilisateur.Utilisateur;

public class Serveur extends Thread{
	
	private Socket socket;
	static ArrayList<Utilisateur> clients = new ArrayList<Utilisateur>();
	static Boolean auth;
	
	public static int findClientByName(ArrayList<Utilisateur> listOfClients,Utilisateur client) {
		for(int i=0; i < listOfClients.size(); i++) {
			if (client.getId().equals(listOfClients.get(i).getId())) return i;
		}
		return -1;
	}
	
	public static int[][] sumOfmatrix(int[][] A,int[][] B){
		int[][] C = new int[A.length][A[0].length];
		for(int i=0;i<A.length;i++) {
    		for(int j=0;j<A[i].length;j++) {
    			C[i][j] = A[i][j] + B[i][j];
    		}	
    	}
		return C;
	}

	public static int[][] productOfmatrix(int[][] A,int[][] B){
		int[][] C = new int[A.length][B[0].length];
		for(int i=0;i<A.length;i++) {
    		for(int j=0;j<B[0].length;j++) {
    			for(int k=0;k<A[0].length;k++)
    			C[i][j] += A[i][k] * B[k][j];
    		}	
    	}
		return C;
	}
	
	public static int[][] transposeOfmatrix(int[][] A){
		int[][] C = new int[A.length][A[0].length];
		for(int i=0;i<A.length;i++) {
    		for(int j=0;j<A[0].length;j++) {
    			C[j][i] = A[i][j];
    		}	
    	}
		return C;
	}
	
	int[][] subMatrix(int[][] M, int r, int c) {

		int[][] subMatrix = new int[M.length - 1][M.length - 1];

        int k = 0;
        for (int i = 0; i < M.length; i++) {
            int l = 0;
            if (i != r) {
                for (int j = 0; j < M[i].length; j++) {
                    if (j != c)
                        subMatrix[k][l++] = M[i][j];
                }
                k++;
            }
        }

        return subMatrix;
    }

	int sign(int n) {

        if (n % 2 == 0)
            return 1;
        else
            return -1;
    }
    
    public int determinant(int[][] M) {

    	if (M.length == 1) {
            return M[0][0];
        }
    	
        if (M.length == 2) {
            return (M[0][0] * M[1][1]) - (M[0][1] * M[1][0]);
        }
        
        int sum = 0;
        for (int i = 0; i < M[0].length; i++) {
            sum += sign(i) * M[0][i] * determinant(subMatrix(M, 0, i));
        }
        
        return sum;
    }
    
    public int[][] coMatrix(int[][] M) {

    	int[][] t = new int[M.length][M[0].length];

        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < M[i].length; j++)
                t[i][j] = sign(i) * sign(j) * determinant(subMatrix(M, i, j));
        }

        return t;
    }
    
    public double[][] inverseOfMatrix(int[][] m) {
    	
    	int[][] M = transposeOfmatrix(coMatrix(m));
    	
    	double[][] inv = new double[M[0].length][M.length];
    	double det = determinant(m);
    	for (int i = 0; i < M.length; i++)
            for (int j = 0; j < M[i].length; j++)
            	inv[i][j] = M[i][j]/det;

        return inv;

    }
    
    
	
	
	public static void main(String argv[]) {
		int port = 1235;
		
		clients.add(new Utilisateur("zakaria","zaki2213"));//just for the test
		try {
		
		ServerSocket serverSocket = new ServerSocket(port);

		while(true) {	
			System.out.println("Serveur en attente: ");	
			Socket socketClient = serverSocket.accept();			
			Serveur serveur= new Serveur(socketClient);
			serveur.start();
			}

		}catch(Exception e) {
			e.printStackTrace();
			}
		}
	
	public Serveur(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			System.out.println(" adresse client:"+socket.getRemoteSocketAddress() );
			ObjectOutputStream ostream = new ObjectOutputStream (socket.getOutputStream ());

			ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

			Utilisateur client = (Utilisateur)reader.readObject();
			String nom = client.getId();
			String pass = client.getPass();
			
			if (findClientByName(clients,client) != -1) {
				
				if(clients.get(findClientByName(clients,client)).getPass().equals(pass)) {
					System.out.println("Le client: "+nom+" est authentifier!");
					ostream.writeObject("choisissez une opération: \nSomme(+)\nMultiplication(*)\nTransposée(t)\nInverse(i)");
					ostream.flush();
					char opperation = (char)reader.readObject();
					System.out.println(nom+" a choisi "+opperation);
					switch (opperation) {
						case '+':
							ostream.writeObject("Entrez la taille et les elements des deux matrices");
							ostream.flush();
							int[][] M1 = (int[][]) reader.readObject();
							int[][] M2 = (int[][]) reader.readObject();
							ostream.writeObject(sumOfmatrix(M1,M2));
							ostream.flush();
							break;
						case '*':
							ostream.writeObject("Entrez la taille et les elements des deux matrices");
							ostream.flush();
							int[][] M3 = (int[][]) reader.readObject();
							int[][] M4 = (int[][]) reader.readObject();
							ostream.writeObject(productOfmatrix(M3,M4));
							ostream.flush();
							break;
							
						case 't':
							ostream.writeObject("Entrez la taille et les elements de le matrice");
							ostream.flush();
							int[][] M5 = (int[][]) reader.readObject();
							ostream.writeObject(transposeOfmatrix(M5));
							ostream.flush();
							break;
							
						case 'i':
							ostream.writeObject("Entrez la taille et les elements de le matrice");
							ostream.flush();
							int[][] Mi = (int[][]) reader.readObject();
							System.out.println(determinant(Mi));
							ostream.writeObject(inverseOfMatrix(Mi));
							ostream.flush();
							break;
							
						default: break;
					}
					
					
				}
				else {
					ostream.writeObject("mot de passe incorrect pour le client "+client.id);
					ostream.flush();
					ostream.close();
					reader.close();
				}
			}
			else {
				clients.add(client);
				System.out.println("Le client: "+client.id+" est ajouté!");
				ostream.writeObject("choisissez une opération: \nSomme(+)\nMultiplication(*)\nTransposée(t)\nInverse(i)");
				ostream.flush();
				
				char opperation = (char)reader.readObject();
				System.out.println(client.id+" a choisi "+opperation);
				switch (opperation) {
					case '+':
						ostream.writeObject("Entrez la taille et les elements des deux matrices");
						ostream.flush();
						int[][] M1 = (int[][]) reader.readObject();
						int[][] M2 = (int[][]) reader.readObject();
						ostream.writeObject(sumOfmatrix(M1,M2));
						ostream.flush();
						break;
						
					case '*':
						ostream.writeObject("Entrez la taille et les elements des deux matrices");
						ostream.flush();
						int[][] M3 = (int[][]) reader.readObject();
						int[][] M4 = (int[][]) reader.readObject();
						ostream.writeObject(productOfmatrix(M3,M4));
						ostream.flush();
						break;
						
					case 't':
						
						ostream.writeObject("Entrez la taille et les elements de le matrice");
						ostream.flush();
						int[][] M5 = (int[][]) reader.readObject();
						ostream.writeObject(transposeOfmatrix(M5));
						ostream.flush();
						break;
						
					case 'i':
						ostream.writeObject("Entrez la taille et les elements de le matrice");
						ostream.flush();
						int[][] Mi = (int[][]) reader.readObject();
						ostream.writeObject(inverseOfMatrix(Mi));
						ostream.flush();
						break;
						
					default: break;
				}
			}

			
		}catch(Exception e) {

			System.err.println("Erreur:"+e);
	}}
	
	
	}
