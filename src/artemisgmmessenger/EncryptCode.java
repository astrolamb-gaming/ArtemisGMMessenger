/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import java.util.Random;

/**
 *
 * @author Matthew
 */
public class EncryptCode {
    
    Random random;
    
    public EncryptCode() {
        random = new Random();
    }
        
    String scrambleMessage(String message, String encryptKey) {
        //String encryptKey = (String)encryptionKeyComboBox.getSelectedItem();
        //String message = messageField.getText();
        int rand = random.nextInt(6);

        StringBuilder builder = new StringBuilder();
        int current = 0;
        boolean convert = random.nextBoolean();
        
        for (int i = 0; i < message.length(); i++) {
            try {
                char letter = message.charAt(i);

                String hex = encrypt(Integer.toHexString((int)letter), encryptKey);

                if (current <= rand) {

                    if (convert) {
                        builder.append(hex);
                    } else {
                        builder.append(letter);
                    }
                    current++;

                } else {

                    convert = !convert;
                    current = 0;
                    rand = random.nextInt(6);

                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //messageField.setText(builder.toString()); 
        return builder.toString();
    }
    
    public static String encrypt(String strToBeEncoded,String strKey) throws Exception{
	String strData="";
	
	
	return strData;
}
  
}
