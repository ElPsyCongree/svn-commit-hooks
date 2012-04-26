from StdOutCapture import StdOutCapture

__author__ = 'Fede Lopez'

import unittest

class MyTestCase(unittest.TestCase):

    def test_captureStdOut(self):
        stdOutCapture = StdOutCapture('help')
        actual = stdOutCapture.linesFromStdOut()
        self.assertTrue(len(actual) > 1)

    def test_trimCommand(self):
        stdOutCapture = StdOutCapture('  help  ')
        actual = stdOutCapture.linesFromStdOut()
        self.assertEquals('help', stdOutCapture.cmd)

if __name__ == '__main__':
    unittest.main()
