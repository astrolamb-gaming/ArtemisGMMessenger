/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import artemisgmmessenger.PersistenceData.Option;
import artemisgmmessenger.PersistenceData.OptionList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Matthew
 */
public class JSONPersistenceHandler {
    
    File persistenceFile;
    OptionList ol;
    HashMap<String,Object> optionList;
    public JSONPersistenceHandler(File file) {
        try {
            persistenceFile = file;
            if (persistenceFile.exists()) {
                FileInputStream fis = new FileInputStream(persistenceFile);
                ol = PersistenceData.OptionList.parseFrom(fis);
            } else {
                ol = PersistenceData.OptionList.getDefaultInstance();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveOptions() {
        for (Map.Entry<String, Object> s : optionList.entrySet()) {
            Option o = PersistenceData.Option.newBuilder().setName(s.getKey()).setValue(s.getValue().toString()).build();
            ol.getOptionsList().add(o);
        }
        try {
        FileOutputStream fis = new FileOutputStream(persistenceFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object get(String key, Object defaultResult) {
        Object o = get(key);
        if (o != null) {
            return o;
        } else {
            return defaultResult;
        }
    }
    public Object get(String key) {
        return optionList.get(key);
    }
    public void addOption(String key, Object o) {
        optionList.put(key, o);
    }
    public void removeOption(String key) {
        optionList.remove(key);
    }
}