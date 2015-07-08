package weatherpony.pml.premain;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import weatherpony.pml.launch.PMLLoadFocuser;
import weatherpony.pml.launch.PMLRoot;
import weatherpony.util.classloading.ParentLastClassLoader;

public class ReplacementMainThread extends Thread{
	public static void main(String[] args){
		String proxiedMain = System.getProperty("pml.proxyMain");
		new ReplacementMainThread(proxiedMain, args).start();
	}
	public ReplacementMainThread(String mainClass, String[] args){
		super();
		System.out.println("PML: replacementMainThread being created");
		mainClass = mainClass.replace('/', '.');
		this.replacementloader = new ParentLastClassLoader((URLClassLoader) Thread.currentThread().getContextClassLoader());
		this.main = mainClass;
		this.args = args;
		System.out.println("PML: replacementMainThread created");
	}
	public void setContextClassLoader(ClassLoader loader){
		super.setContextClassLoader(loader);
	}
	String main;
	ClassLoader replacementloader;
	String[] args;
	@Override
	public void run(){
		System.setProperty(PMLLoadFocuser.pmlMainClassTransformerSystemProperty, "t");
		Method proxyMain;
		System.out.println("PML: replacementMainThread replacing ClassLoader");
		this.setContextClassLoader(this.replacementloader);
		System.out.println("PML: replacementMainThread replaced ClassLoader");
		ClassLoader loader = this.getContextClassLoader();
		
		PMLRoot.addTransformationException("org.objectweb.asm");
		PMLRoot.addSecondTransformationException("org.objectweb.asm");
		
		PMLRoot.addTransformationException("com.google.common");
		PMLRoot.addSecondTransformationException("com.google.common");
		
		PMLLoadFocuser.addLoadNote(loader, "com.google.common.");
		
		//fixer.doNotLoadWithYourClassLoader("weatherpony.partial.launch.PMLStart");
		PMLLoadFocuser.addLoadNote(loader, "weatherpony.partial.");
		//fixer.doNotModifyOrLookAtByteCode("weatherpony.partial.");
		PMLRoot.addTransformationException("weatherpony.partial.");
		PMLLoadFocuser.addLoadNote(loader, "weatherpony.util.copies");
		//fixer.doNotModifyOrLookAtByteCode("weatherpony.util.copies");
		PMLRoot.addTransformationException("weatherpony.util.lists");
		//fixer.doNotModifyOrLookAtByteCode("weatherpony.util.lists");
		PMLRoot.addTransformationException("weatherpony.util.lists");
		PMLLoadFocuser.addLoadNote(loader, "weatherpony.util.reflection");
		//fixer.doNotModifyOrLookAtByteCode("weatherpony.util.reflection");
		PMLRoot.addTransformationException("weatherpony.util.reflection");
		PMLLoadFocuser.addLoadNote(loader, "weatherpony.util.structuring");
		//fixer.doNotModifyOrLookAtByteCode("weatherpony.util.structuring");
		PMLRoot.addTransformationException("weatherpony.util.structuring");
		//fixer.doNotModifyOrLookAtByteCode("weatherpony.partial.generated");
		//PMLRoot.addTransformationException("weatherpony.partial.generated");
		
		PMLRoot.preload(args);
		loader = this.getContextClassLoader();
		
		try{
			proxyMain = loader.loadClass(this.main).getMethod("main", String[].class);
		}catch(Throwable e){
			System.err.println("PML-ReplacementMainThread: Critical Error occured");
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
		System.out.println("PML: replaced Thread and launching application");
		
		try{
			proxyMain.invoke(null, new Object[]{args});
		}catch(Exception e){
			System.err.println("PML-ReplacementMainThread: Critical Error occured");
			e.printStackTrace(System.err);
			System.exit(108);
		}
	}
}
