package test;

import api.DataNode;
import api.DataNodeHelper;
import api.NameNodeHelper;
import impl.ClientImpl;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import server.DataNodeLauncher;
import server.NameNodeLauncher;
import utils.FileSystem;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import impl.ClientImpl;

public class ClientTest {
    static ClientImpl clientImpl;
    @Before
    public void setUp() throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound, InterruptedException {

        String[] args = new String[]{"-ORBInitialHost", "127.0.0.1", "-ORBInitialPort", "1050"};
        new Thread(() -> DataNodeLauncher.main(args)).start();
        new Thread(() -> NameNodeLauncher.main(args)).start();
        // 确保启动
        Thread.sleep(4000);

        clientImpl = new ClientImpl();

        Properties properties = new Properties();
        properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
        properties.put("org.omg.CORBA.ORBInitialPort", "1050");
        ORB orb = ORB.init(args, properties);
        // 获取到 Naming 上下文
        Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        // 获取 NameNode 对象
        String nameNodeName = "NameNode";
        clientImpl.namenode = NameNodeHelper.narrow(ncRef.resolve_str(nameNodeName));
        // 获取 DataNode1 对象
        String dataNode1Name = "DataNode1";
        DataNode dataNode1 = DataNodeHelper.narrow(ncRef.resolve_str(dataNode1Name));
        clientImpl.dataNode.add(dataNode1);
        // 获取 DataNode2 对象
        String dataNode2Name = "DataNode2";
        DataNode dataNode2 = DataNodeHelper.narrow(ncRef.resolve_str(dataNode2Name));
        clientImpl.dataNode.add(dataNode2);
    }


    @Test
    public void testWriteRead(){
        String filename = FileSystem.newFilename();
        int fd = clientImpl.open(filename, 0b11);
        clientImpl.append(fd,"hello".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(clientImpl.read(fd),"hello".getBytes(StandardCharsets.UTF_8));
        clientImpl.append(fd," world".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(clientImpl.read(fd),"hello world".getBytes(StandardCharsets.UTF_8));
        clientImpl.close(fd);
    }

    @Test
    public void testWriteFail(){
        String filename = FileSystem.newFilename();
        int fd = clientImpl.open(filename,0b01);
        clientImpl.append(fd,"Lala-land".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(clientImpl.read(fd),"".getBytes(StandardCharsets.UTF_8));
        clientImpl.close(fd);
    }

    @Test
    public void testReadFail(){
        String filename = FileSystem.newFilename();
        int fd = clientImpl.open(filename,0b10);
        assertNull(clientImpl.read(fd));
        clientImpl.close(fd);
    }
}
