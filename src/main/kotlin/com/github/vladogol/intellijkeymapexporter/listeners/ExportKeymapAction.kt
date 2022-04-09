package com.github.vladogol.intellijkeymapexporter.listeners

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.SystemProperties
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


internal class ExportKeymapAction : AnAction(), DumbAware {
    data class SelectResult(val keymap: Keymap, val path: String)

    override fun actionPerformed(event: AnActionEvent) {
        val selectResult = selectKeymapDialog(event)
        if (selectResult != null) {
            val dumper = XmlDumper()
            val xmlString = dumper.dump(selectResult.keymap)
            File(selectResult.path, "${selectResult.keymap}.xml").writeText(xmlString)
        }
    }

    private fun selectKeymapDialog(event: AnActionEvent): SelectResult? {
        val keymaps = KeymapManagerEx.getInstanceEx().allKeymaps
        val outputPathLabelText = "Output path:"
        val pathControl = TextFieldWithBrowseButton()
        pathControl.addBrowseFolderListener(
            "Choose Output Path", outputPathLabelText, null,
            FileChooserDescriptorFactory.createSingleFileDescriptor(),
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        )
        val keymapComboBox = ComboBox(keymaps)
        pathControl.text = SystemProperties.getUserHome()
        val dialog: DialogWrapper = object : DialogWrapper(PlatformDataKeys.PROJECT.getData(event.dataContext)) {
            init {
                init()
                title = "Export Keymap to XML"
            }

            override fun createCenterPanel(): JComponent {
                val dialogPanel = JPanel(GridBagLayout())
                val c = GridBagConstraints()
                val labelKeymap = JLabel("Keymap:")
                val outputPathLabel = JLabel(outputPathLabelText)
                c.fill = GridBagConstraints.HORIZONTAL
                c.gridx = 0
                c.gridy = 0
                c.ipadx = 10
                dialogPanel.add(labelKeymap, c)
                c.gridx = 1
                dialogPanel.add(keymapComboBox, c)
                c.gridx = 0
                c.gridy = 1
                dialogPanel.add(outputPathLabel, c)
                c.gridx = 1
                c.gridy = 1
                dialogPanel.add(pathControl, c)
                return dialogPanel
            }
        }

        dialog.show()
        return if (!dialog.isOK) null else SelectResult(keymapComboBox.selectedItem as Keymap, pathControl.text)
    }
}
