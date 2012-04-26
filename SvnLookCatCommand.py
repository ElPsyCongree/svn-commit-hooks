__author__ = 'Fede Lopez'

class SvnLookCatCommand:
    def __init__(self, repositoryPath, transaction, filePath):
        self.repositoryPath = repositoryPath
        self.transaction = transaction
        self.filePath = filePath.strip()

    def command(self):
        if self.notDeleted() and self.acceptFile():
            self.filePath = self.filePath.replace('UU ', '', 1).replace('U ', '', 1).replace('A ', '', 1).strip()
            return 'svnlook cat -t ' + self.transaction + ' ' + self.repositoryPath + ' ' +self.filePath
        else:
            return None

    def notDeleted(self):
        return self.filePath.startswith('U ') or self.filePath.startswith('UU ')  or self.filePath.startswith('A ')

    def acceptFile(self):
        accepted = (".JAVA", ".FORM")
        return self.filePath.upper().endswith(accepted)
