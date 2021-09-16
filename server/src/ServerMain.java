

public class ServerMain {
    public static void main(String[] args){
        int port = 8000;
        Server server = new Server(port);
        server.startWorking();
    }

}

