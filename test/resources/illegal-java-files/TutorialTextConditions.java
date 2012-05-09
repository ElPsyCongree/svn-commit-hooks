/*
 * Copyright (c) 2012 by Pacific Knowledge Systems Pty. Ltd.
 * Suite 309, 50 Holt St, Surry Hills, NSW, 2010 Australia
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of Pacific Knowledge Systems ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with PKS.
 */
package rippledown.training.kb.functiontest;

import rippledown.kb.test.KBTestProxy;
import rippledown.test.TestBase;
import rippledown.training.SimpleTutorialMessages;
import rippledown.training.TutorialUserStrings;
import rippledown.training.databases.TrainingProjectName;
import rippledown.training.kb.RuleBuildingPractice;
import rippledown.training.test.UITutorial;
import rippledown.ui.UserStrings;
import rippledown.util.Assert;

/**
 * @author Tim Lavers
 */
public class TutorialTextConditions extends TutorialTestBase {

    private final static UserStrings us = TutorialUserStrings.instance();

    @Override
    public boolean runTutorial() throws Exception {
        KBTestProxy kbProxy = TestBase.startKBClient();

        //Open the training project from the Help menu
        UITutorial uiTutorial = kbProxy.helpTutorials();
        TestBase.pause();
        uiTutorial.start(SimpleTutorialMessages.TEXT_CONDITIONS);

        //Check that the training project is opened
        kbProxy.waitForProjectToBeOpen(TrainingProjectName.RuleBuildingPractice.name());
        Assert.aequals(us.menuItemLabel(TrainingProjectName.RuleBuildingPractice.key()), uiTutorial.tutorialName());

        //Check that all the steps indicated can be performed in the sequence indicated.
        uiTutorial.run(new RuleBuildingPractice(null));

        //The following check shows that the rule was built, i.e. the steps were all done
        kbProxy.wCL().selectIndex(1);
        kbProxy.cView().waitForCaseInCaseView("Case1");
        Assert.aequals("Normal glucose tolerance.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(2);
        kbProxy.cView().waitForCaseInCaseView("Case2");
        Assert.aequals("Normal glucose tolerance.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(3);
        kbProxy.cView().waitForCaseInCaseView("Case3");
        Assert.aequals("Impaired glucose tolerance.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(4);
        kbProxy.cView().waitForCaseInCaseView("Case4");
        Assert.aequals("Diabetes mellitus.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(5);
        kbProxy.cView().waitForCaseInCaseView("Case5");
        Assert.aequals("Diabetes mellitus.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(6);
        kbProxy.cView().waitForCaseInCaseView("Case6");
        Assert.aequals("Impaired fasting glycaemia.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(7);
        kbProxy.cView().waitForCaseInCaseView("Case7");
        Assert.aequals("Impaired glucose tolerance.", kbProxy.reportView().report());

        kbProxy.wCL().selectIndex(8);
        kbProxy.cView().waitForCaseInCaseView("Case8");
        Assert.aequals("", kbProxy.reportView().report());

        kbProxy.exit();
        return true;
    }

}