# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
        . ~/.bashrc
fi

# User specific environment and startup programs

PATH=$PATH:$HOME/bin
ENV=$HOME/.bashrc
USERNAME="root"
CVSROOT=:pserver:valyt@belch:/share/nlp/src/CVS_Repository
export USERNAME ENV PATH CVSROOT

mesg n
