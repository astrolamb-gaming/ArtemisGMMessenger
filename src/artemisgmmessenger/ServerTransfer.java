/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Matthew
 */
public class ServerTransfer {
    
    List<JCheckBox> shipList;
    List<String> ipList;
    List<TransferListener> transferListeners;
    
    public ServerTransfer() {}
    
    public void setup() {
        
        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(650,700));
        frame.setLayout(new BorderLayout());
    
        shipList = new ArrayList<>();
        ipList = new ArrayList<>();
        transferListeners = new ArrayList<>();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel newServerPanel = new JPanel(new BorderLayout());
        mainPanel.add(newServerPanel, BorderLayout.NORTH);
        JComboBox cb = new JComboBox();
        
        cb.setEditable(true); 
        String item = "starry.mytsn.net:2010";
        cb.addItem(item);
        newServerPanel.add(cb);
        JPanel shipSelectionPanel = new JPanel(new MigLayout("flowy, fillx"));
        for (int i = 1; i < 9; i++) {
            JCheckBox c = new JCheckBox();
            c.setName("Ship " + String.valueOf(i));
            shipSelectionPanel.add(c);
            shipList.add(c);
            c.setText("Ship " + String.valueOf(i)); 
        }
        
        JPanel sendPanel = new JPanel(new MigLayout());
        JButton transfer = new JButton("Transfer");
        transfer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (TransferListener t : transferListeners) {
                    for (JCheckBox j : shipList) {
                        if (j.isSelected()) {
                            String s;
                            String i = (String)cb.getSelectedItem();
                            System.out.println(i);
                            String ip = i.substring(0, i.indexOf(":"));
                            System.out.println(j.getText());
                            String port = i.substring(i.indexOf(":")+1, i.length());
                            s = "updateShipServer(" + j.getText().replace(" ", "").replace("S", "s") + ",'" + ip +  "','" + port + "');\n";
                            t.transfer(s);
                        }
                    }
                    
                }
            }
        });
        sendPanel.add(transfer);
        mainPanel.add(sendPanel, BorderLayout.SOUTH);
        mainPanel.add(shipSelectionPanel); 
        frame.add(mainPanel);
        frame.pack();
    }
    
    public void addTransferListener(TransferListener t) {
        transferListeners.add(t);
    }
    
    public abstract class TransferListener {
        public TransferListener() {}
        public abstract void transfer(String s);
    }
}
