/*******************************************************
 *                                                     *
 * Copyright (C) 2011 Yahoo! Inc. All Rights Reserved. *
 *                                                     *
 *                Licensed under the New               *
 *                 BSD License. See the                *
 *              accompanying LICENSE file              *
 *              for the specific language              *
 *              governing permissions and              *
 *                limitations under the                *
 *                       License.                      *
 *******************************************************/
package com.yahoo.flowetl.core.util;

/*******************************************************
 *                                                     *
 *                                                     *
 *******************************************************/
import java.lang.reflect.Modifier;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;

/**
 * This class provides a set of java "class" utility functions to be used for
 * reflection or other class lookup mechanisms used by a framework to
 * dynamically create classes from strings.
 * 
 * @author harlowja
 */
public class KlassUtils
{
    private KlassUtils() {
        // a util class
    }

    /**
     * Checks if a class is an interface.
     * 
     * @param klass
     *            the class to check
     * 
     * @return true, if it is an interface
     */
    public static boolean isInterface(Class<?> klass) {
        int modifiers = klass.getModifiers();
        if (Modifier.isInterface(modifiers)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a class is abstract.
     * 
     * @param klass
     *            the class to check
     * @return true, if it is an abstract class
     */
    public static boolean isAbstract(Class<?> klass) {
        int modifiers = klass.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the class for a given string name
     * 
     * @param <T>
     *            the generic type to that is this class.
     * 
     * @param fullyQualifiedName
     *            the fully qualified java name of that class
     * 
     * @return the class for that fully qualified name
     * 
     * @throws ClassNotFoundException
     *             the class not found exception if the name can not be found
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassForName(String fullyQualifiedName) throws ClassNotFoundException {
        Class<?> klass = ClassUtils.getClass(fullyQualifiedName);
        return (Class<T>) klass;
    }

    /**
     * Gets the instance of a class by calling the constructor with the given
     * object arguments.
     * 
     * @param <T>
     *            the generic type to construct
     * @param klass
     *            the class to attempt to construct
     * 
     * @param constructorArgs
     *            the constructor args to provide to that class
     * 
     * @return an instance of that object
     * 
     * @throws ConstructException
     *             a exception if that class can not be constructed
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstanceOf(Class<T> klass, Object[] constructorArgs) {
        try {
            return (T) ConstructorUtils.invokeConstructor(klass, constructorArgs);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to construct class " + klass, e);
        }
    }

}
