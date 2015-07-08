package weatherpony.util.classloading;

import java.net.URLClassLoader;

import weatherpony.pml.launch.PMLLoadFocuser;

public class ParentLastClassLoader extends URLClassLoader{

	public ParentLastClassLoader(URLClassLoader parent){
		super(parent.getURLs(), parent);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if(c == null)
			if((name != null) && name.startsWith("java."))//protected package
				c = getParent().loadClass(name);
		if (c == null){
			try{
				c = findClass(name);
			}catch(ClassNotFoundException e){
				c = getParent().loadClass(name);
			}
		}
		if(resolve){
			resolveClass(c);
		}
		return c;
	}
}
