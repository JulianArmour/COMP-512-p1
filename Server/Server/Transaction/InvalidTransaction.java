package Server.Transaction;

public class InvalidTransaction extends Exception{
  private final int xid;

  public InvalidTransaction(int xid, String msg) {

    super("The transaction " + xid + " is invalid:" + msg);
    this.xid = xid;
  }

  int getXId()
  {
    return xid;
  }
}
