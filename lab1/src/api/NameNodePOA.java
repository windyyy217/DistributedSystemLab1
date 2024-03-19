package api;


/**
* api/NameNodePOA.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��api.idl
* 2023��11��13�� ����һ ����12ʱ17��08�� CST
*/

public abstract class NameNodePOA extends org.omg.PortableServer.Servant
 implements api.NameNodeOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("open", new java.lang.Integer (0));
    _methods.put ("close", new java.lang.Integer (1));
    _methods.put ("getLocations", new java.lang.Integer (2));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // api/NameNode/open
       {
         String filepath = in.read_string ();
         int mode = in.read_long ();
         String $result = null;
         $result = this.open (filepath, mode);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // api/NameNode/close
       {
         String fileInfo = in.read_string ();
         this.close (fileInfo);
         out = $rh.createReply();
         break;
       }

       case 2:  // api/NameNode/getLocations
       {
         long id = in.read_longlong ();
         int size = in.read_long ();
         String $result = null;
         $result = this.getLocations (id, size);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:api/NameNode:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public NameNode _this() 
  {
    return NameNodeHelper.narrow(
    super._this_object());
  }

  public NameNode _this(org.omg.CORBA.ORB orb) 
  {
    return NameNodeHelper.narrow(
    super._this_object(orb));
  }


} // class NameNodePOA
