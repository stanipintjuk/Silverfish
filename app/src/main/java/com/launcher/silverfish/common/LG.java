package com.launcher.silverfish.common;

import android.util.Log;

/** Intelligent logger optionally provides calling class & method for the message. <br>
 Standard usage: lg("any string");  [Will generate a TAG of Class.methodName ] <br>
 @author Matt Arnold */
public class LG {

    private static boolean debug = false;
    public static void setDebug(boolean debug) {LG.debug = debug;}
    public static boolean isDebug() {return debug;}

    private static boolean voluminous = false;
    private void setVoluminous(boolean voluminous) {LG.voluminous = voluminous;}
    private boolean isVoluminous() {return voluminous;}

    private static final String regex_splitAtdot = "\\.";
    /**
     * Get the method name for a depth in call stack.
     * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
     * @return method name
     */
    public static String getMethodNme(final int depth)
    {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        int rd = depth+2;
        String[] Cfull = ste[rd].getClassName().split(regex_splitAtdot);
        String Cname = Cfull[Cfull.length-1];       // Class
        String Mname = ste[rd].getMethodName();     // Method
        //lg("gmn2","depth="+ depth+":"+ Cname + "." + Mname);
        return Cname + "." + Mname;
    }

    /** Universal non-voluminous logging */
    public static void lg(String s2) {lg(getMethodNme(2),s2); }
    /** Universal non-voluminous logging */
    public static void lg(String s1, String s2) {
        if (s1 == null || s1.isEmpty()) s1="U.lg() ???";
        if (s2 == null || s2.isEmpty()) s2="!";
        if (debug) Log.d(s1,s2);
    }

    /** Universal voluminous logging */
    public static void lgV(String s2) {lgV(getMethodNme(2),s2); }
    /** Universal voluminous logging */
    public static void lgV(String s1, String s2) { if (voluminous) Log.d(s1,s2); }

    /** Universal forced logging */
    public static void lgF(String s2) {lgF(getMethodNme(2),s2); }
    /** Universal forced logging */
    public static void lgF(String s1, String s2) {Log.d(s1,s2); }

    /** Universal forced exception logging */
    public static void lgX(String s2) {lgX(getMethodNme(2),s2); }
    /** Universal forced exception logging */
    public static void lgX(String s1, String s2) {
        if (s1 == null || s1.isEmpty()) s1="U.lgx() ???";
        Log.e(s1,s2);
    }
}


