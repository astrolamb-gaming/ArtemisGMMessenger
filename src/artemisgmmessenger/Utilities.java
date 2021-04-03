/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

/**
 *
 * @author Matthew Holderbaum
 */
public class Utilities {
         /**
     * get string from Clipboard
     */
    public static String getSysClipboardText() {
        String ret = "";
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

        Transferable clipTf = sysClip.getContents(null);

        if (clipTf != null) {

            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf
                            .getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    /**
     * put string into Clipboard
     */
    public static void setSysClipboardText(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }
    
    /**
     * Returns true if the item is added. It is only added if the string is not already an option.
     * @param jcb The JComboBox which is being added to.
     * @param newOption The new String being added to the combo box.
     * @return True if the new string is added as an option. Otherwise return false.
     */
    public static boolean TryAddOption(JComboBox jcb, String newOption) {
        boolean found = false;
        for (int i = 0; i < jcb.getItemCount(); i++) {
            if (newOption.equals((String)jcb.getItemAt(i))) {
                found = true;
            }
        }
        if (!found) {
            jcb.addItem(newOption);
            return true;
        }
        return false;
    }
}
