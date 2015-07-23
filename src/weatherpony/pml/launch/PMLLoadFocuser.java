package weatherpony.pml.launch;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import weatherpony.util.misc.Tristate;

public class PMLLoadFocuser{
	public static final String LoadFocuserClassName = "weatherpony.pml.launch.PMLLoadFocuser";
	public static final String pmlClassTransformationMethodName = "pmlClassTransformation";
	public static final String pmlClassSearchOverrideMethodName = "huntForClass";
	public static final String pmlClassLoaderRegistrationMethodName = "registerClassLoader";
	public static final String pmlClassLoaderContextClassLoaderChangeMethodName = "changeInContextClassLoader";
	
	public static final String pmlMainClassTransformerSystemProperty = "PML_mainSwap";
	
	public static String agentargs;
	
	//public static <T2 extends ClassLoader, T extends T2> void registerClassLoader(T loader, Class<T2> fromClass, String constructorDesc){
	public static void registerClassLoader(ClassLoader loader, Class<? extends ClassLoader> fromClass, String constructorDesc){
		try{
			distributeLoadUpdate(new ILoadStateListener.ClassLoaderConstructionUpdate((ClassLoader) loader, fromClass, constructorDesc));
		}catch(Throwable e){
			System.out.println("issue found");
			e.printStackTrace();
			throw e;
		}
	}
	public static void changeInContextClassLoader(Thread thread, ClassLoader loader){
		distributeLoadUpdate(new ILoadStateListener.ThreadChangeContextClassLoaderUpdate(loader, thread));
	}
	static void init(){
		ILoadStateListener.class.getDeclaredClasses();
		LoadNote.init();
		addLoadNote(PMLLoadFocuser.class.getClassLoader(),"weatherpony.pml.");
	}
	static class LoadNote{
		static void init(){}
		LoadNote(String prefix, ClassLoader loader){
			this.namePrefix = prefix;
			this.forceFrom_ifPossible = loader;
		}
		String namePrefix;
		ClassLoader forceFrom_ifPossible;
	}
	static List<LoadNote> notes = new ArrayList();
	static Map<String, List<ClassLoader>> notes2 = new HashMap();
	static Method findClass;
	static{
		try{
			findClass = ClassLoader.class.getDeclaredMethod("findClass", String.class);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		findClass.setAccessible(true);
	}
	private static List<IPMLClassLoaderAncestrySupplier> ancestries;
	static{
		ancestries = new ArrayList();
		addClassLoaderAncestrySupplier(new DefaultAncestrySupplier());
		PMLLoadFocuser.addFocusedLoadNote_late(Tristate.class);
	}
	public static void addClassLoaderAncestrySupplier(IPMLClassLoaderAncestrySupplier supplier){
		ancestries.add(supplier);
	}
	public static Tristate isAncestorClassLoader(ClassLoader base, ClassLoader possibleAncestor, boolean extraDebug){
		if(extraDebug){
			System.err.println("ancestry debug on. base is "+base);
		}
		Tristate state = Tristate.OTHER;
		int cur = 0;
		if(base.equals(possibleAncestor)){
			if(extraDebug){
				System.err.println("base and possibleAncestor are the same");
			}
			return Tristate.TRUE;
		}
		while(!(state.isSolid() || ancestries.size() <= cur)){
			state = ancestries.get(cur).isAncestorClassLoader(base, possibleAncestor, extraDebug);
			if(extraDebug){
				System.err.println("ancestry seach in number "+cur+" gave state "+state);
			}
			cur++;
		}
		if(extraDebug){
			System.err.println("ancestry hunt over");
		}
		return state;
	}
	static class DefaultAncestrySupplier implements IPMLClassLoaderAncestrySupplier{
		private Method isAncestor;
		{
			try{
				isAncestor = ClassLoader.class.getDeclaredMethod("isAncestor",ClassLoader.class);
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			isAncestor.setAccessible(true);
		}
		public Tristate isAncestorClassLoader(ClassLoader base, ClassLoader possibleAncestor, boolean extraDebug){
			if(possibleAncestor == null){
				if(extraDebug){
					System.err.println("possibleAncestor is null");
				}
				return Tristate.OTHER;//System Class Loader?
			}
			if(base == null){
				if(extraDebug){
					System.err.println("self is null - no ancestors, no ClassLoader");
				}
				return Tristate.FALSE;
			}
			try{
				boolean request = (Boolean)isAncestor.invoke(base, possibleAncestor);
				if(extraDebug){
					System.err.println("isAncestor method returned "+request);
				}
				if(request)
					return Tristate.TRUE;
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			base = base.getParent();
			if(base != null)
				return PMLLoadFocuser.isAncestorClassLoader(base, possibleAncestor, extraDebug);
			if(extraDebug){
				System.err.println("no registered ancestor");
			}
			return Tristate.OTHER;
		}
	}
	public static void addLoadNote(ClassLoader forceClassLoader, String classPrefix){
		notes.add(new LoadNote(classPrefix, forceClassLoader));
	}
	public static void addFocusedLoadNote(ClassLoader forceClassLoader, String className){
		List<ClassLoader> loaders = notes2.get(className);
		if(loaders == null){
			notes2.put(className, (loaders = new ArrayList(1)));
		}
		loaders.add(forceClassLoader);
	}
	public static void addFocusedLoadNote_late(Class clazz){
		addFocusedLoadNote(clazz.getClassLoader(), clazz.getName());
	}
	public static Class huntForClass(ClassLoader currentClassLoader, String name){
		//System.out.println("hunting :D ("+currentClassLoader+','+name+')');
		boolean debug = false;
		/*if("weatherpony.seasons.pml.Seasons_PMLMod".equals(name)){
			debug = true;
			System.err.println("hunting for target Class: enabling extra debug");
		}*/
		if(notes2.containsKey(name)){
			if(debug){
				System.err.println("target Class' name in notes2");
			}
			List<ClassLoader> loaders = notes2.get(name);
			for(ClassLoader loader : loaders){
				if(currentClassLoader.equals(loader)){
					if(debug){
						System.err.println("target Class' being hunted for in expected ClassLoader - bailing");
					}
					return null;
				}
				Tristate state = isAncestorClassLoader(currentClassLoader, loader, debug);
				if(debug){
					System.err.println("ancestry state: "+state);
				}
				if(state == Tristate.TRUE){
					if(debug){
						System.err.println("target Class' being hunted for in descendant ClassLoader");
					}
					try{
						return (Class) findClass.invoke(loader, name);
					}catch(Exception e){
						throw new RuntimeException(e);
					}
				}
			}
			if(debug){
				System.err.println("target Class' being hunted for in totally separate ClassLoader... (notes2)");
			}
		}
		for(LoadNote note : notes){
			if(name.startsWith(note.namePrefix)){
				if(currentClassLoader.equals(note.forceFrom_ifPossible)){
					return null;
				}
				Tristate state = isAncestorClassLoader(currentClassLoader, note.forceFrom_ifPossible, debug);
				if(debug){
					System.err.println("ancestry state: "+state);
				}
				if(state == Tristate.TRUE){
					try{
						return (Class) findClass.invoke(note.forceFrom_ifPossible, name);
					}catch(Exception e){
						throw new RuntimeException(e);
					}
				}
			}
		}
		return null;
	}
	private static List<ILoadStateListener> listeners = new ArrayList();
	public static void registerLoadStateListener(ILoadStateListener listener){
		listeners.add(listener);
	}
	public static void distributeLoadUpdate(ILoadStateListener.LoadUpdate update){
		Iterator<ILoadStateListener> iter = listeners.iterator();
		while(iter.hasNext()){
			ILoadStateListener n = iter.next();
			if(!n.stateUpdate(update))
				iter.remove();
		}
	}
	
	public static byte[] pmlClassTransformation(ClassLoader loader, String name, byte[] data, int off, int len, ProtectionDomain protectionDomain)throws Throwable{
		//System.err.println("PMLLoadFocuser: pmlClassTransformation");
		try{
			data = Arrays.copyOfRange(data, off, off+len);
			data = PMLRoot.transformClass(loader, name, data, protectionDomain);
			return data;
		}catch(Throwable e){
			e.printStackTrace();
			throw e;
		}
	}
}
