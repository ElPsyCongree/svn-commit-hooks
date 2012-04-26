from subprocess import Popen, PIPE

__author__ = 'Fede Lopez'

class StdOutCapture:
    def __init__(self, cmd):
        self.cmd = cmd.strip()

    def linesFromStdOut(self):
        p = Popen(self.cmd, stdout=PIPE, stderr=PIPE)
        stdout = p.communicate()
        lines = []
        if stdout is None:
            pass
        for line in stdout:
            lines.append(line)
        return stdout