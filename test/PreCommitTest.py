from PreCommit import PreCommit, PAUSE_IN_JAVA_FILE_MSG, INTELLIJ_FORM_WITH_INVALID_REFERENCE_MSG, ILLEGAL_IMPORT_MSG, SOLO_TEST_MSG

__author__ = 'Fede Lopez'

import unittest


class MyTestCase(unittest.TestCase):
    def test_checkFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-java-files/Attribute.java', 'r')
        actual = commit.checkFile(file.read())

        self.assertEqual(None, actual)

    def test_checkFileIgnoresCommentedLines(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-java-files/AttributeEditor.java', 'r')
        actual = commit.checkFile(file.read())

        self.assertEqual(None, actual)

    def test_checkFileIgnoresCommentedLineNotStartingWithForwardSlash(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-java-files/CommentedLineNotStartingWithForwardSlash.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(None, actual)

    def test_checkFileIgnoresCommentedBlock(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-java-files/CommentedBlock.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(None, actual)

    def test_checkFileFailsForTestSetupPause(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-java-files/Attribute.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(PAUSE_IN_JAVA_FILE_MSG, actual)

    def test_checkFileFailsForFunctionTestWithPause(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-java-files/TutorialTextConditions.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(PAUSE_IN_JAVA_FILE_MSG, actual)

    def test_checkFileFailsForTestBasePause(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-java-files/AttributeWithTestBasePause.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(PAUSE_IN_JAVA_FILE_MSG, actual)

    def test_checkFileFailsWhenOutsideCommentedBlock(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-java-files/OutsideCommentedBlock.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(PAUSE_IN_JAVA_FILE_MSG, actual)

    def test_checkIntelliJForm(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-form-files/AttributePropertiesDialog.form', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(None, actual)

    def test_checkIntelliJFormFails(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-form-files/AttributePropertiesDialog.form', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(INTELLIJ_FORM_WITH_INVALID_REFERENCE_MSG, actual)

    def test_checkIllegalImportsFails(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-java-files/IllegalImport.java', 'r')
        javaClass = file.read().splitlines()
        actual = commit.checkFile(javaClass)

        self.assertEqual(ILLEGAL_IMPORT_MSG, actual)

    def test_checkDartFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-dart-files/DartTest.dart', 'r')
        actual = commit.checkFile(file.read().splitlines())

        self.assertEqual(None, actual)

    def test_checkIllegalSoloGroupDartFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-dart-files/IllegalDartTest1.dart', 'r')
        actual = commit.checkFile(file.read().splitlines())

        self.assertEqual(SOLO_TEST_MSG, actual)

    def test_checkIllegalSoloTestDartFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-dart-files/IllegalDartTest2.dart', 'r')
        actual = commit.checkFile(file.read().splitlines())

        self.assertEqual(SOLO_TEST_MSG, actual)

    def test_checkJSFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/clean-js-files/jsSpec.js', 'r')
        actual = commit.checkFile(file.read().splitlines())

        self.assertEqual(None, actual)

    def test_checkIllegalSoloGroupJSFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-js-files/IllegalJsSpec1.js', 'r')
        actual = commit.checkFile(file.read().splitlines())

        self.assertEqual(SOLO_TEST_MSG, actual)

    def test_checkIllegalSoloTestJSFile(self):
        commit = PreCommit(None, None)
        file = open('./resources/illegal-js-files/IllegalJsSpec2.js', 'r')
        actual = commit.checkFile(file.read().splitlines())

        self.assertEqual(SOLO_TEST_MSG, actual)


if __name__ == '__main__':
    unittest.main()
