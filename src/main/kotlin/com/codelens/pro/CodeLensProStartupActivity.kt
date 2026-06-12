package com.codelens.pro

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CodeLensProStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        LOG.info("CodeLens Pro startup activity executed for project: ${project.name}")
        val listener = CodeLensProEditorListener()
        EditorFactory.getInstance().addEditorFactoryListener(listener, project)
        EditorLensManager.getInstance().attachExistingEditors()
    }

    companion object {
        private val LOG = Logger.getInstance(CodeLensProStartupActivity::class.java)
    }
}
