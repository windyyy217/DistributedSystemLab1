package impl;
//TODO: your implementation
import api.*;
import utils.FileDesc;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;


public class ClientImpl implements Client {

    public NameNode namenode;
    public ArrayList<DataNode> dataNode = new ArrayList<>();//客户端维护一个NameNode对象和一组DataNode对象的列表
    int globalBlockId = 0;//全局块ID计数器
    ArrayList<Integer> writeByte=new ArrayList<>();
    int temp=0;
    int MAX_DATA_NODE=2;
    class CFile{
        FileDesc fileDesc;//文件信息
        int fd;//文件内存标识符
    }

    ArrayList<CFile> files = new ArrayList<>();
    static int fd;
    public static int getFd() {
        return fd;
    }
    CFile getFileByFd(int fd){//根据文件内存标识符找到文件信息
        for (CFile file : files) {
            if (file.fd == fd)
                return file;
        }
        return null;
    }

    @Override
    public int open(String filepath, int mode) {//Client接口打开新的文件
        for (CFile existingFile : files) {
            if (existingFile.fileDesc.getName().equals(filepath)&&existingFile.fileDesc.getStatus()==1) {
                System.out.println("File " + filepath + " is already open.");
                return existingFile.fd;
            }
        }
        FileDesc fileDesc = FileDesc.fromString(namenode.open(filepath,mode));
        CFile newFile = new CFile();
        newFile.fileDesc=fileDesc;
        newFile.fd = temp++;
        files.add(newFile);
        return newFile.fd;
    }
    public ArrayList<Integer> jsonToList(String json) {

        Gson gson = new Gson();
        ArrayList<Integer> persons = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {
        }.getType());//对于不是类的情况，用这个参数给出

        return persons;
    }
    @Override
    public void close(int fd) {
        CFile file = getFileByFd(fd);
        files.remove(file);
        namenode.close(file.fileDesc.toString());
        file.fileDesc.setStatus(0);
    }

    public void exit(){
        while(files.size()!=0){
            CFile file=files.get(0);
            close(file.fd);
        }
    }

    // 创建需要先打开，空文件没有blockid
    @Override
    public void append(int fd, byte[] bytes) {
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        int size= bytes.length/(4*1024)+1;// 计算需要多少个块来存储传入的字节数组（每块4KB）
        byte[] temp=bytes;
        System.out.println("size"+size);
        int s=0;
        int startIndex=0;
        int endIndex=-1;
        int allLength=bytes.length;
        ArrayList<Integer>locations=fileDesc.getLocations();
        //追加的内容如果是新的文件内容 那么直接对于新的location直接分配blockid
        //如果追加的内容其实是已经有的文件那么  对于得到的原来的location中是否有没有满的block
        // 如果block没满，那就补全，如果block满，那就新location分配blockid
        for (int i=0;i<size;i++){
            int blockId;
            int loc;
            byte[] writeThings;
            if(writeByte.size()==0||fileDesc.getBlockIds().size()==0||
                    writeByte.get(fileDesc.getBlockIds().get(fileDesc.getBlockIds().size() - 1))==4096){
                Random random = new Random();
                loc= random.nextInt(2);
                locations.add(loc);
                blockId=globalBlockId++;
                s++;
                if(allLength>4096){
                    writeByte.add(blockId,4096);
                    endIndex=startIndex+4096;
                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
                    allLength-=4096;
                }
                else{
                    writeByte.add(blockId,allLength);
                    endIndex=startIndex+allLength;
                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
                    allLength=0;
                }
                fileDesc.addBlockIds(blockId);
            }
            else{
                loc= fileDesc.getLocations().get(fileDesc.getLocations().size() - 1);
                blockId = fileDesc.getBlockIds().get(fileDesc.getBlockIds().size() - 1);
                if(allLength>4096-writeByte.get(blockId)){
                    //bytes更长
                    writeByte.set(blockId,4096);
                    endIndex=startIndex+4096-writeByte.get(blockId);
                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
                    allLength-=4096-writeByte.get(blockId);
                }
                else{//block 能写完
                    writeByte.set(blockId,writeByte.get(blockId)+allLength);
                    endIndex=startIndex+allLength;
                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
                    allLength=0;
                }
            }
//            if(writeByte.get(fileDesc.getBlockIds().get(fileDesc.getBlockIds().size() - 1))<4096){
//                //旧的文件,且最后一个block没写满
//                loc= fileDesc.getLocations().get(fileDesc.getLocations().size() - 1);
//                blockId = fileDesc.getBlockIds().get(fileDesc.getBlockIds().size() - 1);
//                if(allLength>4096-writeByte.get(blockId)){
//                    //bytes更长
//                    writeByte.set(blockId,4096);
//                    endIndex=startIndex+4096-writeByte.get(blockId);
//                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
//                    allLength-=4096-writeByte.get(blockId);
//                }
//                else{//block 能写完
//                    writeByte.set(blockId,writeByte.get(blockId)+allLength);
//                    endIndex=startIndex+allLength;
//                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
//                    allLength=0;
//                }
//            }
//            else{//需要新建location和分配blockid
//                Random random = new Random();
//                loc= random.nextInt(2);
//                locations.add(loc);
//                blockId=globalBlockId++;
//                if(allLength>4096){
//                    writeByte.set(blockId,4096);
//                    endIndex=startIndex+4096;
//                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
//                    allLength-=4096;
//                }
//                else{
//                    writeByte.set(blockId,allLength);
//                    endIndex=startIndex+allLength;
//                    writeThings=Arrays.copyOfRange(temp, startIndex, endIndex);
//                    allLength=0;
//                }
//            }
            System.out.println("client:append:block id:"+blockId);
            if (fileDesc.getMode() == 2 || fileDesc.getMode() == 3) {// 如果文件有写入权限（2表示写权限，3表示读写权限）
                dataNode.get(loc).append(blockId, writeThings);
            }
            else {
                System.out.println("client: append: no write authority!");//没有写入权限
            }
            startIndex=endIndex;
        }
        fileDesc.setLocations(locations);
        fileDesc.setFileSize(fileDesc.getFileSize()+s);
    }

    @Override
    public byte[] read(int fd) {
        StringBuilder sb = new StringBuilder();
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        if(fileDesc==null){System.out.println("client: read: file not exist"); return null;}
        System.out.println("client: read: mode:"+fileDesc.getMode());
        byte[] res = new byte[0];
        if (fileDesc.getMode() == 1 || fileDesc.getMode() == 3){
            if (fileDesc.getLocations().size()==0){
                System.out.println("no location");
                return "".getBytes(StandardCharsets.UTF_8);
            }
            for (int i=0;i<fileDesc.getFileSize();i++){
                int blockId = fileDesc.getBlockIds().get(i);
                System.out.println("client read:block id:"+blockId);

                byte[] array2 = dataNode.get(fileDesc.getLocations().get(i)).read(blockId);
                byte[] result = new byte[res.length + array2.length];
                System.arraycopy(res, 0, result, 0, res.length);
                System.arraycopy(array2, 0, result, res.length, array2.length);

                res = result;
            }
            return res;
        }
        else return null;
    }

    public static int parse_command(Scanner input){
        String command = "";
        if (input.hasNext()){
            command = input.next();
        }
        if (Objects.equals(command, "open"))
            return 1;
        else if (Objects.equals(command, "read")) {
            return 2;
        } else if (Objects.equals(command, "append")) {
            return 3;
        } else if (Objects.equals(command, "close")) {
            return 4;
        } else if (Objects.equals(command, "exit")) {
            return 5;
        }
        return 0;

    }

    public static int parse_mode(String mode){
        int res = 0;

        if (Objects.equals(mode, "r"))
            return 1;
        else if (Objects.equals(mode, "w")) {
            return 2;
        } else if (Objects.equals(mode, "rw")) {
            return 3;
        }
        return 0;
    }

    public void run(String[] args) {
        try {
            // 初始化 ORB
//            ORB orb = ORB.init(args, null);
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");
            ORB orb = ORB.init(args, properties);
            // 获取到 Naming 上下文
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // 获取 NameNode 对象
            String nameNodeName = "NameNode";
            namenode = NameNodeHelper.narrow(ncRef.resolve_str(nameNodeName));

            String dataNode1Name = "DataNode1";
            DataNode dataNode1 = DataNodeHelper.narrow(ncRef.resolve_str(dataNode1Name));
            dataNode.add(dataNode1);
            String dataNode2Name = "DataNode2";
            DataNode dataNode2 = DataNodeHelper.narrow(ncRef.resolve_str(dataNode2Name));
            dataNode.add(dataNode2);

//            for(int dataNodeId=0;dataNodeId<MAX_DATA_NODE;dataNodeId++){
//                dataNode.add(DataNodeHelper.narrow(ncRef.resolve_str("DataNode"+dataNodeId)));
//                System.out.println("DataNode"+dataNodeId+" is obtained.");
//            }
            Scanner input = new Scanner(System.in);
            boolean m = true;   //用于while循环
            int n;              //switch判断
            System.out.println("\t\t\t\t\t\t\t\t分布式文件系统\t\t\t\t\t\t\t\t\t\t\t\t");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
            System.out.println("\t\topen\t\tread\t\tappend\t\tclose\t\texit");
            while (m) {
                System.out.print(">>");
                n = parse_command(input);
                switch (n) {
                    case 1: // open
                        String fileName = input.next();
                        int mode = parse_mode(input.next());
                        input.nextLine();
                        fd = this.open(fileName, mode);
                        System.out.println("INFO: fd="+fd);
                        break;
                    case 2:
                        fd = input.nextInt();
                        input.nextLine();
                        System.out.println("read "+fd);
                        byte[] res = this.read(fd);
                        if (res==null){
                            System.out.println("INFO: READ not allowed");
                        } else {
                            String resstr = new String(res);
                            System.out.println(resstr);
                        }
                        break;
                    case 3:
                        fd = input.nextInt();
                        String content = input.nextLine().trim();
                        this.append(fd,content.getBytes(StandardCharsets.UTF_8));
                        System.out.println("INFO: write done");
                        break;
                    case 4:
                        fd = input.nextInt();
//                        input.nextLine();
                        this.close(fd);
                        System.out.println("INFO: fd "+fd+" closed");
                        break;
                    case 5:
                        m = false;
                        this.exit();
                        System.out.println("INFO: bye");
                        break;
                    default:
                        System.out.println("输入错误，请重试！");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}