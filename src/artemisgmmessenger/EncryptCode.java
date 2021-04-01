/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthew
 */
public class EncryptCode {
    
    Random random;
    
    public EncryptCode() {
        random = new Random();
    }
    
    char[] unknowns = {
        '!',
        '@',
        '#',
        '$',
        '%',
        '&',
        '*',
        '?',
        '~',
        '+',
        '=',
        '.',
        '.',
        '.',
        '.',
        '.',
        ',',
        '.'
    };

    String scrambleMessage(String message, String encryptKey) {
        //String encryptKey = (String)encryptionKeyComboBox.getSelectedItem();
        //String message = messageField.getText();
        
        int keyInt = encryptKey.hashCode();
        System.out.println(encryptKey + keyInt);
        if (encryptKey.equals("None")) {
            return message;
        } else if (encryptKey.equals("Garble")) {
            boolean start = random.nextBoolean();
            boolean useRegular = random.nextBoolean();
            int rand = random.nextInt(6);

            StringBuilder builder = new StringBuilder();
            int current = 0;
            boolean convert = random.nextBoolean();
            
            char[] chars = message.toCharArray();
            
            for (int i = 0; i < chars.length; i++) {
                if (i <= rand) {
                    if (useRegular) {
                            builder.append(chars[i]);
                    } else {
                            builder.append(unknowns[random.nextInt(unknowns.length)]);
                    }
                } 
                if (i >= rand) {
                    useRegular = !useRegular;
                    rand = random.nextInt(6); 
                    System.out.println("Rand: " + rand);
                    rand = rand + i;
                    rand = (useRegular) ? rand : rand/2;
                }
            }
            return builder.toString();

//            for (int i = 0; i < message.length(); i++) {
//                try {
//                    char letter = message.charAt(i);
//
//                    String hex = encrypt(Integer.toHexString((int)letter), keyInt);
//
//                    if (current <= rand) {
//
//                        if (convert) {
//                            builder.append(hex);
//                        } else {
//                            builder.append(letter);
//                        }
//                        current++;
//
//                    } else {
//
//                        convert = !convert;
//                        current = 0;
//                        rand = random.nextInt(6);
//
//                    }
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            return builder.toString();
        } else {
            try {
                
                return encrypt(message, keyInt);
            } catch (Exception ex) {
                Logger.getLogger(EncryptCode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "";
        //messageField.setText(builder.toString()); 
        //
    }
    
    
        // Encrypts text using a shift od s
    public static String encrypt(String text, int s)
    {
        StringBuilder result= new StringBuilder();
  
        for (int i=0; i<text.length(); i++)
        {
            if (Character.isUpperCase(text.charAt(i)))
            {
                char ch = (char)(((int)text.charAt(i) + s - 65) % 26 + 65);
                result.append(ch);
            }
            else
            {
                char ch = (char)(((int)text.charAt(i) + s - 97) % 26 + 97);
                result.append(ch);
            }
        }
        return result.toString();
    }

}
