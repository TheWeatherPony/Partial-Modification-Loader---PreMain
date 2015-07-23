package weatherpony.pml.premain;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
	private static final boolean testingcode = true;
	public Class defineClass2(String name, byte[] dat, int off, int len, ProtectionDomain domain){
		{
			boolean testing = testingcode;
			if(testing){
				if(dat == null)
					throw new RuntimeException("PML: the class' bytecode was null");
				if(name == null)
					throw new RuntimeException("PML: the class' name was null");
			}
		}
		try{
			if(name != null && name.startsWith("sun.reflect"))
				;
			else{
				Class pmlLoadFocuser = this.loadClass(PMLLoadFocuser.LoadFocuserClassName);
				Method edit = pmlLoadFocuser.getDeclaredMethod(PMLLoadFocuser.pmlClassTransformationMethodName, ClassLoader.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
				edit.setAccessible(true);
				Object ret = edit.invoke(null, this, name, dat, off, len, domain);
				if(ret == null && dat != null){
					throw new RuntimeException("PML: PML has royally messed up");
				}
				dat = (byte[])ret;
				off = 0;
				len = dat.length;
			}
		}catch(ClassNotFoundException e){
		}catch(Throwable e2){
			if(e2 instanceof RuntimeException)
				throw (RuntimeException)e2;
			throw new RuntimeException(e2);
		}
		
		return Class.class;
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
	public Class findClass(String name) throws ClassNotFoundException{
		{
			Class found = this.findLoadedClass(name);
			if(found != null)
				return found;
			//sun.reflect.MethodAccessorImpl
			if(!name.startsWith("sun.reflect.")){//just let all reflection implementations pass through - otherwise there could be some circular logic (StackOverflowError)
				ClassLoader above = null;
				{
					{
						Class loader = this.getClass();
						while(!loader.equals(Object.class) && above == null){
							Field parent = null;
							try{
								parent = loader.getDeclaredField("parent");
							}catch(Throwable e){
							}
							if(parent != null){
								parent.setAccessible(true);
								try{
									above = (ClassLoader) parent.get(this);
								}catch(Throwable e){
									throw new RuntimeException();
								}
							}
							loader = loader.getSuperclass();
						}
					}
						
					if(above == null)
						above = ClassLoader.getSystemClassLoader();
					
					ClassLoader check = above;
					while(check != null){
						if(this.equals(check)){
							above = null;
							break;
						}
						Class loader = check.getClass();
						ClassLoader temp = null;
						while(!loader.equals(Object.class) && temp == null){
							Field parent = null;
							try{
								parent = loader.getDeclaredField("parent");
							}catch(Throwable e){
							}
							if(parent != null){
								parent.setAccessible(true);
								try{
									temp = (ClassLoader) parent.get(check);
									break;
								}catch(Throwable e){
									throw new RuntimeException();
								}
							}
							loader = loader.getSuperclass();
						}
						check = temp;
					}
					
					//"above" is actually below - because we grabbed the system class loader when this was it's ancestor - we need to check the bootstrap class loader instead
				}
				
				System.err.printf("PML-ClassLoader(generic) edit (findClass) :");
				System.err.println(name);
				
				if(name.equals(PMLLoadFocuser.LoadFocuserClassName)){//public static final - it'll become a literal constant at compile time
					if(above != null){
						try{
							Method findClass = ClassLoader.class.getDeclaredMethod("findClass", String.class);
							findClass.setAccessible(true);
							return (Class) findClass.invoke(above, name);
						}catch(InvocationTargetException e){
							/*Throwable cause = e.getCause();
							if(cause instanceof ClassNotFoundException){
								throw (ClassNotFoundException)cause;
							}
							if(cause instanceof RuntimeException)
								throw (RuntimeException)cause;
							throw new RuntimeException(cause);*/
							throw new RuntimeException(name, e);
						}catch(Exception e){
							throw new RuntimeException(name, e);
						}
					}else
						throw new ClassNotFoundException(name);
				}
				try{
					Class pmlloadfocuser = this.loadClass(PMLLoadFocuser.LoadFocuserClassName);
					Method searchOverride = pmlloadfocuser.getMethod(PMLLoadFocuser.pmlClassSearchOverrideMethodName, ClassLoader.class, String.class);
					found = (Class)searchOverride.invoke(null, this, name);
					if(found != null)
						return found;
				}catch(ClassNotFoundException e){//couldn't find PMLLoadFocuser - too early...?
				}catch (InvocationTargetException e){
					Throwable e2 = e.getCause();
					if(e2 instanceof RuntimeException)
						throw (RuntimeException)e2;
					throw new RuntimeException(e2);
				}catch(RuntimeException e){
					throw e;
				}catch(Exception e){
					throw new RuntimeException(e);
				}
			}
		}
		
		return Class.class;
	}
	
	@Override
	public Class loadClass(String name, boolean resolve) throws ClassNotFoundException{
		{
			Class found = this.findLoadedClass(name);
			if(found != null)
				return found;
			//sun.reflect.MethodAccessorImpl
			if(!name.startsWith("sun.reflect.")){//just let all reflection implementations pass through - otherwise there could be some circular logic (StackOverflowError)
				ClassLoader above = null;
				{
					{
						Class loader = this.getClass();
						while(!loader.equals(Object.class) && above == null){
							Field parent = null;
							try{
								parent = loader.getDeclaredField("parent");
							}catch(Throwable e){
							}
							if(parent != null){
								parent.setAccessible(true);
								try{
									above = (ClassLoader) parent.get(this);
								}catch(Throwable e){
									throw new RuntimeException();
								}
							}
							loader = loader.getSuperclass();
						}
					}
						
					if(above == null)
						above = ClassLoader.getSystemClassLoader();
					
					ClassLoader check = above;
					while(check != null){
						if(this.equals(check)){
							above = null;
							break;
						}
						Class loader = check.getClass();
						ClassLoader temp = null;
						while(!loader.equals(Object.class) && temp == null){
							Field parent = null;
							try{
								parent = loader.getDeclaredField("parent");
							}catch(Throwable e){
							}
							if(parent != null){
								parent.setAccessible(true);
								try{
									temp = (ClassLoader) parent.get(check);
									break;
								}catch(Throwable e){
									throw new RuntimeException();
								}
							}
							loader = loader.getSuperclass();
						}
						check = temp;
					}
					
					//"above" is actually below - because we grabbed the system class loader when this was it's ancestor - we need to check the bootstrap class loader instead
				}
				
				System.err.printf("PML-ClassLoader(generic) edit (loadClass) :");
				System.err.println(name);
				
				if(name.equals(PMLLoadFocuser.LoadFocuserClassName)){//public static final - it'll become a literal constant at compile time
					if(above != null){
						return above.loadClass(name);
					}else
						throw new ClassNotFoundException(name);
				}
				
				try{
					Class pmlloadfocuser = this.loadClass(PMLLoadFocuser.LoadFocuserClassName);
					Method searchOverride = pmlloadfocuser.getMethod(PMLLoadFocuser.pmlClassSearchOverrideMethodName, ClassLoader.class, String.class);
					found = (Class)searchOverride.invoke(null, this, name);
					if(found != null)
						return found;
				}catch(ClassNotFoundException e){//couldn't find PMLLoadFocuser - too early...?
				}catch (InvocationTargetException e){
					/*Throwable e1 = e.getCause();
					if(e1 instanceof RuntimeException)
						throw (RuntimeException)e1;
					throw new RuntimeException(e1);*/
					throw new RuntimeException(name, e);
				}catch(RuntimeException e){
					//throw e;
					throw new RuntimeException(name, e);
				}catch(Exception e){
					throw new RuntimeException(name, e);
				}
			}
		}
		
		return Class.class;
	}
	@SuppressWarnings("resource")//not yet used for anything
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
			//if (cl.equals(acl)) {//the only important change. This allows for the absurd notion of collective ClassLoaders... such as my SwarmClassLoader/SwarmlingClassLoader pair (Work in Progress)
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
