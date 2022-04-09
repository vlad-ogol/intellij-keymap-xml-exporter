package com.github.vladogol.intellijkeymapexporter.listeners

import com.intellij.openapi.actionSystem.KeyboardModifierGestureShortcut
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.MouseShortcut
import com.intellij.openapi.actionSystem.PressureShortcut
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.openapi.keymap.Keymap
import com.intellij.util.BitUtil
import java.awt.event.InputEvent
import java.util.*
import java.util.stream.Collectors
import javax.swing.KeyStroke

@Suppress("UNUSED_PARAMETER")
internal class XmlDumper {

    fun dump(keymap: Keymap): String {
        val xmlBody = keymap.actionIdList.stream()
                .map { actionId: String? -> toXmlString(actionId, keymap.getShortcuts(actionId)) }
                .filter {s: String? -> s != null}
                .collect(Collectors.joining("\n"))
        return "<keymap version=\"1\" name=\"${keymap.presentableName}\">\n$xmlBody\n</keymap>"
    }

    private fun toXmlString(actionId: String?, shortcuts: Array<Shortcut>): String? {
        if (shortcuts.isEmpty()) {
            return null
        }
        val shortcutsXml = Arrays.stream(shortcuts)
                .map { shortcut: Shortcut -> toShortcutXml(shortcut) }
                .collect(Collectors.joining("\n"))
        return "  <action id=\"$actionId\">\n$shortcutsXml\n  </action>"
    }

    private fun toShortcutXml(shortcut: Shortcut): String {
        if (shortcut is KeyboardShortcut) {
            return toShortcutXml(shortcut)
        } else if (shortcut is KeyboardModifierGestureShortcut) {
            return toShortcutXml(shortcut)
        } else if (shortcut is PressureShortcut) {
            return toShortcutXml(shortcut)
        } else if (shortcut is MouseShortcut) {
            return toShortcutXml(shortcut)
        }
        throw UnsupportedOperationException("unknown shortcut type")
    }

    // <keyboard-shortcut first-keystroke="ctrl meta 6" second-keystroke="f" />
    private fun toShortcutXml(shortcut: KeyboardShortcut): String {
        val firstStrokeAttr = keyStrokeToXmlString("first-keystroke", shortcut.firstKeyStroke)
        val secondStrokeAttr = keyStrokeToXmlString("second-keystroke", shortcut.secondKeyStroke)
        val shortcutXml = "    <keyboard-shortcut $firstStrokeAttr $secondStrokeAttr />"
        return shortcutXml
    }

    private fun keyStrokeToXmlString(keyStrokeName: String, keyStroke: KeyStroke?): String {
        if (keyStroke == null) {
            return ""
        } else {
            return "$keyStrokeName=\"%s\"".format(keyStroke.toString().replace("pressed ", ""))
        }
    }

    // <keyboard-gesture-shortcut keystroke="meta 1" modifier="dblClick"/>
    private fun toShortcutXml(shortcut: KeyboardModifierGestureShortcut): String {
        val keystrokeAttr = keyStrokeToXmlString("keystroke", shortcut.stroke)
        val modifierAttr = "modifier=\"${shortcut.type}\""
        return "    <keyboard-gesture-shortcut $keystrokeAttr $modifierAttr />"
    }

    // <mouse-shortcut keystroke="Force touch" />
    private fun toShortcutXml(shortcut: PressureShortcut): String {
        return """<mouse-shortcut keystroke="Force touch" />"""
    }

    // <mouse-shortcut keystroke="shift control meta button1" />
    private fun toShortcutXml(shortcut: MouseShortcut): String {
        val modifiers = getMouseModifiersString(shortcut)
        val button = getMouseButton(shortcut)
        val clickCountName = getClickCountName(shortcut)
        val shortcutXml = "    <mouse-shortcut keystroke=\"${modifiers}${button}${clickCountName}\" />"
        return shortcutXml
    }

    private fun getMouseModifiersString(shortcut: MouseShortcut): String {
        val modifiers = shortcut.modifiers
        var result = ""
        if (BitUtil.isSet(modifiers, InputEvent.SHIFT_DOWN_MASK)) {
            result += "shift "
        }
        if (BitUtil.isSet(modifiers, InputEvent.ALT_DOWN_MASK)) {
            result += "alt "
        }
        if (BitUtil.isSet(modifiers, InputEvent.ALT_GRAPH_DOWN_MASK)) {
            result += "alt "
        }
        if (BitUtil.isSet(modifiers, InputEvent.CTRL_DOWN_MASK)) {
            result += "ctrl "
        }
        if (BitUtil.isSet(modifiers, InputEvent.META_DOWN_MASK)) {
            result += "meta "
        }
        return result
    }

    private fun getMouseButton(shortcut: MouseShortcut): String {
        return "button" + shortcut.button
    }

    private fun getClickCountName(shortcut: MouseShortcut): String {
        return if (shortcut.clickCount == 2) " doubleClick" else ""
    }
}
