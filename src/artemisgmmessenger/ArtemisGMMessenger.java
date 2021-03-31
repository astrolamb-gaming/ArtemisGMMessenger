/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import artemisgmmessenger.ServerTransfer.TransferListener;
import com.sun.java.swing.plaf.motif.MotifBorders;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import net.miginfocom.swing.MigLayout;
import java.util.Random;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JComboBox;

/**
 *
 * @author Matthew
 */
public class ArtemisGMMessenger {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                
                
                ArtemisGMMessenger agmm = new ArtemisGMMessenger();
                agmm.setup();
                
                
                
            } 
        });
    }
    
    EncryptCode encryptCode;
    boolean cancelConnection;
    
    Random random;
    int port = 2015;
    String ip = "starry.mytsn.net";
    Socket socket;
    String defaultIP = "starry.mytsn.net";
    Runnable r;
    JMenu indicator;
    ScheduledFuture<?> beeperHandle;
    
    List<String> stationList = new ArrayList<>();
    private boolean attemptingConnect;
    private boolean isConnected = false;
    PersistenceHandler persistenceHandler;
    JTextArea messageField;
    JComboBox<String> encryptionKeyComboBox;
    String[] encryptionKeys = {
        "None",
        "Arvonian",
        "Kralien",
        "Skaraan",
        "Hegemony",
        "Torgoth",
        "Pirate",
        "N'Tani",
        "Hjorden",
        "Rumarian"
    };
    String currentKey;
    
    
    public ArtemisGMMessenger() {}
    
    public void setup() {
        
        encryptCode = new EncryptCode();
        
        random = new Random();
        
        // Some of these are supported but not used
        stationList.add("Helm");
        stationList.add("Weapons");
        stationList.add("Engineering");
        stationList.add("Science");
        stationList.add("Communications");
        stationList.add("Fighter");
        stationList.add("Data");
        stationList.add("Observer");
        stationList.add("CaptainsMap");
        stationList.add("GameMaster");
        stationList.add("MainScreen");
        // TODO: Get this working!!!!!!!!!!!
        File file = new File(System.getProperty("user.home"),".artemisGMMessenger");
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(System.getProperty("user.home"),".artemisGMMessenger/settings.dat");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        persistenceHandler = new PersistenceHandler(file);
        try {
            defaultIP = (String)persistenceHandler.options.get("defaultIP");
        } catch (NullPointerException e) {
            defaultIP = "localhost";
        }
//        if (defaultIP == null) {
//            
//        }
        //sendIdleTextAllClients("hello", "there");
        Dimension buttonSize = new Dimension(175,40);
        JFrame frame = new JFrame("Artemis Game Master Comms Console - v1.40");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(650,700));
        frame.setLayout(new BorderLayout());
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Connection");
        menuBar.add(menu);
        JMenuItem connectMenuItem = new JMenuItem("Connect");
        connectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        }); 
        JMenuItem transferButton = new JMenuItem("Server Transfer");
        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServerTransfer();
            }
        });
        JMenuItem debug = new JMenuItem("Debug");
        menu.add(connectMenuItem); 
        menu.add(debug);
        menu.add(transferButton);
        JMenu kickPlayerMenu = new JMenu("Kick Connected Console");
        menuBar.add(kickPlayerMenu);
        
        indicator = new JMenu();
        indicator.setText("Not Connected");
        indicator.setForeground(Color.red); 
        menuBar.add(Box.createHorizontalGlue());
        
        
        
        debug.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = showDebugTextPrompt();
                s+="\n";
                sendDebugText(s);
             }
        }); 
        menuBar.add(indicator);
        
        r = new Runnable() {
            @Override 
            public synchronized void run() {
                System.out.println("Checking runnable");
                if (!attemptingConnect) {
                    try {
                        if (socket != null && socket.getInputStream().read() != -1) {
                            System.out.println("Still connected");
                        } else {
                            indicator.setText("Not Connected"); 
                            indicator.setForeground(Color.red);
                            //beeperHandle.cancel(true);
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Not Connected");
                        indicator.setText("Not Connected");
                        indicator.setForeground(Color.red);
                        return;
                    }
                    try {
                        wait(500); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
            }
        };
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        frame.setJMenuBar(menuBar);
        
        
        JPanel top = new JPanel(new MigLayout("flowy"));
        top.setPreferredSize(new Dimension(600,180)); 
        frame.add(top, BorderLayout.NORTH); 
        top.setVisible(true);
        
        JPanel bottom = new JPanel(new BorderLayout());
        frame.add(bottom);
        
        JPanel buttonPanel = new JPanel(new MigLayout("flowx"));
        buttonPanel.setVisible(true);
        buttonPanel.setPreferredSize(new Dimension(600,500));
        bottom.add(buttonPanel, BorderLayout.CENTER);
        
        JPanel fromPanel = new JPanel(new BorderLayout());
        
        JLabel fromLabel = new JLabel("From: ");
        fromLabel.setPreferredSize(new Dimension(50,40));
        fromPanel.add(fromLabel,BorderLayout.NORTH);
        
        JTextField from = new JTextField(); 
        from.setPreferredSize(new Dimension(15000, 40));
        //from.setBorder(new EtchedBorder());
        fromPanel.add(from, BorderLayout.CENTER);
        top.add(fromPanel);
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("Message: "); 
        
        // Setting minimum size tells MigLayout to shrink when resized for some reason. Sounds like a bug to me, but not one of mine.
        messagePanel.setMinimumSize(new Dimension(10,10));
        messageLabel.setPreferredSize(new Dimension(1000,40));
        //messagePanel.add(messageLabel, BorderLayout.NORTH);
        
        JButton previewButton = new JButton("Preview Scrambled Text");
        previewButton.addActionListener(new ActionListener() {
                       
            String cachedMessage;
            boolean isEncryptedVisible = false;
            
            @Override
            public void actionPerformed(ActionEvent event) {
                if (isEncryptedVisible) {
                    String s = messageField.getText();
                    messageField.setText(cachedMessage);
                } else {
                    cachedMessage = messageField.getText();
                    messageField.setText(encryptCode.scrambleMessage(cachedMessage, currentKey));
                }
                isEncryptedVisible = !isEncryptedVisible;
            }
        });
        
        JLabel encryptOptionLabel = new JLabel("Scramble code: ");
        encryptionKeyComboBox = new JComboBox<>(encryptionKeys);
        encryptionKeyComboBox.setSelectedIndex(0);
        encryptionKeyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                currentKey = encryptionKeys[encryptionKeyComboBox.getSelectedIndex()];
            }
        });
        //messagePanel.add(encryptionKeyComboBox, BorderLayout.NORTH);
        
        JPanel messageThings = new JPanel(new MigLayout("flowx"));
        messageThings.add(messageLabel);
        messageThings.add(previewButton);
        messageThings.add(encryptOptionLabel);
        messageThings.add(encryptionKeyComboBox);
        messagePanel.add(messageThings, BorderLayout.NORTH);
        
        messageField = new JTextArea();
        messageField.setPreferredSize(new Dimension(15000, 60));
        messageField.setBorder(new EtchedBorder());
        messageField.setLineWrap(true); 
        messagePanel.add(messageField, BorderLayout.CENTER);

        top.add(messagePanel);
        
        
        
        
        JPanel allPopup = new JPanel(new MigLayout("flowx, fillY"));
        JPanel pops = new JPanel(new BorderLayout(10, 10));
        pops.add(allPopup, BorderLayout.CENTER);
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(40,100));

        JPanel shipPanel = new JPanel(new MigLayout("flowy, fillx"));
        JPanel popupPanel = new JPanel(new MigLayout("flowy, fillx"));
        JPanel filterPanel = new JPanel(new MigLayout("flowy, fillx"));
        allPopup.add(shipPanel);
        allPopup.add(popupPanel);
        buttonPanel.add(filterPanel);
        buttonPanel.add(spacer);
        buttonPanel.add(pops);
        
        pops.setBorder(new MotifBorders.FocusBorder(Color.blue, Color.white));
        filterPanel.setBorder(new MotifBorders.FocusBorder(Color.blue, Color.white));

        JToggleButton all = new JToggleButton("All Ships");
        JToggleButton ship1 = new JToggleButton("Ship 1");
        JToggleButton ship2 = new JToggleButton("Ship 2");
        JToggleButton ship3 = new JToggleButton("Ship 3");
        JToggleButton ship4 = new JToggleButton("Ship 4");
        JToggleButton ship5 = new JToggleButton("Ship 5");
        JToggleButton ship6 = new JToggleButton("Ship 6");
        JToggleButton ship7 = new JToggleButton("Ship 7");
        JToggleButton ship8 = new JToggleButton("Ship 8");
        JButton clearShips = new JButton("Clear Ship Options");

        List<JToggleButton> ships = new ArrayList<>();
        ships.add(ship1);
        ships.add(ship2);
        ships.add(ship3);
        ships.add(ship4);
        ships.add(ship5);
        ships.add(ship6);
        ships.add(ship7);
        ships.add(ship8);

        HashMap<JToggleButton, ButtonState> shipStates = new HashMap<>();
        shipStates.put(ship1, new ButtonState(false));
        shipStates.put(ship2, new ButtonState(false));
        shipStates.put(ship3, new ButtonState(false));
        shipStates.put(ship4, new ButtonState(false));
        shipStates.put(ship5, new ButtonState(false));
        shipStates.put(ship6, new ButtonState(false));
        shipStates.put(ship7, new ButtonState(false));
        shipStates.put(ship8, new ButtonState(false));

        JLabel popupLabel = new JLabel("Popup Messages:");
        popupLabel.setPreferredSize(buttonSize);
        popupPanel.add(popupLabel, "spanx");
        JToggleButton allConsoles = new JToggleButton("Send to All");
        JToggleButton toMain = new JToggleButton("Send to Mainscreen");
        JToggleButton toHelm = new JToggleButton("Send to Helm");
        JToggleButton toWea = new JToggleButton("Send to Weapons");
        JToggleButton toEng = new JToggleButton("Send to Engineering");
        JToggleButton toScience = new JToggleButton("Send to Science");
        JToggleButton toComms = new JToggleButton("Send to Comms");
        JToggleButton toCaptain = new JToggleButton("Send to Captain's Map");
        
        
        
        
        JButton clearConsole = new JButton("Clear Console Options");

        HashMap<JToggleButton, ButtonState> consoleStates = new HashMap<>();
        consoleStates.put(toScience, new ButtonState(false));
        consoleStates.put(toComms, new ButtonState(false));
        consoleStates.put(toCaptain, new ButtonState(false));
        consoleStates.put(toEng, new ButtonState(false));
        consoleStates.put(toHelm, new ButtonState(false));
        consoleStates.put(toWea, new ButtonState(false));
        consoleStates.put(toMain, new ButtonState(false));

        List<JToggleButton> consoles = new ArrayList<>();
        consoles.add(toScience);
        consoles.add(toComms);
        consoles.add(toCaptain);
        consoles.add(toEng);
        consoles.add(toHelm);
        consoles.add(toWea);
        consoles.add(toMain);
        
        

        shipPanel.add(all);
        shipPanel.add(ship1);
        shipPanel.add(ship2); 
        shipPanel.add(ship3);
        shipPanel.add(ship4);
        shipPanel.add(ship5);
        shipPanel.add(ship6);
        shipPanel.add(ship7);
        shipPanel.add(ship8);
        shipPanel.add(clearShips, "wrap");
        for (JToggleButton j : ships) {
            j.setPreferredSize(buttonSize); 
        }

        //panel.add(spacer);
        popupPanel.add(allConsoles, "spanx");
        allConsoles.setPreferredSize(buttonSize); 
        for (JToggleButton b : consoles) {
            popupPanel.add(b, "spanx");
            b.setPreferredSize(buttonSize);
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    allConsoles.setSelected(false);
                }
            }); 
        }
        popupPanel.add(clearConsole, "spanx");
        clearConsole.setPreferredSize(buttonSize); 

        JPanel spacer2 = new JPanel();
        spacer2.setPreferredSize(new Dimension(60, 40)); 
        buttonPanel.add(spacer2);



        JLabel filterLabel = new JLabel("Comms Message Options:");
        filterLabel.setPreferredSize(buttonSize);
        filterPanel.add(filterLabel, "spanx");
        JToggleButton alert = new JToggleButton("Alert");
        alert.setForeground(Color.RED);
        JToggleButton side = new JToggleButton("Side");
        side.setForeground(Color.blue); 
        JToggleButton status = new JToggleButton("Status");
        status.setForeground(Color.red);
        JToggleButton player = new JToggleButton("Player");
        player.setForeground(Color.green);
        JToggleButton base = new JToggleButton("Station");
        base.setForeground(Color.YELLOW); 
        JToggleButton enemy = new JToggleButton("Enemy");
        enemy.setForeground(Color.red);
        JToggleButton friend = new JToggleButton("Friend");
        friend.setForeground(Color.blue); 
        List<JToggleButton> filters = new ArrayList<>();
        filters.add(alert);
        filters.add(side);
        filters.add(status);
        filters.add(player);
        filters.add(base);
        filters.add(enemy);
        filters.add(friend);
        for (JToggleButton f : filters) {
            f.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (JToggleButton j : filters) {
                        if (f != j) {
                            j.setSelected(false);
                        }
                    }
                }
            }); 
            f.setPreferredSize(buttonSize);
            filterPanel.add(f, "spanx"); 
        }
        

        all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(all.isSelected());
                if (all.isSelected()) {
                    for(JToggleButton j : shipStates.keySet()) {
                        j.setSelected(all.isSelected()); 
                    }
                } else {
                    for (JToggleButton j : shipStates.keySet()) {
                        j.setSelected(shipStates.get(j).bool);
                    }
                }
            }
        });

        allConsoles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (allConsoles.isSelected()) {
                    for (JToggleButton j : consoleStates.keySet()) {
                        j.setSelected(allConsoles.isSelected());
                    }
                } else {
                    for (JToggleButton j : consoleStates.keySet()) {
                        j.setSelected(consoleStates.get(j).bool);
                    }
                }
            }
        }); 
        
        clearShips.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JToggleButton j : ships) {
                    j.setSelected(false);
                    shipStates.get(j).bool = false;
                }
            }
        });
        
        clearConsole.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JToggleButton j : consoles) {
                    j.setSelected(false);
                    consoleStates.get(j).bool = false;
                }
            }
        }); 

        for (JToggleButton j : shipStates.keySet()) {
            j.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shipStates.get(j).bool = j.isSelected();
                }
            }); 
        }


        for (JToggleButton j : consoleStates.keySet()) {
            j.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    consoleStates.get(j).bool = j.isSelected();
                }
            }); 
        }
        
        JPanel sendPanel = new JPanel();
        JPanel s = new JPanel();
        s.setPreferredSize(new Dimension(500,50));
        
        JButton sendPopup = new JButton("Send Popup Message");
        sendPopup.setPreferredSize(buttonSize);
        sendPopup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Send message code here
                System.out.println("Sending... (Not really)");
                if (!isConnected) {
                    showNotConnectedWarning("You aren't connected to an artemis server with Hermes support! Please check your connection.");
                    return;
                }
                
                String stations = "";
                String shipString = "";
                for (JToggleButton j : ships) {
                    if (j.isSelected() && j.getText().matches("Ship \\d")) { 
                        System.out.println("Worked!");
                        if (shipString.equals("")) {
                            shipString += "popup" + j.getText().replace(" ", ""); 
                        } else {
                            shipString += "+popup" + j.getText().replace(" ", "");
                        }
                    }
                }
                // Check if no ships are selected. If so, send error popup message
                if (shipString.equals("")) {
                    JOptionPane.showMessageDialog(null, "One or more ships need to be selected to send a popup message.", "No Ship Selected!", JOptionPane.OK_OPTION);
                    return;
                }
                
                for (JToggleButton j : consoles) {
                    String js = j.getText().replace("Send to ", "").substring(0,3); 
                    if (j.isSelected()) {
                        System.out.println(js);
                        for (String cL : stationList) {
                            String console = cL.substring(0, 3);
                            System.out.println(console);
                            if (console.equals(js)) {
                                System.out.println("Equal!");
                                if (stations.equals("")) {
                                    stations += "popup" + cL;
                                } else {
                                    stations += "+popup" + cL;
                                }
                            }
                        }
                    }
                }
                // Check if no consoles are selected. If so, send error popup message
                if (stations.equals("")) {
                    JOptionPane.showMessageDialog(null, "One or more consoles need to be selected to send a popup message.", "No Console Selected!", JOptionPane.OK_OPTION);
                    return;
                }
                
                try {
                    sendPopupTextAllClients(stations, shipString, handleMessageField());  
                } catch (Exception ex) {
                    // Don't send message
                }
                

            }
        }); 
        JButton sendComms = new JButton("Send Comms Message"); 
        sendComms.setPreferredSize(buttonSize);
        sendComms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    showNotConnectedWarning("Warning! Not connected to an Artemis server with Hermes support. Please check your connection.");
                    return;
                }
                String filter = "";
                for (JToggleButton j : filters) {
                    if (j.isSelected()) { 
                        filter += "commsFilter"+j.getText();
                    }
                }
                if (filter.equals("")) {
                    JOptionPane.showMessageDialog(null, "A Comms Message Option must be selected to send a comms message.", "No Comms Message Options Selected!", JOptionPane.OK_OPTION);
                    return;
                }
                
                String fromText = from.getText();
                if (fromText.equals("")) {
                    JOptionPane.showMessageDialog(null, "An origin must be specified to send a comms message.", "No Comms Origin!", JOptionPane.OK_OPTION);
                    return;
                }
//                String messageText = messageField.getText();
//                if (messageText.equals("")) {
//                    j
//                }
                try {
                    sendCommsTextAllClients(filter, fromText, handleMessageField()); 
                    sendIdleTextAllClients(fromText, handleMessageField()); 
                } catch (Exception ex) {
                    
                }
            }
        }); 
        pops.add(sendPanel, BorderLayout.SOUTH);
        spacer = new JPanel();
        spacer.setPreferredSize(buttonSize);
        filterPanel.add(spacer);
        filterPanel.add(sendComms);
        sendPanel.add(sendPopup);
        
        beeperHandle = scheduler.scheduleWithFixedDelay(r, 5, 5, SECONDS);
//          scheduler.schedule(new Runnable() {
//            public void run() { beeperHandle.cancel(true); }
//        }, 60 * 60, SECONDS);
        
        for (int i = 1; i < 9; i++) {
            final int j = i;
            JMenu ship = new JMenu("Ship " + i);
            JMenuItem helm = new JMenuItem("Helm");
            helm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String shipNums = String.valueOf(j);
                    String consoleNums = "Helm";
                    System.out.println("Kicking Hlm");
                    kickClients(shipNums, consoleNums);
                }
            }); 
            JMenuItem wea = new JMenuItem("Weapons");
            wea.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String shipNums = String.valueOf(j);
                    String consoleNums = "Weapons";
                    System.out.println("Kicking Weapons");
                    kickClients(shipNums, consoleNums);
                }
            }); 
            JMenuItem eng = new JMenuItem("Engineering");
            eng.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String shipNums = String.valueOf(j);
                    String consoleNums = "Engineering";
                    System.out.println("Kicking Eng");
                    kickClients(shipNums, consoleNums);
                }
            }); 
            
            JMenuItem allConsole = new JMenuItem("Kick Everyone from ship");
            allConsole.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String shipNums = String.valueOf(j);
                    String consoleNums = "";
                    for (String s : stationList) {
                        consoleNums += s;
                        consoleNums += ",";
                    }
                    consoleNums = consoleNums.substring(0, consoleNums.length()-1);
                    System.out.println("All: " + consoleNums);
                    kickClients(shipNums, consoleNums);
                }
            }); 
            ship.add(helm);
            ship.add(wea);
            ship.add(eng);
            ship.add(allConsole);
            kickPlayerMenu.add(ship);
            
        }
        JMenuItem everone = new JMenuItem("Kick Everyone");
        everone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = "";
                for (String c : stationList) {
                    s += c;
                    s+= ",";
                }
                s = s.substring(0, s.length()-1);
                for (int m = 1; m < 9; m++){
                    kickClients(String.valueOf(m), s); 
                }
            }
        });
        kickPlayerMenu.add(everone);
        JMenuItem gm = new JMenuItem("Kick Game Master");
        gm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int m = 1; m < 9; m++) {
                    kickClients(String.valueOf(m), "GameMaster");
                }
            }
        });
        kickPlayerMenu.add(gm);
        
        frame.pack();

    } 
    
    public String handleMessageField() throws Exception {
        String s = messageField.getText(); 
        if (s.equals("")) {
            throw new Exception("Message Field empty");
        } 
        return s;
    }
    
    public void connect() {
        if (cancelConnection) {
            cancelConnection = false;
            return;
        }
        System.out.println("Connecting");
        indicator.setText("Trying to connect...");
        indicator.setForeground(Color.yellow); 
        attemptingConnect = true;
        try {
            //String wholeSentence = "";
            System.out.println("Making socket");
            String adr = showIPPrompt();
            if (attemptSocket(adr)) {
                attemptingConnect = false;
                setConnected(true); 
            } else {
                System.err.println("Didn't connect??");
                attemptingConnect = false;
                setConnected(false); 
                if (!cancelConnection) {
                    connect();
                }
            }
        } catch (Exception e) {
            setConnected(false); 
            System.out.println("Exception");
            e.printStackTrace();
        }

    }
    private boolean attemptSocket(String ip) {
        if (ip == null) {
            setConnected(false); 
            return false;
        }
        try {
            if (socket != null) {
                try {
                    socket.close();
                    setConnected(false); 
                } catch (IOException e) {
                    setConnected(false); 
                    System.err.println("Socket not closing!!!");
                }
            }
            System.out.println("Creating socket...");
            socket = new Socket(ip, port);
            defaultIP = ip;
            setConnected(true); 
            return true;
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host Exception");
            e.printStackTrace();
            setConnected(false); 
            return false;
        } catch (ConnectException e) {
            setConnected(false); 
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            setConnected(false); 
            System.out.println("IOException");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There is no server running at that IP address.");
//            String s = showIPPrompt();
//            if (s != null) {
//                connect(s);
//            } else {
//                return false;
//            }
            return false;
        }
    }
    
    /**
     * Displays a prompt requesting the desired IP address with which to connect.
     * @return A string representing an IP address
     */
    public String showIPPrompt() {
        Object message = "IP to connect to:";
        String title = "Connect to Server";
        String[] options;
        System.out.println(persistenceHandler.options ==  null);
        if (persistenceHandler.options.containsKey("ipList")) {
            options = persistenceHandler.options.get("ipList").split(",");
        } else {
            options = new String[]{defaultIP};
        }
        System.out.println(Arrays.toString(options));
        JComboBox jcb = new JComboBox(options);
        jcb.setEditable(true); 
        //JOptionPane.show
        message = jcb;
        int response = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION);//, null, options, defaultIP);
        System.out.println(response);
        if (response != 0) {
            cancelConnection = true;
            return null;
        }
        String s = (String)jcb.getSelectedItem();
        System.out.println(jcb.getSelectedItem().toString());
        if (s == null) {
            return null;
        }
        if (!s.equals("")) {
            this.ip = s;
            persistenceHandler.options.put("defaultIP", s);
            if (persistenceHandler.options.containsKey("ipList")) {
                if (!persistenceHandler.options.get("ipList").contains(s)) {
                    persistenceHandler.options.put("ipList", persistenceHandler.options.get("ipList") + "," + s);
                }
            } else {
                persistenceHandler.options.put("ipList", s);
            }
            persistenceHandler.saveOptions();
            defaultIP = s;
            //connect(ip);
        } else {
            JOptionPane.showMessageDialog(null, "That is not a valid IP address.");
            s = showIPPrompt();
        }
        
        return s;
    }
    
    public String showDebugTextPrompt() {
        Object message = "Debug Message";
        String title = "Send Debug Message";
        String s = (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE, null, null, defaultIP);
        if (s == null) {
            return null;
        }
        if (!s.equals("")) {
            this.ip = s;
            //connect(ip);
        } else {
            JOptionPane.showMessageDialog(null, "That is not a valid IP address.");
            s = showIPPrompt();
        }
        return s;
    }
    
    /**
     * popup message intended to indicate that the client is not connected.
     * @param text 
     */
    public void showNotConnectedWarning(String text) {
        //JOptionPane.showConfirmDialog(null, text);
        JOptionPane.showMessageDialog(null, text, "Connection Error!", JOptionPane.OK_OPTION);
    }
    
    void sendDebugText(String text) {
        try {
            socket.getOutputStream().write(text.getBytes());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    String filterString(String str) {
        
        str = str.replace("'", "\'");
        str = str.replace("\\","\\\\");
        str = str.replace("\"", "\\\"");
        
        return str;
    }
    
    void sendIdleTextAllClients(String from, String message) {
        //from = from.replace("\\","\\\\");
        //from = from.replace("/", message);
        from = filterString(from);
        message = filterString(message);
        //TODO: Figure out how to utilize the "key"
        message = encryptCode.scrambleMessage(message, currentKey);
        messageField.setText(message); 
        String m = "sendIdleTextAllClients(\"" + from + "\",\"" + message + "^\");\n";
        System.out.println(m);
        try {
            socket.getOutputStream().write(m.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sendCommsTextAllClients(String filter, String from, String message) {
        if (filter.equals("")) {
            filter = "0";
        }
        from = filterString(from);
        message = filterString(message);
        String m = "sendCommsTextAllClients(" + filter + ",\"" + from + "\",\"" + message + "^\");\n";
        System.out.println(m);
        try {
            socket.getOutputStream().write(m.getBytes());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sendPopupTextAllClients(String stations, String ships, String message) {
        message = filterString(message);
        if (!message.equals("")){
            //String s = ""
            
            String m = "sendPopup(" + stations + "," + ships +  ",\"" + message + "\");\n";
            System.out.println(m);
            try { 
                socket.getOutputStream().write(m.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }  
    void kickClients(String ships, String stations) {
        if (ships.equals("") || stations.equals("")) {
            System.err.println("Sting is empty! Should not be!");
            return;
        }
        stations = stations.substring(0, stations.length());
        ships = "kickShip" + ships;
        stations = "kick" + stations;
        String m = "kickClients(" + ships + "," + stations + ");\n";
        System.out.println(m);
        try { 
            socket.getOutputStream().write(m.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void transferServers(String s) {
        try {
            socket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void startServerTransfer() {
        ServerTransfer st = new ServerTransfer();
        st.setup();
        st.addTransferListener(st.new TransferListener() {
            @Override
            public void transfer(String s) {
                try {
                    System.out.println("Trying to send:");
                    System.out.println(s);
                    socket.getOutputStream().write(s.getBytes()); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
    }
    
    void setConnected (boolean connected) {
        this.isConnected = connected;
        if (connected) {
            indicator.setText("Connected");
            indicator.setForeground(Color.green);
        } else {
            indicator.setText("Not Connected");
            indicator.setForeground(Color.red); 
        }
    }
    
    class ButtonState {
        
        public boolean bool;
        
        public ButtonState(boolean b) {
            bool = b;
        }
    
    }
    class ConnectException extends IOException {
        public ConnectException() {
            super();
        }
    }

}
