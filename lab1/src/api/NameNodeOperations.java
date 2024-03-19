package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月13日 星期一 上午12时17分08秒 CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fileInfo);
  String getLocations (long id, int size);
} // interface NameNodeOperations
