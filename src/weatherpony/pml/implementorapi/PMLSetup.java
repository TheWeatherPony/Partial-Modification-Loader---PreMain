package weatherpony.pml.implementorapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;

public abstract class PMLSetup<self extends PMLSetup<self,EnvType>, EnvType extends Enum<EnvType> & IEnviornment<EnvType>>{
	protected PMLSetup(IEnviornmentASMSetup<EnvType> enviornmentloader){
		if(instance != null)
			throw new IllegalStateException();
		instance = this;
		this.enviornmentloader = enviornmentloader;
	}
	protected IEnviornmentASMSetup<EnvType> enviornmentloader;
	public IEnviornmentASMSetup<EnvType> getEnviornmentLoader(){
		return this.enviornmentloader;
	}
	private static PMLSetup<?,?> instance;
	public static <T extends PMLSetup<T,ET>, ET extends Enum<ET> & IEnviornment<ET>> T getSetup(){
		return (T)instance;
	}
	
	public abstract IPMLModLocator getModLocator();
	public abstract IProgramInformation<EnvType> getPMLApplicationAPI();
	public String getApplicationName(){
		return this.getPMLApplicationAPI().programName();
	}
	public IEnviornment<EnvType> getApplicationEnviornment(){
		return this.getPMLApplicationAPI().getApplicationEnviornment();
	}
	public abstract void givePluginManager(IPMLPluginManagement manager);
	
	protected File saveExternalDependancyLocation(ExternalLibraryDependancy lib){
		return new File(new File(new File("PML","PML_downloadedLibs"),lib.name),lib.version+".jar");
	}
	public void loadExternalDependancy(ExternalLibraryDependancy lib, ClassLoader loader) throws DependancyException{
		File libFile = this.saveExternalDependancyLocation(lib);
		if(!libFile.exists()){
			//the library is not already downloaded. need to fetch it, if we can
			URL download = null;
			try {
				download = new URL(lib.downloadURL);
			} catch (MalformedURLException e) {
				System.out.println("PML: a mod asked to download an external dependancy, but supplied a bad download URL ("+lib.name+'-'+lib.version+'['+lib.downloadURL+"])");
				throw new DependancyException(lib);
			}
			try {
				FileUtils.copyURLToFile(download, libFile, 30000, 5000);//wait no more than 30 seconds while trying to connect, and 5 seconds if the connection is broken
			} catch (IOException e) {
				System.out.println("PML: a mod is tying to download an external dependancy, but can't. If you aren't connected to the internet, then please do. If you are connected, please try again later. If problems persist, please talk to the mod maker.");
				throw new DependancyException(lib);
			}
		}
		//at this point, the libarary was either successfully downloaded, or it was already available.
		try {
			Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			m.setAccessible(true);
			m.invoke(loader, libFile.toURI().toURL());
		}catch(MalformedURLException e){
			e.printStackTrace();
			throw new DependancyException(lib);
		}catch(Throwable e){
			e.printStackTrace();
			throw new DependancyException(lib);
		}
	}
	public static class DependancyException extends Exception{
		final ExternalLibraryDependancy lib;
		public DependancyException(ExternalLibraryDependancy lib){
			this.lib = lib;
		}
	}
	public void notifyOfEndCoreLoad(){
	}
}
