package gate.creole.tokeniser;

import java.util.*;

import gate.util.*;

  class DFSMState{ //extends FSMState{
    public DFSMState(DefaultTokeniser owner){
      myIndex = index++;
      owner.dfsmStates.add(this);
    }

    void put(UnicodeType type, DFSMState state){
      put(type.type, state);
    }

    DFSMState next(byte type){//UnicodeType type){
      return transitionFunction[type +128];
    }

    void put(int index, DFSMState state){
      transitionFunction[index +128] =state;
    }

    String getEdgesGML(){
      String res = "";
      Set nextSet;
      Iterator nextSetIter;
      DFSMState nextState;
      for(int i = 0; i< transitionFunction.length; i++){
        nextState = transitionFunction[i];
        if(null != nextState){
          res += "edge [ source " + myIndex +
          " target " + nextState.getIndex() +
          " label \"";
          res += DefaultTokeniser.typesMnemonics[i];
          res += "\" ]\n";
        }
      };
      return res;
    }

    void buildTokenDesc() throws TokeniserException{
      String ignorables = " \t\f";
      String token = null,
             type = null,
             attribute = null,
             value = null,
             prefix = null,
             read ="";
      LinkedList attributes = new LinkedList(),
                 values = new LinkedList();
      StringTokenizer mainSt =
        new StringTokenizer(rhs, ignorables + "\\\";=", true);
      int descIndex = 0;
      //phase means:
      //0 == looking for type;
      //1 == looking for attribute;
      //2 == looking for value;
      //3 == write the attr/value pair
      int phase = 0;
      while(mainSt.hasMoreTokens()){
        token = DefaultTokeniser.skipIgnoreTokens(mainSt);
        if(token.equals("\\")){
          if(null == prefix) prefix = mainSt.nextToken();
          else prefix += mainSt.nextToken();
          continue;
        }else if(null != prefix){
          read += prefix;
          prefix = null;
        }
        if(token.equals("\"")){
          read = mainSt.nextToken("\"");
          if(read.equals("\"")) read = "";
          else{
            //delete the remaining enclosing quote and restore the delimiters
            mainSt.nextToken(ignorables + "\\\";=");
          }
        }else if(token.equals("=")){
          if(phase == 1){
            attribute = read;
            read = "";
            phase = 2;
          }else throw new TokeniserException("Invalid attribute format: " +
                                             read);
        }else if(token.equals(";")){
          if(phase == 0){
            type = read;
            read = "";
//System.out.print("Type: " + type);
            attributes.addLast(type);
            values.addLast("");
            phase = 1;
          }else if(phase == 2){
            value = read;
            read = "";
            phase = 3;
          }else throw new TokeniserException("Invalid value format: " +
                                             read);
        }else read += token;

        if(phase == 3){
//System.out.print("; " + attribute + "=" + value);
          attributes.addLast(attribute);
          values.addLast(value);
          phase = 1;
        }
      }
//System.out.println();
      if(attributes.size() < 1)
        throw new InvalidRuleException("Invalid right hand side " + rhs);
      tokenDesc = new String[attributes.size()][2];
      for(int i = 0; i < attributes.size(); i++){
        tokenDesc[i][0] = (String)attributes.get(0);
        tokenDesc[i][1] = (String)values.get(0);
      }
    }

    void setRhs(String rhs){this.rhs = rhs;}
    String getRhs(){return rhs;}
    boolean isFinal() {return (null != rhs);}
    int getIndex(){return myIndex;}

    String[][] getTokenDesc(){
      return tokenDesc;
    }

    String[][] tokenDesc;
    DFSMState[] transitionFunction = new DFSMState[256];
    String rhs;
    int myIndex;
    static int index;
    static{
      index = 0;
    }
  }//class DFSMState
