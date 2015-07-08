package weatherpony.pml.premain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import weatherpony.pml.launch.PMLLoadFocuser;
import weatherpony.pml.launch.PMLRoot;

public class PMLBootClassLoader extends URLClassLoader{
	static{
		ClassLoader.registerAsParallelCapable();
	}
	public PMLBootClassLoader(ClassLoader ucl){
		super(((URLClassLoader)ucl).getURLs(),ucl);
		//Thread.currentThread().setContextClassLoader(cl);
		if(this.getClass().getName().startsWith("sun.reflect.")){
			return;
		}
		try{
			Method m = Thread.currentThread().getContextClassLoader().loadClass(PMLLoadFocuser.LoadFocuserClassName)
			.getMethod(PMLLoadFocuser.pmlClassLoaderRegistrationMethodName, ClassLoader.class, Class.class, String.class);
			m.setAccessible(true);
			m.invoke(null, this, PMLBootClassLoader.class, "methodDesc");
		}catch(ClassNotFoundException e){
			//shouldn't happen...
		}catch(InvocationTargetException e){
			throw new RuntimeException(e.getCause());
		}catch(Throwable e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	@Override
	public void addURL(URL url){
		super.addURL(url);
		if(url == null)
			return;
	}
	private void registerClassLoader(){
		//goes at end of all constructors in/extending ClassLoader.class
		try{
			Method m = this.findClass(PMLLoadFocuser.LoadFocuserClassName)
			.getMethod(PMLLoadFocuser.pmlClassLoaderRegistrationMethodName, ClassLoader.class, Class.class, String.class);
			m.setAccessible(true);
			m.invoke(null, this, PMLBootClassLoader.class, "methodDesc");
		}catch(ClassNotFoundException e){
			//shouldn't happen...
		}catch(InvocationTargetException e){
			throw new RuntimeException(e.getCause());
		}catch(Throwable e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	@Override
	public Class loadClass(String name, boolean resolve) throws ClassNotFoundException{
		Class found = this.findLoadedClass(name);
		if(found != null)
			return found;
		if(name.equals(PMLLoadFocuser.LoadFocuserClassName)){//public static final - it'll become a literal constant at compile time
			if(this.getParent() != null){
				return this.getParent().loadClass(name);
			}else
				throw new ClassNotFoundException(name);
		}
		try{
			Class pmlloadfocuser = Class.forName(PMLLoadFocuser.LoadFocuserClassName);
			Method searchOverride = pmlloadfocuser.getMethod(PMLLoadFocuser.pmlClassSearchOverrideMethodName, ClassLoader.class, String.class);
			found = (Class)searchOverride.invoke(null, this, name);
			if(found != null)
				return found;
		}catch(ClassNotFoundException e){//couldn't find PMLLoadFocuser - too early...?
		}catch (InvocationTargetException e){
			Throwable e1 = e.getCause();
			if(e1 instanceof RuntimeException)
				throw (RuntimeException)e1;
			throw new RuntimeException(e1);
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	
	
		return Class.class;
	}
	@Override
	public Class findClass(String name) throws ClassNotFoundException{
		{
			Class found = this.findLoadedClass(name);
			if(found != null)
				return found;
			if(name.equals(PMLLoadFocuser.LoadFocuserClassName)){//public static final - it'll become a literal constant at compile time
				if(this.getParent() != null){
					try{
						Method findClass = ClassLoader.class.getDeclaredMethod("findClass", String.class);
						findClass.setAccessible(true);
						return (Class) findClass.invoke(this.getParent(), name);
					}catch(InvocationTargetException e){
						Throwable cause = e.getCause();
						if(cause instanceof ClassNotFoundException){
							throw (ClassNotFoundException)cause;
						}
						if(cause instanceof RuntimeException)
							throw (RuntimeException)cause;
						throw new RuntimeException(cause);
					}catch(Exception e){
						throw new RuntimeException(e);
					}
				}else
					throw new ClassNotFoundException(name);
			}
			try{
				Class pmlloadfocuser = Class.forName(PMLLoadFocuser.LoadFocuserClassName);
				Method searchOverride = pmlloadfocuser.getMethod(PMLLoadFocuser.pmlClassSearchOverrideMethodName, ClassLoader.class, String.class);
				found = (Class)searchOverride.invoke(null, this, name);
				if(found != null)
					return found;
			}catch(ClassNotFoundException e){//couldn't find PMLLoadFocuser - too early...?
			}catch (InvocationTargetException e){
				Throwable e1 = e.getCause();
				if(e1 instanceof RuntimeException)
					throw (RuntimeException)e1;
				throw new RuntimeException(e1);
			}catch(RuntimeException e){
				throw e;
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		
		return Class.class;
	}
	@SuppressWarnings("resource")
	boolean isAncestor2(ClassLoader cl){
		ClassLoader acl = this;
		do {
			
			//acl = acl.parent;
			acl = acl.getParent();//changed to get ASM code
			if (cl == acl) {
				/*
				 mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 2);
				Label l3 = new Label();
				mv.visitJumpInsn(IF_ACMPNE, l3);
				 */
			//if (cl.equals(acl)) {//the only important change. This allows for the absurd notion of collective ClassLoaders... such as my SwarmClassLoader/SwarmlingClassLoader pair
				/*
				mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 2);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
				Label l3 = new Label();
				mv.visitJumpInsn(IFEQ, l3);
				 */
				return true;
			}
		} while (acl != null);
		return false;
	}
}
