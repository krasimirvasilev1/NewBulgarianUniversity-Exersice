package server;

import client.ClientRegister;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server class
 * it encapsulate most methods of quizz app
 * and manages players and theirs messages
 */
public class MyServer extends javax.swing.JFrame {

    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea msgBox;
    private javax.swing.JLabel sStatus;
    private String mode = "f";// first in first
    String iD;
    boolean isStartedGame = false, isAndedGame = false;
    Entry entry = new Entry("q?", "R");
    String answer = "";
    HashMap<String, Boolean> answeringClients = new HashMap<>();
    HashMap<String, Integer> listClientPoints = new HashMap<>();

    public static ServerSocket ss;
    DataOutputStream dout;
    ConcurrentHashMap clientColl=new ConcurrentHashMap ();
    Quiz quiz;
    ArrayList<Integer> pickedEntries = new ArrayList<Integer>();

    boolean isWaitingAnswer = false;

    int connectedClients = 0;

    //Constructor
    public MyServer() {
        iD = "myServer";
        initComponents();
        this.quiz = new Quiz();
        try {

            ss=new ServerSocket(2089);
            this.sStatus.setText("Server Started.");

            acceptClient();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * method that accept the client socket and start it thread
     */
    private void acceptClient() {
        new ClientAccept().start();
    }

    /**
     * inner class to help connecting with clients sockets
     */
    class ClientAccept extends Thread {
        public void run(){

            while(true){
                try {
                    Socket s=ss.accept();

                    String i=new DataInputStream(s.getInputStream()).readUTF();
                    if(clientColl.containsKey(i)){
                        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
                        dout.writeUTF("You Are Already Registered....!!");
                    }else{
                        receiveRequest(s, i);
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * method to receive requests from players
         * @param s
         * @param idClient
         */
        private void receiveRequest(Socket s, String idClient) {
            clientColl.put(idClient,s);
            msgBox.append(idClient+" Joined !\n");
            DataOutputStream dout= null;
            try {
                dout = new DataOutputStream(s.getOutputStream());
                dout.writeUTF("");
                new MsgRead(s,idClient).start();
                new PrepareClientList().start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * inner class to read messages from clients sockets
     */
    class MsgRead extends Thread {
        Socket s;
        String ID;
        MsgRead(Socket s, String ID){
            this.s=s;
            this.ID=ID;
        }
        public void run() {

            if(!isAndedGame) {

            while (!clientColl.isEmpty()) {

                try {

                    for (Object soc : clientColl.values()) {
                        dout = new DataOutputStream(((Socket) soc).getOutputStream());
                    }


                    connectedClients++;

                    if (!isStartedGame) {
                        if (connectedClients >= ClientRegister.numberOfClients) {
                            startGame();
                        }
                    }
                    else if (isStartedGame) {
                        List<String> keyList = new ArrayList<String>(answeringClients.keySet());

                        if (isWaitingAnswer && !answeringClients.get(ID)) {

                            String message = receiveResponse(s, ID);
                            evalResponse(ID, message);

                        } else if(!answeringClients.isEmpty() && mode.equals("f")
                        && answeringClients.get(keyList.get(0))
                                && answeringClients.get(keyList.get(1))
                                && answeringClients.get(keyList.get(2))) {
                            sendQuestion("all");
                        } else if (!answeringClients.isEmpty() && !mode.equals("f")
                        && allAnswering(getHigherScoreClient())){
                            sendQuestion(getHigherScoreClient());
                        }
                    }

                    //--------------------

                    String i = new DataInputStream(s.getInputStream()).readUTF();
                    if (i.equals("mkoihgteazdcvgyhujb096785542AXTY")) {
                        clientColl.remove(ID);
                        msgBox.append(ID + ": removed! \n");
                        new PrepareClientList().start();
                        Set k = clientColl.keySet();
                        Iterator itr = k.iterator();
                        while (itr.hasNext()) {
                            String key = (String) itr.next();
                            if (!key.equalsIgnoreCase(ID)) {
                                try {
                                    new DataOutputStream(((Socket) clientColl.get(key)).getOutputStream()).writeUTF(ID + ": LEFT CHAT!");
                                } catch (Exception ex) {
                                    clientColl.remove(key);
                                    msgBox.append(key + ": removed!");
                                    new PrepareClientList().start();
                                }
                            }
                        }
                    } else if (i.contains("#4344554@@@@@67667@@")) {
                        i = i.substring(20);
                        StringTokenizer st = new StringTokenizer(i, ":");
                        String id = st.nextToken();
                        i = st.nextToken();
                        try {
                            new DataOutputStream(((Socket) clientColl.get(id)).getOutputStream()).writeUTF("< " + ID + " to " + id + " > " + i);
                        } catch (Exception ex) {
                            clientColl.remove(id);
                            msgBox.append(id + ": removed!");
                            new PrepareClientList().start();
                        }
                    } else {
                        Set k = clientColl.keySet();
                        Iterator itr = k.iterator();
                        while (itr.hasNext()) {
                            String key = (String) itr.next();
                            if (!key.equalsIgnoreCase(ID)) {
                                try {
                                    new DataOutputStream(((Socket) clientColl.get(key)).getOutputStream()).writeUTF("< " + ID + " to All > " + i);
                                } catch (Exception ex) {
                                    clientColl.remove(key);
                                    msgBox.append(key + ": removed!");
                                    new PrepareClientList().start();
                                }
                            }
                        }
                    }
                    //--------------------



                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        }
    }

    /**
     * evaluation of players's answers
     * @param idClient
     * @param response
     */
    private void evalResponse(String idClient, String response) {
        if (entry.getData()[Integer.valueOf(response) - 1].equals(answer)) {
            System.out.println("Great !!!!!");
            addPointsToClient(idClient);

            // check if condition of ending game is true

            if(listClientPoints.get(idClient) >= 100) {
                isAndedGame = true;
                String messageWinner = "\n*******************************\n" +
                        "\nCongratulation!\n" +
                        "\nPlayer " + idClient + " wins!   ********\n\n" +
                        "Game Over." +
                        "\n*******************************\n";
                msgBox.append(messageWinner);
                msgBox.setCaretPosition(msgBox.getDocument().getLength());


                //show message winner in all players windows
                Set k=clientColl.keySet();
                Iterator itr=k.iterator();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    try {
                        new DataOutputStream(((Socket) clientColl.get(key)).getOutputStream()).writeUTF(messageWinner);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                pickClient();
            }

            //

        } else {
            System.out.println("Wrong answer!");
            if (finishedAnswering()) {
                if(mode.equals("f"))
                    sendQuestion("All");
                else
                    sendQuestion(getHigherScoreClient());
            }

        }
    }

    /**
     * method to receive answers from players
     * @param s
     * @param idClient
     */
    private String receiveResponse(Socket s, String idClient) {
        System.out.println("Player " + idClient + " answers.");
        answeringClients.put(idClient, true);
        String message = "";
        try {
            message = new DataInputStream(s.getInputStream()).readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgBox.append("\nPlayer " + idClient + " answers.");
        msgBox.setCaretPosition(msgBox.getDocument().getLength());
        return message;
    }

    /**
     * method to help checking if the server may launch another question when all participants
     * answered with wrong answers
     * @param idClient
     * @return
     */
    private boolean allAnswering(String idClient) {
        boolean flag = true;
        if(idClient.equalsIgnoreCase("all")) {

            for(Map.Entry<String, Boolean> entry: answeringClients.entrySet()) {
                flag = flag && answeringClients.get(entry.getKey()) ;
            }
        } else {
            flag = answeringClients.get(idClient);
        }
        return flag;
    }

    /**
     * method to check if client(s) are answering
     * it cooperate with previous method.
     * @return
     */
    private boolean finishedAnswering() {

        boolean finished = true;

        if(mode.equals("f")) {
            Set keys = answeringClients.keySet();
            Iterator itr=keys.iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if(!answeringClients.get(key))
                    finished = false;
            }
        }
        else {
            finished = allAnswering(getHigherScoreClient());
        }

        return finished;

    }

    /**
     * method to add points (+20) to client where ID = id
     * will called whenevr client answered with good answer
     * @param id
     */
    private void addPointsToClient(String id) {
        String idClient;
        listClientPoints.put(id, listClientPoints.get(id) + 20);
    }

    /**
     * inner class to prepare list of clients threads
     */
    class PrepareClientList extends Thread {

        public void run(){
            try {
                String ids="";
                Set k=clientColl.keySet();
                Iterator itr=k.iterator();
                while(itr.hasNext()){
                    String key=(String)itr.next();
                    ids+=key+",";
                }
                if(ids.length()!=0)
                    ids=ids.substring(0, ids.length()-1);

                itr=k.iterator();
                while(itr.hasNext()){
                    String key=(String)itr.next();
                    try{
                        new DataOutputStream(((Socket)clientColl.get(key)).getOutputStream()).writeUTF(":;.,/="+ids);
                    } catch (Exception ex) {
                        clientColl.remove(key);
                        msgBox.append(key+": removed!");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        sStatus = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msgBox = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Server Status:");

        sStatus.setFont(new java.awt.Font("Tahoma", 3, 14)); // NOI18N
        sStatus.setText("--------------------------------");

        msgBox.setEditable(false);
        msgBox.setColumns(20);
        msgBox.setRows(5);
        jScrollPane1.setViewportView(msgBox);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jScrollPane1)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(sStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(116, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(sStatus))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
        );

        pack();
    }

    /**
     * method to close server socket
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        }

         */

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MyServer().setVisible(true);
            }
        });
    }

    /**
     * method starting the quizz game
     */
    private void startGame() {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the mode of managing clients: \n'f' for first in first receive question\n" +
                "'h' for hiegher score receive questions.");
        mode = sc.nextLine();

        String welcomeMessage = "Welcome to Quiz Sockets App\n" +
                "the player having 100 points first will win!";
        System.out.println(welcomeMessage);
        msgBox.append(welcomeMessage);

        Set k=clientColl.keySet();
        Iterator itr=k.iterator();


        while (itr.hasNext()) {
            String key = (String) itr.next();

            listClientPoints.put(key, 0);
        }

 //*/

        isStartedGame = true;
        System.out.println("start game!");
        msgBox.append("start game! \n");
        quiz = Quiz.createSampleQuiz();
        if(mode.equalsIgnoreCase("h"))
            mode = "h";
        else mode = "f";

        sendQuestion("All");

        for(Map.Entry<String, Boolean> entry: answeringClients.entrySet()) {
            answeringClients.put(entry.getKey(), false);
        }

    }

    /**
     * method to send question to destination
     * @param destination maybe any idClient or "all"
     */
    private void sendQuestion(String destination) {
        showStats();

        entry = pickRandomlyQuestion();
        answer = entry.getData()[0];

        shuffleArray(entry);

        sendQuestion(entry, destination);
        isWaitingAnswer = true;

    }

    /**
     * Show statistics for all clients (players)
     */
    private void showStats() {
        String stats = "\nStats:          -----------------------------";

        for(Map.Entry<String, Integer> entry: listClientPoints.entrySet()) {
            stats += "\n# " + entry.getKey() + " : " + entry.getValue() + " pts";
        }
        stats += "\n-----------------------------------------";

        msgBox.append(stats);
        msgBox.setCaretPosition(msgBox.getDocument().getLength());


        Set k=clientColl.keySet();
        Iterator itr=k.iterator();


        while (itr.hasNext()) {
            String key = (String) itr.next();

            answeringClients.put(key, false);
            try {
                new DataOutputStream(((Socket) clientColl.get(key)).getOutputStream()).writeUTF(stats);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        System.out.println(stats);

    }

    /**
     * method to pick the right client to answer
     * all in case mode = "f"
     * client with higher score else.
     */

    private void pickClient() {
        if(mode.equals("f"))
            sendQuestion("All");
        else {
            sendQuestion(getHigherScoreClient());
        }
    }

    /**
     * method to send entry question (question with good answer with three falsy answers)
     * to destination
     **/
    private void sendQuestion(Entry entry, String destination) {

        try {
            String message = buildQuestion(entry);

            if(destination.equalsIgnoreCase("all")) {

        Set k=clientColl.keySet();
        Iterator itr=k.iterator();


                while (itr.hasNext()) {
                    String key = (String) itr.next();

                    answeringClients.put(key, false);
                    new DataOutputStream(((Socket) clientColl.get(key)).getOutputStream()).writeUTF(message);

                }
                //*
            } else {

                answeringClients.put(destination, false);
                new DataOutputStream(((Socket) clientColl.get(destination)).getOutputStream()).writeUTF(message);
            }
            //*/
        } catch (Exception ex) {

        }


    }

    /**
     * method to build text representation of entry
     * will send later to all participants
     * @param entry
     * @return
     */
    private String buildQuestion(Entry entry) {
        String question = entry.getQuestion() + "\n";
        for (int i = 0; i < 4; i++) {
            question += String.valueOf(i+1) + ". " + entry.getData()[i] + "\n";
        }
        return question;
    }

    /**
     * random picking of questions(entries)
     * @return
     */
    private Entry pickRandomlyQuestion() {
        int index = -1;
        boolean found = false;
        while(!found) {
            Random rand = new Random();
            index = rand.nextInt((quiz.entries.size()));
            if(!pickedEntries.contains(index)) {
                found = true;
                pickedEntries.add(index);
            }
        }

        return quiz.entries.get(index);
    }

    /**
     * method helper to randomly shffle the questions inside entries
     * @param entry
     */
    static void shuffleArray(Entry entry)  {
        String[] ar = entry.getData();
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)  {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        entry.setData(ar);
    }

    /**
     * method returns the id of client with higher score
     * for higher score access algorithme
     * @return
     */
    private String getHigherScoreClient() {
        int max = -1;
        String idMax = null;
        for(Map.Entry<String, Integer> entry: listClientPoints.entrySet()) {
            if (entry.getValue() > max) {
                idMax = entry.getKey();
                max = entry.getValue();
            }
        }
        if(max <= 0)
            idMax = "all";
        return idMax;
    }
}