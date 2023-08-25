package com.krizej.ShellPipe;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;

public class ShellPipe extends AnAction {
    public ShellPipe() {
        super("ShellPipe");
    }

    public List<String> tokenize(String cmd) {
        List<String> ret = new ArrayList<>();

        boolean inQuote = false;
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            if(!inQuote && c == ' ') {
                ret.add(s.toString());
                s = new StringBuilder();
            } else if(c == '"') {
                inQuote = !inQuote;
            } else {
                s.append(c);
            }
        }

        if(!s.isEmpty())
            ret.add(s.toString());

        return ret;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Document document = editor.getDocument();

        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int start = primaryCaret.getSelectionStart();
        int end = primaryCaret.getSelectionEnd();

        String cmd = Messages.showInputDialog(project, "Run", "ShellPipe", null);

        if(cmd == null)
            return;

        String input = document.getText(new TextRange(start, end));

        try {
            Process proc = new ProcessBuilder()
                    .command(tokenize(cmd))
                    .directory(new File(project.getBasePath()))
                    .redirectErrorStream(true)
                    .start();

            BufferedWriter stdin = proc.outputWriter();
            stdin.write(input);
            stdin.flush();
            stdin.close();

            if(!proc.waitFor(1, TimeUnit.SECONDS)) {
                throw new Exception("command timed out");
            }

            BufferedReader rdr = proc.inputReader();
            StringBuilder output = new StringBuilder();
            while(rdr.ready())
                output.append((char)rdr.read());
            String finalstr = output.toString();

            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(start, end, finalstr)
            );

            primaryCaret.setSelection(start, start + finalstr.length());

        } catch (Exception e) {
            Messages.showErrorDialog(e.toString(), "ShellPipe Error");
        }
    }

}
