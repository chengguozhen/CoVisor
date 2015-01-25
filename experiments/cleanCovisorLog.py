import os.path;
import sys;

'''
Get rid of uninteresting parts of covisor.log.

python cleanCovisorLog.py save0 save1 ... saven

keeps only lines containing save0, save1, ... saven.
'''

INLOG = "covisor.log"

def clean(save):
    outlog = string_of_save(save) + "." + INLOG;
    if os.path.exists(INLOG):
        inlog = open(INLOG, "r");
        outlog = open(outlog, "w");
        for line in inlog.readlines():
            interesting = False
            for to_save in save:
                if to_save in line:
                    interesting = True
            if interesting:
                outlog.write(shorten_line(line))
        inlog.close()
        outlog.close()
    else:
        print INLOG + " does not exist."

def shorten_line(line):
    if "INFO" in line:
        return line[line.find("INFO"):]
    elif "DEBUG" in line:
        return line[line.find("DEBUG"):]
    else:
        return line

def string_of_save(save):
    s = ""
    for to_save in save:
        if len(s) > 0:
            s = s + "-"
        s = s + to_save
    return s

if __name__ == "__main__":
    save = sys.argv[1:]
    clean(save);
