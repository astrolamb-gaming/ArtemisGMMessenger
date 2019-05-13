/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import com.sun.java.swing.plaf.motif.MotifBorders;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
    
    int port = 2015;
    String ip = "starry.mytsn.net";
    Socket socket;
    String defaultIP = "starry.mytsn.net";
    Runnable r;
    JMenu indicator;
    ScheduledFuture<?> beeperHandle;
    
    List<String> stationList = new ArrayList<>();
    private boolean attemptingConnect;

    
    
    
    
    public ArtemisGMMessenger() {}
    
    public void setup() {
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
        //sendIdleTextAllClients("hello", "there");
        Dimension buttonSize = new Dimension(175,40);
        JFrame frame = new JFrame("Artemis Game Master Comms Console");
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
                connect(ip);
            }
        }); 
        menu.add(connectMenuItem);
        JMenu kickPlayerMenu = new JMenu("Kick Connected Console");
        menuBar.add(kickPlayerMenu);
        JMenuItem debug = new JMenuItem("Debug");
        indicator = new JMenu();
        indicator.setText("Not Connected");
        indicator.setForeground(Color.red); 
        menuBar.add(Box.createHorizontalGlue());
        menu.add(debug);
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
        messageLabel.setPreferredSize(new Dimension(15000,40));
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        
        JTextArea field = new JTextArea();
        field.setPreferredSize(new Dimension(15000, 60));
        field.setBorder(new EtchedBorder());
        messagePanel.add(field, BorderLayout.CENTER);
        
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
        JToggleButton side = new JToggleButton("Side");
        JToggleButton status = new JToggleButton("Status");
        JToggleButton player = new JToggleButton("Player");
        JToggleButton base = new JToggleButton("Station");
        JToggleButton enemy = new JToggleButton("Enemy");
        JToggleButton friend = new JToggleButton("Friend");
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

                String s = field.getText();
                if (s.equals("")) {
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
                
                sendPopupTextAllClients(stations, shipString, s); 
                

            }
        }); 
        JButton sendComms = new JButton("Send Comms Message"); 
        sendComms.setPreferredSize(buttonSize);
        sendComms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filter = "";
                for (JToggleButton j : filters) {
                    if (j.isSelected()) {
                        filter += "commsFilter"+j.getText();
                    }
                }
                String fromText = from.getText();
                String messageText = field.getText();
                sendCommsTextAllClients(filter, fromText, messageText); 
                sendIdleTextAllClients(fromText, messageText); 
            }
        }); 
        pops.add(sendPanel, BorderLayout.SOUTH);
        spacer = new JPanel();
        spacer.setPreferredSize(buttonSize);
        filterPanel.add(spacer);
        filterPanel.add(sendComms);
        sendPanel.add(sendPopup);
        frame.pack();
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

    } 
    
    public void connect(String ip) {
        System.out.println("Connecting");
        indicator.setText("Trying to connect...");
        indicator.setForeground(Color.yellow); 
        attemptingConnect = true;
        try {
            String wholeSentence = "";
            System.out.println("Making socket");
            if (attemptSocket(showIPPrompt())) {
                attemptingConnect = false;
                indicator.setText("Connected");
                indicator.setForeground(Color.GREEN); 
            } else {
                System.err.println("Didn't connect??");
                attemptingConnect = false;
            }
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }

    }
    private boolean attemptSocket(String ip) {
        if (ip == null) {
            indicator.setText("Not Connected");
            indicator.setForeground(Color.red);
            return false;
        }
        try {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Socket not closing!!!");
                }
            }
            System.out.println("Creating socket...");
            socket = new Socket(ip, port);
            defaultIP = ip;
            return true;
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host Exception");
            e.printStackTrace();
            indicator.setText("Not Connected");
            indicator.setForeground(Color.red);
            return false;
        } catch (ConnectException e) {
            indicator.setText("Not Connected");
            indicator.setForeground(Color.red);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            indicator.setText("Not Connected");
            indicator.setForeground(Color.red);
            System.out.println("IOException");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There is no server running at that IP address.");
            String s = showIPPrompt();
            if (s != null) {
                connect(s);
            } else {
                return false;
            }
            return false;
        }
    }
    public String showIPPrompt() {
        Object message = "IP to connect to:";
        String title = "Connect to Server";
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
    void sendDebugText(String text) {
        try {
            socket.getOutputStream().write(text.getBytes());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    void sendIdleTextAllClients(String from, String message) {
        String m = "sendIdleTextAllClients('" + from + "','" + message + "^');\n";
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
        String m = "sendCommsTextAllClients(" + filter + ",'" + from + "','" + message + "^');\n";
        System.out.println(m);
        try {
            socket.getOutputStream().write(m.getBytes());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sendPopupTextAllClients(String stations, String ships, String message) {
        if (!message.equals("")){
            //String s = ""
            
            String m = "sendPopup(" + stations + "," + ships +  ",'" + message + "');\n";
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
