/*
 *  collection types
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 05/Feb/2002
 *
 *  $Id$
 *
 */  
create or replace type INT_ARRAY as varray(10) of number;
/

create or replace type STRING_ARRAY as varray(10) of varchar2(4000);
/
