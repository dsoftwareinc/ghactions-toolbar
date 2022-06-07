package com.dsoftware.githubactionstab.workflow.action

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RefreshAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger

class WorkflowRunReloadListAction : RefreshAction("Refresh Workflow Runs", null, AllIcons.Actions.Refresh) {
    override fun update(e: AnActionEvent) {
        val context = e.getData(WorkflowRunActionKeys.ACTION_DATA_CONTEXT)
        e.presentation.isEnabled = context != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("WorkflowRunReloadListAction action performed")
        e.getRequiredData(WorkflowRunActionKeys.ACTION_DATA_CONTEXT).resetAllData()
    }

    companion object {
        private val LOG = thisLogger()
    }
}