package weatherpony.pml.implementorapi;
/**
 * This provides basic communication fundamentals between PML implementors and PML modders, involving details for application and plugin loading.</br>
 * It is highly recommended that you share either your implementation of this, or share an abstract class that is a superclass of your full implementation, as this is only intended as a basic API from PML about what your program is, mostly so that plugins don't get loaded for the wrong application.</br>
 * @author The_WeatherPony
 */
public interface IProgramInformation<EnvType extends Enum<EnvType> & IEnviornment<EnvType>>{
	/**
	 * Returns the name of the program, so PML plugins can determine if they should be loading with this application.
	 * @return The name of the program/application
	 */
	public String programName();
	/**
	 * In case the implementing application wishes to provide more information about it's execution, this provides a pre-established API versioning system. This is so plugins designed for a previous version know that they're outdated.  
	 * @return an integer representing the application's additional PML plugin API version
	 */
	public int PMLProgramAPIVersion();
	/**
	 * Helpful for determining if a given previous API edition's use is supported, or if the changes between the API versions break compatibility.
	 * @param version the API edition that is being used by the requesting plugin 
	 * @return whether or not the supplied version is directly compatible or not (if new things were added that aren't present in the supplied version, nor are required for loading)
	 */
	public boolean isPMLProgramAPIVersionDirectlyCompatible(int version);
	
	public IEnviornment<EnvType> getApplicationEnviornment();
}
