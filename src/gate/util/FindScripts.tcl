# FindScripts.tcl
#
# lists all the .tcl scripts in the gate2/src directory
#
# Hamish, 14/3/00 
# $Id$

set tclFiles [list]

proc filter { dir } {
  global tclFiles

  foreach f [glob -nocomplain ${dir}/*] {
    if [file isdirectory $f] { filter $f }
    if [string match {*.tcl} $f] { lappend tclFiles [string range $f 2 end] }
  }
}

proc findScripts { } {
  global tclFiles

  # cd to the gate2/src directory
  # assumes that we are in gate2 or one of its subdirectories when we start
  set WD [pwd]
  if { ! [string match "*gate2*" $WD] } { error "not in the gate2 directories" }
  while { [file tail $WD] != "gate2" } { cd ..; set WD [pwd] }
  cd src

  filter {.}

  set tclFiles
}

