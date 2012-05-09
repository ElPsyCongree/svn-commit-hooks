from SvnLookCatCommand import SvnLookCatCommand

REPOSITORIES_DEV = 'D:\BackedUp\csvn\data\\repositories\dev'

__author__ = 'Fede Lopez'

import unittest

class MyTestCase(unittest.TestCase):
    def test_commandForUpdatedFiles(self):
        transaction = 'ax8'
        file = 'U   rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)
        expected = 'svnlook cat -t ax8 D:\BackedUp\csvn\data\\repositories\dev rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        self.assertEqual(expected, svnLook.command())

    def test_commandForUpdatedFilesTrims(self):
        transaction = 'ax8'
        file = '  U   rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)
        expected = 'svnlook cat -t ax8 D:\BackedUp\csvn\data\\repositories\dev rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        self.assertEqual(expected, svnLook.command())

    def test_commandForPropertiesUpdatedFiles(self):
        transaction = 'ax8'
        file = 'UU   rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)
        expected = 'svnlook cat -t ax8 D:\BackedUp\csvn\data\\repositories\dev rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        self.assertEqual(expected, svnLook.command())

    def test_commandForAddedFiles(self):
        transaction = 'ax8'
        file = 'A  rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)
        expected = 'svnlook cat -t ax8 D:\BackedUp\csvn\data\\repositories\dev rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        self.assertEqual(expected, svnLook.command())

    def test_commandForDeletedFiles(self):
        transaction = 'ax8'
        file = 'D  rippledown/trunk/src/rippledown/val/gui/CaseViewer.java'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)

        self.assertEqual(None, svnLook.command())

    def test_commandForIntelliJFormFiles(self):
        transaction = 'ax8'
        file = 'A  rippledown/trunk/src/rippledown/help/val/CaseViewer.form'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)

        expected = 'svnlook cat -t ax8 D:\BackedUp\csvn\data\\repositories\dev rippledown/trunk/src/rippledown/help/val/CaseViewer.form'

        self.assertEqual(expected, svnLook.command())

    def test_commandIgnoresOtherFiles(self):
        transaction = 'ax8'
        file = 'A  rippledown/trunk/src/rippledown/help/val/CaseViewer.png'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)

        self.assertEqual(None, svnLook.command())

    def test_commandWithUpperCase(self):
        transaction = 'ax8'
        file = 'A  rippledown/trunk/src/rippledown/attribute/Attribute.java'

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)

        self.assertEqual('svnlook cat -t ax8 D:\BackedUp\csvn\data\\repositories\dev rippledown/trunk/src/rippledown/attribute/Attribute.java', svnLook.command())

    def test_commandForEmptyString(self):
        transaction = 'ax8'
        file = '   '

        svnLook = SvnLookCatCommand(REPOSITORIES_DEV, transaction, file)

        self.assertEqual(None, svnLook.command())

if __name__ == '__main__':
    unittest.main()
