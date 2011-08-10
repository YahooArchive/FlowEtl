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
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A class that provides useful operations on enumerations.
 * 
 * @author harlowja
 */
public class EnumUtils
{
    private EnumUtils() {
        // a util class...
    }

    /**
     * Attempts to convert a string that represents an enumeration of a given
     * class into the actual enum object.
     * 
     * @param <T>
     *            the generic type of the enum class to use
     * 
     * @param enumKlass
     *            the enum class that has the enumerations to select from
     * 
     * @param enumStr
     *            the enum string we will attempt to match
     * 
     * @param caseSensitive
     *            whether to compare case sensitive or not
     * 
     * @return the enum object or null if not found/invalid...
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum> T fromString(Class<T> enumKlass, String enumStr, boolean caseSensitive) {
        if (StringUtils.isEmpty(enumStr) || enumKlass == null) {
            // not valid
            return null;
        }
        Object[] types = enumKlass.getEnumConstants();
        if (types == null) {
            // not an enum
            return null;
        }
        Object enumInstance = null;
        for (int i = 0; i < types.length; i++) {
            enumInstance = types[i];
            if (caseSensitive == false) {
                if (StringUtils.equalsIgnoreCase(ObjectUtils.toString(enumInstance), enumStr)) {
                    return (T) (enumInstance);
                }
            }
            else {
                if (StringUtils.equals(ObjectUtils.toString(enumInstance), enumStr)) {
                    return (T) (enumInstance);
                }
            }
        }
        // not found
        throw new IllegalArgumentException("Unknown enumeration [" + enumStr + "] for enum class [" + enumKlass + "]");
    }

}
