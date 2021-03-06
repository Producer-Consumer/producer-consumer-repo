package org.producerconsumer.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DocumentationBuilder {

	private static String baseDir = System.getProperty("java.io.tmpdir");
	public static String documentPath = baseDir+File.separator+"RESTfulDocument";
	
	private String css;
	
	private static DocumentationBuilder INSTANCE ;
	
	private File document;
	
	private FileOutputStream outputStream;
	
	private DocumentationBuilder() {
	
		document = new File(documentPath);
		try {
			String style = "<style>"+readCssResource()+"</style>";
			
			String template = "<!DOCTYPE html><html><head><title>RESTful Documentation</title>"+style+"</head><body>";
			
			this.outputStream = new FileOutputStream(document);
			outputStream.write(template.getBytes());
		} catch (IOException e) {
			System.out.println("[echo]:Can't write content to file, as the file is not found/doesn't  have read/write permissions");
		}
	}
	
	private DocumentationBuilder(String css) {
		this.css = css;
		document = new File(documentPath);
		try {
			this.outputStream = new FileOutputStream(document);
			String style = "<style>"+readCss(this.css)+"</style>";
			
			String template = "<!DOCTYPE html><html><head><title>RESTful Documentation</title>"+style+"</head><body>";
			outputStream.write(template.getBytes());
			
		} catch (IOException e) {
			System.out.println("[echo]:Can't write content to file/open file, as the file is not found/doesn't  have read/write permissions");
		}
	}
	
	public static DocumentationBuilder getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new DocumentationBuilder();
			return INSTANCE;
		}
		else
			return INSTANCE;
	}
	
	public static DocumentationBuilder getInstance(String css) {
		if(INSTANCE == null) {
			INSTANCE = new DocumentationBuilder(css);
			return INSTANCE;
		}
		else
			return INSTANCE;
	}
	
	public void writeService(String serviceDocumentation) {
		try {
			String div = "<div>"+serviceDocumentation+"</div>";
			this.outputStream.write(div.getBytes());
		} catch (IOException e) {
			System.out.println("[echo]:Can't write content to file, as the file is not found/doesn't  have read/write permissions");
			
		}
	}
	
	public void closeDocumentation() {
		try {

			String closing = "</body></html>";
			this.outputStream.write(closing.getBytes());
			this.outputStream.close();
		} catch (IOException e) {
			System.out.println("[echo]:Can't close file, as the file is not found/doesn't  have read/write permissions");
			
		}
		
	}
	
	private String readCss(String path) throws IOException {
		File file = new File(path);
		FileInputStream inputStream = new FileInputStream(file);
		byte[] content = inputStream.readAllBytes();
		inputStream.close();
		
		return new String(content);
	}
	
	private String readCssResource() {
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			InputStream inputStream = classLoader.getResource("style.css").openStream();
			byte[] bytes = inputStream.readAllBytes();
			return new String(bytes);
		} catch (IOException e) {
			System.out.println("[echo]:Error while reading resouce CSS"+e.getMessage());
			return "";
		}
	}
	
	
}
