package weatherpony.pml.launch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import weatherpony.pml.implementorapi.PMLSetup;

public final class PMLRoot{
	public static final String selfLoadPrefix;
	static{
		File prefixFile = new File(new File("PML"),"dev.load.directory.ini");
		if(prefixFile.exists()){
			String prefix = null;
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixFile)))){
				prefix = reader.readLine();
			}catch(Exception e){
			}
			if(prefix.isEmpty())
				selfLoadPrefix = null;
			else
				selfLoadPrefix = prefix;
			
		}else{
			selfLoadPrefix = null;
		}
	}
	public static void premainload(){
		PMLLoadFocuser.init();
		
		PMLInitialization.preloadClasses();
	}
	public static void premainload2(){
		
	}
	public static void preload(String[] givenargs){
		PMLInitialization.preloadSequence();
		File pmlMain = new File(selfLoadPrefix,"PML");
		if(!pmlMain.exists()){
			installError = new ReportedError(InstallErrors.PMLFolderDoesntExist, null);
			return;
		}
		File pmlLoader = new File(pmlMain, "PML-loader.jar");
		if(!pmlLoader.exists()){
			installError = new ReportedError(InstallErrors.PMLLoaderJarNotFound, null);
			return;
		}
			try {
				Throwable e = addURL((URLClassLoader) Thread.currentThread().getContextClassLoader(), pmlLoader.toURI().toURL());
				if(e != null){
					installError = new ReportedError(InstallErrors.AddURLReflectionError, e);
					return;
				}
			} catch (MalformedURLException e1) {
				installError = new ReportedError(InstallErrors.AddURLReflectionError, e1);
				return;
			}
			
		try{
			first = (Callable) Thread.currentThread().getContextClassLoader().loadClass("weatherpony.pml_loader.PMLLoader").newInstance();
		}catch(ClassNotFoundException e){
			installError = new ReportedError(InstallErrors.PMLLoaderNotFound, e);
			return;
		}catch(Throwable e){
			installError = new ReportedError(InstallErrors.PMLLoaderInitializationFailed, e);
			return;
		}
		load();//seperated due to legacy code. Will be fused later.
	}
	private static Callable<Callable<Callable<Void>>> first;
	public static void load(){
		if(installError != null){
			return;
		}
		
		Callable<Callable<Void>> second;
		Callable<Void> third;
		try{
			second = first.call();
		}catch(Throwable e){
			installError = new ReportedError(InstallErrors.PMLLoaderFailed, e);
			return;
		}
		try{
			third = second.call();
		}catch(Throwable e){
			installError = new ReportedError(InstallErrors.SecondaryPMLCallFailed, e);
			return;
		}
		try{
			third.call();
		}catch(Throwable e){
			installError = new ReportedError(InstallErrors.PMLCoreError, e);
		}
		PMLSetup.getSetup().notifyOfEndCoreLoad();
	}
	public static Throwable addURL(URLClassLoader loader, URL url){
		if(addURL == null){
			try{
				addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				addURL.setAccessible(true);
			}catch(Throwable e){
			}
		}
		{
			try{
				addURL.invoke(loader, url);
				return null;
			}catch(Throwable e){
				return e;
			}
		}
	}
	private static Method addURL;
	
	private static ReportedError installError;
	public static class ReportedError{
		public ReportedError(InstallErrors errorType, Throwable e){
			this.errorType = errorType;
			this.e = e;
			File pmlfolder = new File("PML");
			File pmlselferror = new File(pmlfolder, "PML-selfErrorReport.txt");
			try{
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pmlselferror)));
				PrintWriter printout = new PrintWriter(out);
				out.append("PML errored on last run.");
				out.newLine();
				out.append("error type/placement = "+errorType);
				out.newLine();
				if(e == null){
					out.append("  <Throwable not supplied>");
				}else{
					e.printStackTrace(printout);
				}
				printout.flush();
				printout.close();
			}catch(Exception e1){
				e1.printStackTrace();
			}
			System.err.println("PML errored - type "+errorType);
			e.printStackTrace(System.err);
		}
		public final InstallErrors errorType;
		public final Throwable e;
	}
	public static enum InstallErrors{
		AutoPreLoadNotUsingURLClassLoader,
		PMLFolderDoesntExist,
		PMLLoaderJarNotFound,
		AddURLReflectionError,
		PMLLoaderNotFound,
		PMLLoaderInitializationFailed,
		PMLLoaderFailed,
		SecondaryPMLCallFailed,
		
		PMLCoreError
	}
	public static ReportedError installError(){
		return installError;
	}
	private static void extractPreAgent() throws Exception {
		File preagent = File.createTempFile("PMLPreAgent", ".jar");
		preagent.deleteOnExit();
		Files.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("PMLPreAgent.jar"), preagent.toPath(), StandardCopyOption.REPLACE_EXISTING);
		URL add = preagent.toURI().toURL();
		Throwable e = addURL((URLClassLoader) Thread.currentThread().getContextClassLoader(), add);
		if(e != null){
			if(e instanceof Exception)
				throw (Exception)e;
			throw new Exception(e);
		}
	}
	private static void startPreAgent() throws Exception {
		Class.forName("weatherpony.pmlpreinstrumentation.AgentStart").newInstance();
	}
	private static final List<String> transformationExceptions = new ArrayList();
	private static final List<String> transformationExceptions2 = new ArrayList();
	public static void addTransformationException(String prefix){
		transformationExceptions.add(prefix);
	}
	public static void addTransformationDoubleException(String name){
		transformationExceptions2.add(name);
	}
	private static boolean transform(String name){
		boolean du = true;
		for(String each : transformationExceptions){
			if(name.startsWith(each)){
				du = false;
				break;
			}
		}
		if(!du){
			if(transformationExceptions2.contains(name)){
				transformationExceptions2.remove(name);
				return true;
			}
		}
		return du;
	}
	private static final List<String> secondTransformationExceptions = new ArrayList();
	private static final List<String> secondTransformationExceptions2 = new ArrayList();
	public static void addSecondTransformationException(String prefix){
		secondTransformationExceptions.add(prefix);
	}
	public static void addSecondTransformationDoubleException(String name){
		secondTransformationExceptions2.add(name);
	}
	private static boolean secondTransform(String name){
		boolean du = true;
		for(String each : secondTransformationExceptions){
			if(name.startsWith(each)){
				du = false;
				break;
			}
		}
		if(!du){
			if(secondTransformationExceptions2.contains(name)){
				secondTransformationExceptions2.remove(name);
				return true;
			}
		}
		return du;
	}
	public static byte[] transformClass(ClassLoader loader, String name, byte[] data, ProtectionDomain protectionDomain)throws Throwable{
		if(name == null)
			return data;
		try{
			if(secondTransform(name))
				for(IClassManipulator each : secondManipulatorList){
					data = each.transformClass(loader, name, data);
				}
			if(transform(name))
				for(IClassManipulator each : manipulatorList){
					data = each.transformClass(loader, name, data);
				}
			return data;
		}catch(Throwable e){
			e.printStackTrace();
			throw e;
		}
	}
	private static List<IClassManipulator> manipulatorList = new ArrayList();
	public static void addManipulators(IClassManipulator... manipulators){
		addManipulators(Arrays.asList(manipulators));
	}
	public static void addManipulators(List<IClassManipulator> transformers){
		manipulatorList.addAll(transformers);
	}
	private static List<IClassManipulator> secondManipulatorList = new ArrayList();
	public static void addSecondManipulators(IClassManipulator... manipulators){
		addSecondManipulators(Arrays.asList(manipulators));
	}
	public static void addSecondManipulators(List<IClassManipulator> transformers){
		secondManipulatorList.addAll(transformers);
	}
}