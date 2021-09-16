import com.sun.deploy.util.StringUtils;

import javax.print.DocFlavor;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient implements UserStatusListener,MsgListner {
    private final String servereName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    private ArrayList<UserStatusListener>userStatusListeners=new ArrayList<>();
    private ArrayList<MsgListner>msgListners=new ArrayList<>();

    public ChatClient(String servereName, int serverPort){
        this.servereName=servereName;
        this.serverPort=serverPort;
    }

    public void online(String login) {
        System.out.println("Online:  "+login);
    }
    public void offline(String login) {
        System.out.println("Offline:  "+login);

    }
    public void onMsg(String formLogin, String msgBody) {
        System.out.println("You get a message from:  "+formLogin+"-->"+msgBody);
    }

    public static void main(String[] args)throws IOException{
        ChatClient client=new ChatClient("localhost",8000);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("Online:  "+login);

            }

            @Override
            public void offline(String login) {
                System.out.println("Offline:  "+login);

            }
        });
        client.addMsgListner(new MsgListner() {
            @Override
            public void onMsg(String formLogin, String msgBody) {
                System.out.println("You get a message from:"+formLogin+"-->"+msgBody);
            }
        });

        if(!client.connect()){
            System.out.println("Connection Failed!!");

        }else{
            System.out.println("Connect Succesfully");
            if(client.login("guest","guest")||(client.login("mani","mani"))||(client.login("meena","meena"))||(client.login("sai","sir"))){
                System.out.println("Login Successfully...");
            }else
                System.out.println("Login Failed..");
        }
    }

    private void addMsgListner(MsgListner listner) {
        msgListners.add(listner);

    }
    private void removeMsgListner(MsgListner listner) {
        msgListners.remove(listner);

    }

    private boolean connect() {
        try{
            this.socket=new Socket(servereName,serverPort);
            System.out.println("Client Port is"+socket.getLocalPort());
            this.serverOut=socket.getOutputStream();
            this.serverIn=socket.getInputStream();
            this.bufferedIn=new BufferedReader(new InputStreamReader(serverIn));
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }return false;
    }

    private boolean login(String login, String passwd) throws IOException{
        String cmd="Login"+login+" "+"Password"+passwd;
        serverOut.write(cmd.getBytes());
        String response=bufferedIn.readLine();
        System.out.println("Response Line:"+response);
        if("Ok Login".equalsIgnoreCase(response)){
            startMsgReader();
            return true;
        }else return false;

    }

    private void startMsgReader() {
        Thread t=new Thread(){
            @Override
            public void run() {
                readMsgLoop();
            }
        }; t.start();
    }

    private void readMsgLoop() {
        try{
            String line;
            while ((line=bufferedIn.readLine())!=null){
                String [] tokens= line.split("-");
                if(tokens!=null&&tokens.length>0){
                    String cmd=tokens[0];
                    if("Online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    }else if("Offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    }else if("msg".equalsIgnoreCase(cmd)){
                        String[] tokenMsg=line.split("-");
                        handleMsg(tokenMsg);
                    }
                    }
                }
            }catch (Exception e){
            e.printStackTrace();
        }try{
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void handleMsg(String[] tokenMsg) {
        String login=tokenMsg[1];
        String msgBody=tokenMsg[2];
        for(MsgListner listeners:msgListners){
            listeners.onMsg(login,msgBody);
        }


    }

    private void handleOffline(String[] tokens) {
        String login=tokens[1];
        for(UserStatusListener listeners:userStatusListeners){
            listeners.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login=tokens[1];
        for(UserStatusListener listeners:userStatusListeners){

            listeners.online(login);

        }
    }




    private void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);

    }

}
