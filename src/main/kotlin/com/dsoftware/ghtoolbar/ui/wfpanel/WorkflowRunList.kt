package com.dsoftware.ghtoolbar.ui.wfpanel

import com.dsoftware.ghtoolbar.api.model.GitHubWorkflowRun
import com.dsoftware.ghtoolbar.ui.Icons
import com.dsoftware.ghtoolbar.workflow.action.ActionKeys
import com.intellij.icons.AllIcons
import com.intellij.ide.CopyProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.ListUtil
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.util.text.DateFormatUtil
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListUiUtil
import com.intellij.util.ui.UIUtil
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.event.MouseEvent
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import javax.swing.*


class WorkflowRunList(model: ListModel<GitHubWorkflowRun>) : JBList<GitHubWorkflowRun>(model), DataProvider,
    CopyProvider {

    init {
        selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val renderer = WorkflowRunsListCellRenderer()
        cellRenderer = renderer
        putClientProperty(UIUtil.NOT_IN_HIERARCHY_COMPONENTS, listOf(renderer))

        ScrollingUtil.installActions(this)
    }

    override fun getToolTipText(event: MouseEvent): String? {
        val childComponent = ListUtil.getDeepestRendererChildComponentAt(this, event.point)
        if (childComponent !is JComponent) return null
        return childComponent.toolTipText
    }

    override fun getData(dataId: String): Any? = when {
        PlatformDataKeys.COPY_PROVIDER.`is`(dataId) -> this
        ActionKeys.SELECTED_WORKFLOW_RUN.`is`(dataId) -> selectedValue
        else -> null
    }

    private inner class WorkflowRunsListCellRenderer : ListCellRenderer<GitHubWorkflowRun>, JPanel() {

        private val stateIcon = JLabel()
        private val title = JLabel()
        private val info = JLabel()
        private val labels = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
        }
        private val assignees = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
        }

        init {
            border = JBUI.Borders.empty(5, 8)

            layout = MigLayout(
                LC().gridGap("0", "0")
                    .insets("0", "0", "0", "0")
                    .fillX()
            )

            val gapAfter = "${JBUI.scale(5)}px"
            add(
                stateIcon, CC()
                    .gapAfter(gapAfter)
            )
            add(
                title, CC()
                    .growX()
                    .pushX()
                    .minWidth("pref/2px")
            )
            add(
                labels, CC()
                    .minWidth("pref/2px")
                    .alignX("right")
                    .wrap()
            )
            add(
                info, CC()
                    .minWidth("pref/2px")
                    .skip(1)
                    .spanX(3)
            )
        }

        override fun getListCellRendererComponent(
            list: JList<out GitHubWorkflowRun>,
            ghWorkflowRun: GitHubWorkflowRun,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            UIUtil.setBackgroundRecursively(this, ListUiUtil.WithTallRow.background(list, isSelected, list.hasFocus()))
            val primaryTextColor = ListUiUtil.WithTallRow.foreground(isSelected, list.hasFocus())
            val secondaryTextColor = ListUiUtil.WithTallRow.secondaryForeground(list, isSelected)

            stateIcon.icon = ghWorkflowRunIcon(ghWorkflowRun)
            title.apply {
                text = ghWorkflowRun.head_commit.message
                foreground = primaryTextColor
            }

            info.apply {
                text = ghWorkflowRunInfo(ghWorkflowRun)
                foreground = secondaryTextColor
            }
            labels.apply {
                removeAll()
                add(JBLabel(" ${ghWorkflowRun.head_branch} ", UIUtil.ComponentStyle.SMALL).apply {
                    foreground = JBColor(ColorUtil.softer(secondaryTextColor), ColorUtil.softer(secondaryTextColor))
                })
                add(Box.createRigidArea(JBDimension(4, 0)))
            }
            return this
        }
    }

    companion object {
        private val LOG = thisLogger()
        fun ghWorkflowRunIcon(ghWorkflowRun: GitHubWorkflowRun): Icon {
            return when (ghWorkflowRun.status) {
                "completed" -> {
                    when (ghWorkflowRun.conclusion) {
                        "success" -> AllIcons.Actions.Commit
                        "failure" -> Icons.X
                        else -> Icons.PrimitiveDot
                    }
                }
                "queued" -> Icons.PrimitiveDot
                "in progress" -> Icons.PrimitiveDot
                "neutral" -> Icons.PrimitiveDot
                "success" -> AllIcons.Actions.Commit
                "failure" -> Icons.X
                "cancelled" -> Icons.X
                "action required" -> Icons.Watch
                "timed out" -> Icons.Watch
                "skipped" -> Icons.X
                "stale" -> Icons.Watch
                else -> Icons.PrimitiveDot
            }
        }

        fun ghWorkflowRunInfo(ghWorkflowRun: GitHubWorkflowRun): String {
            val updatedAtLabel =
                if (ghWorkflowRun.updated_at == null) "Unknown"
                else makeTimePretty(ghWorkflowRun.updated_at)

            var action = "pushed by"
            if (ghWorkflowRun.event == "release") {
                action = "created by"
            }
            return "${ghWorkflowRun.workflowName} #${ghWorkflowRun.run_number}: " +
                "$action ${ghWorkflowRun.head_commit.author.name} " +
                "on $updatedAtLabel"
        }

        fun makeTimePretty(date: Date): String {
            val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
            val zonedDateTime = localDateTime.atZone(ZoneOffset.UTC)
            return DateFormatUtil.formatPrettyDateTime(zonedDateTime.toInstant().toEpochMilli())
        }
    }

    override fun performCopy(dataContext: DataContext) {
        TODO("Not yet implemented")
    }

    override fun isCopyEnabled(dataContext: DataContext): Boolean {
        return false
    }

    override fun isCopyVisible(dataContext: DataContext): Boolean {
        return false
    }
}