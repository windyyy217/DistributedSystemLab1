package client;

import impl.ClientImpl;

public class ClientLauncher {
    static ClientImpl client = new ClientImpl();

    public static void main(String[] args) {
        client.run(args);
    }
}
