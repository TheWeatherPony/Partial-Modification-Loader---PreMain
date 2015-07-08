package weatherpony.pml.launch;

import weatherpony.util.misc.Tristate;

public interface IPMLClassLoaderAncestrySupplier{
	public Tristate isAncestorClassLoader(ClassLoader base, ClassLoader possibleAncestor, boolean extraDebug);
}
