package gate;
import java.sql.SQLException;
import gate.util.*;

/**This interface should be implemented by any persistent language resource.
  */
public interface LRDBWrapper extends LanguageResource{

  /**Rolls back all the actions performed since the last beginTransaction call
  */
  public void rollback() throws SQLException;

  /**
  *Starts a transaction. All database actions should be enclosed between a
  *beginTransaction() and a commit()/rollback() call.
  **/
  public void beginTransaction() throws SQLException;

  /**Commits the reads/writes*/
  public void commit() throws SQLException;

  /**Disconnects from the Datastore object, that is returns the connection*/
  public void disconnect() throws GateException;
} 
