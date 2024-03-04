package com.gruecorner.forwardproxy;

import com.gruecorner.forwardproxy.utils.CommandLineHelper;

class FwdProxy {
    public static void main(String inArgs[]) {
        CommandLineHelper.processArguments("FwdProxy", inArgs);
        if (CommandLineHelper.isHelp())
            System.out.println(CommandLineHelper.getHelp());
        else { // start the server

        }

    }
}