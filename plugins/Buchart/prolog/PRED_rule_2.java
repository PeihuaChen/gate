package shef.nlp.buchart.prolog.cafe;
import jp.ac.kobe_u.cs.prolog.lang.*;
import jp.ac.kobe_u.cs.prolog.builtin.*;

/*
 * *** Please do not edit ! ***
 * @(#) PRED_rule_2.java
 * @procedure rule/2 in compile_grammar.pl
 */

/*
 * @version Prolog Cafe 0.8 November 2003
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura    (tamura@kobe-u.ac.jp)
 */

public class PRED_rule_2 extends Predicate {
    static Predicate rule_2_1 = new PRED_rule_2_1();
    static Predicate rule_2_2 = new PRED_rule_2_2();
    static Predicate rule_2_var = new PRED_rule_2_var();

    public Term arg1, arg2;

    public PRED_rule_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1; 
        arg2 = a2; 
        this.cont = cont;
    }

    public PRED_rule_2(){}
    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0]; 
        arg2 = args[1]; 
        this.cont = cont;
    }

    public Predicate exec(Prolog engine) {
        engine.aregs[1] = arg1;
        engine.aregs[2] = arg2;
        engine.cont = cont;
        return call(engine);
    }

    public Predicate call(Prolog engine) {
        engine.setB0();
        return engine.switch_on_term(
                                   rule_2_var,
                                   rule_2_1,
                                   rule_2_var,
                                   rule_2_1,
                                   rule_2_1
                                   );
    }

    public int arity() { return 2; }

    public String toString() {
        return "rule(" + arg1 + ", " + arg2 + ")";
    }
}

class PRED_rule_2_var extends PRED_rule_2 {
    static Predicate rule_2_var_1 = new PRED_rule_2_var_1();

    public Predicate exec(Prolog engine) {
        return engine.jtry(rule_2_1, rule_2_var_1);
    }
}

class PRED_rule_2_var_1 extends PRED_rule_2 {

    public Predicate exec(Prolog engine) {
        return engine.trust(rule_2_2);
    }
}

class PRED_rule_2_1 extends PRED_rule_2 {
    static SymbolTerm f1 = SymbolTerm.makeSymbol("rule", 2);
    static SymbolTerm f3 = SymbolTerm.makeSymbol(":", 2);
    static SymbolTerm s4 = SymbolTerm.makeSymbol("shef.nlp.buchart.prolog.cafe");

    public Predicate exec(Prolog engine) {
        Term a1, a2, a3, a4, a5;
        Predicate p1;
        a1 = engine.aregs[1].dereference();
        a2 = engine.aregs[2].dereference();
        Predicate cont = engine.cont;

        Term[] h2 = {a1, a2};
        a3 = new StructureTerm(f1, h2);
        a4 = new VariableTerm(engine);
        Term[] h5 = {s4, a4};
        a5 = new StructureTerm(f3, h5);
        p1 = new PRED_translated_goal_1(a5, cont);
        return new PRED_clause_2(a3, a4, p1);
    }
}

class PRED_rule_2_2 extends PRED_rule_2 {
    static SymbolTerm s1 = SymbolTerm.makeSymbol("dummy");

    public Predicate exec(Prolog engine) {
        Term a1, a2;
        a1 = engine.aregs[1].dereference();
        a2 = engine.aregs[2].dereference();
        Predicate cont = engine.cont;

        if ( !s1.unify(a1, engine.trail) ) return engine.fail();
        if ( !s1.unify(a2, engine.trail) ) return engine.fail();
        return cont;
    }
}

