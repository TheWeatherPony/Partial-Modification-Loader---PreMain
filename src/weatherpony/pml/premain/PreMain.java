package weatherpony.pml.premain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import weatherpony.pml.launch.PMLLoadFocuser;
import weatherpony.pml.launch.PMLRoot;
import weatherpony.util.classloading.ParentLastClassLoader;
import weatherpony.util.streams.TeeOutputStream;

public class PreMain{
	public static void main(String[] args){
		String agentArgs = System.getProperty("pml.dev.agentArgs");
		start(agentArgs);
		
		String proxiedMain = System.getProperty("pml.proxyMain");
		new ReplacementMainThread(proxiedMain, args).start();
	}
	public static void premain(String agentargs, Instrumentation i){
		start(agentargs);
	}
	static void start(String agentargs){
		if(agentargs == null)
			agentargs = "";
		PMLLoadFocuser.agentargs = agentargs;
		attemptLoggingStart();
		
		checkMinimumJVMRequirements();
		
		try{
			handleSelfSetup();
		}catch(Exception e){
			throw new Error("Critical Error!", e);
		}
		
		PMLLoadFocuser.addFocusedLoadNote_late(PreMain.class);
		
		ClassLoader norm = ClassLoader.getSystemClassLoader();
		if(norm instanceof URLClassLoader){
				
			PMLRoot.premainload();
			
			try{
				handleAgent();//even though this is a premain, and so has access to an instance of Instrumentation, PML uses an agentmain after backdooring natives, to ensure that classes can be retransformed.
			}catch(Exception e){
				throw new Error("Critical Error in Agent setup!", e);
			}
			
			try{
				Field scl = ClassLoader.class.getDeclaredField("scl"); //system class loader
		        scl.setAccessible(true);
		        URLClassLoader urlnorm = (URLClassLoader)norm;
		        ClassLoader newcl = new ParentLastClassLoader(urlnorm);
				scl.set(null, newcl);
				Thread.currentThread().setContextClassLoader(newcl);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
			PMLRoot.premainload2();
        }else{
        	throw new RuntimeException();
        }
	}
	static void attemptLoggingStart(){
		try {
			File pmloutfolder = new File("PML");
			
			File pmloutlog = new File(pmloutfolder,"PML-OUT_log.txt");
			File pmlerrlog = new File(pmloutfolder,"PML-ERR_log.txt");
			File pmlouterrlog = new File(pmloutfolder,"PML-OUTERR_log.txt");
			File pmlselferror = new File(pmloutfolder, "PML-selfErrorReport.txt");
			if(pmlselferror.exists())
				pmlselferror.delete();
			FileOutputStream outerrfile = new FileOutputStream(pmlouterrlog);
			
			OutputStream outmerge = new TeeOutputStream(new FileOutputStream(pmloutlog),outerrfile);
			OutputStream errmerge = new TeeOutputStream(new FileOutputStream(pmlerrlog),outerrfile);
			
			
			PrintStream out = new PrintStream(new TeeOutputStream(outmerge,System.out));
			PrintStream err = new PrintStream(new TeeOutputStream(errmerge,System.err));
			System.out.println("PML: about to start logging the out stream...");
			System.setOut(out);
			System.out.println("PML: started logging out stream");
			System.err.println("PML: about to start logging the err stream...");
			System.setErr(err);
			System.err.println("PML: started logging err stream");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	static void checkMinimumJVMRequirements(){
		final int[] requiredJavaVersion = {1,7};
		final String javaVersion = System.getProperty("java.specification.version");
		final String[] javaVersionParts = javaVersion.split("\\.");
		final int[] javaVersionParsedParts = new int[javaVersionParts.length];
		for(int cur=0;cur<javaVersionParts.length;cur++){
			javaVersionParsedParts[cur] = Integer.parseInt(javaVersionParts[cur]);
		}
		for(int cur=0;cur<requiredJavaVersion.length;cur++){
			if(javaVersionParsedParts[cur]>=requiredJavaVersion[cur])
				continue;
			String insufficientJavaVersion = "PML Requires a minimum Java version of Java 7 to function, due to features added in said version.";
			System.err.print(insufficientJavaVersion);
			throw new Error();
		}
	}

	static Method addURL;
	static void handleSelfSetup() throws Exception {
		_setupAddURL();
		_setupLoadPMLDependencies();
	}
	private static void _setupAddURL() throws Exception {
		addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURL.setAccessible(true);
	}
	static void addURL(URLClassLoader loader, URL url) throws Exception {
		addURL.invoke(loader, url);
	}
	private static void _setupLoadPMLDependencies(){ 
		File depfile = new File("PML","pml.earlyDependencies.ini");
		if(depfile.exists() && depfile.isFile()){
			URLClassLoader loader = (URLClassLoader) PreMain.class.getClassLoader();
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(depfile)));
				String line = reader.readLine();
				while(line != null && !line.isEmpty()){
					addURL(loader, new File(line).toURI().toURL());
					line = reader.readLine();
				}
				reader.close();
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	static void handleAgent() throws Exception {
		ClassLoader currentLoader =Thread.currentThread().getContextClassLoader();
		if(System.getProperty("pml.dev") == null){
			URL myLocation = PreMain.class.getProtectionDomain().getCodeSource().getLocation();
			Path myloc = Paths.get(myLocation.toURI());
			Path preagent = myloc.resolveSibling("PMLPreAgent.jar");
			addURL((URLClassLoader) currentLoader, preagent.toUri().toURL());//throws Exception
		}
		//after setting up, run the PreAgent, which will extract and install the natives and OS specific code, as well as start the Agent itself
		currentLoader.loadClass("weatherpony.pmlpreinstrumentation.AgentStart").newInstance();
	}
}
