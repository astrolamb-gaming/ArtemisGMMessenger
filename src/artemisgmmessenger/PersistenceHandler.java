/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.w3c.dom.Document;


/**
 *
 * @author Matthew
 */
public class PersistenceHandler {
    
    public HashMap<String, String> options;
    
    private Document doc;
    private File persistenceFile;
    
    public PersistenceHandler(File file) {
        persistenceFile = file;
        try {
            options = getFileContent(file, "UTF-8");
            if (options == null) {
                options = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public void saveOptions() {
        try {
            setFileContents(persistenceFile, options, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static HashMap<String,String> getFileContent(File file, String encoding) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try(BufferedReader br = new BufferedReader( new InputStreamReader(fis, encoding ))) {
            HashMap<String,String> map = new HashMap<>();
            String line;
            while(( line = br.readLine()) != null ) {
                if (line.contains(":")) {
                    String[] ops = line.split(":");
                    map.put(ops[0], ops[1]);
                }
            }
            fis.close();
            br.close();
            return map;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
    
    public static void setFileContents(File file, HashMap<String,String> map, String encoding) throws IOException {
        String s = new String();
        for (String key : map.keySet()) {
            s = s.concat(key + ":" + map.get(key) + "\n");
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(s.getBytes(encoding));
            fos.flush();
            fos.close();
        } 
    }

}
