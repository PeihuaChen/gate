package shef.nlp.buchart.prolog.cafe;
import jp.ac.kobe_u.cs.prolog.lang.*;
import jp.ac.kobe_u.cs.prolog.builtin.*;

/*
 * *** Please do not edit ! ***
 * @(#) PRED_cvsid_parse_file_1.java
 * @procedure cvsid_parse_file/1 in parse_file.pl
 */

/*
 * @version Prolog Cafe 0.8 November 2003
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura    (tamura@kobe-u.ac.jp)
 */

public class PRED_cvsid_parse_file_1 extends Predicate {
    static IntegerTerm s1 = new IntegerTerm(36);
    static IntegerTerm s2 = new IntegerTerm(73);
    static IntegerTerm s3 = new IntegerTerm(100);
    static IntegerTerm s4 = new IntegerTerm(58);
    static IntegerTerm s5 = new IntegerTerm(32);
    static IntegerTerm s6 = new IntegerTerm(112);
    static IntegerTerm s7 = new IntegerTerm(97);
    static IntegerTerm s8 = new IntegerTerm(114);
    static IntegerTerm s9 = new IntegerTerm(115);
    static IntegerTerm s10 = new IntegerTerm(101);
    static IntegerTerm s11 = new IntegerTerm(95);
    static IntegerTerm s12 = new IntegerTerm(102);
    static IntegerTerm s13 = new IntegerTerm(105);
    static IntegerTerm s14 = new IntegerTerm(108);
    static IntegerTerm s15 = new IntegerTerm(46);
    static IntegerTerm s16 = new IntegerTerm(44);
    static IntegerTerm s17 = new IntegerTerm(118);
    static IntegerTerm s18 = new IntegerTerm(49);
    static IntegerTerm s19 = new IntegerTerm(50);
    static IntegerTerm s20 = new IntegerTerm(48);
    static IntegerTerm s21 = new IntegerTerm(52);
    static IntegerTerm s22 = new IntegerTerm(47);
    static IntegerTerm s23 = new IntegerTerm(53);
    static IntegerTerm s24 = new IntegerTerm(56);
    static IntegerTerm s25 = new IntegerTerm(55);
    static IntegerTerm s26 = new IntegerTerm(57);
    static IntegerTerm s27 = new IntegerTerm(103);
    static IntegerTerm s28 = new IntegerTerm(111);
    static IntegerTerm s29 = new IntegerTerm(110);
    static IntegerTerm s30 = new IntegerTerm(69);
    static IntegerTerm s31 = new IntegerTerm(120);
    static SymbolTerm s32 = SymbolTerm.makeSymbol("[]");
    static ListTerm s33 = new ListTerm(s1, s32);
    static ListTerm s34 = new ListTerm(s5, s33);
    static ListTerm s35 = new ListTerm(s6, s34);
    static ListTerm s36 = new ListTerm(s31, s35);
    static ListTerm s37 = new ListTerm(s30, s36);
    static ListTerm s38 = new ListTerm(s5, s37);
    static ListTerm s39 = new ListTerm(s29, s38);
    static ListTerm s40 = new ListTerm(s28, s39);
    static ListTerm s41 = new ListTerm(s13, s40);
    static ListTerm s42 = new ListTerm(s27, s41);
    static ListTerm s43 = new ListTerm(s27, s42);
    static ListTerm s44 = new ListTerm(s7, s43);
    static ListTerm s45 = new ListTerm(s9, s44);
    static ListTerm s46 = new ListTerm(s5, s45);
    static ListTerm s47 = new ListTerm(s26, s46);
    static ListTerm s48 = new ListTerm(s21, s47);
    static ListTerm s49 = new ListTerm(s4, s48);
    static ListTerm s50 = new ListTerm(s25, s49);
    static ListTerm s51 = new ListTerm(s18, s50);
    static ListTerm s52 = new ListTerm(s4, s51);
    static ListTerm s53 = new ListTerm(s24, s52);
    static ListTerm s54 = new ListTerm(s20, s53);
    static ListTerm s55 = new ListTerm(s5, s54);
    static ListTerm s56 = new ListTerm(s23, s55);
    static ListTerm s57 = new ListTerm(s19, s56);
    static ListTerm s58 = new ListTerm(s22, s57);
    static ListTerm s59 = new ListTerm(s18, s58);
    static ListTerm s60 = new ListTerm(s18, s59);
    static ListTerm s61 = new ListTerm(s22, s60);
    static ListTerm s62 = new ListTerm(s21, s61);
    static ListTerm s63 = new ListTerm(s20, s62);
    static ListTerm s64 = new ListTerm(s20, s63);
    static ListTerm s65 = new ListTerm(s19, s64);
    static ListTerm s66 = new ListTerm(s5, s65);
    static ListTerm s67 = new ListTerm(s18, s66);
    static ListTerm s68 = new ListTerm(s15, s67);
    static ListTerm s69 = new ListTerm(s18, s68);
    static ListTerm s70 = new ListTerm(s15, s69);
    static ListTerm s71 = new ListTerm(s18, s70);
    static ListTerm s72 = new ListTerm(s15, s71);
    static ListTerm s73 = new ListTerm(s18, s72);
    static ListTerm s74 = new ListTerm(s5, s73);
    static ListTerm s75 = new ListTerm(s17, s74);
    static ListTerm s76 = new ListTerm(s16, s75);
    static ListTerm s77 = new ListTerm(s14, s76);
    static ListTerm s78 = new ListTerm(s6, s77);
    static ListTerm s79 = new ListTerm(s15, s78);
    static ListTerm s80 = new ListTerm(s10, s79);
    static ListTerm s81 = new ListTerm(s14, s80);
    static ListTerm s82 = new ListTerm(s13, s81);
    static ListTerm s83 = new ListTerm(s12, s82);
    static ListTerm s84 = new ListTerm(s11, s83);
    static ListTerm s85 = new ListTerm(s10, s84);
    static ListTerm s86 = new ListTerm(s9, s85);
    static ListTerm s87 = new ListTerm(s8, s86);
    static ListTerm s88 = new ListTerm(s7, s87);
    static ListTerm s89 = new ListTerm(s6, s88);
    static ListTerm s90 = new ListTerm(s5, s89);
    static ListTerm s91 = new ListTerm(s4, s90);
    static ListTerm s92 = new ListTerm(s3, s91);
    static ListTerm s93 = new ListTerm(s2, s92);
    static ListTerm s94 = new ListTerm(s1, s93);

    public Term arg1;

    public PRED_cvsid_parse_file_1(Term a1, Predicate cont) {
        arg1 = a1; 
        this.cont = cont;
    }

    public PRED_cvsid_parse_file_1(){}
    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0]; 
        this.cont = cont;
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1;
        a1 = arg1.dereference();

        if ( !s94.unify(a1, engine.trail) ) return engine.fail();
        return cont;
    }

    public int arity() { return 1; }

    public String toString() {
        return "cvsid_parse_file(" + arg1 + ")";
    }
}

