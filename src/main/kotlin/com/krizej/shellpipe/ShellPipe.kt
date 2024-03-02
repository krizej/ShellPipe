package com.krizej.shellpipe

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import java.io.File
import java.util.concurrent.TimeUnit


class ShellPipe : AnAction() {

    private fun tokenize(cmd: String): List<String> {
        val ret: MutableList<String> = ArrayList()

        var inQuote = false
        val s = StringBuilder()
        for (c in cmd) {
            if (!inQuote && c == ' ') {
                ret.add(s.toString())
                s.clear()
            } else if (c == '"') {
                inQuote = !inQuote
            } else {
                s.append(c)
            }
        }

        if (s.isNotEmpty())
            ret.add(s.toString())

        return ret
    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val document: Document = editor.document

        val primaryCaret: Caret = editor.caretModel.primaryCaret
        val start: Int = primaryCaret.selectionStart
        val end: Int = primaryCaret.selectionEnd

        val p: Messages.InputDialog = Messages.InputDialog("Run", "ShellPipe", null, null, null)
        p.show()
        val cmd: String = p.inputString ?: return

        val input: String = document.getText(TextRange(start, end))

        try {
            val proc = ProcessBuilder()
                    .command(tokenize(cmd))
                    .directory(File(project.basePath!!))
                    .redirectErrorStream(true)
                    .start()

            val stdin = proc.outputWriter()
            stdin.write(input)
            stdin.flush()
            stdin.close()

            if (!proc.waitFor(1, TimeUnit.SECONDS)) {
                throw Exception("command timed out")
            }

            val rdr = proc.inputReader()
            val output = java.lang.StringBuilder()
            while (rdr.ready())
                output.append(rdr.read().toChar())
            val finalstr = output.toString()

            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(start, end, finalstr)
            }

            primaryCaret.setSelection(start, start + finalstr.length)
        } catch (e: Exception) {
            Messages.showErrorDialog(e.toString(), "ShellPipe Error")
        }
    }

}