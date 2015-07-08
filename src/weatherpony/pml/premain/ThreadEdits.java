package weatherpony.pml.premain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import weatherpony.pml.launch.PMLLoadFocuser;

public class ThreadEdits extends Thread{
	ClassLoader contextClassLoader2;
	@Override
	public void setContextClassLoader(ClassLoader loader){
		//super.setContextClassLoader(loader);//throws a SecurityException if fails, returns if succeeds
		SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        this.contextClassLoader2 = loader;

		try{
			if(loader == null)
				loader = ClassLoader.getSystemClassLoader();
			Class pmlloadfocuser = loader.loadClass(PMLLoadFocuser.LoadFocuserClassName);//got it right here - might as well
			Method cclchangemethod = pmlloadfocuser.getMethod(PMLLoadFocuser.pmlClassLoaderContextClassLoaderChangeMethodName, Thread.class, ClassLoader.class);
			cclchangemethod.setAccessible(true);
			cclchangemethod.invoke(null, this, loader);
		}catch(ClassNotFoundException e){
			//ignore... it's too early...
		}catch(Exception e){
			System.err.println("PML-Thread_edit: critical error in changing the Context ClassLoader");
			e.printStackTrace(System.err);
			System.exit(1000);
		}
	}
}
