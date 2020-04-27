package com.github.davidherdu.jpgOcrTxt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class SplittingPDF {
	
	private static final String SEPARATOR = System.getProperty("file.separator");
	private static final String TESSERACT_PATH = "/usr/local/Cellar/tesseract/4.1.1/share/tessdata/";
	
	//TESSDATA_PREFIX=/usr/local/Cellar/tesseract/4.1.1/share/tessdata/
	
	public static void main(String[] args) throws IOException {
		//System.out.println(System.getenv());
		pdfToImage(args[0]);

	    try (Stream<Path> walk = Files.walk(Paths.get("toImage" + SEPARATOR + args[0]))) {

			List<String> result = walk.filter(Files::isRegularFile)
					.map(x -> x.getFileName().toString()).collect(Collectors.toList());

			File folder = new File("toPdf" + SEPARATOR + args[0]);
			boolean success = folder.mkdirs();
			
			if (success) {
				result.forEach(file -> extractImage(args[0], file, result.size()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void pdfToImage(String fileName) throws IOException {		
		// Loading an existing PDF document
		File file = new File(fileName + ".pdf");
		PDDocument doc = PDDocument.load(file);

		// Instantiating Splitter class
		Splitter splitter = new Splitter();

		// splitting the pages of a PDF document
		List<PDDocument> pages = splitter.split(doc);

		// Creating an iterator
		Iterator<PDDocument> iterator = pages.listIterator();

		// Saving each page as an individual document
		int i = 1;
		File folder = new File("toImage" + SEPARATOR + fileName);
		boolean success = folder.mkdirs();
		
		if (success) {
			System.out.println("Folder created");
			while (iterator.hasNext()) {
				System.out.println("File " + i + " of " + pages.size());
				PDDocument pd = iterator.next();
				PDFRenderer pr = new PDFRenderer(pd);
			    BufferedImage bi = pr.renderImageWithDPI(0, 300);
			    ImageIO.write(bi, "JPEG", new File(folder, i++ + ".jpg")); 
			    pd.close();
			}			
		}

		doc.close();

		System.out.println("PDF splitted");
	}
	
	
	private static void extractImage(String folder, String input, int total) {
		System.out.println("Image " + input.split("\\.")[0] + " of " + total);
		File imageFile = new File("toImage" + SEPARATOR + folder + SEPARATOR + input);
		//System.out.println(System.getenv());
		ITesseract instance = new Tesseract();
		try {
			instance.setDatapath(TESSERACT_PATH);
			instance.setLanguage("lit"); 
			String output = "toPdf" + SEPARATOR + folder + SEPARATOR + input;
			String result = instance.doOCR(imageFile);
			PrintWriter pw = new PrintWriter(output.replace("jpg", "txt"));
			pw.print(result);
			pw.close();
		} catch (TesseractException | FileNotFoundException e) {
			System.err.println(e.getMessage());
		}
	}
}