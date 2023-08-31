package com.krizej.ShellPipe;



import com.intellij.openapi.ui.Messages;

import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ShellPipePopup extends Messages.InputDialog {
    static class HistoryListener implements KeyListener {
        private final JTextComponent field;
        private final HistoryService hist;
        private int idx = 0;

        public HistoryListener(JTextComponent field) {
            this.field = field;
            hist = HistoryService.getInstance();
        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {}

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if(keyEvent.getKeyCode() != KeyEvent.VK_UP && keyEvent.getKeyCode() != KeyEvent.VK_DOWN)
                return;

            int add = keyEvent.getKeyCode() == KeyEvent.VK_UP ? 1 : -1;
            idx += add;

            if(idx > hist.size()) {
                idx = 0;
            }

            if(idx < 0) {
                idx += hist.size() + 1;
            }

            if(idx == 0)
                field.setText("");
            else
                field.setText(hist.getString(idx - 1));
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {}
    };

    public ShellPipePopup(String message, String title) {
        super(message, title, null, null, null);
        myField.addKeyListener(new HistoryListener(myField));
        show();
    }
}


