package com.loongee.logger

import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtBehavior
import javassist.CtClass
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

public class Injector {
    private ClassPool pool = ClassPool.getDefault()

    private SubExprEditor editor = new SubExprEditor();

    private class SubExprEditor extends ExprEditor {
        @Override
        void edit(MethodCall m) throws CannotCompileException {
            if (HookerConfigExtension.getDefault().verbose) {
                println('    ' + getFullSignature(m))
            }
            String signature = m.getClassName();
            def editorRule = HookerConfigExtension.getDefault().editorRule
            if (editorRule.containsKey(signature)) {
                String replaceTo = "\$_=" + editorRule.get(signature) + "." + m.getMethodName() + "(\$\$);"
                m.replace(replaceTo)
                if (HookerConfigExtension.getDefault().verbose) {
                    println('        replace to: ' + replaceTo)
                }
            }
        }

        private static String getFullSignature(MethodCall m) {
            return m.getSignature() + ' ' + m.getClassName() + ' ' + m.getMethodName();
        }
    }

    public void addDir(String path) {
        pool.appendClassPath(path)
    }

    public void addJar(String jarPath) {
        pool.insertClassPath(jarPath)
    }

    public void injectDir(String path) {
        File dir = new File(path)
        if (!dir.isDirectory()) {
            return
        }

        dir.eachFileRecurse { File file ->
            String filePath = file.absolutePath
            if (file.isDirectory()) {
                return
            }
            if (filePath.endsWith(".class")
                    && !filePath.contains('R$')
                    && !filePath.contains('R.class')
                    && !filePath.contains("BuildConfig.class")) {
                int end = filePath.length() - 6; // '.class' == 6
                String className = filePath.substring(path.length() + 1, end).replace('\\', '.').replace('/', '.')
                if (inWhiteList(className)) {
                    if (HookerConfigExtension.getDefault().verbose) {
                        println '[white list skip] ' + className
                    }
                    return
                }
                CtClass c = pool.getCtClass(className)

                if (c.isFrozen()) {
                    c.defrost()
                }

                c.getDeclaredBehaviors().each { CtBehavior behavior ->
                    println behavior.getLongName()
                    behavior.instrument(editor)
                }

                c.writeFile()
                println(filePath + '---> [modified]')
            }
        }
    }

    public void injectJar(File jarFile) {
        ZipFile zipFile = new ZipFile(jarFile.absolutePath)
        zipFile.entries().each { ZipEntry entry ->
            if (entry.name.endsWith(".class")) {
                if (HookerConfigExtension.getDefault().verbose) {
                    println('jar entry: ' + entry.name)
                }
                String className = entry.name.substring(0, entry.name.length() - 6).replace('/', '.')
                if (inWhiteList(className)) {
                    if (HookerConfigExtension.getDefault().verbose) {
                        println('white list ignore: ' + className)
                    }
                    return
                }
                CtClass c = pool.getCtClass(className)
                if (c.isFrozen()) {
                    c.defrost()
                }

                c.getDeclaredBehaviors().each { CtBehavior behavior ->
                    println behavior.getLongName()
                    behavior.instrument(editor)
                }

                c.writeFile()
            }
        }
    }

    private static boolean inWhiteList(String className) {
        return HookerConfigExtension.getDefault().whiteList.any { String whiteItem ->
            return className.matches(whiteItem)
        }
    }

    public Injector() {
    }
}