package weatherpony.pml.implementorapi;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

public interface IPMLPluginManagementListener{
	public void modDisabledFromSelfError(Callable<String> mod);
	public boolean mod_PMLCriticalError(Callable<String> mod, Error criticalError);
	public boolean acceptableFactories(List<String> factories, URL url);
}
