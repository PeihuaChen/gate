package shef.nlp.buchart.prolog.cafe;
import jp.ac.kobe_u.cs.prolog.lang.*;
import jp.ac.kobe_u.cs.prolog.builtin.*;

/*
 * *** Please do not edit ! ***
 * @(#) PRED_once_1.java
 * @procedure once/1 in buchart_utils.pl
 */

/*
 * @version Prolog Cafe 0.8 November 2003
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura    (tamura@kobe-u.ac.jp)
 */

public class PRED_once_1 extends Predicate {
    static SymbolTerm f1 = SymbolTerm.makeSymbol(":", 2);
    static SymbolTerm s2 = SymbolTerm.makeSymbol("shef.nlp.buchart.prolog.cafe");

    public Term arg1;

    public PRED_once_1(Term a1, Predicate cont) {
        arg1 = a1; 
        this.cont = cont;
    }

    public PRED_once_1(){}
    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0]; 
        this.cont = cont;
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2, a3;
        Predicate p1, p2;
        a1 = arg1.dereference();

        a2 = new VariableTerm(engine);
        Term[] h3 = {s2, a1};
        a3 = new StructureTerm(f1, h3);
        p1 = new PRED_$cut_1(a2, cont);
        p2 = new PRED_translated_goal_1(a3, p1);
        return new PRED_$get_level_1(a2, p2);
    }

    public int arity() { return 1; }

    public String toString() {
        return "once(" + arg1 + ")";
    }
}

