import os.path;
import sys;

'''
Get rid of uninteresting parts of covisor.log.

python cleanCovisorLog.py save0 save1 ... saven

keeps only lines containing save0, save1, ... saven.
'''

INLOG = "covisor.log"

def clean(args, yes_remove=True):
    outlog = string_of_args(args) + "." + INLOG
    if os.path.exists(INLOG):
        inlog = open(INLOG, "r")
        outlog = open(outlog, "w")
        for line in inlog.readlines():
            if yes_remove:
                remove(line, args, outlog)
            else:
                save(line, args, outlog)
        inlog.close()
        outlog.close()
    else:
        print INLOG + " does not exist."

def save(line, args, outlog):
    interesting = False
    for to_save in args:
        if to_save in line:
            interesting = True
        if interesting:
            outlog.write(shorten_line(line))

def remove(line, args, outlog):
    interesting = True
    for to_remove in args:
        if to_remove in line:
            return
    outlog.write(shorten_line(line))

def shorten_line(line):
    if "INFO" in line:
        return line[line.find("INFO"):]
    elif "DEBUG" in line:
        return line[line.find("DEBUG"):]
    else:
        return line

def string_of_args(args):
    s = ""
    for this_arg in args:
        if len(s) > 0:
            s = s + "-"
        s = s + this_arg
    return s

if __name__ == "__main__":
    if sys.argv[1] == "save":
        to_save = ["FlowMod", "priority", "match", "actions", "cookie"]
        to_save.extend(sys.argv[2:])
        clean(to_save, yes_remove=False)
    elif sys.argv[1] == "remove":
        to_remove = ["DBManager"]
        to_remove.extend(sys.argv[2:])
        clean(to_remove)

    
