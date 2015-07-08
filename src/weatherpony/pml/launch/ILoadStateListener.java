package weatherpony.pml.launch;

public interface ILoadStateListener{
	boolean stateUpdate(LoadUpdate state);
	public static abstract class LoadUpdate{
		@Override
		public String toString(){
			return this.getClass().getName();
		}
	}
	public static abstract class ClassLoaderUpdate extends LoadUpdate{
		public final ClassLoader loader;
		ClassLoaderUpdate(ClassLoader loader){
			this.loader = loader;
		}
		@Override
		public String toString(){
			return this.getClass().getName()+"{"+loader.toString()+"}";
		}
	}
	public static class ClassLoaderConstructionUpdate extends ClassLoaderUpdate{
		public final Class<? extends ClassLoader> finishedConstructor;
		public final String constructorDescription;
		public ClassLoaderConstructionUpdate(ClassLoader loader, Class<? extends ClassLoader> finishedConstructor, String constructorDescription){
			super(loader);
			this.finishedConstructor = finishedConstructor;
			this.constructorDescription = constructorDescription;
		}
		@Override
		public String toString(){
			return this.getClass().getName()+"{"+loader.toString()+','+finishedConstructor+constructorDescription+"}";
		}
	}
	/**
	 * This is deprecated because the generation of the update event is unreliable in certain cases.  
	 */
	@Deprecated
	public static class ThreadChangeContextClassLoaderUpdate extends ClassLoaderUpdate{
		public final Thread thread;
		public ThreadChangeContextClassLoaderUpdate(ClassLoader loader, Thread thread){
			super(loader);
			this.thread = thread;
		}
	}
}
