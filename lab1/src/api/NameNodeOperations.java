package api;


/**
* api/NameNodeOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��api.idl
* 2023��11��13�� ����һ ����12ʱ17��08�� CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fileInfo);
  String getLocations (long id, int size);
} // interface NameNodeOperations
