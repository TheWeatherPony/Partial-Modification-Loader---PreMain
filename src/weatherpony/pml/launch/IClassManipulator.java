package weatherpony.pml.launch;

public interface IClassManipulator{
	
	public byte[] transformClass(ClassLoader loader, String name, byte[] data);
}
