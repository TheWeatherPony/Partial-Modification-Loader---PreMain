package weatherpony.pml.implementorapi;

import java.util.List;
import java.util.concurrent.Callable;

public interface IPMLPluginManagement{
	public List<? extends Callable<String>> getMods();
	public boolean isModActive(Callable<String> mod);
	public void enableMod(Callable<String> mod);
	public void disableMod(Callable<String> mod);
	public void setModEnabledState(Callable<String> mod, boolean state);
	
	public void applicationRecommendedLoadTime(ClassLoader loader);
	public void giveListener(IPMLPluginManagementListener listener);
}
