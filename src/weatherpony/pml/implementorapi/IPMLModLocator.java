package weatherpony.pml.implementorapi;

/**
 * Since every program has it's own setup/structure, this allows your application to tell PML where to load PML mods from.
 * @author The_WeatherPony
 */
public interface IPMLModLocator{
	/**
	 * PML calls this method when it's ready to load it's addons. This will only be called once, so make sure you tell PML about everywhere you want it to look. PML will automatically look for a "pml.info" file on the classpath, but this is intended for mod development testing. If you want to include a PML mod in your program directly, use a different header name and register it with the searchForLocalMod method. 
	 * @param loadEngine PML's mod finder
	 */
	public void onLoad(IPMLLoadDirector loadEngine);
}
