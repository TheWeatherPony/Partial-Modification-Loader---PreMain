package weatherpony.pml.implementorapi;

import java.util.List;

/**
 * This is used between PML implementors and PML modders to describe different types of application setups. Classes that implement this inteface are enumerations, and not all details are necessarily covered. A typical setup might have 'CLIENT' and 'SERVER'.</br>
 * PML also uses this for enviornment-based loading of both the application and plugins</br>
 * @author The_WeatherPony
 *
 * @param <self> The enum that implements this interface
 */
public interface IEnviornment<self extends Enum<self> & IEnviornment<self>>{
	public self[] getAllValues();
}
