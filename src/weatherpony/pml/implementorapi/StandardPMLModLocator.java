package weatherpony.pml.implementorapi;

import java.io.File;
/**
 * This is intended as a "standard" implementation of IPMLModLocator, and has support for mods from additional mod systems in a "mods" folder.</br>
 * 
 * @author The_WeatherPony
 */
public class StandardPMLModLocator implements IPMLModLocator {

	@Override
	public void onLoad(IPMLLoadDirector loadEngine){
		File mods = new File("mods");
		if(mods.exists()){
			loadEngine.searchDirectoryForMods(mods, false);//this way you can have a second mod/plugin system, more tailored to traditional addons
		}
		File pmlmods = new File("PMLMods");
		if(!pmlmods.exists())
			pmlmods.mkdir();
		loadEngine.searchDirectoryForMods(pmlmods, true);

	}

}
