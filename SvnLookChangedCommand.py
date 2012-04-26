__author__ = 'Fede Lopez'

class SvnLookChangedCommand:
    def __init__(self, repositoryPath, transaction):
        self.repositoryPath = repositoryPath
        self.transaction = transaction

    def command(self):
        return 'svnlook changed -t ' + self.transaction + ' ' + self.repositoryPath
