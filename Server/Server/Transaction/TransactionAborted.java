package Server.Transaction;

public class TransactionAborted extends Exception{
  private final int xid;

  public TransactionAborted(int xid, String msg) {

    super("The transaction " + xid + " has aborted:" + msg);
    this.xid = xid;
  }

  int getXId()
  {
    return xid;
  }
}
