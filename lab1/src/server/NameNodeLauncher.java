package server;

import api.NameNode;
import api.NameNodeHelper;
import impl.NameNodeImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class NameNodeLauncher{
    public static void main(String[] args) {
        try{
            try {
                // 将空内容写回 JSON 文件
                FileOutputStream fileoutput = new FileOutputStream("FSImage.json");
                fileoutput.write("[]".getBytes(StandardCharsets.UTF_8));
                fileoutput.flush();
                fileoutput.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");  //指定ORB的ip地址
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");       //指定ORB的端口

            // 初始化ORB对象
            ORB orb = ORB.init(args, properties);

            // 获取到rootPOA的引用，激活POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // 创建一个接口实现对象
            NameNodeImpl nameNodeImpl = new NameNodeImpl();

            // 将服务实现对象交给POA管理，并向外暴露接口
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(nameNodeImpl);
            NameNode href = NameNodeHelper.narrow(ref);

            // Naming 上下文
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // 绑定远程对象到Naming
            NameComponent[] path = ncRef.to_name("NameNode");
            ncRef.rebind(path, href);
            System.out.println("NameNode is ready and waiting...");

            // 等待客户端调用
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}