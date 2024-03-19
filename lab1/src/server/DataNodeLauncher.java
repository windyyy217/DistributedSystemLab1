package server;

import api.DataNodeHelper;
import impl.DataNodeImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import api.DataNode;

import java.util.Properties;

public class DataNodeLauncher {

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
            properties.put("org.omg.CORBA.ORBInitialPort", "1050"); // 设置正确的端口号

            ORB orb = ORB.init(args, properties);

            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // 第一个 DataNode
            DataNodeImpl dataNodeImpl1 = new DataNodeImpl();
            org.omg.CORBA.Object ref1 = rootpoa.servant_to_reference(dataNodeImpl1);
            DataNode href1 = DataNodeHelper.narrow(ref1);

            // 第二个 DataNode
            DataNodeImpl dataNodeImpl2 = new DataNodeImpl();
            org.omg.CORBA.Object ref2 = rootpoa.servant_to_reference(dataNodeImpl2);
            DataNode href2 = DataNodeHelper.narrow(ref2);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // 绑定第一个 DataNode
            String name1 = "DataNode1";
            NameComponent[] path1 = ncRef.to_name(name1);
            ncRef.rebind(path1, href1);

            // 绑定第二个 DataNode
            String name2 = "DataNode2";
            NameComponent[] path2 = ncRef.to_name(name2);
            ncRef.rebind(path2, href2);

            System.out.println("DataNodes ready and waiting...");
            orb.run(); // 等待客户端调用
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DataNodes Exiting...");
    }
}