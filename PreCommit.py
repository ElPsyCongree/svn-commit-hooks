import re
import sys
from StdOutCapture import StdOutCapture
from SvnLookCatCommand import SvnLookCatCommand
from SvnLookChangedCommand import SvnLookChangedCommand

PAUSE_IN_JAVA_FILE_MSG = 'File contains: a pause: '
INTELLIJ_FORM_WITH_INVALID_REFERENCE_MSG = 'IntelliJ form references to a resource bundle: '

__author__ = 'Fede Lopez'

class PreCommit():
    def __init__(self, repositoryPath, transaction):
        self.repositoryPath = repositoryPath
        self.transaction = transaction

    def pauseInCommentedLine(self, line):
        return re.search(r"//.+\b(TestSetup|TestBase)\.\bpause\(\)", line) is not None

    def pauseInCommentedBlock(self, line):
        return re.search(r"/\*.+\b(TestSetup|TestBase)\.\bpause\(\).+\*/", line) is not None

    def pauseInLine(self, line):
        pauseInstruction = ('TestSetup.pause()', 'TestBase.pause()')
        for instruction in pauseInstruction:
            if instruction in line:
                return True
        return False

    def hasPause(self, line):
        if self.pauseInLine(line):
            if self.pauseInCommentedLine(line):
                return False
            if self.pauseInCommentedBlock(line):
                return False
            return True
        return False

    def intelliJFormHasInvalidReference(self, line):
        return re.search(r"resource-bundle=\".+key=\"\w*\"", line) is not None

    def checkFile(self, fileAsString):
        for lineOfCode in fileAsString:
            if self.hasPause(lineOfCode):
                return PAUSE_IN_JAVA_FILE_MSG
            if self.intelliJFormHasInvalidReference(lineOfCode):
                return INTELLIJ_FORM_WITH_INVALID_REFERENCE_MSG
        return None

    def main(self):
        svnCommand = SvnLookChangedCommand(self.repositoryPath, self.transaction)
        stdOutCapture = StdOutCapture(svnCommand.command())
        changedFiles = stdOutCapture.linesFromStdOut()

        f = open('C:\\temp\\log.txt', 'w')

        for changedFile in changedFiles:
            f.write("\nAbout to process: "  + changedFile)
            svnLookCatCommand = SvnLookCatCommand(self.repositoryPath, self.transaction, changedFile)
            cmd = svnLookCatCommand.command()
            if cmd is None:
                f.write('\nNone for ' + changedFile)
                continue
            f.write("\nCommand is: " + cmd)
            stdOutCapture = StdOutCapture(cmd)
            lines = stdOutCapture.linesFromStdOut()
            result = self.checkFile(lines)
            if result is not None:
                sys.stderr.write(result + svnCommand.filePath)
                return -1
        return 0

if __name__ == '__main__':
    preCommit = PreCommit(sys.argv[1], sys.argv[2])
    sys.exit(preCommit.main())