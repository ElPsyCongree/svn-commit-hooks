from SvnLookChangedCommand import SvnLookChangedCommand

__author__ = 'Fede Lopez'

import unittest

class MyTestCase(unittest.TestCase):
    def test_command(self):
        transaction = 'ax8'
        svnLook = SvnLookChangedCommand('D:\BackedUp\csvn\data\\repositories\dev', transaction)

        self.assertEqual('svnlook changed -t ax8 D:\BackedUp\csvn\data\\repositories\dev', svnLook.command())

if __name__ == '__main__':
    unittest.main()
