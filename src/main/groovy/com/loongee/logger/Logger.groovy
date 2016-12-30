package com.loongee.logger

import org.gradle.api.Project
import org.gradle.api.Plugin

class Logger implements Plugin<Project> {
    void apply(Project project) {
        project.android.registerTransform(new LoggerTransform())
    }
}