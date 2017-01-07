package com.loongee.logger

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager

public class HookerTransform extends Transform {
    private String androidDependencyPath

    public HookerTransform(android) {
        androidDependencyPath = android.sdkDirectory.absolutePath + "/platforms/" +
                android.compileSdkVersion + "/android.jar"
    }

    @Override
    String getName() {
        return "Hooker"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Injector injector = new Injector()
        if (HookerConfigExtension.getDefault().verbose) {
            println 'android sdk location: ' + androidDependencyPath
            HookerConfigExtension.getDefault().printRules()
        }
        if (androidDependencyPath != null) {
            injector.addJar(androidDependencyPath)
        }
        def pathList = []
        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY)

                if (HookerConfigExtension.getDefault().verbose) {
                    println(directoryInput.file.absolutePath + '---->' + dest.absolutePath)
                }
                FileUtils.copyDirectory(directoryInput.file, dest)
                pathList.add(dest)
                injector.addDir(dest.absolutePath)
            }

            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                def dest = transformInvocation.getOutputProvider().getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
                if (HookerConfigExtension.getDefault().verbose) {
                    println(jarInput.file.absolutePath + '---->' + dest)
                }
                pathList.add(dest)
                injector.addJar(dest.absolutePath)
            }
        }
        pathList.each { File dest ->
            if (dest.isDirectory()) {
                injector.injectDir(dest.absolutePath)
            } else if (dest.name.endsWith('.jar')) {
                injector.injectJar(dest)
            }
        }
    }
}