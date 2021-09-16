import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.lang.reflect.Member;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.lang.*;
public class ServerWorker extends Thread {
    static Server server;
    private OutputStream outputStream;
    private Socket clientSocket;
    public String[] tokens;
    private HashSet<String>topicSet=new HashSet<>();
    private String login=null;
    public ServerWorker(Socket clientSocket){
        this.clientSocket=clientSocket;
    }

    @Override
    public void run() {
        try{
            handleClientSocket();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void handleClientSocket() throws IOException,InterruptedException{
        InputStream inputStream=clientSocket.getInputStream();
        this.outputStream=clientSocket.getOutputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line=reader.readLine())!=null){
            tokens= line.split("-");
            if(tokens!=null&&tokens.length>0){
                String cmd=tokens[0];
                if("bye".equalsIgnoreCase(cmd)||"logoff".equalsIgnoreCase(cmd)){
                    handlelogoff();
                } else if ("login".equalsIgnoreCase(cmd)) {

                    handlelogin(outputStream,tokens);
                }else if("msg".equalsIgnoreCase(cmd)){
                    handleMsg(tokens);
                }else if("join".equalsIgnoreCase(cmd)){
                    handleJoin(tokens);
                }else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }
                else{
                    String msg="Unknown"+cmd+"\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if(tokens.length>1){
            String topic=tokens[1];
            topicSet.remove(topic);
        }
    }


    private void handleJoin(String[] tokens) {
        if(tokens.length>1){
            String topic=tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleMsg(String[] tokenmsg)throws IOException {
        String sendTo=tokens[1];
        String body=tokens[2];
        boolean isTopic=sendTo.charAt(0)=='#';
        List<ServerWorker>workerList=server.getWorkerList();
        for(ServerWorker worker:workerList) {
            if (worker.isMemberOfTopic(sendTo)) {
                String outMsg ="msg"+login+"---"+body+"\n";
                worker.send(outMsg);

            } else if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "MSg" + login + " " + body+"\n";
                worker.send(outMsg);
            }
        }
    }

    private boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    public String getLogin(){
        return login;
    }
    private void handlelogoff() throws IOException{
        server.removeworker(this);
        List<ServerWorker>workerList=server.getWorkerList();
        String offineMsg="Offline:   "+login+"\n";
        for(ServerWorker worker:workerList){
            if(!login.equals(worker.getLogin()));{
                worker.send(offineMsg);
            }
        }
        clientSocket.close();

    }
    private void handlelogin(OutputStream outputStream,String[] tokens)throws IOException{
        if(tokens.length==3){
            String login=tokens[1];
            String passwd=tokens[2];
            if((login.equalsIgnoreCase("guest")&&passwd.equalsIgnoreCase("guest"))||
                    (login.equalsIgnoreCase("mani")&&passwd.equalsIgnoreCase("mani"))||(login.equalsIgnoreCase("meena")&&passwd.equalsIgnoreCase("meena"))||(login.equalsIgnoreCase("sai")&&passwd.equalsIgnoreCase("sir"))){
                outputStream.write("ok login\n".getBytes());
                this.login=login;
                System.out.println("User Logged in sucessfully:   "+login);
                List<ServerWorker> workerList=server.getWorkerList();
                for(ServerWorker worker:workerList){
                    if(worker.getLogin()!=null){
                        if(!login.equals(worker.getLogin())){
                            String msg1="online:   "+worker.getLogin()+"\n";
                            send(msg1);
                        }
                    }
                }
                String onlinemsg="online:   "+login+"\n";
                for(ServerWorker worker:workerList){
                    if(!login.equals(worker.getLogin()))
                        worker.send(onlinemsg);
                    else{
                        String msg="Error Login"+"\n";
                        outputStream.write(msg.getBytes());
                    }

                }

            }

        }

    }
    private void send(String msg)throws IOException{
        outputStream.write(msg.getBytes());
    }



}
