import java.io.IOException;
import java.util.Scanner;


public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {

		System.out.println("------------------------------------------------");
		System.out.println("Iniciando programa...");
		
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Introduce el número máximo de threads que pueden descargar simultaneamente: ");
	    while (!scanner.hasNextInt()) {
	    	scanner.next();
	    }
	    int maxTh = scanner.nextInt();
		
		FileDownloader fileDownloader = new FileDownloader(maxTh);
		fileDownloader.startFileDownloader();
		
		System.out.println("------------------------------------------------");
		System.out.println("Programa Finalizado");
	}

}
