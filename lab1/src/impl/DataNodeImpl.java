package impl;
//TODO: your implementation
import api.DataNodePOA;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class DataNodeImpl extends DataNodePOA {

    class Block{
        int id;
        byte[] data = new byte[0]; // 内部类Block，表示数据块，包含一个ID和数据字节数组
    }
    ArrayList<Block> blocks = new ArrayList<>(0);
    int size = 0;

    @Override
    public byte[] read(int block_id) {
        System.out.println("DataNode:read");
        Block block= getBlockById(block_id);
        if (block==null)
            return "".getBytes();// 打印一条消息，然后调用getBlockById方法获取指定ID的数据块，如果数据块不存在则返回一个空字节数组
        return block.data;
    }

    private Block getBlockById(int id) {//遍历数据块列表，找到指定ID的数据块并返回，如果找不到则返回null
        for (Block block : blocks) {
            if (block.id == id) {
                return block;
            }
        }
        return null;
    }

    @Override
    public void append(int block_id, byte[] bytedata) {//实现DataNodePOA接口中的append方法，用于向数据块追加数据
        System.out.println("DataNode:write");
        Block block;
        if ((block = getBlockById(block_id))==null){//调用getBlockById方法查找指定ID的数据块
            block = new Block();
            block.id = block_id;
            blocks.add(block);//创建一个新的数据块并加入列表
        }
        byte[] newbytedata = new byte[block.data.length+bytedata.length];//创建一个新的字节数组然后将新数组赋给数据块
        System.arraycopy(block.data, 0, newbytedata, 0, block.data.length);//将原数据块的内容和要追加的数据拷贝到新数组中
        System.arraycopy(bytedata, 0, newbytedata, block.data.length, bytedata.length);
        block.data = newbytedata;
        try {
            FileOutputStream fos = new FileOutputStream("block"+block_id+".txt");
            fos.write(newbytedata);//将数据块内容写入名为"blockX.txt"的文件，其中X为数据块的ID
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int randomBlockId() {//实现DataNodePOA接口中的randomBlockId方法，用于生成随机数据块ID
        int min = 0;
        int max = 10;
        Random random = new Random();
        int randomInRange = random.nextInt(max - min + 1) + min;
        return randomInRange;
    }
}