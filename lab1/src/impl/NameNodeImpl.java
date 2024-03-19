package impl;
//TODO: your implementation
import api.NameNodePOA;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import utils.FileDesc;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class NameNodeImpl extends NameNodePOA {
    ArrayList<FileDesc> allFileInfo = initFileDesc();
    long globalFileDescId=0;
    // 初始化时把硬盘上的FSImage读取到内存
    public ArrayList<FileDesc> initFileDesc(){
        try {
            ArrayList<FileDesc> allFileInfo = new ArrayList<>();
            File file = new File("FSImage.json");// 创建一个文件对象
            FileReader fr = new FileReader(file);// 创建一个文件读取器以读取文件内容
            StringBuilder str = new StringBuilder();// 创建一个 StringBuilder 以存储文件内容
            int c = 0;
            while((c = fr.read()) != -1){//读取文件内容并将其附加到 StringBuilder 中
                str.append((char) c);
            }
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonElements = jsonParser.parse(String.valueOf(str)).getAsJsonArray();//获取JsonArray对象
            Gson gson=new Gson();//将 JSON 数据转换为 Java 对象
            for (JsonElement fileInfo : jsonElements){
                FileDesc fileInfo1 = gson.fromJson(fileInfo,FileDesc.class);// 使用 Gson 将 JSON 元素转换为 FileDesc 对象，并添加到 allFileInfo 列表中
                allFileInfo.add(fileInfo1);
            }
            System.out.println("NameNode:initfile,current file num:"+allFileInfo.size());
            return allFileInfo;
        }catch (IOException e){
            System.out.println("NameNode:IO exception");
            return new ArrayList<>();
        }

    }
    @Override
    public String open(String path, int mode) {
        System.out.println("NameNode: fileinfo size:"+allFileInfo.size());
        for (FileDesc fileInfo : allFileInfo) {// 遍历 allFileInfo 列表中的每个 FileDesc 对象
            if (Objects.equals(fileInfo.getName(), path)){//有没有已经打开的文件
//                System.out.println("NameNode: open file:"+fileInfo.toString());
                if(fileInfo.getStatus()==1){
                    if(fileInfo.getMode()==1&&mode==1){
                        return fileInfo.toString();
                    }
                    else{
                        System.out.println("NameNode: open: file can not be opened");
                        return "";
                    }
                }
                fileInfo.setMode(mode);
                fileInfo.setStatus(1);
                fileInfo.setAccessTime(System.currentTimeMillis());
                System.out.println("NameNode: open: "+fileInfo.toString());
                return fileInfo.toString();
            }
        }
        System.out.println("NameNode: create file");//需要新建文件
        FileDesc newFile = new FileDesc(globalFileDescId++,path);
        newFile.setMode(mode);
        newFile.setAccessTime(System.currentTimeMillis());
        newFile.setCreateTime(System.currentTimeMillis());
        newFile.setStatus(1);
        allFileInfo.add(newFile);
        return newFile.toString();
    }

    @Override
    public void close(String fileInfo) {
        FileDesc fileDesc = FileDesc.fromString(fileInfo);
        updateFileDesc(fileInfo,fileDesc.getId());
        FileDesc fileDesc1 = getFileDescById(fileDesc.getId());
        fileDesc1.setModifyTime(System.currentTimeMillis());
        fileDesc1.setStatus(0);
        try {
            FileOutputStream fileoutput = new FileOutputStream("FSImage.json");
            fileoutput.write(listToJson(allFileInfo).getBytes(StandardCharsets.UTF_8));
            fileoutput.flush();//将缓冲区中的所有数据写入到文件
            fileoutput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("NameNode:close:"+fileDesc1.toString());
    }
    FileDesc getFileDescById(long id){
        for (FileDesc fileDesc : allFileInfo) {
            if (id == fileDesc.getId()){
                return fileDesc;
            }
        }
        return null;
    }

    public String getLocations(long id,int size){
        FileDesc fileDesc = getFileDescById(id);
        if (fileDesc.getLocations().isEmpty()){
            ArrayList<Integer> locations = new ArrayList<>();
            for (int i=0;i<size;i++){// 随机生成指定数量的位置信息，这些位置是0或1
                Random random = new Random();
                int randomNumber= random.nextInt(2);
                locations.add(randomNumber);
            }
            System.out.println("NameNode:create location:"+locations.toString());
            return locations.toString();
        } else
            return fileDesc.getLocations().toString();
    }

    public void updateFileDesc(String fileDescStr,long id){
        for (int i = 0; i < allFileInfo.size(); i++) {
            if (allFileInfo.get(i).getId()==id){
                allFileInfo.set(i, FileDesc.fromString(fileDescStr));
            }
        }
    }

    public static String listToJson(ArrayList<FileDesc> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        System.out.println("NameNode:json"+json);
        return json;
    }
}