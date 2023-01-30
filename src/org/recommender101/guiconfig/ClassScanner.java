package org.recommender101.guiconfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101HideFromGui;
import org.recommender101.gui.annotations.R101Setting;

@SuppressWarnings({"rawtypes","unchecked"})
public class ClassScanner {

	/**
	 * This is being used to store the Class objects along with the name of
	 * their corresponding superclass
	 */

	private HashMap<String, ArrayList<Class>> subclasses;

	/**
	 * Caches all Class objects for future calls of findClass
	 */
	public static Class[] findClassCache = null;

	/**
	 * When performing a scan, these are the superclasses that are being
	 * searched for in the class hierarchy.
	 */
	private final String[] superClassNames = { "org.recommender101.recommender.AbstractRecommender",
			"org.recommender101.eval.interfaces.Evaluator", "org.recommender101.data.DataSplitter",
			"org.recommender101.data.DefaultDataLoader"};

	public ClassScanner() {
		subclasses = new HashMap<>();

		// Perform initial scan
		performScan();
	}

	/**
	 * Searches for a specific class
	 * 
	 * @param fullName
	 *            The fully qualified name of the class to search for
	 * @return null if no matching class has been found, the found Class object
	 *         otherwise
	 */
	public static Class findClass(String fullName) {
		// First, get all Classes inside the package
		Class[] c = null;
		if (findClassCache == null) {
			try {
				findClassCache = getClasses("org.recommender101");
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

		c = findClassCache;

		for (Class curr : c) {
			if (curr.getName().equals(fullName)) {
				return curr;
			}
		}

		return null;
	}

	/**
	 * Performas a scan for class files
	 */
	private void performScan() {

		// First, get all Classes inside the package
		Class[] c = null;
		try {
			c = getClasses("org.recommender101");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		for (Class curr : c) {

			for (String sClass : superClassNames) {
				if (isSubClassOf(curr, sClass)) {
					ArrayList<Class> classes;
					classes = subclasses.get(sClass);

					if (classes == null)
						classes = new ArrayList<Class>();

					classes.add(curr);

					subclasses.put(sClass, classes);
				}
			}
	
		}
	}

	/**
	 * Recursive method that checks whether a class is a subclass of a specific
	 * class
	 * 
	 * @param c
	 *            The class that should be checked
	 * @param superClassSearchName
	 *            Fully qualified name of the class to search for
	 * @return
	 */
	private boolean isSubClassOf(Class c, String superClassSearchName) {
		if (c.getSuperclass() == null || c.getSuperclass().getName().equals("java.lang.Object"))
			return false;

		if (c.getSuperclass().getName().equals(superClassSearchName))
			return true;

		return isSubClassOf(c.getSuperclass(), superClassSearchName);
	}

	public ArrayList<InternalR101Class> getAnnotatedRecommenders() {
		return getClassObjects("org.recommender101.recommender.AbstractRecommender");			}

	

	public ArrayList<InternalR101Class> getAnnotatedMetrics() {
		return getClassObjects("org.recommender101.eval.interfaces.Evaluator");
	}	
	
	public ArrayList<InternalR101Class> getDataSplitter() {
		return getClassObjects("org.recommender101.data.DataSplitter");
	}
	
	public ArrayList<InternalR101Class> getDataLoader() {
		ArrayList<InternalR101Class> arr = getClassObjects("org.recommender101.data.DefaultDataLoader");
		// Since the "interface" class is actually a useable data loader, we have to add it manually
		arr.add(new InternalR101Class("org.recommender101.data.DefaultDataLoader"));
		sortClassesAlphabetically(arr);
		return arr;
	}
	
	/**
	 * A helper function that is being used internally to gather the InternalR101Class objects into an ArrayList
	 * @param superClass The full name of the super class
	 * @return The ArrayList containing the requested InternalR101Class objects
	 */
	private ArrayList<InternalR101Class> getClassObjects(String superClass)
	{
		ArrayList<Class> arr = getClassObjectsBySuperclassName(superClass);
		ArrayList<InternalR101Class> ret = new ArrayList<InternalR101Class>();
		for (Class c : arr) {
			
			// Skip if this should be hidden from the GUI
			if (c.isAnnotationPresent(R101HideFromGui.class))
			{
				continue;
			}

			R101Class annotation = getClassAnnotation(c);
			
			if (annotation != null) {
				ret.add(new InternalR101Class(c, annotation.name(), annotation.description(), getClassSettings(c)));
			} else {
				// Class has not been annotated, add it anyway
				ret.add(new InternalR101Class(c, c.getSimpleName(), "", new ArrayList<InternalR101Setting>()));
			}
		}

		// Sort alphabetically
		sortClassesAlphabetically(ret);

		return ret;
	}
		
	
	public static ArrayList<InternalR101Class> sortClassesAlphabetically(ArrayList<InternalR101Class> list) {
		if (list.size() < 2)
		{
			return list;
		}
		
		Collections.sort(list, new Comparator<InternalR101Class>() {

			@Override
			public int compare(InternalR101Class o1, InternalR101Class o2) {
				return (o1.getDisplayName().compareTo(o2.getDisplayName()));
			}

		});

		return list;
	}

	

	private ArrayList<Class> getClassObjectsBySuperclassName(String nameOfSuperclass) {
		return subclasses.get(nameOfSuperclass);
	}

	public static R101Class getClassAnnotation(Class c) {
			
		if (c.isAnnotationPresent(R101Class.class))
		{
			return (R101Class) c.getAnnotation(R101Class.class);
		}
		
		return null;
	}

	public static ArrayList<InternalR101Setting> getClassSettings(Class c) {
		ArrayList<InternalR101Setting> list = new ArrayList<InternalR101Setting>();

		for (Method method : c.getMethods()) {
			//Annotation[] annotations = method.getAnnotations();

			if (method.isAnnotationPresent(R101Setting.class)) {
				list.add(new InternalR101Setting(method.getName(),method.getAnnotation(R101Setting.class)));
				
			} else {
				// Since annotations are discarded when child classes override
				// superclass methods, we probably should search in the
				// superclass chain, too

				Class d = c.getSuperclass();
				R101Setting setting = getSettingsAnnotationRec(d, method);
				
				if (setting != null)
				{
					list.add(new InternalR101Setting(method.getName(),setting));
				}
			}
		}

		return list;
	}
	
	
	
	public static R101Setting getSettingsAnnotationRec(Class c, Method method)
	{
		if (c.getName().equals("java.lang.Object"))
		{
			// Not found
			return null;
		}
		
		Method m = null;
		try {
			m = c.getMethod(method.getName(), method.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException e) {
			return getSettingsAnnotationRec(c.getSuperclass(), method);
		}		
		
		if (m != null)
		{
			if (m.isAnnotationPresent(R101Setting.class))
			{
				// The desired setting has been found
				return m.getAnnotation(R101Setting.class);
			}
		}		
		
		return getSettingsAnnotationRec(c.getSuperclass(), method);
	}
	

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */

	private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */

	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

}
