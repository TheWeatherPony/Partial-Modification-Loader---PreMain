package weatherpony.pml.implementorapi;

import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;

/**
 * This is used to convey details from your application to PML about class loading involving enviornment-based code culling.
 * @author The_WeatherPony
 */
public interface IEnviornmentASMSetup<Env extends Enum<Env> & IEnviornment<Env>>{
	/**
	 * This is for PML to know what annotation(s) to look for when parsing code for enviornment-based code culling. You should either have one or two: one to cull the code if not loaded under a specific enviornment, and possibly another to cull code that is loaded in a specific enviornment.</br>
	 * Possible names for these annotations include EnviornmentOnly and EnviornmentOnlyNot.</br>
	 * </br>
	 * The requirements for your annotations consist of the following:</br>
	 * - the annotations you return have exactly one member, which is named "value", which is of your IEnviornment enumeration's type, and which does not have a default value</br>
	 * - the annotations have a Retention of RetentionPolicy.RUNTIME</br>
	 * - the annotations must Target any combination of ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, and ElementType.CONSTRUCTOR; exclusively (meaning it cannot Target parameters, local variables, annotations, and packages)</br>
	 * </br>
	 * Code not annotated with one of your annotations will not be culled.
	 * @return The Class object for your annotation (or annotations, if plural)
	 */
	public Class<? extends Annotation>[] enviornmentCullingAnnotations();
	/**
	 * PML calls this to figure out what code with your annotations should be culled and what should not be. There is no gaurentee that PML will call this every time it finds code with one of your annotations - PML's side of the contract is mearly that the code will be culled appropriately. (In fact, it probably won't call it more than once for each combination.)
	 * @param annotation The annotation that was present.
	 * @param applicationEnviornment The enviornment that you provided as the running enviornment, for simplicity on your end.
	 * @param annotationedEnviornment The enviornment that was specified in the annotation.
	 * @return
	 */
	public boolean shouldKeepCode(Class<? extends Annotation> annotation, Env applicationEnviornment, Env annotationedEnviornment);
}
