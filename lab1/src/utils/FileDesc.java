package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Field;
import java.util.ArrayList;


//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
public class FileDesc {
    long id = 0;//分配唯一的id值
    String name;//文件名
//    int fd;//内存标识符
    ArrayList<Integer> locations=new ArrayList<>();//文件位置
    int fileSize;
    int status; // 打开1  关闭2
    ArrayList<Integer> blockIds = new ArrayList<>();//存储块id
    int mode;// r1 w2 rw3
    long accessTime;
    long modifyTime;
    long createTime;

    public ArrayList<Integer> getBlockIds() {
        return blockIds;
    }

    public void addBlockIds(int blockId){
        blockIds.add(blockId);
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setId(long id) {
        this.id = id;
    }
    public ArrayList<Integer> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Integer> locations) {
        this.locations = locations;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public FileDesc(long gid,String name) {//构造函数，用于初始化FileDesc实例
        this.id = gid;
        this.name = name;
    }
    // 将Java对象转换为JSON字符串
    @Override
    public String toString() {//可以返回字符串，这样在IDL中更容易编写
        StringBuilder sb = new StringBuilder();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public static FileDesc fromString(String str){
        if (str == "")
            return null;
        Gson gson = new Gson();
        FileDesc fileInfo = gson.fromJson(str, FileDesc.class);
        return fileInfo;
    }
}