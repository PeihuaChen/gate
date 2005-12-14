#!/usr/bin/perl -w -C

# Script to read all the creole.xml files for every plugin
# to produce a summary HTML page (GATE/doc/plugins.html)
# by Andrew Golightly

use strict;
use XML::Simple;
use XML::XPath;
use XML::XPath::XMLParser;

# ********** Some constants **********
my $internalPluginsTitle = "Plugins included in the GATE distribution";
my $externalPluginsTitle = "Other contributed plugins";

# Grab all the creole filenames for all the plugins
my @creoleFileList = glob("../plugins/*/creole.xml");

my @elementsToGet = ("NAME", "COMMENT", "CLASS");
# **************************************************

print "Extracting information on GATE plugins\n";
print "--------------------------------------\n\n";

# ********** Write HTML for the top of the plugins page **********
# Open file handle to the HTML file we are creating
my $htmlFilename = '../doc/plugins.html';
open(HTMLFILE , ">$htmlFilename") || die("Cannot Open File $htmlFilename");

print HTMLFILE <<ENDHTML;
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<head>
<title>List of plugins available to GATE</title>
<META http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
	a img {border: none;}
	th {background-color: #00CCFF;}
</style>
</head>
<body>
<center>
	<a href="http://gate.ac.uk/"><img src="http://www.gate.ac.uk/gateHeader.gif" alt="GATE" height="76" width="356"/></a>
</center>
<br/>
<p>This page lists the plugins that are currently available with GATE:</p>
<ul>
	<li><a href="#internal-plugins">
ENDHTML

print HTMLFILE "$internalPluginsTitle";

print HTMLFILE <<ENDHTML;
	</a></li>
	<li><a href="#external-plugins">
ENDHTML

print HTMLFILE $externalPluginsTitle,
	<<ENDHTML;
	
	</a></li>
</ul>

<p>For more information on how the plugins work, see the online user guide "<a href="http://gate.ac.uk/sale/tao/#sec:howto:plugins">Developing Language Processing Components with GATE</a>".</p>

<hr/>
ENDHTML
# **************************************************

# ********** Write internal plugin information to the HTML file **********
print "Extracting internal plugins information..\n";
print HTMLFILE "<a name='internal-plugins'></a>\n",
				"<h2>$internalPluginsTitle</h2>\n",
				"<ul type='circle'>";

foreach my $creoleFileName (@creoleFileList)
{
	$creoleFileName =~ /plugins\/(\w+)\/creole.xml/;
	print HTMLFILE "<li><a href='#$1'>$1</a></li>\n";
}

print HTMLFILE "</ul>\n",
				"<table border='1'>\n";

# foreach plugin creole.xml file...
foreach my $creoleFileName (@creoleFileList)
{
	$creoleFileName =~ /plugins\/(\w+)\/creole.xml/;
	print "$1\n";
	print HTMLFILE "\t<tr>\n\t\t<th colspan='3'><a name='$1'>$1</a></th>\n\t</tr>\n";
   	my $xp = XML::XPath->new(filename => $creoleFileName); # parse the XML file
    my $nodeset = $xp->find('//RESOURCE'); 	# find all resources in this creole.xml file..
	foreach my $node ($nodeset->get_nodelist) 
	{
		my $creoleFragment = XML::XPath::XMLParser::as_string($node);
		print HTMLFILE "\t<tr>\n";
		
		foreach my $elementToGet (@elementsToGet)
		{
			print HTMLFILE "\t\t<td>", getElement($creoleFragment, $elementToGet), "</td>\n";
		}
		print HTMLFILE "\t</tr>\n";
	}
}

print HTMLFILE "</table>\n",
				"<hr/>\n";    
print ".. all internal plugin information extracted.\n\n";
# **************************************************

# ********** Include external-plugins.html page **********
print "Importing external external plugins information ... ";
print HTMLFILE "<a name='external-plugins'></a>\n",
	"<h2>$externalPluginsTitle</h2>\n";
my $externalPluginsFilename = '../doc/external-plugins.html';
open(EXTERNALHTMLFILE , "<$externalPluginsFilename") || die("Cannot Open File $externalPluginsFilename");
while(<EXTERNALHTMLFILE>)
{
	print HTMLFILE;
}
close(EXTERNALHTMLFILE);
print "done!\n";
# **************************************************

# ********** Write the footer images of the plugins page **********
print HTMLFILE <<ENDHTML;
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

close(HTMLFILE);
# **************************************************

print "\nAll done. ($htmlFilename created)\n";

# Get the value of an element
# $_[0] is the resource node
# $_[1] is the element name
sub getElement {

	if($_[0] =~ /<$_[1]>(.*)<\/$_[1]>/s)
	{
		my $elementValue = $1;
		# Finds all urls and converts them to links.
		$elementValue =~ s|(http://[^\s)]+)|<a href="$1">(docs)</a>|g;
		return $elementValue;
	}
	else 
	{
		return "&nbsp;";
	}
}