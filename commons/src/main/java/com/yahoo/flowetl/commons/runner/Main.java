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
package com.yahoo.flowetl.commons.runner;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;

import com.yahoo.flowetl.core.util.KlassUtils;
import com.yahoo.flowetl.core.util.NetUtils;

/**
 * This class acts as the main entry point which can create a given runner class
 * and then proxy to it for running whichever actions it decides are necessary.
 * This is nicer than setting up X main functions and then having to handle all
 * of those. Instead you just make sure your class to run is a runner class and
 * it does most of the work for you.
 * 
 * @author Joshua Harlow
 */
public class Main
{
    // used for showing that it is...
    private static final Charset DEF_CHAR_SET = Charset.defaultCharset();

    private Main() {
        // not creatable...
    }

    /**
     * The main method entry point.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(Main.class.getSimpleName() + " [runner fully qualified java class name] arguments ...");
            return;
        }
        System.out.println("+Argument info:");
        StringBuilder argsStr = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            argsStr.append("(" + (i + 1) + ") " + args[i] + " [" + args[i].length() + " chars]");
            if (i + 1 != args.length) {
                argsStr.append(" ");
            }
        }
        System.out.println(argsStr);
        Map<String, Object> sysInfo = getRuntimeInfo();
        System.out.println("+Runtime info:");
        for (Entry<String, Object> e : sysInfo.entrySet()) {
            System.out.println("--- " + e.getKey() + " => " + (e.getValue() == null ? "" : e.getValue()));
        }
        String classToRun = args[0];
        Class<?> testToRun = KlassUtils.getClassForName(classToRun);
        if (KlassUtils.isAbstract(testToRun) || KlassUtils.isInterface(testToRun)) {
            System.out.println("+Runner class name that is not abstract or an interface is required!");
            return;
        }
        if (ClassUtils.isAssignable(testToRun, (Runner.class)) == false) {
            System.out.println("+Runner class name that is a instance/subclass of " + Runner.class.getSimpleName() + " is required!");
            return;
        }
        Class<Runner> rToRun = KlassUtils.getClassForName(classToRun);
        System.out.println("+Running program specified by runner class " + rToRun);
        Runner r = KlassUtils.getInstanceOf(rToRun, new Object[] {});
        String[] nargs = (String[]) ArrayUtils.subarray(args, 1, args.length);
        System.out.println("+Proxying to object " + r + " with arguments [" + StringUtils.join(nargs, ",") + "]");
        r.runProgram(nargs);
    }

    /**
     * Gets some useful runtime info as a map of names -> info.
     */
    private static Map<String, Object> getRuntimeInfo() {
        Map<String, Object> sysInfo = new TreeMap<String, Object>();
        StringBuilder jvminfo = new StringBuilder();
        jvminfo.append("Vendor: ");
        jvminfo.append(SystemUtils.JAVA_VENDOR);
        jvminfo.append(", Version: ");
        jvminfo.append(SystemUtils.JAVA_VERSION + " - " + SystemUtils.JAVA_VM_INFO);
        jvminfo.append(", OS: ");
        jvminfo.append(SystemUtils.OS_NAME + " (" + SystemUtils.OS_VERSION + " : " + SystemUtils.OS_ARCH + ")");
        sysInfo.put(WordUtils.capitalizeFully("jvm"), jvminfo.toString());
        sysInfo.put(WordUtils.capitalizeFully("default charset encoding"), DEF_CHAR_SET.name());
        String netAdd = NetUtils.getLocalAddress();
        if (StringUtils.isEmpty(netAdd)) {
            netAdd = "????";
        }
        String localName = NetUtils.getLocalHostName();
        if (StringUtils.isEmpty(localName)) {
            localName = "????";
        }
        sysInfo.put(WordUtils.capitalizeFully("network"), localName + " at ip address " + netAdd);
        String cPath = SystemUtils.JAVA_CLASS_PATH;
        String linesep = StringEscapeUtils.escapeJava(SystemUtils.LINE_SEPARATOR);
        sysInfo.put(WordUtils.capitalizeFully("classpath"), cPath);
        sysInfo.put(WordUtils.capitalizeFully("jvm home"), SystemUtils.JAVA_HOME);
        sysInfo.put(WordUtils.capitalizeFully("jvm tmpdir"), SystemUtils.JAVA_IO_TMPDIR);
        sysInfo.put(WordUtils.capitalizeFully("jvm libpath"), SystemUtils.JAVA_LIBRARY_PATH);
        sysInfo.put(WordUtils.capitalizeFully("line separator"), linesep);
        sysInfo.put(WordUtils.capitalizeFully("path separator"), StringEscapeUtils.escapeJava(SystemUtils.PATH_SEPARATOR));
        sysInfo.put(WordUtils.capitalizeFully("user timezone"), SystemUtils.USER_TIMEZONE);
        sysInfo.put(WordUtils.capitalizeFully("user home"), SystemUtils.USER_HOME);
        sysInfo.put(WordUtils.capitalizeFully("user language"), SystemUtils.USER_LANGUAGE);
        sysInfo.put(WordUtils.capitalizeFully("user name"), SystemUtils.USER_NAME);
        return sysInfo;
    }
}
