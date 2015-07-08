package weatherpony.pml.implementorapi;

import java.io.File;
import java.net.URL;

/**
 * This is your connection to PML's mod loading system - PML implements this and gives a copy to your IPMLModLocator
 * @author The_WeatherPony
 */
public interface IPMLLoadDirector{
	/**
	 * Use this to define your mod folder(s)
	 * @param folder - where PML should look for mods
	 * @param everythingIsAPMLMod - if true, every sub file and sub directory are loaded and parsed as PML mods. if false, only subfiles with the 'pmlm' extension are loaded
	 */
	public void searchDirectoryForMods(File folder, boolean everythingIsAPMLMod);
	/**
	 * Call this to load a specific file that's not in a registered mod folder
	 * @param modlocation - the file or directory PML should look through as a mod
	 */
	public void loadMod(File modlocation);
	/**
	 * This is so your application can choose to load an internal PML mod - please note that PML mods cannot alter code loaded before the hook injector is ready. If you want your hook to have parts that can be hooked into, prepare your code accordingly
	 * @param headerName 
	 */
	public void searchForLocalMod(String headerName);
}
