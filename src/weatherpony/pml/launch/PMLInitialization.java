package weatherpony.pml.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

import weatherpony.pml.implementorapi.PMLSetup;

public class PMLInitialization {
	private static final String[] preloadClasses = {
		"weatherpony.pml.implementorapi.IEnviornment",
		"weatherpony.pml.implementorapi.IEnviornmentASMSetup",
		"weatherpony.pml.implementorapi.IPMLModLocator",
		"weatherpony.pml.implementorapi.IPMLPluginManagement",
		"weatherpony.pml.implementorapi.IProgramInformation",
		"weatherpony.pml.implementorapi.IPMLLoadDirector",
		"weatherpony.pml.implementorapi.PMLSetup",
		"weatherpony.pml.implementorapi.StandardPMLModLocator"
		};
	public static void preloadClasses(){
		for(String eachName : preloadClasses)
			try{
				PMLLoadFocuser.addFocusedLoadNote_late(Class.forName(eachName));
			}catch (ClassNotFoundException e){
				e.printStackTrace();
			}
	}
	
	
	public static void preloadSequence(){
		File PMLFolder = new File("PML");
		File PMLINI = new File(PMLFolder,"pml.ini");
		BufferedReader reader = null;
		if(PMLINI.exists()){
			try{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(PMLINI)));
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
		
		String PMLApplicationMainAPIJar = System.getProperty("PMLApplicationMainAPIJar");
		String iniAPIJar = null;
		try{
			if(reader != null)
				iniAPIJar = reader.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}
		if(PMLApplicationMainAPIJar == null){
			PMLApplicationMainAPIJar = iniAPIJar;
		}
		if(PMLApplicationMainAPIJar != null){
			File jar = new File(PMLRoot.selfLoadPrefix, PMLApplicationMainAPIJar);
			if(!jar.exists()){
				jar = new File(PMLApplicationMainAPIJar);
			}
			if(jar.exists()){
				try{
					Throwable e = PMLRoot.addURL((URLClassLoader)Thread.currentThread().getContextClassLoader(), jar.toURI().toURL());
					if(e != null){
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}catch(MalformedURLException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				throw new RuntimeException();
			}
		}
		String PMLApplicationMainAPIClass = System.getProperty("PMLApplicationMainAPIClass");
		//String iniAPIClass = (reader == null) ? null : reader.readLine();
		if(PMLApplicationMainAPIClass == null){
			try{
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("PMLApplicationMainAPIClass.ini");
				if(in != null){
					BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));
					PMLApplicationMainAPIClass = reader2.readLine();
				}
			}catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(PMLApplicationMainAPIClass == null){
			new RuntimeException("PML Application API Main class not found");
		}
		try{
			Class temp = Thread.currentThread().getContextClassLoader().loadClass(PMLApplicationMainAPIClass);
			PMLSetup setup = (PMLSetup) temp.newInstance();
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
}
