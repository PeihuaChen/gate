# .cshrc

# User specific aliases and functions

alias rm 'rm -i'
alias cp 'cp -i'
alias mv 'mv -i'

setenv PATH "/usr/sbin:/sbin:${PATH}"

set prompt=\[`id -nu`@`hostname -s`\]\#\ 
