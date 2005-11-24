#!/usr/bin/perl -w

# Script to read all the creole.xml files for every plugin
# to produce a summary HTML page (GATE/doc/plugins.html)
# by Andrew Golightly

use strict;
use XML::Simple;
use XML::XPath;
use XML::XPath::XMLParser;

print "Extracting info on GATE plugins...\n";

# Grab all the creole files for all plugins
my @creoleFileList = glob("../plugins/*/creole.xml");

# Open file handle to the HTML file we are creating
my $htmlFilename = '../doc/plugins.html';
open(HTMLFILE , ">$htmlFilename") || die("Cannot Open File $htmlFilename");
print HTMLFILE <<ENDHTML;

<html>
<head>
<title>List of plugins available to GATE</title>
<style>
	a img {border: none;}
</style>
</head>
<body>
<center>
	<a href="http://gate.ac.uk/"><img src="http://www.gate.ac.uk/gateHeader.gif" alt="GATE" height="76" width="356"/></a>
</center>
<br/>
<p>This page lists the plugins that are currently shipped with GATE.</p>
ENDHTML

# foreach plugin creole.xml file...
foreach my $creoleFileName (@creoleFileList)
{
	$creoleFileName =~ /plugins\/(\w+)\/creole.xml/;
	print "$1\n";
	print HTMLFILE "<h3>$1</h3>\n";
	# parse the XML file
   	my $xp = XML::XPath->new(filename => $creoleFileName);
	# find all resources in this creole.xml file..
    my $nodeset = $xp->find('//RESOURCE');
    
	print HTMLFILE "<table border='1'>\n";
    foreach my $node ($nodeset->get_nodelist) 
	{
		my $creoleFragment = XML::XPath::XMLParser::as_string($node);
		print HTMLFILE "\t<tr>\n";
		printElement($creoleFragment, 'NAME');
		printElement($creoleFragment, 'COMMENT');
		printElement($creoleFragment, 'CLASS');
		print HTMLFILE "\t</tr>\n";
	}
	
	print HTMLFILE "</table>\n";
}

print HTMLFILE <<ENDHTML;
</table>
<br/>
<hr/>
<table width="100%">
	<tr>
		<td>
			<a href="http://nlp.shef.ac.uk/"><img src="http://www.gate.ac.uk/revNlpLogo.jpg" width="164" height="60" alt="NLP group"/></a>
		</td>
		<td align="right">
			<img src="http://www.gate.ac.uk/nlpTitle.gif" width="250" height="18"/>
			<br/>
			<img src="http://www.gate.ac.uk/redline.jpg" width="500" height="17"/>
		</td>
	</tr>
</table>	
</body>
</html>

ENDHTML

print "done. ($htmlFilename created)\n";

# Print out the value of an element
# $_[0] is the resource node
# $_[1] is the element name
sub printElement {

	my $creoleFragment = XMLin($_[0], ForceArray => 1);
	my $elementValue = $creoleFragment->{$_[1]}->[0];
	print HTMLFILE "\t\t<td>";
	print HTMLFILE $elementValue ? $elementValue : "<i>no data available</i>";
	print HTMLFILE "</td>\n";
}
