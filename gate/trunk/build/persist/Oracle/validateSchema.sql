/*
 *  validateSchema.sql
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 01/Oct/2001
 *
 *  $Id$
 */



select * from sys.all_objects
where object_type='PACKAGE'
and   status != 'VALID';