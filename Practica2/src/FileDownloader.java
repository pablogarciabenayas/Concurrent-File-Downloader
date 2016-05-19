import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class FileDownloader {

	private Semaphore nDownMax;
	
	private static final String URL = "https://dl.dropboxusercontent.com/u/1784661/download_list.txt";


	public FileDownloader(int nDownMax) {
		this.nDownMax = new Semaphore(nDownMax);
		
	}
	
	
	public void startFileDownloader() throws IOException,
			InterruptedException {
		URL website = new URL(URL);
        File directory = new File("./Downloads");
        directory.mkdir();
		
		InputStream in = website.openStream();
//		Path pathOut = Paths.get("/home/pablo/Descargas/D/list.txt");
		Path pathOut = Paths.get("./Downloads/list.txt");
		Files.copy(in, pathOut, StandardCopyOption.REPLACE_EXISTING);
		in.close();


		
		String line = "";
//		BufferedReader reader = new BufferedReader(new FileReader("/home/pablo/Descargas/D/list.txt"));
		BufferedReader reader = new BufferedReader(new FileReader("./Downloads/list.txt"));

		while ((line = reader.readLine()) != null) {

			processLine(line);
		}
	}
	
	public void processLine(String line) throws IOException, InterruptedException {
		// System.out.println(l);
		String[] data = line.split("\\s+");

		String url = data[0];
		String fileName = data[1];
		int numberOfParts = Integer.parseInt(data[2]);
		startParallelDownloader(url, fileName, numberOfParts);

	}
	

	public void startParallelDownloader(String urlpart, String fileName,
			int numberOfParts) throws InterruptedException, IOException {
		CountDownLatch cdl = new CountDownLatch(numberOfParts);
		// forma url completa 
		String completeUrl =  urlpart + '/' + fileName + ".part";
		//Lanza hilo de descarga para cada .part
		for (int i = 0; i < numberOfParts; i++) {
			Thread t = new PartDownloaderThread(cdl, fileName+".part"+i,completeUrl+i);
			t.setName(fileName+".part"+i);
			t.start();
		}
		cdl.await();
//		mergeFile("/home/pablo/Descargas/D/",fileName);
		mergeFile("./Downloads",fileName);
		deletePartFiles(fileName, numberOfParts);
	}
	
	public class PartDownloaderThread extends Thread {
		private CountDownLatch stopLatch;
		private String urlPartToDownload;
		private String fileName;

		public PartDownloaderThread(CountDownLatch stopLatch, String fileName,String url) {
			this.stopLatch = stopLatch;
			this.urlPartToDownload = url;
			this.fileName = fileName;

		}

		public void run() {
			try {
				// download part
				downloadPartFile(urlPartToDownload, fileName);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				stopLatch.countDown();
			}
		}
	}



	public void downloadPartFile(String urlPart, String fileName) throws IOException, InterruptedException {
		//Pide permiso para crear un nuevo hilo de descarga simultanea
		nDownMax.acquire(); 
		URL url = new URL(urlPart);
		InputStream inputStream = url.openStream();
//		Path outputPath = Paths.get("/home/pablo/Descargas/D/" + fileName);
		Path outputPath = Paths.get("./Downloads/" + fileName);
		Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
		//Suelta permiso de descarga
		nDownMax.release();
	}

	
	public void mergeFile(String dir, String fileStart) {
		File ofile = new File(dir + "/" + fileStart);
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		String[] files = new File(dir).list((path, name) -> Pattern.matches(
				fileStart + Pattern.quote(".") + "part.*", name));
		Arrays.sort(files);
		try {
			fos = new FileOutputStream(ofile, true);
			for (String fileName : files) {
				File f = new File(dir + "/" + fileName);
				System.out.println(f.getAbsolutePath());
				fis = new FileInputStream(f);
				fileBytes = new byte[(int) f.length()];
				bytesRead = fis.read(fileBytes, 0, (int) f.length());
				assert (bytesRead == fileBytes.length);
				assert (bytesRead == (int) f.length());
				fos.write(fileBytes);
				fos.flush();
				fileBytes = null;
				fis.close();
				fis = null;
			}
			fos.close();
			fos = null;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void deletePartFiles(String filename, int parts) throws IOException {
		for (int i = 0; i < parts; i++) {
//			Path outputPath = Paths.get("/home/pablo/Descargas/D/" + filename+ ".part" + i);
			Path outputPath = Paths.get("./Downloads/" + filename+ ".part" + i);
			Files.delete(outputPath);
		}
	}

}
