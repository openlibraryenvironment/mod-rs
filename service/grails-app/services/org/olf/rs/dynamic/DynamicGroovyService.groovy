package org.olf.rs.dynamic;

/**
 * Exposes the necessary methods required to dynamically run groovy code
 */
public class DynamicGroovyService {

    /** The default method name we will try and execute against the compiled class */
    static public final String DEFAULT_METHOD_NAME = "perform";

    static private final String TEMPLATE_KEY_PREFIX        = '<';
    static private final String TEMPLATE_KEY_POSTFIX       = '>';
    static private final String TEMPLATE_KEY_CLASS_NAME    = TEMPLATE_KEY_PREFIX + 'className' + TEMPLATE_KEY_POSTFIX;
    static private final String TEMPLATE_KEY_GROOVY_SOURCE = TEMPLATE_KEY_PREFIX + 'groovySource' + TEMPLATE_KEY_POSTFIX;

    static private final String CLASS_TEMPLATE = "class Dynamic" + TEMPLATE_KEY_CLASS_NAME + " {\n" +
                                                 "    public Object " + DEFAULT_METHOD_NAME + "(Map arguments) {\n" +
                                                 "        " + TEMPLATE_KEY_GROOVY_SOURCE + "\n" +
                                                 "    }\n" +
                                                 "}";

    /** the groovy class loader we will use for parseing the source code */
    private GroovyClassLoader groovyClassLoader = null;

    /** The instance cache that we use for invoking the methods */
    private Map classInstanceCache = [ : ];

    /**
     * Parses a groovy script, no cacheing is involved, so this is more for a simple snippet of groovy
     * @param groovySource The groovy scripts that needs parsing
     * @return A grrovy.lang.Script object of the parsed script if successful otherwise null
     */
    private Script parseScript(String groovySource) {

        // Lookup to see if it already exists in the cache
        Script groovyScript = null;

        // Did we find it in the cache
        if ((groovyScript == null) && groovySource) {
            try {
                // We did not, so attempt to parse it
                GroovyShell groovyShell = new GroovyShell();
                groovyScript = groovyShell.parse(groovySource);
            } catch (Exception e) {
                log.error("Exception parsing groovy source: " + groovySource, e);
            }
        }

        // return the groovy script
        return(groovyScript);
    }

    /**
     * Executes the supplied groovy source with the supplied arguments passed in as variables
     * Note: No cacheing of the compiled groovy source occurs
     * @param groovySource The source that is to be executed
     * @param arguments A map of variables that the source can make use of
     * @return The object returned from the source or null if it failed to execute the source correctly
     */
    public Object executeScript(String groovySource, Map arguments) {

        // Defualt to null for the result
        Object result = null;

        // Get hold of the script
        Script groovyScript = parseScript(groovySource);

        // Did we manag to find the script
        if (groovyScript != null) {
            try {
               // The source was successfully parse, so lets try and run it
                Binding scriptBinding = new Binding();
                groovyScript.setBinding(scriptBinding);
                if (arguments != null) {
                    arguments.each{ argKey, argValue -> scriptBinding.setProperty(argKey, argValue); };
                }

                // Now run the script
                result = groovyScript.run();
            } catch (Exception e) {
                log.error("Exception executing groovy script with key: " + key + " and source " + groovySource + " and variables " + arguments.toString(), e);
            }
        }
        return(result);
    }

    /**
     * Returns the class loader used by this service, instantiating a new instance the first time through
     * @return The GroovyClassLoader
     */
    private GroovyClassLoader getClassLoader() {
        // Has it been initialised yet
        if (groovyClassLoader == null) {
            groovyClassLoader = new GroovyClassLoader();
        }
        return(groovyClassLoader);
    }

    /**
     * Obtains a new instance of the groovy class provided in groovySource
     * @param key The key used for caching the instance
     * @param classSource The source that defines the class that we want an insance for
     * @return An instance of the class defined in groovySource otherwise null if an error occurred generating an instance
     */
    private Object getClassInstance(String key, String classSource) {
        Object instance = null;

        // We must have a key and class source
        if (key && classSource) {
            // See if we have it in our cache
            instance = classInstanceCache[key];

            // If we did not find it in our cache then attempt to create a new instance
            if (instance == null) {
                try {
                    // Now attempt to parse the code source
                    Class parsedClass = getClassLoader().parseClass(classSource);

                    // Now create a new instance of this class
                    instance = parsedClass.newInstance();

                    // Now add it to our cache
                    classInstanceCache[key] = instance;
                } catch (Exception e)
                {
                    log.error("Exception thrown parsing groovy source\n" + classSource, e);
                }
            }
        }

        // Return the instance to the caller
        return(instance);
    }

    /**
     * Executes the method on the supplied class, passing in arguments as a parameter
     * @param key The key used to store an instance of the source in the cache
     * @param classSource The groovy source that defines the class
     * @param arguments The arguments to be passed to the method, if null then it is assumed the method does not take any arguments
     * @param methodName The method to be executed, if null, blank or whitespace it defaults to "perform"
     * @return The result of calling the method or null if we failed to parse the source or execute the method
     */
    public Object executeClass(String key, String classSource, Map arguments, String methodName = DEFAULT_METHOD_NAME) {
        Object result = null;

        // Ensure we have a method name
        String actualMethodName = methodName.trim();
        if (actualMethodName.length() == 0) {
            actualMethodName = DEFAULT_METHOD_NAME;
        }

        // Get hold of an instance of the object
        Object instance = getClassInstance(key, classSource);

        // Did we get hold of an instance
        if (instance != null) {
            try {
                // We did, so attempt to execute the supplied method
                // If arguments are null then we do not pass it through as a parameter
                if (arguments == null) {
                    result = instance."$actualMethodName"();
                } else {
                    result = instance."$actualMethodName"(arguments);
                }
            } catch (Exception e) {
                log.error("Exception thrown while trying to invoke method: " + actualMethodName + " for class source with key " + key + " and arguments: " + arguments?.toString());
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Executes the supplied groovy source wrapping it in a class, as opposed to the non caching version
     * the arguments will be supplied in the variable arguments
     * @param key The key used to store an instance of the source in the cache
     * @param groovySource The source that is to be executed
     * @param arguments A map of variables that the source can make use of
     * @return The object returned from the source or null if it failed to execute the source correctly
     */
    public Object executeScript(String key, String groovySource, Map arguments) {
        // Define the template replacements for the class
        Map templateReplacements = [
            "$TEMPLATE_KEY_CLASS_NAME" : UUID.randomUUID().toString().replace("-", ""),
            "$TEMPLATE_KEY_GROOVY_SOURCE" : groovySource
        ];

        // Now make the replacements
        String classSource = CLASS_TEMPLATE;
        templateReplacements.each { replacement ->
            classSource = classSource.replace(replacement.key, replacement.value);
        }

        // Now we have created our temporary class, lets execute the source
        return(executeClass(key, classSource, arguments));
    }
}
