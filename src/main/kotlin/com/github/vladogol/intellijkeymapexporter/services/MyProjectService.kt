package com.github.vladogol.intellijkeymapexporter.services

import com.intellij.openapi.project.Project
import com.github.vladogol.intellijkeymapexporter.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
