/*ObjectWriter.java
  Valentin Tablan 21 Feb 2000
 */

package gate.util;

import java.io.*;
import java.util.*;

/**Writes an object to an PipedOutputStream wich can be connected to a
  *PipedInputStream.
  *Before writting the object it also writes it in a buffer and finds 
  *out its size so it can be reported via getSize method.
  *All read/writes occur in separate threads to avoid a deadlock. 
  */
public class ObjectWriter extends Thread{
  public ObjectWriter(Object obj) throws IOException{
    size = 0;
    Writer writer = new Writer(obj);
    InputStream is = writer.getInputStream();
    writer.start();
    boolean over = false;
    buffer = new LinkedList();

    int space = buffSize;//how much space is available in lastBuff
    int writeOffset = 0;//where to write in lastBuff
    byte lastBuff[] = new byte[buffSize];

    while (!over){
      int read = is.read(lastBuff, writeOffset, space);
      if(read == -1) {
        lastOffset = writeOffset;
        buffer.addLast(lastBuff);
        over = true;
      }else{
        space-= read;
        size+=read;
        if(space == 0){
          //no more space; we need a new buffer
          buffer.addLast(lastBuff);
          space = buffSize;
          writeOffset = 0;
          lastBuff = new byte[buffSize];
        }else{
          //current buffer not full yet
          writeOffset+=read;
        }
      }
    };//while(!over)
    outputStream = new PipedOutputStream(); //will be used to write the data
    //will be returned for objects that want to read the object
    inputStream = new PipedInputStream(outputStream);
  }

  /**
   * Returns a PipedInputStream from which the object given as parameter for
   *the constructor can be read.
   *
   * @return a PipedInputStream connected to PipedOutputStream which writes
   *the object which this ObjectWriter was built for.
   */
  public InputStream getInputStream(){
    return inputStream;
  }

  /**
   * Obtains the object size.
   *
   * @return the size of the object recieved as parameter for the constructor.
   */
  public int getSize(){
    return size;
  }
  /*Writes all the buffers to the output stream
  */
  public void run(){
    try{
      Iterator buffIter = buffer.iterator();
      while(buffIter.hasNext()){
        byte currentBuff[] = (byte[])buffIter.next();
        if(buffIter.hasNext()) {
          //is not the last buffer
          outputStream.write(currentBuff,0,buffSize);
        }else{
          //is the last buffer
//          currentBuff[lastOffset] = '\u001a';
          outputStream.write(currentBuff,0,lastOffset);
        }
      }//while(buffIter.hasNext())

      outputStream.flush();
      outputStream.close();
    }catch(IOException ioe){
      throw new RuntimeException(ioe.toString());
//      ioe.printStackTrace(System.err);
    }
  }


  //I need a thread to write the object so I can read it in an buffer
  //After that I know the size ana I can write it to the output stream
  //after I report the size.
  private class Writer extends Thread{
    public Writer(Object _obj){
      _object = _obj;
      _outputStream = new PipedOutputStream();
      try{
        _inputStream = new PipedInputStream(_outputStream);
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }
    }

    public InputStream getInputStream(){
      return _inputStream;
    }
    /**
     * Describe 'run' method here.
     *
     */
    public void run(){
      try{
        ObjectOutputStream _oos = new ObjectOutputStream(_outputStream);
        _oos.writeObject(_object);
        _oos.close();
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }
    };
    private Object _object;
    private InputStream _inputStream;
    private PipedOutputStream _outputStream;
  };



  private Object object;
  private InputStream inputStream ;
  private PipedOutputStream outputStream;
  private int size;
  private int lastOffset;
  private LinkedList buffer;
  private int buffSize = 1024;
}
