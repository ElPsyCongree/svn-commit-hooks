from subprocess import Popen, PIPE

__author__ = 'Fede Lopez'

class StdOutCapture:
    def __init__(self, cmd):
        self.cmd = cmd.strip()

    def linesFromStdOut(self):
        p = Popen(self.cmd, stdout=PIPE, stderr=PIPE)
        std_out = p.communicate()[0]
        result = []
        lines = std_out.splitlines()
        for line in lines:
            trimmed = line.strip()
            if trimmed != "":
                result.append(trimmed)
        return result