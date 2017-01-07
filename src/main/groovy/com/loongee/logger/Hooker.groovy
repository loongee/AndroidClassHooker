package com.loongee.logger

import org.gradle.api.Project
import org.gradle.api.Plugin

class Hooker implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("hooker", HookerConfigExtension.class)
        HookerConfigExtension.defaultConfig = project.hooker
        project.android.registerTransform(new HookerTransform(project.android))
    }
}