package com.codelens.pro

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CodeLensProStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        EditorLensManager.getInstance().initialize()
    }
}
