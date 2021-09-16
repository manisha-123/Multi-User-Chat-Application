import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int severPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.severPort = serverPort;
    }

    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    public void startWorking() {
        try {
            ServerWorker.server = this;
            ServerSocket serverSocket = new ServerSocket(severPort);
            while (true) {
                System.out.println("About to accept client connection");
                Socket clientScoket = serverSocket.accept();
                System.out.println("Accepted connection from" + clientScoket);
                ServerWorker worker = new ServerWorker(clientScoket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void removeworker(ServerWorker serverWorker){
       workerList.remove(serverWorker);
    }


}