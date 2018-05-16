/*
 * Copyright 2011 Holger Brandl
 *
 * This code is licensed under BSD. For details see
 * http://www.opensource.org/licenses/bsd-license.php
 */

package io.github.holgerbrandl.send2terminal.connectors;


import com.intellij.openapi.ui.DialogBuilder;
import io.github.holgerbrandl.send2terminal.Utils;
import io.github.holgerbrandl.send2terminal.settings.S2TSettings;

import javax.swing.*;
import java.io.IOException;


/**
 * A connector using apple script
 *
 * @author Holger Brandl
 */
public class AppleScriptConnector implements CodeLaunchConnector {

    public static void main(String[] args) {
        new AppleScriptConnector().submitCode("write.table(head(iris), file=\"~/Desktop/iris.txt\", sep=\"\\t\")\n", true);
    }


    private static void submitCodeInternal(String codeSelection, boolean switchFocusToTerminal) {
        try {

            if (Utils.isMacOSX()) {
                Runtime runtime = Runtime.getRuntime();

                String dquotesExpandedText = codeSelection.replace("\\", "\\\\");
                dquotesExpandedText = dquotesExpandedText.replace("\"", "\\\"");

                // trim to remove tailing newline for blocks and especially line evaluation
                dquotesExpandedText = dquotesExpandedText.trim();


                String evalTarget = S2TSettings.getInstance().codeSnippetEvalTarget;
//                String evalTarget = "R64";

//                http://stackoverflow.com/questions/1870270/sending-commands-and-strings-to-terminal-app-with-applescript

                String evalSelection;
                if (evalTarget.equals("Terminal")) {
//                    if (codeSelection.length() > 1000) {
//                        DialogBuilder db = new DialogBuilder();
//                        db.setTitle("Operation canceled: ");
//                        db.setCenterPanel(new JLabel("Can't paste more that 1024 characters to MacOS terminal."));
//                        db.addOkAction();
//                        db.show();
//
//                        return;
//                    }

                    evalSelection = "tell application \"" + "Terminal" + "\" to do script \"" + dquotesExpandedText + "\" in window 0";

                    if (switchFocusToTerminal) {
                        evalSelection = "tell application \"Terminal\" to activate\n" + evalSelection;
                    }

                } else if (evalTarget.equals("iTerm")) {
                    evalSelection = "tell application \"iTerm\" to tell current session of current terminal  to write text  \"" + dquotesExpandedText + "\"";
                    if (switchFocusToTerminal) {
                        evalSelection = "tell application \"iTerm\" to activate\n" + evalSelection;
                    }

                } else {
                    if (switchFocusToTerminal) {
                        evalSelection = "tell application \"" + evalTarget + "\" to activate\n" +
                                "tell application \"" + evalTarget + "\" to cmd \"" + dquotesExpandedText + "\"";
                    } else {
                        evalSelection = "tell application \"" + evalTarget + "\" to cmd \"" + dquotesExpandedText + "\"";
                    }
                }

                String[] args = {"osascript", "-e", evalSelection};

                runtime.exec(args);
            }
        } catch (IOException e1) {
            ConnectorUtils.log.error(e1);
        }
    }


    @Override
    public void submitCode(String rCommands, boolean switchFocusToTerminal) {
        // If code is long split it up into chunks, because terminal does not accept more than 1024 characters
        // See http://unix.stackexchange.com/questions/204815/terminal-does-not-accept-pasted-or-typed-lines-of-more-than-1024-characters
        // See http://stackoverflow.com/questions/13216480/paste-character-limit
        // and for R in particular: Command lines entered at the console are limited[3] to about 4095 bytes (not characters).



        // chunk it --> does not work because if script is slow, paste buffer will be exceeded nevertheless

        // // this breaks words and does not work because evaluation is always terminated with enter
        // Iterable<String> textChunks = Splitter.fixedLength(1000).split(rCommands);

//        String lines[] = rCommands.split("\\r?\\n");
//        String lines[] = rCommands.split("\\R");
//        List<String> textChunks = new ArrayList<>();
//
//        int chunkLimit = 900; // actually 1024 would be possible but lest leave some room for along line
//        String curChunk = "";
//
//        for (String line : lines) {
//            if (curChunk.length() + line.length() + 1 > chunkLimit) {
//                textChunks.add(curChunk);
//                curChunk = line;
//            } else {
//                curChunk += "\n" + line;
//            }
//        }
//        // process the chunk in progress
//        if(!curChunk.isEmpty()) textChunks.add(curChunk);
//
//        textChunks.forEach(chunk -> {
//            submitCodeInternal(chunk, false);
//        });
//
//        if (switchFocusToTerminal) {
//            submitCodeInternal("", true);
//        }

        submitCodeInternal(rCommands, switchFocusToTerminal);
    }
}
