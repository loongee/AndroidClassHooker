package com.loongee.logger

import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtBehavior
import javassist.CtClass
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

public class Injector {
    private static ClassPool pool = ClassPool.getDefault()

    private static Map<String, String> editorRule = new TreeMap<>()

    private static class SubExprEditor extends ExprEditor {
        @Override
        void edit(MethodCall m) throws CannotCompileException {
            String signature = m.getSignature()
            if (editorRule.containsKey(signature)) {
                m.replace(editorRule.get(signature))
            }
            println('    ' + m.getSignature() + ' ' + m.getClassName() + ' ' + m.getMethodName())
        }
    }

    public static void addMethodRedirect(String oldSignature, String newExpr) {
        editorRule[oldSignature] = newExpr
    }

    public static void printRules() {
        println '===========Rules==========='
        editorRule.entrySet().each { Map.Entry<String, String> entry ->
            println entry.key + '->' + entry.value
        }
        println '==========End Rules========'
    }

    public static void injectDir(String path) {
        pool.appendClassPath(path)
        File dir = new File(path)
        if (!dir.isDirectory()) {
            return
        }

        SubExprEditor editor = new SubExprEditor()

        dir.eachFileRecurse { File file ->
            String filePath = file.absolutePath
            if (filePath.endsWith(".class")
                    && !filePath.contains('R$')
                    && !filePath.contains('R.class')
                    && !filePath.contains("BuildConfig.class")) {
                int end = filePath.length() - 6; // '.class' == 6
                String className = filePath.substring(path.length() + 1, end).replace('\\', '.').replace('/', '.')
                CtClass c = pool.getCtClass(className)

                if (c.isFrozen()) {
                    c.defrost()
                }

                c.getDeclaredBehaviors().each {CtBehavior behavior ->
                    println behavior.getLongName()
                    behavior.instrument(editor)
                }
            }
        }
    }

    public static void injectJar(String path) {
        pool.appendClassPath(path)
    }
}